import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:intl/intl.dart' as intl;

import 'app_localizations_ar.dart';
import 'app_localizations_bn.dart';
import 'app_localizations_de.dart';
import 'app_localizations_en.dart';
import 'app_localizations_es.dart';
import 'app_localizations_fr.dart';
import 'app_localizations_id.dart';
import 'app_localizations_it.dart';
import 'app_localizations_ja.dart';
import 'app_localizations_ko.dart';
import 'app_localizations_mn.dart';
import 'app_localizations_pt.dart';
import 'app_localizations_ru.dart';
import 'app_localizations_th.dart';
import 'app_localizations_uk.dart';
import 'app_localizations_vi.dart';
import 'app_localizations_zh.dart';

// ignore_for_file: type=lint

/// Callers can lookup localized strings with an instance of AppLocalizations
/// returned by `AppLocalizations.of(context)`.
///
/// Applications need to include `AppLocalizations.delegate()` in their app's
/// `localizationDelegates` list, and the locales they support in the app's
/// `supportedLocales` list. For example:
///
/// ```dart
/// import 'gen/app_localizations.dart';
///
/// return MaterialApp(
///   localizationsDelegates: AppLocalizations.localizationsDelegates,
///   supportedLocales: AppLocalizations.supportedLocales,
///   home: MyApplicationHome(),
/// );
/// ```
///
/// ## Update pubspec.yaml
///
/// Please make sure to update your pubspec.yaml to include the following
/// packages:
///
/// ```yaml
/// dependencies:
///   # Internationalization support.
///   flutter_localizations:
///     sdk: flutter
///   intl: any # Use the pinned version from flutter_localizations
///
///   # Rest of dependencies
/// ```
///
/// ## iOS Applications
///
/// iOS applications define key application metadata, including supported
/// locales, in an Info.plist file that is built into the application bundle.
/// To configure the locales supported by your app, you’ll need to edit this
/// file.
///
/// First, open your project’s ios/Runner.xcworkspace Xcode workspace file.
/// Then, in the Project Navigator, open the Info.plist file under the Runner
/// project’s Runner folder.
///
/// Next, select the Information Property List item, select Add Item from the
/// Editor menu, then select Localizations from the pop-up menu.
///
/// Select and expand the newly-created Localizations item then, for each
/// locale your application supports, add a new item and select the locale
/// you wish to add from the pop-up menu in the Value field. This list should
/// be consistent with the languages listed in the AppLocalizations.supportedLocales
/// property.
abstract class AppLocalizations {
  AppLocalizations(String locale)
    : localeName = intl.Intl.canonicalizedLocale(locale.toString());

  final String localeName;

  static AppLocalizations? of(BuildContext context) {
    return Localizations.of<AppLocalizations>(context, AppLocalizations);
  }

  static const LocalizationsDelegate<AppLocalizations> delegate =
      _AppLocalizationsDelegate();

  /// A list of this localizations delegate along with the default localizations
  /// delegates.
  ///
  /// Returns a list of localizations delegates containing this delegate along with
  /// GlobalMaterialLocalizations.delegate, GlobalCupertinoLocalizations.delegate,
  /// and GlobalWidgetsLocalizations.delegate.
  ///
  /// Additional delegates can be added by appending to this list in
  /// MaterialApp. This list does not have to be used at all if a custom list
  /// of delegates is preferred or required.
  static const List<LocalizationsDelegate<dynamic>> localizationsDelegates =
      <LocalizationsDelegate<dynamic>>[
        delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
      ];

  /// A list of this localizations delegate's supported locales.
  static const List<Locale> supportedLocales = <Locale>[
    Locale('ar'),
    Locale('bn'),
    Locale('de'),
    Locale('en'),
    Locale('es'),
    Locale('fr'),
    Locale('id'),
    Locale('it'),
    Locale('ja'),
    Locale('ko'),
    Locale('mn'),
    Locale('pt'),
    Locale('ru'),
    Locale('th'),
    Locale('uk'),
    Locale('vi'),
    Locale('zh'),
  ];

  /// No description provided for @app_name.
  ///
  /// In en, this message translates to:
  /// **'Nihongo Mochi'**
  String get app_name;

  /// No description provided for @navigation_drawer_open.
  ///
  /// In en, this message translates to:
  /// **'Open navigation drawer'**
  String get navigation_drawer_open;

  /// No description provided for @navigation_drawer_close.
  ///
  /// In en, this message translates to:
  /// **'Close navigation drawer'**
  String get navigation_drawer_close;

  /// No description provided for @nav_header_title.
  ///
  /// In en, this message translates to:
  /// **'Nihongo Mochi'**
  String get nav_header_title;

  /// No description provided for @nav_header_subtitle.
  ///
  /// In en, this message translates to:
  /// **'vincent.lecoq@gmail.com'**
  String get nav_header_subtitle;

  /// No description provided for @nav_header_desc.
  ///
  /// In en, this message translates to:
  /// **'Navigation header'**
  String get nav_header_desc;

  /// No description provided for @action_settings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get action_settings;

  /// No description provided for @action_sign_in.
  ///
  /// In en, this message translates to:
  /// **'Sign In'**
  String get action_sign_in;

  /// No description provided for @action_achievements.
  ///
  /// In en, this message translates to:
  /// **'Achievements'**
  String get action_achievements;

  /// No description provided for @menu_transform.
  ///
  /// In en, this message translates to:
  /// **'Transform'**
  String get menu_transform;

  /// No description provided for @menu_reflow.
  ///
  /// In en, this message translates to:
  /// **'Reflow'**
  String get menu_reflow;

  /// No description provided for @menu_slideshow.
  ///
  /// In en, this message translates to:
  /// **'Slideshow'**
  String get menu_slideshow;

  /// No description provided for @menu_settings.
  ///
  /// In en, this message translates to:
  /// **'Settings'**
  String get menu_settings;

  /// No description provided for @lorem_ipsum_title.
  ///
  /// In en, this message translates to:
  /// **'Lorem Ipsum'**
  String get lorem_ipsum_title;

  /// No description provided for @lorem_ipsum.
  ///
  /// In en, this message translates to:
  /// **'Lorem Ipsum is simply placeholder text of the printing and\n        typesetting industry. Lorem Ipsum has been the industry\'s standard placeholder text ever\n        since the 1500s, when an unknown printer took a galley of type and scrambled it to make a\n        type specimen book.'**
  String get lorem_ipsum;

  /// No description provided for @fab_content_description.
  ///
  /// In en, this message translates to:
  /// **'Represents a view to invoke an action'**
  String get fab_content_description;

  /// No description provided for @image_view_item_transform_content_description.
  ///
  /// In en, this message translates to:
  /// **'Represents an image view in the\n        item'**
  String get image_view_item_transform_content_description;

  /// No description provided for @settings_title.
  ///
  /// In en, this message translates to:
  /// **'Options'**
  String get settings_title;

  /// No description provided for @settings_language.
  ///
  /// In en, this message translates to:
  /// **'Language'**
  String get settings_language;

  /// No description provided for @settings_pronunciation.
  ///
  /// In en, this message translates to:
  /// **'Pronunciation'**
  String get settings_pronunciation;

  /// No description provided for @settings_pronunciation_roman.
  ///
  /// In en, this message translates to:
  /// **'Roman alphabet'**
  String get settings_pronunciation_roman;

  /// No description provided for @settings_pronunciation_hiragana.
  ///
  /// In en, this message translates to:
  /// **'Japanese Hiragana'**
  String get settings_pronunciation_hiragana;

  /// No description provided for @settings_default_user_list.
  ///
  /// In en, this message translates to:
  /// **'Default user list'**
  String get settings_default_user_list;

  /// No description provided for @settings_add_wrong_answers.
  ///
  /// In en, this message translates to:
  /// **'Automatically add wrong answers'**
  String get settings_add_wrong_answers;

  /// No description provided for @settings_remove_good_answers.
  ///
  /// In en, this message translates to:
  /// **'Automatically remove 10/10 words'**
  String get settings_remove_good_answers;

  /// No description provided for @settings_text_size.
  ///
  /// In en, this message translates to:
  /// **'Text size'**
  String get settings_text_size;

  /// No description provided for @settings_animation_speed.
  ///
  /// In en, this message translates to:
  /// **'Animation speed'**
  String get settings_animation_speed;

  /// No description provided for @settings_theme.
  ///
  /// In en, this message translates to:
  /// **'Theme (Light/Dark)'**
  String get settings_theme;

  /// No description provided for @settings_reset_tutorial.
  ///
  /// In en, this message translates to:
  /// **'Reset tutorial'**
  String get settings_reset_tutorial;

  /// No description provided for @settings_send_bug_report.
  ///
  /// In en, this message translates to:
  /// **'Send bug report'**
  String get settings_send_bug_report;

  /// No description provided for @game_recap_title.
  ///
  /// In en, this message translates to:
  /// **'Recognition game'**
  String get game_recap_title;

  /// No description provided for @game_recap_meaning.
  ///
  /// In en, this message translates to:
  /// **'Meaning'**
  String get game_recap_meaning;

  /// No description provided for @game_recap_reading.
  ///
  /// In en, this message translates to:
  /// **'Reading'**
  String get game_recap_reading;

  /// No description provided for @game_recap_common_pronunciations.
  ///
  /// In en, this message translates to:
  /// **'Common pronunciations'**
  String get game_recap_common_pronunciations;

  /// No description provided for @game_recap_random_pronunciations.
  ///
  /// In en, this message translates to:
  /// **'Random pronunciations'**
  String get game_recap_random_pronunciations;

  /// No description provided for @game_recap_play.
  ///
  /// In en, this message translates to:
  /// **'Play'**
  String get game_recap_play;

  /// No description provided for @game_writing_label_meaning.
  ///
  /// In en, this message translates to:
  /// **'Meaning'**
  String get game_writing_label_meaning;

  /// No description provided for @game_writing_label_reading.
  ///
  /// In en, this message translates to:
  /// **'Reading'**
  String get game_writing_label_reading;

  /// No description provided for @enter_your_answer.
  ///
  /// In en, this message translates to:
  /// **'Enter your answer'**
  String get enter_your_answer;

  /// No description provided for @submit.
  ///
  /// In en, this message translates to:
  /// **'Submit'**
  String get submit;

  /// No description provided for @game_writing_correction.
  ///
  /// In en, this message translates to:
  /// **'Correction'**
  String get game_writing_correction;

  /// No description provided for @recognition_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Choose a group of kanji and learn to recognize their different basic meanings'**
  String get recognition_subtitle;

  /// No description provided for @recognition_fundamentals.
  ///
  /// In en, this message translates to:
  /// **'Fundamentals'**
  String get recognition_fundamentals;

  /// No description provided for @recognition_jlpt.
  ///
  /// In en, this message translates to:
  /// **'Japanese Language Proficiency Test'**
  String get recognition_jlpt;

  /// No description provided for @recognition_primary_school.
  ///
  /// In en, this message translates to:
  /// **'Primary school (Jōyō 1..1006)'**
  String get recognition_primary_school;

  /// No description provided for @recognition_high_school.
  ///
  /// In en, this message translates to:
  /// **'High school (Jōyō 1007..2136)'**
  String get recognition_high_school;

  /// No description provided for @section_challenges.
  ///
  /// In en, this message translates to:
  /// **'Challenges'**
  String get section_challenges;

  /// No description provided for @challenge_native.
  ///
  /// In en, this message translates to:
  /// **'Native Challenge'**
  String get challenge_native;

  /// No description provided for @challenge_no_reading.
  ///
  /// In en, this message translates to:
  /// **'No Reading'**
  String get challenge_no_reading;

  /// No description provided for @challenge_no_meaning.
  ///
  /// In en, this message translates to:
  /// **'No Meaning'**
  String get challenge_no_meaning;

  /// No description provided for @writing_game_recap_title.
  ///
  /// In en, this message translates to:
  /// **'Writing Game'**
  String get writing_game_recap_title;

  /// No description provided for @writing_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Choose a group of words and learn how to write them.'**
  String get writing_subtitle;

  /// No description provided for @writing_kanji_tree.
  ///
  /// In en, this message translates to:
  /// **'Kanji Tree'**
  String get writing_kanji_tree;

  /// No description provided for @writing_kanji_tree_subtitle.
  ///
  /// In en, this message translates to:
  /// **'If you haven\'t learned something yet:\\n(10/10 in the relevant game)'**
  String get writing_kanji_tree_subtitle;

  /// No description provided for @writing_kanji_tree_ignore.
  ///
  /// In en, this message translates to:
  /// **'Ignore'**
  String get writing_kanji_tree_ignore;

  /// No description provided for @writing_kanji_tree_include.
  ///
  /// In en, this message translates to:
  /// **'include them anyway'**
  String get writing_kanji_tree_include;

  /// No description provided for @writing_find_words.
  ///
  /// In en, this message translates to:
  /// **'Find words'**
  String get writing_find_words;

  /// No description provided for @writing_most_used_words.
  ///
  /// In en, this message translates to:
  /// **'Most used words'**
  String get writing_most_used_words;

  /// No description provided for @writing_find_kanji.
  ///
  /// In en, this message translates to:
  /// **'Find a Kanji'**
  String get writing_find_kanji;

  /// No description provided for @writing_lists.
  ///
  /// In en, this message translates to:
  /// **'Lists'**
  String get writing_lists;

  /// No description provided for @writing_user_lists.
  ///
  /// In en, this message translates to:
  /// **'User list / study lists'**
  String get writing_user_lists;

  /// No description provided for @reading_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Choose a group of words and learn their pronunciations.'**
  String get reading_subtitle;

  /// No description provided for @reading_kanji_tree_subtitle.
  ///
  /// In en, this message translates to:
  /// **'If the word contains kanji you haven\'t learned yet:\\n(10/10 in the recognition game)'**
  String get reading_kanji_tree_subtitle;

  /// No description provided for @reading_user_list.
  ///
  /// In en, this message translates to:
  /// **'User list'**
  String get reading_user_list;

  /// No description provided for @reading_modify_button.
  ///
  /// In en, this message translates to:
  /// **'Modify'**
  String get reading_modify_button;

  /// No description provided for @reading_kanji_solo.
  ///
  /// In en, this message translates to:
  /// **'Kanji only'**
  String get reading_kanji_solo;

  /// No description provided for @reading_simple_words.
  ///
  /// In en, this message translates to:
  /// **'Simple words'**
  String get reading_simple_words;

  /// No description provided for @reading_compound_words.
  ///
  /// In en, this message translates to:
  /// **'Compound words'**
  String get reading_compound_words;

  /// No description provided for @reading_ignore_known_words.
  ///
  /// In en, this message translates to:
  /// **'Ignore perfectly known words'**
  String get reading_ignore_known_words;

  /// No description provided for @menu_recognition.
  ///
  /// In en, this message translates to:
  /// **'Recognition'**
  String get menu_recognition;

  /// No description provided for @menu_reading.
  ///
  /// In en, this message translates to:
  /// **'Reading'**
  String get menu_reading;

  /// No description provided for @menu_writing.
  ///
  /// In en, this message translates to:
  /// **'Writing'**
  String get menu_writing;

  /// No description provided for @menu_dictionary.
  ///
  /// In en, this message translates to:
  /// **'Dictionary'**
  String get menu_dictionary;

  /// No description provided for @menu_results.
  ///
  /// In en, this message translates to:
  /// **'Results'**
  String get menu_results;

  /// No description provided for @menu_about.
  ///
  /// In en, this message translates to:
  /// **'About'**
  String get menu_about;

  /// No description provided for @level_n5.
  ///
  /// In en, this message translates to:
  /// **'N5'**
  String get level_n5;

  /// No description provided for @level_n4.
  ///
  /// In en, this message translates to:
  /// **'N4'**
  String get level_n4;

  /// No description provided for @level_n3.
  ///
  /// In en, this message translates to:
  /// **'N3'**
  String get level_n3;

  /// No description provided for @level_n2.
  ///
  /// In en, this message translates to:
  /// **'N2'**
  String get level_n2;

  /// No description provided for @level_n1.
  ///
  /// In en, this message translates to:
  /// **'N1'**
  String get level_n1;

  /// No description provided for @level_class_1.
  ///
  /// In en, this message translates to:
  /// **'Class 1'**
  String get level_class_1;

  /// No description provided for @level_class_2.
  ///
  /// In en, this message translates to:
  /// **'Class 2'**
  String get level_class_2;

  /// No description provided for @level_class_3.
  ///
  /// In en, this message translates to:
  /// **'Class 3'**
  String get level_class_3;

  /// No description provided for @level_class_4.
  ///
  /// In en, this message translates to:
  /// **'Class 4'**
  String get level_class_4;

  /// No description provided for @level_class_5.
  ///
  /// In en, this message translates to:
  /// **'Class 5'**
  String get level_class_5;

  /// No description provided for @level_class_6.
  ///
  /// In en, this message translates to:
  /// **'Class 6'**
  String get level_class_6;

  /// No description provided for @level_high_school_1.
  ///
  /// In en, this message translates to:
  /// **'Test 4'**
  String get level_high_school_1;

  /// No description provided for @level_high_school_2.
  ///
  /// In en, this message translates to:
  /// **'Test 3'**
  String get level_high_school_2;

  /// No description provided for @level_high_school_3.
  ///
  /// In en, this message translates to:
  /// **'Pre-2 Test'**
  String get level_high_school_3;

  /// No description provided for @level_high_school_4.
  ///
  /// In en, this message translates to:
  /// **'Test 2'**
  String get level_high_school_4;

  /// No description provided for @level_hiragana.
  ///
  /// In en, this message translates to:
  /// **'Hiragana'**
  String get level_hiragana;

  /// No description provided for @level_katakana.
  ///
  /// In en, this message translates to:
  /// **'Katakana'**
  String get level_katakana;

  /// No description provided for @home_recognition_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Learn to recognize kanji and remember their basic meanings.'**
  String get home_recognition_subtitle;

  /// No description provided for @home_reading_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Learn to read whole words and how to pronounce them in different contexts'**
  String get home_reading_subtitle;

  /// No description provided for @home_writing_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Practice writing words and burn the kanji into your memory.'**
  String get home_writing_subtitle;

  /// No description provided for @title_home.
  ///
  /// In en, this message translates to:
  /// **'Nihongo Mochi'**
  String get title_home;

  /// No description provided for @title_game_recap.
  ///
  /// In en, this message translates to:
  /// **'Game recap'**
  String get title_game_recap;

  /// No description provided for @about_retribution.
  ///
  /// In en, this message translates to:
  /// **'Retribution'**
  String get about_retribution;

  /// No description provided for @about_patreon.
  ///
  /// In en, this message translates to:
  /// **'Support me on Patreon'**
  String get about_patreon;

  /// No description provided for @about_tipeee.
  ///
  /// In en, this message translates to:
  /// **'Donate on Tipeee'**
  String get about_tipeee;

  /// No description provided for @my_image_description.
  ///
  /// In en, this message translates to:
  /// **'Next page button'**
  String get my_image_description;

  /// No description provided for @about_version_info.
  ///
  /// In en, this message translates to:
  /// **'Version Information'**
  String get about_version_info;

  /// No description provided for @about_version_label.
  ///
  /// In en, this message translates to:
  /// **'Version:'**
  String get about_version_label;

  /// No description provided for @about_date_label.
  ///
  /// In en, this message translates to:
  /// **'Date:'**
  String get about_date_label;

  /// No description provided for @about_issue_tracker.
  ///
  /// In en, this message translates to:
  /// **'Issue tracker,\\nKnown issues and planned features'**
  String get about_issue_tracker;

  /// No description provided for @about_credits.
  ///
  /// In en, this message translates to:
  /// **'Credits'**
  String get about_credits;

  /// No description provided for @about_author_name.
  ///
  /// In en, this message translates to:
  /// **'LECOQ Vincent'**
  String get about_author_name;

  /// No description provided for @about_author_role.
  ///
  /// In en, this message translates to:
  /// **'Design, programming, etc.'**
  String get about_author_role;

  /// No description provided for @about_pedagogical_content.
  ///
  /// In en, this message translates to:
  /// **'Japanese pedagogical content'**
  String get about_pedagogical_content;

  /// No description provided for @about_kanji_data_credit.
  ///
  /// In en, this message translates to:
  /// **'Kanji data by David Gouveia'**
  String get about_kanji_data_credit;

  /// No description provided for @about_header_picture_credit.
  ///
  /// In en, this message translates to:
  /// **'Header picture by Charles Knowles'**
  String get about_header_picture_credit;

  /// No description provided for @results_title.
  ///
  /// In en, this message translates to:
  /// **'Results'**
  String get results_title;

  /// No description provided for @results_recognition_title.
  ///
  /// In en, this message translates to:
  /// **'Recognition'**
  String get results_recognition_title;

  /// No description provided for @results_section_kanas.
  ///
  /// In en, this message translates to:
  /// **'Kanas (Hiragana / Katakana)'**
  String get results_section_kanas;

  /// No description provided for @results_section_jlpt.
  ///
  /// In en, this message translates to:
  /// **'JLPT (N5 - N1)'**
  String get results_section_jlpt;

  /// No description provided for @results_section_school.
  ///
  /// In en, this message translates to:
  /// **'School (Grades / High School)'**
  String get results_section_school;

  /// No description provided for @results_section_frequency.
  ///
  /// In en, this message translates to:
  /// **'Frequency'**
  String get results_section_frequency;

  /// No description provided for @results_frequency_x_words.
  ///
  /// In en, this message translates to:
  /// **'{param1} most frequent words'**
  String results_frequency_x_words(Object param1);

  /// No description provided for @results_hiragana.
  ///
  /// In en, this message translates to:
  /// **'Hiraganas - {param1}%'**
  String results_hiragana(Object param1);

  /// No description provided for @results_katakana.
  ///
  /// In en, this message translates to:
  /// **'Katakanas - {param1}%'**
  String results_katakana(Object param1);

  /// No description provided for @results_jlpt_n5.
  ///
  /// In en, this message translates to:
  /// **'JLPT N5 - {param1}%'**
  String results_jlpt_n5(Object param1);

  /// No description provided for @results_jlpt_n4.
  ///
  /// In en, this message translates to:
  /// **'JLPT N4 - {param1}%'**
  String results_jlpt_n4(Object param1);

  /// No description provided for @results_jlpt_n3.
  ///
  /// In en, this message translates to:
  /// **'JLPT N3 - {param1}%'**
  String results_jlpt_n3(Object param1);

  /// No description provided for @results_jlpt_n2.
  ///
  /// In en, this message translates to:
  /// **'JLPT N2 - {param1}%'**
  String results_jlpt_n2(Object param1);

  /// No description provided for @results_jlpt_n1.
  ///
  /// In en, this message translates to:
  /// **'JLPT N1 - {param1}%'**
  String results_jlpt_n1(Object param1);

  /// No description provided for @results_grade_1.
  ///
  /// In en, this message translates to:
  /// **'Grade 1 - {param1}%'**
  String results_grade_1(Object param1);

  /// No description provided for @results_grade_2.
  ///
  /// In en, this message translates to:
  /// **'Grade 2 - {param1}%'**
  String results_grade_2(Object param1);

  /// No description provided for @results_grade_3.
  ///
  /// In en, this message translates to:
  /// **'Grade 3 - {param1}%'**
  String results_grade_3(Object param1);

  /// No description provided for @results_grade_4.
  ///
  /// In en, this message translates to:
  /// **'Grade 4 - {param1}%'**
  String results_grade_4(Object param1);

  /// No description provided for @results_grade_5.
  ///
  /// In en, this message translates to:
  /// **'Grade 5 - {param1}%'**
  String results_grade_5(Object param1);

  /// No description provided for @results_grade_6.
  ///
  /// In en, this message translates to:
  /// **'Grade 6 - {param1}%'**
  String results_grade_6(Object param1);

  /// No description provided for @results_college.
  ///
  /// In en, this message translates to:
  /// **'College - {param1}%'**
  String results_college(Object param1);

  /// No description provided for @results_high_school.
  ///
  /// In en, this message translates to:
  /// **'High school - {param1}%'**
  String results_high_school(Object param1);

  /// No description provided for @results_reading_title.
  ///
  /// In en, this message translates to:
  /// **'Reading'**
  String get results_reading_title;

  /// No description provided for @results_total_count.
  ///
  /// In en, this message translates to:
  /// **'Total: {param1}'**
  String results_total_count(Object param1);

  /// No description provided for @results_most_frequent_words.
  ///
  /// In en, this message translates to:
  /// **'{param1} most frequent words - {param2}%'**
  String results_most_frequent_words(Object param1, Object param2);

  /// No description provided for @results_writing_title.
  ///
  /// In en, this message translates to:
  /// **'Writing'**
  String get results_writing_title;

  /// No description provided for @results_backup.
  ///
  /// In en, this message translates to:
  /// **'Backup'**
  String get results_backup;

  /// No description provided for @results_restore.
  ///
  /// In en, this message translates to:
  /// **'Restore'**
  String get results_restore;

  /// No description provided for @results_sync.
  ///
  /// In en, this message translates to:
  /// **'Sync'**
  String get results_sync;

  /// No description provided for @mode_revise.
  ///
  /// In en, this message translates to:
  /// **'Revise'**
  String get mode_revise;

  /// No description provided for @options.
  ///
  /// In en, this message translates to:
  /// **'Options'**
  String get options;

  /// No description provided for @word_type_all.
  ///
  /// In en, this message translates to:
  /// **'All'**
  String get word_type_all;

  /// No description provided for @word_type_wa.
  ///
  /// In en, this message translates to:
  /// **'Japanese (和)'**
  String get word_type_wa;

  /// No description provided for @word_type_ko.
  ///
  /// In en, this message translates to:
  /// **'Proper noun (固)'**
  String get word_type_ko;

  /// No description provided for @word_type_gai.
  ///
  /// In en, this message translates to:
  /// **'Foreign (外)'**
  String get word_type_gai;

  /// No description provided for @word_type_kon.
  ///
  /// In en, this message translates to:
  /// **'Mixed (混)'**
  String get word_type_kon;

  /// No description provided for @word_type_kan.
  ///
  /// In en, this message translates to:
  /// **'Sino-Japanese (漢)'**
  String get word_type_kan;

  /// No description provided for @word_type_kigo.
  ///
  /// In en, this message translates to:
  /// **'Symbol (記号)'**
  String get word_type_kigo;

  /// No description provided for @language_fr_fr.
  ///
  /// In en, this message translates to:
  /// **'Français (France)'**
  String get language_fr_fr;

  /// No description provided for @language_en_gb.
  ///
  /// In en, this message translates to:
  /// **'English (UK)'**
  String get language_en_gb;

  /// No description provided for @language_it_it.
  ///
  /// In en, this message translates to:
  /// **'Italiano'**
  String get language_it_it;

  /// No description provided for @language_de_de.
  ///
  /// In en, this message translates to:
  /// **'Deutsch (DE)'**
  String get language_de_de;

  /// No description provided for @language_es_sp.
  ///
  /// In en, this message translates to:
  /// **'Español (España)'**
  String get language_es_sp;

  /// No description provided for @language_bn_bn.
  ///
  /// In en, this message translates to:
  /// **'বাংলা'**
  String get language_bn_bn;

  /// No description provided for @language_th.
  ///
  /// In en, this message translates to:
  /// **'ไทย'**
  String get language_th;

  /// No description provided for @language_ar_ar.
  ///
  /// In en, this message translates to:
  /// **'العربية'**
  String get language_ar_ar;

  /// No description provided for @language_pt_br.
  ///
  /// In en, this message translates to:
  /// **'Português (Brasil)'**
  String get language_pt_br;

  /// No description provided for @language_ko_kr.
  ///
  /// In en, this message translates to:
  /// **'한국어'**
  String get language_ko_kr;

  /// No description provided for @language_ru_ru.
  ///
  /// In en, this message translates to:
  /// **'Русский'**
  String get language_ru_ru;

  /// No description provided for @language_in_id.
  ///
  /// In en, this message translates to:
  /// **'Bahasa Indonesia'**
  String get language_in_id;

  /// No description provided for @language_zh_cn.
  ///
  /// In en, this message translates to:
  /// **'简体中文'**
  String get language_zh_cn;

  /// No description provided for @language_vi_vn.
  ///
  /// In en, this message translates to:
  /// **'Tiếng Việt'**
  String get language_vi_vn;

  /// No description provided for @dictionary_search_hint_text.
  ///
  /// In en, this message translates to:
  /// **'Text search'**
  String get dictionary_search_hint_text;

  /// No description provided for @dictionary_search_by_drawing_desc.
  ///
  /// In en, this message translates to:
  /// **'Search by drawing'**
  String get dictionary_search_by_drawing_desc;

  /// No description provided for @dictionary_mode_reading.
  ///
  /// In en, this message translates to:
  /// **'Reading (Kanas)'**
  String get dictionary_mode_reading;

  /// No description provided for @dictionary_mode_meaning.
  ///
  /// In en, this message translates to:
  /// **'Meaning (Romaji)'**
  String get dictionary_mode_meaning;

  /// No description provided for @dictionary_search_hint_strokes.
  ///
  /// In en, this message translates to:
  /// **'Strokes'**
  String get dictionary_search_hint_strokes;

  /// No description provided for @dictionary_match_exact.
  ///
  /// In en, this message translates to:
  /// **'Exact'**
  String get dictionary_match_exact;

  /// No description provided for @dictionary_clear_drawing_desc.
  ///
  /// In en, this message translates to:
  /// **'Clear drawing filter'**
  String get dictionary_clear_drawing_desc;

  /// No description provided for @dictionary_search_button.
  ///
  /// In en, this message translates to:
  /// **'Search'**
  String get dictionary_search_button;

  /// No description provided for @dictionary_results_count_format.
  ///
  /// In en, this message translates to:
  /// **'{param1} results'**
  String dictionary_results_count_format(Object param1);

  /// No description provided for @dictionary_reading_on.
  ///
  /// In en, this message translates to:
  /// **'On:'**
  String get dictionary_reading_on;

  /// No description provided for @dictionary_reading_kun.
  ///
  /// In en, this message translates to:
  /// **'Kun:'**
  String get dictionary_reading_kun;

  /// No description provided for @kanji_detail_components.
  ///
  /// In en, this message translates to:
  /// **'Components'**
  String get kanji_detail_components;

  /// No description provided for @settings_category_general.
  ///
  /// In en, this message translates to:
  /// **'General'**
  String get settings_category_general;

  /// No description provided for @settings_category_learning.
  ///
  /// In en, this message translates to:
  /// **'Learning'**
  String get settings_category_learning;

  /// No description provided for @settings_category_interface.
  ///
  /// In en, this message translates to:
  /// **'Interface'**
  String get settings_category_interface;

  /// No description provided for @section_fundamentals.
  ///
  /// In en, this message translates to:
  /// **'Fundamentals'**
  String get section_fundamentals;

  /// No description provided for @section_fundamentals_desc.
  ///
  /// In en, this message translates to:
  /// **'Basics of Japanese writing'**
  String get section_fundamentals_desc;

  /// No description provided for @section_jlpt.
  ///
  /// In en, this message translates to:
  /// **'JLPT'**
  String get section_jlpt;

  /// No description provided for @section_jlpt_desc.
  ///
  /// In en, this message translates to:
  /// **'Japanese Language Proficiency Test'**
  String get section_jlpt_desc;

  /// No description provided for @section_school.
  ///
  /// In en, this message translates to:
  /// **'Japanese School'**
  String get section_school;

  /// No description provided for @section_school_desc.
  ///
  /// In en, this message translates to:
  /// **'Japanese school curriculum'**
  String get section_school_desc;

  /// No description provided for @section_challenges_desc.
  ///
  /// In en, this message translates to:
  /// **'Challenges for advanced speakers'**
  String get section_challenges_desc;

  /// No description provided for @level_hiragana_desc.
  ///
  /// In en, this message translates to:
  /// **'hiragana syllabary'**
  String get level_hiragana_desc;

  /// No description provided for @level_katakana_desc.
  ///
  /// In en, this message translates to:
  /// **'Katakana syllabary'**
  String get level_katakana_desc;

  /// No description provided for @level_n5_desc.
  ///
  /// In en, this message translates to:
  /// **'Beginner level'**
  String get level_n5_desc;

  /// No description provided for @level_n4_desc.
  ///
  /// In en, this message translates to:
  /// **'Elementary level'**
  String get level_n4_desc;

  /// No description provided for @level_n3_desc.
  ///
  /// In en, this message translates to:
  /// **'Intermediate level'**
  String get level_n3_desc;

  /// No description provided for @level_n2_desc.
  ///
  /// In en, this message translates to:
  /// **'Pre-advanced level'**
  String get level_n2_desc;

  /// No description provided for @level_n1_desc.
  ///
  /// In en, this message translates to:
  /// **'Advanced level'**
  String get level_n1_desc;

  /// No description provided for @level_grade_1_desc.
  ///
  /// In en, this message translates to:
  /// **'First year of primary school'**
  String get level_grade_1_desc;

  /// No description provided for @level_grade_2_desc.
  ///
  /// In en, this message translates to:
  /// **'Second year of primary school'**
  String get level_grade_2_desc;

  /// No description provided for @level_grade_3_desc.
  ///
  /// In en, this message translates to:
  /// **'Third year of primary school'**
  String get level_grade_3_desc;

  /// No description provided for @level_grade_4_desc.
  ///
  /// In en, this message translates to:
  /// **'Fourth year of primary school'**
  String get level_grade_4_desc;

  /// No description provided for @level_grade_5_desc.
  ///
  /// In en, this message translates to:
  /// **'Fifth year of primary school'**
  String get level_grade_5_desc;

  /// No description provided for @level_grade_6_desc.
  ///
  /// In en, this message translates to:
  /// **'Sixth year of primary school'**
  String get level_grade_6_desc;

  /// No description provided for @level_college_1_desc.
  ///
  /// In en, this message translates to:
  /// **'First year of college'**
  String get level_college_1_desc;

  /// No description provided for @level_college_2_desc.
  ///
  /// In en, this message translates to:
  /// **'Second year of college'**
  String get level_college_2_desc;

  /// No description provided for @level_lycee_1_desc.
  ///
  /// In en, this message translates to:
  /// **'First year of high school'**
  String get level_lycee_1_desc;

  /// No description provided for @level_lycee_2_desc.
  ///
  /// In en, this message translates to:
  /// **'Second year of high school'**
  String get level_lycee_2_desc;

  /// No description provided for @level_native_challenge_desc.
  ///
  /// In en, this message translates to:
  /// **'Challenge for native level'**
  String get level_native_challenge_desc;

  /// No description provided for @level_no_reading_desc.
  ///
  /// In en, this message translates to:
  /// **'Recognition without reading'**
  String get level_no_reading_desc;

  /// No description provided for @level_no_meaning_desc.
  ///
  /// In en, this message translates to:
  /// **'Reading without meaning'**
  String get level_no_meaning_desc;

  /// No description provided for @activity_type_recognition.
  ///
  /// In en, this message translates to:
  /// **'Character recognition'**
  String get activity_type_recognition;

  /// No description provided for @activity_type_reading.
  ///
  /// In en, this message translates to:
  /// **'Reading and pronunciation'**
  String get activity_type_reading;

  /// No description provided for @activity_type_writing.
  ///
  /// In en, this message translates to:
  /// **'Writing and plotting'**
  String get activity_type_writing;

  /// No description provided for @activity_type_grammar.
  ///
  /// In en, this message translates to:
  /// **'Grammar and structure'**
  String get activity_type_grammar;

  /// No description provided for @activity_type_games.
  ///
  /// In en, this message translates to:
  /// **'Interactive games and challenges'**
  String get activity_type_games;

  /// No description provided for @level_grade_1.
  ///
  /// In en, this message translates to:
  /// **'Grade 1'**
  String get level_grade_1;

  /// No description provided for @level_grade_2.
  ///
  /// In en, this message translates to:
  /// **'Grade 2'**
  String get level_grade_2;

  /// No description provided for @level_grade_3.
  ///
  /// In en, this message translates to:
  /// **'Grade 3'**
  String get level_grade_3;

  /// No description provided for @level_grade_4.
  ///
  /// In en, this message translates to:
  /// **'Grade 4'**
  String get level_grade_4;

  /// No description provided for @level_grade_5.
  ///
  /// In en, this message translates to:
  /// **'Grade 5'**
  String get level_grade_5;

  /// No description provided for @level_grade_6.
  ///
  /// In en, this message translates to:
  /// **'Grade 6'**
  String get level_grade_6;

  /// No description provided for @level_grade_7.
  ///
  /// In en, this message translates to:
  /// **'College 1'**
  String get level_grade_7;

  /// No description provided for @level_grade_8.
  ///
  /// In en, this message translates to:
  /// **'College 2'**
  String get level_grade_8;

  /// No description provided for @level_grade_9.
  ///
  /// In en, this message translates to:
  /// **'High School 1'**
  String get level_grade_9;

  /// No description provided for @level_grade_10.
  ///
  /// In en, this message translates to:
  /// **'High school 2'**
  String get level_grade_10;

  /// No description provided for @level_native_challenge.
  ///
  /// In en, this message translates to:
  /// **'Native Challenge'**
  String get level_native_challenge;

  /// No description provided for @level_no_reading.
  ///
  /// In en, this message translates to:
  /// **'No Reading'**
  String get level_no_reading;

  /// No description provided for @level_no_meaning.
  ///
  /// In en, this message translates to:
  /// **'No Meaning'**
  String get level_no_meaning;

  /// No description provided for @level_college_1.
  ///
  /// In en, this message translates to:
  /// **'College 1'**
  String get level_college_1;

  /// No description provided for @level_college_2.
  ///
  /// In en, this message translates to:
  /// **'College 2'**
  String get level_college_2;

  /// No description provided for @level_lycee_1.
  ///
  /// In en, this message translates to:
  /// **'High School 1'**
  String get level_lycee_1;

  /// No description provided for @level_lycee_2.
  ///
  /// In en, this message translates to:
  /// **'High school 2'**
  String get level_lycee_2;

  /// No description provided for @about_category_informations.
  ///
  /// In en, this message translates to:
  /// **'Information'**
  String get about_category_informations;

  /// No description provided for @about_category_credits.
  ///
  /// In en, this message translates to:
  /// **'Credits'**
  String get about_category_credits;

  /// No description provided for @about_category_resources.
  ///
  /// In en, this message translates to:
  /// **'Resources'**
  String get about_category_resources;

  /// No description provided for @about_design_dev.
  ///
  /// In en, this message translates to:
  /// **'Design & Development'**
  String get about_design_dev;

  /// No description provided for @about_pedagogical.
  ///
  /// In en, this message translates to:
  /// **'Educational Content'**
  String get about_pedagogical;

  /// No description provided for @about_coming_soon.
  ///
  /// In en, this message translates to:
  /// **'(Future)'**
  String get about_coming_soon;

  /// No description provided for @about_rate_app.
  ///
  /// In en, this message translates to:
  /// **'Rate the application'**
  String get about_rate_app;

  /// No description provided for @dictionary_draw_kanji.
  ///
  /// In en, this message translates to:
  /// **'Draw a Kanji'**
  String get dictionary_draw_kanji;

  /// No description provided for @dictionary_recognize.
  ///
  /// In en, this message translates to:
  /// **'Recognize'**
  String get dictionary_recognize;

  /// No description provided for @verb_dict_form_desc.
  ///
  /// In en, this message translates to:
  /// **'Dictionary form of verbs (neutral form)'**
  String get verb_dict_form_desc;

  /// No description provided for @base_verbale_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base (-i form)'**
  String get base_verbale_desc;

  /// No description provided for @forme_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'Negative form of verbs'**
  String get forme_nai_desc;

  /// No description provided for @forme_ta_desc.
  ///
  /// In en, this message translates to:
  /// **'Past form of verbs'**
  String get forme_ta_desc;

  /// No description provided for @forme_te_desc.
  ///
  /// In en, this message translates to:
  /// **'-te form of verbs'**
  String get forme_te_desc;

  /// No description provided for @conjugaison_conditionnel_desc.
  ///
  /// In en, this message translates to:
  /// **'Conjugation of conditionals'**
  String get conjugaison_conditionnel_desc;

  /// No description provided for @adjectifs_i_desc.
  ///
  /// In en, this message translates to:
  /// **'Adjectives in -i'**
  String get adjectifs_i_desc;

  /// No description provided for @adjectifs_na_desc.
  ///
  /// In en, this message translates to:
  /// **'Adjectives ending in -na'**
  String get adjectifs_na_desc;

  /// No description provided for @noms_desc.
  ///
  /// In en, this message translates to:
  /// **'Nouns and nouns'**
  String get noms_desc;

  /// No description provided for @noms_temps_desc.
  ///
  /// In en, this message translates to:
  /// **'Names of time'**
  String get noms_temps_desc;

  /// No description provided for @noms_lieux_desc.
  ///
  /// In en, this message translates to:
  /// **'Place names'**
  String get noms_lieux_desc;

  /// No description provided for @particules_de_base_desc.
  ///
  /// In en, this message translates to:
  /// **'Basic particles (は、が、を、に、で、へ、と、から、まで、より)'**
  String get particules_de_base_desc;

  /// No description provided for @aru_iru_desc.
  ///
  /// In en, this message translates to:
  /// **'ある・いる - Expressing existence'**
  String get aru_iru_desc;

  /// No description provided for @ato_de_desc.
  ///
  /// In en, this message translates to:
  /// **'後で - After ~, after ~, after ~'**
  String get ato_de_desc;

  /// No description provided for @base_dasu_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + 出す - To start doing something'**
  String get base_dasu_desc;

  /// No description provided for @base_hajimeru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + 始める - Start at ~'**
  String get base_hajimeru_desc;

  /// No description provided for @base_ni_iku_kuru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + にいく・にくる - To go or come to do something'**
  String get base_ni_iku_kuru_desc;

  /// No description provided for @base_sugiru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + すぎる - Express excess'**
  String get base_sugiru_desc;

  /// No description provided for @chu_ju_desc.
  ///
  /// In en, this message translates to:
  /// **'中・じゅう - During ~, during ~, everywhere in ~, throughout ~'**
  String get chu_ju_desc;

  /// No description provided for @particule_de_desc.
  ///
  /// In en, this message translates to:
  /// **'で - Particle of means, method, place'**
  String get particule_de_desc;

  /// No description provided for @desho_daro_desc.
  ///
  /// In en, this message translates to:
  /// **'でしょう・だろう - Expressing probability'**
  String get desho_daro_desc;

  /// No description provided for @forme_conjonctive_i_desc.
  ///
  /// In en, this message translates to:
  /// **'Conjunctive form in -i - Verbal base'**
  String get forme_conjonctive_i_desc;

  /// No description provided for @forme_masu_desc.
  ///
  /// In en, this message translates to:
  /// **'-ます form - Polite form of a verb'**
  String get forme_masu_desc;

  /// No description provided for @forme_naide_kudasai_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -ないでください - Polite imperative'**
  String get forme_naide_kudasai_desc;

  /// No description provided for @forme_dict_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -る, -う - Neutral or dictionary form'**
  String get forme_dict_desc;

  /// No description provided for @forme_tai_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -たい - Volitional form'**
  String get forme_tai_desc;

  /// No description provided for @particule_ga_desc.
  ///
  /// In en, this message translates to:
  /// **'が - Subject particle'**
  String get particule_ga_desc;

  /// No description provided for @particule_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'は – Theme particle'**
  String get particule_ha_desc;

  /// No description provided for @ha_ikemasen_desc.
  ///
  /// In en, this message translates to:
  /// **'はいけません - Expressing prohibition or impossibility'**
  String get ha_ikemasen_desc;

  /// No description provided for @ho_ga_ii_desc.
  ///
  /// In en, this message translates to:
  /// **'方がいい - To be better than to do something'**
  String get ho_ga_ii_desc;

  /// No description provided for @particule_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'か - Question particle, enumeration'**
  String get particule_ka_desc;

  /// No description provided for @kara_made_desc.
  ///
  /// In en, this message translates to:
  /// **'から・まで - Particles marking a starting and destination point'**
  String get kara_made_desc;

  /// No description provided for @kara_node_desc.
  ///
  /// In en, this message translates to:
  /// **'から・ので - Expressing the reason or cause'**
  String get kara_node_desc;

  /// No description provided for @koto_ga_dekiru_desc.
  ///
  /// In en, this message translates to:
  /// **'ことができる - Being able to do'**
  String get koto_ga_dekiru_desc;

  /// No description provided for @mae_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'前に - Before ~'**
  String get mae_ni_desc;

  /// No description provided for @particule_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'も - ~ also, ~ no more'**
  String get particule_mo_desc;

  /// No description provided for @mo_ii_desu_desc.
  ///
  /// In en, this message translates to:
  /// **'もいいです - Ask permission'**
  String get mo_ii_desu_desc;

  /// No description provided for @superlatif_desc.
  ///
  /// In en, this message translates to:
  /// **'最も・一番・最 - The superlative'**
  String get superlatif_desc;

  /// No description provided for @mo_mada_desc.
  ///
  /// In en, this message translates to:
  /// **'もう・まだ - Already ~, don\'t ~ anymore, again ~, not yet ~'**
  String get mo_mada_desc;

  /// No description provided for @nado_desc.
  ///
  /// In en, this message translates to:
  /// **'など - etc, among other things, e.g.'**
  String get nado_desc;

  /// No description provided for @nakereba_narimasen_desc.
  ///
  /// In en, this message translates to:
  /// **'なければなりません - Expressing obligation'**
  String get nakereba_narimasen_desc;

  /// No description provided for @naru_desc.
  ///
  /// In en, this message translates to:
  /// **'なる - Become'**
  String get naru_desc;

  /// No description provided for @particule_ne_desc.
  ///
  /// In en, this message translates to:
  /// **'ね - Particle of confirmation and invitation'**
  String get particule_ne_desc;

  /// No description provided for @particule_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'に - Particle for indirect object, direction, place'**
  String get particule_ni_desc;

  /// No description provided for @particule_no_desc.
  ///
  /// In en, this message translates to:
  /// **'の - Particle of possession, interrogation'**
  String get particule_no_desc;

  /// No description provided for @no_desu_desc.
  ///
  /// In en, this message translates to:
  /// **'のです - Explanatory nuance, showing interest, mitigation'**
  String get no_desu_desc;

  /// No description provided for @particule_to_desc.
  ///
  /// In en, this message translates to:
  /// **'と - Conjunction, preposition, quotation, conditional'**
  String get particule_to_desc;

  /// No description provided for @to_iu_desc.
  ///
  /// In en, this message translates to:
  /// **'という - Called ~, named ~, named after ~, who is called ~'**
  String get to_iu_desc;

  /// No description provided for @to_omoimasu_desc.
  ///
  /// In en, this message translates to:
  /// **'と思います・どう思いますか - Give your opinion and ask for an opinion'**
  String get to_omoimasu_desc;

  /// No description provided for @tsumori_desu_desc.
  ///
  /// In en, this message translates to:
  /// **'つもりです - Expressing intention'**
  String get tsumori_desu_desc;

  /// No description provided for @ta_koto_ga_aru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ことがある – Indicate an experience'**
  String get ta_koto_ga_aru_desc;

  /// No description provided for @ta_ri_shimasu_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + りします - Series of non-exhaustive actions'**
  String get ta_ri_shimasu_desc;

  /// No description provided for @te_iru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + いる – Progressive form'**
  String get te_iru_desc;

  /// No description provided for @te_kara_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + から - After ~'**
  String get te_kara_desc;

  /// No description provided for @particule_wo_desc.
  ///
  /// In en, this message translates to:
  /// **'を - Particle to indicate the direct object, mark the COI in the causative, indicate a place crossed'**
  String get particule_wo_desc;

  /// No description provided for @particule_ya_desc.
  ///
  /// In en, this message translates to:
  /// **'や - Incomplete list of things'**
  String get particule_ya_desc;

  /// No description provided for @particule_yo_desc.
  ///
  /// In en, this message translates to:
  /// **'よ - Particle of affirmation and insistence'**
  String get particule_yo_desc;

  /// No description provided for @particule_yori_desc.
  ///
  /// In en, this message translates to:
  /// **'より - Particle to indicate the origin or designate the point of reference in a comparison'**
  String get particule_yori_desc;

  /// No description provided for @yori_no_ho_ga_desc.
  ///
  /// In en, this message translates to:
  /// **'より・の方が - The comparison'**
  String get yori_no_ho_ga_desc;

  /// No description provided for @amari_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'あまり + verb in -ない - Almost not ~, not so much ~'**
  String get amari_nai_desc;

  /// No description provided for @base_au_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + あう - Do something reciprocally'**
  String get base_au_desc;

  /// No description provided for @base_nagara_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + ながら - The gerund'**
  String get base_nagara_desc;

  /// No description provided for @base_so_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + そう - To seem, to have the air of ~'**
  String get base_so_desc;

  /// No description provided for @base_tsuzukeru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + 続ける - Continue to do something'**
  String get base_tsuzukeru_desc;

  /// No description provided for @base_yasui_nikui_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + やすい・にくい - Easy or difficult to do an action'**
  String get base_yasui_nikui_desc;

  /// No description provided for @beki_desc.
  ///
  /// In en, this message translates to:
  /// **'べき - Moral obligation, having to do something'**
  String get beki_desc;

  /// No description provided for @conditionnel_naraba_desc.
  ///
  /// In en, this message translates to:
  /// **'Conditional with ならば - Contextual conditional'**
  String get conditionnel_naraba_desc;

  /// No description provided for @conditionnel_to_desc.
  ///
  /// In en, this message translates to:
  /// **'Conditional with と - Logical conditional'**
  String get conditionnel_to_desc;

  /// No description provided for @conditionnel_eba_desc.
  ///
  /// In en, this message translates to:
  /// **'Conditional in -えば - General conditional'**
  String get conditionnel_eba_desc;

  /// No description provided for @conditionnel_tara_desc.
  ///
  /// In en, this message translates to:
  /// **'Conditional in -たら - Past conditional'**
  String get conditionnel_tara_desc;

  /// No description provided for @dake_shika_nomi_desc.
  ///
  /// In en, this message translates to:
  /// **'だけ・しか・のみ - Particles to limit something'**
  String get dake_shika_nomi_desc;

  /// No description provided for @conjecturale_to_suru_desc.
  ///
  /// In en, this message translates to:
  /// **'Conjectural form + とする - Try to do'**
  String get conjecturale_to_suru_desc;

  /// No description provided for @forme_areru_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -あれる - Passive form'**
  String get forme_areru_desc;

  /// No description provided for @forme_aseru_saseru_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -あせる・させる - Causative form'**
  String get forme_aseru_saseru_desc;

  /// No description provided for @forme_eru_rareru_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -える・られる - Potential form'**
  String get forme_eru_rareru_desc;

  /// No description provided for @forme_nasai_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -なさい - Soft imperative'**
  String get forme_nasai_desc;

  /// No description provided for @forme_saserareru_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -させられる・あせられる - Passive causative form'**
  String get forme_saserareru_desc;

  /// No description provided for @forme_zuni_naide_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -ずに・ないで - To do something without doing another'**
  String get forme_zuni_naide_desc;

  /// No description provided for @forme_conjecturale_desc.
  ///
  /// In en, this message translates to:
  /// **'Form in -おう・よう・ましょう - Conjectural form'**
  String get forme_conjecturale_desc;

  /// No description provided for @garu_desc.
  ///
  /// In en, this message translates to:
  /// **'がる - Expressing someone else\'s desire and emotions'**
  String get garu_desc;

  /// No description provided for @hazu_desc.
  ///
  /// In en, this message translates to:
  /// **'はず - To indicate that something is supposed to be done'**
  String get hazu_desc;

  /// No description provided for @hoshii_desc.
  ///
  /// In en, this message translates to:
  /// **'ほしい - Want something'**
  String get hoshii_desc;

  /// No description provided for @ka_do_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'かどうか - Whether or not'**
  String get ka_do_ka_desc;

  /// No description provided for @keigo_bases_desc.
  ///
  /// In en, this message translates to:
  /// **'Keigo – Basic forms of keigo'**
  String get keigo_bases_desc;

  /// No description provided for @ki_ni_suru_naru_desc.
  ///
  /// In en, this message translates to:
  /// **'気にする・気になる - To be preoccupied, to feel worried'**
  String get ki_ni_suru_naru_desc;

  /// No description provided for @koto_ni_suru_naru_kimeru_desc.
  ///
  /// In en, this message translates to:
  /// **'ことにする・ことになる・ことを決める - Deciding to do something'**
  String get koto_ni_suru_naru_kimeru_desc;

  /// No description provided for @ni_yotte_yoruto_yoreba_desc.
  ///
  /// In en, this message translates to:
  /// **'によって・によると・によれば・の意見では - According to ~, according to the opinion of ~'**
  String get ni_yotte_yoruto_yoreba_desc;

  /// No description provided for @noni_desc.
  ///
  /// In en, this message translates to:
  /// **'のに - Despite ~, with the aim of ~'**
  String get noni_desc;

  /// No description provided for @rashii_desc.
  ///
  /// In en, this message translates to:
  /// **'らしい - Expressing a rumor'**
  String get rashii_desc;

  /// No description provided for @shi_desc.
  ///
  /// In en, this message translates to:
  /// **'し - List states and evoke multiple reasons'**
  String get shi_desc;

  /// No description provided for @suru_adjectif_desc.
  ///
  /// In en, this message translates to:
  /// **'する - Give something back ~'**
  String get suru_adjectif_desc;

  /// No description provided for @toki_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'ときに - When ~'**
  String get toki_ni_desc;

  /// No description provided for @ta_bakari_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ばかり - Have just done something'**
  String get ta_bakari_desc;

  /// No description provided for @ta_ra_do_desu_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ら + どうですか – Offer a suggestion'**
  String get ta_ra_do_desu_ka_desc;

  /// No description provided for @te_ageru_sashiageru_yaru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + あげる・さしあげる・やる - To do something for someone'**
  String get te_ageru_sashiageru_yaru_desc;

  /// No description provided for @te_aru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + ある - Describes a state of a completed action'**
  String get te_aru_desc;

  /// No description provided for @te_bakari_iru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + ばかりいる - To only do something'**
  String get te_bakari_iru_desc;

  /// No description provided for @te_itadakemasen_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + いただけませんか – Polite request'**
  String get te_itadakemasen_ka_desc;

  /// No description provided for @te_kuremasen_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + くれませんか – Polite request'**
  String get te_kuremasen_ka_desc;

  /// No description provided for @te_kureru_kudasaru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + くれる・くださる - Someone does something for us'**
  String get te_kureru_kudasaru_desc;

  /// No description provided for @te_miru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + みる - Try to see'**
  String get te_miru_desc;

  /// No description provided for @te_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + も - Even if ~'**
  String get te_mo_desc;

  /// No description provided for @te_morau_itadaku_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + もらう・いただく – Receive an action from someone'**
  String get te_morau_itadaku_desc;

  /// No description provided for @te_oku_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + おく – Do an action shortly in preparation for'**
  String get te_oku_desc;

  /// No description provided for @te_shimau_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + しまう – To regret an action or do something completely'**
  String get te_shimau_desc;

  /// No description provided for @te_sumimasen_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + すみません - To be sorry for ~'**
  String get te_sumimasen_desc;

  /// No description provided for @te_yokatta_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + よかった - To be happy that ~'**
  String get te_yokatta_desc;

  /// No description provided for @yo_ni_suru_naru_desc.
  ///
  /// In en, this message translates to:
  /// **'ようにする・ようになる - Getting to the point of ~, getting to the point of ~'**
  String get yo_ni_suru_naru_desc;

  /// No description provided for @base_gachi_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + がち - Have a tendency to often ~'**
  String get base_gachi_desc;

  /// No description provided for @base_kireru_kirenai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + きれる・きれない - To be able or not to be able to do something completely'**
  String get base_kireru_kirenai_desc;

  /// No description provided for @base_kkonai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + っこない - Is not possible to ~, absolutely cannot ~'**
  String get base_kkonai_desc;

  /// No description provided for @base_shidai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + 次第 - Immediately after ~, as soon as ~'**
  String get base_shidai_desc;

  /// No description provided for @base_yo_ga_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + ようがない - There is no way to ~'**
  String get base_yo_ga_nai_desc;

  /// No description provided for @darake_desc.
  ///
  /// In en, this message translates to:
  /// **'だらけ - Full of ~'**
  String get darake_desc;

  /// No description provided for @dokoro_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'どころか - Far from ~, ~ actually'**
  String get dokoro_ka_desc;

  /// No description provided for @ha_motoyori_mochiron_desc.
  ///
  /// In en, this message translates to:
  /// **'はもちろん・はもちろん - Obviously ~ but also ~'**
  String get ha_motoyori_mochiron_desc;

  /// No description provided for @ippo_da_desc.
  ///
  /// In en, this message translates to:
  /// **'一方だ - Indicate that a stock is increasingly following a certain trend'**
  String get ippo_da_desc;

  /// No description provided for @kara_ni_kakete_desc.
  ///
  /// In en, this message translates to:
  /// **'から～にかけて - From ~ to ~'**
  String get kara_ni_kakete_desc;

  /// No description provided for @kawari_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'代わりに - In place of ~, instead of ~, in compensation for ~'**
  String get kawari_ni_desc;

  /// No description provided for @koso_desc.
  ///
  /// In en, this message translates to:
  /// **'こそ – Accented theme particle'**
  String get koso_desc;

  /// No description provided for @kuseni_desc.
  ///
  /// In en, this message translates to:
  /// **'くせに - Although ~, despite the fact that ~, despite ~'**
  String get kuseni_desc;

  /// No description provided for @mono_n3_desc.
  ///
  /// In en, this message translates to:
  /// **'もの - Because ~'**
  String get mono_n3_desc;

  /// No description provided for @nagara_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'ながらも - Although~'**
  String get nagara_mo_desc;

  /// No description provided for @ni_kagitte_desc.
  ///
  /// In en, this message translates to:
  /// **'に限って - Particularly when ~, only when ~'**
  String get ni_kagitte_desc;

  /// No description provided for @ni_kan_shite_desc.
  ///
  /// In en, this message translates to:
  /// **'に関して - Concerning ~, about ~'**
  String get ni_kan_shite_desc;

  /// No description provided for @ni_kawatte_kawari_desc.
  ///
  /// In en, this message translates to:
  /// **'に変わって・にかわり - In place of ~, instead of ~, in the name of ~'**
  String get ni_kawatte_kawari_desc;

  /// No description provided for @ni_kimatte_iru_desc.
  ///
  /// In en, this message translates to:
  /// **'に決まっている - Without a doubt, it is certain that ~'**
  String get ni_kimatte_iru_desc;

  /// No description provided for @ni_kurabete_desc.
  ///
  /// In en, this message translates to:
  /// **'に比べて - Compared to ~'**
  String get ni_kurabete_desc;

  /// No description provided for @ni_kuwaete_desc.
  ///
  /// In en, this message translates to:
  /// **'に加えて - In addition to ~'**
  String get ni_kuwaete_desc;

  /// No description provided for @ni_taishite_desc.
  ///
  /// In en, this message translates to:
  /// **'に対して - Against ~, against ~'**
  String get ni_taishite_desc;

  /// No description provided for @ni_totte_desc.
  ///
  /// In en, this message translates to:
  /// **'にとって - From ~\'s point of view'**
  String get ni_totte_desc;

  /// No description provided for @ni_tsuke_tsukete_tsuitemo_desc.
  ///
  /// In en, this message translates to:
  /// **'につけ・につけて・につけても - Whenever ~, whenever ~, whether ~ or ~'**
  String get ni_tsuke_tsukete_tsuitemo_desc;

  /// No description provided for @okage_de_desc.
  ///
  /// In en, this message translates to:
  /// **'おかげで - Thanks to ~'**
  String get okage_de_desc;

  /// No description provided for @sae_eb_desc.
  ///
  /// In en, this message translates to:
  /// **'さえ + verb -えば - If only ~ then ~'**
  String get sae_eb_desc;

  /// No description provided for @seide_desc.
  ///
  /// In en, this message translates to:
  /// **'せいで - Because of ~'**
  String get seide_desc;

  /// No description provided for @tabi_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'度に - Whenever ~'**
  String get tabi_ni_desc;

  /// No description provided for @tatoe_te_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'たとえ + verb -て + も - Even if ~'**
  String get tatoe_te_mo_desc;

  /// No description provided for @to_ieba_desc.
  ///
  /// In en, this message translates to:
  /// **'と言えば - Speaking of ~'**
  String get to_ieba_desc;

  /// No description provided for @to_ittara_desc.
  ///
  /// In en, this message translates to:
  /// **'と言ったら - Speaking of ~, about ~'**
  String get to_ittara_desc;

  /// No description provided for @to_iu_to_desc.
  ///
  /// In en, this message translates to:
  /// **'と言うと - Speaking of ~, about ~'**
  String get to_iu_to_desc;

  /// No description provided for @tokoro_he_ni_wo_desc.
  ///
  /// In en, this message translates to:
  /// **'ところへ・ところに・ところを - Emphasize the moment when an action is performed'**
  String get tokoro_he_ni_wo_desc;

  /// No description provided for @toori_ni_doori_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'通りに・どおりに - Like ~'**
  String get toori_ni_doori_ni_desc;

  /// No description provided for @toshite_desc.
  ///
  /// In en, this message translates to:
  /// **'として - As ~'**
  String get toshite_desc;

  /// No description provided for @tsuide_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'ついでに - Take advantage of doing something to do another'**
  String get tsuide_ni_desc;

  /// No description provided for @uchi_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'うちに - While ~, before ~'**
  String get uchi_ni_desc;

  /// No description provided for @ta_tokoro_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ところ - When ~, when ~'**
  String get ta_tokoro_desc;

  /// No description provided for @ta_totan_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + とたん - As soon as ~, as soon as ~'**
  String get ta_totan_desc;

  /// No description provided for @te_irai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + 以来 - Since ~'**
  String get te_irai_desc;

  /// No description provided for @wake_deha_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'わけではない - This does not mean that ~, not necessarily ~, not especially ~'**
  String get wake_deha_nai_desc;

  /// No description provided for @wo_chushin_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'を中心に - Centered on ~, around ~'**
  String get wo_chushin_ni_desc;

  /// No description provided for @wo_hajime_hajime_to_suru_desc.
  ///
  /// In en, this message translates to:
  /// **'をはじめ・をはじめとする - Starting with ~'**
  String get wo_hajime_hajime_to_suru_desc;

  /// No description provided for @wo_nuki_ni_shite_ha_nuki_ni_shite_desc.
  ///
  /// In en, this message translates to:
  /// **'を抜きにして・は抜きにして - Leave aside ~, do not take into account ~'**
  String get wo_nuki_ni_shite_ha_nuki_ni_shite_desc;

  /// No description provided for @yo_ni_n3_desc.
  ///
  /// In en, this message translates to:
  /// **'ように - Like ~, with the aim of ~'**
  String get yo_ni_n3_desc;

  /// No description provided for @ageku_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'あげくに - Finally, in the end'**
  String get ageku_ni_desc;

  /// No description provided for @amari_excessif_desc.
  ///
  /// In en, this message translates to:
  /// **'あまり - ~ so much that'**
  String get amari_excessif_desc;

  /// No description provided for @bakari_ka_bakari_de_naku_desc.
  ///
  /// In en, this message translates to:
  /// **'ばかりか・ばかりでなく - Not only ~ but also ~'**
  String get bakari_ka_bakari_de_naku_desc;

  /// No description provided for @base_gatai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + がたい - Difficult to ~, hard to ~'**
  String get base_gatai_desc;

  /// No description provided for @base_gimi_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + ぎみ - To have a tendency to ~, to be a little ~, a slight feeling of ~'**
  String get base_gimi_desc;

  /// No description provided for @base_kakeru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + かける - Without finishing, leaving unfinished, without finishing something'**
  String get base_kakeru_desc;

  /// No description provided for @base_kanenai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + かねない - It is feared that ~, it is possible that ~'**
  String get base_kanenai_desc;

  /// No description provided for @base_kaneru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + かねる - Not being able to do'**
  String get base_kaneru_desc;

  /// No description provided for @base_kiru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + きる – Do something completely'**
  String get base_kiru_desc;

  /// No description provided for @base_nuku_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + ぬく – Do something thoroughly until the end'**
  String get base_nuku_desc;

  /// No description provided for @base_uru_enai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + うる・えない - Be possible to ~'**
  String get base_uru_enai_desc;

  /// No description provided for @dake_quantite_desc.
  ///
  /// In en, this message translates to:
  /// **'だけ - As much as ~'**
  String get dake_quantite_desc;

  /// No description provided for @dake_atte_desc.
  ///
  /// In en, this message translates to:
  /// **'だけあって - As expected'**
  String get dake_atte_desc;

  /// No description provided for @dake_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'だけに - Because ~, as expected'**
  String get dake_ni_desc;

  /// No description provided for @dokoro_deha_nai_naku_desc.
  ///
  /// In en, this message translates to:
  /// **'どころではない・どころではなく - To be far from ~, to be out of the question of ~'**
  String get dokoro_deha_nai_naku_desc;

  /// No description provided for @conjecturale_deha_nai_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'Conjectural form + ではないか - Why not ~?, couldn\'t we ~?'**
  String get conjecturale_deha_nai_ka_desc;

  /// No description provided for @ge_desc.
  ///
  /// In en, this message translates to:
  /// **'げ - Look like ~'**
  String get ge_desc;

  /// No description provided for @ha_tomokaku_desc.
  ///
  /// In en, this message translates to:
  /// **'はともかく - Without taking into account ~, ignoring ~'**
  String get ha_tomokaku_desc;

  /// No description provided for @hanmen_desc.
  ///
  /// In en, this message translates to:
  /// **'反面 - On the other hand'**
  String get hanmen_desc;

  /// No description provided for @hodo_desc.
  ///
  /// In en, this message translates to:
  /// **'ほど - More ~, more ~'**
  String get hodo_desc;

  /// No description provided for @hoka_nai_shikata_ga_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'ほかない・ほか仕方がない - To have no choice but to ~, to have to ~'**
  String get hoka_nai_shikata_ga_nai_desc;

  /// No description provided for @igai_no_desc.
  ///
  /// In en, this message translates to:
  /// **'以外の - Other than ~, except ~'**
  String get igai_no_desc;

  /// No description provided for @ijo_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'以上は - Since ~, since ~, since ~'**
  String get ijo_ha_desc;

  /// No description provided for @ippo_ippo_deha_desc.
  ///
  /// In en, this message translates to:
  /// **'一方・一方では - Although ~, on the one hand ~ on the other hand ~'**
  String get ippo_ippo_deha_desc;

  /// No description provided for @jo_ha_mo_no_desc.
  ///
  /// In en, this message translates to:
  /// **'上は・上も・上の - From ~\'s point of view'**
  String get jo_ha_mo_no_desc;

  /// No description provided for @ka_to_omou_to_omottara_desc.
  ///
  /// In en, this message translates to:
  /// **'かと思うと・かと思ったら - Right after ~, as soon as ~'**
  String get ka_to_omou_to_omottara_desc;

  /// No description provided for @kagiri_desc.
  ///
  /// In en, this message translates to:
  /// **'限り - As long as ~, as long as ~'**
  String get kagiri_desc;

  /// No description provided for @kagiri_deha_desc.
  ///
  /// In en, this message translates to:
  /// **'限りでは - As far as ~, as far as ~'**
  String get kagiri_deha_desc;

  /// No description provided for @kanoyo_ni_na_da_desc.
  ///
  /// In en, this message translates to:
  /// **'かのようだ・かのような・かのように - Like ~'**
  String get kanoyo_ni_na_da_desc;

  /// No description provided for @kara_iu_to_ieba_itte_desc.
  ///
  /// In en, this message translates to:
  /// **'から言うと・から言えば・から言って - From ~\'s point of view'**
  String get kara_iu_to_ieba_itte_desc;

  /// No description provided for @kara_mite_mo_miru_to_mireba_desc.
  ///
  /// In en, this message translates to:
  /// **'から見ても・から見ると・から見れば - From ~\'s point of view'**
  String get kara_mite_mo_miru_to_mireba_desc;

  /// No description provided for @kara_ni_ha_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'からには・からは - Now that ~, since that ~'**
  String get kara_ni_ha_ha_desc;

  /// No description provided for @kara_suru_to_sureba_shite_desc.
  ///
  /// In en, this message translates to:
  /// **'からすると・からすれば・からして - Judging by ~, considering ~'**
  String get kara_suru_to_sureba_shite_desc;

  /// No description provided for @kara_to_itte_desc.
  ///
  /// In en, this message translates to:
  /// **'からと言って - Only because~'**
  String get kara_to_itte_desc;

  /// No description provided for @karakoso_desc.
  ///
  /// In en, this message translates to:
  /// **'からこそ - It\'s precisely because~'**
  String get karakoso_desc;

  /// No description provided for @ki_ga_suru_desc.
  ///
  /// In en, this message translates to:
  /// **'気がする - Feeling like ~'**
  String get ki_ga_suru_desc;

  /// No description provided for @kiri_desc.
  ///
  /// In en, this message translates to:
  /// **'きり - Just ~, only ~'**
  String get kiri_desc;

  /// No description provided for @kke_desc.
  ///
  /// In en, this message translates to:
  /// **'っけ - Expression to indicate confirmation or that we have just remembered something'**
  String get kke_desc;

  /// No description provided for @koto_da_recommandation_desc.
  ///
  /// In en, this message translates to:
  /// **'ことだ - Have to do something (recommendation)'**
  String get koto_da_recommandation_desc;

  /// No description provided for @koto_dakara_desc.
  ///
  /// In en, this message translates to:
  /// **'ことだから - This is typical of ~, given that ~'**
  String get koto_dakara_desc;

  /// No description provided for @koto_ha_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'ことはない - Not to be necessary ~'**
  String get koto_ha_nai_desc;

  /// No description provided for @koto_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'ことか - So much, so much'**
  String get koto_ka_desc;

  /// No description provided for @koto_kara_desc.
  ///
  /// In en, this message translates to:
  /// **'ことから - Because'**
  String get koto_kara_desc;

  /// No description provided for @koto_naku_desc.
  ///
  /// In en, this message translates to:
  /// **'ことなく - Without ~'**
  String get koto_naku_desc;

  /// No description provided for @koto_ni_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'ことには - Completely ~, really ~, to my ~'**
  String get koto_ni_ha_desc;

  /// No description provided for @kurai_gurai_hodo_desc.
  ///
  /// In en, this message translates to:
  /// **'くらい・ぐらい・ほど - To the point of ~, not as much ~ as ~, even ~, at least ~, rather than ~'**
  String get kurai_gurai_hodo_desc;

  /// No description provided for @mai_desc.
  ///
  /// In en, this message translates to:
  /// **'まい - Shouldn\'t ~, don\'t ~ probably'**
  String get mai_desc;

  /// No description provided for @mai_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'まいか - Not knowing whether or not~'**
  String get mai_ka_desc;

  /// No description provided for @mo_kamawazu_desc.
  ///
  /// In en, this message translates to:
  /// **'もかまわず - Without worrying about ~'**
  String get mo_kamawazu_desc;

  /// No description provided for @mono_da_n2_desc.
  ///
  /// In en, this message translates to:
  /// **'ものだ - Feel admiration, describe the obvious, suggest advice, express nostalgia'**
  String get mono_da_n2_desc;

  /// No description provided for @mono_dakara_desc.
  ///
  /// In en, this message translates to:
  /// **'ものだから - Because ~'**
  String get mono_dakara_desc;

  /// No description provided for @mono_ga_aru_desc.
  ///
  /// In en, this message translates to:
  /// **'ものがある - To have the impression of ~, to feel something'**
  String get mono_ga_aru_desc;

  /// No description provided for @mono_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'ものか - Not at all'**
  String get mono_ka_desc;

  /// No description provided for @mono_nara_desc.
  ///
  /// In en, this message translates to:
  /// **'ものなら - If only ~ were possible, then ~'**
  String get mono_nara_desc;

  /// No description provided for @mono_no_desc.
  ///
  /// In en, this message translates to:
  /// **'ものの - Although~'**
  String get mono_no_desc;

  /// No description provided for @muke_desc.
  ///
  /// In en, this message translates to:
  /// **'向け - Intended for ~'**
  String get muke_desc;

  /// No description provided for @muki_desc.
  ///
  /// In en, this message translates to:
  /// **'向き - To be adapted to ~'**
  String get muki_desc;

  /// No description provided for @nado_nanka_nante_desc.
  ///
  /// In en, this message translates to:
  /// **'など・なんか・なんて - Particles emphasizing something negative'**
  String get nado_nanka_nante_desc;

  /// No description provided for @nai_koto_ha_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'ないことはない - It\'s not impossible that ~, there\'s a small chance that ~, it might be that ~'**
  String get nai_koto_ha_nai_desc;

  /// No description provided for @nai_koto_ni_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'ないことには - As long as ~, if ~'**
  String get nai_koto_ni_ha_desc;

  /// No description provided for @nakanaka_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'なかなか + verb in -ない - Not being able to do something easily'**
  String get nakanaka_nai_desc;

  /// No description provided for @ni_atatte_atari_desc.
  ///
  /// In en, this message translates to:
  /// **'に当たって・に当たり - On the occasion of ~'**
  String get ni_atatte_atari_desc;

  /// No description provided for @ni_chigai_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'に違いない - Surely'**
  String get ni_chigai_nai_desc;

  /// No description provided for @ni_hanshite_desc.
  ///
  /// In en, this message translates to:
  /// **'に反して - Unlike ~'**
  String get ni_hanshite_desc;

  /// No description provided for @ni_hoka_naranai_desc.
  ///
  /// In en, this message translates to:
  /// **'にほかならない - To be nothing other than ~'**
  String get ni_hoka_naranai_desc;

  /// No description provided for @ni_kagirazu_desc.
  ///
  /// In en, this message translates to:
  /// **'に限らず - Not just for ~'**
  String get ni_kagirazu_desc;

  /// No description provided for @ni_kagiru_desc.
  ///
  /// In en, this message translates to:
  /// **'に限る - Nothing is better than ~, nothing is worth ~'**
  String get ni_kagiru_desc;

  /// No description provided for @ni_kakawarazu_kakawarinaku_desc.
  ///
  /// In en, this message translates to:
  /// **'にかかわらず・にかかわりなく - It doesn\'t matter~'**
  String get ni_kakawarazu_kakawarinaku_desc;

  /// No description provided for @ni_kakete_ha_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'にかけては・にかけても - Concerning ~, speaking of ~'**
  String get ni_kakete_ha_mo_desc;

  /// No description provided for @ni_kotaete_desc.
  ///
  /// In en, this message translates to:
  /// **'に応えて - In response to ~'**
  String get ni_kotaete_desc;

  /// No description provided for @ni_mo_kakawarazu_desc.
  ///
  /// In en, this message translates to:
  /// **'にもかかわらず - Although ~, despite the fact that ~, despite ~'**
  String get ni_mo_kakawarazu_desc;

  /// No description provided for @ni_moto_zuite_desc.
  ///
  /// In en, this message translates to:
  /// **'に基づいて - Based on ~, based on ~'**
  String get ni_moto_zuite_desc;

  /// No description provided for @ni_oite_okeru_desc.
  ///
  /// In en, this message translates to:
  /// **'において・における - In ~, during ~, at ~, in ~'**
  String get ni_oite_okeru_desc;

  /// No description provided for @ni_sai_shite_sai_shi_desc.
  ///
  /// In en, this message translates to:
  /// **'に際して・に際し - During ~, on the occasion of ~'**
  String get ni_sai_shite_sai_shi_desc;

  /// No description provided for @ni_sakidatte_desc.
  ///
  /// In en, this message translates to:
  /// **'に先立って - Before ~, before ~'**
  String get ni_sakidatte_desc;

  /// No description provided for @ni_shiro_shitemo_seyo_desc.
  ///
  /// In en, this message translates to:
  /// **'にしろ・にしても・にせよ - Even if ~, it doesn\'t matter that ~, in one case or the other'**
  String get ni_shiro_shitemo_seyo_desc;

  /// No description provided for @ni_shitagatte_desc.
  ///
  /// In en, this message translates to:
  /// **'に従って - Following ~, in accordance with ~, as ~'**
  String get ni_shitagatte_desc;

  /// No description provided for @ni_shitara_sureba_shite_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'にしたら・にすれば・にしても - Putting yourself in ~\'s place, from ~\'s point of view'**
  String get ni_shitara_sureba_shite_mo_desc;

  /// No description provided for @ni_shite_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'にしては - Although ~, despite ~'**
  String get ni_shite_ha_desc;

  /// No description provided for @ni_sotte_soi_sou_desc.
  ///
  /// In en, this message translates to:
  /// **'に沿って・に沿い・に沿う - In accordance with ~, following ~, along ~'**
  String get ni_sotte_soi_sou_desc;

  /// No description provided for @ni_suginai_desc.
  ///
  /// In en, this message translates to:
  /// **'に過ぎない - To be nothing more than ~, to be at most only ~'**
  String get ni_suginai_desc;

  /// No description provided for @ni_soi_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'に相違ない - Surely, without a doubt'**
  String get ni_soi_nai_desc;

  /// No description provided for @ni_tomonatte_desc.
  ///
  /// In en, this message translates to:
  /// **'に伴って - At the same time as ~, with ~'**
  String get ni_tomonatte_desc;

  /// No description provided for @ni_tsuite_desc.
  ///
  /// In en, this message translates to:
  /// **'について - Concerning ~, about ~'**
  String get ni_tsuite_desc;

  /// No description provided for @ni_tsuki_desc.
  ///
  /// In en, this message translates to:
  /// **'につき - Because of ~, because of ~'**
  String get ni_tsuki_desc;

  /// No description provided for @ni_tsurete_desc.
  ///
  /// In en, this message translates to:
  /// **'につれて - As ~'**
  String get ni_tsurete_desc;

  /// No description provided for @ni_wataru_watatte_desc.
  ///
  /// In en, this message translates to:
  /// **'にわたる・にわたって - During ~, throughout ~, through ~'**
  String get ni_wataru_watatte_desc;

  /// No description provided for @ni_ojite_desc.
  ///
  /// In en, this message translates to:
  /// **'に応じて - According to ~, following ~, in response to ~'**
  String get ni_ojite_desc;

  /// No description provided for @no_moto_de_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'のもとで・のもとに - Under ~, following ~'**
  String get no_moto_de_ni_desc;

  /// No description provided for @nomi_narazu_desc.
  ///
  /// In en, this message translates to:
  /// **'のみならず - Not only ~ but also ~'**
  String get nomi_narazu_desc;

  /// No description provided for @nuki_de_no_desc.
  ///
  /// In en, this message translates to:
  /// **'抜きで・抜きの - Without ~'**
  String get nuki_de_no_desc;

  /// No description provided for @osore_ga_aru_desc.
  ///
  /// In en, this message translates to:
  /// **'恐れがある - I\'m afraid that ~, it\'s unfortunately likely that ~'**
  String get osore_ga_aru_desc;

  /// No description provided for @ppoi_desc.
  ///
  /// In en, this message translates to:
  /// **'っぽい - Appear ~, resemble ~'**
  String get ppoi_desc;

  /// No description provided for @sae_karashite_desc.
  ///
  /// In en, this message translates to:
  /// **'さえ・からして - Even ~, not even ~'**
  String get sae_karashite_desc;

  /// No description provided for @sai_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'際に - When ~, when ~'**
  String get sai_ni_desc;

  /// No description provided for @saichu_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'最中に - Right in the middle of ~'**
  String get saichu_ni_desc;

  /// No description provided for @shidai_deha_da_desc.
  ///
  /// In en, this message translates to:
  /// **'次第では・次第だ - Next ~, depends on ~, the reason is that ~'**
  String get shidai_deha_da_desc;

  /// No description provided for @shikanai_desc.
  ///
  /// In en, this message translates to:
  /// **'しかない - To have no choice but to ~, to have to ~'**
  String get shikanai_desc;

  /// No description provided for @sue_ni_no_sue_desc.
  ///
  /// In en, this message translates to:
  /// **'末に・末の・の末 - After ~'**
  String get sue_ni_no_sue_desc;

  /// No description provided for @to_itte_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'と言っても - Although ~, even if it is said that ~'**
  String get to_itte_mo_desc;

  /// No description provided for @to_iu_koto_da_desc.
  ///
  /// In en, this message translates to:
  /// **'と言うことだ - I heard that ~, that means that ~'**
  String get to_iu_koto_da_desc;

  /// No description provided for @to_iu_mono_da_desc.
  ///
  /// In en, this message translates to:
  /// **'と言うものだ - I think ~, it\'s ~'**
  String get to_iu_mono_da_desc;

  /// No description provided for @to_iu_mono_deha_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'と言うものではない - This does not mean that ~, it is not necessarily ~'**
  String get to_iu_mono_deha_nai_desc;

  /// No description provided for @to_iu_yori_desc.
  ///
  /// In en, this message translates to:
  /// **'と言うより - Rather than ~'**
  String get to_iu_yori_desc;

  /// No description provided for @to_shitara_sureba_desc.
  ///
  /// In en, this message translates to:
  /// **'としたら・とすれば - Assuming ~, if ~'**
  String get to_shitara_sureba_desc;

  /// No description provided for @to_shite_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'としては - For ~'**
  String get to_shite_ha_desc;

  /// No description provided for @to_shite_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'としても - Even if ~, assuming that ~'**
  String get to_shite_mo_desc;

  /// No description provided for @to_tomo_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'とともに - With ~, at the same time as ~, simultaneously'**
  String get to_tomo_ni_desc;

  /// No description provided for @toka_desc.
  ///
  /// In en, this message translates to:
  /// **'とか - It seems that ~, I heard that ~'**
  String get toka_desc;

  /// No description provided for @tsutsu_desc.
  ///
  /// In en, this message translates to:
  /// **'つつ - Although ~, despite ~, while ~'**
  String get tsutsu_desc;

  /// No description provided for @ue_de_desc.
  ///
  /// In en, this message translates to:
  /// **'上で - After ~'**
  String get ue_de_desc;

  /// No description provided for @ue_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'上は - As long as ~, as long as ~'**
  String get ue_ha_desc;

  /// No description provided for @ue_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'上に - Not only ~ but also ~'**
  String get ue_ni_desc;

  /// No description provided for @eba_verb_neutre_hodo_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -えば + neutral verb + ほど - The more we do this, the more ~'**
  String get eba_verb_neutre_hodo_desc;

  /// No description provided for @naide_zuni_ha_irarenai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -ないで or verb -ずに + はいられない - Can\'t help but ~'**
  String get naide_zuni_ha_irarenai_desc;

  /// No description provided for @ta_kiri_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + きり - For good, once and for all'**
  String get ta_kiri_desc;

  /// No description provided for @te_hajimete_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + はじめて - Only after starting ~'**
  String get te_hajimete_desc;

  /// No description provided for @te_karade_nai_to_nakereba_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + からでないと・からでなければ - If I don\'t ~ before, ~'**
  String get te_karade_nai_to_nakereba_desc;

  /// No description provided for @te_naranai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + ならない - Not being able to hold back ~, not being able to stop ~'**
  String get te_naranai_desc;

  /// No description provided for @te_tamaranai_shoganai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + たまらない・しょうがない - Not being able to bear ~, not being able to hold back ~'**
  String get te_tamaranai_shoganai_desc;

  /// No description provided for @keigo_vocabulaire_desc.
  ///
  /// In en, this message translates to:
  /// **'Keigo Vocabulary – Words Used Formally'**
  String get keigo_vocabulaire_desc;

  /// No description provided for @wake_desu_desc.
  ///
  /// In en, this message translates to:
  /// **'わけです - It is natural that ~, it is because ~'**
  String get wake_desu_desc;

  /// No description provided for @wake_ga_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'わけがない - It is not possible to ~'**
  String get wake_ga_nai_desc;

  /// No description provided for @wake_ni_ha_ikanai_desc.
  ///
  /// In en, this message translates to:
  /// **'わけにはいかない - Not being able to afford ~'**
  String get wake_ni_ha_ikanai_desc;

  /// No description provided for @wari_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'わりに - Despite ~, despite ~'**
  String get wari_ni_desc;

  /// No description provided for @wo_keiki_ni_shite_toshite_desc.
  ///
  /// In en, this message translates to:
  /// **'を契機に・を契機として - Following ~'**
  String get wo_keiki_ni_shite_toshite_desc;

  /// No description provided for @wo_kikkake_ni_shite_toshite_desc.
  ///
  /// In en, this message translates to:
  /// **'をきっかけに・をきっかけとして - Since ~, following ~'**
  String get wo_kikkake_ni_shite_toshite_desc;

  /// No description provided for @wo_komete_desc.
  ///
  /// In en, this message translates to:
  /// **'を込めて - With everything ~'**
  String get wo_komete_desc;

  /// No description provided for @wo_megutte_desc.
  ///
  /// In en, this message translates to:
  /// **'をめぐって - About ~, around ~, concerning ~'**
  String get wo_megutte_desc;

  /// No description provided for @wo_moto_ni_shite_desc.
  ///
  /// In en, this message translates to:
  /// **'を元に・を元にして - Based on ~, from ~, based on ~'**
  String get wo_moto_ni_shite_desc;

  /// No description provided for @wo_towazu_ha_towazu_desc.
  ///
  /// In en, this message translates to:
  /// **'を問わず・は問わず - Unrelated to ~, whatsoever~'**
  String get wo_towazu_ha_towazu_desc;

  /// No description provided for @wo_tsujite_tooshite_desc.
  ///
  /// In en, this message translates to:
  /// **'を通じて・を通して - During ~, through ~, through ~'**
  String get wo_tsujite_tooshite_desc;

  /// No description provided for @zaru_wo_enai_desc.
  ///
  /// In en, this message translates to:
  /// **'ざるを得ない - To have no choice but to ~, to have to ~'**
  String get zaru_wo_enai_desc;

  /// No description provided for @ka_nai_ka_no_uchi_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'~か~ないかのうちに - Barely~'**
  String get ka_nai_ka_no_uchi_ni_desc;

  /// No description provided for @yara_yara_desc.
  ///
  /// In en, this message translates to:
  /// **'~やら~やら - Things like ~ and ~ among others'**
  String get yara_yara_desc;

  /// No description provided for @mo_eba_nara_mo_desc.
  ///
  /// In en, this message translates to:
  /// **'~も + verb -えば or nara + ~も - Not only ~ but ~ also, neither ~ nor ~'**
  String get mo_eba_nara_mo_desc;

  /// No description provided for @base_naosu_desc.
  ///
  /// In en, this message translates to:
  /// **'Verbal base + 直す - Try again'**
  String get base_naosu_desc;

  /// No description provided for @base_owaru_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb base + 終わる - End of ~'**
  String get base_owaru_desc;

  /// No description provided for @conjecturale_to_omou_desc.
  ///
  /// In en, this message translates to:
  /// **'Conjectural form + と思う - Intend to do'**
  String get conjecturale_to_omou_desc;

  /// No description provided for @goro_gurai_yaku_desc.
  ///
  /// In en, this message translates to:
  /// **'ごろ・ぐらい・約 - About ~'**
  String get goro_gurai_yaku_desc;

  /// No description provided for @hitsuyo_desc.
  ///
  /// In en, this message translates to:
  /// **'必要 - Evoking the necessary'**
  String get hitsuyo_desc;

  /// No description provided for @ichio_desc.
  ///
  /// In en, this message translates to:
  /// **'一応 - Just in case, anyway'**
  String get ichio_desc;

  /// No description provided for @nidoto_desc.
  ///
  /// In en, this message translates to:
  /// **'二度と - Never again'**
  String get nidoto_desc;

  /// No description provided for @sonna_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'そんなに - Not that much'**
  String get sonna_ni_desc;

  /// No description provided for @tada_no_tan_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'ただの・単に - A simple ~, simply ~'**
  String get tada_no_tan_ni_desc;

  /// No description provided for @to_iu_no_ha_desc.
  ///
  /// In en, this message translates to:
  /// **'というのは - Want to say or talk about something'**
  String get to_iu_no_ha_desc;

  /// No description provided for @eba_ii_noni_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -えば + いいのに - To hope or suggest something opposite to what is happening'**
  String get eba_ii_noni_desc;

  /// No description provided for @ta_ra_ii_desu_ka_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ら + いいですか - Ask for advice'**
  String get ta_ra_ii_desu_ka_desc;

  /// No description provided for @te_mo_shoganai_shikataganai_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -て + も + しょうがない・仕方がない - There is no point in doing something'**
  String get te_mo_shoganai_shikataganai_desc;

  /// No description provided for @zutsu_desc.
  ///
  /// In en, this message translates to:
  /// **'ずつ - Each ~, ~ at a time'**
  String get zutsu_desc;

  /// No description provided for @games_title.
  ///
  /// In en, this message translates to:
  /// **'Games'**
  String get games_title;

  /// No description provided for @game_taquin_title.
  ///
  /// In en, this message translates to:
  /// **'Tease'**
  String get game_taquin_title;

  /// No description provided for @game_taquin_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Put the Kanas table back in order'**
  String get game_taquin_subtitle;

  /// No description provided for @game_simon_title.
  ///
  /// In en, this message translates to:
  /// **'Simon Says'**
  String get game_simon_title;

  /// No description provided for @game_simon_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Memorize and repeat the sequence of sounds'**
  String get game_simon_subtitle;

  /// No description provided for @game_tetris_title.
  ///
  /// In en, this message translates to:
  /// **'Blocks'**
  String get game_tetris_title;

  /// No description provided for @game_tetris_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Assemble the components to form Kanji'**
  String get game_tetris_subtitle;

  /// No description provided for @game_crosswords_title.
  ///
  /// In en, this message translates to:
  /// **'Crosswords'**
  String get game_crosswords_title;

  /// No description provided for @game_crosswords_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Thematic crosswords by level JLPT'**
  String get game_crosswords_subtitle;

  /// No description provided for @game_memorize_title.
  ///
  /// In en, this message translates to:
  /// **'Memorize'**
  String get game_memorize_title;

  /// No description provided for @game_memorize_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Find the pairs of cards (Kanji, Meaning, Reading)'**
  String get game_memorize_subtitle;

  /// No description provided for @game_taquin_japanese_title.
  ///
  /// In en, this message translates to:
  /// **'スライディングパズル'**
  String get game_taquin_japanese_title;

  /// No description provided for @game_taquin_mode_label.
  ///
  /// In en, this message translates to:
  /// **'Game mode'**
  String get game_taquin_mode_label;

  /// No description provided for @game_taquin_rows_label.
  ///
  /// In en, this message translates to:
  /// **'Number of lines: {param1}'**
  String game_taquin_rows_label(Object param1);

  /// No description provided for @game_taquin_moves_label.
  ///
  /// In en, this message translates to:
  /// **'Blows'**
  String get game_taquin_moves_label;

  /// No description provided for @game_taquin_time_label.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get game_taquin_time_label;

  /// No description provided for @game_taquin_congrats.
  ///
  /// In en, this message translates to:
  /// **'Congratulations ! Table completed.'**
  String get game_taquin_congrats;

  /// No description provided for @game_taquin_back.
  ///
  /// In en, this message translates to:
  /// **'Back'**
  String get game_taquin_back;

  /// No description provided for @game_taquin_score_history_label.
  ///
  /// In en, this message translates to:
  /// **'{param1} ({param2} lines)'**
  String game_taquin_score_history_label(Object param1, Object param2);

  /// No description provided for @game_memorize_grid_size.
  ///
  /// In en, this message translates to:
  /// **'Grid size'**
  String get game_memorize_grid_size;

  /// No description provided for @game_memorize_max_strokes.
  ///
  /// In en, this message translates to:
  /// **'Max complexity (traits: {param1})'**
  String game_memorize_max_strokes(Object param1);

  /// No description provided for @game_memorize_recent_scores.
  ///
  /// In en, this message translates to:
  /// **'Latest scores'**
  String get game_memorize_recent_scores;

  /// No description provided for @game_memorize_no_scores.
  ///
  /// In en, this message translates to:
  /// **'No score yet'**
  String get game_memorize_no_scores;

  /// No description provided for @game_memorize_score_format.
  ///
  /// In en, this message translates to:
  /// **'{param1} moves'**
  String game_memorize_score_format(Object param1);

  /// No description provided for @game_memorize_time_format.
  ///
  /// In en, this message translates to:
  /// **'{param1}s'**
  String game_memorize_time_format(Object param1);

  /// No description provided for @game_memorize_grid_label.
  ///
  /// In en, this message translates to:
  /// **'{param1}'**
  String game_memorize_grid_label(Object param1);

  /// No description provided for @game_memorize_quit.
  ///
  /// In en, this message translates to:
  /// **'To leave'**
  String get game_memorize_quit;

  /// No description provided for @game_memorize_moves.
  ///
  /// In en, this message translates to:
  /// **'Moves: {param1}'**
  String game_memorize_moves(Object param1);

  /// No description provided for @game_memorize_time.
  ///
  /// In en, this message translates to:
  /// **'Time: {param1}s'**
  String game_memorize_time(Object param1);

  /// No description provided for @game_memorize_pairs_count.
  ///
  /// In en, this message translates to:
  /// **'Pairs: {param1} / {param2}'**
  String game_memorize_pairs_count(Object param1, Object param2);

  /// No description provided for @game_memorize_finished.
  ///
  /// In en, this message translates to:
  /// **'Finished !'**
  String get game_memorize_finished;

  /// No description provided for @game_memorize_congrats.
  ///
  /// In en, this message translates to:
  /// **'Well done ! Pairs found.'**
  String get game_memorize_congrats;

  /// No description provided for @game_memorize_replay.
  ///
  /// In en, this message translates to:
  /// **'Replay'**
  String get game_memorize_replay;

  /// No description provided for @game_memorize_back_menu.
  ///
  /// In en, this message translates to:
  /// **'Back to menu'**
  String get game_memorize_back_menu;

  /// No description provided for @game_simon_max_sequence.
  ///
  /// In en, this message translates to:
  /// **'Maximum sequence: {param1}'**
  String game_simon_max_sequence(Object param1);

  /// No description provided for @game_simon_game_over.
  ///
  /// In en, this message translates to:
  /// **'GAME OVER'**
  String get game_simon_game_over;

  /// No description provided for @game_simon_btn_content.
  ///
  /// In en, this message translates to:
  /// **'Button content'**
  String get game_simon_btn_content;

  /// No description provided for @game_simon_mode_kanji.
  ///
  /// In en, this message translates to:
  /// **'Kanji'**
  String get game_simon_mode_kanji;

  /// No description provided for @game_simon_mode_meaning.
  ///
  /// In en, this message translates to:
  /// **'Sense'**
  String get game_simon_mode_meaning;

  /// No description provided for @game_simon_mode_reading_std.
  ///
  /// In en, this message translates to:
  /// **'Reading (Std)'**
  String get game_simon_mode_reading_std;

  /// No description provided for @game_simon_mode_reading_rnd.
  ///
  /// In en, this message translates to:
  /// **'Reading (Rnd)'**
  String get game_simon_mode_reading_rnd;

  /// No description provided for @game_simon_record.
  ///
  /// In en, this message translates to:
  /// **'Record: {param1}'**
  String game_simon_record(Object param1);

  /// No description provided for @game_particles_title.
  ///
  /// In en, this message translates to:
  /// **'Particle Defender'**
  String get game_particles_title;

  /// No description provided for @game_particles_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Grammar shooter: find the right particle'**
  String get game_particles_subtitle;

  /// No description provided for @game_forge_title.
  ///
  /// In en, this message translates to:
  /// **'Radical Forge'**
  String get game_forge_title;

  /// No description provided for @game_forge_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Assemble the radicals to forge Kanji'**
  String get game_forge_subtitle;

  /// No description provided for @game_shiritori_title.
  ///
  /// In en, this message translates to:
  /// **'Shiritori Zen'**
  String get game_shiritori_title;

  /// No description provided for @game_shiritori_subtitle.
  ///
  /// In en, this message translates to:
  /// **'The traditional Japanese string of words'**
  String get game_shiritori_subtitle;

  /// No description provided for @game_shadow_title.
  ///
  /// In en, this message translates to:
  /// **'Shadow Kanji'**
  String get game_shadow_title;

  /// No description provided for @game_shadow_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Identify Kanji by their silhouette'**
  String get game_shadow_subtitle;

  /// No description provided for @game_config_title.
  ///
  /// In en, this message translates to:
  /// **'Configuration'**
  String get game_config_title;

  /// No description provided for @game_history_title.
  ///
  /// In en, this message translates to:
  /// **'History'**
  String get game_history_title;

  /// No description provided for @game_replay_button.
  ///
  /// In en, this message translates to:
  /// **'Replay'**
  String get game_replay_button;

  /// No description provided for @game_menu_button.
  ///
  /// In en, this message translates to:
  /// **'Menu'**
  String get game_menu_button;

  /// No description provided for @game_simon_japanese_title.
  ///
  /// In en, this message translates to:
  /// **'記憶'**
  String get game_simon_japanese_title;

  /// No description provided for @game_simon_mode_kana_same.
  ///
  /// In en, this message translates to:
  /// **'Same Syllabary'**
  String get game_simon_mode_kana_same;

  /// No description provided for @game_simon_mode_kana_cross.
  ///
  /// In en, this message translates to:
  /// **'Cross Syllabary'**
  String get game_simon_mode_kana_cross;

  /// No description provided for @game_simon_score_label.
  ///
  /// In en, this message translates to:
  /// **'Score: {param1}'**
  String game_simon_score_label(Object param1);

  /// No description provided for @game_simon_record_label.
  ///
  /// In en, this message translates to:
  /// **'Record: {param1}'**
  String game_simon_record_label(Object param1);

  /// No description provided for @game_simon_time_label.
  ///
  /// In en, this message translates to:
  /// **'Time: {param1}s'**
  String game_simon_time_label(Object param1);

  /// No description provided for @game_taquin_mode_hiragana.
  ///
  /// In en, this message translates to:
  /// **'Hiragana'**
  String get game_taquin_mode_hiragana;

  /// No description provided for @game_taquin_mode_katakana.
  ///
  /// In en, this message translates to:
  /// **'Katakana'**
  String get game_taquin_mode_katakana;

  /// No description provided for @game_taquin_mode_numbers.
  ///
  /// In en, this message translates to:
  /// **'Numbers'**
  String get game_taquin_mode_numbers;

  /// No description provided for @grammar_filter_categories.
  ///
  /// In en, this message translates to:
  /// **'Filter Categories'**
  String get grammar_filter_categories;

  /// No description provided for @grammar_no_categories.
  ///
  /// In en, this message translates to:
  /// **'No categories found.'**
  String get grammar_no_categories;

  /// No description provided for @grammar_done.
  ///
  /// In en, this message translates to:
  /// **'Done'**
  String get grammar_done;

  /// No description provided for @grammar_clear_all.
  ///
  /// In en, this message translates to:
  /// **'Clear All'**
  String get grammar_clear_all;

  /// No description provided for @grammar_pass_exam.
  ///
  /// In en, this message translates to:
  /// **'Pass Exam'**
  String get grammar_pass_exam;

  /// No description provided for @level_user_custom_list.
  ///
  /// In en, this message translates to:
  /// **'Revisions'**
  String get level_user_custom_list;

  /// No description provided for @game_writing_on.
  ///
  /// In en, this message translates to:
  /// **'ON'**
  String get game_writing_on;

  /// No description provided for @game_writing_kun.
  ///
  /// In en, this message translates to:
  /// **'KUN'**
  String get game_writing_kun;

  /// No description provided for @game_kana_link_title.
  ///
  /// In en, this message translates to:
  /// **'Kana Link'**
  String get game_kana_link_title;

  /// No description provided for @game_kana_link_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Link Kanas to form words and clear blocks'**
  String get game_kana_link_subtitle;

  /// No description provided for @game_kana_link_setup_title.
  ///
  /// In en, this message translates to:
  /// **'Kana Link Setup'**
  String get game_kana_link_setup_title;

  /// No description provided for @game_kana_link_mode_settings.
  ///
  /// In en, this message translates to:
  /// **'Mode Settings'**
  String get game_kana_link_mode_settings;

  /// No description provided for @game_kana_link_time_attack_title.
  ///
  /// In en, this message translates to:
  /// **'Time Attack'**
  String get game_kana_link_time_attack_title;

  /// No description provided for @game_kana_link_time_attack_desc.
  ///
  /// In en, this message translates to:
  /// **'60 seconds to find as many words as possible. Correct words add time!'**
  String get game_kana_link_time_attack_desc;

  /// No description provided for @game_kana_link_survival_title.
  ///
  /// In en, this message translates to:
  /// **'Survival'**
  String get game_kana_link_survival_title;

  /// No description provided for @game_kana_link_survival_desc.
  ///
  /// In en, this message translates to:
  /// **'No timer, but wrong words cost more points. See how long you can last!'**
  String get game_kana_link_survival_desc;

  /// No description provided for @game_kana_link_no_history.
  ///
  /// In en, this message translates to:
  /// **'No games played yet'**
  String get game_kana_link_no_history;

  /// No description provided for @game_kana_link_start.
  ///
  /// In en, this message translates to:
  /// **'START GAME'**
  String get game_kana_link_start;

  /// No description provided for @game_kana_link_score_label.
  ///
  /// In en, this message translates to:
  /// **'Score'**
  String get game_kana_link_score_label;

  /// No description provided for @game_kana_link_time_label.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get game_kana_link_time_label;

  /// No description provided for @game_kana_link_words_label.
  ///
  /// In en, this message translates to:
  /// **'Words'**
  String get game_kana_link_words_label;

  /// No description provided for @game_kana_link_game_over.
  ///
  /// In en, this message translates to:
  /// **'GAME OVER'**
  String get game_kana_link_game_over;

  /// No description provided for @game_kana_link_final_score.
  ///
  /// In en, this message translates to:
  /// **'Final Score'**
  String get game_kana_link_final_score;

  /// No description provided for @game_kana_link_words_found.
  ///
  /// In en, this message translates to:
  /// **'Words Found'**
  String get game_kana_link_words_found;

  /// No description provided for @game_kana_link_return_menu.
  ///
  /// In en, this message translates to:
  /// **'RETURN TO MENU'**
  String get game_kana_link_return_menu;

  /// No description provided for @game_kana_link_history_item_format.
  ///
  /// In en, this message translates to:
  /// **'{param1} words found'**
  String game_kana_link_history_item_format(Object param1);

  /// No description provided for @writing_most_frequent_words_format.
  ///
  /// In en, this message translates to:
  /// **'{param1} most frequent words'**
  String writing_most_frequent_words_format(Object param1);

  /// No description provided for @game_crossword_setup_mode.
  ///
  /// In en, this message translates to:
  /// **'Game Mode'**
  String get game_crossword_setup_mode;

  /// No description provided for @game_crossword_setup_word_count.
  ///
  /// In en, this message translates to:
  /// **'Number of words: {param1}'**
  String game_crossword_setup_word_count(Object param1);

  /// No description provided for @game_crossword_generating.
  ///
  /// In en, this message translates to:
  /// **'Generating grid...'**
  String get game_crossword_generating;

  /// No description provided for @game_crossword_congrats.
  ///
  /// In en, this message translates to:
  /// **'Congratulations!'**
  String get game_crossword_congrats;

  /// No description provided for @game_crossword_completed_in.
  ///
  /// In en, this message translates to:
  /// **'Grid completed in {param1}'**
  String game_crossword_completed_in(Object param1);

  /// No description provided for @game_crossword_back_menu.
  ///
  /// In en, this message translates to:
  /// **'Back to menu'**
  String get game_crossword_back_menu;

  /// No description provided for @game_crossword_history_item.
  ///
  /// In en, this message translates to:
  /// **'{param1} words ({param2})'**
  String game_crossword_history_item(Object param1, Object param2);

  /// No description provided for @game_snake_title.
  ///
  /// In en, this message translates to:
  /// **'Snake'**
  String get game_snake_title;

  /// No description provided for @game_snake_subtitle.
  ///
  /// In en, this message translates to:
  /// **'Learn characters by eating them in order'**
  String get game_snake_subtitle;

  /// No description provided for @game_snake_japanese_title.
  ///
  /// In en, this message translates to:
  /// **'ヘビゲーム'**
  String get game_snake_japanese_title;

  /// No description provided for @game_snake_eat_phonetics.
  ///
  /// In en, this message translates to:
  /// **'Eat phonetics in order'**
  String get game_snake_eat_phonetics;

  /// No description provided for @game_snake_score.
  ///
  /// In en, this message translates to:
  /// **'Score: {param1}'**
  String game_snake_score(Object param1);

  /// No description provided for @game_snake_words.
  ///
  /// In en, this message translates to:
  /// **'Words: {param1}'**
  String game_snake_words(Object param1);

  /// No description provided for @game_snake_game_over.
  ///
  /// In en, this message translates to:
  /// **'Game Over'**
  String get game_snake_game_over;

  /// No description provided for @game_snake_final_score.
  ///
  /// In en, this message translates to:
  /// **'Final Score'**
  String get game_snake_final_score;

  /// No description provided for @game_snake_next_format.
  ///
  /// In en, this message translates to:
  /// **'Next: {param1}'**
  String game_snake_next_format(Object param1);

  /// No description provided for @game_snake_word_format.
  ///
  /// In en, this message translates to:
  /// **'Word: {param1}'**
  String game_snake_word_format(Object param1);

  /// No description provided for @game_success.
  ///
  /// In en, this message translates to:
  /// **'Success'**
  String get game_success;

  /// No description provided for @game_over.
  ///
  /// In en, this message translates to:
  /// **'Game Over'**
  String get game_over;

  /// No description provided for @game_score_label.
  ///
  /// In en, this message translates to:
  /// **'Final Score'**
  String get game_score_label;

  /// No description provided for @game_record_label.
  ///
  /// In en, this message translates to:
  /// **'Record: {param1}'**
  String game_record_label(Object param1);

  /// No description provided for @settings_learning_mode.
  ///
  /// In en, this message translates to:
  /// **'Learning Mode'**
  String get settings_learning_mode;

  /// No description provided for @settings_tts_category.
  ///
  /// In en, this message translates to:
  /// **'Text-to-Speech'**
  String get settings_tts_category;

  /// No description provided for @settings_tts_speed.
  ///
  /// In en, this message translates to:
  /// **'Voice Speed'**
  String get settings_tts_speed;

  /// No description provided for @settings_tts_voice_selection.
  ///
  /// In en, this message translates to:
  /// **'Voice Selection'**
  String get settings_tts_voice_selection;

  /// No description provided for @settings_tts_available_voices.
  ///
  /// In en, this message translates to:
  /// **'Available Japanese Voices'**
  String get settings_tts_available_voices;

  /// No description provided for @settings_tts_voice_default.
  ///
  /// In en, this message translates to:
  /// **'Default'**
  String get settings_tts_voice_default;

  /// No description provided for @settings_tts_voice_male.
  ///
  /// In en, this message translates to:
  /// **'Male (High Quality)'**
  String get settings_tts_voice_male;

  /// No description provided for @settings_tts_voice_female.
  ///
  /// In en, this message translates to:
  /// **'Female (High Quality)'**
  String get settings_tts_voice_female;

  /// No description provided for @settings_tts_voice_forced_arabic.
  ///
  /// In en, this message translates to:
  /// **'Male (Forced for Arabic)'**
  String get settings_tts_voice_forced_arabic;

  /// No description provided for @activity_type_vocabulary.
  ///
  /// In en, this message translates to:
  /// **'Vocabulary'**
  String get activity_type_vocabulary;

  /// No description provided for @activity_type_grammar_bases.
  ///
  /// In en, this message translates to:
  /// **'Bases'**
  String get activity_type_grammar_bases;

  /// No description provided for @activity_type_grammar_verbs.
  ///
  /// In en, this message translates to:
  /// **'Verbs'**
  String get activity_type_grammar_verbs;

  /// No description provided for @activity_type_grammar_syntax.
  ///
  /// In en, this message translates to:
  /// **'Syntax'**
  String get activity_type_grammar_syntax;

  /// No description provided for @activity_type_dictionary.
  ///
  /// In en, this message translates to:
  /// **'Search Kanji and Words'**
  String get activity_type_dictionary;

  /// No description provided for @recognition_title.
  ///
  /// In en, this message translates to:
  /// **'見覚え'**
  String get recognition_title;

  /// No description provided for @writing_title.
  ///
  /// In en, this message translates to:
  /// **'書き方'**
  String get writing_title;

  /// No description provided for @reading_title.
  ///
  /// In en, this message translates to:
  /// **'読み方'**
  String get reading_title;

  /// No description provided for @in_memoriam.
  ///
  /// In en, this message translates to:
  /// **'In memory of エビ, my beloved puppy'**
  String get in_memoriam;

  /// No description provided for @reading.
  ///
  /// In en, this message translates to:
  /// **'By reading'**
  String get reading;

  /// No description provided for @meaning.
  ///
  /// In en, this message translates to:
  /// **'By meaning'**
  String get meaning;

  /// No description provided for @shiritori_rules_desc.
  ///
  /// In en, this message translates to:
  /// **'• Word must start with the last syllable of the previous word.\\n• Don\'t end with \'N\' (ん)!\\n• Words cannot be repeated.\\n• AI is limited to your current JLPT level.'**
  String get shiritori_rules_desc;

  /// No description provided for @shiritori_next.
  ///
  /// In en, this message translates to:
  /// **'Next :'**
  String get shiritori_next;

  /// No description provided for @shiritori_starts_by.
  ///
  /// In en, this message translates to:
  /// **'Shall start by'**
  String get shiritori_starts_by;

  /// No description provided for @shiritori_finishes_by.
  ///
  /// In en, this message translates to:
  /// **'Finishes by \'ん\' !'**
  String get shiritori_finishes_by;

  /// No description provided for @shiritori_taken.
  ///
  /// In en, this message translates to:
  /// **'Already taken !'**
  String get shiritori_taken;

  /// No description provided for @shiritori_unknown_word.
  ///
  /// In en, this message translates to:
  /// **'Unknown word'**
  String get shiritori_unknown_word;

  /// No description provided for @shiritori_typing.
  ///
  /// In en, this message translates to:
  /// **'Type in Romaji...'**
  String get shiritori_typing;

  /// No description provided for @shiritori_words_found.
  ///
  /// In en, this message translates to:
  /// **'Words found'**
  String get shiritori_words_found;

  /// No description provided for @shiritori_time.
  ///
  /// In en, this message translates to:
  /// **'Time'**
  String get shiritori_time;

  /// No description provided for @shiritori_ia_sink.
  ///
  /// In en, this message translates to:
  /// **'IA is thinking...'**
  String get shiritori_ia_sink;

  /// No description provided for @shiritori_rules.
  ///
  /// In en, this message translates to:
  /// **'Rules'**
  String get shiritori_rules;

  /// No description provided for @settings_downloading_pack.
  ///
  /// In en, this message translates to:
  /// **'Downloading language pack...'**
  String get settings_downloading_pack;

  /// No description provided for @settings_download_success.
  ///
  /// In en, this message translates to:
  /// **'Language pack ready!'**
  String get settings_download_success;

  /// No description provided for @settings_download_error.
  ///
  /// In en, this message translates to:
  /// **'Download failed. Check your connection.'**
  String get settings_download_error;

  /// No description provided for @settings_update_pack.
  ///
  /// In en, this message translates to:
  /// **'Update language pack'**
  String get settings_update_pack;

  /// No description provided for @numbers_desc.
  ///
  /// In en, this message translates to:
  /// **'Numbers and counters'**
  String get numbers_desc;

  /// No description provided for @forme_causatif_t_desc.
  ///
  /// In en, this message translates to:
  /// **'Causative form て - Request or favor'**
  String get forme_causatif_t_desc;

  /// No description provided for @forme_potentiel_nai_desc.
  ///
  /// In en, this message translates to:
  /// **'Potiential form ない - thing you can\'t do'**
  String get forme_potentiel_nai_desc;

  /// No description provided for @particule_he_desc.
  ///
  /// In en, this message translates to:
  /// **'へ - Particle of direction'**
  String get particule_he_desc;

  /// No description provided for @n_desu_desc.
  ///
  /// In en, this message translates to:
  /// **'のです - Explanatory nuance, showing interest, mitigation'**
  String get n_desu_desc;

  /// No description provided for @desu_masu_desc.
  ///
  /// In en, this message translates to:
  /// **'です/ます - Politeness'**
  String get desu_masu_desc;

  /// No description provided for @bakari_desc.
  ///
  /// In en, this message translates to:
  /// **'Verb -た + ばかり - Have just done something'**
  String get bakari_desc;

  /// No description provided for @fukushi_desc.
  ///
  /// In en, this message translates to:
  /// **'ふくし - Adverbs~'**
  String get fukushi_desc;

  /// No description provided for @tame_ni_desc.
  ///
  /// In en, this message translates to:
  /// **'~ため　に - Motivation ~'**
  String get tame_ni_desc;

  /// No description provided for @notification_channel_name.
  ///
  /// In en, this message translates to:
  /// **'Mochi Learning Reminders'**
  String get notification_channel_name;

  /// No description provided for @notification_channel_description.
  ///
  /// In en, this message translates to:
  /// **'Reminders to keep your Mochi fresh!'**
  String get notification_channel_description;

  /// No description provided for @notification_title.
  ///
  /// In en, this message translates to:
  /// **'Nihongo Mochi'**
  String get notification_title;

  /// No description provided for @notification_msg_1.
  ///
  /// In en, this message translates to:
  /// **'Don\'t let your Mochi dry out! 🍡 Time for a quick review?'**
  String get notification_msg_1;

  /// No description provided for @notification_msg_2.
  ///
  /// In en, this message translates to:
  /// **'Your Kanji missed you! Come back to refresh your memory! ✨'**
  String get notification_msg_2;

  /// No description provided for @notification_msg_3.
  ///
  /// In en, this message translates to:
  /// **'A fresh Mochi is a happy Mochi! Let\'s practice! 🍵'**
  String get notification_msg_3;

  /// No description provided for @notification_msg_4.
  ///
  /// In en, this message translates to:
  /// **'Mochi-Mochi! It\'s time to stretch your brain! 🧠'**
  String get notification_msg_4;

  /// No description provided for @kanji_readings.
  ///
  /// In en, this message translates to:
  /// **'Readings'**
  String get kanji_readings;

  /// No description provided for @kanji_meanings.
  ///
  /// In en, this message translates to:
  /// **'Meanings'**
  String get kanji_meanings;

  /// No description provided for @onboarding_next.
  ///
  /// In en, this message translates to:
  /// **'Next'**
  String get onboarding_next;

  /// No description provided for @onboarding_finish.
  ///
  /// In en, this message translates to:
  /// **'Finish'**
  String get onboarding_finish;

  /// No description provided for @onboarding_choose_language.
  ///
  /// In en, this message translates to:
  /// **'Choose your language'**
  String get onboarding_choose_language;

  /// No description provided for @onboarding_mode_label.
  ///
  /// In en, this message translates to:
  /// **'Mode'**
  String get onboarding_mode_label;

  /// No description provided for @onboarding_mode_description.
  ///
  /// In en, this message translates to:
  /// **'This determines the levels available (JLPT levels, School grades, etc.)'**
  String get onboarding_mode_description;

  /// No description provided for @onboarding_ready_title.
  ///
  /// In en, this message translates to:
  /// **'Ready to start!'**
  String get onboarding_ready_title;

  /// No description provided for @onboarding_ready_description.
  ///
  /// In en, this message translates to:
  /// **'Use the slider on the home screen to change levels and start practicing!'**
  String get onboarding_ready_description;

  /// No description provided for @onboarding_slider_hint.
  ///
  /// In en, this message translates to:
  /// **'Slide to change level'**
  String get onboarding_slider_hint;

  /// No description provided for @exit_dialog_title.
  ///
  /// In en, this message translates to:
  /// **'Game Paused'**
  String get exit_dialog_title;

  /// No description provided for @exit_dialog_message.
  ///
  /// In en, this message translates to:
  /// **'Are you sure you want to quit?'**
  String get exit_dialog_message;

  /// No description provided for @exit_dialog_lose_progress.
  ///
  /// In en, this message translates to:
  /// **'Progression of this session will be lost.'**
  String get exit_dialog_lose_progress;

  /// No description provided for @exit_dialog_resume.
  ///
  /// In en, this message translates to:
  /// **'Resume Game'**
  String get exit_dialog_resume;

  /// No description provided for @exit_dialog_pause_exit.
  ///
  /// In en, this message translates to:
  /// **'Pause and Quit'**
  String get exit_dialog_pause_exit;

  /// No description provided for @exit_dialog_quit_lose_progress.
  ///
  /// In en, this message translates to:
  /// **'Quit and lose progression'**
  String get exit_dialog_quit_lose_progress;

  /// No description provided for @game_particles_how_to_play.
  ///
  /// In en, this message translates to:
  /// **'How to play'**
  String get game_particles_how_to_play;

  /// No description provided for @game_particles_rules_list.
  ///
  /// In en, this message translates to:
  /// **'• Drag your ship to move\\n• Tap to shoot the correct particle\\n• Don\'t let the correct particle reach the bottom!'**
  String get game_particles_rules_list;

  /// No description provided for @game_result_title_session.
  ///
  /// In en, this message translates to:
  /// **'Session Mastery'**
  String get game_result_title_session;

  /// No description provided for @game_result_title_global.
  ///
  /// In en, this message translates to:
  /// **'Global Mastery'**
  String get game_result_title_global;

  /// No description provided for @game_result_lot_mastery.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery'**
  String get game_result_lot_mastery;

  /// No description provided for @game_result_lot_mastery_recognition.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery (Recognition)'**
  String get game_result_lot_mastery_recognition;

  /// No description provided for @game_result_lot_mastery_reading.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery (Reading)'**
  String get game_result_lot_mastery_reading;

  /// No description provided for @game_result_lot_mastery_writing.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery (Writing)'**
  String get game_result_lot_mastery_writing;

  /// No description provided for @game_result_lot_mastery_grammar.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery (Grammar)'**
  String get game_result_lot_mastery_grammar;

  /// No description provided for @game_result_lot_mastery_kana.
  ///
  /// In en, this message translates to:
  /// **'Set Mastery (Kana)'**
  String get game_result_lot_mastery_kana;

  /// No description provided for @previous_page.
  ///
  /// In en, this message translates to:
  /// **'Previous page'**
  String get previous_page;

  /// No description provided for @next_page.
  ///
  /// In en, this message translates to:
  /// **'Next page'**
  String get next_page;

  /// No description provided for @size.
  ///
  /// In en, this message translates to:
  /// **'Size'**
  String get size;

  /// No description provided for @game_recap_sort_default.
  ///
  /// In en, this message translates to:
  /// **'Default'**
  String get game_recap_sort_default;

  /// No description provided for @game_recap_sort_frequency.
  ///
  /// In en, this message translates to:
  /// **'Frequency'**
  String get game_recap_sort_frequency;

  /// No description provided for @order_by.
  ///
  /// In en, this message translates to:
  /// **'Sort by'**
  String get order_by;

  /// No description provided for @game_recap_sort_strokes.
  ///
  /// In en, this message translates to:
  /// **'Strokes number'**
  String get game_recap_sort_strokes;

  /// No description provided for @tutorial_skip.
  ///
  /// In en, this message translates to:
  /// **'Skip'**
  String get tutorial_skip;

  /// No description provided for @tutorial_next.
  ///
  /// In en, this message translates to:
  /// **'Next'**
  String get tutorial_next;

  /// No description provided for @tutorial_finish.
  ///
  /// In en, this message translates to:
  /// **'Got it!'**
  String get tutorial_finish;

  /// No description provided for @tutorial_recap_welcome.
  ///
  /// In en, this message translates to:
  /// **'Welcome to the Kanji recognition area!'**
  String get tutorial_recap_welcome;

  /// No description provided for @tutorial_recap_grid.
  ///
  /// In en, this message translates to:
  /// **'Here you can see all the Kanji in this level.\\nTheir colors indicate your mastery.\\nClick on a Kanji to get more detailled view.'**
  String get tutorial_recap_grid;

  /// No description provided for @tutorial_recap_modes.
  ///
  /// In en, this message translates to:
  /// **'Switch between Meaning and Reading modes here.\\nIncrease the challenge by selecting more frequent or random reading propositions'**
  String get tutorial_recap_modes;

  /// No description provided for @tutorial_recap_filter.
  ///
  /// In en, this message translates to:
  /// **'Customize your quiz experience by selecting sorting type and length'**
  String get tutorial_recap_filter;

  /// No description provided for @tutorial_recap_play.
  ///
  /// In en, this message translates to:
  /// **'Press Play to start a quiz on these Kanji.\\nAll errors will be automatically added on the Review for the next time.'**
  String get tutorial_recap_play;

  /// No description provided for @game_result_errors.
  ///
  /// In en, this message translates to:
  /// **'Errors:'**
  String get game_result_errors;

  /// No description provided for @tutorial_kana_quiz_topbar.
  ///
  /// In en, this message translates to:
  /// **'You will have to answer correctly to 10 questions before move forward on the next block of 10.\\nEach Kana will be asked on both way symbol to pronounciation and pronounciation to symbol'**
  String get tutorial_kana_quiz_topbar;

  /// No description provided for @tutorial_kana_quiz_buttons.
  ///
  /// In en, this message translates to:
  /// **'Select the correct answer here, only one is correct.'**
  String get tutorial_kana_quiz_buttons;

  /// No description provided for @tutorial_kanji_detail_card.
  ///
  /// In en, this message translates to:
  /// **'This is the main Kanji character. Notice the font: it shows you the correct stroke order!'**
  String get tutorial_kanji_detail_card;

  /// No description provided for @tutorial_kanji_detail_star.
  ///
  /// In en, this message translates to:
  /// **'Click the star to add this Kanji to your personalized Revision List.'**
  String get tutorial_kanji_detail_star;

  /// No description provided for @tutorial_kanji_detail_readings.
  ///
  /// In en, this message translates to:
  /// **'Readings are divided into ON (Chinese origin, usually in Katakana) and KUN (Japanese origin, in Hiragana).'**
  String get tutorial_kanji_detail_readings;

  /// No description provided for @tutorial_kanji_detail_components.
  ///
  /// In en, this message translates to:
  /// **'This graph shows how the Kanji is built from simpler radicals. You can click them to explore!'**
  String get tutorial_kanji_detail_components;

  /// No description provided for @tutorial_home_welcome.
  ///
  /// In en, this message translates to:
  /// **'Welcome to Nihongo Mochi! Let\'s take a quick tour.'**
  String get tutorial_home_welcome;

  /// No description provided for @tutorial_home_level.
  ///
  /// In en, this message translates to:
  /// **'Use this slider to change your current level (JLPT, School grades, etc.).'**
  String get tutorial_home_level;

  /// No description provided for @tutorial_home_vocabulary.
  ///
  /// In en, this message translates to:
  /// **'Practice Kanji recognition, reading, and writing here.'**
  String get tutorial_home_vocabulary;

  /// No description provided for @tutorial_home_grammar.
  ///
  /// In en, this message translates to:
  /// **'Learn Japanese grammar rules and verb conjugations.'**
  String get tutorial_home_grammar;

  /// No description provided for @tutorial_home_utilities.
  ///
  /// In en, this message translates to:
  /// **'Access games, dictionary, and your learning results here.'**
  String get tutorial_home_utilities;

  /// No description provided for @settings_audio_volume.
  ///
  /// In en, this message translates to:
  /// **'Audio effects volume'**
  String get settings_audio_volume;
}

class _AppLocalizationsDelegate
    extends LocalizationsDelegate<AppLocalizations> {
  const _AppLocalizationsDelegate();

  @override
  Future<AppLocalizations> load(Locale locale) {
    return SynchronousFuture<AppLocalizations>(lookupAppLocalizations(locale));
  }

  @override
  bool isSupported(Locale locale) => <String>[
    'ar',
    'bn',
    'de',
    'en',
    'es',
    'fr',
    'id',
    'it',
    'ja',
    'ko',
    'mn',
    'pt',
    'ru',
    'th',
    'uk',
    'vi',
    'zh',
  ].contains(locale.languageCode);

  @override
  bool shouldReload(_AppLocalizationsDelegate old) => false;
}

AppLocalizations lookupAppLocalizations(Locale locale) {
  // Lookup logic when only language code is specified.
  switch (locale.languageCode) {
    case 'ar':
      return AppLocalizationsAr();
    case 'bn':
      return AppLocalizationsBn();
    case 'de':
      return AppLocalizationsDe();
    case 'en':
      return AppLocalizationsEn();
    case 'es':
      return AppLocalizationsEs();
    case 'fr':
      return AppLocalizationsFr();
    case 'id':
      return AppLocalizationsId();
    case 'it':
      return AppLocalizationsIt();
    case 'ja':
      return AppLocalizationsJa();
    case 'ko':
      return AppLocalizationsKo();
    case 'mn':
      return AppLocalizationsMn();
    case 'pt':
      return AppLocalizationsPt();
    case 'ru':
      return AppLocalizationsRu();
    case 'th':
      return AppLocalizationsTh();
    case 'uk':
      return AppLocalizationsUk();
    case 'vi':
      return AppLocalizationsVi();
    case 'zh':
      return AppLocalizationsZh();
  }

  throw FlutterError(
    'AppLocalizations.delegate failed to load unsupported locale "$locale". This is likely '
    'an issue with the localizations generation tool. Please file an issue '
    'on GitHub with a reproducible sample app and the gen-l10n configuration '
    'that was used.',
  );
}
