import copy
import hashlib
import io
import json
import sys
import tempfile
import unittest
from pathlib import Path
from unittest import mock


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import prepare_catalog

from PIL import Image


def coherent_catalog():
    return {
        "products": [
            {
                "id": 1,
                "name": "Basic Tee",
                "description": "Soft",
                "price": "10.05",
                "imageUrl": "https://img.test/shared.png",
                "active": True,
            },
            {
                "id": 2,
                "name": "Coat",
                "description": "Warm",
                "price": "19.90",
                "imageUrl": "https://img.test/shared.png",
                "active": False,
            },
        ],
        "categories": [
            {"id": 1, "name": "Shirts"},
            {"id": 2, "name": "Accessories"},
        ],
        "productCategories": [
            {"productId": 1, "categoryId": 1},
            {"productId": 2, "categoryId": 2},
        ],
        "productVariants": [
            {
                "id": 1,
                "productId": 1,
                "variantCode": "1_S_BLACK",
                "size": "S",
                "color": "BLACK",
                "stock": 3,
                "active": True,
            },
            {
                "id": 2,
                "productId": 2,
                "variantCode": "2_M_BLUE",
                "size": "M",
                "color": "BLUE",
                "stock": 0,
                "active": False,
            },
        ],
    }


def png_bytes(color=(20, 40, 60), size=(3, 2)):
    output = io.BytesIO()
    Image.new("RGB", size, color).save(output, format="PNG")
    return output.getvalue()


class FakeResponse:
    def __init__(self, body, status=200, content_type="image/png"):
        self.status = status
        self.headers = {"Content-Type": content_type}
        self.stream = io.BytesIO(body)
        self.read_sizes = []

    def read(self, size=-1):
        self.read_sizes.append(size)
        return self.stream.read(size)

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_value, traceback):
        return False


class RedirectResponse(FakeResponse):
    def __init__(self, body, final_url):
        super().__init__(body)
        self.final_url = final_url

    def geturl(self):
        return self.final_url


class ValidatingRedirectHandlerTest(unittest.TestCase):
    def test_rejects_https_to_http_before_parent_can_follow(self):
        handler = prepare_catalog.ValidatingRedirectHandler()
        request = prepare_catalog.Request("https://img.test/source.png")
        with mock.patch.object(
            prepare_catalog.HTTPRedirectHandler, "redirect_request"
        ) as parent_redirect:
            with self.assertRaisesRegex(ValueError, r"HTTPS"):
                handler.redirect_request(
                    request, None, 302, "Found", {}, "http://cdn.test/insecure.png"
                )

        parent_redirect.assert_not_called()

    def test_delegates_normal_https_redirect_to_urllib(self):
        handler = prepare_catalog.ValidatingRedirectHandler()
        request = prepare_catalog.Request("https://img.test/source.png")
        redirected = prepare_catalog.Request("https://cdn.test/final.png")
        with mock.patch.object(
            prepare_catalog.HTTPRedirectHandler,
            "redirect_request",
            return_value=redirected,
        ) as parent_redirect:
            result = handler.redirect_request(
                request, None, 302, "Found", {}, "https://cdn.test/final.png"
            )

        self.assertIs(redirected, result)
        parent_redirect.assert_called_once()


class ValidateCatalogTest(unittest.TestCase):
    def test_accepts_a_coherent_catalog_and_expected_counts(self):
        catalog = coherent_catalog()
        prepare_catalog.validate_catalog(
            catalog,
            {"products": 2, "categories": 2, "productCategories": 2, "productVariants": 2},
        )

    def test_rejects_invalid_catalogs_with_actionable_messages(self):
        def duplicate_product_id(catalog):
            catalog["products"][1]["id"] = 1

        def unknown_category(catalog):
            catalog["productCategories"][0]["categoryId"] = 99

        def unknown_product(catalog):
            catalog["productVariants"][0]["productId"] = 99

        def negative_stock(catalog):
            catalog["productVariants"][0]["stock"] = -1

        def nonpositive_price(catalog):
            catalog["products"][0]["price"] = "0.00"

        def orphan_product(catalog):
            catalog["productCategories"] = [
                {"productId": 2, "categoryId": 1},
                {"productId": 2, "categoryId": 2},
            ]

        def orphan_category(catalog):
            catalog["productCategories"] = [
                {"productId": 1, "categoryId": 1},
                {"productId": 2, "categoryId": 1},
            ]

        def duplicate_relation(catalog):
            catalog["productCategories"].append(copy.deepcopy(catalog["productCategories"][0]))

        def duplicate_variant_code(catalog):
            catalog["productVariants"][1]["variantCode"] = "1_S_BLACK"

        def duplicate_product_size_color(catalog):
            catalog["productVariants"].append(
                {
                    "id": 3,
                    "productId": 1,
                    "variantCode": "1_S_BLACK_ALT",
                    "size": "S",
                    "color": "BLACK",
                    "stock": 1,
                    "active": True,
                }
            )

        def active_product_without_stock(catalog):
            catalog["productVariants"][0]["stock"] = 0

        cases = [
            ("duplicate product id", duplicate_product_id, r"duplicate.*product.*1"),
            ("unknown category", unknown_category, r"category.*99"),
            ("unknown product", unknown_product, r"product.*99"),
            ("negative stock", negative_stock, r"stock.*-1"),
            ("nonpositive price", nonpositive_price, r"price.*0\.00"),
            ("orphan product", orphan_product, r"product.*1.*categor"),
            ("orphan category", orphan_category, r"categor.*2.*product"),
            ("duplicate relation", duplicate_relation, r"duplicate.*product.*categor.*1"),
            ("duplicate variantCode", duplicate_variant_code, r"variantCode.*1_S_BLACK"),
            ("duplicate size/color", duplicate_product_size_color, r"product.*1.*S.*BLACK"),
            ("active product without stock", active_product_without_stock, r"active.*product.*1.*stock"),
        ]
        for name, mutate, message_pattern in cases:
            with self.subTest(name=name):
                catalog = coherent_catalog()
                mutate(catalog)
                with self.assertRaisesRegex(ValueError, message_pattern):
                    prepare_catalog.validate_catalog(catalog)

    def test_accepts_kotlin_numeric_upper_bounds(self):
        catalog = coherent_catalog()
        int_max = 2_147_483_647
        catalog["products"][0]["id"] = int_max
        catalog["products"][0]["price"] = "92233720368547758.07"
        catalog["productCategories"][0]["productId"] = int_max
        catalog["productVariants"][0]["id"] = int_max
        catalog["productVariants"][0]["productId"] = int_max
        catalog["productVariants"][0]["stock"] = int_max

        prepare_catalog.validate_catalog(catalog)

    def test_rejects_values_outside_generator_numeric_bounds_and_boolean_references(self):
        int_overflow = 2_147_483_648

        def product_id_overflow(catalog):
            catalog["products"][0]["id"] = int_overflow
            catalog["productCategories"][0]["productId"] = int_overflow
            catalog["productVariants"][0]["productId"] = int_overflow

        def category_id_overflow(catalog):
            catalog["categories"][0]["id"] = int_overflow
            catalog["productCategories"][0]["categoryId"] = int_overflow

        def variant_id_overflow(catalog):
            catalog["productVariants"][0]["id"] = int_overflow

        cases = [
            ("product ID", product_id_overflow, r"product id.*32-bit"),
            ("category ID", category_id_overflow, r"category id.*32-bit"),
            ("variant ID", variant_id_overflow, r"variant id.*32-bit"),
            (
                "relation boolean product reference",
                lambda catalog: catalog["productCategories"][0].update(productId=True),
                r"productCategories.*productId.*integer",
            ),
            (
                "variant boolean product reference",
                lambda catalog: catalog["productVariants"][0].update(productId=True),
                r"productVariants.*productId.*integer",
            ),
            (
                "stock overflow",
                lambda catalog: catalog["productVariants"][0].update(stock=int_overflow),
                r"stock.*32-bit",
            ),
            (
                "price cents overflow",
                lambda catalog: catalog["products"][0].update(price="92233720368547758.08"),
                r"price.*64-bit",
            ),
        ]
        for name, mutate, message_pattern in cases:
            with self.subTest(name=name):
                catalog = coherent_catalog()
                mutate(catalog)
                with self.assertRaisesRegex(ValueError, message_pattern):
                    prepare_catalog.validate_catalog(catalog)

    def test_rejects_non_https_and_credentialed_image_urls(self):
        for url in (
            "http://img.test/shared.png",
            "https://user:secret@img.test/shared.png",
        ):
            with self.subTest(url=url):
                catalog = coherent_catalog()
                catalog["products"][0]["imageUrl"] = url
                with self.assertRaisesRegex(ValueError, r"HTTPS|credentials"):
                    prepare_catalog.validate_catalog(catalog)

    def test_rejects_blank_or_outer_whitespace_generator_text_fields(self):
        cases = [
            ("product name blank", lambda catalog: catalog["products"][0].update(name="  "), r"name.*blank"),
            ("product name outer", lambda catalog: catalog["products"][0].update(name=" Basic Tee"), r"name.*outer whitespace"),
            ("product description", lambda catalog: catalog["products"][0].update(description="Soft "), r"description.*outer whitespace"),
            ("category name", lambda catalog: catalog["categories"][0].update(name="\t"), r"category.*name.*blank"),
            ("variant code", lambda catalog: catalog["productVariants"][0].update(variantCode=" CODE"), r"variantCode.*outer whitespace"),
            ("variant size", lambda catalog: catalog["productVariants"][0].update(size=" "), r"size.*blank"),
            ("variant color", lambda catalog: catalog["productVariants"][0].update(color="BLACK\n"), r"color.*outer whitespace"),
        ]
        for name, mutate, message_pattern in cases:
            with self.subTest(name=name):
                catalog = coherent_catalog()
                mutate(catalog)
                with self.assertRaisesRegex(ValueError, message_pattern):
                    prepare_catalog.validate_catalog(catalog)

    def test_accepts_canonical_text_with_punctuation_and_accents(self):
        catalog = coherent_catalog()
        catalog["products"][0]["name"] = "L'été O'Brien: Édition #2"
        catalog["products"][0]["description"] = "Soft, warm & durable."
        catalog["categories"][0]["name"] = "Men's / Unisex"
        catalog["productVariants"][0]["variantCode"] = "TEE-S/BLACK#2"

        prepare_catalog.validate_catalog(catalog)


class ImageNamingTest(unittest.TestCase):
    def test_slug_hash_filename_and_slug_byte_limit_are_deterministic(self):
        body = b"decoded image bytes"
        slug = prepare_catalog.product_slug("Extremely long Crème d'été O’Brien " * 20)

        self.assertLessEqual(len(slug.encode("ascii")), prepare_catalog.MAX_SLUG_BYTES)
        self.assertFalse(slug.endswith("-"))
        self.assertEqual(
            f"007-cafe-runner-{hashlib.sha256(body).hexdigest()[:12]}.webp",
            prepare_catalog.image_filename(
                {"id": 7, "name": "Café Runner"}, body, ".webp"
            ),
        )


class DownloadImagesTest(unittest.TestCase):
    def test_shared_url_uses_lowest_product_as_canonical_filename(self):
        body = png_bytes()
        catalog = coherent_catalog()
        catalog["products"] = list(reversed(catalog["products"]))
        expected_name = f"001-basic-tee-{hashlib.sha256(body).hexdigest()[:12]}.png"
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            with mock.patch.object(
                prepare_catalog, "urlopen", return_value=FakeResponse(body)
            ):
                assets = prepare_catalog.download_images(
                    catalog, root / "images", root / "reports", False
                )

        self.assertEqual(
            {"https://img.test/shared.png": f"catalog/images/{expected_name}"},
            assets,
        )

    def test_republishes_valid_cached_image_without_network(self):
        body = png_bytes()
        sha256 = hashlib.sha256(body).hexdigest()
        old_name = "001-basic-tee-aaaaaaaaaaaa.png"
        expected_name = f"001-basic-tee-{sha256[:12]}.png"
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            output_dir.mkdir()
            report_dir.mkdir()
            (output_dir / old_name).write_bytes(body)
            (report_dir / "download-report.json").write_text(
                json.dumps(
                    {
                        "downloads": [
                            {
                                "url": "https://img.test/shared.png",
                                "imageAssetPath": f"catalog/images/{old_name}",
                                "sha256": sha256,
                            }
                        ],
                        "failures": [],
                    }
                ),
                encoding="utf-8",
            )

            with mock.patch.object(prepare_catalog, "urlopen") as opener:
                assets = prepare_catalog.download_images(
                    coherent_catalog(), output_dir, report_dir, False
                )

            opener.assert_not_called()
            self.assertEqual(
                f"catalog/images/{expected_name}",
                assets["https://img.test/shared.png"],
            )
            self.assertEqual(body, (output_dir / expected_name).read_bytes())
            self.assertTrue((output_dir / old_name).exists())

    def test_skips_oversized_cached_image_without_unbounded_read(self):
        body = png_bytes()
        sha256 = hashlib.sha256(body).hexdigest()
        old_name = "001-basic-tee-aaaaaaaaaaaa.png"
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            output_dir.mkdir()
            report_dir.mkdir()
            (output_dir / old_name).write_bytes(body)
            (report_dir / "download-report.json").write_text(
                json.dumps(
                    {
                        "downloads": [
                            {
                                "url": "https://img.test/shared.png",
                                "imageAssetPath": f"catalog/images/{old_name}",
                                "sha256": sha256,
                            }
                        ]
                    }
                ),
                encoding="utf-8",
            )

            with mock.patch.object(prepare_catalog, "MAX_IMAGE_BYTES", len(body) - 1):
                with mock.patch.object(
                    Path, "read_bytes", side_effect=AssertionError("unbounded cache read")
                ):
                    reusable = prepare_catalog._load_reusable_downloads(
                        report_dir / "download-report.json", output_dir
                    )

            self.assertEqual({}, reusable)

    def test_keeps_stale_files_when_any_image_fails(self):
        first_url = "https://img.test/first.png"
        second_url = "https://img.test/second.png"
        catalog = coherent_catalog()
        catalog["products"][0]["imageUrl"] = first_url
        catalog["products"][1]["imageUrl"] = second_url
        first_body = png_bytes((255, 0, 0))
        second_body = png_bytes((0, 255, 0))
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            output_dir.mkdir()
            stale = output_dir / "stale-owned-image.jpg"
            stale.write_bytes(b"stale")

            responses = {
                first_url: FakeResponse(first_body),
                second_url: FakeResponse(second_body, status=503),
            }
            with mock.patch.object(
                prepare_catalog,
                "urlopen",
                side_effect=lambda request, timeout: responses[request.full_url],
            ):
                with self.assertRaisesRegex(ValueError, r"1 image download"):
                    prepare_catalog.download_images(catalog, output_dir, report_dir, False)
            self.assertTrue(stale.exists())

    def test_fetches_unique_url_once_and_writes_valid_image_stable_report_in_chunks(self):
        body = png_bytes()
        response = FakeResponse(body)
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            opener = mock.Mock(return_value=response)
            with mock.patch.object(prepare_catalog, "urlopen", opener):
                assets = prepare_catalog.download_images(
                    coherent_catalog(), output_dir, report_dir, False
                )

            self.assertEqual(1, opener.call_count)
            asset_path = assets["https://img.test/shared.png"]
            image_path = output_dir / Path(asset_path).name
            with Image.open(image_path) as decoded:
                decoded.verify()
            report = json.loads(
                (report_dir / prepare_catalog.DOWNLOAD_REPORT).read_text(encoding="utf-8")
            )
            self.assertEqual(
                {
                    "url": "https://img.test/shared.png",
                    "imageAssetPath": asset_path,
                    "sha256": hashlib.sha256(body).hexdigest(),
                },
                report["downloads"][0],
            )
            self.assertEqual([], report["failures"])
            self.assertFalse(list(output_dir.glob("*.part")))
        self.assertTrue(response.read_sizes)
        self.assertTrue(
            all(0 < size <= prepare_catalog.DOWNLOAD_CHUNK_BYTES for size in response.read_sizes)
        )

    def test_rejects_insecure_final_redirect_url_before_reading_bytes(self):
        response = RedirectResponse(png_bytes(), "http://cdn.test/shared.png")
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            with mock.patch.object(prepare_catalog, "urlopen", return_value=response):
                with self.assertRaisesRegex(ValueError, r"HTTPS"):
                    prepare_catalog.download_images(
                        coherent_catalog(), root / "images", root / "reports", False
                    )

        self.assertEqual([], response.read_sizes)

    def test_rejects_decoded_dimensions_and_total_pixels_over_limits(self):
        body = png_bytes(size=(3, 2))
        cases = [
            ("width", "MAX_IMAGE_WIDTH", 2, r"width|dimensions"),
            ("height", "MAX_IMAGE_HEIGHT", 1, r"height|dimensions"),
            ("pixels", "MAX_IMAGE_PIXELS", 5, r"pixels"),
        ]
        for name, constant, limit, message_pattern in cases:
            with self.subTest(name=name):
                with mock.patch.object(prepare_catalog, constant, limit):
                    with self.assertRaisesRegex(ValueError, message_pattern):
                        prepare_catalog._verify_image_bytes(
                            body, "https://img.test/large.png"
                        )

    def test_attempts_all_unique_urls_and_preserves_last_successful_report_on_failure(self):
        urls = [
            "https://img.test/01-success.png",
            "https://img.test/02-status-failure.png",
            "https://img.test/03-success.png",
            "https://img.test/04-content-failure.png",
        ]
        catalog = coherent_catalog()
        catalog["products"] = [
            {
                **copy.deepcopy(catalog["products"][0]),
                "id": index,
                "imageUrl": url,
            }
            for index, url in enumerate(reversed(urls), start=1)
        ]
        responses = {
            urls[0]: FakeResponse(png_bytes((255, 0, 0))),
            urls[1]: FakeResponse(png_bytes(), status=503),
            urls[2]: FakeResponse(png_bytes((0, 255, 0))),
            urls[3]: FakeResponse(png_bytes(), content_type="text/html"),
        }
        attempted = []

        def open_by_url(request, timeout):
            del timeout
            attempted.append(request.full_url)
            return responses[request.full_url]

        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            report_dir.mkdir()
            successful_report = b'{"lastSuccessful": true}\n'
            (report_dir / "download-report.json").write_bytes(successful_report)
            with mock.patch.object(prepare_catalog, "urlopen", side_effect=open_by_url):
                with self.assertRaisesRegex(ValueError, r"2.*fail"):
                    prepare_catalog.download_images(catalog, output_dir, report_dir, False)

            self.assertEqual(urls, attempted)
            self.assertEqual(
                successful_report,
                (report_dir / "download-report.json").read_bytes(),
            )
            published = sorted(output_dir.glob("*.png"))
            self.assertEqual(2, len(published))
            for image_path in published:
                with Image.open(image_path) as decoded:
                    decoded.verify()
            self.assertFalse(list(output_dir.glob("*.part")))

    def test_oversized_or_bad_response_leaves_no_published_or_partial_image(self):
        cases = [
            ("oversized", FakeResponse(b"x" * 11), 10, r"maximum.*10|10.*bytes"),
            ("content type", FakeResponse(png_bytes(), content_type="text/html"), None, r"content.?type"),
            ("invalid bytes", FakeResponse(b"not an image"), None, r"image|decode|invalid"),
        ]
        for name, response, byte_limit, message_pattern in cases:
            with self.subTest(name=name), tempfile.TemporaryDirectory() as temporary:
                root = Path(temporary)
                output_dir = root / "images"
                patches = [mock.patch.object(prepare_catalog, "urlopen", return_value=response)]
                if byte_limit is not None:
                    patches.append(mock.patch.object(prepare_catalog, "MAX_IMAGE_BYTES", byte_limit))
                with patches[0]:
                    if len(patches) == 2:
                        patches[1].start()
                    try:
                        with self.assertRaisesRegex(ValueError, message_pattern):
                            prepare_catalog.download_images(
                                coherent_catalog(), output_dir, root / "reports", False
                            )
                    finally:
                        if len(patches) == 2:
                            patches[1].stop()
                self.assertFalse(list(output_dir.glob("*")))
                self.assertFalse((root / "reports" / prepare_catalog.DOWNLOAD_REPORT).exists())

    def test_reuses_valid_cache_unless_forced(self):
        body = png_bytes()
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            output_dir = root / "images"
            report_dir = root / "reports"
            with mock.patch.object(
                prepare_catalog, "urlopen", return_value=FakeResponse(body)
            ):
                first = prepare_catalog.download_images(
                    coherent_catalog(), output_dir, report_dir, False
                )
            first_report = (report_dir / prepare_catalog.DOWNLOAD_REPORT).read_bytes()

            with mock.patch.object(prepare_catalog, "urlopen") as cached_opener:
                cached = prepare_catalog.download_images(
                    coherent_catalog(), output_dir, report_dir, False
                )
            cached_opener.assert_not_called()
            self.assertEqual(first, cached)
            self.assertEqual(
                first_report, (report_dir / prepare_catalog.DOWNLOAD_REPORT).read_bytes()
            )

            with mock.patch.object(
                prepare_catalog, "urlopen", return_value=FakeResponse(body)
            ) as forced_opener:
                prepare_catalog.download_images(coherent_catalog(), output_dir, report_dir, True)
            forced_opener.assert_called_once()


if __name__ == "__main__":
    unittest.main()
