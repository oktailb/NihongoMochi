import 'package:json_annotation/json_annotation.dart';

part 'grammar_quiz.g.dart';

enum ExerciseType {
  @JsonValue('FILL_BLANK')
  fillBlank,
  @JsonValue('SENTENCE_ORDER')
  sentenceOrder,
  @JsonValue('UNDERLINE_READING')
  underlineReading,
  @JsonValue('UNDERLINE_WRITING')
  underlineWriting,
  @JsonValue('PARAPHRASE')
  paraphrase,
  @JsonValue('WORD_USAGE')
  wordUsage
}

@JsonSerializable()
class Exercise {
  final String id;
  final ExerciseType type;
  final List<String> tags;
  final Map<String, dynamic> payload;

  Exercise({
    required this.id,
    required this.type,
    required this.tags,
    required this.payload,
  });

  factory Exercise.fromJson(Map<String, dynamic> json) => _$ExerciseFromJson(json);
  Map<String, dynamic> toJson() => _$ExerciseToJson(this);
}

// Payloads
class ExercisePayload {}

class FillBlankPayload extends ExercisePayload {
  final String sentence;
  final String correct;
  final List<String> distractors;
  FillBlankPayload({required this.sentence, required this.correct, required this.distractors});
}

class SentenceOrderPayload extends ExercisePayload {
  final String prefix;
  final String suffix;
  final List<String> blocks;
  SentenceOrderPayload({this.prefix = "", this.suffix = "", required this.blocks});
}

class UnderlinePayload extends ExercisePayload {
  final String sentence;
  final String correct;
  final List<String> distractors;
  UnderlinePayload({required this.sentence, required this.correct, required this.distractors});
}

class UsageOption {
  final String text;
  final bool isCorrect;
  UsageOption({required this.text, required this.isCorrect});
}

class WordUsagePayload extends ExercisePayload {
  final String word;
  final List<UsageOption> options;
  WordUsagePayload({required this.word, required this.options});
}
