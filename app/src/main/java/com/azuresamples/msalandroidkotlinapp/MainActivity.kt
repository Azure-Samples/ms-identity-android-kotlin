// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.azuresamples.msalandroidkotlinapp

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.azuresamples.msalandroidkotlinapp.SingleAccountModeFragment
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnFragmentInteractionListener {
    internal enum class AppFragment {
        SingleAccount, MultipleAccount, B2C
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
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.addDrawerListener(object : DrawerListener {
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

                drawer.removeDrawerListener(this)
            }

            override fun onDrawerStateChanged(newState: Int) {}
        })
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun setCurrentFragment(newFragment: AppFragment) {
        if (newFragment == mCurrentFragment) {
            return
        }
        mCurrentFragment = newFragment
        setHeaderString(mCurrentFragment)
        displayFragment(mCurrentFragment)
    }

    private fun setHeaderString(fragment: AppFragment?) {
        when (fragment) {
            AppFragment.SingleAccount -> {
                supportActionBar!!.title = "Single Account Mode"
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLUE))
                return
            }
            AppFragment.MultipleAccount -> {
                supportActionBar!!.title = "Multiple Account Mode"
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLUE))
                return
            }
            AppFragment.B2C -> {
                supportActionBar!!.title = "B2C Mode"
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.BLUE))
                return
            }

            else -> {
                return
            }
        }
    }

    private fun displayFragment(fragment: AppFragment?) {
        when (fragment) {
            AppFragment.SingleAccount -> {
                attachFragment(SingleAccountModeFragment())
                return
            }
            AppFragment.MultipleAccount -> {
                attachFragment(MultipleAccountModeFragment())
                return
            }
            AppFragment.B2C -> {
                attachFragment(B2CModeFragment())
                return
            }


            else -> {
                return
            }
        }
    }

    private fun attachFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            .replace(mContentMain!!.id, fragment)
            .commit()
    }
}