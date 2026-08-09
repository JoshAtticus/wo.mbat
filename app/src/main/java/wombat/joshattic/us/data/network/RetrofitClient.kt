package wombat.joshattic.us.data.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.wasteof.money/"

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        )
        .build()

    // ImgBB can take several minutes to finish processing an upload.
    private val uploadOkHttpClient = okHttpClient.newBuilder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.MINUTES)
        .readTimeout(5, TimeUnit.MINUTES)
        .callTimeout(5, TimeUnit.MINUTES)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    private val uploadRetrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(uploadOkHttpClient)
        .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
    val uploadApiService: ApiService = uploadRetrofit.create(ApiService::class.java)
}
