package com.example.reviewsclassifier

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.anychart.AnyChart
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Pie
import com.chaquo.python.Kwarg
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier


class MainViewModel(application: Application) : AndroidViewModel(application){

    //var result = MutableLiveData<Double>()
    lateinit var reviews: List<String>
    private val context = this.getApplication<Application>()
    //var predictions: MutableList<Float> = mutableListOf()
    var percentuale: MutableLiveData<String> = MutableLiveData("Premi il bottone")
    var lastReview: MutableLiveData<String> = MutableLiveData()
    var buttonVisibility: MutableLiveData<Int> = MutableLiveData(View.VISIBLE)
    var pieReady: MutableLiveData<Boolean> = MutableLiveData(false)
    var pie = AnyChart.pie()

    init{
        Python.start(AndroidPlatform(context))
    }

    fun getInsight(){
        buttonVisibility.value = View.INVISIBLE
        viewModelScope.async(Dispatchers.IO) {
            val py = Python.getInstance()
            val scraper = py.getModule("google_play_scraper")
            val result = scraper.callAttr(
                "reviews", "com.latuabancaperandroid", Kwarg("count", 100), Kwarg(
                    "country",
                    "it"
                ), Kwarg("lang", "it")
            )
            reviews = result.asList()[0].asList().map { el -> el.asMap()[PyObject.fromJava("content")].toString() }
            startModel()
        }
    }

    private suspend fun startModel() {
        return withContext(Dispatchers.IO) {
            val classifier = BertNLClassifier.createFromFile(context, "quantized_mb.tflite")
            var positive = 0.0
            var counter = 0
            for (element: String in reviews) {
                counter += 1
                if(counter==1){
                    if (classifier.classify(element)[0].score < 0.5)
                        lastReview.postValue("Ultima review: " + reviews[0] + "\n\nclassificazione: positiva")
                    else
                        lastReview.postValue("Ultima review: " + reviews[0] + "\n\nclassificazione: negativa")
                }
                percentuale.postValue("avanzamento: $counter / 100")
                Log.e("", percentuale.value!!)

                if (classifier.classify(element)[0].score < 0.5) {
                    positive += 1
                }
            }

            val data: MutableList<DataEntry> = ArrayList()
            data.add(ValueDataEntry("Positive", positive))
            data.add(ValueDataEntry("Negative", 100-positive))
            pie.background().fill("#C9D2D3");
            pie.data(data)
            pie.legend().enabled(true)
            pieReady.postValue(true)
            percentuale.postValue("$positive% di recensioni positive")
            buttonVisibility.postValue(View.VISIBLE)
        }
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

}