package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.network.ApiWrapper
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception

class FinalViewModel(application: Application) : BaseViewModel(application) {

    lateinit var apiWrapper: ApiWrapper
    lateinit var twitterWrapper: TwitterWrapper
    lateinit var editor: SharedPreferences.Editor

    var oauthToken = ""
    var oauthSecret = ""

    val initTokenFetched = MutableLiveData<Int>()
    val finalTokenFetched = MutableLiveData<Int>()


    init {
        initTokenFetched.value = STATUS_INIT
        finalTokenFetched.value = STATUS_INIT
        editor = getEditor(application)
        apiWrapper = ApiWrapper()
        fetchInitToken()
    }

    fun fetchInitToken()
    {
        scope.launch {

            try {

                initTokenFetched.value = STATUS_WAITING

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
        Timber.i("URL DATA IS $url")
        val token = url.split("&")[0].split("=")[1]
        val secret= url.split("&")[1].split("=")[1]

        twitterWrapper = TwitterWrapper.getInstance(token, secret)

        scope.launch {

            finalTokenFetched.value = STATUS_WAITING
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
                twitterWrapper.updateCredentials(sptoken, spsecret)
                finalTokenFetched.value = STATUS_SUCCESS
            }
            catch (e: Exception)
            {
                finalTokenFetched.value = STATUS_NETWORK_ERROR
                Timber.i("Request errored!!")
                e.printStackTrace()
            }

        }
    }

}
