package com.example.studentapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) :
    SQLiteOpenHelper(context, "UserDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(
            "CREATE TABLE users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT," +
                    "email TEXT," +
                    "mobile TEXT," +
                    "location TEXT," +
                    "skillTeach TEXT," +
                    "skillLearn TEXT," +
                    "password TEXT)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    // Insert User
    fun insertUser(
        name: String,
        email: String,
        mobile: String,
        location: String,
        teach: String,
        learn: String,
        password: String
    ): Boolean {

        val db = this.writableDatabase

        val values = ContentValues()

        values.put("name", name)
        values.put("email", email)
        values.put("mobile", mobile)
        values.put("location", location)
        values.put("skillTeach", teach)
        values.put("skillLearn", learn)
        values.put("password", password)

        val result = db.insert("users", null, values)

        return result != -1L
    }

    // Get all users for list
    fun getAllUsers(): ArrayList<String> {

        val list = ArrayList<String>()

        val db = this.readableDatabase

        val cursor = db.rawQuery(
            "SELECT name, skillTeach, skillLearn FROM users",
            null
        )

        if (cursor.moveToFirst()) {

            do {

                val name = cursor.getString(0)
                val teach = cursor.getString(1)
                val learn = cursor.getString(2)

                val data = "$name\nTeach : $teach\nLearn : $learn"

                list.add(data)

            } while (cursor.moveToNext())
        }

        cursor.close()

        return list
    }

    // Get total user count
    fun getUserCount(): Int {

        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM users", null)

        val count = cursor.count

        cursor.close()

        return count
    }
}
