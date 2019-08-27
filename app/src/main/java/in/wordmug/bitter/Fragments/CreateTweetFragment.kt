package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.*
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.CreateTweetFragmentBinding
import android.app.ProgressDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

class CreateTweetFragment : Fragment() {

    companion object {
        fun newInstance() = CreateTweetFragment()
    }

    private lateinit var binding: CreateTweetFragmentBinding
    private lateinit var viewModel: CreateTweetViewModel
    private lateinit var pDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.create_tweet_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(CreateTweetViewModel::class.java)

        pDialog = ProgressDialog(context!!)
        pDialog.setCancelable(false)
        pDialog.setMessage("Posting your tweet...")

        viewModel._status.observe(viewLifecycleOwner, Observer { status->

            if(status == STATUS_WAITING)
            {
                pDialog.show()
            }
            else
            {
                pDialog.hide()
                if(status == STATUS_SUCCESS)
                {
                    showToast(context!!, "Tweet successfully created!")
                    findNavController().navigateUp()
                }
                else if(status == STATUS_NETWORK_ERROR || status == STATUS_DATA_ERROR)
                {
                    showToast(context!!, "Unable to create tweet!")
                    viewModel._statusDoneWith()
                }
            }

        })

        binding.submit.setOnClickListener {

            val text = binding.tweetInput.text.toString()
            if(text.trim().isNotEmpty())
            {
                viewModel.uploadTweet(text)
            }

        }

    }

}
