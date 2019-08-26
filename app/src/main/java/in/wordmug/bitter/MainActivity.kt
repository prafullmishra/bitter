package `in`.wordmug.bitter

import `in`.wordmug.bitter.databinding.ActivityMainBinding
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController

class MainActivity : AppCompatActivity(), LifecycleOwner {

    private lateinit var vmodel: MainViewModel
    private lateinit var binding: ActivityMainBinding


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menu?.let {
            menuInflater.inflate(R.menu.home_menu, it)
            return true
        }
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            vmodel.optionSelected.value = it.itemId
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        setSupportActionBar(binding.toolbar)
        vmodel  = ViewModelProviders.of(this).get(MainViewModel::class.java)

        val controller =findNavController(R.id.myNavHostFragment)
        controller.addOnDestinationChangedListener{ _, destination, _ ->
            if(destination.id == R.id.homeFragment)
            {
                binding.toolbar.visibility = View.VISIBLE
            }
            else
            {
                binding.toolbar.visibility = View.GONE
            }
        }

        vmodel.target.observe(this, Observer { msg->
            if(!msg.isNullOrEmpty()) Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        })
    }
}
