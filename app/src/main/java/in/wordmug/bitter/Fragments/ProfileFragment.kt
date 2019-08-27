package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.*
import `in`.wordmug.bitter.DataUtils.SP_SCREEN_NAME
import `in`.wordmug.bitter.DataUtils.STATUS_SUCCESS
import `in`.wordmug.bitter.DataUtils.getSP
import `in`.wordmug.bitter.DataUtils.getShortenedCount
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ItemPagerBinding
import `in`.wordmug.bitter.databinding.ItemTweetBinding
import `in`.wordmug.bitter.databinding.ItemTweetGifBinding
import `in`.wordmug.bitter.databinding.ProfileFragmentBinding
import android.graphics.Color
import android.text.method.LinkMovementMethod
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.profile_fragment.*
import timber.log.Timber

class ProfileFragment : Fragment(), CallbackInterface {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var binding: ProfileFragmentBinding
    private lateinit var viewModel: ProfileViewModel
    private lateinit var viewModelFactory: ProfileViewModelFactory
    private lateinit var pagerAdapter: ListAdapter
    private lateinit var adapter: TweetAdapter
    private lateinit var args: ProfileFragmentArgs
    private lateinit var user: User

    private var cachedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(cachedView == null)
        {
            binding = DataBindingUtil.inflate(inflater, R.layout.profile_fragment, container, false)
            cachedView = binding.root
        }
        return cachedView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        args = ProfileFragmentArgs.fromBundle(arguments!!)
        user = args.user

        Timber.i("in fragment ${user!!.name}")

        viewModelFactory    = ProfileViewModelFactory(activity!!.application, user)
        viewModel           = ViewModelProviders.of(this, viewModelFactory).get(ProfileViewModel::class.java)

        viewModel._status.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_SUCCESS)
            {
                pagerAdapter = ListAdapter()
                binding.pager.adapter = pagerAdapter
                binding.tabLayout.setupWithViewPager(binding.pager)


                adapter =  TweetAdapter(this as CallbackInterface, viewModel as TweetActionInterface)
                binding.list.adapter = adapter
                adapter.submitList(viewModel.tweetList)

            }
        })

        viewModel._shouldRefresh.observe(viewLifecycleOwner, Observer { shouldRefresh->
            if(shouldRefresh)
            {
                adapter.submitList(viewModel.tweetList)
                viewModel.refreshDone()
            }
        })

        populateData(user)
    }

    private fun populateData(user: User)
    {


        binding.apply {

            if(user.handle == getSP(context!!).getString(SP_SCREEN_NAME,"")?:"")
            {
                binding.follow.visibility = View.GONE
            }
            else
            {
                when {
                    user.followingRequested -> binding.follow.text = "Follow requested"
                    user.following -> binding.follow.text = "Following"
                    else -> binding.follow.text = "Not Following"
                }
            }

            banner.setBackgroundColor(Color.parseColor("#${user.backgroundColor}"))
            Glide.with(context!!).load(user.backgroundImage).into(banner)
            Glide.with(context!!).load(user.profileImage!!.replace("_normal","_x96")).into(profile)
            userName.text = user.name
            userHandle.text = "@${user.handle}"
            profileText.text = user.description
            friendsCount.text = getShortenedCount(user.friendsCount)
            followersCount.text = getShortenedCount(user.followersCount) //change this to observer pattern
            if(user.isVerified) verifiedIcon.visibility = View.VISIBLE
            else verifiedIcon.visibility = View.GONE

            if(user.location.isNullOrEmpty())
            {
                locationIcon.visibility = View.GONE
                locationText.visibility = View.GONE
            }
            else
            {
                locationText.text = user.location
            }

            if(user.url.isNullOrEmpty())
            {
                linkIcon.visibility = View.GONE
                linkText.visibility = View.GONE
            }
            else
            {
                linkText.text = user.url
            }

            joinedText.text = "Joined ${viewModel.formatter.format(viewModel.parser.parse(user.joinedOn))}"

        }
    }


    inner class ListAdapter(): PagerAdapter()
    {
        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view == `object`
        }

        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return if(position == 0) "Tweets"
            else "Media"
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            //return super.instantiateItem(container, position)
            val binding = ItemPagerBinding.inflate(LayoutInflater.from(context))
            container.addView(binding.root)

            if(position == 0)
            {
                val adapter1 = TweetAdapter(this@ProfileFragment as CallbackInterface, viewModel as TweetActionInterface)
                binding.list.adapter = adapter1
                adapter1.submitList(viewModel.tweetList)
            }
            else
            {
                val adapter2 = TweetAdapter(this@ProfileFragment as CallbackInterface, viewModel as TweetActionInterface)
                binding.list.adapter = adapter2
                adapter2.submitList(viewModel.mediaList)
            }

            return binding.root
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }
    }

    override fun openProfile(pos: Int) {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentSelf(viewModel.tweetList[pos].user))
    }

    override fun openTweet(pos: Int) {
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToViewTweet(viewModel.tweetList[pos].id))
    }

    override fun openImage(pos: Int, index: Int) {
        if(index < viewModel.tweetList[pos].images.size)
        {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToViewImageFragment(viewModel.tweetList[pos].images[index]))
        }
    }

    override fun openVideo(pos: Int) {

        val url =   if(viewModel.tweetList[pos].gifUrl.isNotEmpty()) { viewModel.tweetList[pos].gifUrl}
        else viewModel.tweetList[pos].videoUrl

        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToViewVideoFragment(url))
    }

}
