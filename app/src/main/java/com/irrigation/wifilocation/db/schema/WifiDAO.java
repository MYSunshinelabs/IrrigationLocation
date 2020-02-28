package com.irrigation.wifilocation.db.schema;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Dalvendra Kumar on 17/03/19.
 */

@Dao
public interface WifiDAO {

    @Insert
    void insert(Wifi... wifis);

    @Query("SELECT * FROM Wifi")
    List<Wifi> getAllWifi();

    @Delete
    void deleteWifi(Wifi wifi);

    @Update
    void updatewifi(Wifi wifi);

    @Query ("SELECT * FROM Wifi WHERE id = :wifiId")
    Wifi getWifiById(int wifiId);


}
