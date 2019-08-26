package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.SP_OAUTH_SECRET
import `in`.wordmug.bitter.DataUtils.SP_OAUTH_TOKEN
import `in`.wordmug.bitter.DataUtils.getEditor
import `in`.wordmug.bitter.DataUtils.getSP
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.WelcomeFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import timber.log.Timber

class WelcomeFragment : Fragment() {

    companion object {
        fun newInstance() = WelcomeFragment()
    }

    private lateinit var binding: WelcomeFragmentBinding
    private lateinit var viewModel: WelcomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context!!), R.layout.welcome_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WelcomeViewModel::class.java)

        val token = getSP(context!!).getString(SP_OAUTH_TOKEN,"")
        val secret= getSP(context!!).getString(SP_OAUTH_SECRET, "")

        if(token!=null && token.isNotEmpty() && secret!=null && token.isNotEmpty())
        {
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToHomeFragment())
        }
        else
        {
            getEditor(context!!).clear().apply()
        }

        binding.button.setOnClickListener {
            Timber.i("navigating away")
            findNavController().navigate(WelcomeFragmentDirections.actionWelcomeFragmentToFinalFragment())
        }
    }

}
