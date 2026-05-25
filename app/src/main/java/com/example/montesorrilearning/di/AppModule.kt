package com.example.montesorrilearning.di

import android.content.Context
import androidx.room.Room
import com.example.montesorrilearning.data.local.AppDatabase
import com.example.montesorrilearning.data.local.PendingUploadDao
import com.example.montesorrilearning.data.remote.ApiService
import com.example.montesorrilearning.data.remote.AuthInterceptor
import com.example.montesorrilearning.data.remote.SocketManager
import com.example.montesorrilearning.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private const val BASE_URL = "http://10.0.2.2:3000/"

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        apiService: Provider<ApiService>
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, apiService)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "montessori_db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providePendingUploadDao(database: AppDatabase): PendingUploadDao {
        return database.pendingUploadDao()
    }

    @Provides
    @Singleton
    fun provideSocketManager(): SocketManager {
        return SocketManager(BASE_URL)
    }
}
