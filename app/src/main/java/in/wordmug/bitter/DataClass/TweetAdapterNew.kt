package `in`.wordmug.bitter.DataClass

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ItemTweetBinding
import `in`.wordmug.bitter.databinding.ItemTweetGifBinding
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import timber.log.Timber


class TweetAdapterNew(private val callbackInterface: CallbackInterface,
                      private val actionInterface: TweetActionInterface,
                      private val loadMore: MutableLiveData<Boolean>? = null,
                      private val tweetList: ArrayList<Tweet>): RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    override fun getItemCount(): Int {
        return tweetList.size
    }

    val TYPE_NORMAL = 1
    val TYPE_GIF = 2
    val TYPE_VIDEO = 3

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == TYPE_GIF || viewType == TYPE_VIDEO) {
            TweetGifHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_tweet_gif, parent, false))
        } else {
            TweetHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_tweet, parent, false))
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = tweetList[position]

        return when {
            item.gifThumb.isNotEmpty() -> TYPE_GIF
            item.videoThumb.isNotEmpty() -> TYPE_VIDEO
            else -> TYPE_NORMAL //images - 1 upto 4
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = tweetList[position]
        if(holder is TweetGifHolder) holder.bind(item)
        else if(holder is TweetHolder) holder.bind(item)

        if(itemCount >2 && position == itemCount-2 && loadMore!=null && loadMore.value == false)
        {
            loadMore.value = true
        }
    }

    inner class TweetHolder(val binding: ItemTweetBinding) : RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.apply {
                userImage.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                userName.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                userHandle.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                likeIcon.setOnClickListener { actionInterface.toggleLike(adapterPosition) }
                likeCount.setOnClickListener { actionInterface.toggleLike(adapterPosition) }
                retweetIcon.setOnClickListener { actionInterface.toggleRetweet(adapterPosition) }
                retweetCount.setOnClickListener { actionInterface.toggleRetweet(adapterPosition) }
                root.setOnClickListener { callbackInterface.openTweet(adapterPosition) }

                image1.setOnClickListener { callbackInterface.openImage(adapterPosition,0) }
                image2.setOnClickListener { callbackInterface.openImage(adapterPosition,1) }
                image3.setOnClickListener { callbackInterface.openImage(adapterPosition,2) }
                image4.setOnClickListener { callbackInterface.openImage(adapterPosition,3) }
            }
        }

        fun bind(item: Tweet)
        {
            binding.apply {
                if(item.isRetweeted)
                {
                    retweetedBy.text = "by ${item.retweetedBy}"
                    retweetedBy.visibility = View.VISIBLE
                }
                else
                {
                    retweetedBy.visibility = View.GONE
                }
                userName.text = item.user.name
                userHandle.text = "@${item.user.handle}"
                Glide.with(userImage).load(item.user.profileImage).into(userImage)
                tweetText.text = item.spanText
                tweetText.movementMethod = LinkMovementMethod.getInstance()
                retweetCount.text = item.getRetweetCount()
                likeCount.text = item.getFavCount()
                tweetTime.text = item.time

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
                        Glide.with(image1).load(item.images[0]).into(image1)

                        if(item.images.size>1)
                        {
                            Timber.i("in else2")
                            Glide.with(image2).load(item.images[1]).into(image2)
                            image2.visibility = View.VISIBLE

                            if(item.images.size>2)
                            {
                                Timber.i("in else3")
                                imageLine2.visibility = View.VISIBLE
                                Glide.with(image3).load(item.images[2]).into(image3)
                                image3.visibility = View.VISIBLE
                                if(item.images.size>3)
                                {
                                    Glide.with(image4).load(item.images[3]).into(image4)
                                    image4.visibility = View.VISIBLE
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
                    }
                    else
                    {
                        //show thumb
                        Glide.with(image1).load(item.videoThumb).into(image1)
                        imageLine1.visibility = View.VISIBLE
                    }
                    imageContainer.visibility = View.VISIBLE
                }

                if(item.isQuoted && item.quoteStatus != null)
                {
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
                            item.quoteStatus.thumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.thumb).into(quoteTweetImage)
                            item.quoteStatus.gifThumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.gifThumb).into(quoteTweetImage)
                            item.quoteStatus.videoThumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.videoThumb).into(quoteTweetImage)
                        }
                        quoteTweetImage.visibility = View.VISIBLE
                    }


                    Glide.with(quoteUserImage).load(item.quoteStatus.userImage).into(quoteUserImage)


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
                    quoteTweetText.visibility = View.VISIBLE
                }
                else
                {
                    quoteTweetLine.visibility = View.GONE
                    quoteTweetHeader.visibility = View.GONE
                    quoteTweetText.visibility = View.GONE
                    quoteTweetImage.visibility = View.GONE
                }
            }
        }
    }

    inner class TweetGifHolder(val binding: ItemTweetGifBinding): RecyclerView.ViewHolder(binding.root)
    {
        init {
            binding.apply {
                userImage.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                userName.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                userHandle.setOnClickListener { callbackInterface.openProfile(adapterPosition) }
                likeIcon.setOnClickListener { actionInterface.toggleLike(adapterPosition) }
                likeCount.setOnClickListener { actionInterface.toggleLike(adapterPosition) }
                retweetIcon.setOnClickListener { actionInterface.toggleRetweet(adapterPosition) }
                retweetCount.setOnClickListener { actionInterface.toggleRetweet(adapterPosition) }
                tweetImage.setOnClickListener { callbackInterface.openVideo(adapterPosition) }
                root.setOnClickListener { callbackInterface.openTweet(adapterPosition) }
            }
        }

        fun bind(item: Tweet)
        {
            binding.apply {
                if(item.isRetweeted)
                {
                    retweetedBy.text = "by ${item.retweetedBy}"
                    retweetedBy.visibility = View.VISIBLE
                }
                else
                {
                    retweetedBy.visibility = View.GONE
                }
                userName.text = item.user.name
                userHandle.text = "@${item.user.handle}"
                Glide.with(userImage).load(item.user.profileImage).into(userImage)
                tweetText.text = item.spanText
                tweetText.movementMethod = LinkMovementMethod.getInstance()

                if(item.spanText.toString().trim().isEmpty()) tweetText.visibility = View.GONE
                else tweetText.visibility = View.VISIBLE

                retweetCount.text = item.getRetweetCount()
                likeCount.text = item.getFavCount()
                tweetTime.text = item.time

                if(item.isVerified) verifiedIcon.visibility = View.VISIBLE
                else verifiedIcon.visibility = View.INVISIBLE

                retweetIcon.isSelected = item.isFurtherRetweeted
                retweetCount.isSelected = item.isFurtherRetweeted

                likeIcon.isSelected = item.isLiked
                likeCount.isSelected = item.isLiked

                if(item.gifThumb.isNotEmpty())
                {
                    Timber.i("gif thumb present")
                    Glide.with(tweetImage).load(item.gifThumb).into(tweetImage)
                    gifTag.visibility = View.VISIBLE
                }
                else if(item.videoThumb.isNotEmpty())
                {
                    Timber.i("video thumb present")
                    Glide.with(tweetImage).load(item.videoThumb).into(tweetImage)
                    gifTag.visibility = View.GONE
                }

                if(item.isQuoted && item.quoteStatus != null)
                {
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
                            item.quoteStatus.thumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.thumb).into(quoteTweetImage)
                            item.quoteStatus.gifThumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.gifThumb).into(quoteTweetImage)
                            item.quoteStatus.videoThumb.isNotEmpty() -> Glide.with(quoteTweetImage).load(item.quoteStatus.videoThumb).into(quoteTweetImage)
                        }
                        quoteTweetImage.visibility = View.VISIBLE
                    }


                    Glide.with(quoteUserImage).load(item.quoteStatus.userImage).into(quoteUserImage)


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
                    quoteTweetText.visibility = View.VISIBLE
                }
                else
                {
                    quoteTweetLine.visibility = View.GONE
                    quoteTweetHeader.visibility = View.GONE
                    quoteTweetText.visibility = View.GONE
                    quoteTweetImage.visibility = View.GONE
                }
            }
        }
    }

    fun updateList(newList: ArrayList<Tweet>)
    {
        Timber.i("prev - new : ${tweetList.size} - ${newList.size}")
        val result = DiffUtil.calculateDiff(TweetDiffCallback(tweetList, newList))
        tweetList.clear()
        tweetList.addAll(newList)
        result.dispatchUpdatesTo(this)
    }


    class TweetDiffCallback(private val oldList: ArrayList<Tweet>, private val newList: ArrayList<Tweet>): DiffUtil.Callback()
    {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun getOldListSize(): Int {
            return oldList.size
        }

        override fun getNewListSize(): Int {
            return newList.size
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    
}