package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nawabattler.*
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.data.OpponentData
import com.example.nawabattler.databinding.FragmentBattleBinding
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.DeckManager
import com.example.nawabattler.structure.FieldManager
import java.io.File
import kotlin.math.roundToInt

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private val args: BattleFragmentArgs by navArgs()

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
    private val totalTurn = 5
    // 今のターン数
    private var nowTurnCount = 1
    // Player1 のスコア
    private var player1Score = 0
    // Player2 のスコア
    private var player2Score = 0

    private var fieldBase = Array(12){ Array(10){ Condition.Empty } }
    private val fieldMain = FieldManager(fieldBase)
    private val deck1 = DeckManager()
    private val deck2 = DeckManager()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        val view = binding.root

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
            v.setOnClickListener { onClickGrid(i) }
        }

        // プレイヤーのデッキを生成する
        val internal = requireContext().filesDir
        val file = File(internal, "deckContent")
        // ファイルにかかれたカードの画像を表示する
        val bufferedReader = file.bufferedReader()

        bufferedReader.readLines().forEach {
            val cardId = it.toInt()
            deck1.deck.add(AllCard[cardId])
        }

        // 選ばれた対戦相手のデッキを生成する

        val playerId = args.opponentNumber.toInt()
        for (i in 0 until OpponentData[playerId].DeckId.size){
            deck2.deck.add(AllCard[OpponentData[playerId].DeckId[i]])
        }
        // フロントへ更新
        deck1.deckSetUp()
        deck2.deckSetUp()
        updateField(fieldMain.field)
        setCard(deck1.deckImageList())

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        binding.cardbutton1.setOnClickListener { onClickCard(0) }
        binding.cardbutton2.setOnClickListener { onClickCard(1) }
        binding.cardbutton3.setOnClickListener { onClickCard(2) }

        binding.rotatebutton.setOnClickListener { onClickRotate() }
        binding.passbutton.setOnClickListener { onClickPass() }

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickGrid(clickIndex : Int){

        // カードが選択されていなければ何もしない
        if (!cardFlag){
            return
        }

        // クリックしたボタンを座標に変換
        val index = intArrayOf(clickIndex / 10, clickIndex % 10)

        // プレビューを表示する
        if ( !( fieldFlag && (index[0] == selectGridCoordinates[0]) && (index[1] == selectGridCoordinates[1]) ) ){

            selectGridCoordinates = index
            fieldFlag = true
            preview()
            return
        }

        // 置ければ置く
        if ( fieldMain.canSet(index, selectCardRange, Condition.Player1) ){
            play()
        }
    }

    // 画面下のカードをクリックしたとき、その情報を引き渡す
    private fun onClickCard(clickButton: Int){

        // カードが何もない時には何もしない
        if (deck1.handCard[clickButton] == -1){
            cardFlag = false
            selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
            preview()
            return
        }

        // 同じカードを連続でクリックした場合にはキャンセルする
        if (cardFlag && (clickButton == selectCardId)){
            cardFlag = false
            selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
            preview()
            return
        }

        selectCardId = clickButton
        selectCardRange = deck1.deck[deck1.handCard[selectCardId]].Range
        cardFlag = true
        fieldFlag = false
        preview()

    }

    // 回転ボタンをクリックしたときに実行される関数
    private fun onClickRotate(){
        // カードが選択されなければ実行しない
        if (!cardFlag) {
            return
        }
        selectCardRange = rotateRange(selectCardRange)
        preview()
    }

    // パスボタンをクリックしたときに実行される関数
    private fun onClickPass(){
        if (!cardFlag) {
            return
        }
        selectGridCoordinates = intArrayOf(0, 0)
        selectCardRange = Array(5){ intArrayOf(0, 0, 0, 0, 0)}
        play()
    }

/* 以下、プライベート関数 */

    // fieldMain からフロントへの更新を行う
    @SuppressLint("SetTextI18n")
    private fun updateField (field: Array<Array<Condition>>) {

        // まずフィールドの初期化
        player1Score = 0
        player2Score = 0
        val row = 10

        // こっちが動けばいい
        for (i in 0 until binding.fieldGrid.childCount) {
            val fieldRow = i / row
            val fieldColumn = i % row
            val v = binding.fieldGrid.getChildAt(i)
            when(field[fieldRow][fieldColumn]){
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
        if (nowTurnCount < totalTurn){
            binding.Turn.text = "Turn $nowTurnCount / $totalTurn"
        }
        // ゲーム終了なら
        else if (nowTurnCount > totalTurn){
            val resultText : String =
                if (player1Score > player2Score){
                    "WIN!!"
                } else if (player1Score < player2Score){
                    "LOSE..."
                } else{
                    "DRAW"
                }

            // 結果をダイアログで表示
            CustomDialog.Builder(this)
                .setTitle(resultText)
                .setMessage("$player1Score vs $player2Score")
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
        nowTurnCount++
    }

    // デッキからカードをランダムで選んで設置する
    private fun setCard (imageList: MutableList<Int>) {
        // 枚数が足りない場合にはwhite で対応する
        binding.cardbutton1.setBackgroundResource(imageList[0])
        binding.cardbutton2.setBackgroundResource(imageList[1])
        binding.cardbutton3.setBackgroundResource(imageList[2])
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


    // プレビューを表示する
    private fun preview(){

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
        val coreImage = binding.fieldGrid.getChildAt (10 * selectGridCoordinates[0] +selectGridCoordinates[1])
        coreImage.setBackgroundResource(R.drawable.tentative_core)

        // カードの範囲から座標にまず変換する
        for (i in selectCardRange.indices){
            for (j in 0 until selectCardRange[0].size){
                if (selectCardRange[i][j] == 1){

                    val x = selectGridCoordinates[0] + (i -2)
                    val y = selectGridCoordinates[1] + (j -2)
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

        // カードの範囲が大きい順に更新していく
        if (myRangeSize < comRangeSize){
            fieldMain.setColor(computer.first, computer.second, computer.third)
            fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
        }
        else if(myRangeSize > comRangeSize){
            fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
            fieldMain.setColor(computer.first, computer.second, computer.third)
        }
        else{
            // 同じならランダムで更新
            val rv = Math.random()
            if (100 * rv > 50){
                fieldMain.setColor(computer.first, computer.second, computer.third)
                fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
            }
            else{
                fieldMain.setColor(selectGridCoordinates, selectCardRange, Condition.Player1)
                fieldMain.setColor(computer.first, computer.second, computer.third)
            }
        }

        cardFlag = false
        fieldFlag = false
        deck1.deckDraw(selectCardId)
        setCard(deck1.deckImageList())
        updateField(fieldMain.field)
        selectGridCoordinates = intArrayOf(6, 4)
    }

}
