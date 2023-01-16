package com.example.nawabattler

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.nawabattler.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 上部のアクションバーを非表示にする
        val actionBar: ActionBar? = supportActionBar
        actionBar?.hide()

        // 下のナビゲーションに関する記述
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment, R.id.lobbyFragment, R.id.deckFragment
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // 特定のFragment の時にはナビゲーションを無効にする
        navController.addOnDestinationChangedListener { _, destination, _ ->
            navView.visibility = if(destination.id == R.id.battleFragment) View.GONE else View.VISIBLE
        }
//
//        // プレイヤーの情報を初期化する(まだ作成していない場合)
//        val internal = applicationContext.filesDir
//        // デッキはdeckContent ファイルにid として記載されている
//        val file = File(internal, "playerStatics")
//
//        // ファイルが無ければ作成する
//        if (!file.exists()){
//            val bufferedWriter = file.bufferedWriter()
//            val fileContent = "player\n0\n0\n0\n0"
//            bufferedWriter.write(fileContent)
//            bufferedWriter.close()
//        }

    }
}