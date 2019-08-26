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
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class ViewTweetViewModel(application: Application, tweetId: String) : BaseViewModel(application), TweetActionInterface {

    private var sp: SharedPreferences = getSP(application.applicationContext)
    private var wrapper = TwitterWrapper.getInstance(sp.getString(SP_OAUTH_TOKEN,"")?:"", sp.getString(SP_OAUTH_SECRET,"")?:"")
    private val color = ContextCompat.getColor(application, R.color.colorAccent)


    private val partialStatus = MutableLiveData<Int>()
    private val status = MutableLiveData<Int>()
    private val currentLink = MutableLiveData<String>()
    private val shouldRefresh = MutableLiveData<Boolean>()
    private val locale = Locale.getDefault() ?: Locale.US
    private val today       = Date()
    private val parser      = SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", locale)

    lateinit var currentTweet: Tweet
    val replies = ArrayList<Tweet>()

    val _partialStatus: LiveData<Int>
        get() = partialStatus

    val _status: LiveData<Int>
        get() = status

    val _shouldRefresh: LiveData<Boolean>
        get() = shouldRefresh

    val _currentLink: LiveData<String>
        get() = currentLink

    init {
        status.value = STATUS_INIT
        partialStatus.value = STATUS_INIT
        getTweetAndReplies(tweetId)
    }

    fun refreshDone()
    {
        shouldRefresh.value = false
    }

    override fun toggleLike(pos: Int)
    {
        Timber.i("called for $pos")
        Timber.i("before was ${replies[pos].favCount}")
        if(replies[pos].isLiked)
        {
            replies[pos].isLiked = false
            replies[pos].favCount--
            scope.launch {
                try {
                    val resp = wrapper.removeFavorite(replies[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: Exception)
                {
                    Timber.i("toggle failed reverting")
                    replies[pos].isLiked = true
                    replies[pos].favCount++
                    shouldRefresh.value = true
                }
            }
        }
        else
        {
            replies[pos].isLiked = true
            replies[pos].favCount++
            scope.launch {
                try {
                    val resp = wrapper.createFavorite(replies[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: Exception)
                {
                    Timber.i("toggle failed, reverting")
                    replies[pos].isLiked = false
                    replies[pos].favCount--
                    shouldRefresh.value = true
                }
            }
        }
        Timber.i("now is ${replies[pos].favCount}")
        shouldRefresh.value = true
    }

    override fun toggleRetweet(pos: Int)
    {
        Timber.i("called for $pos")
        if(replies[pos].isFurtherRetweeted)
        {
            replies[pos].isFurtherRetweeted = false
            replies[pos].retweetCount--
            scope.launch {
                try {
                    val resp = wrapper.unRetweetStatus(replies[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: Exception)
                {
                    Timber.i("toggle failed reverting")
                    //revert back changes
                    replies[pos].isFurtherRetweeted = false
                    replies[pos].retweetCount++
                    shouldRefresh.value = true
                }
            }
        }
        else
        {
            replies[pos].isFurtherRetweeted = true
            replies[pos].retweetCount++
            scope.launch {
                try {
                    val resp = wrapper.retweetStatus(replies[pos].id)
                    Timber.i("response is $resp")
                }
                catch (e: Exception)
                {
                    Timber.i("toggle failed, reverting")
                    //revert back changes
                    replies[pos].isFurtherRetweeted = false
                    replies[pos].retweetCount--
                    shouldRefresh.value = true
                }
            }
        }
        shouldRefresh.value = true
    }

    private fun getTweetAndReplies(statusId: String)
    {
        if(status.value == STATUS_INIT)
        {
            scope.launch {
                try {
                    status.value = STATUS_WAITING
                    partialStatus.value = STATUS_WAITING
                    val resp = wrapper.getStatusFromId(statusId)

                    currentTweet = getParsedTweet(JSONArray(resp).getJSONObject(0),today,parser, currentLink,locale, color)
                    partialStatus.value = STATUS_SUCCESS

                    val resp2 = wrapper.getStatusReplies(currentTweet.user.handle, currentTweet.id)
                    //Timber.i("replies are $resp2")

                    val array = JSONObject(resp2).getJSONArray("statuses")

                    for(i in 0 until array.length())
                    {
                        val t = array.getJSONObject(i)
                        if(t.getString("in_reply_to_status_id_str") == currentTweet.id)
                        {
                            replies.add(getParsedTweet(t, today, parser, currentLink, locale, color))
                            Timber.i("tweet by ${replies[replies.size-1].user.name} is ${replies[replies.size-1].isLiked}")
                        }
                    }
                    status.value = STATUS_SUCCESS
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    if(e is JSONException)
                    {
                        status.value = STATUS_DATA_ERROR
                    }
                    else
                    {
                        status.value = STATUS_NETWORK_ERROR
                    }
                }
            }
        }
    }

}
