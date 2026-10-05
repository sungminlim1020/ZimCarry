package com.example.zimcarry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔹 검사 시작 버튼 → CameraActivity
        val btnStart = findViewById<Button>(R.id.btnStartCamera)
        btnStart.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }

        // 🔹 기능 1 카드 → Feature1Activity
        findViewById<MaterialCardView>(R.id.cardFeature1).setOnClickListener {
            startActivity(Intent(this, Feature1Activity::class.java))
        }

        // 🔹 기능 2 카드 → Feature2Activity
        findViewById<MaterialCardView>(R.id.cardFeature2).setOnClickListener {
            startActivity(Intent(this, Feature2Activity::class.java))
        }

        // 🔹 기능 3 카드 → Feature3Activity
        findViewById<MaterialCardView>(R.id.cardFeature3).setOnClickListener {
            startActivity(Intent(this, Feature3Activity::class.java))
        }
    }
}
