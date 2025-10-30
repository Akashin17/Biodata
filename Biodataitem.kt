import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "biodata_table")
data class Biodataitem(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1, // Kita hanya akan menyimpan satu biodata

    val name: String,
    val nim: String,
    val birthPlace: String,
    val birthDate: String, // Simpan sebagai String agar mudah
    val address: String,
    val photoUri: String? = null // Menyimpan URI foto sebagai String
)