package ru.skillbranch.skillarticles.viewmodels

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.ArticleData
import ru.skillbranch.skillarticles.data.ArticlePersonalInfo
import ru.skillbranch.skillarticles.data.repositories.ArticleRepository
import ru.skillbranch.skillarticles.extensions.data.toAppSettings
import ru.skillbranch.skillarticles.extensions.data.toArticlePersonalInfo
import ru.skillbranch.skillarticles.extensions.format

class ArticleViewModel(private val articleId:String) : BaseViewModel<ArticleState>(ArticleState()), IArticleViewModel {


    private val repository = ArticleRepository

    init {
        subscribeOnDataSource(getArticleData()){article, state ->
            article ?: return@subscribeOnDataSource null
            state.copy(
                shareLink = article.shareLink,
                title = article.title,
                category = article.category,
                categoryIcon = article.categoryIcon,
                date = article.date.format(),
                author = article.author?:null
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

    override fun getArticleContent(): LiveData<List<Any>?> {
        return repository.loadArticleContent(articleId)
    }

    override fun getArticleData(): LiveData<ArticleData?> {
        return repository.getArticle(articleId)
    }

    override fun getArticlePersonalInfo(): LiveData<ArticlePersonalInfo?> {
        return repository.loadArticlePersonalInfo(articleId)
    }

    override fun handleNightMode() {
        val settings = currentState.toAppSettings()
        repository.updateSettings(settings.copy(isDarkMode = !settings.isDarkMode))
    }

    override fun handleUpText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = true))
    }

    override fun handleDownText() {
        repository.updateSettings(currentState.toAppSettings().copy(isBigText = false))
    }

    override fun handleBookmark() {
        val toogleBookmark:() -> Unit ={
            val info: ArticlePersonalInfo = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isBookmark = !info.isBookmark))
        }
        toogleBookmark()
        val msg:Notify = if(currentState.isBookmark) Notify.TextMessage("Add to bookmarks")
        else Notify.TextMessage("Remove from bookmarks")
        notify(msg)
    }

    override fun handleLike() {
        val toogleLike :() -> Unit ={
            val info :ArticlePersonalInfo = currentState.toArticlePersonalInfo()
            repository.updateArticlePersonalInfo(info.copy(isLike = !info.isLike))
        }
        toogleLike()
        val msg:Notify = if(currentState.isLike) Notify.TextMessage("Mark is liked")
        else{
            Notify.ActionMessage(
                "Don`t like it anymore",
                "No, still like it",
                toogleLike
            )
        }
        notify(msg)
    }

    override fun handleShare() {
        Log.d("T", "handleShare")
        val msg:Notify = Notify.ErrorMessage("Share is not implemented","OK",null)
        notify(msg)
    }

    override fun handleToggleMenu() {
        updateState { it.copy(isShowMenu = !it.isShowMenu) }
    }

    override fun handleSearchMode(isSearch: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleSearch(query: String?) {
        Log.d("T", query)
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
    val shareLink: String? = null,
    val title: String? = null,
    val category: String? = null,
    val categoryIcon: Any? = null,
    val date: String? = null,
    val author: Any? = null,
    val posted: String? = null,
    val content: List<Any> = emptyList(),
    val reviews: List<Any> = emptyList()
)

