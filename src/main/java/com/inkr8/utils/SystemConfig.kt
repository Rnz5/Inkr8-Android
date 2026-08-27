package com.inkr8.utils

object SystemConfig {
    const val APP_VERSION = "pre-beta v0.6.6"

    //AdMob IDs (testing)
    const val ADMOB_APP_ID = "ca-app-pub-8013473658949032~8188988578"
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
    const val BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // Collection Names
    const val USERS_COLLECTION = "users"
    const val TOURNAMENTS_COLLECTION = "tournaments"
    const val SUBMISSIONS_COLLECTION = "submissions"
    const val USERNAMES_COLLECTION = "usernames"
    const val METADATA_COLLECTION = "metadata"
    const val WORDS_COLLECTION = "words"
    const val THEMES_COLLECTION = "themes"
    const val TOPICS_COLLECTION = "topics"

    // Functions Names
    const val APPLY_MERIT_ACTION = "applyMeritAction"
    const val ACTIVATE_PHILOSOPHER_STATUS = "activatePhilosopherStatus"
    const val ENROLL_IN_TOURNAMENT = "enrollInTournament"
    const val CREATE_USER_TOURNAMENT = "createUserTournament"
    const val SEND_TOURNAMENT_TIP = "sendTournamentTip"

    // Merit Action Types
    const val ACTION_PURCHASE_REPUTATION = "PURCHASE_REPUTATION_VIEW"
    const val ACTION_EXPAND_MERIT_CAP = "EXPAND_MERIT_CAP"
    const val ACTION_PURCHASE_EXAMPLE_SENTENCE = "PURCHASE_EXAMPLE_SENTENCE"
    const val ACTION_CHANGE_USERNAME = "CHANGE_USERNAME"
    const val ACTION_SAVE_SUBMISSION = "SAVE_SUBMISSION"
}
