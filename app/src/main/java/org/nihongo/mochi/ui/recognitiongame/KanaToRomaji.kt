package org.nihongo.mochi.ui.recognitiongame

object KanaToRomaji {
    private val kanaMap = HashMap<String, String>()

    init {
        // Hiragana
        kanaMap["あ"] = "a"; kanaMap["い"] = "i"; kanaMap["う"] = "u"; kanaMap["え"] = "e"; kanaMap["お"] = "o"
        kanaMap["か"] = "ka"; kanaMap["き"] = "ki"; kanaMap["く"] = "ku"; kanaMap["け"] = "ke"; kanaMap["こ"] = "ko"
        kanaMap["さ"] = "sa"; kanaMap["し"] = "shi"; kanaMap["す"] = "su"; kanaMap["せ"] = "se"; kanaMap["そ"] = "so"
        kanaMap["た"] = "ta"; kanaMap["ち"] = "chi"; kanaMap["つ"] = "tsu"; kanaMap["て"] = "te"; kanaMap["と"] = "to"
        kanaMap["な"] = "na"; kanaMap["に"] = "ni"; kanaMap["ぬ"] = "nu"; kanaMap["ね"] = "ne"; kanaMap["の"] = "no"
        kanaMap["は"] = "ha"; kanaMap["ひ"] = "hi"; kanaMap["ふ"] = "fu"; kanaMap["へ"] = "he"; kanaMap["ほ"] = "ho"
        kanaMap["ま"] = "ma"; kanaMap["み"] = "mi"; kanaMap["む"] = "mu"; kanaMap["め"] = "me"; kanaMap["も"] = "mo"
        kanaMap["や"] = "ya"; kanaMap["ゆ"] = "yu"; kanaMap["よ"] = "yo"
        kanaMap["ら"] = "ra"; kanaMap["り"] = "ri"; kanaMap["る"] = "ru"; kanaMap["れ"] = "re"; kanaMap["ろ"] = "ro"
        kanaMap["わ"] = "wa"; kanaMap["を"] = "wo"; kanaMap["ん"] = "n"
        
        kanaMap["が"] = "ga"; kanaMap["ぎ"] = "gi"; kanaMap["ぐ"] = "gu"; kanaMap["げ"] = "ge"; kanaMap["ご"] = "go"
        kanaMap["ざ"] = "za"; kanaMap["じ"] = "ji"; kanaMap["ず"] = "zu"; kanaMap["ぜ"] = "ze"; kanaMap["ぞ"] = "zo"
        kanaMap["だ"] = "da"; kanaMap["ぢ"] = "ji"; kanaMap["づ"] = "zu"; kanaMap["で"] = "de"; kanaMap["ど"] = "do"
        kanaMap["ば"] = "ba"; kanaMap["び"] = "bi"; kanaMap["ぶ"] = "bu"; kanaMap["べ"] = "be"; kanaMap["ぼ"] = "bo"
        kanaMap["ぱ"] = "pa"; kanaMap["ぴ"] = "pi"; kanaMap["ぷ"] = "pu"; kanaMap["ぺ"] = "pe"; kanaMap["ぽ"] = "po"
        
        kanaMap["きゃ"] = "kya"; kanaMap["きゅ"] = "kyu"; kanaMap["きょ"] = "kyo"
        kanaMap["しゃ"] = "sha"; kanaMap["しゅ"] = "shu"; kanaMap["しょ"] = "sho"
        kanaMap["ちゃ"] = "cha"; kanaMap["ちゅ"] = "chu"; kanaMap["ちょ"] = "cho"
        kanaMap["にゃ"] = "nya"; kanaMap["にゅ"] = "nyu"; kanaMap["にょ"] = "nyo"
        kanaMap["ひゃ"] = "hya"; kanaMap["ひゅ"] = "hyu"; kanaMap["ひょ"] = "hyo"
        kanaMap["みゃ"] = "mya"; kanaMap["みゅ"] = "myu"; kanaMap["みょ"] = "myo"
        kanaMap["りゃ"] = "rya"; kanaMap["りゅ"] = "ryu"; kanaMap["りょ"] = "ryo"
        kanaMap["ぎゃ"] = "gya"; kanaMap["ぎゅ"] = "gyu"; kanaMap["ぎょ"] = "gyo"
        kanaMap["じゃ"] = "ja"; kanaMap["じゅ"] = "ju"; kanaMap["じょ"] = "jo"
        kanaMap["びゃ"] = "bya"; kanaMap["びゅ"] = "byu"; kanaMap["びょ"] = "byo"
        kanaMap["ぴゃ"] = "pya"; kanaMap["ぴゅ"] = "pyu"; kanaMap["ぴょ"] = "pyo"

        // Katakana
        kanaMap["ア"] = "a"; kanaMap["イ"] = "i"; kanaMap["ウ"] = "u"; kanaMap["エ"] = "e"; kanaMap["オ"] = "o"
        kanaMap["カ"] = "ka"; kanaMap["キ"] = "ki"; kanaMap["ク"] = "ku"; kanaMap["ケ"] = "ke"; kanaMap["コ"] = "ko"
        kanaMap["サ"] = "sa"; kanaMap["シ"] = "shi"; kanaMap["ス"] = "su"; kanaMap["セ"] = "se"; kanaMap["ソ"] = "so"
        kanaMap["タ"] = "ta"; kanaMap["チ"] = "chi"; kanaMap["ツ"] = "tsu"; kanaMap["テ"] = "te"; kanaMap["ト"] = "to"
        kanaMap["ナ"] = "na"; kanaMap["ニ"] = "ni"; kanaMap["ヌ"] = "nu"; kanaMap["ネ"] = "ne"; kanaMap["ノ"] = "no"
        kanaMap["ハ"] = "ha"; kanaMap["ヒ"] = "hi"; kanaMap["フ"] = "fu"; kanaMap["ヘ"] = "he"; kanaMap["ホ"] = "ho"
        kanaMap["マ"] = "ma"; kanaMap["ミ"] = "mi"; kanaMap["ム"] = "mu"; kanaMap["メ"] = "me"; kanaMap["モ"] = "mo"
        kanaMap["ヤ"] = "ya"; kanaMap["ユ"] = "yu"; kanaMap["ヨ"] = "yo"
        kanaMap["ラ"] = "ra"; kanaMap["リ"] = "ri"; kanaMap["ル"] = "ru"; kanaMap["レ"] = "re"; kanaMap["ロ"] = "ro"
        kanaMap["ワ"] = "wa"; kanaMap["ヲ"] = "wo"; kanaMap["ン"] = "n"
        
        kanaMap["ガ"] = "ga"; kanaMap["ギ"] = "gi"; kanaMap["グ"] = "gu"; kanaMap["ゲ"] = "ge"; kanaMap["ゴ"] = "go"
        kanaMap["ザ"] = "za"; kanaMap["ジ"] = "ji"; kanaMap["ズ"] = "zu"; kanaMap["ゼ"] = "ze"; kanaMap["ゾ"] = "zo"
        kanaMap["ダ"] = "da"; kanaMap["ヂ"] = "ji"; kanaMap["ヅ"] = "zu"; kanaMap["デ"] = "de"; kanaMap["ド"] = "do"
        kanaMap["バ"] = "ba"; kanaMap["ビ"] = "bi"; kanaMap["ブ"] = "bu"; kanaMap["ベ"] = "be"; kanaMap["ボ"] = "bo"
        kanaMap["パ"] = "pa"; kanaMap["ピ"] = "pi"; kanaMap["プ"] = "pu"; kanaMap["ペ"] = "pe"; kanaMap["ポ"] = "po"
        
        kanaMap["キャ"] = "kya"; kanaMap["キュ"] = "kyu"; kanaMap["キョ"] = "kyo"
        kanaMap["シャ"] = "sha"; kanaMap["シュ"] = "shu"; kanaMap["ショ"] = "sho"
        kanaMap["チャ"] = "cha"; kanaMap["チュ"] = "chu"; kanaMap["チョ"] = "cho"
        kanaMap["ニャ"] = "nya"; kanaMap["ニュ"] = "nyu"; kanaMap["ニョ"] = "nyo"
        kanaMap["ヒャ"] = "hya"; kanaMap["ヒュ"] = "hyu"; kanaMap["ヒョ"] = "hyo"
        kanaMap["ミャ"] = "mya"; kanaMap["ミュ"] = "myu"; kanaMap["ミョ"] = "myo"
        kanaMap["リャ"] = "rya"; kanaMap["リュ"] = "ryu"; kanaMap["リョ"] = "ryo"
        kanaMap["ギャ"] = "gya"; kanaMap["ギュ"] = "gyu"; kanaMap["ギョ"] = "gyo"
        kanaMap["ジャ"] = "ja"; kanaMap["ジュ"] = "ju"; kanaMap["ジョ"] = "jo"
        kanaMap["ビャ"] = "bya"; kanaMap["ビュ"] = "byu"; kanaMap["ビョ"] = "byo"
        kanaMap["ピャ"] = "pya"; kanaMap["ピュ"] = "pyu"; kanaMap["ピョ"] = "pyo"
        
        // Long vowels
        kanaMap["ー"] = "-"
    }

    fun convert(text: String): String {
        val sb = StringBuilder()
        var i = 0
        while (i < text.length) {
            // Check 2 chars
            if (i + 1 < text.length) {
                val pair = text.substring(i, i + 2)
                if (kanaMap.containsKey(pair)) {
                    sb.append(kanaMap[pair])
                    i += 2
                    continue
                }
            }
            
            // Check 1 char
            val single = text.substring(i, i + 1)
            if (kanaMap.containsKey(single)) {
                sb.append(kanaMap[single])
            } else {
                // If sokuon
                if (single == "っ" || single == "ッ") {
                    if (i + 1 < text.length) {
                        // Look ahead for next character's romaji to double the consonant
                        var nextRomaji: String? = null
                        if (i + 2 < text.length && kanaMap.containsKey(text.substring(i + 1, i + 3))) {
                             nextRomaji = kanaMap[text.substring(i + 1, i + 3)]
                        } else if (i + 1 < text.length && kanaMap.containsKey(text.substring(i + 1, i + 2))) {
                             nextRomaji = kanaMap[text.substring(i + 1, i + 2)]
                        }
                        
                        if (nextRomaji != null && nextRomaji.isNotEmpty()) {
                            sb.append(nextRomaji[0])
                        }
                    }
                } else {
                    sb.append(single) // Keep original if not found (kanji, punctuation)
                }
            }
            i++
        }
        return sb.toString()
    }
}