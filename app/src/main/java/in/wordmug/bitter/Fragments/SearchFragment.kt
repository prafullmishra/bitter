package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.CallbackInterface
import `in`.wordmug.bitter.DataClass.TweetActionInterface
import `in`.wordmug.bitter.DataClass.TweetAdapter
import `in`.wordmug.bitter.DataUtils.STATUS_SUCCESS
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.databinding.SearchFragmentBinding
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import timber.log.Timber

class SearchFragment : Fragment(), CallbackInterface {

    companion object {
        fun newInstance() = SearchFragment()
    }

    private lateinit var binding: SearchFragmentBinding
    private lateinit var viewModel: SearchViewModel
    private lateinit var viewModelFactory: SearchViewModelFactory
    private lateinit var adapter: TweetAdapter
    private var prevSize = -1

    private var cachedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(inflater, container, false)
        if(cachedView == null) cachedView = binding.root
        return cachedView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val args = SearchFragmentArgs.fromBundle(arguments!!)

        viewModelFactory = SearchViewModelFactory(activity!!.application, args.query)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SearchViewModel::class.java)

        binding.name.text = "\"${args.query}\""

        viewModel._status.observe(viewLifecycleOwner, Observer { status->

            /**
             * this deals with when the FIRST TIME the list is loaded, not when new tweets are added
             */

            when(status)
            {
                STATUS_SUCCESS -> {
                    adapter = TweetAdapter(this@SearchFragment as CallbackInterface, viewModel as TweetActionInterface)
                    binding.list.adapter = adapter
                    adapter.submitList(viewModel.tweetList)
                }
            }

        })

        viewModel._addonStatus.observe(viewLifecycleOwner, Observer { status->

            /**
             * this deals with new tweets which are added periodically after the FIRST TIME initialization is done
             */

            when(status)
            {
                STATUS_SUCCESS -> {
                    Timber.i("submitting list")

                    if(prevSize!= viewModel.tweetList.size)
                    {
                        prevSize = viewModel.tweetList.size
                        adapter.submitList(viewModel.tweetList) //uses diffutil to update item
                        binding.list.scrollToPosition(prevSize-1)
                    }

                    //binding.list.scrollToPosition(0)
                    //adapter.notifyDataSetChanged()
                }
            }

        })

        viewModel._shouldRefresh.observe(viewLifecycleOwner, Observer { shouldRefresh->
            if(shouldRefresh)
            {
                adapter.submitList(viewModel.tweetList)
                viewModel.refreshDone()
            }
        })
    }


    override fun openProfile(pos: Int) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToProfileFragment(viewModel.tweetList[pos].user))
    }

    override fun openTweet(pos: Int) {
        findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToViewTweet(viewModel.tweetList[pos].id))
    }

    override fun openImage(pos: Int, index: Int) {
        if(index < viewModel.tweetList[pos].images.size)
        {
            findNavController().navigate(SearchFragmentDirections.actionSearchFragmentToViewImageFragment(viewModel.tweetList[pos].images[index]))
        }
    }

    override fun openVideo(pos: Int) {

        val url =   if(viewModel.tweetList[pos].gifUrl.isNotEmpty()) { viewModel.tweetList[pos].gifUrl}
        else viewModel.tweetList[pos].videoUrl

        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToViewVideoFragment(url))
    }

}
