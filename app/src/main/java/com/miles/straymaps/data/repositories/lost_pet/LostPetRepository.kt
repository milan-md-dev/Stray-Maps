package com.miles.straymaps.data.repositories.lost_pet

import com.miles.straymaps.data.lost_pet.LostPet
import kotlinx.coroutines.flow.Flow

interface LostPetRepositoryInterface {

    //Retrieves all the items from the given data source
    fun loadAllLostPetReports(): Flow<List<LostPet>>

    //Retrieves all the items from the given data source that match with the [microchip id]
    fun getLostPetsByMicrochipId(id: String): LostPet

    //Retrieves all the items from the given data source that match with the [type]
    fun getAllLostPetsOfSpecificType(type: String): Flow<List<LostPet>>

    //Retrieves all the items from the given data source that match with the [name]
    fun getLostPetsByName(name: String): Flow<List<LostPet>>

    //Retrieves all the items from the given data source sorted by [type]
    fun loadAllByType(): Flow<List<LostPet>>

    //Retrieves all the items from the given data source sorted by [colour]
    fun loadAllByColour(): Flow<List<LostPet>>

    //Retrieves all the items from the given data source sorted by [sex]
    fun loadAllBySex(): Flow<List<LostPet>>

    //Retrieves all the items from the given data source sorted by [date and time]
    fun loadAllByDateAndTime(): Flow<List<LostPet>>

    fun loadAllNotUploadedReports(): List<LostPet>

    //Upserts item in the data source
    suspend fun upsertLostPet(lostPet: LostPet)

    //Deletes item from the data source
    suspend fun deleteLostPet(lostPet: LostPet)

}