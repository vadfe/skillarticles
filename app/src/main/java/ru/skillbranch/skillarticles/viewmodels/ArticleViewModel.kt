package ru.skillbranch.skillarticles.viewmodels

class ArticleViewModel(articleId:String) :BaseViewModel<ArticleState>ArticleState() {
}
data class ArticleState(
    val isAutch: Boolean,
    val isLike: Boolean,
    val isBookmark: Boolean,
    val isDarkMode: Boolean,
    val isBigText: Boolean
)

