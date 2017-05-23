package com.hashbrown.erebor.locationwisenew.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Erebor on 19/05/17.
 */

public class db_locationwise extends SQLiteOpenHelper
{
    public static  int version=1;
    public static String db_name="db_locationwise";
    static final String tb_photo_details="tb_photo_details";
    static final String latitude="latitude",longitude="longitude",address="address",date="date",time="time",path="path",co_ordinate_one="co_ordinate_one",co_ordinate_two="co_ordinate_two";


    public db_locationwise(Context Context)
    {
        super(Context, db_name, null, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String q="create table if not exists "+tb_photo_details+" ("+latitude+" varchar(500),"+longitude+" varchar(500),"+address +" varchar(1000),"+date+" varchar(500),"+time+" varchar(500),"+path+" varchar(500),"+co_ordinate_one+" varchar(500),"+co_ordinate_two+" varchar(500))";

        db.execSQL(q);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {

        String q="drop table if exists"+tb_photo_details;
        db.execSQL(q);
    }

    public boolean add_pic_details(pic_details pic_details)
    {
        SQLiteDatabase SQLiteDatabase = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("latitude",pic_details.getLatitude());
        cv.put("longitude",pic_details.getLongitude());
        cv.put("address",pic_details.getAddress());
        cv.put("date",pic_details.getDate());
        cv.put("time",pic_details.getTime());
        cv.put("path",pic_details.getPath());
        cv.put("co_ordinate_one",pic_details.getCo_ordinate_one());
        cv.put("co_ordinate_two",pic_details.getCo_ordinate_two());
        SQLiteDatabase.insert(tb_photo_details, null, cv);
        SQLiteDatabase.close();
        return true;
    }

    public String getAddress(String loc)
    {
        String data="";
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db. query(tb_photo_details,new String[]{address},path + "=?",new String[]{loc},null,null,null,null);
        if(c!=null)
        {
            c.moveToFirst();
            data=c.getString(c.getColumnIndex(address));
            return data;
        }
        else
        {
            data="";
        }
        c.close();
        return data;
    }

    public String[] getAllindexes_LawDict()
    {
        int total=0;
        SQLiteDatabase db=this.getReadableDatabase();

        Cursor csr=db.query(tb_photo_details, null, null,null,null,null,null);
        csr.moveToFirst();
        while(!csr.isAfterLast())
        {
            total++;
            csr.moveToNext();
        }

        String strarray[] = new String[total];

        Cursor csrs=db.query(tb_photo_details, null, null,null,null,null,null);
        csrs.moveToFirst();
        int aray=0;
        while(!csrs.isAfterLast())
        {
            strarray[aray]=csrs.getString(0);
            aray++;
            csrs.moveToNext();
        }
        return strarray;
    }

    public boolean deleteimagedata(String loc)
    {
        SQLiteDatabase db=this.getReadableDatabase();
        db.delete(tb_photo_details,
                path + " = ?",
                new String[] { loc });
        return false;

    }

}
