package wombat.joshattic.us

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.svg.SvgDecoder
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import wombat.joshattic.us.platform.AndroidPlatformContext

class WombatApplication : Application(), SingletonImageLoader.Factory {
    override fun onCreate() {
        super.onCreate()
        AndroidPlatformContext.init(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        val httpClient = HttpClient {
            followRedirects = true
            defaultRequest {
                header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                )
            }
        }

        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient = { httpClient }))
                add(SvgDecoder.Factory())
            }
            .build()
    }
}
