package com.example.modu.di

import android.content.Context
import androidx.room.Room
import com.example.modu.data.dataSource.local.demo.database.ModuDemoDatabase
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
        ).build()
    }
}