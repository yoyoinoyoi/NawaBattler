package com.example.nawabattler.ui.lobby

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nawabattler.LobbyFragmentAdapter
import com.example.nawabattler.R
import com.example.nawabattler.data.AllCard
import com.example.nawabattler.databinding.FragmentLobbyBinding

class LobbyFragment : Fragment() {

    private var _binding: FragmentLobbyBinding? = null
    private val binding get() = _binding!!

    private var selectPlayerNumber = -1
    private var selectPlayerView: View? = null

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLobbyBinding.inflate(inflater, container, false)
        val view = binding.root

        // 1次元のリストを作成
        val layoutManager = GridLayoutManager(requireContext(), 1, RecyclerView.VERTICAL, false)
        // リストに区切り線を追加
        val dividerItemDecoration = DividerItemDecoration(requireContext(), layoutManager.orientation)

        val images: ArrayList<Int> = arrayListOf()
        for (element in AllCard) {
            images.add(element.Image)
        }

        val adapter = LobbyFragmentAdapter()

        // バインディングに適用
        binding.playerRecyclerView.layoutManager = layoutManager
        binding.playerRecyclerView.addItemDecoration(dividerItemDecoration)
        binding.playerRecyclerView.adapter = adapter

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        // playerRecyclerView をクリックしたときに実行する関数
        adapter.setOnItemClickListener(object : LobbyFragmentAdapter.OnItemClickListener {
            override fun onItemClickListener(view: View, position: Int) {
                onClickPlayer(view, position)
            }
        })

        binding.goToBattleButton.setOnClickListener { onClickGoToBattleButton() }

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onClickPlayer(view: View, position: Int){
        if (selectPlayerNumber == position){
            selectPlayerNumber = -1
            selectPlayerView!!.setBackgroundResource(R.color.transparent)
            return
        }

        if (selectPlayerNumber >= 0){
            selectPlayerView!!.setBackgroundResource(R.color.transparent)
        }
        selectPlayerNumber = position
        selectPlayerView = view
        view.setBackgroundResource(R.color.selected)
    }

    private fun onClickGoToBattleButton(){
        if (selectPlayerNumber >= 0){
            val action = LobbyFragmentDirections
            .actionLobbyFragmentToBattleFragment(selectPlayerNumber.toString())
            findNavController().navigate(action)
        }
    }
}