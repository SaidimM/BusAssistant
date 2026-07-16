package com.saidi.busassistant.di

import android.app.Application
import androidx.room.Room
import com.saidi.busassistant.data.local.AppDatabase
import com.saidi.busassistant.data.local.BusLineDao
import com.saidi.busassistant.data.local.BehaviorLogDao
import com.saidi.busassistant.data.remote.BeijingBusApi
import com.saidi.busassistant.data.repository.BusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(application: Application): AppDatabase {
        return Room.databaseBuilder(
            application,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideBusLineDao(database: AppDatabase): BusLineDao {
        return database.busLineDao()
    }

    @Provides
    @Singleton
    fun provideBehaviorLogDao(database: AppDatabase): BehaviorLogDao {
        return database.behaviorLogDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideBeijingBusApi(okHttpClient: OkHttpClient): BeijingBusApi {
        return Retrofit.Builder()
            .baseUrl(BeijingBusApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BeijingBusApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBusRepository(
        busLineDao: BusLineDao,
        behaviorLogDao: BehaviorLogDao,
        busApi: BeijingBusApi
    ): BusRepository {
        return BusRepository(busLineDao, behaviorLogDao, busApi)
    }
}
