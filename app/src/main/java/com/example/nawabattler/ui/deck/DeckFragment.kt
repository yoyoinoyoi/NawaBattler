package com.example.nawabattler.ui.deck

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nawabattler.DeckFragmentAdapter
import com.example.nawabattler.R
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.databinding.FragmentDeckBinding
import java.io.*
import kotlin.math.roundToInt

class DeckFragment : Fragment() {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!

    // 暫定のデッキ内容
    private val tmpDeck = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)

    // 選択されたカードid
    var selectAllCard = -1

    // 選択されたカードのView
    var selectCardView: View? = null

    // カード選択フラグ
    var cardFlag = false

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckBinding.inflate(inflater, container, false)
        val view = binding.root

        // 1次元のリストを作成
        val layoutManager = GridLayoutManager(requireContext(), 3, RecyclerView.VERTICAL, false)
        // リストに区切り線を追加
        val dividerItemDecoration = DividerItemDecoration(requireContext(), layoutManager.orientation)

        val images: ArrayList<Int> = arrayListOf()
        for (element in AllCard) {
            images.add(element.Image)
        }

        val adapter = DeckFragmentAdapter(images)

        // バインディングに適用
        binding.cardRecyclerView.layoutManager = layoutManager
        binding.cardRecyclerView.addItemDecoration(dividerItemDecoration)
        binding.cardRecyclerView.adapter = adapter

        /*
        * 画面下のデッキ編集ウィジェットの初期化
         */

        // デッキ画像の生成
        val deckImageColumn = 2
        val deckImageRow = 4
        for (i in 0 until deckImageColumn * deckImageRow){
            // GridLayoutを使用するので、rowとcolumnを指定
            val dp = resources.displayMetrics.density
            val params = GridLayout.LayoutParams().also {
                it.rowSpec = GridLayout.spec(i / deckImageRow)
                it.columnSpec = GridLayout.spec(i % deckImageRow)
                it.width = (95 * dp).roundToInt()
                it.height = (95 * dp).roundToInt()
            }
            val imageView = ImageView(view.context).also {
                it.layoutParams = params
                it.setBackgroundResource(R.drawable.empty)
                it.setOnClickListener { onClickDeckCard(i) }
            }
            binding.cardViewGrid.addView(imageView)
        }

        /*
        * 内部ファイルのデータからカードを表示する
         */

        val internal = requireContext().filesDir
        // デッキはdeckContent ファイルにid として記載されている
        val file = File(internal, "deckContent")

        // ファイルが無ければ作成する
        if (!file.exists()){
            val bufferedWriter = file.bufferedWriter()
            val fileContent = "0\n1\n2\n3\n4\n5\n6\n7"
            bufferedWriter.write(fileContent)
            bufferedWriter.close()
            println("Create New Deck")
        }

        val bufferedReader = file.bufferedReader()
        var cardIndex = 0
        bufferedReader.readLines().forEach {
            val cardId = it.toInt()

            // ファイルに記載されているid を一時データに保存
            tmpDeck[cardIndex] = cardId

            // ファイルにかかれたカードの画像を表示する
            val v = binding.cardViewGrid.getChildAt(cardIndex)
            v.setBackgroundResource(AllCard[cardId].Image)
            cardIndex++
        }

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        // 上の画面のカードをクリックしたときに実行する関数
        adapter.setOnItemClickListener(object : DeckFragmentAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                onClickAllCard(view, position)
            }
        })
        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 画面下のカードをクリックしたときに実行される関数
    private fun onClickDeckCard(cardId : Int) {
        if (!cardFlag) {
            return
        }

        // 選択していたカードの背景色を透明に直す
        selectCardView!!.setBackgroundResource(R.color.transparent)

        // 画面下のデッキの変更を反映する
        val v = binding.cardViewGrid.getChildAt(cardId)
        v.setBackgroundResource(AllCard[selectAllCard].Image)

        // 内部ファイルも更新を行う
        tmpDeck[cardId] = selectAllCard

        val internal = requireContext().filesDir
        val file = File(internal, "deckContent")
        val bufferedWriter = file.bufferedWriter()
        tmpDeck.forEach(){
            println(it.toString())
            bufferedWriter.write(it.toString())
            bufferedWriter.newLine()
        }
        bufferedWriter.close()

        cardFlag = false
        selectAllCard = -1
        return
    }

    // 上画面のカードをクリックしたときに実行する関数
    @SuppressLint("ResourceAsColor")
    private fun onClickAllCard(view: View, cardId: Int) {
        if (selectAllCard == cardId){
            cardFlag = false
            selectAllCard = -1
            selectCardView!!.setBackgroundResource(R.color.transparent)
            return
        }

        selectAllCard = cardId
        cardFlag = true
        if (selectCardView != null){
            selectCardView!!.setBackgroundResource(R.color.transparent)
        }
        selectCardView = view
        view.setBackgroundResource(R.color.selected)
    }
}