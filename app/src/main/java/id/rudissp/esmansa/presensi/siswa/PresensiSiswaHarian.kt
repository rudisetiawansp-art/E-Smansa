package id.rudissp.esmansa.presensi.siswa

data class PresensiSiswaHarian(
    val nis: String,
    val nama: String,
    val masuk: String,
    val pulang: String,
    val status: String
)