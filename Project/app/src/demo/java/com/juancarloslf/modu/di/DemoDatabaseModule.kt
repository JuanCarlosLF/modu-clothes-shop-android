package com.juancarloslf.modu.di

import android.content.Context
import androidx.room.Room
import com.juancarloslf.modu.data.dataSource.local.demo.database.ModuDemoDatabase
import com.juancarloslf.modu.data.dataSource.local.demo.database.cart.CartDao
import com.juancarloslf.modu.data.dataSource.local.demo.database.product.ProductDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DEMO_DATABASE_NAME = "modu_demo_database"

@Module
@InstallIn(SingletonComponent::class)
object DemoDatabaseModule {

    @Provides
    @Singleton
    fun provideDemoDatabase(@ApplicationContext context: Context): ModuDemoDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ModuDemoDatabase::class.java,
            name = DEMO_DATABASE_NAME
        )
            .createFromAsset("database/modu_demo_database.db")
            .build()
    }

    @Provides
    @Singleton
    fun provideDemoProductDao(database: ModuDemoDatabase): ProductDao = database.productDao()

    @Provides
    @Singleton
    fun provideDemoCartDao(database: ModuDemoDatabase): CartDao = database.cartDao()
}
