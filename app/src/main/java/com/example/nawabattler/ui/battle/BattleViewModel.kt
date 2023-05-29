package com.example.nawabattler.ui.battle

import android.content.Context
import androidx.lifecycle.*
import com.example.nawabattler.R
import com.example.nawabattler.ai.Agent
import com.example.nawabattler.data.*
import com.example.nawabattler.structure.Card
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.DeckManager
import com.example.nawabattler.structure.FieldManager
import java.io.File
import java.lang.Integer.max

class BattleViewModel(
    opponentNumber : Int,
    context : Context
) : ViewModel() {

    // 現在グリッドを操作しているか
    private var fieldFlag = false
    // 現在カードを操作しているか
    private var cardFlag = false
    // 選択されたカードの識別番号
    private var selectCardId = -1
    // 選択されたカードの能力(回転させるときなどに一時的に保持するため)
    private var selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0) }
    // 選択されたgrid の座標
    private var selectGridCoordinates = intArrayOf(6, 4)
    // 全ターン数
    val totalTurn = 6
    // 今のターン数
    val nowTurnCount: LiveData<Int>
        get() = _nowTurnCount
    private val _nowTurnCount = MutableLiveData(1)

    // Player1 のスコア
    var player1Score = 0
    // Player2 のスコア
    var player2Score = 0

    // 盤面を生成する
    private val _fieldBase = transConditionArray(standard)
    // ゲーム情報用の盤面
    private val fieldMain = FieldManager(_fieldBase)
    // UIに反映する用の盤面
    val fieldSub = transConditionArray(standard)
    // 更新フラグ
    val updateFlag : LiveData<Boolean>
        get() = _updateFlag
    private  val _updateFlag = MutableLiveData(false)

    val deck1: DeckManager
    val deck2: DeckManager

    private val agent: Agent

    // 内部ファイルにアクセスする
    private val internal = context.filesDir

    // コンストラクタに引数を持たせるための工夫
    class Factory(
        private val context: Context,
        private val opponentNumber: Int
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            BattleViewModel(opponentNumber, context) as T
    }

    init {
        // プレイヤーのデッキを生成する
        val file = File(internal, DECK_CONTENT)
        val bufferedReader = file.bufferedReader()
        val deckCard1 = mutableListOf<Card>()
        val deckCard2 = mutableListOf<Card>()
        bufferedReader.readLines().onEach {
            val cardId = it.toInt()
            deckCard1.add(AllCard[cardId])
        }
        // 選ばれた対戦相手のデッキを生成する
        for (i in 0 until OpponentData[opponentNumber].DeckId.size){
            deckCard2.add(AllCard[OpponentData[opponentNumber].DeckId[i]])
        }
        deck1 = DeckManager(deckCard1)
        deck2 = DeckManager(deckCard2)
        agent = OpponentData[opponentNumber].Agent
    }

    // 盤面をクリックしたとき、その情報を受け渡す
    fun onClickGrid(clickIndex : Int){

        // カードが選択されていなければ何もしない
        if (!cardFlag){
            return
        }

        // クリックしたボタンを座標に変換
        val index = intArrayOf(clickIndex / 10, clickIndex % 10)

        // 確定ボタン以外で盤面をクリックした場合
        if (!( fieldFlag
                    && (index[0] == selectGridCoordinates[0])
                    && (index[1] == selectGridCoordinates[1]) ) ){
            selectGridCoordinates = index
            fieldFlag = true
        }
        // 置ければ置く
        else if ( fieldMain.canSet(index, selectCardRange, Condition.Player1) ){
            play()
        }
        updateField()
        return
    }

    // 画面下のカードをクリックしたとき、その情報を受け渡す
    fun onClickCard(clickButton: Int){

        if (deck1.handCard[clickButton].Image == R.drawable.empty){
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
            selectCardRange = deck1.handCard[selectCardId].Range
            cardFlag = true
            fieldFlag = false
        }
        updateField()
        return
    }

    // 回転ボタンをクリックしたときに実行される関数
    fun onClickRotate(){
        // カードが選択されなければ実行しない
        if (!cardFlag) {
            return
        }
        selectCardRange = rotateRange(selectCardRange)
        updateField()
    }

    // パスボタンをクリックしたときに実行される関数
    fun onClickPass(){
        if (!cardFlag) {
            return
        }
        selectGridCoordinates = intArrayOf(0, 0)
        selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        play()
        updateField()
    }

    // Range を右に90度だけ回転させる
    private fun rotateRange(range: Array<IntArray>): Array<IntArray>{
        val newList = Array(5){ intArrayOf(0, 0, 0, 0, 0) }
        for (i in range.indices){
            for (j in 0 until range[0].size){
                newList[i][j] = range[-j +4][i]
            }
        }
        return newList
    }

    // 自分と相手のカードから盤面を更新するまでの流れ
    private fun play(){
        val computer = agent.play(fieldMain, deck1.handCard, deck2.handCard)
        val myRangeSize = AllCard[selectCardId].cardSize()
        var comRangeSize = 0
        val comCardCoordinates = mutableListOf<IntArray>()
        val wallRange = Array(5){ intArrayOf(0, 0, 0, 0, 0) }

        // カードのキャスト順を求める
        // それぞれのカードの範囲を比較
        for (i in 0 until 5){
            for (j in 0 until 5){
                if (computer.second[i][j] == 1){
                    comRangeSize++
                    val x = i -2 +computer.first[0]
                    val y = j -2 +computer.first[1]
                    comCardCoordinates.add(intArrayOf(x, y))
                }
            }
        }

        for (i in 0 until 5){
            for (j in 0 until 5){
                if (selectCardRange[i][j] == 0){
                    continue
                }
                for ((x, y) in comCardCoordinates){
                    if ((x == i -2 +selectGridCoordinates[0]) && (y == j -2 +selectGridCoordinates[1])){
                        wallRange[i][j] = 1
                    }
                }
            }
        }

        // カードの範囲が大きい順に更新していく
        if (myRangeSize < comRangeSize){
            fieldMain.setColor(computer.first, computer.second, Condition.Player2)
            fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
        } else if (myRangeSize > comRangeSize) {
            fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
            fieldMain.setColor(computer.first, computer.second, Condition.Player2)
        }
        else {
            fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
            fieldMain.setColor(computer.first, computer.second, Condition.Player2)
            fieldMain.setColor(selectGridCoordinates, wallRange, Condition.Wall)
        }

        cardFlag = false
        fieldFlag = false
        deck1.castCard(selectCardId)
        selectGridCoordinates = intArrayOf(6, 4)
        selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        _nowTurnCount.value = _nowTurnCount.value!! +1
    }

    // 実際にゲームが動くときにスコアを変更
    fun updateScore () {
        // まずフィールドの初期化
        player1Score = 0
        player2Score = 0
        // こっちが動けばいい
        for (i in 0 until fieldMain.field.size){
            for (j in 0 until fieldMain.field[0].size) {
                when (fieldMain.field[i][j]) {
                    Condition.Player1 -> {
                        player1Score += 1
                    }
                    Condition.Player2 -> {
                        player2Score += 1
                    }
                    else -> {}
                }
            }
        }
    }

    // 設置予定場所とゲームの情報をリンクさせる
    // ゲーム情報であるfieldMain が変わることはない
    private fun updateField() {
        // 以前仮置きしていたものを一度リセットする
        for (i in 0 until fieldMain.field.size){
            for (j in 0 until fieldMain.field[0].size){
                fieldSub[i][j] = fieldMain.field[i][j]
            }
        }

        // 中心の状態を変更
        if (fieldFlag){
            fieldSub[selectGridCoordinates[0]][selectGridCoordinates[1]] = Condition.TentativeCenterEmpty
        }

        // 仮置きしているものを反映する
        for (i in selectCardRange.indices){
            for (j in 0 until selectCardRange[0].size) {
                if (selectCardRange[i][j] == 0){
                    continue
                }

                val x = selectGridCoordinates[0] + (i -2)
                val y = selectGridCoordinates[1] + (j -2)
                // 範囲外なら何もしない
                if ((x < 0) || (x >= fieldMain.field.size) ||
                    (y < 0) || (y >= fieldMain.field[0].size)){
                    continue
                }

                // カード表示の中心のとき
                if ((i == 2) && (j == 2)){
                    if (fieldMain.field[x][y] == Condition.Empty){
                        fieldSub[x][y] = Condition.TentativeCenterOK
                    } else{
                        fieldSub[x][y] = Condition.TentativeCenterNG
                    }
                }
                // カード表示の中心でないとき
                else{
                    if (fieldMain.field[x][y] == Condition.Empty) {
                        fieldSub[x][y] = Condition.TentativeOK
                    } else {
                        fieldSub[x][y] = Condition.TentativeNG
                    }
                }
            }
        }
        _updateFlag.postValue(true)
    }

    // ゲームが終了した際の処理
    fun gameSet(){
        val file = File(internal, PLAYER_STATICS)
        val playerStatics = arrayOf("", "", "", "", "", "")
        val bufferedReader = file.bufferedReader()
        var t = 0
        bufferedReader.readLines().onEach {
            playerStatics[t] = it
            t++
        }
        if (player1Score > player2Score){
            playerStatics[1] = (playerStatics[1].toInt() +1).toString()
            playerStatics[4] = (playerStatics[4].toInt() +1).toString()
            playerStatics[5] = max(playerStatics[4].toInt(), playerStatics[5].toInt()).toString()
        }
        else if (player1Score < player2Score) {
            playerStatics[2] = (playerStatics[2].toInt() +1).toString()
            playerStatics[5] = "0"
        }
        else{
            playerStatics[3] = (playerStatics[3].toInt() +1).toString()
        }
        val bufferedWriter = file.bufferedWriter()
        playerStatics.onEach{
            bufferedWriter.write(it)
            bufferedWriter.newLine()
        }
        bufferedWriter.close()
    }
}