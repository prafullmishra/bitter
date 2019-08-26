package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.showToast
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ViewImageFragmentBinding
import android.graphics.Bitmap
import androidx.navigation.fragment.findNavController
import com.davemorrissey.labs.subscaleview.ImageSource
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener

class ViewImageFragment : Fragment() {

    companion object {
        fun newInstance() = ViewImageFragment()
    }

    private lateinit var binding: ViewImageFragmentBinding
    private lateinit var viewModel: ViewImageViewModel
    private lateinit var options: DisplayImageOptions
    private lateinit var config: ImageLoaderConfiguration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = ViewImageFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewImageViewModel::class.java)
        initImageLoader()

        binding.back.setOnClickListener { findNavController().navigateUp() }

        val args = ViewImageFragmentArgs.fromBundle(arguments!!)
        showImage(args.imageUrl)
    }

    private fun initImageLoader()
    {
        options     = DisplayImageOptions.Builder()
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .build()

        config      = ImageLoaderConfiguration.Builder(context!!.applicationContext)
            .memoryCacheExtraOptions(2000,2000)
            .memoryCacheSize(500000000)
            .diskCacheFileCount(20)
            .diskCacheSize(1000000000)
            .defaultDisplayImageOptions(options)
            .build()

        ImageLoader.getInstance().init(config)
    }

    private fun showImage(url: String)
    {
        val loader = ImageLoader.getInstance()
        loader.loadImage(url, object : SimpleImageLoadingListener(){
            override fun onLoadingComplete(imageUri: String?, view: View?, loadedImage: Bitmap?) {
                super.onLoadingComplete(imageUri, view, loadedImage)
                loadedImage?.let {
                    binding.subSampleScaleView.setImage(ImageSource.bitmap(loadedImage))
                }
            }

            override fun onLoadingStarted(imageUri: String?, view: View?) {
                super.onLoadingStarted(imageUri, view)
            }

            override fun onLoadingCancelled(imageUri: String?, view: View?) {
                super.onLoadingCancelled(imageUri, view)
                binding.loader.visibility = View.GONE
            }

            override fun onLoadingFailed(imageUri: String?, view: View?, failReason: FailReason?) {
                super.onLoadingFailed(imageUri, view, failReason)
                view?.let { showToast(it.context,"Failed to load image") }
                findNavController().navigateUp()
            }
        })
    }

}
