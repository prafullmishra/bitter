package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.CallbackInterface
import `in`.wordmug.bitter.DataClass.TweetActionInterface
import `in`.wordmug.bitter.DataClass.TweetAdapter
import `in`.wordmug.bitter.DataClass.User
import `in`.wordmug.bitter.DataUtils.*
import `in`.wordmug.bitter.MainViewModel
import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.HomeFragmentBinding
import `in`.wordmug.bitter.databinding.ItemTweetBinding
import `in`.wordmug.bitter.databinding.ItemTweetGifBinding
import `in`.wordmug.bitter.databinding.ItemTweetVideoBinding
import android.app.ProgressDialog
import android.content.Intent
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Patterns
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.core.text.toHtml
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber
import java.util.regex.Matcher

class HomeFragment : Fragment(), CallbackInterface {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var mainModel: MainViewModel
    private lateinit var binding: HomeFragmentBinding
    private lateinit var adapter: TweetAdapter
    private lateinit var dialog: ProgressDialog

    private var cachedView: View? = null



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
        if(cachedView == null) cachedView = binding.root
        return cachedView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        activity?.let { mainModel = ViewModelProviders.of(it).get(MainViewModel::class.java) }

        var sample = "01234567"
        sample = sample.replaceRange(1,3,"--")
        Timber.i("replaced TEST is $sample")

        dialog = ProgressDialog(context!!)
        dialog.setMessage("Please wait...")
        dialog.setCancelable(false)

        viewModel._status.observe(viewLifecycleOwner, Observer {status->
            if(status == STATUS_SUCCESS)
            {
                adapter = TweetAdapter(this as CallbackInterface, viewModel as TweetActionInterface, viewModel.loadMore)
                binding.tweetList.adapter = adapter
                adapter.submitList(viewModel.tweetList)
            }
        })

        viewModel._currentLink.observe(viewLifecycleOwner, Observer { link->
            if(link.isNotEmpty())
            {
                Toast.makeText(context!!,"Dealing with |$link|", Toast.LENGTH_SHORT).show()
                if(link.startsWith("#"))
                {
                    //its a hashtag
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToSearchFragment(link))
                }
                else if(link.startsWith("@"))
                {
                    Timber.i("starting profile fetch")
                    if(viewModel._profileStatus.value != STATUS_INIT)
                    {
                        //if not already busy
                        viewModel.fetchProfile(link.replaceFirst("@",""))
                    }
                }
                else
                {
                    val urlMatcher = Patterns.WEB_URL.matcher(link)
                    if(urlMatcher.find())
                    {
                    }
                }
                viewModel._linkDoneWith()
            }
        })

        viewModel._profileStatus.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_WAITING)
            {
                dialog.show()
            }
            else
            {
                dialog.dismiss()
                if(status == STATUS_SUCCESS && viewModel.currentProfile!=null)
                {
                    findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment(viewModel.currentProfile as User))
                }
            }
        })

        viewModel._shouldRefresh.observe(viewLifecycleOwner, Observer { shouldRefresh->
            if(shouldRefresh)
            {
                adapter.notifyDataSetChanged()
                viewModel.refreshDone()
            }
        })

        viewModel.loadMore.observe(viewLifecycleOwner, Observer { loadMore->
            if(loadMore) Timber.i("should load more now")
        })

        mainModel.optionSelected.observe(viewLifecycleOwner, Observer { option->
            if(option == R.id.trendsFragment)
            {
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTrendsFragment())
            }
            else if(option == R.id.profileFragment)
            {
                //findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment())
            }
        })

        binding.fab.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToTrendsFragment())
        }

    }

    /**
     * callback methods to be used by TweetAdapter for user specific actions on list item
     */

    override fun openProfile(pos: Int) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToProfileFragment(viewModel.tweetList[pos].user))
    }

    override fun openTweet(pos: Int) {
        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToViewTweet(viewModel.tweetList[pos].id))
    }

    override fun openImage(pos: Int, index: Int) {
        if(index < viewModel.tweetList[pos].images.size)
        {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToViewImageFragment(viewModel.tweetList[pos].images[index]))
        }
    }

    override fun openVideo(pos: Int) {

        Timber.i("gifurl is ${viewModel.tweetList[pos].gifUrl}")
        Timber.i("videourl is ${viewModel.tweetList[pos].videoUrl}")

        val url =   if(viewModel.tweetList[pos].gifUrl.isNotEmpty()) { viewModel.tweetList[pos].gifUrl}
                    else viewModel.tweetList[pos].videoUrl

        findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToViewVideoFragment(url))
    }

}
