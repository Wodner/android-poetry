package com.guoziwei.poetry.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.ToxicBakery.viewpager.transforms.DepthPageTransformer
import com.guoziwei.poetry.R
import com.guoziwei.poetry.model.BaseResponse
import com.guoziwei.poetry.model.Poetry
import com.guoziwei.poetry.ui.adapter.SimpleFragmentPagerAdapter
import com.guoziwei.poetry.util.HttpUtil
import com.guoziwei.poetry.util.Utils
import com.guoziwei.poetry.view.YViewPager
import com.trello.rxlifecycle2.android.ActivityEvent
import com.yalantis.guillotine.animation.GuillotineAnimation
import com.yalantis.guillotine.interfaces.GuillotineListener
import immortalz.me.library.TransitionsHeleper


class MainActivity : BaseActivity(), View.OnClickListener {

    private var mMenu: GuillotineAnimation? = null
    private var viewpager: YViewPager? = null

    private var mIsMenuOpen = false

    private val fragments = mutableListOf<ContentFragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvSearch = findViewById<TextView>(R.id.tv_search)
        tvSearch.setOnClickListener(this)
        val tvTitle = findViewById<TextView>(R.id.tv_title)
        viewpager = findViewById(R.id.view_pager)
        viewpager?.setPageTransformer(false, DepthPageTransformer())
        val rootView = findViewById<FrameLayout>(R.id.root)
        val guillotineMenu = LayoutInflater.from(this).inflate(R.layout.guillotine_menu, rootView, false)
        rootView.addView(guillotineMenu)
        guillotineMenu.findViewById<View>(R.id.tv_toggle_simplify).setOnClickListener(this)
        guillotineMenu.findViewById<View>(R.id.tv_menu_list).setOnClickListener(this)

        mMenu = GuillotineAnimation.GuillotineBuilder(guillotineMenu, guillotineMenu.findViewById(R.id.tv_close), findViewById(R.id.tv_menu))
                .setActionBarViewForAnimation(findViewById(R.id.toolbar))
                .setClosedOnStart(true)
                .setGuillotineListener(object : GuillotineListener {
                    override fun onGuillotineClosed() {
                        mIsMenuOpen = false
                    }

                    override fun onGuillotineOpened() {
                        mIsMenuOpen = true
                    }
                })
                .build()

        requestData()
    }

    private fun requestData() {
        HttpUtil.create().randomTenPoetry()
                .compose(Utils.applyBizSchedulers<BaseResponse<MutableList<Poetry>>>())
                .compose(bindUntilEvent<BaseResponse<MutableList<Poetry>>>(ActivityEvent.DESTROY))
                .subscribe({
                    fragments.addAll(it.data.map { ContentFragment.newInstance(it) })
                    val adapter = SimpleFragmentPagerAdapter(supportFragmentManager, fragments)
                    viewpager?.adapter = adapter
                }, {
                    Utils.showToast(this, it.message)
                })
    }

    override fun onBackPressed() {
        if (mIsMenuOpen) {
            mMenu?.close()
        } else {
            super.onBackPressed()
        }

    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_toggle_simplify -> Utils.toggleSimplify(this)
            R.id.tv_search -> TransitionsHeleper.startActivity(this, SearchActivity::class.java, v)
            R.id.tv_menu_list -> startActivity(Intent(this, PoetryListActivity::class.java))
        }
    }


}
