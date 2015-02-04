package com.github.android.downloader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.android.downloader.db.annotation.Id;
import com.github.android.downloader.db.annotation.Table;
import com.github.android.downloader.db.annotation.Transient;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhaocheng on 2015/2/3.
 */
public class SqliteDAO {
    static final String TAG = SqliteDAO.class.getSimpleName();

    private SQLiteDatabase db;

    private MySQLiteHelper mySQLiteHelper;

    private int conflictType = 2;

    public SqliteDAO(Context c) {
        if (mySQLiteHelper == null) {
            String prefix = "";
            try {
                prefix = c.getExternalFilesDir(null).getAbsolutePath() + "/";
            } catch (Exception e) {
                prefix = "";
            }
            mySQLiteHelper = new MySQLiteHelper(c, prefix, null, 1);
        }
        db = mySQLiteHelper.getWritableDatabase();
    }

    public void close() {
//        db.close();
    }

    public SQLiteDatabase getSQLiteDatabase() {
        return db;
    }

    public <T> T insert(T entity) {
        return insert(entity, false);
    }

    public <T> int delete(T entity) {
        Object[] args = getPrimarySelectionAndArgs(entity);
        return db.delete(getTableName(entity), (String) args[0], (String[]) args[1]);
    }

    public <T> int deleteByWhereClause(T entity, String whereClause, String[] whereArgs) {
        return db.delete(getTableName(entity), whereClause, whereArgs);
    }

    public <T> List<T> query(T entity, String[] columns, String selection,
                             String[] selectionArgs, String groupBy, String having,
                             String orderBy, String limit) {
        List<T> entities = new ArrayList<T>();
        Cursor cursor = db.query(getTableName(entity), columns,
                selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                T obj = (T) entity.getClass().newInstance();
                getEntity(cursor, obj);
                entities.add(obj);
                while (cursor.moveToNext()) {
                    obj = (T) entity.getClass().newInstance();
                    getEntity(cursor, obj);
                    entities.add(obj);
                }
            }
            return entities;
        } catch (Exception e) {
            Log.e(TAG, "" + e, e);
            return entities;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public <T> List<T> query(T entity, String sql, String[] selectionArgs) {
        List<T> list = new ArrayList<T>();
        Cursor cursor = db.rawQuery(sql, selectionArgs);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                T obj = (T) entity.getClass().newInstance();
                getEntity(cursor, obj);
                list.add(obj);
                while (cursor.moveToNext()) {
                    obj = (T) entity.getClass().newInstance();
                    getEntity(cursor, obj);
                    list.add(obj);
                }
            }
            return list;
        } catch (Exception e) {
            Log.e(TAG, "" + e, e);
            return list;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public <T> List<T> loadAll(T entity, String orderBy, String limit) {
        List<T> entities = new ArrayList<T>();

        Cursor cursor = db.query(getTableName(entity), null, null, null, null, null, orderBy, limit);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                T obj = (T) entity.getClass().newInstance();
                getEntity(cursor, obj);
                entities.add(obj);
                while (cursor.moveToNext()) {
                    obj = (T) entity.getClass().newInstance();
                    getEntity(cursor, obj);
                    entities.add(obj);
                }
            }
            return entities;
        } catch (Exception e) {
            Log.e(TAG, "" + e, e);
            return entities;
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    public int updateByPrimaryKey(Object entity) {
        return updateByPrimaryKey(entity, false);
    }

    private int updateByPrimaryKey(Object entity, boolean selective) {
        ContentValues values = getContentValues(entity, selective);
        Object[] args = getPrimarySelectionAndArgs(entity);

        int r = db.update(getTableName(entity), values, (String) args[0], (String[]) args[1]);

        return r;
    }

    private <T> T insert(T entity, boolean selective) {
        ContentValues values = getContentValues(entity, selective);
        try {
            this.loadByPrimaryKey(entity);  // 这个时候，如果load成功，则entity更新为数据库中的记录值
        } catch (Exception e) {
            /*no op*/
        }
        long r;
        if (conflictType == 2) {
            r = db.replace(getTableName(entity), null, values);
        } else {
            r = db.insert(getTableName(entity), null, values);
        }

        if (r >= 0) {
            return entity;
        }

        return null;
    }

    private Object[] getPrimarySelectionAndArgs(Object entity) {
        Object[] ret = new Object[2];
        String selection = null;
        List<String> args = new ArrayList<String>();
        try {
            Class<?> entity_class = entity.getClass();
            Field[] fs = entity_class.getDeclaredFields();
            for (Field f : fs) {
                if (isPrimaryKey(f)) {
                    Method get = getGetMethod(entity_class, f);
                    if (get != null) {
                        Object o = get.invoke(entity);
                        String value = null;
                        if (o != null) {
                            value = o.toString();
                            if (selection == null) {
                                selection = f.getName() + "=?";
                            } else {
                                selection += " AND " + f.getName() + "=?";
                            }

                            args.add(value);

                        } else {
                            throw new RuntimeException("Primary key: " + f.getName() + " must not be null");
                        }
                    }
                }
            }
            if (selection == null) {
                throw new RuntimeException("Primary key not found!");
            }

            ret[0] = selection;
            ret[1] = args.toArray(new String[args.size()]);
            return ret;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Method getGetMethod(Class<?> entity_class, Field f) {
        String fn = f.getName();
        String mn = "get" + fn.substring(0, 1).toUpperCase() + fn.substring(1);
        try {
            return entity_class.getDeclaredMethod(mn);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Method: " + mn + " not found.");

            return null;
        }
    }

    private boolean isPrimaryKey(Field f) {
        Annotation an = f.getAnnotation(Id.class);
        if (an != null) {
            return true;
        }
        return false;
    }

    public <T> T loadByPrimaryKey(T entity) {
        Object[] args = getPrimarySelectionAndArgs(entity);
        Cursor cursor = db.query(getTableName(entity), null, (String) args[0], (String[]) args[1], null, null, null);
        try {
            if (cursor.moveToNext()) {
                T db_entity = getEntity(cursor, entity);
                return db_entity;
            } else {
                return null;
            }
        } finally {
            cursor.close();
        }
    }

    private <T> T getEntity(Cursor cursor, T entity) {
        try {
            Class<?> entity_class = entity.getClass();

            Field[] fs = entity_class.getDeclaredFields();
            for (Field f : fs) {
                int index = cursor.getColumnIndex(f.getName());
                if (index >= 0) {
                    Method set = getSetMethod(entity_class, f);
                    if (set != null) {
                        String value = cursor.getString(index);
                        if (cursor.isNull(index)) {
                            value = null;
                        }
                        Class<?> type = f.getType();
                        if (type == String.class) {
                            set.invoke(entity, value);
                        } else if (type == int.class || type == Integer.class) {
                            set.invoke(entity, value == null ? (Integer) null : Integer.parseInt(value));
                        } else if (type == float.class || type == Float.class) {
                            set.invoke(entity, value == null ? (Float) null : Float.parseFloat(value));
                        } else if (type == long.class || type == Long.class) {
                            set.invoke(entity, value == null ? (Long) null : Long.parseLong(value));
                        } else if (type == Date.class) {
                            set.invoke(entity, value == null ? (Date) null : stringToDateTime(value));
                        } else {
                            set.invoke(entity, value);
                        }
                    }
                }
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Date stringToDateTime(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (s != null) {
            try {
                return sdf.parse(s);
            } catch (ParseException e) {
                Log.e(TAG, "time error: " + s, e);
            }
        }
        return null;
    }

    private ContentValues getContentValues(Object entity, boolean selective) {
        ContentValues values = new ContentValues();
        try {
            Class<?> entity_class = entity.getClass();
            Field[] fs = entity_class.getDeclaredFields();
            for (Field f : fs) {
                if (isTransient(f) == false) {
                    Method get = getGetMethod(entity_class, f);
                    if (get != null) {
                        Object o = get.invoke(entity);
                        if (!selective || (selective && o != null)) {
                            String name = f.getName();
                            Class<?> type = f.getType();
                            if (type == String.class) {
                                values.put(name, (String) o);
                            } else if (type == int.class || type == Integer.class) {
                                values.put(name, (Integer) o);
                            } else if (type == float.class || type == Float.class) {
                                values.put(name, (Float) o);
                            } else if (type == long.class || type == Long.class) {
                                values.put(name, (Long) o);
                            } else if (type == Date.class) {
                                values.put(name, datetimeToString((Date) o));
                            } else {
                                values.put(name, o.toString());
                            }
                        }
                    }
                }
            }
            return values;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private boolean isTransient(Field f) {
        Annotation an = f.getAnnotation(Transient.class);
        if (an != null) {
            return true;
        }
        return false;
    }

    private String datetimeToString(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (d != null) {
            return sdf.format(d);
        }
        return null;
    }

    private Method getSetMethod(Class<?> entity_class, Field f) {
        String fn = f.getName();
        String mn = "set" + fn.substring(0, 1).toUpperCase() + fn.substring(1);
        try {
            return entity_class.getDeclaredMethod(mn, f.getType());
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "Method: " + mn + " not found.");

            return null;
        }
    }

    private String getTableName(Object entity) {
        Table table = entity.getClass().getAnnotation(Table.class);
        String name = table.name();
        return name;
    }
}

