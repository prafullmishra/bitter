package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException

class TrendsViewModel(application: Application) : BaseViewModel(application) {

    private val status = MutableLiveData<Int>()
    private var sp: SharedPreferences = getSP(application.applicationContext)
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(SP_OAUTH_SECRET,"")?:"")
    val trends = ArrayList<Trend>()

    val _status : LiveData<Int>
        get() = status

    init {
        status.value = STATUS_INIT
        getTrends()
    }

    private fun getTrends()
    {
        scope.launch {
            status.value = STATUS_WAITING
            try {
                val resp = wrapper.getTrends("23424848") //Where On Earth Id - 1 is for universal
                val array = JSONArray(resp).getJSONObject(0).getJSONArray("trends")

                for(i in 0 until array.length())
                {
                    val t = array.getJSONObject(i)
                    var subtitle = ""
                    if(t.has("tweet_volume") && !t.isNull("tweet_volume"))
                    {
                        subtitle = "${getShortenedCount(t.getLong("tweet_volume"))} tweets"
                    }
                    if(subtitle.isNotEmpty()) trends.add(Trend(t.getString("name"), subtitle))
                    if(trends.size == 10) break
                }

                status.value = STATUS_DONE
            }
            catch (e: Exception)
            {
                status.value = STATUS_NETWORK_ERROR
                e.printStackTrace()
            }
        }
    }

    data class Trend(val title: String, val subtitle: String)

}
