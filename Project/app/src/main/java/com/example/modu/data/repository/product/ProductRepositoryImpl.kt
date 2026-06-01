package com.example.modu.data.repository.product

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.modu.data.dataSource.local.database.cart.CartLocalDataSource
import com.example.modu.data.dataSource.local.database.cart.dbo.CartItemDetailDbo
import com.example.modu.data.dataSource.local.database.cart.dbo.ProductVariantDbo
import com.example.modu.data.dataSource.remote.exception.ErrorHandler
import com.example.modu.data.dataSource.remote.product.ProductDataSource
import com.example.modu.domain.entity.detail.Detail
import com.example.modu.domain.entity.product.Category
import com.example.modu.domain.entity.product.Product
import com.example.modu.domain.repository.product.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val PAGING_DEFAULT_SIZE = 20
private const val PAGING_INITIAL_SIZE = 20

class ProductRepositoryImpl @Inject constructor(
    private val dataSource: ProductDataSource,
    private val localCartDataSource: CartLocalDataSource,
    private val errorHandler: ErrorHandler
) : ProductRepository {

    override fun getProductsBy(
        title: String?,
        orderByPrice: String?,
        maxPrice: Int?,
        categories: List<String>?
    ): Flow<PagingData<Product>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGING_DEFAULT_SIZE,
                initialLoadSize = PAGING_INITIAL_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                dataSource.getPaginatedProducts(
                    title,
                    orderByPrice,
                    maxPrice,
                    categories
                ) { exception ->
                    errorHandler.handle(exception)
                }
            }
        )
            .flow
            .map { pagingDataDto ->
                pagingDataDto.map { productDto ->
                    productDto.toDomain()
                }
            }
    }

    override suspend fun getDetailById(id: Int): Detail {
        return try {
            val dto = dataSource.getDetailById(id)
            val domainModel = dto.toDomain()

            val detailDbo = CartItemDetailDbo(
                id = domainModel.id,
                name = domainModel.name,
                imageUrl = domainModel.imageUrl
            )

            val variantDbos = domainModel.productVariantsList.map {
                ProductVariantDbo(
                    id = it.id,
                    productId = domainModel.id,
                    size = it.size,
                    color = it.color
                )
            }
            localCartDataSource.deleteVariantsByProductId(domainModel.id)
            localCartDataSource.insertProductDetails(listOf(detailDbo))
            localCartDataSource.insertProductVariants(variantDbos)

            domainModel
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun getCategories(): List<Category> {
        return try {
            dataSource.getCategories().map { it.toDomain() }
        } catch (error: Exception) {
            throw errorHandler.handle(error)
        }
    }

    override suspend fun getRelatedProducts(category: List<String>): List<Product> =
        dataSource.getRelatedProducts(category).map { it.toDomain() }
}