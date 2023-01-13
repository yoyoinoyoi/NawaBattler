package com.example.nawabattler

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.example.nawabattler.databinding.ActivityBattleBinding

class BattleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

    }
}