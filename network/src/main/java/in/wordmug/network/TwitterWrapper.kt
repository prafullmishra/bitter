package `in`.wordmug.network

import android.util.Log
import android.util.Xml
import okio.ByteString
import timber.log.Timber
import java.lang.StringBuilder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.security.SecureRandom
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.sign

private const val CONSUMER_KEY = "Etb4y4XnXI66TpRcgcfhmUmos"
private const val CONSUMER_SEC = "70o8QKYTRkybZBS7q0wqA6z9kSEipLHl1TRcznVrIrjekaHDod"

class TwitterWrapper private constructor(private var token: String, private var verifier: String) {

    //only Singleton object of this wrapper will be used always

    companion object{
        @Volatile
        private var INSTANCE: TwitterWrapper? = null
        fun getInstance(token: String, verifier: String) = INSTANCE ?: synchronized(this){
                INSTANCE ?: TwitterWrapper(token, verifier).also { INSTANCE = it }
            }
    }

    init {
        Timber.i("init values $token, $verifier")
    }

    fun updateCredentials(token2: String, verifier2: String)
    {
        token = token2
        verifier = verifier2
        Timber.i("updated credentials are $token, $verifier")
    }

    private var comparator: HeaderComparator = HeaderComparator()

    suspend fun convertToAccessToken(authVerifier: String): String
    {
        return TwitterApi.retrofitService.convertToAcessToken(verifier, getSignedHeaders("POST","https://api.twitter.com/oauth/access_token", HashMap(), authVerifier)).await().string()
    }

    suspend fun changeToAccessToken(authVerifier: String): String
    {
        return TwitterApi.retrofitService.convertToAcessToken(verifier, getSignedHeadersForToken("POST", "https://api.twitter.com/oauth/access_token", HashMap(), authVerifier)).await().string()
    }

    suspend fun getTimeline(count: Int = 50): String
    {
        val params = HashMap<String,String>()
        params["count"] = count.toString()
        params["tweet_mode"] = "extended"
        return TwitterApi.retrofitService.getTimeline(getSignedHeaders("GET","https://api.twitter.com/1.1/statuses/home_timeline.json", params), count.toString(), "extended").await().string()
    }

    suspend fun getTimeLineMore(maxId: String, count: Int = 50): String
    {
        val params = HashMap<String, String>()
        params["count"] = count.toString()
        params["max_id"] = maxId
        params["tweet_mode"] = "extended"
        return TwitterApi.retrofitService.getTimelineMore(getSignedHeaders("GET","https://api.twitter.com/1.1/statuses/home_timeline.json", params), count.toString(), maxId, "extended").await().string()
    }

    suspend fun createFavorite(statusId: String): String
    {
        val params = HashMap<String,String>()
        params["id"] = statusId
        return TwitterApi.retrofitService.createFavorite(getSignedHeaders("POST", "https://api.twitter.com/1.1/favorites/create.json", params), statusId).await().string()
    }

    suspend fun removeFavorite(statusId: String): String
    {
        val params = HashMap<String,String>()
        params["id"] = statusId
        return TwitterApi.retrofitService.removeFavorite(getSignedHeaders("POST", "https://api.twitter.com/1.1/favorites/destroy.json", params), statusId).await().string()
    }

    suspend fun retweetStatus(statusId: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/statuses/retweet/$statusId.json"
        return TwitterApi.retrofitService.retweetStatus(getSignedHeaders("POST", tempurl, HashMap()), statusId).await().string()
    }

    suspend fun unRetweetStatus(statusId: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/statuses/unretweet/$statusId.json"
        return TwitterApi.retrofitService.unretweetStatus(getSignedHeaders("POST", tempurl, HashMap()), statusId).await().string()
    }

    suspend fun getTrends(woeid: String = "1"): String
    {
        val tempurl = "https://api.twitter.com/1.1/trends/place.json"
        val params = HashMap<String,String>()
        params["id"] = woeid
        return TwitterApi.retrofitService.getTrends(getSignedHeaders("GET", tempurl, params), woeid).await().string()
    }


    suspend fun getUserTimeline(userId: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/statuses/user_timeline.json"
        val params = HashMap<String,String>()
        params["user_id"] = userId
        params["count"] = "50"
        params["tweet_mode"] = "extended"
        return TwitterApi.retrofitService.getUserTimeLine(getSignedHeaders("GET", tempurl, params), userId, "50", "extended").await().string()
    }

    suspend fun getStatusFromId(statusId: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/statuses/lookup.json"
        val params = HashMap<String,String>()
        params["id"] = statusId
        params["tweet_mode"] = "extended"
        return TwitterApi.retrofitService.getStatusFromId(getSignedHeaders("GET", tempurl, params), statusId, "extended").await().string()
    }

    suspend fun getStatusReplies(userHandle: String, sinceId: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/search/tweets.json"
        val params = HashMap<String,String>()
        params["q"]         = "to:$userHandle"
        params["since_id"]  = sinceId
        params["tweet_mode"]= "extended"
        params["count"]     = "50"
        return TwitterApi.retrofitService.getRepliesToStatus(getSignedHeaders("GET", tempurl, params), params["q"]?:"", sinceId, "extended","50").await().string()
    }

    suspend fun searchForTweets(query: String, maxId: String = ""): String
    {
        val tempurl = "https://api.twitter.com/1.1/search/tweets.json"
        val params  = HashMap<String,String>()
        params["count"] = "2"
        params["q"] = query
        params["result_type"] = "recent"
        params["max_id"] = maxId
        params["tweet_mode"] = "extended"
        return TwitterApi.retrofitService.searchForTweets(getSignedHeaders("GET", tempurl, params), query, "extended", maxId, "2", "recent").await().string()
    }

    suspend fun getUser(screenName: String): String
    {
        val tempurl = "https://api.twitter.com/1.1/users/show.json"
        val params = HashMap<String, String>()
        params.put("screen_name", screenName)
        return TwitterApi.retrofitService.getUser(getSignedHeaders("GET", tempurl, params), screenName).await().string()
    }


    private fun getSignedHeaders(method: String, url: String, params: HashMap<String,String> = HashMap(), authVerifier: String = "") : String
    {
        //Timber.i("token is $token")
        //Timber.i("verifier is $verifier")
        val fields = ArrayList<Header>()

        fields.add(Header("oauth_consumer_key",CONSUMER_KEY))
        fields.add(Header("oauth_nonce",getNonce()))
        fields.add(Header("oauth_signature_method","HMAC-SHA1"))
        fields.add(Header("oauth_timestamp",getTimestamp()))
        fields.add(Header("oauth_token",token))
        fields.add(Header("oauth_version","1.0"))

        //Timber.i("before adding $fields")

        val iterator = params.iterator()
        while(iterator.hasNext())
        {
            val pair = iterator.next()
            //Timber.i("Adding ${pair.key} => ${pair.value}")
            fields.add(Header(pair.key,pair.value))
        }
        //Timber.i("after adding $fields")

        Collections.sort(fields, comparator)
        //Timber.i("after sorting $fields")

        val result = StringBuilder()
        for(i in 0 until fields.size)
        {
            if(fields[i].key != "oauth_signature")
            {
                result.append(percentEncode(fields[i].key))
                result.append("=")
                result.append(percentEncode(fields[i].value))

                if(i<fields.size-1) result.append("&")
            }
        }

        //Timber.i("partial result is ${result.toString()}")
        val baseString = StringBuilder()
        baseString.append("$method&${percentEncode(url)}&")
        baseString.append(percentEncode(result.toString()))

        val key = if(authVerifier.isNotEmpty()) "$CONSUMER_SEC&$authVerifier"
                    else "$CONSUMER_SEC&$verifier"
        val signature = applyHMAC(baseString.toString(), key)

        Log.i("fields are:",fields.toString())
        //fields[2].value = signature
        fields.add(Header("oauth_signature", signature))
        Collections.sort(fields, comparator)

        Log.i("fields are now:",fields.toString())

        val header = StringBuilder()
        header.append("OAuth ")
        for(i in 0 until fields.size)
        {
            if(fields[i].key.startsWith("oauth_"))
            {
                header.append(percentEncode(fields[i].key))
                header.append("=\"")
                header.append(percentEncode(fields[i].value))
                header.append("\"")

                if(i < fields.size-1 && fields[i+1].key.startsWith("oauth_")) header.append(", ")
            }
        }
        Log.i("final header:",header.toString())
        return header.toString()
    }


    private fun getSignedHeadersForToken(method: String, url: String, params: HashMap<String,String> = HashMap(), authVerifier: String) : String
    {
        //Timber.i("token is $token")
        //Timber.i("verifier is $verifier")
        val fields = ArrayList<Header>()

        fields.add(Header("oauth_consumer_key",CONSUMER_KEY))
        fields.add(Header("oauth_nonce",getNonce()))
        fields.add(Header("oauth_signature_method","HMAC-SHA1"))
        fields.add(Header("oauth_timestamp",getTimestamp()))
        fields.add(Header("oauth_token",token))
        fields.add(Header("oauth_version","1.0"))

        //Timber.i("before adding $fields")

        val iterator = params.iterator()
        while(iterator.hasNext())
        {
            val pair = iterator.next()
            //Timber.i("Adding ${pair.key} => ${pair.value}")
            fields.add(Header(pair.key,pair.value))
        }
        //Timber.i("after adding $fields")

        Collections.sort(fields, comparator)
        //Timber.i("after sorting $fields")

        val result = StringBuilder()
        for(i in 0 until fields.size)
        {
            if(fields[i].key != "oauth_signature")
            {
                result.append(percentEncode(fields[i].key))
                result.append("=")
                result.append(percentEncode(fields[i].value))

                if(i<fields.size-1) result.append("&")
            }
        }

        //Timber.i("partial result is ${result.toString()}")
        val baseString = StringBuilder()
        baseString.append("$method&${percentEncode(url)}&")
        baseString.append(percentEncode(result.toString()))

        val key = "$CONSUMER_SEC&$authVerifier"
        val signature = applyHMAC(baseString.toString(), key)

        Log.i("fields are:",fields.toString())
        //fields[2].value = signature
        fields.add(Header("oauth_signature", signature))
        Collections.sort(fields, comparator)

        Log.i("fields are now:",fields.toString())

        val header = StringBuilder()
        header.append("OAuth ")
        for(i in 0 until fields.size)
        {
            if(fields[i].key.startsWith("oauth_"))
            {
                header.append(percentEncode(fields[i].key))
                header.append("=\"")
                header.append(percentEncode(fields[i].value))
                header.append("\"")

                if(i < fields.size-1 && fields[i+1].key.startsWith("oauth_")) header.append(", ")
            }
        }
        Log.i("final header:",header.toString())
        return header.toString()
    }


    /**
     * Utility functions which will be used by methods for generating headers(defined above).
     */

    private fun getNonce():String
    {
        val RAND = SecureRandom()
        return (System.nanoTime()).toString() + (abs(RAND.nextLong())).toString()
        //return "102964287396885698378283556054176"
    }

    private fun getTimestamp(): String
    {
        return (System.currentTimeMillis() / 1000).toString()
    }

    private fun percentEncode(s: String?): String
    {

        //this function is from TwitterCore library

        if (s == null) {
            return ""
        }
        val sb = StringBuilder()
        val encoded = URLEncoder.encode(s/*, Xml.Encoding.UTF_8.name*/)
        val encodedLength = encoded.length
        var i = 0
        while (i < encodedLength) {
            val c = encoded[i]
            if (c == '*') {
                sb.append("%2A")
            } else if (c == '+') {
                sb.append("%20")
            } else if (c == '%' && i + 2 < encodedLength &&
                encoded[i + 1] == '7' &&
                encoded[i + 2] == 'E'
            ) {
                sb.append('~')
                i += 2
            } else {
                sb.append(c)
            }
            i++
        }
        return sb.toString()
    }

    private fun applyHMAC(base: String, key:String) : String
    {
        Log.i("received params: ", "||$base<||>$key||")
        val baseBytes   = base.toByteArray(Charset.forName("utf-8"))
        val keyBytes    = key.toByteArray(Charset.forName("utf-8"))
        val secretkey   = SecretKeySpec(keyBytes, "HmacSHA1")
        val mac         = Mac.getInstance("HmacSHA1")

        mac.init(secretkey)
        val signatureBytes = mac.doFinal(baseBytes)

        return ByteString.of(signatureBytes, 0, signatureBytes.size).base64()
    }



}