package com.example.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room database entity representing a historical event associated with a landmark or historical site.
 */
@Entity(
    tableName = "historical_events",
    foreignKeys = [
        ForeignKey(
            entity = LandmarkEntity::class,
            parentColumns = ["id"],
            childColumns = ["landmarkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["landmarkId"]),
        Index(value = ["year"]),
        Index(value = ["era"])
    ]
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val landmarkId: Long?,
    val title: String,
    val date: String?,
    val year: Int? = null,
    val era: String? = null,
    val description: String,
    val significance: String? = null,
    val sourceUrl: String? = null,
    val cachedAt: Long = System.currentTimeMillis()
)
