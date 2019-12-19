package ru.skillbranch.skillarticles.viewmodels

class ArticleViewModel(articleId:String) :BaseViewModel<ArticleState>ArticleState() {
}
data class ArticleState(
    isDarkMode: Boolean,
    isBigText: Boolean
)

