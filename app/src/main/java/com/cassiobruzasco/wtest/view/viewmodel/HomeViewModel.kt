package com.cassiobruzasco.wtest.view.viewmodel

import android.content.Context
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cassiobruzasco.wtest.data.DownloadService
import com.cassiobruzasco.wtest.data.RetrofitInstance
import com.cassiobruzasco.wtest.data.model.PostalCodeModel
import com.cassiobruzasco.wtest.util.normalizeString
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import kotlin.concurrent.schedule

class HomeViewModel : ViewModel() {

    private companion object {
        const val POSTAL_CODE_FILE_URL = "codigos_postais.csv"
        const val FILE_NAME = "/postalcode.csv"
        const val CSV_DIRECTORY = "/csv/"

        val FILE_DIRECTORY: String = Environment.DIRECTORY_DOCUMENTS
    }

    private val downloadServiceAPI =
        RetrofitInstance.getRetrofitInstance().create(DownloadService::class.java)
    private val _fileState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val fileState = _fileState.asStateFlow()

    private val _fileWasRead = MutableStateFlow<MutableList<PostalCodeModel>?>(null)
    val fileWasRead = _fileWasRead.asStateFlow()

    fun scheduleFetch(context: Context) {
        _fileState.value = DownloadState.Loading
        Timer().schedule(1000) { downloadFile(context) }
    }

    private fun downloadFile(context: Context) {
        val file =
            File("${context.getExternalFilesDir(FILE_DIRECTORY)?.absolutePath}$CSV_DIRECTORY")
        if (!hasCsv(file)) downloadCsv(file) else _fileState.value =
            DownloadState.HasFile(File("${file.absoluteFile}$FILE_NAME"))
    }

    fun fileToModel(file: File) {
        val postalCodeList = mutableListOf<PostalCodeModel>()
        viewModelScope.launch(viewModelScope.coroutineContext) {
            csvReader().open(file) {
                readAllWithHeaderAsSequence().forEach { row: Map<String, String> ->
                    postalCodeList.add(
                        PostalCodeModel(
                            localeName = row["nome_localidade"] ?: "",
                            arteryType = row["tipo_arteria"] ?: "",
                            arteryTitle = row["titulo_arteria"] ?: "",
                            arteryName = row["nome_arteria"] ?: "",
                            arteryLocale = row["local_arteria"] ?: "",
                            door = row["porta"] ?: "",
                            client = row["cliente"] ?: "",
                            postalCode = row["num_cod_postal"] ?: "",
                            postalCodeExtension = row["ext_cod_postal"] ?: "",
                            postalCodeComplete = "${row["num_cod_postal"]}-${row["ext_cod_postal"]}",
                            designationPostal = row["desig_postal"] ?: "",
                            normalizedArteryName = row["nome_arteria"]?.normalizeString() ?: ""
                        )
                    )
                }
            }
        }.invokeOnCompletion {
            _fileWasRead.value = postalCodeList
        }
    }

    private fun hasCsv(file: File) = file.exists()

    private fun downloadCsv(file: File) {
        file.mkdirs()
        viewModelScope.launch(viewModelScope.coroutineContext) {
            downloadServiceAPI.downloadCsv(POSTAL_CODE_FILE_URL).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()?.byteStream()
                        if (result != null) {
                            createFile(result, file)
                        } else {
                            _fileState.value = DownloadState.Failure
                        }
                    } else {
                        _fileState.value = DownloadState.Failure
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _fileState.value = DownloadState.Failure
                }
            })
        }
    }

    private fun createFile(inputStream: InputStream, file: File) {
        val finalFile = File("${file.absoluteFile}$FILE_NAME")
        try {
            inputStream.toFile(finalFile.absolutePath)

            _fileState.value = DownloadState.Success(finalFile)
        } catch (e: IOException) {
            _fileState.value = DownloadState.Failure
        }
    }

    private fun InputStream.toFile(path: String) {
        use { input ->
            File(path).outputStream().use { input.copyTo(it) }
        }
    }
}

sealed class DownloadState {
    object Idle : DownloadState()
    object Loading : DownloadState()
    class Success(val data: File) : DownloadState()
    object Failure : DownloadState()
    class HasFile(val data: File) : DownloadState()
}