// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'database.dart';

// ignore_for_file: type=lint
class $LearningScoreEntitiesTable extends LearningScoreEntities
    with TableInfo<$LearningScoreEntitiesTable, LearningScoreEntity> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $LearningScoreEntitiesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _keyMeta = const VerificationMeta('key');
  @override
  late final GeneratedColumn<String> key = GeneratedColumn<String>(
    'key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _typeMeta = const VerificationMeta('type');
  @override
  late final GeneratedColumn<String> type = GeneratedColumn<String>(
    'type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _successesMeta = const VerificationMeta(
    'successes',
  );
  @override
  late final GeneratedColumn<int> successes = GeneratedColumn<int>(
    'successes',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultValue: const Constant(0),
  );
  static const VerificationMeta _failuresMeta = const VerificationMeta(
    'failures',
  );
  @override
  late final GeneratedColumn<int> failures = GeneratedColumn<int>(
    'failures',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultValue: const Constant(0),
  );
  static const VerificationMeta _lastReviewDateMeta = const VerificationMeta(
    'lastReviewDate',
  );
  @override
  late final GeneratedColumn<int> lastReviewDate = GeneratedColumn<int>(
    'last_review_date',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultValue: const Constant(0),
  );
  @override
  List<GeneratedColumn> get $columns => [
    key,
    type,
    successes,
    failures,
    lastReviewDate,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'learning_score_entities';
  @override
  VerificationContext validateIntegrity(
    Insertable<LearningScoreEntity> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('key')) {
      context.handle(
        _keyMeta,
        key.isAcceptableOrUnknown(data['key']!, _keyMeta),
      );
    } else if (isInserting) {
      context.missing(_keyMeta);
    }
    if (data.containsKey('type')) {
      context.handle(
        _typeMeta,
        type.isAcceptableOrUnknown(data['type']!, _typeMeta),
      );
    } else if (isInserting) {
      context.missing(_typeMeta);
    }
    if (data.containsKey('successes')) {
      context.handle(
        _successesMeta,
        successes.isAcceptableOrUnknown(data['successes']!, _successesMeta),
      );
    }
    if (data.containsKey('failures')) {
      context.handle(
        _failuresMeta,
        failures.isAcceptableOrUnknown(data['failures']!, _failuresMeta),
      );
    }
    if (data.containsKey('last_review_date')) {
      context.handle(
        _lastReviewDateMeta,
        lastReviewDate.isAcceptableOrUnknown(
          data['last_review_date']!,
          _lastReviewDateMeta,
        ),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {key, type};
  @override
  LearningScoreEntity map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return LearningScoreEntity(
      key: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}key'],
      )!,
      type: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}type'],
      )!,
      successes: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}successes'],
      )!,
      failures: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}failures'],
      )!,
      lastReviewDate: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}last_review_date'],
      )!,
    );
  }

  @override
  $LearningScoreEntitiesTable createAlias(String alias) {
    return $LearningScoreEntitiesTable(attachedDatabase, alias);
  }
}

class LearningScoreEntity extends DataClass
    implements Insertable<LearningScoreEntity> {
  final String key;
  final String type;
  final int successes;
  final int failures;
  final int lastReviewDate;
  const LearningScoreEntity({
    required this.key,
    required this.type,
    required this.successes,
    required this.failures,
    required this.lastReviewDate,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['key'] = Variable<String>(key);
    map['type'] = Variable<String>(type);
    map['successes'] = Variable<int>(successes);
    map['failures'] = Variable<int>(failures);
    map['last_review_date'] = Variable<int>(lastReviewDate);
    return map;
  }

  LearningScoreEntitiesCompanion toCompanion(bool nullToAbsent) {
    return LearningScoreEntitiesCompanion(
      key: Value(key),
      type: Value(type),
      successes: Value(successes),
      failures: Value(failures),
      lastReviewDate: Value(lastReviewDate),
    );
  }

  factory LearningScoreEntity.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return LearningScoreEntity(
      key: serializer.fromJson<String>(json['key']),
      type: serializer.fromJson<String>(json['type']),
      successes: serializer.fromJson<int>(json['successes']),
      failures: serializer.fromJson<int>(json['failures']),
      lastReviewDate: serializer.fromJson<int>(json['lastReviewDate']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'key': serializer.toJson<String>(key),
      'type': serializer.toJson<String>(type),
      'successes': serializer.toJson<int>(successes),
      'failures': serializer.toJson<int>(failures),
      'lastReviewDate': serializer.toJson<int>(lastReviewDate),
    };
  }

  LearningScoreEntity copyWith({
    String? key,
    String? type,
    int? successes,
    int? failures,
    int? lastReviewDate,
  }) => LearningScoreEntity(
    key: key ?? this.key,
    type: type ?? this.type,
    successes: successes ?? this.successes,
    failures: failures ?? this.failures,
    lastReviewDate: lastReviewDate ?? this.lastReviewDate,
  );
  LearningScoreEntity copyWithCompanion(LearningScoreEntitiesCompanion data) {
    return LearningScoreEntity(
      key: data.key.present ? data.key.value : this.key,
      type: data.type.present ? data.type.value : this.type,
      successes: data.successes.present ? data.successes.value : this.successes,
      failures: data.failures.present ? data.failures.value : this.failures,
      lastReviewDate: data.lastReviewDate.present
          ? data.lastReviewDate.value
          : this.lastReviewDate,
    );
  }

  @override
  String toString() {
    return (StringBuffer('LearningScoreEntity(')
          ..write('key: $key, ')
          ..write('type: $type, ')
          ..write('successes: $successes, ')
          ..write('failures: $failures, ')
          ..write('lastReviewDate: $lastReviewDate')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode =>
      Object.hash(key, type, successes, failures, lastReviewDate);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is LearningScoreEntity &&
          other.key == this.key &&
          other.type == this.type &&
          other.successes == this.successes &&
          other.failures == this.failures &&
          other.lastReviewDate == this.lastReviewDate);
}

class LearningScoreEntitiesCompanion
    extends UpdateCompanion<LearningScoreEntity> {
  final Value<String> key;
  final Value<String> type;
  final Value<int> successes;
  final Value<int> failures;
  final Value<int> lastReviewDate;
  final Value<int> rowid;
  const LearningScoreEntitiesCompanion({
    this.key = const Value.absent(),
    this.type = const Value.absent(),
    this.successes = const Value.absent(),
    this.failures = const Value.absent(),
    this.lastReviewDate = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  LearningScoreEntitiesCompanion.insert({
    required String key,
    required String type,
    this.successes = const Value.absent(),
    this.failures = const Value.absent(),
    this.lastReviewDate = const Value.absent(),
    this.rowid = const Value.absent(),
  }) : key = Value(key),
       type = Value(type);
  static Insertable<LearningScoreEntity> custom({
    Expression<String>? key,
    Expression<String>? type,
    Expression<int>? successes,
    Expression<int>? failures,
    Expression<int>? lastReviewDate,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (key != null) 'key': key,
      if (type != null) 'type': type,
      if (successes != null) 'successes': successes,
      if (failures != null) 'failures': failures,
      if (lastReviewDate != null) 'last_review_date': lastReviewDate,
      if (rowid != null) 'rowid': rowid,
    });
  }

  LearningScoreEntitiesCompanion copyWith({
    Value<String>? key,
    Value<String>? type,
    Value<int>? successes,
    Value<int>? failures,
    Value<int>? lastReviewDate,
    Value<int>? rowid,
  }) {
    return LearningScoreEntitiesCompanion(
      key: key ?? this.key,
      type: type ?? this.type,
      successes: successes ?? this.successes,
      failures: failures ?? this.failures,
      lastReviewDate: lastReviewDate ?? this.lastReviewDate,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (key.present) {
      map['key'] = Variable<String>(key.value);
    }
    if (type.present) {
      map['type'] = Variable<String>(type.value);
    }
    if (successes.present) {
      map['successes'] = Variable<int>(successes.value);
    }
    if (failures.present) {
      map['failures'] = Variable<int>(failures.value);
    }
    if (lastReviewDate.present) {
      map['last_review_date'] = Variable<int>(lastReviewDate.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('LearningScoreEntitiesCompanion(')
          ..write('key: $key, ')
          ..write('type: $type, ')
          ..write('successes: $successes, ')
          ..write('failures: $failures, ')
          ..write('lastReviewDate: $lastReviewDate, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $UserListsTable extends UserLists
    with TableInfo<$UserListsTable, UserList> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $UserListsTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _listNameMeta = const VerificationMeta(
    'listName',
  );
  @override
  late final GeneratedColumn<String> listName = GeneratedColumn<String>(
    'list_name',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _itemKeyMeta = const VerificationMeta(
    'itemKey',
  );
  @override
  late final GeneratedColumn<String> itemKey = GeneratedColumn<String>(
    'item_key',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  @override
  List<GeneratedColumn> get $columns => [listName, itemKey];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'user_lists';
  @override
  VerificationContext validateIntegrity(
    Insertable<UserList> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('list_name')) {
      context.handle(
        _listNameMeta,
        listName.isAcceptableOrUnknown(data['list_name']!, _listNameMeta),
      );
    } else if (isInserting) {
      context.missing(_listNameMeta);
    }
    if (data.containsKey('item_key')) {
      context.handle(
        _itemKeyMeta,
        itemKey.isAcceptableOrUnknown(data['item_key']!, _itemKeyMeta),
      );
    } else if (isInserting) {
      context.missing(_itemKeyMeta);
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {listName, itemKey};
  @override
  UserList map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return UserList(
      listName: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}list_name'],
      )!,
      itemKey: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}item_key'],
      )!,
    );
  }

  @override
  $UserListsTable createAlias(String alias) {
    return $UserListsTable(attachedDatabase, alias);
  }
}

class UserList extends DataClass implements Insertable<UserList> {
  final String listName;
  final String itemKey;
  const UserList({required this.listName, required this.itemKey});
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['list_name'] = Variable<String>(listName);
    map['item_key'] = Variable<String>(itemKey);
    return map;
  }

  UserListsCompanion toCompanion(bool nullToAbsent) {
    return UserListsCompanion(
      listName: Value(listName),
      itemKey: Value(itemKey),
    );
  }

  factory UserList.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return UserList(
      listName: serializer.fromJson<String>(json['listName']),
      itemKey: serializer.fromJson<String>(json['itemKey']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'listName': serializer.toJson<String>(listName),
      'itemKey': serializer.toJson<String>(itemKey),
    };
  }

  UserList copyWith({String? listName, String? itemKey}) => UserList(
    listName: listName ?? this.listName,
    itemKey: itemKey ?? this.itemKey,
  );
  UserList copyWithCompanion(UserListsCompanion data) {
    return UserList(
      listName: data.listName.present ? data.listName.value : this.listName,
      itemKey: data.itemKey.present ? data.itemKey.value : this.itemKey,
    );
  }

  @override
  String toString() {
    return (StringBuffer('UserList(')
          ..write('listName: $listName, ')
          ..write('itemKey: $itemKey')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(listName, itemKey);
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is UserList &&
          other.listName == this.listName &&
          other.itemKey == this.itemKey);
}

class UserListsCompanion extends UpdateCompanion<UserList> {
  final Value<String> listName;
  final Value<String> itemKey;
  final Value<int> rowid;
  const UserListsCompanion({
    this.listName = const Value.absent(),
    this.itemKey = const Value.absent(),
    this.rowid = const Value.absent(),
  });
  UserListsCompanion.insert({
    required String listName,
    required String itemKey,
    this.rowid = const Value.absent(),
  }) : listName = Value(listName),
       itemKey = Value(itemKey);
  static Insertable<UserList> custom({
    Expression<String>? listName,
    Expression<String>? itemKey,
    Expression<int>? rowid,
  }) {
    return RawValuesInsertable({
      if (listName != null) 'list_name': listName,
      if (itemKey != null) 'item_key': itemKey,
      if (rowid != null) 'rowid': rowid,
    });
  }

  UserListsCompanion copyWith({
    Value<String>? listName,
    Value<String>? itemKey,
    Value<int>? rowid,
  }) {
    return UserListsCompanion(
      listName: listName ?? this.listName,
      itemKey: itemKey ?? this.itemKey,
      rowid: rowid ?? this.rowid,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (listName.present) {
      map['list_name'] = Variable<String>(listName.value);
    }
    if (itemKey.present) {
      map['item_key'] = Variable<String>(itemKey.value);
    }
    if (rowid.present) {
      map['rowid'] = Variable<int>(rowid.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('UserListsCompanion(')
          ..write('listName: $listName, ')
          ..write('itemKey: $itemKey, ')
          ..write('rowid: $rowid')
          ..write(')'))
        .toString();
  }
}

class $GameHistoriesTable extends GameHistories
    with TableInfo<$GameHistoriesTable, GameHistory> {
  @override
  final GeneratedDatabase attachedDatabase;
  final String? _alias;
  $GameHistoriesTable(this.attachedDatabase, [this._alias]);
  static const VerificationMeta _idMeta = const VerificationMeta('id');
  @override
  late final GeneratedColumn<int> id = GeneratedColumn<int>(
    'id',
    aliasedName,
    false,
    hasAutoIncrement: true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
    defaultConstraints: GeneratedColumn.constraintIsAlways(
      'PRIMARY KEY AUTOINCREMENT',
    ),
  );
  static const VerificationMeta _gameTypeMeta = const VerificationMeta(
    'gameType',
  );
  @override
  late final GeneratedColumn<String> gameType = GeneratedColumn<String>(
    'game_type',
    aliasedName,
    false,
    type: DriftSqlType.string,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _scoreMeta = const VerificationMeta('score');
  @override
  late final GeneratedColumn<int> score = GeneratedColumn<int>(
    'score',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _movesMeta = const VerificationMeta('moves');
  @override
  late final GeneratedColumn<int> moves = GeneratedColumn<int>(
    'moves',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _timeSecondsMeta = const VerificationMeta(
    'timeSeconds',
  );
  @override
  late final GeneratedColumn<int> timeSeconds = GeneratedColumn<int>(
    'time_seconds',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _maxSequenceMeta = const VerificationMeta(
    'maxSequence',
  );
  @override
  late final GeneratedColumn<int> maxSequence = GeneratedColumn<int>(
    'max_sequence',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _totalPairsMeta = const VerificationMeta(
    'totalPairs',
  );
  @override
  late final GeneratedColumn<int> totalPairs = GeneratedColumn<int>(
    'total_pairs',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _rowsMeta = const VerificationMeta('rows');
  @override
  late final GeneratedColumn<int> rows = GeneratedColumn<int>(
    'rows',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _wordsFoundMeta = const VerificationMeta(
    'wordsFound',
  );
  @override
  late final GeneratedColumn<int> wordsFound = GeneratedColumn<int>(
    'words_found',
    aliasedName,
    true,
    type: DriftSqlType.int,
    requiredDuringInsert: false,
  );
  static const VerificationMeta _timestampMeta = const VerificationMeta(
    'timestamp',
  );
  @override
  late final GeneratedColumn<int> timestamp = GeneratedColumn<int>(
    'timestamp',
    aliasedName,
    false,
    type: DriftSqlType.int,
    requiredDuringInsert: true,
  );
  static const VerificationMeta _metadataMeta = const VerificationMeta(
    'metadata',
  );
  @override
  late final GeneratedColumn<String> metadata = GeneratedColumn<String>(
    'metadata',
    aliasedName,
    true,
    type: DriftSqlType.string,
    requiredDuringInsert: false,
  );
  @override
  List<GeneratedColumn> get $columns => [
    id,
    gameType,
    score,
    moves,
    timeSeconds,
    maxSequence,
    totalPairs,
    rows,
    wordsFound,
    timestamp,
    metadata,
  ];
  @override
  String get aliasedName => _alias ?? actualTableName;
  @override
  String get actualTableName => $name;
  static const String $name = 'game_histories';
  @override
  VerificationContext validateIntegrity(
    Insertable<GameHistory> instance, {
    bool isInserting = false,
  }) {
    final context = VerificationContext();
    final data = instance.toColumns(true);
    if (data.containsKey('id')) {
      context.handle(_idMeta, id.isAcceptableOrUnknown(data['id']!, _idMeta));
    }
    if (data.containsKey('game_type')) {
      context.handle(
        _gameTypeMeta,
        gameType.isAcceptableOrUnknown(data['game_type']!, _gameTypeMeta),
      );
    } else if (isInserting) {
      context.missing(_gameTypeMeta);
    }
    if (data.containsKey('score')) {
      context.handle(
        _scoreMeta,
        score.isAcceptableOrUnknown(data['score']!, _scoreMeta),
      );
    } else if (isInserting) {
      context.missing(_scoreMeta);
    }
    if (data.containsKey('moves')) {
      context.handle(
        _movesMeta,
        moves.isAcceptableOrUnknown(data['moves']!, _movesMeta),
      );
    }
    if (data.containsKey('time_seconds')) {
      context.handle(
        _timeSecondsMeta,
        timeSeconds.isAcceptableOrUnknown(
          data['time_seconds']!,
          _timeSecondsMeta,
        ),
      );
    }
    if (data.containsKey('max_sequence')) {
      context.handle(
        _maxSequenceMeta,
        maxSequence.isAcceptableOrUnknown(
          data['max_sequence']!,
          _maxSequenceMeta,
        ),
      );
    }
    if (data.containsKey('total_pairs')) {
      context.handle(
        _totalPairsMeta,
        totalPairs.isAcceptableOrUnknown(data['total_pairs']!, _totalPairsMeta),
      );
    }
    if (data.containsKey('rows')) {
      context.handle(
        _rowsMeta,
        rows.isAcceptableOrUnknown(data['rows']!, _rowsMeta),
      );
    }
    if (data.containsKey('words_found')) {
      context.handle(
        _wordsFoundMeta,
        wordsFound.isAcceptableOrUnknown(data['words_found']!, _wordsFoundMeta),
      );
    }
    if (data.containsKey('timestamp')) {
      context.handle(
        _timestampMeta,
        timestamp.isAcceptableOrUnknown(data['timestamp']!, _timestampMeta),
      );
    } else if (isInserting) {
      context.missing(_timestampMeta);
    }
    if (data.containsKey('metadata')) {
      context.handle(
        _metadataMeta,
        metadata.isAcceptableOrUnknown(data['metadata']!, _metadataMeta),
      );
    }
    return context;
  }

  @override
  Set<GeneratedColumn> get $primaryKey => {id};
  @override
  GameHistory map(Map<String, dynamic> data, {String? tablePrefix}) {
    final effectivePrefix = tablePrefix != null ? '$tablePrefix.' : '';
    return GameHistory(
      id: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}id'],
      )!,
      gameType: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}game_type'],
      )!,
      score: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}score'],
      )!,
      moves: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}moves'],
      ),
      timeSeconds: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}time_seconds'],
      ),
      maxSequence: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}max_sequence'],
      ),
      totalPairs: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}total_pairs'],
      ),
      rows: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}rows'],
      ),
      wordsFound: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}words_found'],
      ),
      timestamp: attachedDatabase.typeMapping.read(
        DriftSqlType.int,
        data['${effectivePrefix}timestamp'],
      )!,
      metadata: attachedDatabase.typeMapping.read(
        DriftSqlType.string,
        data['${effectivePrefix}metadata'],
      ),
    );
  }

  @override
  $GameHistoriesTable createAlias(String alias) {
    return $GameHistoriesTable(attachedDatabase, alias);
  }
}

class GameHistory extends DataClass implements Insertable<GameHistory> {
  final int id;
  final String gameType;
  final int score;
  final int? moves;
  final int? timeSeconds;
  final int? maxSequence;
  final int? totalPairs;
  final int? rows;
  final int? wordsFound;
  final int timestamp;
  final String? metadata;
  const GameHistory({
    required this.id,
    required this.gameType,
    required this.score,
    this.moves,
    this.timeSeconds,
    this.maxSequence,
    this.totalPairs,
    this.rows,
    this.wordsFound,
    required this.timestamp,
    this.metadata,
  });
  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    map['id'] = Variable<int>(id);
    map['game_type'] = Variable<String>(gameType);
    map['score'] = Variable<int>(score);
    if (!nullToAbsent || moves != null) {
      map['moves'] = Variable<int>(moves);
    }
    if (!nullToAbsent || timeSeconds != null) {
      map['time_seconds'] = Variable<int>(timeSeconds);
    }
    if (!nullToAbsent || maxSequence != null) {
      map['max_sequence'] = Variable<int>(maxSequence);
    }
    if (!nullToAbsent || totalPairs != null) {
      map['total_pairs'] = Variable<int>(totalPairs);
    }
    if (!nullToAbsent || rows != null) {
      map['rows'] = Variable<int>(rows);
    }
    if (!nullToAbsent || wordsFound != null) {
      map['words_found'] = Variable<int>(wordsFound);
    }
    map['timestamp'] = Variable<int>(timestamp);
    if (!nullToAbsent || metadata != null) {
      map['metadata'] = Variable<String>(metadata);
    }
    return map;
  }

  GameHistoriesCompanion toCompanion(bool nullToAbsent) {
    return GameHistoriesCompanion(
      id: Value(id),
      gameType: Value(gameType),
      score: Value(score),
      moves: moves == null && nullToAbsent
          ? const Value.absent()
          : Value(moves),
      timeSeconds: timeSeconds == null && nullToAbsent
          ? const Value.absent()
          : Value(timeSeconds),
      maxSequence: maxSequence == null && nullToAbsent
          ? const Value.absent()
          : Value(maxSequence),
      totalPairs: totalPairs == null && nullToAbsent
          ? const Value.absent()
          : Value(totalPairs),
      rows: rows == null && nullToAbsent ? const Value.absent() : Value(rows),
      wordsFound: wordsFound == null && nullToAbsent
          ? const Value.absent()
          : Value(wordsFound),
      timestamp: Value(timestamp),
      metadata: metadata == null && nullToAbsent
          ? const Value.absent()
          : Value(metadata),
    );
  }

  factory GameHistory.fromJson(
    Map<String, dynamic> json, {
    ValueSerializer? serializer,
  }) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return GameHistory(
      id: serializer.fromJson<int>(json['id']),
      gameType: serializer.fromJson<String>(json['gameType']),
      score: serializer.fromJson<int>(json['score']),
      moves: serializer.fromJson<int?>(json['moves']),
      timeSeconds: serializer.fromJson<int?>(json['timeSeconds']),
      maxSequence: serializer.fromJson<int?>(json['maxSequence']),
      totalPairs: serializer.fromJson<int?>(json['totalPairs']),
      rows: serializer.fromJson<int?>(json['rows']),
      wordsFound: serializer.fromJson<int?>(json['wordsFound']),
      timestamp: serializer.fromJson<int>(json['timestamp']),
      metadata: serializer.fromJson<String?>(json['metadata']),
    );
  }
  @override
  Map<String, dynamic> toJson({ValueSerializer? serializer}) {
    serializer ??= driftRuntimeOptions.defaultSerializer;
    return <String, dynamic>{
      'id': serializer.toJson<int>(id),
      'gameType': serializer.toJson<String>(gameType),
      'score': serializer.toJson<int>(score),
      'moves': serializer.toJson<int?>(moves),
      'timeSeconds': serializer.toJson<int?>(timeSeconds),
      'maxSequence': serializer.toJson<int?>(maxSequence),
      'totalPairs': serializer.toJson<int?>(totalPairs),
      'rows': serializer.toJson<int?>(rows),
      'wordsFound': serializer.toJson<int?>(wordsFound),
      'timestamp': serializer.toJson<int>(timestamp),
      'metadata': serializer.toJson<String?>(metadata),
    };
  }

  GameHistory copyWith({
    int? id,
    String? gameType,
    int? score,
    Value<int?> moves = const Value.absent(),
    Value<int?> timeSeconds = const Value.absent(),
    Value<int?> maxSequence = const Value.absent(),
    Value<int?> totalPairs = const Value.absent(),
    Value<int?> rows = const Value.absent(),
    Value<int?> wordsFound = const Value.absent(),
    int? timestamp,
    Value<String?> metadata = const Value.absent(),
  }) => GameHistory(
    id: id ?? this.id,
    gameType: gameType ?? this.gameType,
    score: score ?? this.score,
    moves: moves.present ? moves.value : this.moves,
    timeSeconds: timeSeconds.present ? timeSeconds.value : this.timeSeconds,
    maxSequence: maxSequence.present ? maxSequence.value : this.maxSequence,
    totalPairs: totalPairs.present ? totalPairs.value : this.totalPairs,
    rows: rows.present ? rows.value : this.rows,
    wordsFound: wordsFound.present ? wordsFound.value : this.wordsFound,
    timestamp: timestamp ?? this.timestamp,
    metadata: metadata.present ? metadata.value : this.metadata,
  );
  GameHistory copyWithCompanion(GameHistoriesCompanion data) {
    return GameHistory(
      id: data.id.present ? data.id.value : this.id,
      gameType: data.gameType.present ? data.gameType.value : this.gameType,
      score: data.score.present ? data.score.value : this.score,
      moves: data.moves.present ? data.moves.value : this.moves,
      timeSeconds: data.timeSeconds.present
          ? data.timeSeconds.value
          : this.timeSeconds,
      maxSequence: data.maxSequence.present
          ? data.maxSequence.value
          : this.maxSequence,
      totalPairs: data.totalPairs.present
          ? data.totalPairs.value
          : this.totalPairs,
      rows: data.rows.present ? data.rows.value : this.rows,
      wordsFound: data.wordsFound.present
          ? data.wordsFound.value
          : this.wordsFound,
      timestamp: data.timestamp.present ? data.timestamp.value : this.timestamp,
      metadata: data.metadata.present ? data.metadata.value : this.metadata,
    );
  }

  @override
  String toString() {
    return (StringBuffer('GameHistory(')
          ..write('id: $id, ')
          ..write('gameType: $gameType, ')
          ..write('score: $score, ')
          ..write('moves: $moves, ')
          ..write('timeSeconds: $timeSeconds, ')
          ..write('maxSequence: $maxSequence, ')
          ..write('totalPairs: $totalPairs, ')
          ..write('rows: $rows, ')
          ..write('wordsFound: $wordsFound, ')
          ..write('timestamp: $timestamp, ')
          ..write('metadata: $metadata')
          ..write(')'))
        .toString();
  }

  @override
  int get hashCode => Object.hash(
    id,
    gameType,
    score,
    moves,
    timeSeconds,
    maxSequence,
    totalPairs,
    rows,
    wordsFound,
    timestamp,
    metadata,
  );
  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      (other is GameHistory &&
          other.id == this.id &&
          other.gameType == this.gameType &&
          other.score == this.score &&
          other.moves == this.moves &&
          other.timeSeconds == this.timeSeconds &&
          other.maxSequence == this.maxSequence &&
          other.totalPairs == this.totalPairs &&
          other.rows == this.rows &&
          other.wordsFound == this.wordsFound &&
          other.timestamp == this.timestamp &&
          other.metadata == this.metadata);
}

class GameHistoriesCompanion extends UpdateCompanion<GameHistory> {
  final Value<int> id;
  final Value<String> gameType;
  final Value<int> score;
  final Value<int?> moves;
  final Value<int?> timeSeconds;
  final Value<int?> maxSequence;
  final Value<int?> totalPairs;
  final Value<int?> rows;
  final Value<int?> wordsFound;
  final Value<int> timestamp;
  final Value<String?> metadata;
  const GameHistoriesCompanion({
    this.id = const Value.absent(),
    this.gameType = const Value.absent(),
    this.score = const Value.absent(),
    this.moves = const Value.absent(),
    this.timeSeconds = const Value.absent(),
    this.maxSequence = const Value.absent(),
    this.totalPairs = const Value.absent(),
    this.rows = const Value.absent(),
    this.wordsFound = const Value.absent(),
    this.timestamp = const Value.absent(),
    this.metadata = const Value.absent(),
  });
  GameHistoriesCompanion.insert({
    this.id = const Value.absent(),
    required String gameType,
    required int score,
    this.moves = const Value.absent(),
    this.timeSeconds = const Value.absent(),
    this.maxSequence = const Value.absent(),
    this.totalPairs = const Value.absent(),
    this.rows = const Value.absent(),
    this.wordsFound = const Value.absent(),
    required int timestamp,
    this.metadata = const Value.absent(),
  }) : gameType = Value(gameType),
       score = Value(score),
       timestamp = Value(timestamp);
  static Insertable<GameHistory> custom({
    Expression<int>? id,
    Expression<String>? gameType,
    Expression<int>? score,
    Expression<int>? moves,
    Expression<int>? timeSeconds,
    Expression<int>? maxSequence,
    Expression<int>? totalPairs,
    Expression<int>? rows,
    Expression<int>? wordsFound,
    Expression<int>? timestamp,
    Expression<String>? metadata,
  }) {
    return RawValuesInsertable({
      if (id != null) 'id': id,
      if (gameType != null) 'game_type': gameType,
      if (score != null) 'score': score,
      if (moves != null) 'moves': moves,
      if (timeSeconds != null) 'time_seconds': timeSeconds,
      if (maxSequence != null) 'max_sequence': maxSequence,
      if (totalPairs != null) 'total_pairs': totalPairs,
      if (rows != null) 'rows': rows,
      if (wordsFound != null) 'words_found': wordsFound,
      if (timestamp != null) 'timestamp': timestamp,
      if (metadata != null) 'metadata': metadata,
    });
  }

  GameHistoriesCompanion copyWith({
    Value<int>? id,
    Value<String>? gameType,
    Value<int>? score,
    Value<int?>? moves,
    Value<int?>? timeSeconds,
    Value<int?>? maxSequence,
    Value<int?>? totalPairs,
    Value<int?>? rows,
    Value<int?>? wordsFound,
    Value<int>? timestamp,
    Value<String?>? metadata,
  }) {
    return GameHistoriesCompanion(
      id: id ?? this.id,
      gameType: gameType ?? this.gameType,
      score: score ?? this.score,
      moves: moves ?? this.moves,
      timeSeconds: timeSeconds ?? this.timeSeconds,
      maxSequence: maxSequence ?? this.maxSequence,
      totalPairs: totalPairs ?? this.totalPairs,
      rows: rows ?? this.rows,
      wordsFound: wordsFound ?? this.wordsFound,
      timestamp: timestamp ?? this.timestamp,
      metadata: metadata ?? this.metadata,
    );
  }

  @override
  Map<String, Expression> toColumns(bool nullToAbsent) {
    final map = <String, Expression>{};
    if (id.present) {
      map['id'] = Variable<int>(id.value);
    }
    if (gameType.present) {
      map['game_type'] = Variable<String>(gameType.value);
    }
    if (score.present) {
      map['score'] = Variable<int>(score.value);
    }
    if (moves.present) {
      map['moves'] = Variable<int>(moves.value);
    }
    if (timeSeconds.present) {
      map['time_seconds'] = Variable<int>(timeSeconds.value);
    }
    if (maxSequence.present) {
      map['max_sequence'] = Variable<int>(maxSequence.value);
    }
    if (totalPairs.present) {
      map['total_pairs'] = Variable<int>(totalPairs.value);
    }
    if (rows.present) {
      map['rows'] = Variable<int>(rows.value);
    }
    if (wordsFound.present) {
      map['words_found'] = Variable<int>(wordsFound.value);
    }
    if (timestamp.present) {
      map['timestamp'] = Variable<int>(timestamp.value);
    }
    if (metadata.present) {
      map['metadata'] = Variable<String>(metadata.value);
    }
    return map;
  }

  @override
  String toString() {
    return (StringBuffer('GameHistoriesCompanion(')
          ..write('id: $id, ')
          ..write('gameType: $gameType, ')
          ..write('score: $score, ')
          ..write('moves: $moves, ')
          ..write('timeSeconds: $timeSeconds, ')
          ..write('maxSequence: $maxSequence, ')
          ..write('totalPairs: $totalPairs, ')
          ..write('rows: $rows, ')
          ..write('wordsFound: $wordsFound, ')
          ..write('timestamp: $timestamp, ')
          ..write('metadata: $metadata')
          ..write(')'))
        .toString();
  }
}

abstract class _$MochiDatabase extends GeneratedDatabase {
  _$MochiDatabase(QueryExecutor e) : super(e);
  $MochiDatabaseManager get managers => $MochiDatabaseManager(this);
  late final $LearningScoreEntitiesTable learningScoreEntities =
      $LearningScoreEntitiesTable(this);
  late final $UserListsTable userLists = $UserListsTable(this);
  late final $GameHistoriesTable gameHistories = $GameHistoriesTable(this);
  @override
  Iterable<TableInfo<Table, Object?>> get allTables =>
      allSchemaEntities.whereType<TableInfo<Table, Object?>>();
  @override
  List<DatabaseSchemaEntity> get allSchemaEntities => [
    learningScoreEntities,
    userLists,
    gameHistories,
  ];
}

typedef $$LearningScoreEntitiesTableCreateCompanionBuilder =
    LearningScoreEntitiesCompanion Function({
      required String key,
      required String type,
      Value<int> successes,
      Value<int> failures,
      Value<int> lastReviewDate,
      Value<int> rowid,
    });
typedef $$LearningScoreEntitiesTableUpdateCompanionBuilder =
    LearningScoreEntitiesCompanion Function({
      Value<String> key,
      Value<String> type,
      Value<int> successes,
      Value<int> failures,
      Value<int> lastReviewDate,
      Value<int> rowid,
    });

class $$LearningScoreEntitiesTableFilterComposer
    extends Composer<_$MochiDatabase, $LearningScoreEntitiesTable> {
  $$LearningScoreEntitiesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get key => $composableBuilder(
    column: $table.key,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get successes => $composableBuilder(
    column: $table.successes,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get failures => $composableBuilder(
    column: $table.failures,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get lastReviewDate => $composableBuilder(
    column: $table.lastReviewDate,
    builder: (column) => ColumnFilters(column),
  );
}

class $$LearningScoreEntitiesTableOrderingComposer
    extends Composer<_$MochiDatabase, $LearningScoreEntitiesTable> {
  $$LearningScoreEntitiesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get key => $composableBuilder(
    column: $table.key,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get type => $composableBuilder(
    column: $table.type,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get successes => $composableBuilder(
    column: $table.successes,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get failures => $composableBuilder(
    column: $table.failures,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get lastReviewDate => $composableBuilder(
    column: $table.lastReviewDate,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$LearningScoreEntitiesTableAnnotationComposer
    extends Composer<_$MochiDatabase, $LearningScoreEntitiesTable> {
  $$LearningScoreEntitiesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get key =>
      $composableBuilder(column: $table.key, builder: (column) => column);

  GeneratedColumn<String> get type =>
      $composableBuilder(column: $table.type, builder: (column) => column);

  GeneratedColumn<int> get successes =>
      $composableBuilder(column: $table.successes, builder: (column) => column);

  GeneratedColumn<int> get failures =>
      $composableBuilder(column: $table.failures, builder: (column) => column);

  GeneratedColumn<int> get lastReviewDate => $composableBuilder(
    column: $table.lastReviewDate,
    builder: (column) => column,
  );
}

class $$LearningScoreEntitiesTableTableManager
    extends
        RootTableManager<
          _$MochiDatabase,
          $LearningScoreEntitiesTable,
          LearningScoreEntity,
          $$LearningScoreEntitiesTableFilterComposer,
          $$LearningScoreEntitiesTableOrderingComposer,
          $$LearningScoreEntitiesTableAnnotationComposer,
          $$LearningScoreEntitiesTableCreateCompanionBuilder,
          $$LearningScoreEntitiesTableUpdateCompanionBuilder,
          (
            LearningScoreEntity,
            BaseReferences<
              _$MochiDatabase,
              $LearningScoreEntitiesTable,
              LearningScoreEntity
            >,
          ),
          LearningScoreEntity,
          PrefetchHooks Function()
        > {
  $$LearningScoreEntitiesTableTableManager(
    _$MochiDatabase db,
    $LearningScoreEntitiesTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$LearningScoreEntitiesTableFilterComposer(
                $db: db,
                $table: table,
              ),
          createOrderingComposer: () =>
              $$LearningScoreEntitiesTableOrderingComposer(
                $db: db,
                $table: table,
              ),
          createComputedFieldComposer: () =>
              $$LearningScoreEntitiesTableAnnotationComposer(
                $db: db,
                $table: table,
              ),
          updateCompanionCallback:
              ({
                Value<String> key = const Value.absent(),
                Value<String> type = const Value.absent(),
                Value<int> successes = const Value.absent(),
                Value<int> failures = const Value.absent(),
                Value<int> lastReviewDate = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LearningScoreEntitiesCompanion(
                key: key,
                type: type,
                successes: successes,
                failures: failures,
                lastReviewDate: lastReviewDate,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String key,
                required String type,
                Value<int> successes = const Value.absent(),
                Value<int> failures = const Value.absent(),
                Value<int> lastReviewDate = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => LearningScoreEntitiesCompanion.insert(
                key: key,
                type: type,
                successes: successes,
                failures: failures,
                lastReviewDate: lastReviewDate,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$LearningScoreEntitiesTableProcessedTableManager =
    ProcessedTableManager<
      _$MochiDatabase,
      $LearningScoreEntitiesTable,
      LearningScoreEntity,
      $$LearningScoreEntitiesTableFilterComposer,
      $$LearningScoreEntitiesTableOrderingComposer,
      $$LearningScoreEntitiesTableAnnotationComposer,
      $$LearningScoreEntitiesTableCreateCompanionBuilder,
      $$LearningScoreEntitiesTableUpdateCompanionBuilder,
      (
        LearningScoreEntity,
        BaseReferences<
          _$MochiDatabase,
          $LearningScoreEntitiesTable,
          LearningScoreEntity
        >,
      ),
      LearningScoreEntity,
      PrefetchHooks Function()
    >;
typedef $$UserListsTableCreateCompanionBuilder =
    UserListsCompanion Function({
      required String listName,
      required String itemKey,
      Value<int> rowid,
    });
typedef $$UserListsTableUpdateCompanionBuilder =
    UserListsCompanion Function({
      Value<String> listName,
      Value<String> itemKey,
      Value<int> rowid,
    });

class $$UserListsTableFilterComposer
    extends Composer<_$MochiDatabase, $UserListsTable> {
  $$UserListsTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<String> get listName => $composableBuilder(
    column: $table.listName,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get itemKey => $composableBuilder(
    column: $table.itemKey,
    builder: (column) => ColumnFilters(column),
  );
}

class $$UserListsTableOrderingComposer
    extends Composer<_$MochiDatabase, $UserListsTable> {
  $$UserListsTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<String> get listName => $composableBuilder(
    column: $table.listName,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get itemKey => $composableBuilder(
    column: $table.itemKey,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$UserListsTableAnnotationComposer
    extends Composer<_$MochiDatabase, $UserListsTable> {
  $$UserListsTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<String> get listName =>
      $composableBuilder(column: $table.listName, builder: (column) => column);

  GeneratedColumn<String> get itemKey =>
      $composableBuilder(column: $table.itemKey, builder: (column) => column);
}

class $$UserListsTableTableManager
    extends
        RootTableManager<
          _$MochiDatabase,
          $UserListsTable,
          UserList,
          $$UserListsTableFilterComposer,
          $$UserListsTableOrderingComposer,
          $$UserListsTableAnnotationComposer,
          $$UserListsTableCreateCompanionBuilder,
          $$UserListsTableUpdateCompanionBuilder,
          (
            UserList,
            BaseReferences<_$MochiDatabase, $UserListsTable, UserList>,
          ),
          UserList,
          PrefetchHooks Function()
        > {
  $$UserListsTableTableManager(_$MochiDatabase db, $UserListsTable table)
    : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$UserListsTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$UserListsTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$UserListsTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<String> listName = const Value.absent(),
                Value<String> itemKey = const Value.absent(),
                Value<int> rowid = const Value.absent(),
              }) => UserListsCompanion(
                listName: listName,
                itemKey: itemKey,
                rowid: rowid,
              ),
          createCompanionCallback:
              ({
                required String listName,
                required String itemKey,
                Value<int> rowid = const Value.absent(),
              }) => UserListsCompanion.insert(
                listName: listName,
                itemKey: itemKey,
                rowid: rowid,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$UserListsTableProcessedTableManager =
    ProcessedTableManager<
      _$MochiDatabase,
      $UserListsTable,
      UserList,
      $$UserListsTableFilterComposer,
      $$UserListsTableOrderingComposer,
      $$UserListsTableAnnotationComposer,
      $$UserListsTableCreateCompanionBuilder,
      $$UserListsTableUpdateCompanionBuilder,
      (UserList, BaseReferences<_$MochiDatabase, $UserListsTable, UserList>),
      UserList,
      PrefetchHooks Function()
    >;
typedef $$GameHistoriesTableCreateCompanionBuilder =
    GameHistoriesCompanion Function({
      Value<int> id,
      required String gameType,
      required int score,
      Value<int?> moves,
      Value<int?> timeSeconds,
      Value<int?> maxSequence,
      Value<int?> totalPairs,
      Value<int?> rows,
      Value<int?> wordsFound,
      required int timestamp,
      Value<String?> metadata,
    });
typedef $$GameHistoriesTableUpdateCompanionBuilder =
    GameHistoriesCompanion Function({
      Value<int> id,
      Value<String> gameType,
      Value<int> score,
      Value<int?> moves,
      Value<int?> timeSeconds,
      Value<int?> maxSequence,
      Value<int?> totalPairs,
      Value<int?> rows,
      Value<int?> wordsFound,
      Value<int> timestamp,
      Value<String?> metadata,
    });

class $$GameHistoriesTableFilterComposer
    extends Composer<_$MochiDatabase, $GameHistoriesTable> {
  $$GameHistoriesTableFilterComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnFilters<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get gameType => $composableBuilder(
    column: $table.gameType,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get score => $composableBuilder(
    column: $table.score,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get moves => $composableBuilder(
    column: $table.moves,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get timeSeconds => $composableBuilder(
    column: $table.timeSeconds,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get maxSequence => $composableBuilder(
    column: $table.maxSequence,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get totalPairs => $composableBuilder(
    column: $table.totalPairs,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get rows => $composableBuilder(
    column: $table.rows,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get wordsFound => $composableBuilder(
    column: $table.wordsFound,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<int> get timestamp => $composableBuilder(
    column: $table.timestamp,
    builder: (column) => ColumnFilters(column),
  );

  ColumnFilters<String> get metadata => $composableBuilder(
    column: $table.metadata,
    builder: (column) => ColumnFilters(column),
  );
}

class $$GameHistoriesTableOrderingComposer
    extends Composer<_$MochiDatabase, $GameHistoriesTable> {
  $$GameHistoriesTableOrderingComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  ColumnOrderings<int> get id => $composableBuilder(
    column: $table.id,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get gameType => $composableBuilder(
    column: $table.gameType,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get score => $composableBuilder(
    column: $table.score,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get moves => $composableBuilder(
    column: $table.moves,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get timeSeconds => $composableBuilder(
    column: $table.timeSeconds,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get maxSequence => $composableBuilder(
    column: $table.maxSequence,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get totalPairs => $composableBuilder(
    column: $table.totalPairs,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get rows => $composableBuilder(
    column: $table.rows,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get wordsFound => $composableBuilder(
    column: $table.wordsFound,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<int> get timestamp => $composableBuilder(
    column: $table.timestamp,
    builder: (column) => ColumnOrderings(column),
  );

  ColumnOrderings<String> get metadata => $composableBuilder(
    column: $table.metadata,
    builder: (column) => ColumnOrderings(column),
  );
}

class $$GameHistoriesTableAnnotationComposer
    extends Composer<_$MochiDatabase, $GameHistoriesTable> {
  $$GameHistoriesTableAnnotationComposer({
    required super.$db,
    required super.$table,
    super.joinBuilder,
    super.$addJoinBuilderToRootComposer,
    super.$removeJoinBuilderFromRootComposer,
  });
  GeneratedColumn<int> get id =>
      $composableBuilder(column: $table.id, builder: (column) => column);

  GeneratedColumn<String> get gameType =>
      $composableBuilder(column: $table.gameType, builder: (column) => column);

  GeneratedColumn<int> get score =>
      $composableBuilder(column: $table.score, builder: (column) => column);

  GeneratedColumn<int> get moves =>
      $composableBuilder(column: $table.moves, builder: (column) => column);

  GeneratedColumn<int> get timeSeconds => $composableBuilder(
    column: $table.timeSeconds,
    builder: (column) => column,
  );

  GeneratedColumn<int> get maxSequence => $composableBuilder(
    column: $table.maxSequence,
    builder: (column) => column,
  );

  GeneratedColumn<int> get totalPairs => $composableBuilder(
    column: $table.totalPairs,
    builder: (column) => column,
  );

  GeneratedColumn<int> get rows =>
      $composableBuilder(column: $table.rows, builder: (column) => column);

  GeneratedColumn<int> get wordsFound => $composableBuilder(
    column: $table.wordsFound,
    builder: (column) => column,
  );

  GeneratedColumn<int> get timestamp =>
      $composableBuilder(column: $table.timestamp, builder: (column) => column);

  GeneratedColumn<String> get metadata =>
      $composableBuilder(column: $table.metadata, builder: (column) => column);
}

class $$GameHistoriesTableTableManager
    extends
        RootTableManager<
          _$MochiDatabase,
          $GameHistoriesTable,
          GameHistory,
          $$GameHistoriesTableFilterComposer,
          $$GameHistoriesTableOrderingComposer,
          $$GameHistoriesTableAnnotationComposer,
          $$GameHistoriesTableCreateCompanionBuilder,
          $$GameHistoriesTableUpdateCompanionBuilder,
          (
            GameHistory,
            BaseReferences<_$MochiDatabase, $GameHistoriesTable, GameHistory>,
          ),
          GameHistory,
          PrefetchHooks Function()
        > {
  $$GameHistoriesTableTableManager(
    _$MochiDatabase db,
    $GameHistoriesTable table,
  ) : super(
        TableManagerState(
          db: db,
          table: table,
          createFilteringComposer: () =>
              $$GameHistoriesTableFilterComposer($db: db, $table: table),
          createOrderingComposer: () =>
              $$GameHistoriesTableOrderingComposer($db: db, $table: table),
          createComputedFieldComposer: () =>
              $$GameHistoriesTableAnnotationComposer($db: db, $table: table),
          updateCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                Value<String> gameType = const Value.absent(),
                Value<int> score = const Value.absent(),
                Value<int?> moves = const Value.absent(),
                Value<int?> timeSeconds = const Value.absent(),
                Value<int?> maxSequence = const Value.absent(),
                Value<int?> totalPairs = const Value.absent(),
                Value<int?> rows = const Value.absent(),
                Value<int?> wordsFound = const Value.absent(),
                Value<int> timestamp = const Value.absent(),
                Value<String?> metadata = const Value.absent(),
              }) => GameHistoriesCompanion(
                id: id,
                gameType: gameType,
                score: score,
                moves: moves,
                timeSeconds: timeSeconds,
                maxSequence: maxSequence,
                totalPairs: totalPairs,
                rows: rows,
                wordsFound: wordsFound,
                timestamp: timestamp,
                metadata: metadata,
              ),
          createCompanionCallback:
              ({
                Value<int> id = const Value.absent(),
                required String gameType,
                required int score,
                Value<int?> moves = const Value.absent(),
                Value<int?> timeSeconds = const Value.absent(),
                Value<int?> maxSequence = const Value.absent(),
                Value<int?> totalPairs = const Value.absent(),
                Value<int?> rows = const Value.absent(),
                Value<int?> wordsFound = const Value.absent(),
                required int timestamp,
                Value<String?> metadata = const Value.absent(),
              }) => GameHistoriesCompanion.insert(
                id: id,
                gameType: gameType,
                score: score,
                moves: moves,
                timeSeconds: timeSeconds,
                maxSequence: maxSequence,
                totalPairs: totalPairs,
                rows: rows,
                wordsFound: wordsFound,
                timestamp: timestamp,
                metadata: metadata,
              ),
          withReferenceMapper: (p0) => p0
              .map((e) => (e.readTable(table), BaseReferences(db, table, e)))
              .toList(),
          prefetchHooksCallback: null,
        ),
      );
}

typedef $$GameHistoriesTableProcessedTableManager =
    ProcessedTableManager<
      _$MochiDatabase,
      $GameHistoriesTable,
      GameHistory,
      $$GameHistoriesTableFilterComposer,
      $$GameHistoriesTableOrderingComposer,
      $$GameHistoriesTableAnnotationComposer,
      $$GameHistoriesTableCreateCompanionBuilder,
      $$GameHistoriesTableUpdateCompanionBuilder,
      (
        GameHistory,
        BaseReferences<_$MochiDatabase, $GameHistoriesTable, GameHistory>,
      ),
      GameHistory,
      PrefetchHooks Function()
    >;

class $MochiDatabaseManager {
  final _$MochiDatabase _db;
  $MochiDatabaseManager(this._db);
  $$LearningScoreEntitiesTableTableManager get learningScoreEntities =>
      $$LearningScoreEntitiesTableTableManager(_db, _db.learningScoreEntities);
  $$UserListsTableTableManager get userLists =>
      $$UserListsTableTableManager(_db, _db.userLists);
  $$GameHistoriesTableTableManager get gameHistories =>
      $$GameHistoriesTableTableManager(_db, _db.gameHistories);
}
