
enum StatisticsType { recognition, reading, writing, grammar, games }

enum SagaTab { jlpt, school, challenges }

class SagaNode {
  final String id;
  final String title;
  final String? recognitionId;
  final String? readingId;
  final String? writingId;
  final String? grammarId;
  final StatisticsType mainType;

  SagaNode({
    required this.id,
    required this.title,
    this.recognitionId,
    this.readingId,
    this.writingId,
    this.grammarId,
    this.mainType = StatisticsType.recognition,
  });
}

class SagaStep {
  final String id;
  final List<SagaNode> nodes;

  SagaStep({required this.id, required this.nodes});
}

class UserSagaProgress {
  final int recognitionIndex;
  final int readingIndex;
  final int writingIndex;
  final int grammarIndex;
  final Map<String, int> nodeProgress;

  UserSagaProgress({
    this.recognitionIndex = 0,
    this.readingIndex = 0,
    this.writingIndex = 0,
    this.grammarIndex = 0,
    this.nodeProgress = const {},
  });
}
