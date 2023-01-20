package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.nawabattler.R
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.data.OpponentData
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.DeckManager
import com.example.nawabattler.structure.FieldManager
import java.io.File

@SuppressLint("StaticFieldLeak")
class BattleViewModel(
    private val opponentNumber: String,
    val context: Context
    ) : ViewModel() {

    // 現在グリッドを操作しているか
    private var fieldFlag = false
    // 現在カードを操作しているか
    private var cardFlag = false
    // 選択されたカードの識別番号
    private var selectCardId = -1

    private var fieldBase = Array(12){ Array(10){ Condition.Empty } }
    private val deck1 = DeckManager()
    private val deck2 = DeckManager()

    // Fragment でも使う変数

    // 選択されたカードの能力(回転させるときなどに一時的に保持するため)
    private var _selectCardRange = MutableLiveData(Array(5){ intArrayOf(0, 0, 0, 0, 0) })
    val selectCardRange: LiveData<Array<IntArray>>get() = _selectCardRange
    // 選択されたgrid の座標
    private var _selectGridCoordinates = MutableLiveData(intArrayOf(6, 4))
    val selectGridCoordinates: LiveData<IntArray> get() = _selectGridCoordinates
    // 全ターン数
    private val _totalTurn = MutableLiveData(5)
    val totalTurn: LiveData<Int> get() = _totalTurn
    // 今のターン数
    private var _nowTurnCount = MutableLiveData(1)
    val nowTurnCount: LiveData<Int> get() = _nowTurnCount
    // Player1 のスコア
    private var _player1Score = MutableLiveData(0)
    val player1Score: LiveData<Int> get() = _player1Score
    // Player2 のスコア
    private var _player2Score = MutableLiveData(0)
    val player2Score: LiveData<Int> get() = _player2Score
    private val _fieldMain = MutableLiveData(FieldManager(fieldBase))
    val fieldMain: LiveData<FieldManager> get() = _fieldMain

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
        if ( fieldMain.value!!.canSet(index, _selectCardRange.value!!, Condition.Player1) ){
            play()
//            updateField(fieldMain.field)
//            viewUpdate()
//            setCard(deck1.deckImageList())
        }
    }

    // 画面下のカードをクリックしたとき、その情報を受け渡す
    fun onClickCard(clickButton: Int){

        if (deck1.handCard[clickButton] == -1){
            // カードが何もない時には何もしない
            cardFlag = false
            _selectCardRange.value = Array(5){ intArrayOf(0, 0, 0, 0, 0)}

        } else if (cardFlag && (clickButton == selectCardId)){
            // 同じカードを連続でクリックした場合にはキャンセルする
            cardFlag = false
            _selectCardRange.value = Array(5){ intArrayOf(0, 0, 0, 0, 0)}

        } else {
            // 正常に受け渡す
            selectCardId = clickButton
            _selectCardRange.value = deck1.deck[deck1.handCard[selectCardId]].Range
            cardFlag = true
            fieldFlag = false

        }
        // preview()
        return
    }

    // 回転ボタンをクリックしたときに実行される関数
    fun onClickRotate(){
        // カードが選択されなければ実行しない
        if (!cardFlag) {
            return
        }
        _selectCardRange.value = rotateRange(selectCardRange.value!!)
        // preview()
    }

    // パスボタンをクリックしたときに実行される関数
    fun onClickPass(){
        if (!cardFlag) {
            return
        }
        _selectGridCoordinates.value = intArrayOf(0, 0)
        _selectCardRange.value = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        play()
//        setCard(deck1.deckImageList())
//        updateField(fieldMain.field)
//        viewUpdate()
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

    // スコアを数え上げるための関数
    fun countPlayerScore () {

        for (i in 0 until fieldMain.value!!.field.size) {
            for (j in 0 until fieldMain.value!!.field[0].size) {
                when (fieldMain.value!!.field[i][j]) {
                    Condition.Player1 -> {
                        _player1Score.value = player1Score.value!! +1
                    }
                    Condition.Player2 -> {
                        _player2Score.value = player2Score.value!! +1
                    }
                    else -> {
                        // pass
                    }
                }
            }
        }
    }

    // ターンの更新
    fun incrementNowTurn(){
        _nowTurnCount.value = nowTurnCount.value!! +1
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
            for (i in 0 until fieldMain.value!!.field.size){
                for (j in 0 until fieldMain.value!!.field[0].size){
                    if (fieldMain.value!!.canSet(intArrayOf(i, j), choiceRange1, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange1)
                    }
                    if (fieldMain.value!!.canSet(intArrayOf(i, j), choiceRange2, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange2)
                    }
                    if (fieldMain.value!!.canSet(intArrayOf(i, j), choiceRange3, Condition.Player2)){
                        candidates.add(intArrayOf(i, j))
                        ranges.add(choiceRange3)
                    }
                    if (fieldMain.value!!.canSet(intArrayOf(i, j), choiceRange4, Condition.Player2)){
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
        // それぞれのカードの範囲を比較
        for (i in 0 until 5){
            for (j in 0 until 5){
                if (_selectCardRange.value!![i][j] == 1){
                    myRangeSize++
                }
                if (computer.second[i][j] == 1){
                    comRangeSize++
                }
            }
        }

        val rv = Math.random()
        // あなたのキャスト順を求める
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
            fieldMain.value!!.setColor(computer.first, computer.second, computer.third)
            fieldMain.value!!.setColor(_selectGridCoordinates.value!!, _selectCardRange.value!!, Condition.Player1)
        } else {
            fieldMain.value!!.setColor(_selectGridCoordinates.value!!, _selectCardRange.value!!, Condition.Player1)
            fieldMain.value!!.setColor(computer.first, computer.second, computer.third)
        }

        cardFlag = false
        fieldFlag = false
        deck1.deckDraw(selectCardId)
        _selectGridCoordinates.value = intArrayOf(6, 4)
        _selectCardRange.value = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
    }


}