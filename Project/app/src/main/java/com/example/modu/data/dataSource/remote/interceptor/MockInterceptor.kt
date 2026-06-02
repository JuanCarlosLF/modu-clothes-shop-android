package com.example.modu.data.dataSource.remote.interceptor

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        val method = request.method

        val mockResponseJson: String? = when {

            (url.contains("cart/addItem") && method == "POST") -> {
                """
                {
                  "deviceId": "10000abcdef00001",
                  "createdAt": "2026-01-15T10:30:00Z",
                  "updatedAt": "2026-01-15T10:42:15Z",
                  "subTotalPrice": 89.98,
                  "shippingCosts": 0,
                  "totalPrice": 89.98,
                  "cartItems": [
                    {
                      "id": 1,
                      "productId": 1,
                      "productVariantId": 1,
                      "currentStock": 25,
                      "quantity": 2,
                      "unitPrice": 29.99,
                      "totalPrice": 59.98
                    },
                    {
                      "id": 2,
                      "productId": 1,
                      "productVariantId": 3,
                      "currentStock": 10,
                      "quantity": 1,
                      "unitPrice": 30.00,
                      "totalPrice": 30.00
                    }
                  ]
                }
                """.trimIndent()
            }

            (url.matches(Regex(".*cart/items/\\d+")) && method == "DELETE") -> {
                """
                {
                  "deviceId": "10000abcdef00001",
                  "createdAt": "2026-01-15T10:30:00Z",
                  "updatedAt": "2026-01-15T10:42:15Z",
                  "subTotalPrice": 59.98,
                  "shippingCosts": 0,
                  "totalPrice": 59.98,
                  "cartItems": [
                    {
                      "id": 1,
                      "productId": 1,
                      "productVariantId": 1,
                      "currentStock": 25,
                      "quantity": 2,
                      "unitPrice": 29.99,
                      "totalPrice": 59.98
                    }
                  ]
                }
                """.trimIndent()
            }

            (url.endsWith("cart/items") && method == "DELETE") -> {
                """
                {
                  "deviceId": "10000abcdef00001",
                  "createdAt": "2026-01-15T10:30:00Z",
                  "updatedAt": "2026-01-15T10:42:15Z",
                  "subTotalPrice": 0.00,
                  "shippingCosts": 0,
                  "totalPrice": 0.00,
                  "cartItems": []
                }
                """.trimIndent()
            }

            (url.contains("cart/update") && method == "PATCH") ||
                    (url.endsWith("cart") && method == "GET") ||
                    (url.endsWith("cart") && method == "PUT") -> {
                """
                {
                  "cartSummary": {
                    "deviceId": "10000abcdef00001",
                    "createdAt": "2026-01-15T10:30:00Z",
                    "updatedAt": "2026-01-15T10:42:15Z",
                    "subTotalPrice": 89.98,
                    "shippingCosts": 0,
                    "totalPrice": 89.98,
                    "cartItems": [
                      {
                        "id": 1,
                        "productId": 1,
                        "productVariantId": 1,
                        "currentStock": 25,
                        "quantity": 2,
                        "unitPrice": 29.99,
                        "totalPrice": 59.98
                      },
                      {
                        "id": 2,
                        "productId": 1,
                        "productVariantId": 3,
                        "currentStock": 10,
                        "quantity": 1,
                        "unitPrice": 30.00,
                        "totalPrice": 30.00
                      }
                    ]
                  },
                  "priceChangedAlert": {
                    "cartItems": [
                      {
                        "productVariantId": 1,
                        "oldPrice": 39.99,
                        "newPrice": 29.99
                      }
                    ]
                  },
                  "insufficientStockAlert": null,
                  "variantAvailabilityAlert": null
                }
                """.trimIndent()
            }

            (url.contains("categories") && method == "GET") -> {
                """
                [
                  { "name": "Ropa" },
                  { "name": "Zapatos" },
                  { "name": "Accesorios" }
                ]
                """.trimIndent()
            }

            (url.contains("products") && !url.matches(Regex(".*products/\\d+.*")) && method == "GET") -> {
                """
                {
                  "data": [
                    { "product_id": 1, "url": "https://picsum.photos/200/300" },
                    { "product_id": 2, "url": "https://picsum.photos/200/301" },
                    { "product_id": 3, "url": "https://picsum.photos/200/302" }
                  ],
                  "meta": {
                    "page": 1,
                    "size": 20,
                    "has_next": false
                  }
                }
                """.trimIndent()
            }

            (url.matches(Regex(".*products/\\d+.*")) && method == "GET") -> {
                """
                {
                  "id": 1,
                  "name": "Camiseta Básica Mock",
                  "description": "Una camiseta de prueba",
                  "imageUrl": "https://picsum.photos/200/300",
                  "price": 29.99,
                  "categoriesSet": [
                    { "name": "Ropa" },
                    { "name": "Novedades" }
                  ],
                  "productVariantsList": [
                    {
                      "id": 1,
                      "name": "Camiseta Básica Mock - M - Rojo",
                      "size": "M",
                      "color": "Rojo",
                      "stock": 25,
                      "active": true,
                      "productId": 1
                    },
                    {
                      "id": 3,
                      "name": "Camiseta Básica Mock - L - Azul",
                      "size": "L",
                      "color": "Azul",
                      "stock": 10,
                      "active": true,
                      "productId": 1
                    }
                  ]
                }
                """.trimIndent()
            }

            else -> null
        }

        return if (mockResponseJson != null) {
            Response.Builder()
                .code(200)
                .message("OK")
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .body(mockResponseJson.toResponseBody("application/json".toMediaTypeOrNull()))
                .addHeader("content-type", "application/json")
                .build()
        } else {
            chain.proceed(request)
        }
    }
}