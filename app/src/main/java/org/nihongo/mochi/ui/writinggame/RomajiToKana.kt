package org.nihongo.mochi.ui.writinggame

object RomajiToKana {
    private val m = HashMap<String, String>()

    init {
        // Vowels
        m["a"] = "あ"; m["i"] = "い"; m["u"] = "う"; m["e"] = "え"; m["o"] = "お"
        m["yi"] = "い"; m["ye"] = "いe"; // approximations
        
        // K
        m["ka"] = "か"; m["ki"] = "き"; m["ku"] = "く"; m["ke"] = "け"; m["ko"] = "こ"
        // S
        m["sa"] = "さ"; m["shi"] = "し"; m["su"] = "す"; m["se"] = "せ"; m["so"] = "そ"
        m["si"] = "し"
        // T
        m["ta"] = "た"; m["chi"] = "ち"; m["tsu"] = "つ"; m["te"] = "て"; m["to"] = "と"
        m["ti"] = "ち"; m["tu"] = "つ"
        // N
        m["na"] = "な"; m["ni"] = "に"; m["nu"] = "ぬ"; m["ne"] = "ね"; m["no"] = "の"
        // H
        m["ha"] = "は"; m["hi"] = "ひ"; m["fu"] = "ふ"; m["he"] = "へ"; m["ho"] = "ほ"
        m["hu"] = "ふ"
        // M
        m["ma"] = "ま"; m["mi"] = "み"; m["mu"] = "む"; m["me"] = "め"; m["mo"] = "も"
        // Y
        m["ya"] = "や"; m["yu"] = "ゆ"; m["yo"] = "よ"
        // R
        m["ra"] = "ら"; m["ri"] = "り"; m["ru"] = "る"; m["re"] = "れ"; m["ro"] = "ろ"
        // W
        m["wa"] = "わ"; m["wo"] = "を"
        // N special
        m["nn"] = "ん"

        // Dakuten
        m["ga"] = "が"; m["gi"] = "ぎ"; m["gu"] = "ぐ"; m["ge"] = "げ"; m["go"] = "ご"
        m["za"] = "ざ"; m["ji"] = "じ"; m["zu"] = "ず"; m["ze"] = "ぜ"; m["zo"] = "ぞ"
        m["zi"] = "じ"
        m["da"] = "だ"; m["ji"] = "ぢ"; m["zu"] = "づ"; m["de"] = "で"; m["do"] = "ど"
        m["di"] = "ぢ"; m["du"] = "づ"
        m["ba"] = "ば"; m["bi"] = "び"; m["bu"] = "ぶ"; m["be"] = "べ"; m["bo"] = "ぼ"
        
        // Handakuten
        m["pa"] = "ぱ"; m["pi"] = "ぴ"; m["pu"] = "ぷ"; m["pe"] = "ぺ"; m["po"] = "ぽ"
        
        // Contracted (Yoon)
        m["kya"] = "きゃ"; m["kyu"] = "きゅ"; m["kyo"] = "きょ"
        m["sha"] = "しゃ"; m["shu"] = "しゅ"; m["sho"] = "しょ"
        m["cha"] = "ちゃ"; m["chu"] = "ちゅ"; m["cho"] = "ちょ"
        m["nya"] = "にゃ"; m["nyu"] = "にゅ"; m["nyo"] = "にょ"
        m["hya"] = "ひゃ"; m["hyu"] = "ひゅ"; m["hyo"] = "ひょ"
        m["mya"] = "みゃ"; m["myu"] = "みゅ"; m["myo"] = "みょ"
        m["rya"] = "りゃ"; m["ryu"] = "りゅ"; m["ryo"] = "りょ"
        m["gya"] = "ぎゃ"; m["gyu"] = "ぎゅ"; m["gyo"] = "ぎょ"
        m["ja"] = "じゃ"; m["ju"] = "じゅ"; m["jo"] = "じょ"
        m["jya"] = "じゃ"; m["jyu"] = "じゅ"; m["jyo"] = "じょ"
        m["bya"] = "びゃ"; m["byu"] = "びゅ"; m["byo"] = "びょ"
        m["pya"] = "ぴゃ"; m["pyu"] = "ぴゅ"; m["pyo"] = "ぴょ"
        
        // Symbols
        m["-"] = "ー"
    }

    // Returns a Pair<Int, String> where Int is the number of characters at the end of text to replace,
    // and String is the replacement. Returns null if no replacement found.
    fun checkReplacement(text: String): Pair<Int, String>? {
        val len = text.length
        if (len == 0) return null
        
        // Priority 1: 3 chars (e.g. kya)
        if (len >= 3) {
            val suffix = text.substring(len - 3)
            if (m.containsKey(suffix)) return Pair(3, m[suffix]!!)
        }
        
        // Priority 2: 2 chars (e.g. ka, nn, or special n+consonant, or double consonant)
        if (len >= 2) {
            val suffix = text.substring(len - 2)
            if (m.containsKey(suffix)) return Pair(2, m[suffix]!!)
            
            val c1 = suffix[0]
            val c2 = suffix[1]
            
            // n + consonant (except y, which waits for nya)
            // vowels and n are not consonants in this context
            if (c1 == 'n' && c2 !in "aiueony") {
                return Pair(2, "ん$c2")
            }
            
            // Double consonant (small tsu)
            if (c1 == c2 && c1 !in "aiueon") {
                return Pair(2, "っ$c2")
            }
        }
        
        // Priority 3: 1 char (e.g. a)
        if (len >= 1) {
            val suffix = text.substring(len - 1)
            if (m.containsKey(suffix)) return Pair(1, m[suffix]!!)
        }
        
        return null
    }
}