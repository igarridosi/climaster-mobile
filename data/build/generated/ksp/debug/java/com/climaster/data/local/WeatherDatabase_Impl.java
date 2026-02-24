package com.climaster.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class WeatherDatabase_Impl extends WeatherDatabase {
  private volatile WeatherDao _weatherDao;

  private volatile UserFeedbackDao _userFeedbackDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `weather_table` (`id` INTEGER NOT NULL, `temperature` REAL NOT NULL, `condition` TEXT NOT NULL, `humidity` INTEGER NOT NULL, `windSpeed` REAL NOT NULL, `cityName` TEXT NOT NULL, `lastUpdated` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_feedback_table` (`id` TEXT NOT NULL, `sensation` TEXT NOT NULL, `recordedTemp` REAL NOT NULL, `recordedHumidity` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9b2950d05a72cf5477b17b8d6724bbca')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `weather_table`");
        db.execSQL("DROP TABLE IF EXISTS `user_feedback_table`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsWeatherTable = new HashMap<String, TableInfo.Column>(7);
        _columnsWeatherTable.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("temperature", new TableInfo.Column("temperature", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("condition", new TableInfo.Column("condition", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("humidity", new TableInfo.Column("humidity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("windSpeed", new TableInfo.Column("windSpeed", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("cityName", new TableInfo.Column("cityName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsWeatherTable.put("lastUpdated", new TableInfo.Column("lastUpdated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysWeatherTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesWeatherTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoWeatherTable = new TableInfo("weather_table", _columnsWeatherTable, _foreignKeysWeatherTable, _indicesWeatherTable);
        final TableInfo _existingWeatherTable = TableInfo.read(db, "weather_table");
        if (!_infoWeatherTable.equals(_existingWeatherTable)) {
          return new RoomOpenHelper.ValidationResult(false, "weather_table(com.climaster.data.local.WeatherEntity).\n"
                  + " Expected:\n" + _infoWeatherTable + "\n"
                  + " Found:\n" + _existingWeatherTable);
        }
        final HashMap<String, TableInfo.Column> _columnsUserFeedbackTable = new HashMap<String, TableInfo.Column>(5);
        _columnsUserFeedbackTable.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFeedbackTable.put("sensation", new TableInfo.Column("sensation", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFeedbackTable.put("recordedTemp", new TableInfo.Column("recordedTemp", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFeedbackTable.put("recordedHumidity", new TableInfo.Column("recordedHumidity", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserFeedbackTable.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserFeedbackTable = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserFeedbackTable = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserFeedbackTable = new TableInfo("user_feedback_table", _columnsUserFeedbackTable, _foreignKeysUserFeedbackTable, _indicesUserFeedbackTable);
        final TableInfo _existingUserFeedbackTable = TableInfo.read(db, "user_feedback_table");
        if (!_infoUserFeedbackTable.equals(_existingUserFeedbackTable)) {
          return new RoomOpenHelper.ValidationResult(false, "user_feedback_table(com.climaster.data.local.UserFeedbackEntity).\n"
                  + " Expected:\n" + _infoUserFeedbackTable + "\n"
                  + " Found:\n" + _existingUserFeedbackTable);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9b2950d05a72cf5477b17b8d6724bbca", "95d0bf60981c4b1d3deee99917649c15");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "weather_table","user_feedback_table");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `weather_table`");
      _db.execSQL("DELETE FROM `user_feedback_table`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(WeatherDao.class, WeatherDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserFeedbackDao.class, UserFeedbackDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public WeatherDao getWeatherDao() {
    if (_weatherDao != null) {
      return _weatherDao;
    } else {
      synchronized(this) {
        if(_weatherDao == null) {
          _weatherDao = new WeatherDao_Impl(this);
        }
        return _weatherDao;
      }
    }
  }

  @Override
  public UserFeedbackDao getUserFeedbackDao() {
    if (_userFeedbackDao != null) {
      return _userFeedbackDao;
    } else {
      synchronized(this) {
        if(_userFeedbackDao == null) {
          _userFeedbackDao = new UserFeedbackDao_Impl(this);
        }
        return _userFeedbackDao;
      }
    }
  }
}
