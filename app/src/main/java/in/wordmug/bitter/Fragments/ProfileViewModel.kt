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
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileViewModel(application: Application, val user: User) : BaseViewModel(application), TweetActionInterface {

    private var sp: SharedPreferences = getSP(application.applicationContext)
    private val status = MutableLiveData<Int>()
    private val currentLink = MutableLiveData<String>()
    private val shouldRefresh = MutableLiveData<Boolean>()
    private val locale = Locale.getDefault() ?: Locale.US
    private val color = ContextCompat.getColor(application, R.color.colorAccent)
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(SP_OAUTH_SECRET,"")?:"")

    val followingCount  = MutableLiveData<String>()
    val isFollowing     = MutableLiveData<Boolean>()
    val parser          = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.getDefault()?: Locale.US)
    val formatter       = SimpleDateFormat("MMM, yyyy", Locale.getDefault()?:Locale.US)

    val tweetList       = ArrayList<Tweet>()
    val mediaList       = ArrayList<Tweet>()

    val _status : LiveData<Int>
        get() = status

    val _shouldRefresh : LiveData<Boolean>
        get() = shouldRefresh

    init {
        Timber.i("user name is ${user.name}")
        followingCount.value    = getShortenedCount(user.followersCount)
        isFollowing.value       = user.following
        status.value = STATUS_INIT
        getTweetList()
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

    fun getTweetList()
    {
        scope.launch {
            status.value = STATUS_WAITING
            try {
                val today       = Date()
                val parser      = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", locale)

                val resp = wrapper.getUserTimeline(user.id)
                val array = JSONArray(resp)
                Timber.i("Received ${array.length()} profile tweets")
                for(i in 0 until array.length())
                {
                    val tweet = getParsedTweet(array.getJSONObject(i), today, parser, currentLink, locale, color)
                    tweetList.add(tweet)

                    if(tweet.images.size>0 || tweet.gifThumb.isNotEmpty() || tweet.videoThumb.isNotEmpty())
                    {
                        mediaList.add(tweet)
                    }
                }
                status.value = STATUS_SUCCESS
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                status.value = STATUS_NETWORK_ERROR
            }

        }
    }

    fun refreshDone()
    {
        shouldRefresh.value = false
    }

}
