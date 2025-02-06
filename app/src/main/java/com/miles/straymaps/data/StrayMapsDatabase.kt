package com.miles.straymaps.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.miles.straymaps.data.lost_pet.LostPet
import com.miles.straymaps.data.lost_pet.LostPetDao
import com.miles.straymaps.data.stray_animal.StrayAnimal
import com.miles.straymaps.data.stray_animal.StrayAnimalDao


@Database(
    entities = [StrayAnimal::class, LostPet::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)

abstract class StrayMapsDatabase : RoomDatabase() {

    abstract fun strayAnimalDao(): StrayAnimalDao
    abstract fun lostPetDAo(): LostPetDao

}