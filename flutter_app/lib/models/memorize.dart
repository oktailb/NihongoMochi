import 'package:json_annotation/json_annotation.dart';

part 'memorize.g.dart';

@JsonSerializable()
class MemorizePlayable {
  final String id;
  final String character;

  MemorizePlayable({required this.id, required this.character});

  factory MemorizePlayable.fromJson(Map<String, dynamic> json) => _$MemorizePlayableFromJson(json);
  Map<String, dynamic> toJson() => _$MemorizePlayableToJson(this);
}

@JsonSerializable()
class MemorizeCardState {
  final int id;
  final MemorizePlayable item;
  final bool isFaceUp;
  final bool isMatched;

  MemorizeCardState({
    required this.id,
    required this.item,
    this.isFaceUp = false,
    this.isMatched = false,
  });

  MemorizeCardState copyWith({bool? isFaceUp, bool? isMatched}) {
    return MemorizeCardState(
      id: id,
      item: item,
      isFaceUp: isFaceUp ?? this.isFaceUp,
      isMatched: isMatched ?? this.isMatched,
    );
  }

  factory MemorizeCardState.fromJson(Map<String, dynamic> json) => _$MemorizeCardStateFromJson(json);
  Map<String, dynamic> toJson() => _$MemorizeCardStateToJson(this);
}

@JsonSerializable()
class MemorizeGridSize {
  final int rows;
  final int cols;

  MemorizeGridSize({required this.rows, required this.cols});

  int get totalCards => rows * cols;
  int get pairsCount => totalCards ~/ 2;

  @override
  String toString() => '${cols}x$rows';

  factory MemorizeGridSize.fromJson(Map<String, dynamic> json) => _$MemorizeGridSizeFromJson(json);
  Map<String, dynamic> toJson() => _$MemorizeGridSizeToJson(this);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is MemorizeGridSize && runtimeType == other.runtimeType && rows == other.rows && cols == other.cols;

  @override
  int get hashCode => rows.hashCode ^ cols.hashCode;
}

@JsonSerializable()
class MemorizeGameResult {
  final int moves;
  final int totalPairs;
  final String gridSizeLabel;
  final int timeSeconds;
  final int timestamp;

  MemorizeGameResult({
    required this.moves,
    required this.totalPairs,
    required this.gridSizeLabel,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory MemorizeGameResult.fromJson(Map<String, dynamic> json) => _$MemorizeGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$MemorizeGameResultToJson(this);
}
