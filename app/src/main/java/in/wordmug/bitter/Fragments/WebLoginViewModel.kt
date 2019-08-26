package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.network.ApiWrapper
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class WebLoginViewModel(application: Application) : BaseViewModel(application) {

    lateinit var oauthToken: String
    lateinit var oauthSecret: String
    lateinit var accessToken: String
    lateinit var apiWrapper: ApiWrapper
    lateinit var twitterWrapper: TwitterWrapper
    lateinit var editor: SharedPreferences.Editor

    private val initTokenFetched = MutableLiveData<Int>()
    private val finalTokenFetched = MutableLiveData<Boolean>()

    val _initTokenFetched : LiveData<Int>
        get() = initTokenFetched

    val _finalTokenFetched : LiveData<Boolean>
        get() = finalTokenFetched

    init {
        initTokenFetched.value = 0
        finalTokenFetched.value = false
        editor = getEditor(application)
        apiWrapper = ApiWrapper()
        fetchInitToken()
    }

    fun fetchInitToken()
    {
        scope.launch {

            try {

                Timber.i("fetching token now")
                val response = apiWrapper.getInitToken()
                val parts = response.split("&")

                if(parts.size>=2)
                {
                    oauthToken = parts[0].split("=")[1]
                    oauthSecret= parts[1].split("=")[1]
                    //TODO: save this value in shared pref and move to homescreen
                    initTokenFetched.value = STATUS_DONE
                }
                else
                {
                    initTokenFetched.value = STATUS_FAILED
                }

            }
            catch (e: Exception)
            {
                e.printStackTrace()
            }
        }
    }

    fun convertToken(url: String)
    {
        val token = url.split("&")[0].split("=")[1]
        val secret= url.split("&")[1].split("=")[1]

        twitterWrapper = TwitterWrapper.getInstance(token, secret)

        Timber.i("data to be broken: $url")

        scope.launch {

            try {
                val accessTokenResponse = twitterWrapper.convertToAccessToken(secret)
                //oauth_token=4672984699-aeHEwJnm3ymdv64gD06h5kZ3sf9jGfOUJGZ32mI&oauth_token_secret=7Lhm4QuYOTd6eOSCNxoly66iVt7SqhO6xGixsWiL4roLh&user_id=4672984699&screen_name=prafullmishra09
                Timber.i("Access token conversion response: $accessTokenResponse")
                val parts = accessTokenResponse.split("&")
                val sptoken = parts[0].split("=")[1]
                val spsecret= parts[1].split("=")[1]
                val spuserid= parts[2].split("=")[1]
                val spscreenname= parts[3].split("=")[1]

                editor.apply {
                    putString(SP_OAUTH_TOKEN, sptoken)
                    putString(SP_OAUTH_SECRET, spsecret)
                    putString(SP_USERID, spuserid)
                    putString(SP_SCREEN_NAME, spscreenname)
                    apply()
                }
                Timber.i("calling update credentials")
                twitterWrapper.updateCredentials(sptoken, spsecret)
                finalTokenFetched.value = true
            }
            catch (e: Exception)
            {
                Timber.i("Request errored!!")
                e.printStackTrace()
            }

        }
    }
}
