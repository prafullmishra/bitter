package `in`.wordmug.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

private val BASE_API = "http://www.wordmug.in/"

private val retrofit = Retrofit
                    .Builder()
                    .addCallAdapterFactory(CoroutineCallAdapterFactory()).baseUrl(BASE_API).build()

interface ApiService
{
    @GET("script.php")
    fun getInitToken(@Query("mode") mode: String = "1") : Deferred<ResponseBody>


}

object WebApi{
    val retrofitService : ApiService by lazy { retrofit.create(ApiService::class.java) }
}