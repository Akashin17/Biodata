package com.example.biodata.ViewModel

import Biodataitem
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.biodata.Data.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BiodataViewModel(application: Application) : AndroidViewModel(application) {

    // Dapatkan referensi ke DAO dari database
    private val biodataDao = AppDatabase.getDatabase(application).biodataDao()

    // Ambil data biodata sebagai Flow. UI akan otomatis update jika data ini berubah.
    val biodata: Flow<Biodataitem?> = biodataDao.getBiodata()

    // Fungsi untuk menyimpan (insert/update) biodata
    fun saveBiodata(
        name: String,
        nim: String,
        birthPlace: String,
        birthDate: String,
        address: String,
        photoUri: String?
    ) = viewModelScope.launch {
        // Validasi sederhana, jangan simpan jika nama atau nim kosong
        if (name.isBlank() || nim.isBlank()) return@launch

        val newBiodata = Biodataitem(
            id = 1, // ID tetap 1
            name = name.trim(),
            nim = nim.trim(),
            birthPlace = birthPlace.trim(),
            birthDate = birthDate.trim(),
            address = address.trim(),
            photoUri = photoUri
        )
        biodataDao.saveBiodata(newBiodata)
    }

    // Fungsi untuk menghapus biodata
    fun deleteBiodata() = viewModelScope.launch {
        biodataDao.deleteBiodata()
    }
}