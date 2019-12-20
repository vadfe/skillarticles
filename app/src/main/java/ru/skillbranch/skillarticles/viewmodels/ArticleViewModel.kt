package ru.skillbranch.skillarticles.viewmodels

import android.view.View
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId:String) : BaseViewModel<ArticleState>(ArticleState()) {


    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()){article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                sharedLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format()
            )
        }

        subscribeOnDataSource(getArticleContent()){ content, state ->
            content ?: return@subscribeOnDataSource null
            state.copy(
                isLoadingContent = false,
                content = content
            )
        }

        subscribeOnDataSource(getArticlePersonalInfo()){ info, state ->
            info ?: return@subscribeOnDataSource null
            state.copy(
                isBookmark = info.isBookmark,
                isLike = info.isLike
            )
        }

        subscribeOnDataSource(repository.getAppSettings()){ settings, state ->
            state.copy(
                isDarkMode = settings.isDarkMode,
                isBigText = settings.isBigText
            )
        }
    }

    private fun getArticleContent():LiveData<List<Any>?>{
        return repository.loadArticleContent(articleId)
    }

    private fun getArticleData():LiveData<ArticleData?>{
        return repository.getArticle(articleId)
    }

    private fun getArticlePersonalInfo():LiveData<ArticlePersonalInfo?>{
        return repository.loadArticlePersonalInfo(articleId)
    }

    fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    fun handleBookmark() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun handleLike() {
        val toogleLike :() -> Unit ={
            val info :ArticlePersonalInfo = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toogleLike()
        val msg:Notify = if(currentState.isLike) Notify.TextMessage("Mark is liked")
        else{
            Notify.ActionMessage(
                "Don`t like anymore",
                "No, still like it",
                toogleLike
            )
        }
        notify(msg)
    }

    fun handleShare() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }


}

data class ArticleState(
    val isAutch: Boolean = false,
    val isLoadingContent:Boolean = true,
    val isLoadingReviews:Boolean = true,
    val isLike: Boolean = false,
    val isBookmark: Boolean = false,
    val isShowMenu:Boolean = false,
    val isBigText:Boolean = false,
    val isDarkMode: Boolean = false,
    val isSearch: Boolean = false,
    val searchQuery: String? = null,
    val searchResuts: List<Pair<Int, Int>> = emptyList(),
    val searchPosition: Int = 0,
    val sharedLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val autor: String? = null,
    val posted: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)

