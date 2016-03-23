package in.bucksbuddy.bucksbuddy;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shrukul on 20/1/16.
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "contactsManager";

    // Contacts table name
    private static final String TABLE_CONTACTS = "contacts";

    // Contacts Table Columns names
    private static final String KEY_ID = "photo_id";
    private static final String KEY_NAME = "name";
    private static final String KEY_PHONE = "age";
    private static final String KEY_AMOUNT = "amt";
    private static final String KEY_TYPE = "type";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "("
                + KEY_ID + " INTEGER ," + KEY_NAME + " TEXT,"
                + KEY_PHONE + " TEXT," + KEY_AMOUNT + " TEXT," + KEY_TYPE + " INTEGER )";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void addContact(Person contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, contact.getName()); // Contact Name
        values.put(KEY_PHONE, contact.getPhone()); // Contact Phone Number
        values.put(KEY_ID, contact.getPhotoId()); // Phone ID
        values.put(KEY_AMOUNT, contact.getAmount()); // Amount
        values.put(KEY_TYPE, contact.getType()); // Amount

        // Inserting Row
        db.insert(TABLE_CONTACTS, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    public Person getContact(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[]{KEY_ID,
                        KEY_NAME, KEY_PHONE, KEY_AMOUNT}, KEY_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Person contact = new Person(cursor.getString(2),
                cursor.getString(1), Integer.parseInt(cursor.getString(0)));
        // return contact
        return contact;
    }

    // Getting All Contacts
    public List<Person> getAllContacts() throws ParseException {
        List<Person> contactList = new ArrayList<Person>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS + " ORDER BY " + KEY_PHONE + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        SimpleDateFormat final_date1 = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        SimpleDateFormat final_date2 = new SimpleDateFormat("dd-MMM-yyyy");

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Person contact = new Person();
                date = final_date1.parse(cursor.getString(2));
                contact.setPhotoId(Integer.parseInt(cursor.getString(0)));
                contact.setName(cursor.getString(1));
                contact.setPhone(final_date2.format(date));
                contact.setAmount(cursor.getString(3));
                contact.setType(Integer.parseInt(cursor.getString(4)));
                // Adding contact to list
                contactList.add(contact);
            } while (cursor.moveToNext());
        }

        // return contact list
        return contactList;
    }

    // Getting contacts Count
    public int getContactsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }
}