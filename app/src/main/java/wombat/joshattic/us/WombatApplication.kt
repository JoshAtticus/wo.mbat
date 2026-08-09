package wombat.joshattic.us

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import okhttp3.OkHttpClient

class WombatApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36")
                    .build()
                chain.proceed(request)
            }
            .followRedirects(true)
            .followSslRedirects(true)
            .build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .respectCacheHeaders(false)
            .build()
    }
}
