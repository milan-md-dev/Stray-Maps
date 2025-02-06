package com.miles.straymaps.data.lost_pet

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow


@Dao
interface LostPetDao {
    @Query("SELECT * FROM lost_pets")
    fun getAll(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets WHERE lost_pet_microchip_id = :id")
    fun getLostPetByMicrochipId(id: String): LostPet

    @Query("SELECT * FROM lost_pets WHERE lost_pet_type = :type")
    fun getLostPetByType(type: String): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets WHERE lost_pet_name = :name")
    fun getLostPetsByName(name: String): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets ORDER BY lost_pet_name ASC")
    fun loadAllLostPetsByName(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets ORDER BY lost_pet_type ASC")
    fun loadAllLostPetsByType(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets ORDER BY lost_pet_colour ASC")
    fun loadAllLostPetsByColours(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets ORDER BY lost_pet_sex ASC")
    fun loadAllLostPetsBySex(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets ORDER BY lost_pet_report_date_and_time ASC")
    fun loadAllByDateAndTime(): Flow<List<LostPet>>

    @Query("SELECT * FROM lost_pets WHERE lost_pet_report_upload_state = 0")
    fun loadAllNotUploadedReports(): List<LostPet>

    @Upsert
    suspend fun upsertLostPet(lostPet: LostPet)

    @Delete
    suspend fun deleteLostPet(lostPet: LostPet)
}