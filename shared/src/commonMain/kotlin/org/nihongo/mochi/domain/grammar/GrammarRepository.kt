package org.nihongo.mochi.domain.grammar

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.nihongo.mochi.domain.kana.ResourceLoader

@Serializable
data class GrammarDefinition(
    val version: String,
    val metadata: GrammarMetadata,
    val dependencies_basics: List<GrammarRule>,
    val rules: List<GrammarRule>
)

@Serializable
data class GrammarMetadata(
    val levels: List<String>,
    val categories: List<String>
)

@Serializable
data class GrammarRule(
    val id: String,
    val description: String,
    val level: String,
    val dependencies: List<String>,
    val category: String? = null,
    val tags: List<String> = emptyList()
)

class GrammarRepository(
    private val resourceLoader: ResourceLoader
) {
    private val json = Json { ignoreUnknownKeys = true }
    private var grammarDefinition: GrammarDefinition? = null
    
    // HACK: List of IDs that we know have lessons. 
    // In a real localized setup, we should check if the file exists for the locale or fallback.
    private val availableLessons = setOf(
        "ge", "kke", "mai", "shi", "beki", "garu", "hazu", "hodo", "kiri", "koso", "muke", "muki", "nado", "naru", "noms", "noni", "ppoi", "toka",
        "ichio", "seide", "te_mo", "to_iu", "ue_de", "ue_ha", "ue_ni", "zutsu", "ato_de", "chu_ju", "darake", "hanmen", "hoshii", "ijo_ha", "kagiri",
        "kuseni", "mae_ni", "mai_ka", "nidoto", "rashii", "sae_eb", "sai_ni", "te_aru", "te_iru", "te_oku", "tsutsu", "aru_iru", "base_au", "base_so",
        "dake_ni", "hitsuyo", "igai_no", "ippo_da", "koto_ka", "mo_mada", "mono_ka", "mono_n3", "mono_no", "no_desu", "ta_kiri", "tabi_ni", "te_irai",
        "te_kara", "te_miru", "to_ieba", "toki_ni", "toshite", "uchi_ni", "wari_ni", "ageku_ni", "forme_ta", "forme_te", "ho_ga_ii", "ka_do_ka",
        "karakoso", "ni_ojite", "ni_totte", "ni_tsuki", "okage_de", "shikanai", "sonna_ni", "ta_totan", "to_iu_to", "yo_ni_n3", "amari_nai", "base_dasu",
        "base_gimi", "base_kiru", "base_nuku", "dake_atte", "dokoro_ka", "forme_nai", "forme_tai", "kara_made", "kara_node", "kawari_ni", "koto_kara",
        "koto_naku", "mono_nara", "nagara_mo", "ni_kagiru", "ni_tsuite", "saichu_ni", "ta_bakari", "ta_tokoro", "te_shimau", "to_ittara", "tsuide_ni",
        "wake_desu", "wo_komete", "yara_yara", "base_gachi", "base_gatai", "base_naosu", "base_owaru", "desho_daro", "forme_dict", "forme_masu",
        "ki_ga_suru", "koto_ni_ha", "mo_ii_desu", "mono_da_n2", "ni_kagitte", "ni_kotaete", "ni_kuwaete", "ni_soi_nai", "ni_suginai", "ni_tsurete",
        "noms_lieux", "noms_temps", "nuki_de_no", "superlatif", "te_naranai", "te_yokatta", "to_itte_mo", "to_iu_yori", "to_tomo_ni", "wo_megutte",
        "adjectifs_i", "base_kakeru", "base_kaneru", "base_kkonai", "base_nagara", "base_shidai", "base_sugiru", "eba_ii_noni", "forme_areru", "forme_nasai",
        "ha_ikemasen", "ha_tomokaku", "jo_ha_mo_no", "kagiri_deha", "keigo_bases", "koto_dakara", "koto_ha_nai", "mo_kamawazu", "mono_dakara", "mono_ga_aru",
        "ni_hanshite", "ni_kagirazu", "ni_kurabete", "ni_shite_ha", "ni_taishite", "nomi_narazu", "tatoe_te_mo", "te_hajimete", "to_iu_no_ha", "to_omoimasu",
        "to_shite_ha", "to_shite_mo", "wake_ga_nai", "adjectifs_na", "base_kanenai", "base_verbale", "kara_to_itte", "nakanaka_nai", "ni_kan_shite",
        "ni_sakidatte", "ni_tomonatte", "osore_ga_aru", "particule_de", "particule_ga", "particule_ha", "particule_ka", "particule_mo", "particule_ne",
        "particule_ni", "particule_no", "particule_to", "particule_wo", "particule_ya", "particule_yo", "te_sumimasen", "tsumori_desu", "zaru_wo_enai",
        "base_hajimeru", "base_uru_enai", "dake_quantite", "kara_ni_ha_ha", "ni_chigai_nai", "ni_moto_zuite", "ni_oite_okeru", "ni_shitagatte",
        "no_moto_de_ni", "sae_karashite", "sue_ni_no_sue", "suru_adjectif", "ta_ri_shimasu", "te_bakari_iru", "to_iu_koto_da", "to_iu_mono_da",
        "wake_deha_nai", "wo_chushin_ni", "yori_no_ho_ga", "amari_excessif", "base_tsuzukeru", "base_yo_ga_nai", "ippo_ippo_deha", "kara_ni_kakete",
        "koto_ga_dekiru", "mo_eba_nara_mo", "nai_koto_ni_ha", "ni_kimatte_iru", "particule_yori", "shidai_deha_da", "ta_koto_ga_aru", "tada_no_tan_ni",
        "verb_dict_form", "conditionnel_to", "dake_shika_nomi", "goro_gurai_yaku", "kanoyo_ni_na_da", "ki_ni_suru_naru", "nai_koto_ha_nai",
        "ni_atatte_atari", "ni_hoka_naranai", "ni_kakete_ha_mo", "te_kuremasen_ka", "tokoro_he_ni_wo", "yo_ni_suru_naru", "base_ni_iku_kuru",
        "base_yasui_nikui", "conditionnel_eba", "forme_eru_rareru", "forme_saserareru", "forme_zuni_naide", "kurai_gurai_hodo", "nado_nanka_nante",
        "ni_mo_kakawarazu", "ni_sotte_soi_sou", "ta_ra_do_desu_ka", "ta_ra_ii_desu_ka", "te_morau_itadaku", "wo_moto_ni_shite", "conditionnel_tara",
        "keigo_vocabulaire", "ni_kawatte_kawari", "ni_wataru_watatte", "to_shitara_sureba", "toori_ni_doori_ni", "wake_ni_ha_ikanai", "forme_aseru_saseru",
        "forme_conjecturale", "nakereba_narimasen", "particules_de_base", "te_itadakemasen_ka", "te_kureru_kudasaru", "base_kireru_kirenai",
        "conditionnel_naraba", "forme_conjonctive_i", "forme_naide_kudasai", "to_iu_mono_deha_nai", "wo_towazu_ha_towazu", "wo_tsujite_tooshite",
        "conjecturale_to_omou", "conjecturale_to_suru", "dokoro_deha_nai_naku", "eba_verb_neutre_hodo", "ha_motoyori_mochiron", "ka_nai_ka_no_uchi_ni",
        "kara_iu_to_ieba_itte", "ni_sai_shite_sai_shi", "ni_shiro_shitemo_seyo", "te_tamaranai_shoganai", "ka_to_omou_to_omottara",
        "koto_da_recommandation", "naide_zuni_ha_irarenai", "ni_yotte_yoruto_yoreba", "hoka_nai_shikata_ga_nai", "bakari_ka_bakari_de_naku",
        "conjecturale_deha_nai_ka", "conjugaison_conditionnel", "koto_ni_suru_naru_kimeru", "te_ageru_sashiageru_yaru", "wo_hajime_hajime_to_suru",
        "kara_suru_to_sureba_shite", "ni_tsuke_tsukete_tsuitemo", "te_karade_nai_to_nakereba", "wo_keiki_ni_shite_toshite", "ni_kakawarazu_kakawarinaku",
        "ni_shitara_sureba_shite_mo", "kara_mite_mo_miru_to_mireba", "te_mo_shoganai_shikataganai", "wo_kikkake_ni_shite_toshite",
        "wo_nuki_ni_shite_ha_nuki_ni_shite"
    )

    suspend fun loadGrammarDefinition(): GrammarDefinition {
        if (grammarDefinition != null) return grammarDefinition!!

        val jsonString = resourceLoader.loadJson("grammar/grammar.json")
        grammarDefinition = json.decodeFromString<GrammarDefinition>(jsonString)
        return grammarDefinition!!
    }

    suspend fun getCategories(): List<String> {
        return loadGrammarDefinition().metadata.categories
    }

    suspend fun getRulesUntilLevel(maxLevelId: String): List<GrammarRule> {
        val def = loadGrammarDefinition()
        val allRules = def.dependencies_basics + def.rules
        
        val levelsOrder = def.metadata.levels
        val maxLevelIndex = levelsOrder.indexOf(maxLevelId)
        
        if (maxLevelIndex == -1) return emptyList()

        return allRules.filter { rule ->
            val ruleLevelIndex = levelsOrder.indexOf(rule.level)
            ruleLevelIndex != -1 && ruleLevelIndex <= maxLevelIndex
        }
    }
    
    fun hasLesson(ruleId: String): Boolean {
        return availableLessons.contains(ruleId)
    }
    
    suspend fun loadCss(isDark: Boolean): String {
        val fileName = if (isDark) "styles_dark.css" else "styles_light.css"
        return try {
            resourceLoader.loadJson("grammar/lessons/$fileName")
        } catch (e: Exception) {
             // Fallback CSS in case file load fails
             if (isDark) {
                 "body { font-family: sans-serif; padding: 16px; color: #E0E0E0; background-color: #121212; }"
             } else {
                 "body { font-family: sans-serif; padding: 16px; color: #333; background-color: #FFFFFF; }"
             }
        }
    }
    
    suspend fun loadLessonHtml(ruleId: String, languageCode: String): String {
        // Safe check for language code to avoid bad path construction
        val safeLang = if (languageCode.length >= 2) languageCode.substring(0, 2).lowercase() else "en"
        
        // Priority 1: Specific language path
        val localizedPath = "grammar/lessons/$safeLang/$ruleId.html"
        
        // Priority 2: Default (English) specific path
        val defaultLangPath = "grammar/lessons/en/$ruleId.html"
        
        // Priority 3: Root path (legacy or shared)
        val rootPath = "grammar/lessons/$ruleId.html"
        
        try {
            return resourceLoader.loadJson(localizedPath)
        } catch (e: Exception) {
            // Log here if possible in KMP
        }

        if (safeLang != "en") {
            try {
                return resourceLoader.loadJson(defaultLangPath)
            } catch (e: Exception) {}
        }

        try {
            return resourceLoader.loadJson(rootPath)
        } catch (e: Exception) {
            return """
                <div style="text-align: center; padding: 20px;">
                    <h3>Lesson not found</h3>
                    <p>Could not load lesson content for ID: <b>$ruleId</b></p>
                    <p><small>Language: $safeLang</small></p>
                </div>
            """.trimIndent()
        }
    }
}
