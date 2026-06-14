package wombat.joshattic.us.ui.state

import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User

data class HomeUiState(
    val session: AuthSession? = null,
    val feed: List<Post> = emptyList(),
    val unreadNotifications: List<Notification> = emptyList(),
    val accountProfile: User? = null,
    val exploreProfile: User? = null,
    val explorePosts: List<Post> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val selectedPost: Post? = null,
    val isLoading: Boolean = false,
    val feedLoading: Boolean = false,
    val exploreLoading: Boolean = false,
    val commentsLoading: Boolean = false,
    val authLoading: Boolean = false,
    val accountLoading: Boolean = false,
    val notificationsLoading: Boolean = false,
    val accountPosts: List<Post> = emptyList(),
    val errorMessage: String? = null,
    val loginError: String? = null,
    val loginUsername: String = "",
    val loginPassword: String = "",
    val exploreQuery: String = "",
    val composeDraft: String = "",
    val commentDraft: String = "",
    val selectedTab: BottomTab = BottomTab.Home,
    val showComposer: Boolean = false,
    val composerDrafts: List<String> = emptyList()
) {
    val accountLabel: String = session?.username ?: "Account"
    val unreadNotificationCount: Int = unreadNotifications.size
}

enum class BottomTab {
    Home,
    Explore,
    Notifications,
    Account
}