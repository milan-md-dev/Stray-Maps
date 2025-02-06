package com.miles.straymaps.data.stray_animal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface StrayAnimalDao {

    @Query("SELECT * FROM stray_animals")
    fun getAll(): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals WHERE stray_animal_microchip_id = :id")
    fun getStrayAnimalByMicrochipId(id: String): StrayAnimal?

    @Query("SELECT * FROM stray_animals WHERE stray_animal_type = :type")
    fun getStrayAnimalByType(type: String): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals ORDER BY stray_animal_type ASC")
    fun loadAllByType(): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals ORDER BY stray_animal_colour ASC")
    fun loadAllByColour(): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals ORDER BY stray_animal_sex ASC")
    fun loadAllBySex(): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals ORDER BY stray_animal_report_date_and_time ASC")
    fun loadAllByDateAndTime(): Flow<List<StrayAnimal>>

    @Query("SELECT * FROM stray_animals WHERE stray_animal_report_upload_state = 0")
    fun loadAllNotUploadedReports(): List<StrayAnimal>

    @Upsert
    suspend fun upsert(strayAnimal: StrayAnimal)

    @Delete
    suspend fun delete(strayAnimal: StrayAnimal)
}