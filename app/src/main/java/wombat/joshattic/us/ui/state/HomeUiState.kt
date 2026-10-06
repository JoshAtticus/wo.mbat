package wombat.joshattic.us.ui.state

import androidx.compose.runtime.Immutable
import wombat.joshattic.us.data.model.AuthSession
import wombat.joshattic.us.data.model.Comment
import wombat.joshattic.us.data.model.Notification
import wombat.joshattic.us.data.model.Post
import wombat.joshattic.us.data.model.User

enum class LoginStep { USERNAME, PASSWORD }

@Immutable
data class HomeUiState(
    val session: AuthSession? = null,
    val feed: List<Post> = emptyList(),
    val unreadNotifications: List<Notification> = emptyList(),
    val readNotifications: List<Notification> = emptyList(),
    val inAppNotification: Notification? = null,
    val hasInitialNotificationsLoaded: Boolean = false,
    val unreadNotificationsPage: Int = 1,
    val unreadNotificationsLast: Boolean = false,
    val readNotificationsPage: Int = 1,
    val readNotificationsLast: Boolean = false,
    val notificationsLoadingMore: Boolean = false,
    val socketUnreadCount: Int = 0,
    val accountProfile: User? = null,
    val exploreProfile: User? = null,
    val explorePosts: List<Post> = emptyList(),
    val exploreTrendingPosts: List<Post> = emptyList(),
    val exploreTrendingLoading: Boolean = false,
    val exploreFollowStatusesLoading: Boolean = false,
    val exploreTrendingTimeframe: String? = null,
    val exploreSearchQuery: String = "",
    val exploreSearchActive: Boolean = false,
    val exploreSearchPostResults: List<Post> = emptyList(),
    val exploreSearchUserResults: List<User> = emptyList(),
    val exploreSearchLoading: Boolean = false,
    val exploreFrogMessage: String? = null,
    val viewingProfileUsername: String? = null,
    val viewingProfile: User? = null,
    val viewingProfilePosts: List<Post> = emptyList(),
    val viewingProfileLoading: Boolean = false,
    val viewingProfileIsFollowing: Boolean? = null,
    val viewingProfileFollowLoading: Boolean = false,
    val blockedUsernames: Set<String> = emptySet(),
    val comments: List<Comment> = emptyList(),
    val selectedPost: Post? = null,
    val viewRepostsPost: Post? = null,
    val reposts: List<Post> = emptyList(),
    val repostsLoading: Boolean = false,
    val repostsError: String? = null,
    val isLoading: Boolean = false,
    val feedLoading: Boolean = false,
    val commentsLoading: Boolean = false,
    val authLoading: Boolean = false,
    val accountLoading: Boolean = false,
    val notificationsLoading: Boolean = false,
    val accountPosts: List<Post> = emptyList(),
    val errorMessage: String? = null,
    val toastMessage: String? = null,
    val loginError: String? = null,
    val loginStep: LoginStep = LoginStep.USERNAME,
    val loginUsername: String = "",
    val loginPassword: String = "",
    val composeDraft: String = "",
    val commentDraft: String = "",
    val selectedTab: BottomTab = BottomTab.Home,
    val showComposer: Boolean = false,
    val composerDrafts: List<String> = emptyList(),
    val composeRepostId: String? = null,
    val composeEditPostId: String? = null,
    val composeEditPostAuthor: String? = null,
    val composeOriginalContent: String? = null,
    val commentReplyParent: Comment? = null,
    val focusedComment: Comment? = null,
    val scrollToCommentId: String? = null,
    val showEditProfile: Boolean = false,
    val editProfileBio: String = "",
    val editProfileLoading: Boolean = false,
    val editProfileError: String? = null,
    val profileCacheBuster: Long = 0L,
    val fullScreenImages: List<String>? = null,
    val fullScreenPost: Post? = null,
    val fullScreenImageUsername: String? = null,
    val initialFullScreenImageIndex: Int = 0,
    val isBanned: Boolean = false,
    val banReason: String? = null,
    val showBannedPopup: Boolean = false,
    val feedPage: Int = 1,
    val feedLast: Boolean = false,
    val accountPage: Int = 1,
    val accountLast: Boolean = false,
    val viewingProfilePage: Int = 1,
    val viewingProfileLast: Boolean = false,
    val savedAccounts: List<AuthSession> = emptyList(),
    val savedAccountUnreadCounts: Map<String, Int> = emptyMap(),
    val isAddingAccount: Boolean = false,
    val showReportDialog: Boolean = false,
    val reportPostId: String? = null,
    val reportCommentId: String? = null,
    val reportTargetUsername: String? = null,
    val reportSuccessMessage: String? = null,
    val reportReason: String = "",
    val reportLoading: Boolean = false,
    val userListToShow: List<User>? = null,
    val userListTitle: String = "",
    val userListUsername: String? = null,
    val userListType: String? = null,
    val userListLoading: Boolean = false,
    val userListPage: Int = 1,
    val userListIsLastPage: Boolean = true,
    val userListLoadingMore: Boolean = false,
    val scrollToTop: Boolean = false,
    val newPostsUsernames: List<String> = emptyList(),
    val viewingWallUsername: String? = null,
    val wallComments: List<Comment> = emptyList(),
    val wallCommentsLoading: Boolean = false,
    val wallCommentsPage: Int = 1,
    val wallCommentsLast: Boolean = false,
    val wallCommentDraft: String = "",
    val wallCommentReplyParent: Comment? = null,
    
    val showSettings: Boolean = false,
    val settingsCategory: SettingsCategory? = null,
    val showImagesInFeed: Boolean = true,
    val showNewPostsPopup: Boolean = true,
    val inAppNotifications: Boolean = true,
    val markReadWhenOpened: Boolean = true,
    val markReadWhenTabOpened: Boolean = false,
    val openLinksInApp: Boolean = true,
    val linkPreviewPriority: String = "images",
    val wearAccount: String = "Last used on phone",
    val wearShowImages: Boolean = false,
    val wearShowProfilePictures: Boolean = true,
    val wearFeedType: String = "Home",
    val isOnline: Boolean = true,
    val followedUsernames: Set<String> = emptySet(),
    val followLoadingUsernames: Set<String> = emptySet(),
    val blockedQuoteHandling: String = "warning",
    val showBlockedQuoteRevealButton: Boolean = false,
    val showBlockedCommentRevealButton: Boolean = false,
    val themeSource: String = "account",
    val customThemeColor: String = "indigo",
    val customThemeDynamic: Boolean = false,
    val blockedWarningTarget: BlockedWarningTarget? = null
) {
    val accountLabel: String = session?.username ?: "Account"
    val unreadNotificationCount: Int = maxOf(unreadNotifications.size, socketUnreadCount)
}

enum class BottomTab {
    Home,
    Explore,
    Notifications,
    Account
}

enum class SettingsCategory {
    FEED, THEMING, NOTIFICATIONS, LINKS, BLOCKED_USERS, WEAR_OS, ABOUT
}

sealed interface BlockedWarningTarget {
    data class Profile(val username: String) : BlockedWarningTarget
    data class Post(val post: wombat.joshattic.us.data.model.Post) : BlockedWarningTarget
}

