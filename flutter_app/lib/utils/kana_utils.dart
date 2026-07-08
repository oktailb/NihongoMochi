class KanaUtils {
  static String hiraganaToKatakana(String s) {
    return s.runes.map((r) {
      if (r >= 0x3041 && r <= 0x3096) {
        return r + 0x60;
      }
      return r;
    }).map((r) => String.fromCharCode(r)).join();
  }

  static String katakanaToHiragana(String s) {
    return s.runes.map((r) {
      if (r >= 0x30A1 && r <= 0x30F6) {
        return r - 0x60;
      }
      return r;
    }).map((r) => String.fromCharCode(r)).join();
  }
}
