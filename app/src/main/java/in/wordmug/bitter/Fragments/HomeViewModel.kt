package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.BaseViewModel
import `in`.wordmug.bitter.DataClass.QuoteTweet
import `in`.wordmug.bitter.DataClass.Tweet
import `in`.wordmug.bitter.DataClass.TweetActionInterface
import `in`.wordmug.bitter.DataClass.User
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.bitter.MainViewModel
import `in`.wordmug.bitter.R
import `in`.wordmug.network.TwitterWrapper
import android.app.Application
import android.content.SharedPreferences
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
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class HomeViewModel(application: Application) : BaseViewModel(application), TweetActionInterface {

    private var sp: SharedPreferences = getSP(application.applicationContext)
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(SP_OAUTH_SECRET,"")?:"")
    private val color = ContextCompat.getColor(application, R.color.colorAccent)


    val tweetList   = ArrayList<Tweet>()
    val userList    = ArrayList<User>()

    private val status = MutableLiveData<Int>()
    private val profileStatus = MutableLiveData<Int>()
    private val currentLink = MutableLiveData<String>()
    var currentProfile : User? = null
    private val shouldRefresh = MutableLiveData<Boolean>()
    private val locale = Locale.getDefault() ?: Locale.US
    val loadMore = MutableLiveData<Boolean>()

    val _status : LiveData<Int>
        get() = status

    val _profileStatus : LiveData<Int>
        get() = profileStatus

    val _currentLink: LiveData<String>
        get() = currentLink

    val _shouldRefresh: LiveData<Boolean>
        get() = shouldRefresh

    init {
        //fetchTweets()
        currentLink.value = ""
        loadMore.value = false
        refreshDone()
        fetchDataNew()
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
                catch (e: Exception)
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
                catch (e: Exception)
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
                catch (e: Exception)
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
                catch (e: Exception)
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

    fun refreshDone()
    {
        shouldRefresh.value = false
    }

    private fun fetchDataNew()
    {
        scope.launch {
            status.value = STATUS_WAITING

            try {
                val response    = wrapper.getTimeline()
                val array       = JSONArray(response)

                val today       = Date()
                val parser      = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", locale)

                for(i in 0 until array.length())
                {
                    tweetList.add(getParsedTweet(array.getJSONObject(i), today, parser, currentLink, locale, color))
                }

                status.value = STATUS_SUCCESS
            }
            catch (e: Exception)
            {
                status.value = STATUS_NETWORK_ERROR
                e.printStackTrace()
            }
        }
    }

    fun fetchProfile(screenName: String)
    {
        Timber.i("fetching profile $screenName")
        scope.launch {
            profileStatus.value = STATUS_WAITING

            try {
                val response = wrapper.getUser(screenName)
                currentProfile = parseUser(response)
                profileStatus.value = STATUS_SUCCESS
            }
            catch (e: Exception)
            {
                profileStatus.value = STATUS_NETWORK_ERROR
                e.printStackTrace()
            }
        }
    }

    fun _linkDoneWith()
    {
        currentLink.value = ""
    }

}

