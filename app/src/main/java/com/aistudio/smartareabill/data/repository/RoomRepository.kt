package com.aistudio.smartareabill.data.repository

import com.aistudio.smartareabill.data.database.RoomDao
import com.aistudio.smartareabill.data.models.RoomEntity
import kotlinx.coroutines.flow.Flow

class RoomRepository(private val roomDao: RoomDao) {
    val allRooms: Flow<List<RoomEntity>> = roomDao.getAllRooms()

    fun getRoomFlow(id: Long): Flow<RoomEntity?> {
        return roomDao.getRoomByIdFlow(id)
    }

    suspend fun getRoomById(id: Long): RoomEntity? {
        return roomDao.getRoomById(id)
    }

    suspend fun insertRoom(room: RoomEntity): Long {
        return roomDao.insertRoom(room)
    }

    suspend fun deleteRoom(id: Long) {
        roomDao.deleteRoomById(id)
    }

    suspend fun clearAll() {
        roomDao.deleteAllRooms()
    }
}
