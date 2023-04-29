package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.*
import com.example.nawabattler.data.*
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.DeckManager
import com.example.nawabattler.structure.FieldManager
import java.io.File

@SuppressLint("StaticFieldLeak")
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
    val totalTurn = 5
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

    val deck1 = DeckManager()
    private val deck2 = DeckManager()

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
        val internal = context.filesDir
        val file = File(internal, DECK_CONTENT)
        // ファイルにかかれたカードの画像を表示する
        val bufferedReader = file.bufferedReader()
        bufferedReader.readLines().onEach {
            val cardId = it.toInt()
            deck1.addCard(AllCard[cardId])
        }
        // 選ばれた対戦相手のデッキを生成する
        for (i in 0 until OpponentData[opponentNumber].DeckId.size){
            deck2.addCard(AllCard[OpponentData[opponentNumber].DeckId[i]])
        }
        // フロントへ更新
        deck1.setUp()
        deck2.setUp()
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
        if ( !( fieldFlag && (index[0] == selectGridCoordinates[0]) && (index[1] == selectGridCoordinates[1]) ) ){

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
            selectCardRange = deck1.handCard(selectCardId).Range
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
    private fun computerTurn(): Pair<IntArray, Array<IntArray>>{
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
                deck2.castCard(choiceCardId)
                return Pair(candidates[randomNum], ranges[randomNum])
            }
        }

        // 置けなくなったら適当に選んで捨てる
        for (choiceCardId in 0 until deck2.handCard.size){
            val choiceCard = deck2.handCard[choiceCardId]
            if (choiceCard != -1){
                deck2.castCard(choiceCardId)
                return Pair(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)})
            }
        }

        // たぶんここまでいかない
        return Pair(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)})
    }

    // 自分と相手のカードから盤面を更新するまでの流れ
    private fun play(){
        val computer = computerTurn()
        var myRangeSize = 0
        var comRangeSize = 0
        val comCardCoordinates = mutableListOf<IntArray>()
        val wallRange = Array(5){ intArrayOf(0, 0, 0, 0, 0) }

        // カードのキャスト順を求める
        // それぞれのカードの範囲を比較
        for (i in 0 until 5){
            for (j in 0 until 5){
                if (selectCardRange[i][j] == 1){
                    myRangeSize++
                }
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
}