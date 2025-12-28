package org.nihongo.mochi.domain.statistics

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType

class WritingViewModel : ViewModel() {
    
    var userListPercentage = 0.0

    fun calculatePercentages() {
        calculateUserListPercentage()
    }

    private fun calculateUserListPercentage() {
        val scores = ScoreManager.getAllScores(ScoreType.WRITING)
        if (scores.isEmpty()) {
            userListPercentage = 0.0
            return
        }
        val totalEncountered = scores.size
        val mastered = scores.count { (_, score) -> (score.successes - score.failures) >= 10 }

        userListPercentage = if (totalEncountered > 0) {
            (mastered.toDouble() / totalEncountered.toDouble()) * 100.0
        } else {
            0.0
        }
    }
}
