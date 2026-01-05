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
        // Grammar descriptions special mapping
        if (key.endsWith("_desc")) {
            return when(key) {
                // Grammar Rules Descriptions
                "verb_dict_form_desc" -> Res.string.verb_dict_form_desc
                "base_verbale_desc" -> Res.string.base_verbale_desc
                "forme_nai_desc" -> Res.string.forme_nai_desc
                "forme_ta_desc" -> Res.string.forme_ta_desc
                "forme_te_desc" -> Res.string.forme_te_desc
                "conjugaison_conditionnel_desc" -> Res.string.conjugaison_conditionnel_desc
                "forme_conjecturale_desc" -> Res.string.forme_conjecturale_desc
                "adjectifs_i_desc" -> Res.string.adjectifs_i_desc
                "adjectifs_na_desc" -> Res.string.adjectifs_na_desc
                "noms_desc" -> Res.string.noms_desc
                "noms_temps_desc" -> Res.string.noms_temps_desc
                "noms_lieux_desc" -> Res.string.noms_lieux_desc
                "particules_de_base_desc" -> Res.string.particules_de_base_desc
                "aru_iru_desc" -> Res.string.aru_iru_desc
                "ato_de_desc" -> Res.string.ato_de_desc
                "base_dasu_desc" -> Res.string.base_dasu_desc
                "base_hajimeru_desc" -> Res.string.base_hajimeru_desc
                "base_ni_iku_kuru_desc" -> Res.string.base_ni_iku_kuru_desc
                "base_sugiru_desc" -> Res.string.base_sugiru_desc
                "chu_ju_desc" -> Res.string.chu_ju_desc
                "particule_de_desc" -> Res.string.particule_de_desc
                "desho_daro_desc" -> Res.string.desho_daro_desc
                "forme_conjonctive_i_desc" -> Res.string.forme_conjonctive_i_desc
                "forme_masu_desc" -> Res.string.forme_masu_desc
                "forme_naide_kudasai_desc" -> Res.string.forme_naide_kudasai_desc
                "forme_dict_desc" -> Res.string.forme_dict_desc
                "forme_tai_desc" -> Res.string.forme_tai_desc
                "particule_ga_desc" -> Res.string.particule_ga_desc
                "particule_ha_desc" -> Res.string.particule_ha_desc
                "ha_ikemasen_desc" -> Res.string.ha_ikemasen_desc
                "ho_ga_ii_desc" -> Res.string.ho_ga_ii_desc
                "particule_ka_desc" -> Res.string.particule_ka_desc
                "kara_made_desc" -> Res.string.kara_made_desc
                "kara_node_desc" -> Res.string.kara_node_desc
                "koto_ga_dekiru_desc" -> Res.string.koto_ga_dekiru_desc
                "mae_ni_desc" -> Res.string.mae_ni_desc
                "particule_mo_desc" -> Res.string.particule_mo_desc
                "mo_ii_desu_desc" -> Res.string.mo_ii_desu_desc
                "superlatif_desc" -> Res.string.superlatif_desc
                "mo_mada_desc" -> Res.string.mo_mada_desc
                "nado_desc" -> Res.string.nado_desc
                "nakereba_narimasen_desc" -> Res.string.nakereba_narimasen_desc
                "naru_desc" -> Res.string.naru_desc
                "particule_ne_desc" -> Res.string.particule_ne_desc
                "particule_ni_desc" -> Res.string.particule_ni_desc
                "particule_no_desc" -> Res.string.particule_no_desc
                "no_desu_desc" -> Res.string.no_desu_desc
                "particule_to_desc" -> Res.string.particule_to_desc
                "to_iu_desc" -> Res.string.to_iu_desc
                "to_omoimasu_desc" -> Res.string.to_omoimasu_desc
                "tsumori_desu_desc" -> Res.string.tsumori_desu_desc
                "ta_koto_ga_aru_desc" -> Res.string.ta_koto_ga_aru_desc
                "ta_ri_shimasu_desc" -> Res.string.ta_ri_shimasu_desc
                "te_iru_desc" -> Res.string.te_iru_desc
                "te_kara_desc" -> Res.string.te_kara_desc
                "particule_wo_desc" -> Res.string.particule_wo_desc
                "particule_ya_desc" -> Res.string.particule_ya_desc
                "particule_yo_desc" -> Res.string.particule_yo_desc
                "particule_yori_desc" -> Res.string.particule_yori_desc
                "yori_no_ho_ga_desc" -> Res.string.yori_no_ho_ga_desc
                "amari_nai_desc" -> Res.string.amari_nai_desc
                "base_au_desc" -> Res.string.base_au_desc
                "base_nagara_desc" -> Res.string.base_nagara_desc
                "base_so_desc" -> Res.string.base_so_desc
                "base_tsuzukeru_desc" -> Res.string.base_tsuzukeru_desc
                "base_yasui_nikui_desc" -> Res.string.base_yasui_nikui_desc
                "beki_desc" -> Res.string.beki_desc
                "conditionnel_naraba_desc" -> Res.string.conditionnel_naraba_desc
                "conditionnel_to_desc" -> Res.string.conditionnel_to_desc
                "conditionnel_eba_desc" -> Res.string.conditionnel_eba_desc
                "conditionnel_tara_desc" -> Res.string.conditionnel_tara_desc
                "dake_shika_nomi_desc" -> Res.string.dake_shika_nomi_desc
                "conjecturale_to_suru_desc" -> Res.string.conjecturale_to_suru_desc
                "forme_areru_desc" -> Res.string.forme_areru_desc
                "forme_aseru_saseru_desc" -> Res.string.forme_aseru_saseru_desc
                "forme_eru_rareru_desc" -> Res.string.forme_eru_rareru_desc
                "forme_nasai_desc" -> Res.string.forme_nasai_desc
                "forme_saserareru_desc" -> Res.string.forme_saserareru_desc
                "forme_zuni_naide_desc" -> Res.string.forme_zuni_naide_desc
                "garu_desc" -> Res.string.garu_desc
                "hazu_desc" -> Res.string.hazu_desc
                "hoshii_desc" -> Res.string.hoshii_desc
                "ka_do_ka_desc" -> Res.string.ka_do_ka_desc
                "keigo_bases_desc" -> Res.string.keigo_bases_desc
                "ki_ni_suru_naru_desc" -> Res.string.ki_ni_suru_naru_desc
                "koto_ni_suru_naru_kimeru_desc" -> Res.string.koto_ni_suru_naru_kimeru_desc
                "ni_yotte_yoruto_yoreba_desc" -> Res.string.ni_yotte_yoruto_yoreba_desc
                "noni_desc" -> Res.string.noni_desc
                "rashii_desc" -> Res.string.rashii_desc
                "shi_desc" -> Res.string.shi_desc
                "suru_adjectif_desc" -> Res.string.suru_adjectif_desc
                "toki_ni_desc" -> Res.string.toki_ni_desc
                "ta_bakari_desc" -> Res.string.ta_bakari_desc
                "ta_ra_do_desu_ka_desc" -> Res.string.ta_ra_do_desu_ka_desc
                "te_ageru_sashiageru_yaru_desc" -> Res.string.te_ageru_sashiageru_yaru_desc
                "te_aru_desc" -> Res.string.te_aru_desc
                "te_bakari_iru_desc" -> Res.string.te_bakari_iru_desc
                "te_itadakemasen_ka_desc" -> Res.string.te_itadakemasen_ka_desc
                "te_kuremasen_ka_desc" -> Res.string.te_kuremasen_ka_desc
                "te_kureru_kudasaru_desc" -> Res.string.te_kureru_kudasaru_desc
                "te_miru_desc" -> Res.string.te_miru_desc
                "te_mo_desc" -> Res.string.te_mo_desc
                "te_morau_itadaku_desc" -> Res.string.te_morau_itadaku_desc
                "te_oku_desc" -> Res.string.te_oku_desc
                "te_shimau_desc" -> Res.string.te_shimau_desc
                "te_sumimasen_desc" -> Res.string.te_sumimasen_desc
                "te_yokatta_desc" -> Res.string.te_yokatta_desc
                "yo_ni_suru_naru_desc" -> Res.string.yo_ni_suru_naru_desc
                "base_gachi_desc" -> Res.string.base_gachi_desc
                "base_kireru_kirenai_desc" -> Res.string.base_kireru_kirenai_desc
                "base_kkonai_desc" -> Res.string.base_kkonai_desc
                "base_shidai_desc" -> Res.string.base_shidai_desc
                "base_yo_ga_nai_desc" -> Res.string.base_yo_ga_nai_desc
                "darake_desc" -> Res.string.darake_desc
                "dokoro_ka_desc" -> Res.string.dokoro_ka_desc
                "ha_motoyori_mochiron_desc" -> Res.string.ha_motoyori_mochiron_desc
                "ippo_da_desc" -> Res.string.ippo_da_desc
                "kara_ni_kakete_desc" -> Res.string.kara_ni_kakete_desc
                "kawari_ni_desc" -> Res.string.kawari_ni_desc
                "koso_desc" -> Res.string.koso_desc
                "kuseni_desc" -> Res.string.kuseni_desc
                "mono_n3_desc" -> Res.string.mono_n3_desc
                "nagara_mo_desc" -> Res.string.nagara_mo_desc
                "ni_kagitte_desc" -> Res.string.ni_kagitte_desc
                "ni_kan_shite_desc" -> Res.string.ni_kan_shite_desc
                "ni_kawatte_kawari_desc" -> Res.string.ni_kawatte_kawari_desc
                "ni_kimatte_iru_desc" -> Res.string.ni_kimatte_iru_desc
                "ni_kurabete_desc" -> Res.string.ni_kurabete_desc
                "ni_kuwaete_desc" -> Res.string.ni_kuwaete_desc
                "ni_taishite_desc" -> Res.string.ni_taishite_desc
                "ni_totte_desc" -> Res.string.ni_totte_desc
                "ni_tsuke_tsukete_tsuitemo_desc" -> Res.string.ni_tsuke_tsukete_tsuitemo_desc
                "okage_de_desc" -> Res.string.okage_de_desc
                "sae_eb_desc" -> Res.string.sae_eb_desc
                "seide_desc" -> Res.string.seide_desc
                "tabi_ni_desc" -> Res.string.tabi_ni_desc
                "tatoe_te_mo_desc" -> Res.string.tatoe_te_mo_desc
                "to_ieba_desc" -> Res.string.to_ieba_desc
                "to_ittara_desc" -> Res.string.to_ittara_desc
                "to_iu_to_desc" -> Res.string.to_iu_to_desc
                "tokoro_he_ni_wo_desc" -> Res.string.tokoro_he_ni_wo_desc
                "toori_ni_doori_ni_desc" -> Res.string.toori_ni_doori_ni_desc
                "toshite_desc" -> Res.string.toshite_desc
                "tsuide_ni_desc" -> Res.string.tsuide_ni_desc
                "uchi_ni_desc" -> Res.string.uchi_ni_desc
                "ta_tokoro_desc" -> Res.string.ta_tokoro_desc
                "ta_totan_desc" -> Res.string.ta_totan_desc
                "te_irai_desc" -> Res.string.te_irai_desc
                "wake_deha_nai_desc" -> Res.string.wake_deha_nai_desc
                "wo_chushin_ni_desc" -> Res.string.wo_chushin_ni_desc
                "wo_hajime_hajime_to_suru_desc" -> Res.string.wo_hajime_hajime_to_suru_desc
                "wo_nuki_ni_shite_ha_nuki_ni_shite_desc" -> Res.string.wo_nuki_ni_shite_ha_nuki_ni_shite_desc
                "yo_ni_n3_desc" -> Res.string.yo_ni_n3_desc
                "ageku_ni_desc" -> Res.string.ageku_ni_desc
                "amari_excessif_desc" -> Res.string.amari_excessif_desc
                "bakari_ka_bakari_de_naku_desc" -> Res.string.bakari_ka_bakari_de_naku_desc
                "base_gatai_desc" -> Res.string.base_gatai_desc
                "base_gimi_desc" -> Res.string.base_gimi_desc
                "base_kakeru_desc" -> Res.string.base_kakeru_desc
                "base_kanenai_desc" -> Res.string.base_kanenai_desc
                "base_kaneru_desc" -> Res.string.base_kaneru_desc
                "base_kiru_desc" -> Res.string.base_kiru_desc
                "base_nuku_desc" -> Res.string.base_nuku_desc
                "base_uru_enai_desc" -> Res.string.base_uru_enai_desc
                "dake_quantite_desc" -> Res.string.dake_quantite_desc
                "dake_atte_desc" -> Res.string.dake_atte_desc
                "dake_ni_desc" -> Res.string.dake_ni_desc
                "dokoro_deha_nai_naku_desc" -> Res.string.dokoro_deha_nai_naku_desc
                "conjecturale_deha_nai_ka_desc" -> Res.string.conjecturale_deha_nai_ka_desc
                "ge_desc" -> Res.string.ge_desc
                "ha_tomokaku_desc" -> Res.string.ha_tomokaku_desc
                "hanmen_desc" -> Res.string.hanmen_desc
                "hodo_desc" -> Res.string.hodo_desc
                "hoka_nai_shikata_ga_nai_desc" -> Res.string.hoka_nai_shikata_ga_nai_desc
                "igai_no_desc" -> Res.string.igai_no_desc
                "ijo_ha_desc" -> Res.string.ijo_ha_desc
                "ippo_ippo_deha_desc" -> Res.string.ippo_ippo_deha_desc
                "jo_ha_mo_no_desc" -> Res.string.jo_ha_mo_no_desc
                "ka_to_omou_to_omottara_desc" -> Res.string.ka_to_omou_to_omottara_desc
                "kagiri_desc" -> Res.string.kagiri_desc
                "kagiri_deha_desc" -> Res.string.kagiri_deha_desc
                "kanoyo_ni_na_da_desc" -> Res.string.kanoyo_ni_na_da_desc
                "kara_iu_to_ieba_itte_desc" -> Res.string.kara_iu_to_ieba_itte_desc
                "kara_mite_mo_miru_to_mireba_desc" -> Res.string.kara_mite_mo_miru_to_mireba_desc
                "kara_ni_ha_ha_desc" -> Res.string.kara_ni_ha_ha_desc
                "kara_suru_to_sureba_shite_desc" -> Res.string.kara_suru_to_sureba_shite_desc
                "kara_to_itte_desc" -> Res.string.kara_to_itte_desc
                "karakoso_desc" -> Res.string.karakoso_desc
                "ki_ga_suru_desc" -> Res.string.ki_ga_suru_desc
                "kiri_desc" -> Res.string.kiri_desc
                "kke_desc" -> Res.string.kke_desc
                "koto_da_recommandation_desc" -> Res.string.koto_da_recommandation_desc
                "koto_dakara_desc" -> Res.string.koto_dakara_desc
                "koto_ha_nai_desc" -> Res.string.koto_ha_nai_desc
                "koto_ka_desc" -> Res.string.koto_ka_desc
                "koto_kara_desc" -> Res.string.koto_kara_desc
                "koto_naku_desc" -> Res.string.koto_naku_desc
                "koto_ni_ha_desc" -> Res.string.koto_ni_ha_desc
                "kurai_gurai_hodo_desc" -> Res.string.kurai_gurai_hodo_desc
                "mai_desc" -> Res.string.mai_desc
                "mai_ka_desc" -> Res.string.mai_ka_desc
                "mo_kamawazu_desc" -> Res.string.mo_kamawazu_desc
                "mono_da_n2_desc" -> Res.string.mono_da_n2_desc
                "mono_dakara_desc" -> Res.string.mono_dakara_desc
                "mono_ga_aru_desc" -> Res.string.mono_ga_aru_desc
                "mono_ka_desc" -> Res.string.mono_ka_desc
                "mono_nara_desc" -> Res.string.mono_nara_desc
                "mono_no_desc" -> Res.string.mono_no_desc
                "muke_desc" -> Res.string.muke_desc
                "muki_desc" -> Res.string.muki_desc
                "nado_nanka_nante_desc" -> Res.string.nado_nanka_nante_desc
                "nai_koto_ha_nai_desc" -> Res.string.nai_koto_ha_nai_desc
                "nai_koto_ni_ha_desc" -> Res.string.nai_koto_ni_ha_desc
                "nakanaka_nai_desc" -> Res.string.nakanaka_nai_desc
                "ni_atatte_atari_desc" -> Res.string.ni_atatte_atari_desc
                "ni_chigai_nai_desc" -> Res.string.ni_chigai_nai_desc
                "ni_hanshite_desc" -> Res.string.ni_hanshite_desc
                "ni_hoka_naranai_desc" -> Res.string.ni_hoka_naranai_desc
                "ni_kagirazu_desc" -> Res.string.ni_kagirazu_desc
                "ni_kagiru_desc" -> Res.string.ni_kagiru_desc
                "ni_kakawarazu_kakawarinaku_desc" -> Res.string.ni_kakawarazu_kakawarinaku_desc
                "ni_kakete_ha_mo_desc" -> Res.string.ni_kakete_ha_mo_desc
                "ni_kotaete_desc" -> Res.string.ni_kotaete_desc
                "ni_mo_kakawarazu_desc" -> Res.string.ni_mo_kakawarazu_desc
                "ni_moto_zuite_desc" -> Res.string.ni_moto_zuite_desc
                "ni_oite_okeru_desc" -> Res.string.ni_oite_okeru_desc
                "ni_sai_shite_sai_shi_desc" -> Res.string.ni_sai_shite_sai_shi_desc
                "ni_sakidatte_desc" -> Res.string.ni_sakidatte_desc
                "ni_shiro_shitemo_seyo_desc" -> Res.string.ni_shiro_shitemo_seyo_desc
                "ni_shitagatte_desc" -> Res.string.ni_shitagatte_desc
                "ni_shitara_sureba_shite_mo_desc" -> Res.string.ni_shitara_sureba_shite_mo_desc
                "ni_shite_ha_desc" -> Res.string.ni_shite_ha_desc
                "ni_sotte_soi_sou_desc" -> Res.string.ni_sotte_soi_sou_desc
                "ni_suginai_desc" -> Res.string.ni_suginai_desc
                "ni_soi_nai_desc" -> Res.string.ni_soi_nai_desc
                "ni_tomonatte_desc" -> Res.string.ni_tomonatte_desc
                "ni_tsuite_desc" -> Res.string.ni_tsuite_desc
                "ni_tsuki_desc" -> Res.string.ni_tsuki_desc
                "ni_tsurete_desc" -> Res.string.ni_tsurete_desc
                "ni_wataru_watatte_desc" -> Res.string.ni_wataru_watatte_desc
                "ni_ojite_desc" -> Res.string.ni_ojite_desc
                "no_moto_de_ni_desc" -> Res.string.no_moto_de_ni_desc
                "nomi_narazu_desc" -> Res.string.nomi_narazu_desc
                "nuki_de_no_desc" -> Res.string.nuki_de_no_desc
                "osore_ga_aru_desc" -> Res.string.osore_ga_aru_desc
                "ppoi_desc" -> Res.string.ppoi_desc
                "sae_karashite_desc" -> Res.string.sae_karashite_desc
                "sai_ni_desc" -> Res.string.sai_ni_desc
                "saichu_ni_desc" -> Res.string.saichu_ni_desc
                "shidai_deha_da_desc" -> Res.string.shidai_deha_da_desc
                "shikanai_desc" -> Res.string.shikanai_desc
                "sue_ni_no_sue_desc" -> Res.string.sue_ni_no_sue_desc
                "to_itte_mo_desc" -> Res.string.to_itte_mo_desc
                "to_iu_koto_da_desc" -> Res.string.to_iu_koto_da_desc
                "to_iu_mono_da_desc" -> Res.string.to_iu_mono_da_desc
                "to_iu_mono_deha_nai_desc" -> Res.string.to_iu_mono_deha_nai_desc
                "to_iu_yori_desc" -> Res.string.to_iu_yori_desc
                "to_shitara_sureba_desc" -> Res.string.to_shitara_sureba_desc
                "to_shite_ha_desc" -> Res.string.to_shite_ha_desc
                "to_shite_mo_desc" -> Res.string.to_shite_mo_desc
                "to_tomo_ni_desc" -> Res.string.to_tomo_ni_desc
                "toka_desc" -> Res.string.toka_desc
                "tsutsu_desc" -> Res.string.tsutsu_desc
                "ue_de_desc" -> Res.string.ue_de_desc
                "ue_ha_desc" -> Res.string.ue_ha_desc
                "ue_ni_desc" -> Res.string.ue_ni_desc
                "eba_verb_neutre_hodo_desc" -> Res.string.eba_verb_neutre_hodo_desc
                "naide_zuni_ha_irarenai_desc" -> Res.string.naide_zuni_ha_irarenai_desc
                "ta_kiri_desc" -> Res.string.ta_kiri_desc
                "te_hajimete_desc" -> Res.string.te_hajimete_desc
                "te_karade_nai_to_nakereba_desc" -> Res.string.te_karade_nai_to_nakereba_desc
                "te_naranai_desc" -> Res.string.te_naranai_desc
                "te_tamaranai_shoganai_desc" -> Res.string.te_tamaranai_shoganai_desc
                "keigo_vocabulaire_desc" -> Res.string.keigo_vocabulaire_desc
                "wake_desu_desc" -> Res.string.wake_desu_desc
                "wake_ga_nai_desc" -> Res.string.wake_ga_nai_desc
                "wake_ni_ha_ikanai_desc" -> Res.string.wake_ni_ha_ikanai_desc
                "wari_ni_desc" -> Res.string.wari_ni_desc
                "wo_keiki_ni_shite_toshite_desc" -> Res.string.wo_keiki_ni_shite_toshite_desc
                "wo_kikkake_ni_shite_toshite_desc" -> Res.string.wo_kikkake_ni_shite_toshite_desc
                "wo_komete_desc" -> Res.string.wo_komete_desc
                "wo_megutte_desc" -> Res.string.wo_megutte_desc
                "wo_moto_ni_shite_desc" -> Res.string.wo_moto_ni_shite_desc
                "wo_towazu_ha_towazu_desc" -> Res.string.wo_towazu_ha_towazu_desc
                "wo_tsujite_tooshite_desc" -> Res.string.wo_tsujite_tooshite_desc
                "zaru_wo_enai_desc" -> Res.string.zaru_wo_enai_desc
                "ka_nai_ka_no_uchi_ni_desc" -> Res.string.ka_nai_ka_no_uchi_ni_desc
                "yara_yara_desc" -> Res.string.yara_yara_desc
                "mo_eba_nara_mo_desc" -> Res.string.mo_eba_nara_mo_desc
                "base_naosu_desc" -> Res.string.base_naosu_desc
                "base_owaru_desc" -> Res.string.base_owaru_desc
                "conjecturale_to_omou_desc" -> Res.string.conjecturale_to_omou_desc
                "goro_gurai_yaku_desc" -> Res.string.goro_gurai_yaku_desc
                "hitsuyo_desc" -> Res.string.hitsuyo_desc
                "ichio_desc" -> Res.string.ichio_desc
                "nidoto_desc" -> Res.string.nidoto_desc
                "sonna_ni_desc" -> Res.string.sonna_ni_desc
                "tada_no_tan_ni_desc" -> Res.string.tada_no_tan_ni_desc
                "to_iu_no_ha_desc" -> Res.string.to_iu_no_ha_desc
                "eba_ii_noni_desc" -> Res.string.eba_ii_noni_desc
                "ta_ra_ii_desu_ka_desc" -> Res.string.ta_ra_ii_desu_ka_desc
                "te_mo_shoganai_shikataganai_desc" -> Res.string.te_mo_shoganai_shikataganai_desc
                "zutsu_desc" -> Res.string.zutsu_desc
                else -> null
            }
        }

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

            // Legacy level descriptions mappings (might be removed if not needed)
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
