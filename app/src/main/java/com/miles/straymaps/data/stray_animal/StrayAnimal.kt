package com.miles.straymaps.data.stray_animal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stray_animals")
data class StrayAnimal(
    @PrimaryKey(autoGenerate = true) val strayAnimalId: Int? = 0,
    @ColumnInfo(name = "stray_animal_photo") val strayAnimalPhotoPath: String?,
    @ColumnInfo(name = "stray_animal_type") val strayAnimalType: String,
    @ColumnInfo(name = "stray_animal_colour") val strayAnimalColour: String,
    @ColumnInfo(name = "stray_animal_sex") val strayAnimalSex: String,
    @ColumnInfo(name = "stray_animal_appearance") val strayAnimalAppearanceDescription: String,
    @ColumnInfo(name = "stray_animal_location") val strayAnimalLocationDescription: String,
    @ColumnInfo(name = "stray_animal_microchip_id") val strayAnimalMicrochipID: String?,
    @ColumnInfo(name = "stray_animal_contact_info") val strayAnimalContactInformation: String,
    @ColumnInfo(name = "stray_animal_additional_info") val strayAnimalAdditionalInformation: String,
    @ColumnInfo(name = "stray_animal_report_date_and_time") val strayAnimalReportDateAndTime: String?,
    @ColumnInfo(name = "stray_animal_report_upload_state") val strayAnimalIsUploaded: Boolean,
    @ColumnInfo(name = "stray_animal_report_made_by_user_id") val strayAnimalReportMadeByUserId: String,
    @ColumnInfo(name = "stray_animal_report_unique_id") val strayAnimalReportUniqueId: String
) {
    constructor() : this(
        0,
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        "",
        null,
        false,
        "",
        ""
    )
}