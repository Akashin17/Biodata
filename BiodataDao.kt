package com.example.biodata.Model.Dao

import Biodataitem
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BiodataDao {

    // Mengganti atau memasukkan biodata. Karena id-nya 1, ini akan selalu update.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBiodata(biodata: Biodataitem)

    // Mengambil biodata. Flow akan update otomatis saat data berubah.
    @Query("SELECT * FROM biodata_table WHERE id = 1")
    fun getBiodata(): Flow<Biodataitem?>

    // Menghapus biodata
    @Query("DELETE FROM biodata_table WHERE id = 1")
    suspend fun deleteBiodata()
}