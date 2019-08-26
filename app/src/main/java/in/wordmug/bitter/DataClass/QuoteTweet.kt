package `in`.wordmug.bitter.DataClass

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class QuoteTweet(val id: String,
                      val userName: String,
                      val userHandle: String,
                      val userImage: String,
                      val isVerified: Boolean,
                      val tweetText: String,
                      val thumb: String,
                      val gifThumb: String,
                      val videoThumb: String,
                      val timestamp: String) : Parcelable