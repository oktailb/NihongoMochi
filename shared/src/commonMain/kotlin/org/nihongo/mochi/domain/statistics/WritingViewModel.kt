package org.nihongo.mochi.domain.statistics

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository

class WritingViewModel(
    private val scoreRepository: ScoreRepository
) : ViewModel() {
    
    var userListPercentage = 0.0

    fun calculatePercentages() {
        calculateUserListPercentage()
    }

    private fun calculateUserListPercentage() {
        val scores = scoreRepository.getAllScores(ScoreManager.ScoreType.WRITING)
        if (scores.isEmpty()) {
            userListPercentage = 0.0
            return
        }
        val totalEncountered = scores.size
        val mastered = scores.count { entry -> (entry.value.successes - entry.value.failures) >= 10 }

        userListPercentage = if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }
}
