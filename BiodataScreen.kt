package com.example.biodata.Screen

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // <-- Import dari Coil
import com.example.biodata.ViewModel.BiodataViewModel
import kotlinx.coroutines.launch

private val Icons.Filled.AddAPhoto: ImageVector
private val Icons.Filled.Cancel: Any
private val Icons.Filled.Save: Any

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiodataScreenContent(
    modifier: Modifier = Modifier,
    viewModel: BiodataViewModel = viewModel() // Dapatkan instance ViewModel
) {
    // Amati data biodata dari ViewModel
    val biodata by viewModel.biodata.collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State untuk mode edit
    var isEditing by remember { mutableStateOf(false) }

    // State untuk menampung input di mode edit
    var nameInput by remember { mutableStateOf("") }
    var nimInput by remember { mutableStateOf("") }
    var birthPlaceInput by remember { mutableStateOf("") }
    var birthDateInput by remember { mutableStateOf("") }
    var addressInput by remember { mutableStateOf("") }
    var photoUriInput by remember { mutableStateOf<String?>(null) }

    // State untuk dialog konfirmasi hapus
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Launcher untuk memilih foto dari galeri
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // Simpan URI foto sebagai string
        uri?.let {
            // Penting: Ambil izin persisten untuk URI
            val context = (coroutineScope.coroutineContext[kotlinx.coroutines.Job] as? androidx.lifecycle.ViewModel?)?.getApplication<android.app.Application>()?.applicationContext
            context?.contentResolver?.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        photoUriInput = uri?.toString()
    }

    // LaunchedEffect untuk mengisi form saat mode edit diaktifkan
    // atau saat data biodata dari database berubah
    LaunchedEffect(biodata, isEditing) {
        if (isEditing) {
            biodata?.let {
                nameInput = it.name
                nimInput = it.nim
                birthPlaceInput = it.birthPlace
                birthDateInput = it.birthDate
                addressInput = it.address
                photoUriInput = it.photoUri
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Biodata Saya") },
                actions = {
                    if (isEditing) {
                        // Aksi saat mode Edit: Simpan dan Batal
                        IconButton(onClick = {
                            viewModel.saveBiodata(
                                name = nameInput,
                                nim = nimInput,
                                birthPlace = birthPlaceInput,
                                birthDate = birthDateInput,
                                address = addressInput,
                                photoUri = photoUriInput
                            )
                            isEditing = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Biodata disimpan")
                            }
                        }) {
                            Icon(Icons.Default.Save, contentDescription = "Simpan")
                        }
                        IconButton(onClick = { isEditing = false }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Batal")
                        }
                    } else {
                        // Aksi saat mode View: Edit dan Hapus
                        IconButton(onClick = {
                            isEditing = true
                            // Jika belum ada biodata, siapkan form kosong
                            if (biodata == null) {
                                nameInput = ""
                                nimInput = ""
                                birthPlaceInput = ""
                                birthDateInput = ""
                                addressInput = ""
                                photoUriInput = null
                            }
                        }) {
                            // Tampilkan "Edit" jika data ada, "Tambah" jika belum
                            Icon(Icons.Default.Edit, contentDescription = if (biodata != null) "Edit" else "Tambah Biodata")
                        }
                        // Tampilkan tombol Hapus hanya jika data ada
                        if (biodata != null) {
                            IconButton(onClick = { showDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus")
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Agar bisa di-scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tampilan Foto
            PhotoSection(
                photoUri = if (isEditing) photoUriInput else biodata?.photoUri, // Tampilkan foto sesuai mode
                isEditing = isEditing,
                onPhotoClick = {
                    photoPickerLauncher.launch("image/*") // Buka galeri
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tampilan Data
            if (isEditing) {
                // Mode Edit: Tampilkan TextField
                BiodataEditFields(
                    name = nameInput, onNameChange = { nameInput = it },
                    nim = nimInput, onNimChange = { nimInput = it },
                    birthPlace = birthPlaceInput, onBirthPlaceChange = { birthPlaceInput = it },
                    birthDate = birthDateInput, onBirthDateChange = { birthDateInput = it },
                    address = addressInput, onAddressChange = { addressInput = it }
                )
            } else {
                // Mode View: Tampilkan Text
                biodata?.let {
                    BiodataView(it)
                } ?: run {
                    // Tampilan jika belum ada data
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "Belum ada biodata.\nKlik ikon 'Edit' ✏️ di atas untuk menambahkan.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Biodata") },
            text = { Text("Apakah Anda yakin ingin menghapus biodata ini? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBiodata()
                        showDeleteDialog = false
                        isEditing = false // Keluar dari mode edit jika sedang aktif
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Biodata dihapus")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = MaterialTheme.colorScheme.onError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

/**
 * Composable untuk menampilkan foto profil.
 */
@Composable
fun PhotoSection(
    photoUri: String?,
    isEditing: Boolean,
    onPhotoClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .clickable(enabled = isEditing, onClick = onPhotoClick), // Hanya bisa diklik saat mode edit
        contentAlignment = Alignment.Center
    ) {
        if (photoUri != null) {
            AsyncImage(
                model = Uri.parse(photoUri), // Coil perlu Uri object
                contentDescription = "Foto Profil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Tampilan placeholder jika tidak ada foto
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Placeholder Foto",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }

        // Tampilkan overlay ikon "edit foto" saat mode edit
        if (isEditing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddAPhoto,
                    contentDescription = "Ubah Foto",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

/**
 * Composable untuk menampilkan data biodata (mode view).
 */
@Composable
fun BiodataView(biodata: com.example.biodata.Model.Biodataitem) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        InfoRow(label = "Nama", value = biodata.name)
        InfoRow(label = "NIM", value = biodata.nim)
        InfoRow(label = "Tempat Lahir", value = biodata.birthPlace)
        InfoRow(label = "Tanggal Lahir", value = biodata.birthDate)
        InfoRow(label = "Alamat", value = biodata.address)
    }
}

/**
 * Composable helper untuk satu baris info (Label & Value).
 */
@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium, // Label lebih kecil
            color = MaterialTheme.colorScheme.onSurfaceVariant // Warna abu-abu
        )
        Text(
            text = value.ifEmpty { "-" }, // Tampilkan "-" jika kosong
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(top = 2.dp)
        )
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}

/**
 * Composable untuk field input (mode edit).
 */
@Composable
fun BiodataEditFields(
    name: String, onNameChange: (String) -> Unit,
    nim: String, onNimChange: (String) -> Unit,
    birthPlace: String, onBirthPlaceChange: (String) -> Unit,
    birthDate: String, onBirthDateChange: (String) -> Unit,
    address: String, onAddressChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = nim,
            onValueChange = onNimChange,
            label = { Text("NIM (Nomor Induk Mahasiswa)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = birthPlace,
            onValueChange = onBirthPlaceChange,
            label = { Text("Tempat Lahir") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            label = { Text("Tanggal Lahir (cth: 1 Januari 2000)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = address,
            onValueChange = onAddressChange,
            label = { Text("Alamat") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3 // Alamat bisa panjang
        )
    }
}