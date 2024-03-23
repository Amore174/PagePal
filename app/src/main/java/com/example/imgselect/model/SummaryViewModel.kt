package com.example.imgselect.model

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.imgselect.data.LocalStorageDatabase
import com.example.imgselect.data.LocalStorageRepository
import com.example.imgselect.data.Summary
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Calendar


class SummaryViewModel(application: Application): AndroidViewModel(application) {

    private val _uiState: MutableStateFlow<DiscussUiState> = MutableStateFlow(DiscussUiState.Initial)
    val uiState=_uiState.asStateFlow()
    private var generativeModel: GenerativeModel
    var output: String by mutableStateOf("")

    private val readAllSummary: LiveData<List<Summary>>
    private val repository: LocalStorageRepository

    var dialogVisible: Boolean by mutableStateOf(false)
    var title: String by mutableStateOf("")
    init{
        val config= generationConfig {temperature=0.70f  }
        generativeModel= GenerativeModel(
            modelName = "gemini-pro",
            apiKey = "AIzaSyAOgrCj3x9WE8JoSDB5yuoGqH7m4Rn0IWI",
            generationConfig = config
        )

        val localStorageDao = LocalStorageDatabase.getDatabase(application).userDao()
        repository = LocalStorageRepository(localStorageDao)
        readAllSummary = repository.readAllSummary
    }
    fun questioning(userInput:String){
        _uiState.value= DiscussUiState.Loading
        val prompt=userInput
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val content= content{

                    text(prompt)
                }
                //var output=""
                output+= generativeModel.generateContent(content).text
                _uiState.value = DiscussUiState.Success(output)
                Log.d("MainActivity","Response done")

//                generativeModel.generateContentStream(content).collect{
//output+=it.text
//                    _uiState.value =HomeUiState.Success(output)
//                }
                Log.d("MainActivity",output)

            }catch (e: Exception){
                _uiState.value=
                    DiscussUiState.Error(e.localizedMessage ?: "Error in Generating content")
                Log.d("MainActivity",e.localizedMessage)
            }

        }

    }


    fun saveSummaryWithImage(image: Bitmap? , title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd")
            val date = dateFormat.format(current.time)
            val imageByte = image?.toBytes()
            val content = Summary(0,output,imageByte , "${date}" , title)
            repository.addSummary(content)
        }
    }

    fun deleteSummary(summary: Summary) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSummary(summary)
        }
    }

    fun Bitmap.toBytes() : ByteArray {
        ByteArrayOutputStream().apply {
            compress(Bitmap.CompressFormat.PNG, 100 , this)
            return toByteArray()
        }
    }

    fun getSummaryList() : LiveData<List<Summary>> {
        val summaryList = repository.readAllSummary
        return summaryList
    }
}
sealed interface DiscussUiState{
    object Initial: DiscussUiState
    object Loading: DiscussUiState
    data class Success(val outputText:String): DiscussUiState
    data class Error(val error:String): DiscussUiState

}