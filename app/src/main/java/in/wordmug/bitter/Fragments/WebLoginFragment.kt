package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.STATUS_DONE
import `in`.wordmug.bitter.DataUtils.STATUS_FAILED
import `in`.wordmug.bitter.DataUtils.STATUS_WAITING
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.WebLoginFragmentBinding
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

class WebLoginFragment : Fragment() {

    companion object {
        fun newInstance() = WebLoginFragment()
    }

    private lateinit var binding: WebLoginFragmentBinding
    private lateinit var viewModel: WebLoginViewModel
    private lateinit var pDialog: ProgressDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("in weblogin frag")
        binding = DataBindingUtil.inflate(inflater, R.layout.web_login_fragment, container, false)
        return binding.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WebLoginViewModel::class.java)

        pDialog = ProgressDialog(context!!)
        pDialog.setCancelable(false)

        Timber.i("preparing webview now")
        binding.webView.webViewClient = object : WebViewClient(){

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                Timber.i("now loading $url")
                super.onPageStarted(view, url, favicon)

                if(url!=null && url.startsWith("bitter://"))
                {
                    binding.webView.visibility = View.GONE
                    viewModel.convertToken(url)
                }

            }

            override fun onPageFinished(view: WebView?, url: String?) {
                Timber.i("finished loading $url")
                super.onPageFinished(view, url)

            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
            }
        }

        Timber.i("loading url now")
        binding.webView.settings.javaScriptEnabled = true


        viewModel._initTokenFetched.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_DONE)
            {
                //next step
                binding.webView.loadUrl("https://api.twitter.com/oauth/authenticate?oauth_token=${viewModel.oauthToken}")
            }
            else if(status == STATUS_FAILED)
            {
                findNavController().navigateUp()
            }

            if(status == STATUS_WAITING)
            {
                pDialog.setMessage("Fetching token...")
                pDialog.show()
            }
            else
            {
                pDialog.dismiss()
            }

        })

        viewModel._finalTokenFetched.observe(viewLifecycleOwner, Observer { status->
            if(status)
            {
                //findNavController().navigate(WebLoginFragmentDirections.actionWebLoginFragmentToHomeFragment())
            }
        })
    }

}
