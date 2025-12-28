package org.nihongo.mochi.domain.statistics

import androidx.lifecycle.ViewModel
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType

class ReadingViewModel : ViewModel() {
    var n5Percentage = 0.0
    var n4Percentage = 0.0
    var n3Percentage = 0.0
    var n2Percentage = 0.0
    var n1Percentage = 0.0
    var words1000Percentage = 0.0
    var words2000Percentage = 0.0
    var words3000Percentage = 0.0
    var words4000Percentage = 0.0
    var words5000Percentage = 0.0
    var words6000Percentage = 0.0
    var words7000Percentage = 0.0
    var words8000Percentage = 0.0
    var userListPercentage = 0.0

    fun calculatePercentages() {
        calculateUserListPercentage()
        // Here we could add logic to calculate other percentages if data is available via repositories
    }

    private fun calculateUserListPercentage() {
        val scores = ScoreManager.getAllScores(ScoreType.READING)
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
