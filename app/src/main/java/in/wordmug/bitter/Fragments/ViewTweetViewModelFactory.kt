package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.User
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ViewTweetViewModelFactory (private val application: Application, private val tweetId: String) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ViewTweetViewModel::class.java))
        {
            return ViewTweetViewModel(application,tweetId) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }

}