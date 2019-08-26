package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.User
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class ProfileViewModelFactory (private val application: Application, private val user: User) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ProfileViewModel::class.java))
        {
            return ProfileViewModel(application,user) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }

}