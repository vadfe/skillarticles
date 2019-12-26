package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_root.*
import kotlinx.android.synthetic.main.layout_bottombar.*
import kotlinx.android.synthetic.main.layout_submenu.*
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.extensions.dpToIntPx
import ru.skillbranch.skillarticles.viewmodels.ArticleState
import ru.skillbranch.skillarticles.viewmodels.ArticleViewModel
import ru.skillbranch.skillarticles.viewmodels.Notify
import ru.skillbranch.skillarticles.viewmodels.ViewModelFactory

class RootActivity : AppCompatActivity() {
    private lateinit var viewModel:ArticleViewModel

    private  var searchItem:MenuItem? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)
        setupToolbar()
        setupSubmenu()
        setupBottomBar()
        val vmFactory = ViewModelFactory("0")
        viewModel = ViewModelProviders.of(this, vmFactory).get(ArticleViewModel::class.java)
        viewModel.observeState(this){
            renderUi(it)
        }
        viewModel.observeNotifications(this){
            renderNotification(it)
        }

    }
    private var searchView:SearchView? = null
    private var isSearchOpened = false
    private var isNeedSearchOpen = false
    private var searchValue = ""


    private fun opencloseSearch(){
        Log.w("T", "opencloseSearch isNeedSearchOpen=$isNeedSearchOpen isSearchOpened=$isSearchOpened")
        if(isNeedSearchOpen && !isSearchOpened && searchItem != null) {
            searchItem?.expandActionView()
            searchView?.setQuery(searchValue, false)
            isSearchOpened = true
            //searchView?.clearFocus()
        }
        if(!isNeedSearchOpen)
            isSearchOpened = false
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        searchItem = menu?.findItem(R.id.action_search)
        searchView = searchItem?.actionView as SearchView
        searchView?.queryHint = "строка поиска"

        opencloseSearch()

        searchItem?.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
                Log.w("T", "onMenuItemActionExpand item=$item")
                isSearchOpened = true
                viewModel.handleSearchMode(true)
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                Log.w("T", "onMenuItemActionCollapse item=$item")
                isSearchOpened = false
                viewModel.handleSearchMode(false)
                return true
            }
        })
        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                Log.w("T", "setOnQueryTextListener")
                viewModel.handleSearch(p0)
           //     searchValue = p0?:""
                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                Log.w("T", "onQueryTextChange")
                viewModel.handleSearch(p0)
             //   searchValue = p0?:""
                return true
            }

        })
        Log.w("T", "onCreateOptionsMenu isSearchOpened=$isSearchOpened")
        return super.onCreateOptionsMenu(menu)
    }
    private fun renderNotification(notify: Notify){
        val snackbar:Snackbar = Snackbar.make(coordinator_container, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))
        when(notify){
            is Notify.TextMessage -> {/*Nothing*/}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel){
                    notify.actionHandler.invoke()
                }
            }
            is Notify.ErrorMessage ->{
                with(snackbar){
                    setBackgroundTint(getColor(R.color.design_default_color_error))
                    setTextColor(getColor(android.R.color.white))
                    setActionTextColor(getColor(android.R.color.white))
                    setAction(notify.errLabel){
                        notify.errHandler?.invoke()
                    }
                }
            }
        }
        snackbar.show()
    }

    private fun renderUi(data: ArticleState) {
        Log.w("T", "renderUi isBookmark= ${data.isBookmark} isLike= ${data.isLike} isSearch= ${data.isSearch} searchQuery= ${data.searchQuery}")
        isNeedSearchOpen = data.isSearch
        searchValue = data.searchQuery?:""
        if(isSearchOpened != isNeedSearchOpen)
            opencloseSearch()
        btn_settings.isChecked = data.isShowMenu
        if(data.isShowMenu) submenu.open() else submenu.close()

        btn_like.isChecked = data.isLike
        btn_bookmark.isChecked = data.isBookmark

        switch_mode.isChecked = data.isDarkMode
        delegate.localNightMode =
            if(data.isDarkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO

        if(data.isBigText){
            tv_text_content.textSize = 18f
            btn_text_up.isChecked = true
            btn_text_down.isChecked = false
        }else{
            tv_text_content.textSize = 14f
            btn_text_up.isChecked = false
            btn_text_down.isChecked = true
        }

        tv_text_content.text = if(data.isLoadingContent) "loading" else data.content.first() as String

        toolbar.title = data.title ?: "loading"
        toolbar.subtitle = data.category ?: "loading"

        if(data.categoryIcon!= null) toolbar.logo = getDrawable(data.categoryIcon as Int)
    }

    private fun setupSubmenu(){
        btn_text_up.setOnClickListener{ viewModel.handleUpText()}
        btn_text_down.setOnClickListener{viewModel.handleDownText()}
        switch_mode.setOnClickListener{ viewModel.handleNightMode()}
    }

    private fun setupBottomBar(){
        btn_like.setOnClickListener{ viewModel.handleLike()}
        btn_bookmark.setOnClickListener{viewModel.handleBookmark()}
        btn_share.setOnClickListener{ viewModel.handleShare()}
        btn_settings.setOnClickListener{ viewModel.handleToggleMenu()}
    }

    private fun setupToolbar(){
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val logo: ImageView? = if(toolbar.childCount > 2) toolbar.getChildAt(2) as ImageView else null
        logo?.scaleType = ImageView.ScaleType.CENTER_CROP
        val lp: Toolbar.LayoutParams? = logo?.layoutParams as Toolbar.LayoutParams
        lp?.let{
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }
    }
}
