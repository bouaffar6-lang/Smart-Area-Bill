package com.aistudio.smartareabill.data.database

import androidx.room.TypeConverter
import com.aistudio.smartareabill.data.models.Opening
import com.aistudio.smartareabill.data.models.WorkItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class RoomTypeConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val openingsListType = Types.newParameterizedType(List::class.java, Opening::class.java)
    private val openingsAdapter = moshi.adapter<List<Opening>>(openingsListType)
    
    private val workItemsListType = Types.newParameterizedType(List::class.java, WorkItem::class.java)
    private val workItemsAdapter = moshi.adapter<List<WorkItem>>(workItemsListType)

    @TypeConverter
    fun stringToOpenings(data: String?): List<Opening> {
        if (data == null) return emptyList()
        return try {
            openingsAdapter.fromJson(data) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun openingsToString(openings: List<Opening>?): String {
        return openingsAdapter.toJson(openings ?: emptyList())
    }

    @TypeConverter
    fun stringToWorkItems(data: String?): List<WorkItem> {
        if (data == null) return emptyList()
        return try {
            workItemsAdapter.fromJson(data) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun workItemsToString(workItems: List<WorkItem>?): String {
        return workItemsAdapter.toJson(workItems ?: emptyList())
    }
}
