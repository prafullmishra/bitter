package `in`.wordmug.bitter.DataUtils

import `in`.wordmug.bitter.DataClass.QuoteTweet
import `in`.wordmug.bitter.DataClass.Tweet
import `in`.wordmug.bitter.DataClass.User
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


val STATUS_DONE = 2
val STATUS_FAILED = 3



const val SP_NAME = "sp"
const val SP_OAUTH_TOKEN = "oauthtoken"
const val SP_OAUTH_SECRET= "oauthsecret"
const val SP_USERID = "userid"
const val SP_SCREEN_NAME = "screenname"

const val STATUS_WAITING = 1
const val STATUS_INIT = 2
const val STATUS_SUCCESS = 3
const val STATUS_NETWORK_ERROR = 4
const val STATUS_DATA_ERROR = 5

fun getSP(context: Context): SharedPreferences
{
    if(context is Activity)
    {
        return context.applicationContext!!.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }
    return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
}

fun getEditor(context: Context): SharedPreferences.Editor
{
    return getSP(context).edit()
}

fun getShortenedCount(number: Long): String
{
    if(number <1000){ return number.toString() }
    else if(number < 1000000) { return "${String.format("%.0f", (number.toDouble() /1000F))}k" }
    else if(number < 1000000000) { return "${String.format("%.1f", (number.toDouble() /1000000F))}M" }
    else { return "${String.format("%.1f", (number.toDouble() /1000000000F))}B" }
}

fun getElapsed(stamp: String, today: Date, parser: SimpleDateFormat, locale: Locale) : String
{
    val sdfShort = SimpleDateFormat("d MMM", locale)
    val sdfExtended = SimpleDateFormat("d MMM yyyy", locale)

    val date = parser.parse(stamp)
    val gap = today.time - date.time

    val nod = TimeUnit.MILLISECONDS.toDays(gap)
    //Timber.i("$stamp -> gap in days is $nod")
    return when {
        nod < 1L -> {
            val hrs = TimeUnit.MILLISECONDS.toHours(gap)
            if(hrs == 1L) return "1h"
            else if(hrs > 1L) return "${hrs}h"
            else
            {
                val mins = TimeUnit.MILLISECONDS.toMinutes(gap)
                if(mins>=1L) return "${mins}m"
                else
                {
                    return "now"
                }
            }
        }
        nod in 1L..6L -> "${nod}d"
        nod < 365L -> sdfShort.format(date)
        else -> sdfExtended.format(date)
    }

}

fun showToast(context: Context, msg: String)
{
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

fun setHTML(view: TextView, text: String)
{
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
    {
        view.text = Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT)
    }
    else
    {
        view.text = Html.fromHtml(text)
    }
}

private fun getClickableSpan(target: MutableLiveData<String>): ClickableSpan
{
    return object : ClickableSpan(){
        override fun onClick(widget: View) {
            val textView    = widget as TextView
            val spanned     = textView.text as Spanned
            target.value = spanned.subSequence(spanned.getSpanStart(this), spanned.getSpanEnd(this)).toString()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
}

fun getParsedTweet(json: JSONObject, today: Date, parser: SimpleDateFormat, target: MutableLiveData<String>, locale: Locale, color: Int): Tweet
{
    var t = json
    var retweeted   = false
    var retweetedBy = ""

    if(t.has("retweeted_status") && !t.isNull("retweeted_status"))
    {
        retweeted   = true
        retweetedBy = t.getJSONObject("user").getString("name")
        t           = t.getJSONObject("retweeted_status")
    }


    /**
     * This section contains editing regarding the object for user profile.
     */

    val user        = t.getJSONObject("user")

    var profileBanner   = ""
    var profileUrl      = ""

    if(user.has("profile_banner_url") && !user.isNull("profile_banner_url"))
    {
        profileBanner   = user.getString("profile_banner_url")
    }

    if(user.has("url") && !user.isNull("url"))
    {
        profileUrl      = user.getString("url")
    }

    val userObj = User(user.getString("id_str"), user.getString("name"),
        user.getString("screen_name"), user.getString("location")?:"", user.getString("description")?:"",
        profileUrl, user.getBoolean("protected"), user.getBoolean("verified"),
        user.getLong("followers_count"), user.getLong("friends_count"), 123L,
        user.getString("created_at"), user.getLong("statuses_count"), user.getString("profile_background_color"),
        profileBanner, user.getBoolean("default_profile_image"),
        (user.getString("profile_image_url_https")?:"").replace("_normal","_x96"), user.getBoolean("following"), user.getBoolean("follow_request_sent"),
        user.getBoolean("notifications"))


    /**
     * User profile section ends.
     * Now starts the tweet text formatting part (most complex)
     */

    var text    = t.getString("full_text")


    /**
     * dealing with native attachments to tweet
     */

    val photos  = java.util.ArrayList<String>()
    var gif     = ""
    var gifThumb= ""
    var video   = ""
    var videoThumb=""

    if(t.has("extended_entities"))
    {
        val extendedEntities = t.getJSONObject("extended_entities").getJSONArray("media")

        for(j in 0 until extendedEntities.length())
        {
            val temp    = extendedEntities.getJSONObject(j)
            text        = text.replace(temp.getString("url"),"")

            when(temp.getString("type"))
            {
                "photo" -> photos.add(temp.getString("media_url_https"))
                "video" -> {
                    video       = extractMP4(temp.getJSONObject("video_info"))
                    videoThumb  = temp.getString("media_url_https")
                    Timber.i("saving video as $video")
                }
                "animated_gif" -> {
                    gif         = extractMP4(temp.getJSONObject("video_info"))
                    gifThumb    = temp.getString("media_url_https")
                    Timber.i("saving gif-video as $gif")
                }
            }
        }
    }

    /**
     * dealing with HTML formatting and links
     */
    val hashtags = t.getJSONObject("entities").getJSONArray("hashtags")
    val usermentions = t.getJSONObject("entities").getJSONArray("user_mentions")
    val urls = t.getJSONObject("entities").getJSONArray("urls")

    for(j in 0 until urls.length())
    {
        val oldUrl = urls.getJSONObject(j).getString("url")
        val newUrl = urls.getJSONObject(j).getString("expanded_url")
        text = text.replace(oldUrl,newUrl)
    }

    //check if quote status
    var isQuoteStatus = false
    var quoteStatus: QuoteTweet? = null

    if(t.has("is_quote_status") && t.getBoolean("is_quote_status") && t.has("quoted_status_permalink"))
    {
        isQuoteStatus = true
        val q = t.getJSONObject("quoted_status")
        val qLink = t.getJSONObject("quoted_status_permalink")
        text = text.replace(qLink.getString("expanded"),"")

        var qText = q.getString("full_text")
        val qUrls = q.getJSONObject("entities").getJSONArray("urls")
        for(k in 0 until qUrls.length())
        {
            val tmp = qUrls.getJSONObject(k)
            qText = qText.replace(tmp.getString("url"), tmp.getString("display_url")) //only here using display url because not attaching any click listeners
        }

        if(q.has("is_quote_status") && q.getBoolean("is_quote_status"))
        {
            val qQlink = q.getJSONObject("quoted_status_permalink")
            qText.replace(qQlink.getString("url"),"")
        }

        var qThumb = ""
        var qGifThumb = ""
        var qVideoThumb = ""

        if(q.has("extended_entities") && q.getJSONObject("extended_entities").getJSONArray("media").length()>0)
        {
            val tmp = q.getJSONObject("extended_entities").getJSONArray("media").getJSONObject(0)
            if(tmp.getString("type") == "photo")
            {
                qThumb = tmp.getString("media_url_https")
            }
            else if(tmp.getString("type") == "animated_gif")
            {
                qGifThumb = tmp.getString("media_url_https")
            }
            else if(tmp.getString("type") == "video")
            {
                qVideoThumb = tmp.getString("media_url_https")
            }
        }

        val qUser = q.getJSONObject("user")
        quoteStatus = QuoteTweet(q.getString("id"), qUser.getString("name"),
            qUser.getString("screen_name"), qUser.getString("profile_image_url_https"),
            qUser.getBoolean("verified"), qText, qThumb, qGifThumb, qVideoThumb, getElapsed(q.getString("created_at"), today, parser, locale))
    }

    val builder = SpannableStringBuilder(text)

    for(j in 0 until hashtags.length())
    {
        val h   = "#${hashtags.getJSONObject(j).getString("text")}"
        //text    = text.replace("#${h.getString("text")}","<a href=\"#${h.getString("text")}\">#${h.getString("text")}</a>")
        val start = builder.toString().indexOf(h,0,true)
        if(start!=-1)
        {
            builder.setSpan(ForegroundColorSpan(color), start, start + h.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(getClickableSpan(target), start, start + h.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    for(j in 0 until usermentions.length())
    {
        val um  = "@${usermentions.getJSONObject(j).getString("screen_name")}"
        //text    = text.replace(um, "<a href=\"@${um.getString("screen_name")}\">@${um.getString("screen_name")}</a>")
        Timber.i("Builder text is $builder")
        val start = builder.toString().indexOf(um,0,true)
        Timber.i("index is $start when looking for $um")
        if(start!=-1)
        {
            builder.setSpan(ForegroundColorSpan(color), start, start + um.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(getClickableSpan(target), start, start + um.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    for(j in 0 until urls.length())
    {
        val url = urls.getJSONObject(j).getString("expanded_url")
        //text    = text.replace(url.getString("url"), "<a href=\"${url.getString("expanded_url")}\">${url.getString("expanded_url")}</a>")
        val start = builder.toString().indexOf(url, 0,true)
        if(start!=-1)
        {
            builder.setSpan(ForegroundColorSpan(color), start, start + url.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(getClickableSpan(target), start, start + url.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }


    /**
     * highlighting done!
     */

    return Tweet(t.getString("id_str"), /*t.getString("full_text")*/text,
        getElapsed(t.getString("created_at"), today, parser, locale), userObj,
        t.getString("source"), "", "","",
        retweeted, retweetedBy, user.getBoolean("verified"),
        t.getLong("favorite_count"), t.getLong("retweet_count"),builder, photos, videoThumb, video, gifThumb,
        gif, t.getBoolean("favorited"), t.getBoolean("retweeted"), isQuoteStatus, quoteStatus)

}

fun parseUser(resp: String):User
{
    val user        = JSONObject(resp)

    var profileBanner   = ""
    var profileUrl      = ""

    if(user.has("profile_banner_url") && !user.isNull("profile_banner_url"))
    {
        profileBanner   = user.getString("profile_banner_url")
    }

    if(user.has("url") && !user.isNull("url"))
    {
        profileUrl      = user.getString("url")
    }

    return User(user.getString("id_str"), user.getString("name"),
        user.getString("screen_name"), user.getString("location")?:"", user.getString("description")?:"",
        profileUrl, user.getBoolean("protected"), user.getBoolean("verified"),
        user.getLong("followers_count"), user.getLong("friends_count"), 123L,
        user.getString("created_at"), user.getLong("statuses_count"), user.getString("profile_background_color"),
        profileBanner, user.getBoolean("default_profile_image"),
        (user.getString("profile_image_url_https")?:"").replace("_normal","_x96"), user.getBoolean("following"), user.getBoolean("follow_request_sent"),
        user.getBoolean("notifications"))
}

internal fun extractMP4(obj: JSONObject): String
{
    val array = obj.getJSONArray("variants")
    for(i in 0 until array.length())
    {
        val t = array.getJSONObject(i)
        if(t.getString("content_type") == "video/mp4")
        {
            return t.getString("url")
        }
    }
    return array.getJSONObject(0).getString("url")
}
