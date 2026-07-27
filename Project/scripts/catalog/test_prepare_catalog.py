import copy
import sys
import unittest
from pathlib import Path


SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

import prepare_catalog


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


if __name__ == "__main__":
    unittest.main()
