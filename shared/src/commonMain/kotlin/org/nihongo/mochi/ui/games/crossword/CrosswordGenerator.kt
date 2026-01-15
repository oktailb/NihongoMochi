package org.nihongo.mochi.ui.games.crossword

import org.nihongo.mochi.domain.words.WordEntry
import kotlin.random.Random

class CrosswordGenerator(
    private val availableWords: List<WordEntry>,
    private val targetWordCount: Int,
    private val mode: CrosswordMode = CrosswordMode.KANAS,
    private val gridSize: Int = 16
) {
    private val grid = Array(gridSize) { Array(gridSize) { "" } }
    private val placedWords = mutableListOf<CrosswordWord>()

    private fun cleanPhonetics(p: String): String {
        return p.split("/")
            .firstOrNull { it.isNotBlank() }
            ?.replace(".", "")
            ?.replace(" ", "")
            ?: ""
    }

    private fun isPureKanji(text: String): Boolean {
        if (text.isEmpty()) return false
        return text.all { char ->
            val code = char.code
            // Kanji range
            (code in 0x4E00..0x9FAF) || (code in 0x3400..0x4DBF)
        }
    }

    fun generate(): Pair<List<CrosswordCell>, List<CrosswordWord>> {
        val candidates = availableWords
            .map { it.copy(phonetics = cleanPhonetics(it.phonetics)) }
            .filter { entry ->
                val solution = if (mode == CrosswordMode.KANJIS) entry.text else entry.phonetics
                val length = solution.length
                val lengthOk = length in 2..8
                
                if (mode == CrosswordMode.KANJIS) {
                    lengthOk && isPureKanji(entry.text)
                } else {
                    lengthOk
                }
            }
            .shuffled()
            .sortedByDescending { if (mode == CrosswordMode.KANJIS) it.text.length else it.phonetics.length }

        if (candidates.isEmpty()) return Pair(emptyList(), emptyList())

        val firstEntry = candidates[0]
        val firstSolution = if (mode == CrosswordMode.KANJIS) firstEntry.text else firstEntry.phonetics
        placeWord(firstEntry, gridSize / 2, (gridSize - firstSolution.length) / 2, true)

        var candidatesIdx = 1
        var attempts = 0
        while (placedWords.size < targetWordCount && candidatesIdx < candidates.size && attempts < 100) {
            val wordEntry = candidates[candidatesIdx]
            if (tryPlaceWord(wordEntry)) {
                attempts = 0
            } else {
                attempts++
            }
            candidatesIdx++
        }

        return finalizeGrid()
    }

    private fun tryPlaceWord(wordEntry: WordEntry): Boolean {
        val word = if (mode == CrosswordMode.KANJIS) wordEntry.text else wordEntry.phonetics
        val possiblePositions = mutableListOf<Triple<Int, Int, Boolean>>()

        for (placed in placedWords) {
            for (i in placed.word.indices) {
                for (j in word.indices) {
                    if (placed.word[i] == word[j]) {
                        if (placed.isHorizontal) {
                            val startRow = placed.row - j
                            val startCol = placed.col + i
                            if (canPlace(word, startRow, startCol, false)) {
                                possiblePositions.add(Triple(startRow, startCol, false))
                            }
                        } else {
                            val startRow = placed.row + i
                            val startCol = placed.col - j
                            if (canPlace(word, startRow, startCol, true)) {
                                possiblePositions.add(Triple(startRow, startCol, true))
                            }
                        }
                    }
                }
            }
        }

        if (possiblePositions.isEmpty()) return false
        val (r, c, horiz) = possiblePositions.random()
        placeWord(wordEntry, r, c, horiz)
        return true
    }

    private fun canPlace(word: String, row: Int, col: Int, isHorizontal: Boolean): Boolean {
        if (row < 0 || col < 0) return false
        if (isHorizontal && col + word.length > gridSize) return false
        if (!isHorizontal && row + word.length > gridSize) return false

        for (i in word.indices) {
            val r = if (isHorizontal) row else row + i
            val c = if (isHorizontal) col + i else col
            val existing = grid[r][c]
            
            if (existing != "" && existing != word[i].toString()) return false
            
            if (existing == "") {
                if (isHorizontal) {
                    if (isOccupied(r - 1, c) || isOccupied(r + 1, c)) return false
                    if (i == 0 && isOccupied(r, c - 1)) return false
                    if (i == word.length - 1 && isOccupied(r, c + 1)) return false
                } else {
                    if (isOccupied(r, c - 1) || isOccupied(r, c + 1)) return false
                    if (i == 0 && isOccupied(r - 1, c)) return false
                    if (i == word.length - 1 && isOccupied(r + 1, c)) return false
                }
            }
        }
        return true
    }

    private fun isOccupied(r: Int, c: Int): Boolean {
        if (r !in 0 until gridSize || c !in 0 until gridSize) return false
        return grid[r][c] != ""
    }

    private fun placeWord(entry: WordEntry, row: Int, col: Int, isHorizontal: Boolean) {
        val solution = if (mode == CrosswordMode.KANJIS) entry.text else entry.phonetics
        
        placedWords.add(
            CrosswordWord(
                number = placedWords.size + 1,
                word = solution,
                kanji = entry.text,
                meaning = "", // On laisse vide, le ViewModel remplira avec la traduction
                phonetics = entry.phonetics,
                row = row,
                col = col,
                isHorizontal = isHorizontal
            )
        )
        for (i in solution.indices) {
            val r = if (isHorizontal) row else row + i
            val c = if (isHorizontal) col + i else col
            grid[r][c] = solution[i].toString()
        }
    }

    private fun finalizeGrid(): Pair<List<CrosswordCell>, List<CrosswordWord>> {
        val cells = mutableListOf<CrosswordCell>()
        val sortedWords = placedWords.sortedWith(compareBy({ it.row }, { it.col }))
        val finalWords = sortedWords.mapIndexed { index, word -> word.copy(number = index + 1) }
        
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {
                val char = grid[r][c]
                val wordAtStart = finalWords.find { it.row == r && it.col == c }
                cells.add(
                    CrosswordCell(
                        r = r,
                        c = c,
                        solution = char,
                        isBlack = char == "",
                        number = wordAtStart?.number
                    )
                )
            }
        }
        return Pair(cells, finalWords)
    }
}
