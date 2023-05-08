package com.example.nawabattler.ui.deck

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nawabattler.R
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.databinding.FragmentDeckBinding
import kotlin.math.roundToInt

class DeckFragment : Fragment() {

    private var _binding: FragmentDeckBinding? = null
    private val binding get() = _binding!!

    private val deckViewModel: DeckViewModel by viewModels { DeckViewModel.Factory(requireContext()) }

    private var selectCardView: View? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * 所持カードのリストを表示する
         */

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

        /**
         * デッキ画面の生成
         */

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
                it.setOnClickListener { deckViewModel.onClickDeckCard(i) }
            }
            binding.cardViewGrid.addView(imageView)
        }

        for (i in 0 until binding.cardViewGrid.childCount) {
            val v = binding.cardViewGrid.getChildAt(i)
            val cardId = deckViewModel.tmpDeck[i]
            v.setBackgroundResource(AllCard[cardId].Image)
        }

        /**
         * クリックイベント・値の監視
         */

        // デッキカードのUIを更新する際の処理
        deckViewModel.selectDeckCard.observe(viewLifecycleOwner){
            val v = binding.cardViewGrid.getChildAt(it)
            val cardId = deckViewModel.tmpDeck[it]
            v.setBackgroundResource(AllCard[cardId].Image)
            // 持っているカードのUI処理
            if (selectCardView != null){
                selectCardView!!.setBackgroundResource(R.color.transparent)
            }
            selectCardView = null
        }

        // 上の画面のカードをクリックしたときに実行する関数
        adapter.setOnItemClickListener(object : DeckFragmentAdapter.OnItemClickListener {
            override fun onClick(view: View, position: Int, clickedText: String) {
                // フラグなどの更新
                deckViewModel.onClickOwnCard(position)
                // ViewModel に格納されている値に基づいてUIを更新
                onClickAllCard(view)
            }
        })
        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // 上画面のカードをクリックしたときに実行する関数
    private fun onClickAllCard(view: View) {
        view.setBackgroundResource(R.color.selected)
        if (selectCardView != null){
            selectCardView!!.setBackgroundResource(R.color.transparent)
        }

        selectCardView = if (selectCardView == view){ null } else { view }
    }
}