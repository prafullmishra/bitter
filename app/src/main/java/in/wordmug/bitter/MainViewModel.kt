package `in`.wordmug.bitter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel() : ViewModel()
{
    val target = MutableLiveData<String>()
    val optionSelected = MutableLiveData<Int>()
}