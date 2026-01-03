package org.nihongo.mochi.ui

import org.jetbrains.compose.resources.StringResource
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

object ResourceUtils {
    /**
     * Attempts to find a string resource dynamically by name.
     * This is a workaround until Compose Multiplatform supports dynamic resource lookup better.
     * It maps known keys to their generated resource IDs.
     */
    fun resolveStringResource(key: String): StringResource? {
        return when(key) {
            "jlpt" -> Res.string.section_jlpt
            "joyo" -> null // No direct match in provided strings, maybe section_school?
            "kana" -> Res.string.results_section_kanas
            "level_hiragana", "hiragana" -> Res.string.level_hiragana
            "level_katakana", "katakana" -> Res.string.level_katakana
            "level_n5", "n5" -> Res.string.level_n5
            "level_n4", "n4" -> Res.string.level_n4
            "level_n3", "n3" -> Res.string.level_n3
            "level_n2", "n2" -> Res.string.level_n2
            "level_n1", "n1" -> Res.string.level_n1
            // Grades mappings
            "level_grade_1", "grade_1", "grade1" -> Res.string.level_grade_1
            "level_grade_2", "grade_2", "grade2" -> Res.string.level_grade_2
            "level_grade_3", "grade_3", "grade3" -> Res.string.level_grade_3
            "level_grade_4", "grade_4", "grade4" -> Res.string.level_grade_4
            "level_grade_5", "grade_5", "grade5" -> Res.string.level_grade_5
            "level_grade_6", "grade_6", "grade6" -> Res.string.level_grade_6
            "level_college_1", "grade_7", "grade7" -> Res.string.level_college_1
            "level_college_2", "grade_8", "grade8" -> Res.string.level_college_2
            "level_lycee_1", "grade_9", "grade9" -> Res.string.level_lycee_1
            "level_lycee_2", "grade_10", "grade10" -> Res.string.level_lycee_2
            "level_native_challenge" -> Res.string.level_native_challenge
            "level_no_reading" -> Res.string.level_no_reading
            "level_no_meaning" -> Res.string.level_no_meaning

            // Descriptions mappings
            "level_hiragana_desc" -> Res.string.level_hiragana_desc
            "level_katakana_desc" -> Res.string.level_katakana_desc
            "level_n5_desc" -> Res.string.level_n5_desc
            "level_n4_desc" -> Res.string.level_n4_desc
            "level_n3_desc" -> Res.string.level_n3_desc
            "level_n2_desc" -> Res.string.level_n2_desc
            "level_n1_desc" -> Res.string.level_n1_desc
            "level_grade_1_desc" -> Res.string.level_grade_1_desc
            "level_grade_2_desc" -> Res.string.level_grade_2_desc
            "level_grade_3_desc" -> Res.string.level_grade_3_desc
            "level_grade_4_desc" -> Res.string.level_grade_4_desc
            "level_grade_5_desc" -> Res.string.level_grade_5_desc
            "level_grade_6_desc" -> Res.string.level_grade_6_desc
            "level_college_1_desc" -> Res.string.level_college_1_desc
            "level_college_2_desc" -> Res.string.level_college_2_desc
            "level_lycee_1_desc" -> Res.string.level_lycee_1_desc
            "level_lycee_2_desc" -> Res.string.level_lycee_2_desc
            "level_native_challenge_desc" -> Res.string.level_native_challenge_desc
            "level_no_reading_desc" -> Res.string.level_no_reading_desc
            "level_no_meaning_desc" -> Res.string.level_no_meaning_desc
            "section_fundamentals_desc" -> Res.string.section_fundamentals_desc
            "section_jlpt_desc" -> Res.string.section_jlpt_desc
            "section_school_desc" -> Res.string.section_school_desc
            "section_challenges_desc" -> Res.string.section_challenges_desc

            "secondary_school" -> Res.string.results_section_school
            "frequency" -> Res.string.results_section_frequency
            "user_list" -> Res.string.reading_user_list
            "writing_user_lists" -> Res.string.writing_user_lists
            "my_list" -> Res.string.reading_user_list
            // Mappings for Dictionary Screen Spinner (Native Challenge, School, etc.)
            "school" -> Res.string.section_school
            "challenges" -> Res.string.section_challenges
            "challenge" -> Res.string.section_challenges
            "section_jlpt" -> Res.string.section_jlpt
            "section_school" -> Res.string.section_school
            "section_challenges" -> Res.string.section_challenges
            "section_challenge" -> Res.string.section_challenges
            "section_fundamentals" -> Res.string.section_fundamentals
            "native_challenge" -> Res.string.level_native_challenge
            "no_reading" -> Res.string.level_no_reading
            "no_meaning" -> Res.string.level_no_meaning
            else -> null
        }
    }
}
