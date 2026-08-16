package com.inkr8.economy

import kotlin.math.ceil

object TournamentEconomyCalculator {

    val ALLOWED_TIP_AMOUNTS = listOf(
        100L,
        150L,
        200L
    )

    /**
     * Calculates the economy projection for a tournament.
     * Uses safe math to prevent crashes on invalid inputs.
     */
    fun calculateProjection(prizePool: Long, maxPlayers: Int): TournamentEconomyProjection {
        // Safe defaults to prevent division by zero or negative results
        val safePrizePool = prizePool.coerceAtLeast(1L)
        val safeMaxPlayers = maxPlayers.coerceAtLeast(2)

        val systemFee = (safePrizePool * EconomyConfig.SYSTEM_CREATION_FEE_PERCENT).toLong()

        val targetRevenue = (safePrizePool * (1 + EconomyConfig.PROFIT_MARGIN_PERCENT)).toLong()

        // ceil ensures we don't undercharge due to rounding
        val entranceFee = ceil(targetRevenue.toDouble() / safeMaxPlayers).toLong()

        val totalRevenue = entranceFee * safeMaxPlayers

        val netProfit = totalRevenue - safePrizePool - systemFee

        val breakEvenPlayers = ceil((safePrizePool + systemFee).toDouble() / entranceFee).toInt()

        return TournamentEconomyProjection(
            prizePool = safePrizePool,
            maxPlayers = safeMaxPlayers,
            entranceFee = entranceFee,
            totalRevenue = totalRevenue,
            systemFee = systemFee,
            netProfit = netProfit,
            breakEvenPlayers = breakEvenPlayers
        )
    }
}