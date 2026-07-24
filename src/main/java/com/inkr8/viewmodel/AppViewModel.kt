package com.inkr8.viewmodel

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import com.inkr8.AdManager
import com.inkr8.data.*
import com.inkr8.repository.*
import com.inkr8.rating.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppViewModel(
    initialUser: Users,
    private val submissionRepository: FirestoreSubmissionRepository = FirestoreSubmissionRepository(),
    private val tournamentRepository: FirestoreTournamentRepository = FirestoreTournamentRepository(),
    private val userRepository: UserRepository = UserRepository()
) : ViewModel() {

    // --- State ---
    var currentUser by mutableStateOf(initialUser)
        private set

    var currentScreen by mutableStateOf(Screen.home)
    var pagerInitialPage by mutableIntStateOf(1)
    
    var currentGamemode by mutableStateOf<Gamemode?>(null)
    var currentPlayMode by mutableStateOf<PlayMode>(PlayMode.Practice)
    var selectedTournament by mutableStateOf<Tournament?>(null)
    var activeTournamentId by mutableStateOf<String?>(null)
    
    var latestSubmission by mutableStateOf<Submissions?>(null)
    var allSubmissions by mutableStateOf<List<Submissions>>(emptyList())
    var isLoadingSubmissions by mutableStateOf(true)
    
    var pantheonPosition by mutableStateOf<Int?>(null)
    var selectedProfileUserId by mutableStateOf<String?>(null)
    var viewedUser by mutableStateOf<Users?>(null)
    var viewedPantheonPosition by mutableStateOf<Int?>(null)
    
    var submissionAdCounter by mutableIntStateOf(0)
    var pendingNavigationAfterAd by mutableStateOf<Screen?>(null)
    
    private var previousUserIsPlaced = initialUser.isPlaced

    // Loading screen state
    var loadingResolved by mutableStateOf(false)
    var loadingTimeout by mutableStateOf(false)
    var loadingElapsedSeconds by mutableIntStateOf(0)
    private var loadingPollJob: Job? = null

    // Tournament details & results state
    var tournamentLeaderboard by mutableStateOf<List<TournamentLeaderboardEntry>>(emptyList())
    var isTournamentLoading by mutableStateOf(false)
    var isEnrolledInSelectedTournament by mutableStateOf(false)
    var isSubmittedToSelectedTournament by mutableStateOf(false)

    // Listeners
    private var submissionsListener: ListenerRegistration? = null
    private var tournamentListener: ListenerRegistration? = null
    private var enrollmentListener: ListenerRegistration? = null
    private var submissionStatusListener: ListenerRegistration? = null
    private var loadingResultListener: ListenerRegistration? = null
    private var userObserverJob: Job? = null

    init {
        observeCurrentUser()
        observeSubmissions()
        observePantheonStatus()
    }

    private fun observeCurrentUser() {
        userObserverJob?.cancel()
        userObserverJob = viewModelScope.launch {
            userRepository.listenToUser(currentUser.id).collectLatest { updated ->
                updated?.let { 
                    currentUser = it 
                    observePantheonStatus()
                }
            }
        }
    }

    // --- Actions ---

    fun navigateTo(screen: Screen, page: Int? = null) {
        page?.let { pagerInitialPage = it }
        currentScreen = screen
    }

    fun startWriting(gamemode: Gamemode, playMode: PlayMode, tournament: Tournament?) {
        currentGamemode = gamemode
        currentPlayMode = playMode
        selectedTournament = tournament
        latestSubmission = null
        activeTournamentId = tournament?.id
        navigateTo(Screen.writing)
    }

    private fun observeSubmissions() {
        submissionsListener?.remove()
        isLoadingSubmissions = true
        submissionsListener = submissionRepository.listenToAllSubmissions(
            authorId = currentUser.id,
            onUpdate = { updated ->
                allSubmissions = updated
                isLoadingSubmissions = false
            },
            onError = { error ->
                Log.e("AppViewModel", "Submissions listener error: ${error.message}")
                isLoadingSubmissions = false
            }
        )
    }

    fun observePantheonStatus() {
        viewModelScope.launch {
            if (currentUser.rating >= PantheonManager.MIN_RATING - 20) {
                userRepository.getTop100Users { top100 ->
                    val (isPantheon, position) = PantheonManager.checkPantheonStatus(currentUser, top100)
                    pantheonPosition = if (isPantheon) position else null
                }
            } else {
                pantheonPosition = null
            }
        }
    }

    fun submitWriting(submission: Submissions, onError: (String) -> Unit) {
        val finalSubmission = submission.copy(
            authorId = currentUser.id,
            status = SubmissionStatus.PENDING,
            evaluation = null
        )

        val isTournament = finalSubmission.playmode == "TOURNAMENT" && activeTournamentId != null

        if (isTournament) {
            tournamentRepository.submitToTournament(
                tournamentId = activeTournamentId!!,
                userId = currentUser.id,
                submission = finalSubmission,
                onSuccess = {
                    navigateTo(Screen.tournamentDetails)
                },
                onError = { e -> onError(e.message ?: "Tournament submission failed") }
            )
        } else {
            submissionRepository.addSubmission(
                submission = finalSubmission,
                onSuccess = {
                    startLoadingResult()
                },
                onError = { e ->
                    userRepository.finishRankedSession(currentUser.id)
                    onError(e.message ?: "Submission failed")
                }
            )
        }
    }

    fun saveSubmission(submissionId: String, onError: (String) -> Unit) {
        submissionRepository.saveSubmission(
            submissionId = submissionId,
            onSuccess = {},
            onError = { e -> onError(e.message ?: "Failed to save") }
        )
    }

    fun deleteSubmission(submissionId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        submissionRepository.deleteSubmission(
            submissionId = submissionId,
            onSuccess = onSuccess,
            onError = { e -> onError(e.message ?: "Failed to delete") }
        )
    }

    fun deleteAccount(onSuccess: () -> Unit, onError: (String) -> Unit) {
        userRepository.deleteAccount(
            userId = currentUser.id,
            onSuccess = onSuccess,
            onError = { e -> onError(e.message ?: "Failed to delete account") }
        )
    }

    fun enablePhilosopher(onSuccess: () -> Unit, onError: (String) -> Unit) {
        userRepository.enablePhilosopher(
            purchaseToken = "test_token",
            productId = "philosopher_sub",
            onSuccess = {
                onSuccess()
            },
            onError = { e -> onError(e.message ?: "Failed to enable Philosopher") }
        )
    }

    fun changeUsername(newName: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        userRepository.changeUsernameWithMerit(
            newUsername = newName,
            onSuccess = {
                onSuccess()
            },
            onError = { e -> onError(e.message ?: "Failed to change username") }
        )
    }

    fun checkUsernameAvailability(name: String, callback: (Boolean) -> Unit) {
        userRepository.isUsernameAvailable(name, callback)
    }

    fun validateUsername(name: String): String? {
        return userRepository.validateUsername(name)
    }

    fun createTournament(title: String, gamemode: String, prizePool: Long, maxPlayers: Int, onError: (String) -> Unit) {
        tournamentRepository.createUserTournament(
            title, gamemode, prizePool, maxPlayers,
            onSuccess = { navigateTo(Screen.home, page = 2) },
            onError = { e -> onError(e.message ?: "Failed to create tournament") }
        )
    }

    fun loadLatestSubmission() {
        submissionRepository.getLastSubmission(
            onSuccess = { latestSubmission = it },
            onError = { it.printStackTrace() }
        )
    }

    fun loadViewedUserProfile(userId: String) {
        selectedProfileUserId = userId
        userRepository.getUserById(userId) { user ->
            viewedUser = user
            if (user != null && user.rating >= PantheonManager.MIN_RATING) {
                userRepository.getTop100Users { top100 ->
                    val (isPantheon, position) = PantheonManager.checkPantheonStatus(user, top100)
                    viewedPantheonPosition = if (isPantheon) position else null
                }
            } else {
                viewedPantheonPosition = null
            }
        }
    }

    fun enrollInTournament(tournamentId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        tournamentRepository.enrollUserViaFunction(
            tournamentId = tournamentId,
            onSuccess = {
                onSuccess()
            },
            onError = { e -> onError(e.message ?: "Failed to enroll") }
        )
    }

    private fun startLoadingResult() {
        loadingResolved = false
        loadingTimeout = false
        loadingElapsedSeconds = 0
        navigateTo(Screen.loading)
        
        loadingResultListener?.remove()
        loadingResultListener = submissionRepository.getLastSubmissionRealtime(
            onUpdate = { submission ->
                handleSubmissionUpdate(submission)
            },
            onError = { it.printStackTrace() }
        )

        loadingPollJob?.cancel()
        loadingPollJob = viewModelScope.launch {
            var pollCount = 0
            while (!loadingResolved && !loadingTimeout) {
                delay(3000)
                pollCount++
                loadingElapsedSeconds = pollCount * 3
                if (loadingElapsedSeconds > 90) {
                    loadingTimeout = true
                    break
                }
                submissionRepository.getLastSubmission(
                    onSuccess = { submission ->
                        submission?.let { handleSubmissionUpdate(it) }
                    },
                    onError = { it.printStackTrace() }
                )
            }
        }
    }

    private fun handleSubmissionUpdate(submission: Submissions) {
        if (!loadingResolved && !loadingTimeout) {
            if (submission.status == SubmissionStatus.EVALUATED) {
                loadingResolved = true
                latestSubmission = submission
                
                val justGotPlaced = !previousUserIsPlaced && currentUser.isPlaced && !currentUser.hasSeenPlacementReveal
                previousUserIsPlaced = currentUser.isPlaced
                navigateTo(if (justGotPlaced) Screen.placementReveal else Screen.results)
                
            } else if (submission.status == SubmissionStatus.FAILED) {
                loadingResolved = true
                navigateTo(Screen.home)
            }
        }
    }

    fun continueWithAd(activity: Activity?, nextScreen: Screen, beforeNavigate: (() -> Unit)? = null) {
        if (currentUser.isPhilosopher) {
            beforeNavigate?.invoke()
            navigateTo(nextScreen)
            return
        }
        submissionAdCounter++
        pendingNavigationAfterAd = nextScreen
        if (submissionAdCounter % 2 == 0) {
            activity?.let { AdManager.showAd(it) }
            beforeNavigate?.invoke()
            navigateTo(nextScreen)
        } else {
            beforeNavigate?.invoke()
            navigateTo(Screen.postSubmissionAd)
        }
    }

    fun loadTournamentResults(tournamentId: String) {
        isTournamentLoading = true
        tournamentRepository.getLeaderboard(
            tournamentId = tournamentId,
            onSuccess = { results ->
                val authorIds = results.map { it.authorId }
                userRepository.getUsersByIds(authorIds) { usersMap ->
                    tournamentLeaderboard = results.map { submission ->
                        TournamentLeaderboardEntry(
                            submission = submission,
                            user = usersMap[submission.authorId]
                        )
                    }
                    isTournamentLoading = false
                }
            },
            onError = { e ->
                e.printStackTrace()
                tournamentLeaderboard = emptyList()
                isTournamentLoading = false
            }
        )
    }

    fun startObservingTournament(tournamentId: String) {
        stopObservingTournament()
        tournamentListener = tournamentRepository.listenToTournament(
            tournamentId = tournamentId,
            onUpdate = { 
                selectedTournament = it 
                if (it?.status == TournamentStatus.COMPLETED) {
                    loadTournamentResults(it.id)
                }
            },
            onError = { it.printStackTrace() }
        )
        enrollmentListener = tournamentRepository.listenToEnrollmentStatus(
            tournamentId = tournamentId,
            userId = currentUser.id,
            onUpdate = { isEnrolledInSelectedTournament = it },
            onError = { it.printStackTrace() }
        )
        submissionStatusListener = tournamentRepository.listenToSubmissionStatus(
            tournamentId = tournamentId,
            userId = currentUser.id,
            onUpdate = { isSubmittedToSelectedTournament = it },
            onError = { it.printStackTrace() }
        )
    }

    fun stopObservingTournament() {
        tournamentListener?.remove()
        enrollmentListener?.remove()
        submissionStatusListener?.remove()
        isEnrolledInSelectedTournament = false
        isSubmittedToSelectedTournament = false
    }

    fun applyMeritAction(action: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        userRepository.applyMeritAction(
            action = action,
            onSuccess = {
                onSuccess()
            },
            onError = { e -> onError(e.message ?: "Action failed") }
        )
    }

    fun sendTip(tournamentId: String, recipientId: String, amount: Long, onError: (String) -> Unit) {
        tournamentRepository.sendTournamentTip(
            tournamentId = tournamentId,
            tipperId = currentUser.id,
            recipientId = recipientId,
            amount = amount,
            onSuccess = {},
            onError = { e -> onError(e.message ?: "Tip failed") }
        )
    }

    fun onPlacementRevealSeen(onComplete: () -> Unit) {
        userRepository.markPlacementRevealSeen(
            userId = currentUser.id,
            onSuccess = {
                navigateTo(Screen.results)
                onComplete()
            },
            onError = { it.printStackTrace() }
        )
    }

    override fun onCleared() {
        submissionsListener?.remove()
        tournamentListener?.remove()
        enrollmentListener?.remove()
        submissionStatusListener?.remove()
        loadingResultListener?.remove()
        userObserverJob?.cancel()
        loadingPollJob?.cancel()
        super.onCleared()
    }
}
