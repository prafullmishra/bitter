package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.*
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.FinalFragmentBinding
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import timber.log.Timber

class FinalFragment : Fragment() {

    companion object {
        fun newInstance() = FinalFragment()
    }

    private lateinit var binding: FinalFragmentBinding
    private lateinit var viewModel: FinalViewModel
    private lateinit var pDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.final_fragment, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FinalViewModel::class.java)

        pDialog  = ProgressDialog(context!!)
        pDialog.setCancelable(false)

        viewModel.initTokenFetched.observe(viewLifecycleOwner, Observer {status->

            if(status == STATUS_WAITING)
            {
                pDialog.setMessage("Generating token..")
                pDialog.show()
            }
            else
            {
                pDialog.dismiss()
            }

            if(status == STATUS_DONE)
            {
                binding.webView.webViewClient = object : WebViewClient(){

                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        Timber.i("now loading $url")
                        super.onPageStarted(view, url, favicon)

                        if(url!=null && url.startsWith("bitter://"))
                        {
                            binding.webView.visibility = View.GONE
                            viewModel.convertToken(url)
                        }

                        pDialog.setMessage("Loading login page...")
                        pDialog.show()

                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        Timber.i("finished loading $url")
                        super.onPageFinished(view, url)
                        if(pDialog.isShowing) pDialog.dismiss()

                    }

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        super.onReceivedError(view, request, error)
                        if(pDialog.isShowing) pDialog.dismiss()
                    }
                }
                binding.webView.settings.javaScriptEnabled = true
                binding.webView.loadUrl("https://api.twitter.com/oauth/authenticate?oauth_token=${viewModel.oauthToken}")
            }
        })

        viewModel.finalTokenFetched.observe(viewLifecycleOwner, Observer { status->

            if(status == STATUS_WAITING)
            {
                pDialog.setMessage("Converting to access token..")
                pDialog.show()
            }
            else
            {
                pDialog.dismiss()
            }

            if(status == STATUS_SUCCESS)
            {
                findNavController().navigate(FinalFragmentDirections.actionFinalFragmentToHomeFragment())
                viewModel.finalTokenFetched.value = STATUS_INIT
            }
            else if(status == STATUS_NETWORK_ERROR)
            {
                showToast(context!!, "Error connecting")
            }
        })
    }

}
