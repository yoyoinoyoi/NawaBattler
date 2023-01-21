package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.Navigation.findNavController
import com.example.nawabattler.BattleCustomDialog
import com.example.nawabattler.R
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.data.OpponentData
import com.example.nawabattler.databinding.FragmentBattleBinding
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.DeckManager
import com.example.nawabattler.structure.FieldManager
import java.io.File
import kotlin.math.max

@SuppressLint("StaticFieldLeak")
class BattleViewModel(
    private val opponentNumber: String,
    val context: Context
) : ViewModel() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    // 現在グリッドを操作しているか
    private var fieldFlag = false
    // 現在カードを操作しているか
    private var cardFlag = false
    // 選択されたカードの識別番号
    private var selectCardId = -1

    private val deck1 = DeckManager()
    private val deck2 = DeckManager()

    // 選択されたカードの能力(回転させるときなどに一時的に保持するため)
    private var selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0) }
    // 選択されたgrid の座標
    private val _selectGridCoordinates = MutableLiveData<IntArray>(intArrayOf(6, 4))
    val selectGridCoordinates : LiveData<IntArray> get() = _selectGridCoordinates
    // 全ターン数
    private val totalTurn = 5
    // 今のターン数
    private val _nowTurnCount = MutableLiveData(1)
    val nowTurnCount : LiveData<Int> get() = _nowTurnCount
    // Player1 のスコア
    private var player1Score = 0
    // Player2 のスコア
    private var player2Score = 0
    private var fieldBase = Array(12){ Array(10){ Condition.Empty } }
    private val fieldMain = FieldManager(fieldBase)

    fun readDeckFile(){

        // プレイヤーのデッキを生成する
        val internal = context.filesDir
        val file = File(internal, "deckContent")
        // ファイルにかかれたカードの画像を表示する
        val bufferedReader = file.bufferedReader()

        bufferedReader.readLines().forEach {
            val cardId = it.toInt()
            deck1.deck.add(AllCard[cardId])
        }

        // 選ばれた対戦相手のデッキを生成する

        for (i in 0 until OpponentData[opponentNumber.toInt()].DeckId.size){
            deck2.deck.add(AllCard[OpponentData[opponentNumber.toInt()].DeckId[i]])
        }
        // フロントへ更新
        deck1.deckSetUp()
        deck2.deckSetUp()

    }

    fun onClickGrid(clickIndex : Int){

        // カードが選択されていなければ何もしない
        if (!cardFlag){
            return
        }

        // クリックしたボタンを座標に変換
        val index = intArrayOf(clickIndex / 10, clickIndex % 10)

        // プレビューを表示する
        if ( !( fieldFlag && (index[0] == _selectGridCoordinates.value!![0]) && (index[1] == _selectGridCoordinates.value!![1]) ) ){

            _selectGridCoordinates.value = index
            fieldFlag = true
            // preview()
            return
        }

        // 置ければ置く
        if ( fieldMain.canSet(index, selectCardRange, Condition.Player1) ){
            play()
        }
    }

    // 画面下のカードをクリックしたとき、その情報を受け渡す
    fun onClickCard(clickButton: Int){

        if (deck1.handCard[clickButton] == -1){
            // カードが何もない時には何もしない
            cardFlag = false
            selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}

        } else if (cardFlag && (clickButton == selectCardId)){
            // 同じカードを連続でクリックした場合にはキャンセルする
            cardFlag = false
            selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}

        } else {
            // 正常に受け渡す
            selectCardId = clickButton
            selectCardRange = deck1.deck[deck1.handCard[selectCardId]].Range
            cardFlag = true
            fieldFlag = false

        }
        return
    }

    // 回転ボタンをクリックしたときに実行される関数
    fun onClickRotate(){
        // カードが選択されなければ実行しない
        if (!cardFlag) {
            return
        }
        selectCardRange = rotateRange(selectCardRange)
    }

    // パスボタンをクリックしたときに実行される関数
    fun onClickPass(){
        if (!cardFlag) {
            return
        }
        _selectGridCoordinates.value = intArrayOf(0, 0)
        selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        play()
    }

    // Range を右に90度だけ回転させる
    // Range: 5x5 の配列
    private fun rotateRange(range: Array<IntArray>): Array<IntArray>{
        val newList = Array(5){ intArrayOf(0, 0, 0, 0, 0) }
        for (i in range.indices){
            for (j in 0 until range[0].size){
                newList[i][j] = range[j][range[0].size -i -1]
            }
        }
        return newList
    }

    // コンピュータが実行するとき
    private fun computerTurn(): Triple<IntArray, Array<IntArray>, Condition>{
        // deck はdeckField2 を用いる

        // 手札からまずカードを決める
        for (choiceCardId in 0 until deck2.handCard.size){
            // 回転させる
            val choiceCard = deck2.handCard[choiceCardId]
            if (choiceCard == -1){
                continue
            }
            val choiceRange1 = deck2.deck[choiceCard].Range
            val choiceRange2 = rotateRange(choiceRange1)
            val choiceRange3 = rotateRange(choiceRange2)
            val choiceRange4 = rotateRange(choiceRange3)

            val candidates = mutableListOf<IntArray>()
            val ranges = mutableListOf<Array<IntArray>>()
            for (i in 0 until fieldMain.field.size){
                for (j in 0 until fieldMain.field[0].size){
                    if (fieldMain.canSet(intArrayOf(i, j), choiceRange1, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange1)
                    }
                    if (fieldMain.canSet(intArrayOf(i, j), choiceRange2, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange2)
                    }
                    if (fieldMain.canSet(intArrayOf(i, j), choiceRange3, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange3)
                    }
                    if (fieldMain.canSet(intArrayOf(i, j), choiceRange4, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange4)
                    }
                }
            }
            if (candidates.isNotEmpty()){
                val randomNum = (candidates.indices).random()
                deck2.deckDraw(choiceCardId)
                return Triple(candidates[randomNum], ranges[randomNum], Condition.Player2)
            }
        }

        // 置けなくなったら適当に選んで捨てる
        for (choiceCardId in 0 until deck2.handCard.size){
            val choiceCard = deck2.handCard[choiceCardId]
            if (choiceCard != -1){
                deck2.deckDraw(choiceCardId)
                return Triple(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)},
                    Condition.Player2
                )
            }
        }

        // たぶんここまでいかない
        return Triple(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)}, Condition.Player2)
    }

    // 自分と相手のカードから盤面を更新するまでの流れ
    private fun play(){
        val computer = computerTurn()
        var myRangeSize = 0
        var comRangeSize = 0

        // カードのキャスト順を求める
        // それぞれのカードの範囲を比較
        for (i in 0 until 5){
            for (j in 0 until 5){
                if (selectCardRange[i][j] == 1){
                    myRangeSize++
                }
                if (computer.second[i][j] == 1){
                    comRangeSize++
                }
            }
        }
        val rv = Math.random()
        val playOrder =
            if (myRangeSize < comRangeSize){
                true
            } else if (myRangeSize > comRangeSize){
                false
            } else{
                100 * rv > 50
            }

        // カードの範囲が大きい順に更新していく
        if (playOrder){
            fieldMain.setColor(computer.first, computer.second, computer.third)
            fieldMain.setColor(_selectGridCoordinates.value!!, selectCardRange, Condition.Player1)
        } else {
            fieldMain.setColor(_selectGridCoordinates.value!!, selectCardRange, Condition.Player1)
            fieldMain.setColor(computer.first, computer.second, computer.third)
        }

        cardFlag = false
        fieldFlag = false
        deck1.deckDraw(selectCardId)
        _selectGridCoordinates.value = intArrayOf(6, 4)
        selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        _nowTurnCount.value = nowTurnCount.value!! +1
    }

    /*
     * 表示
     */


    // fieldMain からフロントへの更新を行う
    @SuppressLint("SetTextI18n")
    fun updateGame () {

        // まずフィールドの初期化
        player1Score = 0
        player2Score = 0
        val row = 10

        // こっちが動けばいい
        for (i in 0 until binding.fieldGrid.childCount) {
            val fieldRow = i / row
            val fieldColumn = i % row
            val v = binding.fieldGrid.getChildAt(i)
            when(fieldMain.field[fieldRow][fieldColumn]){
                Condition.Player1 -> {
                    v.setBackgroundResource(R.drawable.blue)
                    player1Score++
                }
                Condition.Player2 -> {
                    v.setBackgroundResource(R.drawable.yellow)
                    player2Score++
                }
                else -> {
                    v.setBackgroundResource(R.drawable.gray)
                }
            }
        }

        binding.score.text = "$player1Score vs $player2Score"
        // 通常のTurn の更新
        if (_nowTurnCount.value!! < totalTurn){
            binding.Turn.text = "Turn $_nowTurnCount.value!! / $totalTurn"
        }
        // ゲーム終了なら
        else if (_nowTurnCount.value!! > totalTurn){
            afterGameSet()
        }
        else{
            binding.Turn.text = "Final Turn!"
        }
        setCard(deck1.deckImageList())
    }

    // デッキからカードをランダムで選んで設置する
    private fun setCard (imageList: MutableList<Int>) {
        // 枚数が足りない場合にはwhite で対応する
        binding.cardbutton1.setBackgroundResource(imageList[0])
        binding.cardbutton2.setBackgroundResource(imageList[1])
        binding.cardbutton3.setBackgroundResource(imageList[2])
    }

    // プレビューを表示する
    fun preview(){

        for (i in 0 until fieldMain.field.size){
            for (j in 0 until fieldMain.field[0].size) {
                val v = binding.fieldGrid.getChildAt(10 * i +j)
                when (fieldMain.field[i][j]) {
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

        // 中央部をまず表示(中央部がかぶっている処理は後ろで行う)
        if (cardFlag) {
            val coreImage = binding.fieldGrid.getChildAt(10 * _selectGridCoordinates.value!![0] + _selectGridCoordinates.value!![1])
            coreImage.setBackgroundResource(R.drawable.tentative_core)
        }

        // カードの範囲から座標にまず変換する
        for (i in selectCardRange.indices){
            for (j in 0 until selectCardRange[0].size){
                if (selectCardRange[i][j] == 1){

                    val x = _selectGridCoordinates.value!![0] + (i -2)
                    val y = _selectGridCoordinates.value!![1] + (j -2)
                    // 範囲外なら何もしない
                    if ((x < 0) || (x >= fieldMain.field.size)){
                        continue
                    }
                    if ((y < 0) || (y >= fieldMain.field[0].size)){
                        continue
                    }

                    val myImage = binding.fieldGrid.getChildAt (10 * x + y)

                    if ((i == 2) && (j == 2)){
                        if (fieldMain.field[x][y] == Condition.Empty){
                            myImage.setBackgroundResource(R.drawable.tentative_blue_core)
                        }
                        //置けないなら灰色
                        else{
                            myImage.setBackgroundResource(R.drawable.tentative_gray_core)
                        }
                    }
                    else{

                        // 置けるのであれば水色
                        if (fieldMain.field[x][y] == Condition.Empty) {
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

    // ゲームが終了したときに実行する関数
    private fun afterGameSet(){

        val playerStatics = arrayOf("","","","","")

        val internal = context.filesDir
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
        if (player1Score > player2Score){
            resultText = "YOU WIN!!"
            playerStatics[1] = (playerStatics[1].toInt() +1).toString()
            playerStatics[4] = (playerStatics[4].toInt() +1).toString()
            playerStatics[3] = max(playerStatics[3].toInt(), playerStatics[4].toInt()).toString()
        } else if (player1Score < player2Score){
            resultText = "YOU LOSE..."
            playerStatics[2] = (playerStatics[2].toInt() +1).toString()
            playerStatics[4] = "0"
        } else{
            resultText = "DRAW"
        }

        val bufferedWriter = file.bufferedWriter()
        playerStatics.forEach{
            println(it)
            bufferedWriter.write(it)
            bufferedWriter.newLine()
        }
        bufferedWriter.close()
//
//        // 結果をダイアログで表示
//        BattleCustomDialog.Builder(this)
//            .setTitle(resultText)
//            .setMessage("$player1Score vs $player2Score")
//            .setPositiveButton("OK") {
//                val action = BattleFragmentDirections.actionBattleFragmentToHomeFragment()
//                findNavController().navigate(action)
//            }
//            .build()
//            .show(childFragmentManager, BattleCustomDialog::class.simpleName)

    }

}