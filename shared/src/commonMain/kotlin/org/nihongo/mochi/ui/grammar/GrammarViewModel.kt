package org.nihongo.mochi.ui.grammar

import org.nihongo.mochi.data.GrammarRuleScore
import org.nihongo.mochi.data.LearningScore
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.grammar.GrammarRepository
import org.nihongo.mochi.domain.grammar.GrammarRule
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GrammarNode(
    val rule: GrammarRule,
    val x: Float, // Relative X position (0.0 - 1.0)
    val y: Float, // Relative Y position (0.0 - 1.0)
    val score: LearningScore,
    val hasLesson: Boolean = false,
    val children: List<GrammarNode> = emptyList()
)

data class GrammarLevelSeparator(
    val levelId: String,
    val y: Float,
    val completionPercentage: Int = 0,
    val ruleIds: List<String> = emptyList() // Added to store rules for exam
)

class GrammarViewModel(
    private val grammarRepository: GrammarRepository,
    private val settingsRepository: SettingsRepository 
) : ViewModel() {

    private val REVISION_LEVEL_ID = "user_custom_list"

    private val _nodes = MutableStateFlow<List<GrammarNode>>(emptyList())
    val nodes: StateFlow<List<GrammarNode>> = _nodes.asStateFlow()

    private val _separators = MutableStateFlow<List<GrammarLevelSeparator>>(emptyList())
    val separators: StateFlow<List<GrammarLevelSeparator>> = _separators.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _availableCategories = MutableStateFlow<List<String>>(emptyList())
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private val _totalLayoutSlots = MutableStateFlow(1f)
    val totalLayoutSlots: StateFlow<Float> = _totalLayoutSlots.asStateFlow()

    private val _currentLevelId = MutableStateFlow("N5")
    val currentLevelId: StateFlow<String> = _currentLevelId.asStateFlow()

    private val _selectedLessonHtml = MutableStateFlow<String?>(null)
    val selectedLessonHtml: StateFlow<String?> = _selectedLessonHtml.asStateFlow()
    
    private val _selectedLessonTitle = MutableStateFlow<String?>(null)
    val selectedLessonTitle: StateFlow<String?> = _selectedLessonTitle.asStateFlow()

    private val _selectedQuizTags = MutableStateFlow<List<String>?>(null)
    val selectedQuizTags: StateFlow<List<String>?> = _selectedQuizTags.asStateFlow()

    fun loadGraph(maxLevelId: String) {
        _currentLevelId.value = maxLevelId
        viewModelScope.launch {
            _isLoading.value = true
            
            if (_availableCategories.value.isEmpty()) {
                val categories = grammarRepository.getCategories()
                _availableCategories.value = categories
            }

            refreshGraph()
            _isLoading.value = false
        }
    }

    fun toggleCategory(category: String) {
        val current = _selectedCategories.value.toMutableSet()
        if (current.contains(category)) {
            current.remove(category)
        } else {
            current.add(category)
        }
        _selectedCategories.value = current
        viewModelScope.launch {
            refreshGraph()
        }
    }
    
    fun setCategories(categories: Set<String>) {
        _selectedCategories.value = categories
        viewModelScope.launch {
            refreshGraph()
        }
    }
    
    fun openLesson(node: GrammarNode) {
         if (node.hasLesson) {
             _selectedLessonTitle.value = node.rule.description 
             viewModelScope.launch {
                 val locale = settingsRepository.getAppLocale()
                 val languageCode = locale.take(2).lowercase() 
                 
                 val theme = settingsRepository.getTheme()
                 val isDark = theme == "dark" 
                 
                 val css = grammarRepository.loadCss(isDark)
                 val rawHtml = grammarRepository.loadLessonHtml(node.rule.id, languageCode)
                 _selectedLessonHtml.value = applyCommonStyle(rawHtml, css)
             }
         }
    }

    fun startQuiz(ruleIds: List<String>) {
        _selectedQuizTags.value = ruleIds
    }

    fun closeQuiz() {
        _selectedQuizTags.value = null
        viewModelScope.launch {
            refreshGraph() // Refresh to update scores on the map
        }
    }
    
    fun closeLesson() {
        _selectedLessonHtml.value = null
        _selectedLessonTitle.value = null
    }

    private fun applyCommonStyle(htmlContent: String, cssContent: String): String {
        val styleTag = "<style>\n$cssContent\n</style>"
        return if (htmlContent.contains("</head>")) {
            htmlContent.replace("</head>", "$styleTag</head>")
        } else if (htmlContent.contains("<body>")) {
            htmlContent.replace("<body>", "<body>$styleTag")
        } else {
            "$styleTag$htmlContent"
        }
    }

    private suspend fun refreshGraph() {
        val currentMaxLevelId = _currentLevelId.value
        val def = grammarRepository.loadGrammarDefinition()
        
        val (rules, levelsToShow) = if (currentMaxLevelId == REVISION_LEVEL_ID) {
            val scores = ScoreManager.getAllScores(ScoreManager.ScoreType.GRAMMAR)
            val revisionRules = scores.keys.mapNotNull { ruleId ->
                grammarRepository.getRuleById(ruleId)
            }
            revisionRules to listOf(REVISION_LEVEL_ID)
        } else {
            val allLevels = def.metadata.levels
            val targetLevelIndex = allLevels.indexOf(currentMaxLevelId).takeIf { it != -1 } ?: allLevels.size - 1
            val levelsToShow = allLevels.take(targetLevelIndex + 1)
            val rules = grammarRepository.getRulesUntilLevel(currentMaxLevelId)
            rules to levelsToShow
        }
        
        var filteredRules = rules
        val selected = _selectedCategories.value
        if (selected.isNotEmpty()) {
            filteredRules = filteredRules.filter { rule ->
                rule.category != null && selected.contains(rule.category)
            }
        }
        
        val (nodes, separators, totalSlots) = buildGraphLayout(filteredRules, levelsToShow)
        
        _nodes.value = nodes
        _separators.value = separators
        _totalLayoutSlots.value = totalSlots
    }

    private fun buildGraphLayout(rules: List<GrammarRule>, levels: List<String>): Triple<List<GrammarNode>, List<GrammarLevelSeparator>, Float> {
        val slotHeightPerNode = 1.0f 
        val paddingSlotsPerLevel = 3.0f

        val rawSeparators = mutableListOf<Triple<String, Float, List<String>>>() 
        val rulesMap = rules.associateBy { it.id }
        val depthCache = mutableMapOf<String, Int>()

        fun getDepth(ruleId: String): Int {
            if (depthCache.containsKey(ruleId)) return depthCache[ruleId] ?: 0
            val rule = rulesMap[ruleId]
            if (rule == null) return 0 
            depthCache[ruleId] = -1 
            var maxDepDepth = -1
            if (rule.dependencies.isNotEmpty()) {
                for (depId in rule.dependencies) {
                    val d = getDepth(depId)
                    if (d > maxDepDepth) maxDepDepth = d
                }
            }
            val depth = maxDepDepth + 1
            depthCache[ruleId] = depth
            return depth
        }
        rules.forEach { getDepth(it.id) }

        // Rules grouped by level, but for revision mode we treat them as one level
        val isRevisionMode = levels.size == 1 && levels[0] == REVISION_LEVEL_ID
        val rulesByLevel = if (isRevisionMode) {
            mapOf(REVISION_LEVEL_ID to rules)
        } else {
            rules.groupBy { it.level }
        }

        var currentSlot = 0f
        
        levels.forEach { levelId ->
            currentSlot += 0.5f 
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            val ruleIds = levelRules.map { it.id }

            if (levelRules.isNotEmpty()) {
                currentSlot += levelRules.size * slotHeightPerNode
            } else {
                currentSlot += slotHeightPerNode
            }
            currentSlot += (paddingSlotsPerLevel / 2f)
            rawSeparators.add(Triple(levelId, currentSlot, ruleIds))
            currentSlot += (paddingSlotsPerLevel / 2f)
        }
        
        val totalSlots = if (currentSlot == 0f) 1f else currentSlot
        val finalNodes = mutableListOf<GrammarNode>()
        
        currentSlot = 0f
        
         levels.forEach { levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            currentSlot += 0.5f
            
            if (levelRules.isNotEmpty()) {
                val assignedSides = mutableMapOf<String, Float>()
                var leftCount = 0
                var rightCount = 0
                
                val sortedRules = levelRules.sortedWith(
                    compareBy<GrammarRule> { depthCache[it.id] ?: 0 }.thenBy { it.id }
                )
                
                fun assignSide(ruleId: String, preferredSide: Float?): Float {
                    val side = if (preferredSide != null) preferredSide else if (leftCount <= rightCount) 0.3f else 0.7f
                    assignedSides[ruleId] = side
                    if (side < 0.5f) leftCount++ else rightCount++
                    return side
                }

                sortedRules.forEach { rule ->
                    val intraLevelParentId = rule.dependencies.firstOrNull { depId ->
                        rulesMap[depId]?.level == levelId && assignedSides.containsKey(depId)
                    }
                    val preferredSide = if (intraLevelParentId != null) assignedSides[intraLevelParentId] else null
                    assignSide(rule.id, preferredSide)
                }
                
                sortedRules.forEach { rule ->
                    val x = assignedSides[rule.id] ?: 0.5f
                    val rawScore = ScoreManager.getScore(rule.id, ScoreManager.ScoreType.GRAMMAR)
                    val score = GrammarRuleScore(rawScore.successes, rawScore.failures, rawScore.lastReviewDate)
                    val hasLesson = grammarRepository.hasLesson(rule.id)
                    finalNodes.add(GrammarNode(rule, x, currentSlot, score, hasLesson)) 
                    currentSlot += slotHeightPerNode
                }
            } else {
                currentSlot += slotHeightPerNode
            }
            
            currentSlot += paddingSlotsPerLevel
        }
        
        val normalizedNodes = finalNodes.map { it.copy(y = it.y / totalSlots) }
        val normalizedSeparators = rawSeparators.map { triple ->
            val levelId = triple.first
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            val completion = if (levelRules.isNotEmpty()) {
                val successfulNodes = levelRules.count { rule ->
                    val score = ScoreManager.getScore(rule.id, ScoreManager.ScoreType.GRAMMAR)
                    (score.successes - score.failures) >= 1
                }
                (successfulNodes * 100) / levelRules.size
            } else 0
            
            GrammarLevelSeparator(levelId, triple.second / totalSlots, completion, triple.third) 
        }
        
        return Triple(normalizedNodes, normalizedSeparators, totalSlots)
    }
}
