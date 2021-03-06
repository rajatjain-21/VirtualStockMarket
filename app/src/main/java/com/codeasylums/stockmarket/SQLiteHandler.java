package com.codeasylums.stockmarket;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by raunak on 21/5/17.
 */

public class SQLiteHandler extends SQLiteOpenHelper {

  private static final String TAG = SQLiteHandler.class.getSimpleName();

  // All Static variables
  // Database Version
  private static final int DATABASE_VERSION = 1;

  // Database Name
  private static final String DATABASE_NAME = "VIrtualStockMarket";

  //User Tables
  private static final String TABLE_TRANSACTIONS = "myTransactions";
  private static final String TABLE_SHARE_DATA = "shareData";

  private static final String KEY_COMPANY_NAME = "copanyName";

  private static final String KEY_AMOUNT_SHARES = "amountShares";
  private static final String KEY_SHARE_RATE = "shareRate";

  private static final String KEY_ID              = "id";
  private static final String KEY_SOLD_OR_BOUGHT              = "soldOrBought";


  public SQLiteHandler(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }


  @Override
  public void onCreate(SQLiteDatabase sqLiteDatabase) {
    String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
        + KEY_ID + " INTEGER PRIMARY KEY,"
        + KEY_COMPANY_NAME + " TEXT,"
        + KEY_AMOUNT_SHARES + " TEXT,"
        + KEY_SHARE_RATE + " TEXT,"
        +KEY_SOLD_OR_BOUGHT+" BOOLEAN"
        + ")";
    String CREATE_SHARE_TABLE = "CREATE TABLE " + TABLE_SHARE_DATA + "("
        + KEY_ID + " INTEGER PRIMARY KEY,"
        + KEY_COMPANY_NAME + " TEXT,"
        + KEY_AMOUNT_SHARES + " TEXT,"
        + KEY_SHARE_RATE + " TEXT"
        + ")";
    sqLiteDatabase.execSQL(CREATE_TRANSACTIONS_TABLE);
    sqLiteDatabase.execSQL(CREATE_SHARE_TABLE);

    Log.d(TAG, "Database tables created");
  }

  // Upgrading database
  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // Drop older table if existed

    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

    // Create tables again
    onCreate(db);
  }

  public void addTransactions(ShareTransactionObject soldShareDataObject) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues transaction = new ContentValues();
   transaction.put(KEY_COMPANY_NAME,soldShareDataObject.getCompanyName());
    transaction.put(KEY_AMOUNT_SHARES,soldShareDataObject.getAmountShares());
    transaction.put(KEY_SHARE_RATE,soldShareDataObject.getShareRate());
    transaction.put(KEY_SOLD_OR_BOUGHT,soldShareDataObject.isSoldOrBought());

    // Inserting Row
    long id = db.insert(TABLE_TRANSACTIONS, null, transaction);
    ; // Closing database connection

    Log.d(TAG, "New transaction inserted into sqlite: " + id);
  }

  public List<ShareTransactionObject> getTransactions(){
    List<ShareTransactionObject> shareTransactionObjectList= new ArrayList<>();
    String selectQuery = "SELECT  * FROM " + TABLE_TRANSACTIONS;
    int i = 0;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // Move to first row
    cursor.moveToFirst();

    while (cursor.isAfterLast() == false) {
     ShareTransactionObject shareTransactionObject=new ShareTransactionObject();
      shareTransactionObject.setCompanyName(cursor.getString(1));
      shareTransactionObject.setShareRate(cursor.getString(3));
      shareTransactionObject.setAmountShares(cursor.getString(2));
      shareTransactionObject.setSoldOrBought(Boolean.parseBoolean(cursor.getString(4)));
      cursor.moveToNext();
      shareTransactionObjectList.add(i,shareTransactionObject);
      i++;

    }
    cursor.close();
    Collections.reverse(shareTransactionObjectList);
    return shareTransactionObjectList;
  }

  public List<ShareTransactionObject> getSharedData(){
    List<ShareTransactionObject> shareTransactionObjectList= new ArrayList<>();
    String selectQuery = "SELECT  * FROM " + TABLE_SHARE_DATA;
    int i = 0;
    SQLiteDatabase db = this.getReadableDatabase();
    Cursor cursor = db.rawQuery(selectQuery, null);

    // Move to first row
    cursor.moveToFirst();

    while (cursor.isAfterLast() == false) {
      ShareTransactionObject shareTransactionObject=new ShareTransactionObject();
      shareTransactionObject.setCompanyName(cursor.getString(1));
      shareTransactionObject.setShareRate(cursor.getString(3));
      shareTransactionObject.setAmountShares(cursor.getString(2));
      cursor.moveToNext();
      shareTransactionObjectList.add(i,shareTransactionObject);
      i++;

    }
    cursor.close();
    return shareTransactionObjectList;
  }






  public void addShareDataToSqlite(ShareTransactionObject soldShareDataObject) {
    SQLiteDatabase db = this.getWritableDatabase();

    ContentValues transaction = new ContentValues();
    transaction.put(KEY_COMPANY_NAME,soldShareDataObject.companyName);



    String selectString = "SELECT * FROM " + TABLE_SHARE_DATA + " WHERE " + KEY_COMPANY_NAME + " = '"+soldShareDataObject.getCompanyName()+"'";

    // Add the String you are searching by here.
    // Put it in an array to avoid an unrecognized token error
    Cursor cursor = db.rawQuery(selectString,null);
    if(cursor.moveToFirst()){
      double shareRate = Double.parseDouble(cursor.getString(3));
      double amountShare=Double.parseDouble(cursor.getString(2));
      double newShareRate=Double.parseDouble(soldShareDataObject.getShareRate());
      double newAmountShare=Double.parseDouble(soldShareDataObject.getAmountShares());
      double newAmount=((shareRate*amountShare)+newAmountShare*newShareRate)/(amountShare+newAmountShare);
      soldShareDataObject.setAmountShares(cursor.getString(2)+soldShareDataObject.getAmountShares());
      transaction.put(KEY_SHARE_RATE,newAmount);
      transaction.put(KEY_AMOUNT_SHARES,
          newAmountShare+amountShare);


      db.update(TABLE_SHARE_DATA, transaction,KEY_COMPANY_NAME+"='"+soldShareDataObject.getCompanyName()+"'" , null);

      Log.d("UPDATED",cursor.getString(1));
    }else {
      transaction.put(KEY_SHARE_RATE,soldShareDataObject.getShareRate());
      transaction.put(KEY_AMOUNT_SHARES,
          soldShareDataObject.getAmountShares());
      long id = db.insert(TABLE_SHARE_DATA, null, transaction);
      ; // Closing database connection

      Log.d(TAG, "New transaction inserted into sqlite: " + id);
    }




  }



 /* public void remAnn(String title, String description) {
    SQLiteDatabase db = this.getWritableDatabase();
    String selectQuery = "DELETE FROM " + TABLE_ANNOUNCEMENTS + " WHERE " + KEY_ANN_TITLE + " = '" + title + "' AND " + KEY_ANN_DESCRIPTION + " = '" + description+"'";
    db.execSQL(selectQuery);
    Log.d(TAG, "New user inserted into sqlite: ");
  }
*/

  public void deleteAllSharedata() {
    SQLiteDatabase db = this.getWritableDatabase();
    // Delete All Rows
    db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);

    Log.d(TAG, "Deleted all user info from sqlite");
    onCreate(db);
  }


}
