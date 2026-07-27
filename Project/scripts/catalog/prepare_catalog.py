from __future__ import annotations

import hashlib
import io
import json
import os
import re
import tempfile
import unicodedata
from decimal import Decimal, InvalidOperation
from pathlib import Path
from typing import Any
from urllib.parse import urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener

from PIL import Image


JsonObject = dict[str, Any]

ASSET_PREFIX = "catalog/images"
DOWNLOAD_REPORT = "download-report.json"
HTTP_TIMEOUT_SECONDS = 30
MAX_IMAGE_BYTES = 10 * 1024 * 1024
DOWNLOAD_CHUNK_BYTES = 64 * 1024
MAX_IMAGE_WIDTH = 4096
MAX_IMAGE_HEIGHT = 4096
MAX_IMAGE_PIXELS = 16_000_000
MAX_SLUG_BYTES = 80
KOTLIN_INT_MAX = 2_147_483_647
KOTLIN_LONG_MAX = 9_223_372_036_854_775_807
IMAGE_EXTENSIONS_PATTERN = r"(?:jpg|png|webp|gif)"
DESCRIPTIVE_IMAGE_PATTERN = re.compile(
    rf"^[0-9]{{3,10}}-[a-z0-9]+(?:-[a-z0-9]+)*-[0-9a-f]{{12}}\.{IMAGE_EXTENSIONS_PATTERN}$"
)


class ImageLimitError(ValueError):
    pass


def _required_string(value: Any, path: str) -> str:
    if not isinstance(value, str) or not value.strip():
        raise ValueError(f"{path} must be a nonblank string")
    if value != value.strip():
        raise ValueError(f"{path} must not contain outer whitespace")
    return value


def _validate_image_url(value: str, path: str) -> str:
    try:
        parsed = urlsplit(value)
        hostname = parsed.hostname
        has_credentials = parsed.username is not None or parsed.password is not None
    except (TypeError, ValueError) as error:
        raise ValueError(f"{path} is invalid") from error
    if parsed.scheme != "https" or not hostname:
        raise ValueError(f"{path} must use HTTPS")
    if has_credentials:
        raise ValueError(f"{path} must not contain credentials")
    return value


class ValidatingRedirectHandler(HTTPRedirectHandler):
    def redirect_request(self, request, file_pointer, code, message, headers, new_url):
        _validate_image_url(new_url, "image redirect URL")
        return super().redirect_request(
            request, file_pointer, code, message, headers, new_url
        )


_IMAGE_OPENER = build_opener(ValidatingRedirectHandler())


def urlopen(request: Request, timeout: int):
    return _IMAGE_OPENER.open(request, timeout=timeout)


def _effective_image_url(product: JsonObject) -> str:
    for key in ("sourceImageUrl", "imageUrl", "originalImageUrl"):
        value = product.get(key)
        if isinstance(value, str) and value:
            return _validate_image_url(value, f"product {product.get('id')} image URL")
    raise ValueError(f"product {product.get('id')} has no source image URL")


def _decimal_price(product: JsonObject) -> Decimal:
    value = product.get("price")
    try:
        price = Decimal(str(value))
    except (InvalidOperation, ValueError) as error:
        raise ValueError(f"product {product.get('id')} price {value!r} is not a decimal") from error
    if not price.is_finite() or price <= 0:
        raise ValueError(f"product {product.get('id')} price {value} must be positive")
    cents = price * Decimal(100)
    if cents != cents.to_integral_value():
        raise ValueError(f"product {product.get('id')} price {value} has fractional cents")
    if cents > KOTLIN_LONG_MAX:
        raise ValueError(
            f"product {product.get('id')} price in cents must fit a signed 64-bit Kotlin Long"
        )
    return price


def _kotlin_int(value: Any, path: str, minimum: int) -> int:
    if not isinstance(value, int) or isinstance(value, bool):
        raise ValueError(f"{path} {value!r} must be an integer")
    if value < minimum or value > KOTLIN_INT_MAX:
        raise ValueError(
            f"{path} {value} must be between {minimum} and {KOTLIN_INT_MAX} "
            "and fit a signed 32-bit Kotlin Int"
        )
    return value


def validate_catalog(catalog: dict, expected_counts: dict | None = None) -> None:
    collection_names = ("products", "categories", "productCategories", "productVariants")
    for name in collection_names:
        if not isinstance(catalog.get(name), list):
            raise ValueError(f"catalog {name} must be a list")
    if expected_counts is not None:
        for name in collection_names:
            if name in expected_counts and len(catalog[name]) != expected_counts[name]:
                raise ValueError(
                    f"expected {expected_counts[name]} {name}, found {len(catalog[name])}"
                )

    products = catalog["products"]
    categories = catalog["categories"]
    relations = catalog["productCategories"]
    variants = catalog["productVariants"]

    def unique_ids(rows: list[JsonObject], row_name: str) -> set[int]:
        seen: set[int] = set()
        for row in sorted(rows, key=lambda item: (str(item.get("id")), repr(item))):
            row_id = _kotlin_int(row.get("id"), f"{row_name} id", minimum=1)
            if row_id in seen:
                raise ValueError(f"duplicate {row_name} id {row_id}")
            seen.add(row_id)
        return seen

    product_ids = unique_ids(products, "product")
    category_ids = unique_ids(categories, "category")
    unique_ids(variants, "product variant")

    for product in sorted(products, key=lambda row: row["id"]):
        _required_string(product.get("name"), f"product {product['id']} name")
        _required_string(product.get("description"), f"product {product['id']} description")
        _decimal_price(product)
        _effective_image_url(product)
        if not isinstance(product.get("active"), bool):
            raise ValueError(f"product {product['id']} active must be boolean")
    for category in sorted(categories, key=lambda row: row["id"]):
        _required_string(category.get("name"), f"category {category['id']} name")

    relation_keys: set[tuple[int, int]] = set()
    related_products: set[int] = set()
    related_categories: set[int] = set()
    for relation in sorted(relations, key=lambda row: (row.get("productId", -1), row.get("categoryId", -1))):
        product_id = _kotlin_int(
            relation.get("productId"), "productCategories relation productId", minimum=1
        )
        category_id = _kotlin_int(
            relation.get("categoryId"), "productCategories relation categoryId", minimum=1
        )
        if product_id not in product_ids:
            raise ValueError(f"product reference {product_id} is unknown in product-category relation")
        if category_id not in category_ids:
            raise ValueError(f"category reference {category_id} is unknown in product-category relation")
        key = (product_id, category_id)
        if key in relation_keys:
            raise ValueError(
                f"duplicate product-category relation for product {product_id} and category {category_id}"
            )
        relation_keys.add(key)
        related_products.add(product_id)
        related_categories.add(category_id)

    variant_codes: set[str] = set()
    product_options: set[tuple[int, str, str]] = set()
    available_product_ids: set[int] = set()
    for variant in sorted(variants, key=lambda row: row["id"]):
        product_id = _kotlin_int(
            variant.get("productId"), "productVariants productId", minimum=1
        )
        if product_id not in product_ids:
            raise ValueError(f"product reference {product_id} is unknown in product variant")
        code = _required_string(variant.get("variantCode"), f"variant {variant['id']} variantCode")
        if code in variant_codes:
            raise ValueError(f"duplicate variantCode {code}")
        variant_codes.add(code)
        size = _required_string(variant.get("size"), f"variant {variant['id']} size")
        color = _required_string(variant.get("color"), f"variant {variant['id']} color")
        option = (product_id, size, color)
        if option in product_options:
            raise ValueError(f"duplicate product {product_id} size {size} color {color}")
        product_options.add(option)
        stock = _kotlin_int(variant.get("stock"), f"variant {variant['id']} stock", minimum=0)
        active = variant.get("active")
        if not isinstance(active, bool):
            raise ValueError(f"variant {variant['id']} active must be boolean")
        if active and stock > 0:
            available_product_ids.add(product_id)

    for product_id in sorted(product_ids - related_products):
        raise ValueError(f"product {product_id} has no category relation")
    for category_id in sorted(category_ids - related_categories):
        raise ValueError(f"category {category_id} has no product relation")
    for product in sorted(products, key=lambda row: row["id"]):
        if product["active"] and product["id"] not in available_product_ids:
            raise ValueError(
                f"active product {product['id']} has no active variant with positive stock"
            )


def _json_bytes(value: Any) -> bytes:
    return (json.dumps(value, ensure_ascii=True, indent=2, sort_keys=True) + "\n").encode("utf-8")


def _atomic_write_bytes(path: Path, content: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary_name: str | None = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="wb", dir=path.parent, prefix=f".{path.name}.", suffix=".part", delete=False
        ) as temporary:
            temporary.write(content)
            temporary.flush()
            os.fsync(temporary.fileno())
            temporary_name = temporary.name
        os.replace(temporary_name, path)
    finally:
        if temporary_name is not None:
            Path(temporary_name).unlink(missing_ok=True)


def _write_json_atomic(path: Path, value: Any) -> None:
    _atomic_write_bytes(path, _json_bytes(value))


def _validate_image_dimensions(image: Image.Image, source: str) -> None:
    width, height = image.size
    if width <= 0 or width > MAX_IMAGE_WIDTH:
        raise ImageLimitError(
            f"image width {width} from {source} exceeds valid maximum {MAX_IMAGE_WIDTH}"
        )
    if height <= 0 or height > MAX_IMAGE_HEIGHT:
        raise ImageLimitError(
            f"image height {height} from {source} exceeds valid maximum {MAX_IMAGE_HEIGHT}"
        )
    pixels = width * height
    if pixels > MAX_IMAGE_PIXELS:
        raise ImageLimitError(
            f"image pixels {pixels} from {source} exceed maximum {MAX_IMAGE_PIXELS}"
        )


def _verify_image_bytes(content: bytes, url: str) -> str:
    try:
        with Image.open(io.BytesIO(content)) as image:
            image_format = image.format
            _validate_image_dimensions(image, url)
            image.verify()
    except ImageLimitError:
        raise
    except Exception as error:
        raise ValueError(f"invalid image bytes from {url}: decode failed") from error
    extensions = {"JPEG": ".jpg", "PNG": ".png", "WEBP": ".webp", "GIF": ".gif"}
    if image_format not in extensions:
        raise ValueError(f"unsupported decoded image format {image_format!r} from {url}")
    return extensions[image_format]


def product_slug(name: str) -> str:
    normalized = unicodedata.normalize("NFKD", name).encode("ascii", "ignore").decode("ascii")
    normalized = re.sub(r"['\u2018\u2019]", "", normalized.lower())
    slug = re.sub(r"[^a-z0-9]+", "-", normalized).strip("-")
    if not slug:
        raise ValueError(f"product name {name!r} does not contain ASCII letters or digits")
    return slug[:MAX_SLUG_BYTES].rstrip("-")


def image_filename(product: JsonObject, content: bytes, extension: str) -> str:
    content_sha = hashlib.sha256(content).hexdigest()
    return f"{product['id']:03d}-{product_slug(product['name'])}-{content_sha[:12]}{extension}"


def _read_bounded_file(path: Path, maximum_bytes: int) -> bytes:
    if path.stat().st_size > maximum_bytes:
        raise ValueError(f"file {path} exceeds maximum {maximum_bytes} bytes")
    chunks: list[bytes] = []
    total_bytes = 0
    with path.open("rb") as source:
        while chunk := source.read(DOWNLOAD_CHUNK_BYTES):
            total_bytes += len(chunk)
            if total_bytes > maximum_bytes:
                raise ValueError(f"file {path} exceeds maximum {maximum_bytes} bytes")
            chunks.append(chunk)
    return b"".join(chunks)


def _load_reusable_downloads(report_path: Path, output_dir: Path) -> dict[str, JsonObject]:
    if not report_path.is_file():
        return {}
    try:
        report = json.loads(report_path.read_text(encoding="utf-8"))
        downloads = report["downloads"]
    except (OSError, ValueError, KeyError, TypeError):
        return {}
    reusable: dict[str, JsonObject] = {}
    for entry in downloads:
        try:
            url = entry["url"]
            asset_path = entry["imageAssetPath"]
            expected_sha = entry["sha256"]
            image_path = output_dir / Path(asset_path).name
            content = _read_bounded_file(image_path, MAX_IMAGE_BYTES)
            if hashlib.sha256(content).hexdigest() != expected_sha:
                continue
            extension = _verify_image_bytes(content, url)
            reusable[url] = {**entry, "content": content, "extension": extension}
        except (OSError, KeyError, TypeError, ValueError):
            continue
    return reusable


def download_images(
    catalog: dict, output_dir: Path, report_dir: Path, force: bool
) -> dict[str, str]:
    urls = sorted({_effective_image_url(product) for product in catalog["products"]})
    canonical_products: dict[str, JsonObject] = {}
    for product in sorted(catalog["products"], key=lambda row: (row["id"], row["name"])):
        canonical_products.setdefault(_effective_image_url(product), product)
    output_dir.mkdir(parents=True, exist_ok=True)
    report_dir.mkdir(parents=True, exist_ok=True)
    report_path = report_dir / DOWNLOAD_REPORT
    reusable = {} if force else _load_reusable_downloads(report_path, output_dir)
    downloads: list[JsonObject] = []
    failures: list[JsonObject] = []
    assets: dict[str, str] = {}
    for url in urls:
        existing = reusable.get(url)
        if existing is not None:
            content = existing["content"]
            filename = image_filename(canonical_products[url], content, existing["extension"])
            image_asset_path = f"{ASSET_PREFIX}/{filename}"
            _atomic_write_bytes(output_dir / filename, content)
            entry = {
                "url": url,
                "imageAssetPath": image_asset_path,
                "sha256": existing["sha256"],
            }
            downloads.append(entry)
            assets[url] = image_asset_path
            continue

        try:
            request = Request(url, headers={"Accept": "image/*"})
            with urlopen(request, timeout=HTTP_TIMEOUT_SECONDS) as response:
                get_final_url = getattr(response, "geturl", None)
                final_url = get_final_url() if callable(get_final_url) else request.full_url
                _validate_image_url(final_url, f"final image URL for {url}")
                status = getattr(response, "status", None)
                if status != 200:
                    raise ValueError(f"image download for {url} returned HTTP status {status}")
                content_type = response.headers.get("Content-Type", "")
                if not content_type.lower().split(";", 1)[0].strip().startswith("image/"):
                    raise ValueError(
                        f"image download for {url} has invalid content type {content_type!r}"
                    )
                chunks: list[bytes] = []
                total_bytes = 0
                while chunk := response.read(DOWNLOAD_CHUNK_BYTES):
                    total_bytes += len(chunk)
                    if total_bytes > MAX_IMAGE_BYTES:
                        raise ValueError(
                            f"image download for {url} exceeds maximum {MAX_IMAGE_BYTES} bytes"
                        )
                    chunks.append(chunk)
                content = b"".join(chunks)
            extension = _verify_image_bytes(content, url)
            sha256 = hashlib.sha256(content).hexdigest()
            filename = image_filename(canonical_products[url], content, extension)
            image_asset_path = f"{ASSET_PREFIX}/{filename}"
            _atomic_write_bytes(output_dir / filename, content)
            entry = {"url": url, "imageAssetPath": image_asset_path, "sha256": sha256}
            downloads.append(entry)
            assets[url] = image_asset_path
        except (OSError, ValueError) as error:
            failures.append({"url": url, "error": str(error)})

    if failures:
        details = "; ".join(f"{failure['url']}: {failure['error']}" for failure in failures)
        raise ValueError(f"{len(failures)} image download(s) failed: {details}")
    _write_json_atomic(report_path, {"downloads": downloads, "failures": []})
    return assets


def remove_stale_images(output_dir: Path, downloaded_assets: dict[str, str]) -> None:
    if not output_dir.exists():
        return
    desired_filenames = {Path(asset_path).name for asset_path in downloaded_assets.values()}
    for path in output_dir.iterdir():
        if (
            path.name not in desired_filenames
            and DESCRIPTIVE_IMAGE_PATTERN.fullmatch(path.name)
            and (path.is_file() or path.is_symlink())
        ):
            path.unlink()
