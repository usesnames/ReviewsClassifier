package com.example.reviewsclassifier

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.chaquo.python.Kwarg
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier


class MainViewModel(application: Application) : AndroidViewModel(application){

    //var result = MutableLiveData<Double>()
    lateinit var reviews: List<String>
    private val context = this.getApplication<Application>()
    //var predictions: MutableList<Float> = mutableListOf()
    var percentuale: MutableLiveData<String> = MutableLiveData("Premi il bottone")

    fun getInsight(){
        val py = Python.getInstance()
        val scraper = py.getModule("google_play_scraper")
        val result = scraper.callAttr("reviews", "com.latuabancaperandroid", Kwarg("count", 100), Kwarg("country", "it"), Kwarg("lang", "it"))
        reviews = result.asList()[0].asList().map { el -> el.asMap()[PyObject.fromJava("content")].toString()}
        //Log.i("last review", reviews[0])
    }

    fun startModel() {
        val classifier = BertNLClassifier.createFromFile(context, "quantized_mb.tflite")
        var positive = 0.0
        var counter = 0
        for(element:String in reviews){
            counter+=1
            this.percentuale.value = "$counter / 100" //non è sul main thread
            Log.e("",percentuale.value!!)
            
            if(classifier.classify(element)[0].score<0.5) {
                positive += 1
            }
        }
        percentuale.value = "$positive%"
    }

    private fun Float.format(digits: Int) = "%.${digits}f".format(this)

}