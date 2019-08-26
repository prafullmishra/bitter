package `in`.wordmug.bitter.DataClass

interface CallbackInterface {

    fun openProfile(pos: Int)

    fun openTweet(pos: Int)

    fun openImage(pos: Int, index: Int)

    fun openVideo(pos: Int)
}