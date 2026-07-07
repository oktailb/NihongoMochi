import 'dart:io';
import 'package:drift/drift.dart';
import 'package:drift/native.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;

part 'database.g.dart';

class LearningScoreEntities extends Table {
  TextColumn get key => text()();
  TextColumn get type => text()();
  IntColumn get successes => integer().withDefault(const Constant(0))();
  IntColumn get failures => integer().withDefault(const Constant(0))();
  IntColumn get lastReviewDate => integer().withDefault(const Constant(0))();

  @override
  Set<Column> get primaryKey => {key, type};
}

class UserLists extends Table {
  TextColumn get listName => text()();
  TextColumn get itemKey => text()();

  @override
  Set<Column> get primaryKey => {listName, itemKey};
}

class GameHistories extends Table {
  IntColumn get id => integer().autoIncrement()();
  TextColumn get gameType => text()();
  IntColumn get score => integer()();
  IntColumn get moves => integer().nullable()();
  IntColumn get timeSeconds => integer().nullable()();
  IntColumn get maxSequence => integer().nullable()();
  IntColumn get totalPairs => integer().nullable()();
  IntColumn get rows => integer().nullable()();
  IntColumn get wordsFound => integer().nullable()();
  IntColumn get timestamp => integer()();
  TextColumn get metadata => text().nullable()();
}

@DriftDatabase(tables: [LearningScoreEntities, UserLists, GameHistories])
class MochiDatabase extends _$MochiDatabase {
  MochiDatabase() : super(_openConnection());

  @override
  int get schemaVersion => 1;
}

LazyDatabase _openConnection() {
  return LazyDatabase(() async {
    final dbFolder = await getApplicationDocumentsDirectory();
    // Utiliser le même nom que la version Android native pour faciliter la migration si besoin
    final file = File(p.join(dbFolder.path, 'mochi.db'));
    return NativeDatabase(file);
  });
}
