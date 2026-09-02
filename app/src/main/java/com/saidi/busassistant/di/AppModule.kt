package com.saidi.busassistant.di

import android.app.Application
import androidx.room.Room
import com.saidi.busassistant.data.local.AppDatabase
import com.saidi.busassistant.data.local.BusLineDao
import com.saidi.busassistant.data.local.BehaviorLogDao
import com.saidi.busassistant.data.local.CommuteCorridorDao
import com.saidi.busassistant.data.remote.BeijingBusApi
import com.saidi.busassistant.data.repository.BusRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
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
    fun provideCommuteCorridorDao(database: AppDatabase): CommuteCorridorDao {
        return database.commuteCorridorDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        // Add mobile client user-agent headers and connection optimization
        val headerInterceptor = Interceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "BeijingBus/6.0.0 (iPhone; iOS 16.5; Scale/3.00)")
                .header("Accept", "application/json, text/plain, */*")
                .header("Connection", "keep-alive")
                .build()
            chain.proceed(request)
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .retryOnConnectionFailure(true)
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
        commuteCorridorDao: CommuteCorridorDao,
        busApi: BeijingBusApi
    ): BusRepository {
        return BusRepository(busLineDao, behaviorLogDao, commuteCorridorDao, busApi)
    }
}
