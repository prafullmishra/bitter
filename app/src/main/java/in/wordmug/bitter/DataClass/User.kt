package `in`.wordmug.bitter.DataClass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String,
    val name: String,
    val handle: String,
    val location: String?,
    val description: String?,
    val url: String?,
    val isProtected: Boolean,
    val isVerified: Boolean,
    val followersCount: Long,
    val friendsCount: Long,
    val listedCount: Long,
    val joinedOn: String,
    val tweetCount: Long,
    val backgroundColor: String,
    val backgroundImage: String?,
    val defaultProfileImage: Boolean,
    val profileImage: String?,
    val following: Boolean,
    val followingRequested: Boolean,
    val notificationEnabled: Boolean
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if(other == null) return false
        if(other is String) return other == this.id
        if(other is User) return other.id == this.id
        return false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + handle.hashCode()
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (url?.hashCode() ?: 0)
        result = 31 * result + isProtected.hashCode()
        result = 31 * result + isVerified.hashCode()
        result = 31 * result + followersCount.hashCode()
        result = 31 * result + friendsCount.hashCode()
        result = 31 * result + listedCount.hashCode()
        result = 31 * result + joinedOn.hashCode()
        result = 31 * result + tweetCount.hashCode()
        result = 31 * result + backgroundColor.hashCode()
        result = 31 * result + (backgroundImage?.hashCode() ?: 0)
        result = 31 * result + defaultProfileImage.hashCode()
        result = 31 * result + (profileImage?.hashCode() ?: 0)
        result = 31 * result + following.hashCode()
        result = 31 * result + followingRequested.hashCode()
        result = 31 * result + notificationEnabled.hashCode()
        return result
    }
}