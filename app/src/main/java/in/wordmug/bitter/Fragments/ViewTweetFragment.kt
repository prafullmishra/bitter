package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataClass.CallbackInterface
import `in`.wordmug.bitter.DataClass.Tweet
import `in`.wordmug.bitter.DataClass.TweetActionInterface
import `in`.wordmug.bitter.DataClass.TweetAdapter
import `in`.wordmug.bitter.DataUtils.STATUS_SUCCESS
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ItemTweetBinding
import `in`.wordmug.bitter.databinding.ItemTweetGifBinding
import `in`.wordmug.bitter.databinding.ViewTweetFragmentBinding
import android.text.method.LinkMovementMethod
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber

class ViewTweetFragment : Fragment(), CallbackInterface {

    companion object {
        fun newInstance() = ViewTweetFragment()
    }

    private lateinit var viewModel: ViewTweetViewModel
    private lateinit var viewModelFactory: ViewTweetViewModelFactory
    private lateinit var binding: ViewTweetFragmentBinding
    private lateinit var adapter: TweetAdapter

    private var cachedView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_tweet_fragment, container, false)
        if(cachedView == null) cachedView = binding.root
        return cachedView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val args = ViewTweetFragmentArgs.fromBundle(arguments!!)

        viewModelFactory    = ViewTweetViewModelFactory(activity!!.application, args.tweetId)
        viewModel           = ViewModelProviders.of(this, viewModelFactory).get(ViewTweetViewModel::class.java)


        Timber.i("received tweetId is ${args.tweetId}")

        viewModel._partialStatus.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_SUCCESS)
            {
                showView(viewModel.currentTweet)
                binding.apply {
                    loader.visibility = View.GONE
                    mainContainer.visibility = View.VISIBLE
                }
            }
        })

        viewModel._status.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_SUCCESS)
            {

                Timber.i("showing ${viewModel.replies.size} replies")
                adapter = TweetAdapter(this as CallbackInterface, viewModel as TweetActionInterface)
                binding.replyList.adapter = adapter
                adapter.submitList(viewModel.replies)
                binding.secondLoader.visibility = View.GONE
            }
        })

        viewModel._shouldRefresh.observe(viewLifecycleOwner, Observer { should->
            if(should)
            {
                adapter.submitList(viewModel.replies)
                viewModel.refreshDone()
            }
        })

        viewModel._currentLink.observe(viewLifecycleOwner, Observer { link->
            if(!link.isNullOrEmpty())
            {
                Toast.makeText(context!!, "Dealing with $link", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showView(item: Tweet)
    {
        binding.apply {

            userName.text = item.user.name
            userHandle.text = "@${item.user.handle}"
            Glide.with(context!!).load(item.user.profileImage).into(userImage)
            tweetText.text = item.spanText
            tweetText.movementMethod = LinkMovementMethod.getInstance()
            retweetCount.text = item.getRetweetCount()
            likeCount.text = item.getFavCount()
            tweetTime.text = item.time

            when {
                item.gifThumb.isNotEmpty() -> {
                    Glide.with(context!!).load(item.gifThumb).into(tweetImage)
                    gifTag.visibility = View.VISIBLE
                }
                item.videoThumb.isNotEmpty() -> {
                    Glide.with(context!!).load(item.videoThumb).into(tweetImage)
                    gifTag.visibility = View.GONE
                }
                else -> {
                    vidContainer.visibility = View.GONE
                    gifTag.visibility = View.GONE
                    playIcon.visibility = View.GONE
                }
            }

            if(item.spanText.toString().trim().isEmpty()) tweetText.visibility = View.GONE
            else tweetText.visibility = View.VISIBLE

            if(item.isVerified) verifiedIcon.visibility = View.VISIBLE
            else verifiedIcon.visibility = View.INVISIBLE

            retweetIcon.isSelected = item.isFurtherRetweeted
            retweetCount.isSelected = item.isFurtherRetweeted

            likeIcon.isSelected = item.isLiked
            likeCount.isSelected = item.isLiked

            imageLine1.visibility = View.GONE
            imageLine2.visibility = View.GONE
            imageContainer.visibility = View.GONE


            if(item.images.size == 0 && item.gifThumb.isEmpty() && item.videoThumb.isEmpty())
            {
                image1.setImageBitmap(null)
                image2.setImageBitmap(null)
                image3.setImageBitmap(null)
                image4.setImageBitmap(null)
            }
            else
            {
                Timber.i("in else")
                if(item.images.size>0)
                {

                    Timber.i("in else1")
                    imageLine1.visibility = View.VISIBLE
                    image1.visibility = View.VISIBLE
                    image1.setOnClickListener { openImage(viewModel.currentTweet.images[0]) }
                    Glide.with(image1).load(item.images[0]).into(image1)

                    if(item.images.size>1)
                    {
                        Timber.i("in else2")
                        Glide.with(image2).load(item.images[1]).into(image2)
                        image2.visibility = View.VISIBLE
                        image2.setOnClickListener { openImage(viewModel.currentTweet.images[1]) }

                        if(item.images.size>2)
                        {
                            Timber.i("in else3")
                            imageLine2.visibility = View.VISIBLE
                            Glide.with(image3).load(item.images[2]).into(image3)
                            image3.visibility = View.VISIBLE
                            image3.setOnClickListener { openImage(viewModel.currentTweet.images[2]) }

                            if(item.images.size>3)
                            {
                                Glide.with(image4).load(item.images[3]).into(image4)
                                image4.visibility = View.VISIBLE
                                image4.setOnClickListener { openImage(viewModel.currentTweet.images[3]) }
                            }
                            else
                            {
                                image4.visibility = View.GONE
                            }
                        }
                    }
                    else
                    {
                        image2.visibility = View.GONE
                    }
                }
                else if(item.gifThumb.isNotEmpty())
                {
                    Glide.with(image1).load(item.gifThumb).into(image1)
                    imageLine1.visibility = View.VISIBLE
                    image1.setOnClickListener { openImage(item.gifThumb) }
                }
                else
                {
                    //show thumb
                    Glide.with(image1).load(item.videoThumb).into(image1)
                    imageLine1.visibility = View.VISIBLE
                    image1.setOnClickListener { openImage(item.videoThumb) }
                }
                imageContainer.visibility = View.VISIBLE
            }

            if(item.isQuoted && item.quoteStatus != null)
            {
                Timber.i("showing quote now")
                quoteUserName.text = item.quoteStatus.userName
                quoteUserHandle.text = "@${item.quoteStatus.userHandle}"
                quoteTweetText.text = item.quoteStatus.tweetText
                quoteTweetTime.text = item.quoteStatus.timestamp

                if(item.quoteStatus.thumb.isEmpty() && item.quoteStatus.gifThumb.isEmpty() && item.quoteStatus.videoThumb.isEmpty())
                {
                    Timber.i("hiding for ${item.quoteStatus.tweetText}")
                    quoteTweetImage.visibility = View.GONE
                }
                else
                {
                    Timber.i("showing for ${item.quoteStatus.tweetText}")
                    when {
                        item.quoteStatus.thumb.isNotEmpty() -> Glide.with(context!!).load(item.quoteStatus.thumb).into(quoteTweetImage)
                        item.quoteStatus.gifThumb.isNotEmpty() -> Glide.with(context!!).load(item.quoteStatus.gifThumb).into(quoteTweetImage)
                        item.quoteStatus.videoThumb.isNotEmpty() -> Glide.with(context!!).load(item.quoteStatus.videoThumb).into(quoteTweetImage)
                    }
                    quoteTweetImage.visibility = View.VISIBLE
                }


                Glide.with(context!!).load(item.quoteStatus.userImage).into(quoteUserImage)


                if(item.quoteStatus.isVerified)
                {
                    quoteUserVerified.visibility = View.VISIBLE
                }
                else
                {
                    quoteUserVerified.visibility = View.GONE
                }

                quoteTweetLine.visibility = View.VISIBLE
                quoteTweetHeader.visibility = View.VISIBLE
                quoteTweetHeader.setOnClickListener { findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetSelf(item.quoteStatus.id)) }
                quoteTweetText.visibility = View.VISIBLE
                quoteTweetText.setOnClickListener { findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetSelf(item.quoteStatus.id)) }
                quoteTweetImage.setOnClickListener { findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetSelf(item.quoteStatus.id)) }
            }
            else
            {
                Timber.i("hiding quote now")
                quoteTweetLine.visibility = View.GONE
                quoteTweetHeader.visibility = View.GONE
                quoteTweetText.visibility = View.GONE
                quoteTweetImage.visibility = View.GONE
            }

        }
    }

    override fun openProfile(pos: Int) {
        findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetToProfileFragment(viewModel.replies[pos].user))
    }

    override fun openTweet(pos: Int) {
        findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetSelf(viewModel.replies[pos].id))
    }

    override fun openImage(pos: Int, index: Int) {
        if(index < viewModel.replies[pos].images.size)
        {
            findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetToViewImageFragment(viewModel.replies[pos].images[index]))
        }
    }

    override fun openVideo(pos: Int) {

        val url =   if(viewModel.replies[pos].gifUrl.isNotEmpty()) { viewModel.replies[pos].gifUrl}
        else viewModel.replies[pos].videoUrl

        findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetToViewVideoFragment(url))
    }

    /**
     * this function opens image of the current tweet and not from the reply list
     */

    private fun openImage(url: String)
    {
        findNavController().navigate(ViewTweetFragmentDirections.actionViewTweetToViewImageFragment(url))
    }


}
