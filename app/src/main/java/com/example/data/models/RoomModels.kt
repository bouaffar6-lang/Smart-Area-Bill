package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Opening(
    val id: String,
    val type: String, // "WINDOW", "DOOR"
    val width: Double,
    val height: Double,
    val count: Int = 1
) : Serializable {
    val totalArea: Double
        get() = width * height * count
}

@JsonClass(generateAdapter = true)
data class WorkItem(
    val id: String,
    val name: String,
    val unitType: String, // "m²", "m", "piece"
    val unitPrice: Double,
    val quantityType: String = "WALL_NET", // "WALL_NET", "FLOOR", "CEILING", "PERIMETER", "DOORS_COUNT", "WINDOWS_COUNT", "CUSTOM"
    val customQuantity: Double = 1.0
) : Serializable {
    fun getQuantity(room: RoomEntity): Double {
        val metrics = com.example.domain.engine.QuantityCalculationEngine.calculateMetrics(
            length = room.length,
            width = room.width,
            height = room.height,
            openings = room.openings
        )
        return com.example.domain.engine.QuantityEngine.calculateQuantity(
            quantityType = quantityType,
            customQuantity = customQuantity,
            metrics = metrics,
            openings = room.openings
        )
    }
    
    fun getCost(room: RoomEntity): Double {
        return com.example.domain.engine.PricingEngine.calculateCost(getQuantity(room), unitPrice)
    }
}

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val length: Double,
    val width: Double,
    val height: Double,
    val floorArea: Double,
    val wallArea: Double,
    val ceilingArea: Double,
    val perimeter: Double,
    val openingsArea: Double,
    val netWallArea: Double,
    val totalCost: Double,
    val icon: String, // Emoji identifier
    val openings: List<Opening> = emptyList(),
    val workItems: List<WorkItem> = emptyList(),
    val remarks: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@JsonClass(generateAdapter = true)
data class WorkItemTemplate(
    val name: String,
    val price: Double
) : Serializable
