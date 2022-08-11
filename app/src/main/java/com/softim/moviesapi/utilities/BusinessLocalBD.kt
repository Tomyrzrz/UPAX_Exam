package com.softim.moviesapi.utilities

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import androidx.room.TypeConverters

@TypeConverters(Converters::class)
class BusinessLocalBD(context: Context, name: String, factory: SQLiteDatabase.CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("create table business (codigo int primary key, imagen text, nombre text, calle text, " +
                "numeroExterior text, numeroInterior text, codigoPostal text, colonia text, nombrePais text, " +
                "nombreEstado text, nombreMunicipio text, latitud text, longitud text)")
        db.execSQL("create table users (codigo int primary key, nombre text, telefono text, direccion text, " +
                "correo text, imagen text)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }
}