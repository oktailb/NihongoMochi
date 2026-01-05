package org.nihongo.mochi.ui.grammar

import org.nihongo.mochi.domain.grammar.GrammarRepository
import org.nihongo.mochi.domain.grammar.GrammarRule
import org.nihongo.mochi.presentation.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GrammarNode(
    val rule: GrammarRule,
    val x: Float, // Relative X position (0.0 - 1.0)
    val y: Float, // Relative Y position (0.0 - 1.0)
    val children: List<GrammarNode> = emptyList()
)

data class GrammarLevelSeparator(
    val levelId: String,
    val y: Float
)

class GrammarViewModel(
    private val grammarRepository: GrammarRepository
) : ViewModel() {

    private val _nodes = MutableStateFlow<List<GrammarNode>>(emptyList())
    val nodes: StateFlow<List<GrammarNode>> = _nodes.asStateFlow()

    private val _separators = MutableStateFlow<List<GrammarLevelSeparator>>(emptyList())
    val separators: StateFlow<List<GrammarLevelSeparator>> = _separators.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadGraph(maxLevelId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val def = grammarRepository.loadGrammarDefinition()
            
            // Determine level order
            val allLevels = def.metadata.levels
            val targetLevelIndex = allLevels.indexOf(maxLevelId).takeIf { it != -1 } ?: allLevels.size - 1
            val levelsToShow = allLevels.take(targetLevelIndex + 1)
            
            val rules = grammarRepository.getRulesUntilLevel(maxLevelId)
            
            // Build the graph layout
            val (nodes, separators) = buildGraphLayout(rules, levelsToShow)
            
            _nodes.value = nodes
            _separators.value = separators
            _isLoading.value = false
        }
    }

    private fun buildGraphLayout(rules: List<GrammarRule>, levels: List<String>): Pair<List<GrammarNode>, List<GrammarLevelSeparator>> {
        val nodes = mutableListOf<GrammarNode>()
        val separators = mutableListOf<GrammarLevelSeparator>()
        
        // Map rules for easy lookup
        val rulesMap = rules.associateBy { it.id }
        
        // Cache for depth calculation
        val depthCache = mutableMapOf<String, Int>()

        // Recursive function to calculate depth
        fun getDepth(ruleId: String): Int {
            if (depthCache.containsKey(ruleId)) {
                val cached = depthCache[ruleId]!!
                if (cached == -1) return 0 // Cycle detected, break it
                return cached
            }
            
            val rule = rulesMap[ruleId] ?: return 0 // Dependency not found in current set, assume base
            
            depthCache[ruleId] = -1 // Mark as visiting
            
            var maxDepDepth = -1 // Start at -1 so base rules (0 deps) get depth 0 (max(-1) + 1 = 0)
            
            if (rule.dependencies.isNotEmpty()) {
                for (depId in rule.dependencies) {
                    val d = getDepth(depId)
                    if (d > maxDepDepth) maxDepDepth = d
                }
            } else {
                 // Base rule within this set
                 maxDepDepth = -1
            }
            
            val depth = maxDepDepth + 1
            depthCache[ruleId] = depth
            return depth
        }

        // Pre-calculate depths to ensure correct ordering (parents before children)
        rules.forEach { getDepth(it.id) }

        // --- ZIGZAG VERTICAL LAYOUT STRATEGY ---
        
        // 1. Determine total slots needed.
        // Each rule takes 1 slot.
        // Each level adds some padding/separator space (e.g. equivalent to 2 slots).
        
        val rulesByLevel = rules.groupBy { it.level }
        val paddingSlotsPerLevel = 2
        var totalSlots = 0
        
        levels.forEach { levelId ->
            val count = rulesByLevel[levelId]?.size ?: 0
            // Ensure at least some space for empty levels
            val effectiveCount = if (count == 0) 1 else count 
            totalSlots += effectiveCount + paddingSlotsPerLevel
        }
        
        if (totalSlots == 0) totalSlots = 1

        var currentSlotIndex = 0
        
        levels.forEachIndexed { levelIndex, levelId ->
            val levelRules = rulesByLevel[levelId] ?: emptyList()
            
            // Add top padding for the level (1 slot)
            currentSlotIndex++
            
            if (levelRules.isNotEmpty()) {
                // Sort rules:
                // Primary: Depth (so dependencies appear above)
                // Secondary: Alphabetical (for consistency)
                val sortedRules = levelRules.sortedWith(
                    compareBy<GrammarRule> { depthCache[it.id] ?: 0 }
                        .thenBy { it.id }
                )
                
                sortedRules.forEachIndexed { ruleIndex, rule ->
                    val y = currentSlotIndex.toFloat() / totalSlots
                    
                    // X Alternation: 0.25 (Left) vs 0.75 (Right)
                    // We reset the pattern per level or keep it continuous? 
                    // Keeping it continuous within the level looks nice.
                    val x = if (ruleIndex % 2 == 0) 0.25f else 0.75f
                    
                    nodes.add(GrammarNode(rule, x, y))
                    
                    // Move to next slot
                    currentSlotIndex++
                }
            } else {
                // Empty level placeholder space
                currentSlotIndex++
            }
            
            // Separator takes a slot (or is placed at the current gap)
            val separatorY = (currentSlotIndex.toFloat() + 0.5f) / totalSlots
            
            // Add separator (except maybe the very last one if strictly at bottom, 
            // but for "levels passed" visualization, having one after each completed level is good)
            if (levelIndex < levels.size - 1) {
                separators.add(GrammarLevelSeparator(levelId, separatorY))
            }
            
            // Add bottom padding for the level (consume the slot we used for calculation)
            currentSlotIndex++
        }
        
        return Pair(nodes, separators)
    }
}
