package `in`.wordmug.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.*
import retrofit2.http.Header

private val BASE = "https://api.twitter.com/"

private val retrofit = Retrofit
    .Builder()
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE)
    .build()

interface TwitterService {

    @FormUrlEncoded
    @POST("oauth/access_token")
    fun convertToAcessToken(@Field("oauth_verifier") verifier: String, @Header("Authorization") header: String) : Deferred<ResponseBody>

    @GET("1.1/statuses/home_timeline.json")
    fun getTimeline(@Header("Authorization") header: String, @Query("count") count: String, @Query("tweet_mode") mode: String) : Deferred<ResponseBody>

    @GET("1.1/statuses/home_timeline.json")
    fun getTimelineMore(@Header("Authorization") header: String, @Query("count") count: String, @Query("max_id") maxId: String, @Query("tweet_mode") mode: String) : Deferred<ResponseBody>

    @POST("1.1/favorites/create.json")
    fun createFavorite(@Header("Authorization") header: String, @Query("id") statusId: String): Deferred<ResponseBody>

    @POST("1.1/favorites/destroy.json")
    fun removeFavorite(@Header("Authorization") header: String, @Query("id") statusId: String): Deferred<ResponseBody>

    @POST("1.1/statuses/retweet/{status_id}.json")
    fun retweetStatus(@Header("Authorization") header: String, @Path(value="status_id", encoded = true) statusId: String): Deferred<ResponseBody>

    @POST("1.1/statuses/unretweet/{status_id}.json")
    fun unretweetStatus(@Header("Authorization") header: String, @Path(value="status_id", encoded = true) statusId: String): Deferred<ResponseBody>

    @GET("1.1/statuses/user_timeline.json")
    fun getUserTimeLine(@Header("Authorization") header: String, @Query("user_id") userId: String, @Query("count") count: String, @Query("tweet_mode") tweetMode: String): Deferred<ResponseBody>

    @GET("1.1/trends/place.json")
    fun getTrends(@Header("Authorization") header: String, @Query("id") woeid: String): Deferred<ResponseBody>

    @GET("1.1/statuses/lookup.json")
    fun getStatusFromId(@Header("Authorization") header: String, @Query("id") statusId: String, @Query("tweet_mode") tweetMode: String): Deferred<ResponseBody>

    @GET("1.1/search/tweets.json")
    fun getRepliesToStatus(@Header("Authorization") header: String, @Query("q") query: String, @Query("since_id") sinceId: String, @Query("tweet_mode") mode: String, @Query("count") count: String): Deferred<ResponseBody>

    @GET("1.1/search/tweets.json")
    fun searchForTweets(@Header("Authorization") header: String, @Query("q") query: String, @Query("tweet_mode") mode: String, @Query("since_id") sinceId: String, @Query("count") count: String, @Query("result_type") resultType: String): Deferred<ResponseBody>

    @GET("1.1/users/show.json")
    fun getUser(@Header("Authorization") header: String, @Query("screen_name") screenName: String): Deferred<ResponseBody>

    @FormUrlEncoded
    @POST("1.1/statuses/update.json")
    fun createTweet(@Header("Authorization") header: String, @Field("status") status: String): Deferred<ResponseBody>
}


object TwitterApi{
    val retrofitService : TwitterService by lazy { retrofit.create(TwitterService::class.java) }
}