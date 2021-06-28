package com.azuresamples.msalandroidkotlinapp

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, OnFragmentInterationListener  {

    internal enum class AppFragment {
        SingleAccount,
        MultipleAccount,
        B2C
    }

    private var mCurrentFragment: AppFragment? = null

    private var mContentMain: ConstraintLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mContentMain = findViewById(R.id.content_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)

        //Set default fragment
        navigationView.setCheckedItem(R.id.nav_single_account)
        setCurrentFragment(AppFragment.SingleAccount)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        //val drawer = findViewById(R.id.drawer_layout)
        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {
                // Handle navigation view item clicks here.
                val id = item.itemId

                if (id == R.id.nav_single_account) {
                    setCurrentFragment(AppFragment.SingleAccount)
                }

                if (id == R.id.nav_multiple_account) {
                    setCurrentFragment(AppFragment.MultipleAccount)
                }

                if (id == R.id.nav_b2c) {
                    setCurrentFragment(AppFragment.B2C)
                }

                drawer_layout.removeDrawerListener(this)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setCurrentFragment(newFragment: AppFragment) {
        if (newFragment == mCurrentFragment) {
            return
        }

        mCurrentFragment = newFragment
        setHeaderString(mCurrentFragment as AppFragment)
        displayFragment(mCurrentFragment as AppFragment)
    }

    private fun setHeaderString(fragment: AppFragment) {
        when (fragment) {
            MainActivity.AppFragment.SingleAccount -> {
                supportActionBar!!.title = "Single Account Mode"
                return
            }

            MainActivity.AppFragment.MultipleAccount -> {
                supportActionBar!!.title = "Multiple Account Mode"
                return
            }

            MainActivity.AppFragment.B2C -> {
                supportActionBar!!.title = "MB2C Mode"
                return
            }
        }
    }

    private fun displayFragment(fragment: AppFragment) {
        when (fragment) {
            MainActivity.AppFragment.SingleAccount -> {
                attachFragment(SingleAccountModeFragment())
                return
            }

            MainActivity.AppFragment.MultipleAccount -> {
                attachFragment(MultipleAccountModeFragment())
                return
            }

            MainActivity.AppFragment.B2C -> {
                attachFragment(B2CModeFragment())
                return
            }
        }
    }

    private fun attachFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(mContentMain?.id as Int, fragment)
            .commit()
    }
}
