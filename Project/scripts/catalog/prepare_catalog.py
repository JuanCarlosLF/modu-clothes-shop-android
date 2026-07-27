from __future__ import annotations

from decimal import Decimal, InvalidOperation
from typing import Any
from urllib.parse import urlsplit


JsonObject = dict[str, Any]

KOTLIN_INT_MAX = 2_147_483_647
KOTLIN_LONG_MAX = 9_223_372_036_854_775_807


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
