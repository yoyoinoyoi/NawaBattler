package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nawabattler.*
import com.example.nawabattler.databinding.FragmentBattleBinding
import com.example.nawabattler.structure.Condition
import java.io.File
import kotlin.math.max
import kotlin.math.roundToInt

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private val args: BattleFragmentArgs by navArgs()

    private val battleViewModel: BattleViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        val view = binding.root

        val battleViewModel = BattleViewModel(args.opponentNumber, requireContext())
        // まずはボタンを生成
        val column = 12
        val row = 10
        for (i in 0 until column * row) {
            // GridLayoutを使用するので、rowとcolumnを指定
            val dp = resources.displayMetrics.density
            val params = GridLayout.LayoutParams().also {
                it.rowSpec = GridLayout.spec(i / row)
                it.columnSpec = GridLayout.spec(i % row)
                it.width = (40 * dp).roundToInt()
                it.height = (40 * dp).roundToInt()
            }
            val imageButton = ImageButton(requireContext()).also {
                it.layoutParams = params
                it.setBackgroundResource(R.drawable.gray)
            }
            binding.fieldGrid.addView(imageButton)
        }

        // そのボタンにクリックイベントを付与する
        for (i in 0 until binding.fieldGrid.childCount) {
            val v = binding.fieldGrid.getChildAt(i)
            v.setOnClickListener {
                battleViewModel.onClickGrid(i)
                preview()
            }
        }

        // デッキファイルを読み込んで生成
        battleViewModel.readDeckFile()

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        binding.cardbutton1.setOnClickListener {
            battleViewModel.onClickCard(0)
            preview()
        }
        binding.cardbutton2.setOnClickListener {
            battleViewModel.onClickCard(1)
            preview()
        }
        binding.cardbutton3.setOnClickListener {
            battleViewModel.onClickCard(2)
            preview()
        }
        binding.rotatebutton.setOnClickListener {
            battleViewModel.onClickRotate()
            preview()
        }
        binding.passbutton.setOnClickListener {
            battleViewModel.onClickPass()
            preview()
        }

        battleViewModel.fieldMain.observe(viewLifecycleOwner, Observer{
            updateField()
        })

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // デッキからカードをランダムで選んで設置する
    private fun setCard (imageList: MutableList<Int>) {
        // 枚数が足りない場合にはwhite で対応する
        binding.cardbutton1.setBackgroundResource(imageList[0])
        binding.cardbutton2.setBackgroundResource(imageList[1])
        binding.cardbutton3.setBackgroundResource(imageList[2])
    }

    // fieldMain からフロントへの更新を行う
    private fun updateField () {

        val row = 10

        // こっちが動けばいい
        for (i in 0 until binding.fieldGrid.childCount) {
            val fieldRow = i / row
            val fieldColumn = i % row
            val v = binding.fieldGrid.getChildAt(i)
            when (battleViewModel.fieldMain.value!!.field[fieldRow][fieldColumn]) {
                Condition.Player1 -> {
                    v.setBackgroundResource(R.drawable.blue)
                }
                Condition.Player2 -> {
                    v.setBackgroundResource(R.drawable.yellow)
                }
                else -> {
                    v.setBackgroundResource(R.drawable.gray)
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
     private fun viewUpdate(){
        binding.score.text = "$battleViewModel.player1Score.value!! vs $battleViewModel.player2Score.value!!"
        // 通常のTurn の更新
        if (battleViewModel.nowTurnCount.value!! < battleViewModel.totalTurn.value!!){
            binding.Turn.text = "Turn $battleViewModel.nowTurnCount.value!! / $battleViewModel.totalTurn.value!!"
        }
        // ゲーム終了なら
        else if (battleViewModel.nowTurnCount.value!! > battleViewModel.totalTurn.value!!){
            val playerStatics = arrayOf("","","","","")

            val internal = requireContext().filesDir
            // デッキはdeckContent ファイルにid として記載されている
            val file = File(internal, "playerStatics")

            val bufferedReader = file.bufferedReader()
            var t = 0
            bufferedReader.readLines().forEach {
                // ファイルに記載されているid を一時データに保存
                playerStatics[t] = it
                t++
            }

            val resultText : String
            if (battleViewModel.player1Score.value!! > battleViewModel.player2Score.value!!){
                resultText = "YOU WIN!!"
                playerStatics[1] = (playerStatics[1].toInt() +1).toString()
                playerStatics[4] = (playerStatics[4].toInt() +1).toString()
                playerStatics[3] = max(playerStatics[3].toInt(), playerStatics[4].toInt()).toString()
            } else if (battleViewModel.player1Score.value!! < battleViewModel.player2Score.value!!){
                resultText = "YOU LOSE..."
                playerStatics[2] = (playerStatics[2].toInt() +1).toString()
                playerStatics[4] = "0"
            } else{
                resultText = "DRAW"
            }

            val bufferedWriter = file.bufferedWriter()
            playerStatics.forEach(){
                println(it)
                bufferedWriter.write(it)
                bufferedWriter.newLine()
            }
            bufferedWriter.close()

            // 結果をダイアログで表示
            CustomDialog.Builder(this)
                .setTitle(resultText)
                .setMessage("$battleViewModel.player1Score.value!! vs $battleViewModel.player2Score.value!!")
                .setPositiveButton("OK") {
                    val action = BattleFragmentDirections.actionBattleFragmentToHomeFragment()
                    findNavController().navigate(action)
                }
                .build()
                .show(childFragmentManager, CustomDialog::class.simpleName)

        }
        else{
            binding.Turn.text = "Final Turn!"
        }
        battleViewModel.incrementNowTurn()
    }

    // プレビューを表示する
    private fun preview(){

        updateField()
        // 中央部をまず表示(中央部がかぶっている処理は後ろで行う)
        val coreImage = binding.fieldGrid.getChildAt (
            10 * battleViewModel.selectGridCoordinates.value!![0] +battleViewModel.selectGridCoordinates.value!![1]
        )
        coreImage.setBackgroundResource(R.drawable.tentative_core)

        // カードの範囲から座標にまず変換する
        for (i in battleViewModel.selectCardRange.value!!.indices){
            for (j in 0 until battleViewModel.selectCardRange.value!![0].size){
                if (battleViewModel.selectCardRange.value!![i][j] == 1){

                    val x = battleViewModel.selectGridCoordinates.value!![0] + (i -2)
                    val y = battleViewModel.selectGridCoordinates.value!![1] + (j -2)
                    // 範囲外なら何もしない
                    if ((x < 0) || (x >= battleViewModel.fieldMain.value!!.field.size)){
                        continue
                    }
                    if ((y < 0) || (y >= battleViewModel.fieldMain.value!!.field[0].size)){
                        continue
                    }

                    val myImage = binding.fieldGrid.getChildAt (10 * x + y)

                    if ((i == 2) && (j == 2)){
                        if (battleViewModel.fieldMain.value!!.field[x][y] == Condition.Empty){
                            myImage.setBackgroundResource(R.drawable.tentative_blue_core)
                        }
                        //置けないなら灰色
                        else{
                            myImage.setBackgroundResource(R.drawable.tentative_gray_core)
                        }
                    }
                    else{

                        // 置けるのであれば水色
                        if (battleViewModel.fieldMain.value!!.field[x][y] == Condition.Empty) {
                            myImage.setBackgroundResource(R.drawable.tentative_blue)
                        }
                        //置けないなら灰色
                        else {
                            myImage.setBackgroundResource(R.drawable.tentative_gray)
                        }
                    }
                }
            }
        }
    }


}
