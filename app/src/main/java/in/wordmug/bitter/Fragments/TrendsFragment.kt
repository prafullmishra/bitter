package `in`.wordmug.bitter.Fragments

import `in`.wordmug.bitter.DataUtils.STATUS_DONE
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import `in`.wordmug.bitter.R
import `in`.wordmug.bitter.databinding.ItemHeaderListBinding
import `in`.wordmug.bitter.databinding.ItemTrendListBinding
import `in`.wordmug.bitter.databinding.TrendsFragmentBinding
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TrendsFragment : Fragment() {

    companion object {
        fun newInstance() = TrendsFragment()
    }

    private lateinit var viewModel: TrendsViewModel
    private lateinit var binding: TrendsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.trends_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TrendsViewModel::class.java)

        viewModel._status.observe(viewLifecycleOwner, Observer { status->
            if(status == STATUS_DONE)
            {
                //attach adapter
                val adapter = TrendAdapter()
                binding.trendList.layoutManager = LinearLayoutManager(context!!)
                binding.trendList.adapter = adapter
            }
            else
            {
                //show error
            }
        })
    }

    private fun searchTrend(pos: Int)
    {
        findNavController().navigate(TrendsFragmentDirections.actionTrendsFragmentToSearchFragment(viewModel.trends[pos-1].title))
    }


    inner class TrendAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>()
    {
        val TYPE_HEADER = 1
        val TYPE_NORMAL = 2

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(viewType == TYPE_NORMAL)
            {
                TrendHolder(ItemTrendListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
            else
            {
                HeadingHolder(ItemHeaderListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            }
        }

        override fun getItemCount(): Int {
            return viewModel.trends.size+1 //+1 for header
        }

        override fun getItemViewType(position: Int): Int {
            return if(position == 0)
            {
                TYPE_HEADER
            }
            else
            {
                TYPE_NORMAL
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if(holder is TrendHolder)
            {
                holder.binding.apply {
                    title.text = viewModel.trends[position-1].title
                    subtitle.text = viewModel.trends[position-1].subtitle
                    index.text = "$position."
                }
            }
            else if(holder is HeadingHolder)
            {
                holder.binding.trendHeadline.text = "Top Trends"
            }
        }

        inner class TrendHolder(val binding: ItemTrendListBinding): RecyclerView.ViewHolder(binding.root)
        {
            init {
                binding.root.setOnClickListener {
                    searchTrend(adapterPosition)
                }
            }
        }

        inner class HeadingHolder(val binding: ItemHeaderListBinding): RecyclerView.ViewHolder(binding.root)
    }


}
