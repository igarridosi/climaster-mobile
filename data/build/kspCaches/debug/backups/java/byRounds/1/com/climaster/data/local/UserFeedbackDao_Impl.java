package com.climaster.data.local;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserFeedbackDao_Impl implements UserFeedbackDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserFeedbackEntity> __insertionAdapterOfUserFeedbackEntity;

  public UserFeedbackDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserFeedbackEntity = new EntityInsertionAdapter<UserFeedbackEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_feedback_table` (`id`,`sensation`,`recordedTemp`,`recordedHumidity`,`timestamp`) VALUES (?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserFeedbackEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getSensation());
        statement.bindDouble(3, entity.getRecordedTemp());
        statement.bindLong(4, entity.getRecordedHumidity());
        statement.bindLong(5, entity.getTimestamp());
      }
    };
  }

  @Override
  public Object insertFeedback(final UserFeedbackEntity feedback,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserFeedbackEntity.insert(feedback);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<UserFeedbackEntity>> getAllFeedback() {
    final String _sql = "SELECT * FROM user_feedback_table ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_feedback_table"}, new Callable<List<UserFeedbackEntity>>() {
      @Override
      @NonNull
      public List<UserFeedbackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSensation = CursorUtil.getColumnIndexOrThrow(_cursor, "sensation");
          final int _cursorIndexOfRecordedTemp = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedTemp");
          final int _cursorIndexOfRecordedHumidity = CursorUtil.getColumnIndexOrThrow(_cursor, "recordedHumidity");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final List<UserFeedbackEntity> _result = new ArrayList<UserFeedbackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final UserFeedbackEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpSensation;
            _tmpSensation = _cursor.getString(_cursorIndexOfSensation);
            final double _tmpRecordedTemp;
            _tmpRecordedTemp = _cursor.getDouble(_cursorIndexOfRecordedTemp);
            final int _tmpRecordedHumidity;
            _tmpRecordedHumidity = _cursor.getInt(_cursorIndexOfRecordedHumidity);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _item = new UserFeedbackEntity(_tmpId,_tmpSensation,_tmpRecordedTemp,_tmpRecordedHumidity,_tmpTimestamp);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
