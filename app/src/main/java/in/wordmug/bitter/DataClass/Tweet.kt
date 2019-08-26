package `in`.wordmug.bitter.DataClass

import `in`.wordmug.bitter.DataUtils.getShortenedCount
import android.os.Parcelable
import android.text.SpannableStringBuilder
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class Tweet(
    val id: String,
    val text: String,
    val time: String,
    val user: User,
    val clientName: String,
    val repliedStatusId: String,
    val repliedStatusUserId: String,
    val repliedStatusUserName: String,
    val isRetweeted: Boolean,
    val retweetedBy: String,
    val isVerified: Boolean,
    var favCount: Long,
    var retweetCount: Long,
    val spanText: @RawValue SpannableStringBuilder,
    val images: ArrayList<String>,
    val videoThumb: String,
    val videoUrl: String,
    val gifThumb: String,
    val gifUrl: String,
    var isLiked: Boolean,
    var isFurtherRetweeted: Boolean,
    val isQuoted: Boolean,
    val quoteStatus: QuoteTweet?
) : Parcelable {

    fun getFavCount(): String = getShortenedCount(favCount)

    fun getRetweetCount(): String = getShortenedCount(retweetCount)

    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other is String) return other == this.id
        if(other is Tweet) return other.id == this.id
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + time.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + clientName.hashCode()
        result = 31 * result + repliedStatusId.hashCode()
        result = 31 * result + repliedStatusUserId.hashCode()
        result = 31 * result + repliedStatusUserName.hashCode()
        result = 31 * result + isRetweeted.hashCode()
        result = 31 * result + retweetedBy.hashCode()
        return result
    }
}