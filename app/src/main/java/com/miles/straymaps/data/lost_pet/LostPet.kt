package com.miles.straymaps.data.lost_pet

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime


@Entity(tableName = "lost_pets")
data class LostPet(
    @PrimaryKey(autoGenerate = true) val lostPetId: Int? = 0,
    @ColumnInfo(name = "lost_pet_photo") val lostPetPhoto: String?,
    @ColumnInfo(name = "lost_pet_type") val lostPetType: String,
    @ColumnInfo(name = "lost_pet_name") val lostPetName: String,
    @ColumnInfo(name = "lost_pet_colour") val lostPetColour: String,
    @ColumnInfo(name = "lost_pet_sex") val lostPetSex: String,
    @ColumnInfo(name = "lost_pet_appearance") val lostPetAppearanceDescription: String,
    @ColumnInfo(name = "lost_pet_last_known_location") val lostPetLastKnownLocation: String,
    @ColumnInfo(name = "lost_pet_microchip_id") val lostPetMicrochipId: String,
    @ColumnInfo(name = "lost_pet_contact_info") val lostPetContactInformation: String,
    @ColumnInfo(name = "lost_pet_additional_info") val lostPetAdditionalInformation: String,
    @ColumnInfo(name = "lost_pet_report_date_and_time") val lostPetReportDateAndTime: LocalDateTime?,
    @ColumnInfo(name = "lost_pet_report_upload_state") val lostPetIsUploaded: Boolean
)