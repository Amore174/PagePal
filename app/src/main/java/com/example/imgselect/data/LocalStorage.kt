package com.example.imgselect.data

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.imgselect.ChatQueryResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


@Entity(tableName = "content_table_wordMeaning")
data class Meaning(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String?,
    val meaning: String?,

    )

@Entity(tableName = "content_table_summary")
data class Summary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val summary: String?,
    val image: ByteArray?,
    val time: String = "",
    val title: String = ""
)

@Entity(tableName = "content_table_chatbot")
data class Chat(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val message: List<ChatQueryResponse>?
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun chatQueryResponseListToString(chatQueryResponseList: List<ChatQueryResponse>?): String {
        return gson.toJson(chatQueryResponseList)
    }

    @TypeConverter
    fun stringToChatQueryResponseList(data: String): List<ChatQueryResponse>? {
        val type = object : TypeToken<List<ChatQueryResponse>>() {}.type
        return gson.fromJson(data, type)
    }
}


