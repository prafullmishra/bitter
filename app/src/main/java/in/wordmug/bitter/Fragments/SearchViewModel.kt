package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataClass.QuoteTweet
import `in`.wordmug.bitter.DataClass.Tweet
import `in`.wordmug.bitter.DataClass.TweetActionInterface
import `in`.wordmug.bitter.DataClass.User
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.bitter.R
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class SearchViewModel(application: Application, query: String) : BaseViewModel(application), TweetActionInterface {


    private val TIME_INTERVAL = 10000L

    private var sp: SharedPreferences = getSP(application.applicationContext)
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(SP_OAUTH_SECRET,"")?:"")
    private val color = ContextCompat.getColor(application, R.color.colorAccent)

    private val status = MutableLiveData<Int>()
    private val addonStatus = MutableLiveData<Int>()
    private val currentLink = MutableLiveData<String>()
    private val shouldRefresh = MutableLiveData<Boolean>()
    private val locale = Locale.getDefault() ?: Locale.US

    private var hasHandlerStarted = false
    private val handler = Handler()
    private val runnable= object: Runnable{
        override fun run() {
            if(status.value != STATUS_WAITING && addonStatus.value != STATUS_WAITING)
            {
                Timber.i("calling loop")
                loadTweets(query)
            }
            else
            {
                Timber.i("ignoring loop")
            }
            handler.postDelayed(this, TIME_INTERVAL)
        }
    }

    private var sinceId: String = ""

    var tweetList   = ArrayList<Tweet>()

    val _status: LiveData<Int>
        get() = status

    val _addonStatus: LiveData<Int>
        get() = addonStatus

    val _shouldRefresh: LiveData<Boolean>
        get() = shouldRefresh

    init {
        status.value = STATUS_INIT
        addonStatus.value = STATUS_INIT
        loadTweets(query)
    }

    override fun onCleared() {
        super.onCleared()
        Timber.i("killing handler now")
        handler.removeCallbacks(runnable)
    }

    override fun toggleLike(pos: Int)
    {
        Timber.i("called for $pos")
        if(tweetList[pos].isLiked)
        {
            tweetList[pos].isLiked = false
            tweetList[pos].favCount--
            scope.launch {
                try {
                    val resp = wrapper.removeFavorite(tweetList[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: java.lang.Exception)
                {
                    Timber.i("toggle failed reverting")
                    //revert back changes
                    tweetList[pos].isLiked = true
                    tweetList[pos].favCount++
                    shouldRefresh.value = true
                }
            }
        }
        else
        {
            tweetList[pos].isLiked = true
            tweetList[pos].favCount++
            scope.launch {
                try {
                    val resp = wrapper.createFavorite(tweetList[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: java.lang.Exception)
                {
                    Timber.i("toggle failed, reverting")
                    //revert back changes
                    tweetList[pos].isLiked = false
                    tweetList[pos].favCount--
                    shouldRefresh.value = true
                }
            }
        }
        shouldRefresh.value = true
    }

    override fun toggleRetweet(pos: Int)
    {
        Timber.i("called for $pos")
        if(tweetList[pos].isFurtherRetweeted)
        {
            tweetList[pos].isFurtherRetweeted = false
            tweetList[pos].retweetCount--
            scope.launch {
                try {
                    val resp = wrapper.unRetweetStatus(tweetList[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: java.lang.Exception)
                {
                    Timber.i("toggle failed reverting")
                    //revert back changes
                    tweetList[pos].isFurtherRetweeted = false
                    tweetList[pos].retweetCount++
                    shouldRefresh.value = true
                }
            }
        }
        else
        {
            tweetList[pos].isFurtherRetweeted = true
            tweetList[pos].retweetCount++
            scope.launch {
                try {
                    val resp = wrapper.retweetStatus(tweetList[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: java.lang.Exception)
                {
                    Timber.i("toggle failed, reverting")
                    //revert back changes
                    tweetList[pos].isFurtherRetweeted = false
                    tweetList[pos].retweetCount--
                    shouldRefresh.value = true
                }
            }
        }
        shouldRefresh.value = true
    }

    private fun loadTweets(query: String)
    {
        scope.launch {


            if(sinceId.isEmpty()) status.value = STATUS_WAITING
            else addonStatus.value = STATUS_WAITING

            try {
                val response = wrapper.searchForTweets(query, sinceId)
                //Timber.i("response with sinceId = $sinceId is $response")
                val array = JSONObject(response).getJSONArray("statuses")

                val today       = Date()
                val parser      = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", locale)

                Timber.i("sinceId is now $sinceId")
                Timber.i("length before addition ${tweetList.size}")

                val tempList = ArrayList<Tweet>()

                for(i in 0 until array.length())
                {
                    val t = array.getJSONObject(i)
                    val tweet = getParsedTweet(t, today, parser, currentLink, locale, color)

                    if(!tweetList.contains(tweet) && !tempList.contains(tweet))
                    {
                        tempList.add(tweet)
                    }
                }

                tempList.addAll(tweetList)
                tweetList = tempList

                Timber.i("length after addition ${tweetList.size}")

                if(sinceId.isEmpty()) status.value = STATUS_SUCCESS
                else addonStatus.value = STATUS_SUCCESS

                sinceId = tweetList[tweetList.size-1].id

                if(!hasHandlerStarted)
                {
                    hasHandlerStarted = true
                    //handler.postDelayed(runnable, TIME_INTERVAL)
                }

            }
            catch (e: Exception)
            {
                if(sinceId.isEmpty()) status.value = STATUS_NETWORK_ERROR
                else addonStatus.value = STATUS_NETWORK_ERROR

                e.printStackTrace()
            }
        }
    }

    fun refreshDone()
    {
        shouldRefresh.value = false
    }

}
