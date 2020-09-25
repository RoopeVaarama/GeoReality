package com.example.georeality

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_entity.*

class EntityActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entity)
        saveButton.setOnClickListener(doneAction())

    }

    private fun doneAction() = View.OnClickListener {
        val intent = Intent(this, MainActivity::class.java).apply {

        }
        startActivity(intent)
    }
}