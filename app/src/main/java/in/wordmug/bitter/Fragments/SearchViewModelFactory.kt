package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.User
import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.lang.IllegalArgumentException

class SearchViewModelFactory (private val application: Application, private val query: String) : ViewModelProvider.Factory
{
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SearchViewModel::class.java))
        {
            return SearchViewModel(application, query) as T
        }
        throw IllegalArgumentException("Unknown viewmodel class")
    }

}