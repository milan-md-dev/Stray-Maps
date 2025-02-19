package com.miles.straymaps.data.stray_animal

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("UPDATE stray_animals SET stray_animal_report_upload_state = :isUploaded WHERE stray_animal_report_unique_id = :uniqueId")
    suspend fun updateUploadState(uniqueId: String, isUploaded: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStrayAnimalReport(strayAnimal: StrayAnimal)

    @Update(onConflict = OnConflictStrategy.ABORT)
    suspend fun updateStrayAnimalReport(strayAnimal: StrayAnimal)

    @Delete
    suspend fun delete(strayAnimal: StrayAnimal)
}