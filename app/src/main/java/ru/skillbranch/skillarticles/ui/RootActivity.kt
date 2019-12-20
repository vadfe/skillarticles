package ru.skillbranch.skillarticles.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toolbar
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

    private fun renderNotification(notify: Notify){
        val snackbar:Snackbar = Snackbar.make(coordinator_conrainer, notify.message, Snackbar.LENGTH_LONG)
            .setAnchorView(bottombar)
            .setActionTextColor(getColor(R.color.color_accent_dark))
        when(notify){
            is Notify.TextMessage -> {/*Nothing*/}
            is Notify.ActionMessage -> {
                snackbar.setActionTextColor(getColor(R.color.color_accent_dark))
                snackbar.setAction(notify.actionLabel){
                    notify.actionHandler?.invoke()
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
        btn_settings.isChecked = data.isShowMenu
        if(data.isShowMenu) submenu.open() else submenu.close()
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
/*        val lp: Toolbar.LayoutParams? = logo?.layoutParams as Toolbar.LayoutParams
        lp?.let{
            it.width = this.dpToIntPx(40)
            it.height = this.dpToIntPx(40)
            it.marginEnd = this.dpToIntPx(16)
            logo.layoutParams = it
        }*/
    }
}
