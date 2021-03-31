package com.example.reviewsclassifier

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        Python.start(AndroidPlatform(this))

        viewModel.percentuale.observe(this, Observer{
            findViewById<TextView>(R.id.main_text).text = it
        })

        viewModel.lastReview.observe(this, Observer{
            findViewById<TextView>(R.id.last_review).text = it
        })

        viewModel.buttonVisibility.observe(this, Observer{
            findViewById<Button>(R.id.button).visibility = it
        })

        findViewById<Button>(R.id.button).setOnClickListener {
            viewModel.getInsight()
        }

    }
}