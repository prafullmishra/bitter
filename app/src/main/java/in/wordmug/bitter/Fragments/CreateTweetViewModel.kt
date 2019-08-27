package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import timber.log.Timber

class CreateTweetViewModel(application: Application) : BaseViewModel(application) {

    private val sp = getSP(application)
    private val status = MutableLiveData<Int>()
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(
        SP_OAUTH_SECRET,"")?:"")

    val _status : LiveData<Int>
        get() = status

    init {
        status.value = STATUS_INIT
    }

    fun uploadTweet(text: String)
    {
        scope.launch {
            status.value = STATUS_WAITING

            try {
                val resp = wrapper.createTweet(text)
                Timber.i("Create tweet response: $resp")
                if(resp.contains("id_str"))
                {
                    //status was successfully created
                    status.value = STATUS_SUCCESS
                }
                else
                {
                    status.value = STATUS_DATA_ERROR
                }
            }
            catch (e: Exception)
            {
                status.value = STATUS_NETWORK_ERROR
                e.printStackTrace()
            }
        }
    }

    fun _statusDoneWith()
    {
        status.value = STATUS_INIT
    }


}
