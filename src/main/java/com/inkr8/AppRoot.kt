package com.inkr8

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.lifecycle.viewmodel.compose.viewModel
import com.inkr8.data.*
import com.inkr8.screens.*
import com.inkr8.rating.*
import com.inkr8.viewmodel.AppViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.inkr8.utils.SystemConfig

@Suppress("UNCHECKED_CAST")
class AppViewModelFactory(private val initialUser: Users) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AppViewModel(initialUser) as T
    }
}

@Composable
fun AppRoot(
    initialUser: Users,
    googleLauncher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>,
    onSessionEnded: () -> Unit
) {
    val viewModel: AppViewModel = viewModel(
        factory = AppViewModelFactory(initialUser)
    )
    
    val context = LocalContext.current
    val activity = LocalActivity.current

    // Force Placement Reveal if required, regardless of the current navigation state.
    val effectiveScreen = if (viewModel.isPlacementRevealRequired) {
        Screen.placementReveal
    } else {
        viewModel.currentScreen
    }

    when(effectiveScreen) {
        Screen.home -> MainPagerScreen(
            user = viewModel.currentUser,
            pantheonPosition = viewModel.pantheonPosition,
            initialPage = viewModel.pagerInitialPage,
            onNavigateToProfile = { viewModel.navigateTo(Screen.profile) },
            onNavigateToLeaderboard = { viewModel.navigateTo(Screen.leaderboard) },
            onNavigateToWriting = { gamemode, playMode, tournament ->
                viewModel.startWriting(gamemode, playMode, tournament)
            },
            onNavigateToTournamentDetails = { tournament ->
                viewModel.selectedTournament = tournament
                viewModel.navigateTo(Screen.tournamentDetails)
            },
            onNavigateToUserProfile = { userId ->
                viewModel.loadViewedUserProfile(userId)
                viewModel.navigateTo(Screen.userProfile)
            },
            onNavigateToCreateTournament = { viewModel.navigateTo(Screen.createTournament) }
        )
        
        Screen.practice -> {
            viewModel.navigateTo(Screen.home, page = 0)
        }

        Screen.competitions -> {
            viewModel.navigateTo(Screen.home, page = 2)
        }

        Screen.writing -> Writing(
            gamemode = viewModel.currentGamemode ?: StandardWriting,
            playMode = viewModel.currentPlayMode,
            tournamentContext = if (viewModel.currentPlayMode is PlayMode.Tournament) viewModel.selectedTournament else null,
            onAddSubmission = { submission ->
                viewModel.submitWriting(submission) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            },
            onNavigateBack = {
                if (viewModel.activeTournamentId != null && viewModel.currentPlayMode is PlayMode.Tournament) {
                    viewModel.navigateTo(Screen.tournamentDetails)
                } else {
                    viewModel.navigateTo(Screen.home, page = 1)
                }
            },
            onNavigateToResults = { viewModel.navigateTo(Screen.results) }
        )

        Screen.submissions -> SubmissionsScreen(
            user = viewModel.currentUser,
            submissions = viewModel.allSubmissions,
            isLoading = viewModel.isLoadingSubmissions,
            onNavigateToProfile = { viewModel.navigateTo(Screen.profile) },
            onSaveSubmission = { submissionId ->
                viewModel.saveSubmission(submissionId) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        Screen.savedSubmissions -> SavedSubmissionsScreen(
            savedSubmissions = viewModel.allSubmissions.filter { it.isSaved },
            isLoading = viewModel.isLoadingSubmissions,
            onNavigateBack = { viewModel.navigateTo(Screen.profile) },
            onDeleteSubmission = { submissionId ->
                viewModel.deleteSubmission(submissionId, {
                    Toast.makeText(context, "Entry Dissolved", Toast.LENGTH_SHORT).show()
                }) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        Screen.profile -> Profile(
            user = viewModel.currentUser,
            isOwner = true,
            pantheonPosition = viewModel.pantheonPosition,
            onNavigateBack = { viewModel.navigateTo(Screen.home, page = 1) },
            onNavigateToSubmissions = { viewModel.navigateTo(Screen.submissions) },
            onNavigateToSavedSubmissions = { viewModel.navigateTo(Screen.savedSubmissions) },
            onNavigateToSettings = { viewModel.navigateTo(Screen.settings) },
            onPurchaseReputation = { onSuccess ->
                viewModel.applyMeritAction(SystemConfig.ACTION_PURCHASE_REPUTATION, onSuccess) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        Screen.settings -> Settings(
            user = viewModel.currentUser,
            onNavigateBack = { viewModel.navigateTo(Screen.profile) },
            onLogout = {
                AuthManager.signOut()
                onSessionEnded()
            },
            onDeleteAccount = {
                viewModel.deleteAccount({
                    AuthManager.signOut()
                    onSessionEnded()
                }) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            },
            onChangeUsername = { viewModel.navigateTo(Screen.usernameSetup) },
            onExpandCap = {
                viewModel.applyMeritAction(SystemConfig.ACTION_EXPAND_MERIT_CAP, {
                    Toast.makeText(context, "Cap Expanded", Toast.LENGTH_SHORT).show()
                }) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        Screen.results -> {
            val res = viewModel.latestSubmission
            if (res == null) {
                LaunchedEffect(Unit) {
                    viewModel.loadLatestSubmission()
                }
                Box(
                    modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Results(
                    submission = res,
                    isPlaced = viewModel.currentUser.isPlaced,
                    onNavigateBack = {
                        viewModel.continueWithAd(activity, Screen.home) {
                            viewModel.pagerInitialPage = 1
                        }
                    },
                    onNavigateToPractice = {
                        viewModel.continueWithAd(activity, Screen.home) {
                            viewModel.pagerInitialPage = 0
                        }
                    }
                )
            }
        }

        Screen.leaderboard -> LeaderboardScreen(
            currentUser = viewModel.currentUser,
            onNavigateBack = { viewModel.navigateTo(Screen.home, page = 2) },
            onUserClick = { user ->
                viewModel.loadViewedUserProfile(user.id)
                viewModel.navigateTo(Screen.userProfile)
            }
        )

        Screen.tournamentDetails -> {
            val tournament = viewModel.selectedTournament
            var isEnrolling by remember(tournament?.id) { mutableStateOf(false) }

            DisposableEffect(tournament?.id) {
                if (tournament != null) {
                    viewModel.startObservingTournament(tournament.id)
                }
                onDispose {
                    viewModel.stopObservingTournament()
                }
            }

            if (tournament != null) {
                TournamentDetails(
                    tournament = tournament,
                    onNavigateBack = { viewModel.navigateTo(Screen.home, page = 2) },
                    onHostClick = {
                        viewModel.loadViewedUserProfile(tournament.creatorId)
                        viewModel.navigateTo(Screen.userProfile)
                    },
                    isEnrolled = viewModel.isEnrolledInSelectedTournament,
                    isSubmitted = viewModel.isSubmittedToSelectedTournament,
                    isEnrolling = isEnrolling,
                    onEnroll = {
                        if (viewModel.isEnrolledInSelectedTournament || isEnrolling) return@TournamentDetails
                        isEnrolling = true
                        viewModel.enrollInTournament(tournament.id, { isEnrolling = false }) { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            isEnrolling = false
                        }
                    },
                    onSubmitToTournament = {
                        viewModel.startWriting(
                            gamemode = when (tournament.gamemode) {
                                "ON_TOPIC" -> OnTopicWriting(
                                    theme = Theme(id = tournament.themeId ?: "", name = tournament.themeName ?: "Unknown Theme"),
                                    topic = Topic(id = tournament.topicId ?: "", name = tournament.topicName ?: "Unknown Topic")
                                )
                                else -> StandardWriting
                            },
                            playMode = PlayMode.Tournament(tournament.id),
                            tournament = tournament
                        )
                    },
                    onViewResults = { viewModel.navigateTo(Screen.tournamentResults) },
                    completedLeaderboard = viewModel.tournamentLeaderboard,
                    onOpenSubmission = { submission ->
                        viewModel.latestSubmission = submission
                        viewModel.navigateTo(Screen.results)
                    }
                )
            } else {
                viewModel.navigateTo(Screen.home, page = 2)
            }
        }

        Screen.userProfile -> {
            val viewed = viewModel.viewedUser
            if (viewed != null) {
                Profile(
                    user = viewed,
                    isOwner = viewed.id == viewModel.currentUser.id,
                    pantheonPosition = viewModel.viewedPantheonPosition,
                    lastTippedTimestamp = viewModel.globalTipCooldowns[viewed.id],
                    onNavigateBack = { viewModel.navigateTo(Screen.home, page = 2) },
                    onNavigateToSubmissions = { viewModel.navigateTo(Screen.submissions) },
                    onNavigateToSavedSubmissions = { viewModel.navigateTo(Screen.savedSubmissions) },
                    onNavigateToSettings = { viewModel.navigateTo(Screen.settings) },
                    onPurchaseReputation = { onSuccess ->
                        viewModel.applyMeritAction(SystemConfig.ACTION_PURCHASE_REPUTATION, onSuccess) { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onTip = { amount ->
                        viewModel.sendGlobalTip(viewed.id, amount) { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                InitialLoadingScreen()
            }
        }

        Screen.tournamentResults -> {
            val tournament = viewModel.selectedTournament
            if (tournament != null) {
                TournamentResultsScreen(
                    tournament = tournament,
                    leaderboard = viewModel.tournamentLeaderboard,
                    isLoading = viewModel.isTournamentLoading,
                    currentUserId = viewModel.currentUser.id,
                    tippedUsers = viewModel.tippedInCurrentTournament,
                    onNavigateBack = { viewModel.continueWithAd(activity, Screen.tournamentDetails) },
                    onTipUser = { recipientId, amount ->
                        viewModel.sendTip(tournament.id, recipientId, amount) { error ->
                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onOpenUserProfile = { userId ->
                        viewModel.continueWithAd(activity, Screen.userProfile) {
                            viewModel.loadViewedUserProfile(userId)
                        }
                    }
                )
            } else {
                viewModel.navigateTo(Screen.home, page = 2)
            }
        }

        Screen.loading -> LoadingScreen(
            elapsedSeconds = viewModel.loadingElapsedSeconds,
            isTimeout = viewModel.loadingTimeout,
            onReturnHome = { viewModel.navigateTo(Screen.home) }
        )

        Screen.createTournament -> CreateTournamentScreen(
            user = viewModel.currentUser,
            onCreate = { title, gamemode, prizePool, maxPlayers ->
                viewModel.createTournament(title, gamemode, prizePool, maxPlayers) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            },
            onBack = { viewModel.navigateTo(Screen.home, page = 2) }
        )

        Screen.postSubmissionAd -> PostSubmissionAdScreen(
            onContinue = {
                val next = viewModel.pendingNavigationAfterAd
                viewModel.pendingNavigationAfterAd = null
                viewModel.navigateTo(next ?: Screen.home)
            },
            onGoAdFree = { viewModel.navigateTo(Screen.paywall) }
        )

        Screen.paywall -> PaywallScreen(
            onBack = { viewModel.navigateTo(Screen.home, page = 1) },
            onSubscribe = {
                viewModel.enablePhilosopher({
                    Toast.makeText(context, "Status Elevated", Toast.LENGTH_SHORT).show()
                    viewModel.navigateTo(Screen.home, page = 1)
                }) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            }
        )

        Screen.usernameSetup -> UsernameSetupScreen(
            isSaving = false,
            errorMessage = null,
            onSubmit = { newName ->
                viewModel.changeUsername(newName, {
                    viewModel.navigateTo(Screen.settings) // Return to settings after name change
                }) { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            },
            checkAvailability = { name, callback ->
                viewModel.checkUsernameAvailability(name, callback)
            },
            validateUsername = { name ->
                viewModel.validateUsername(name)
            }
        )

        Screen.placementReveal -> PlacementRevealScreen(
            league = League.fromRating(viewModel.currentUser.rating),
            onContinue = {
                viewModel.onPlacementRevealSeen { }
            }
        )
    }
}
