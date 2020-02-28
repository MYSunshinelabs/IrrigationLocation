package com.irrigation.wifilocation.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.irrigation.wifilocation.db.schema.Wifi;
import com.irrigation.wifilocation.db.schema.WifiDAO;

/**
 * Created by laxmi on 26/03/19.
 */
@Database(entities = {Wifi.class}, version = 2, exportSchema = false)
public abstract class AppDataBase extends RoomDatabase {
    private static AppDataBase appDataBase;

    @Override
    protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration config) {
        return null;
    }

    @Override
    protected InvalidationTracker createInvalidationTracker() {
        return null;
    }

    public static AppDataBase getAppDataBase(Context context) {
        if (appDataBase == null)
            appDataBase = Room.databaseBuilder(context.getApplicationContext(), AppDataBase.class, "Wifi-database")
                    // allow queries on the main thread.
                    // Don't do this on a real app! See PersistenceBasicSample for an example.
                    .fallbackToDestructiveMigration()
//                    .addMigrations(migration)
                    .build();
        return appDataBase;
    }


    public abstract WifiDAO wifiDAO();

    static final Migration migration = new Migration(1, 2) {
        @Override
        public void migrate(final SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE Repo ADD COLUMN createdAt TEXT");
        }
    };

}
