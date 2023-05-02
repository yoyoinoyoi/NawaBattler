package com.example.nawabattler.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import com.example.nawabattler.CustomDialog
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.data.PLAYER_STATICS
import com.example.nawabattler.databinding.FragmentHomeBinding
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var playerStatics = arrayOf("", "", "", "", "", "")

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root


        val internal = requireContext().filesDir
        // デッキはdeckContent ファイルにid として記載されている
        val file = File(internal, PLAYER_STATICS)

        // ファイルが無ければ作成する
        if (!file.exists()){
            val bufferedWriter = file.bufferedWriter()
            val fileContent = "player\n0\n0\n0\n0\n0"
            bufferedWriter.write(fileContent)
            bufferedWriter.close()
        }

        val bufferedReader = file.bufferedReader()
        var t = 0
        bufferedReader.readLines().forEach {
            // ファイルに記載されているid を一時データに保存
            playerStatics[t] = it
            t++
        }

        binding.editTextTextPersonName.hint = playerStatics[0]
        binding.playerWinLossRecordNumber.text = "${playerStatics[1]} 勝 ${playerStatics[2]} 敗 ${playerStatics[3]} 分"
        binding.playerMaxWinStreakNumber.text = "${playerStatics[4]} 連勝"
        binding.playerNowWinStreakNumber.text = "${playerStatics[5]} 連勝"

        // playerName を変更する
        binding.editTextTextPersonName
            .setOnEditorActionListener{ editText, action, _ ->
                if (action == EditorInfo.IME_ACTION_NEXT){
                    editText.text.toString().let {
                        playerStatics[0] = editText.text.toString()
                        val bufferedWriter = file.bufferedWriter()
                        playerStatics.onEach(){
                            bufferedWriter.write(it)
                            bufferedWriter.newLine()
                        }
                        bufferedWriter.close()
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }
        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
