package wombat.joshattic.us

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody.Companion.toResponseBody

class WombatApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("Accept", "image/svg+xml,image/*;q=0.8")
                            .build()
                        val response = chain.proceed(request)
                        val contentType = response.body?.contentType()
                        if (contentType?.toString()?.contains("svg") == true) {
                            val bodyString = response.body?.string()
                            if (bodyString != null) {
                                val fixedBodyString = bodyString
                                    .replace("rx=\"undefined\"", "rx=\"0\"")
                                    .replace(Regex("""mask="url\(#.*?\)""""), "")
                                val newBody = fixedBodyString.toResponseBody(contentType)
                                return@addInterceptor response.newBuilder().body(newBody).build()
                            }
                        }
                        response
                    }
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
    }
}
