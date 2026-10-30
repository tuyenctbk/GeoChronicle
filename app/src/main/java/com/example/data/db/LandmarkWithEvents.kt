package com.example.data.db

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a landmark along with its associated historical events.
 */
data class LandmarkWithEvents(
    @Embedded val landmark: LandmarkEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "landmarkId"
    )
    val events: List<EventEntity> = emptyList()
)
