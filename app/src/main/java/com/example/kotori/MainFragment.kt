package com.example.kotori

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kotori.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding.root

        // バトル準備画面に遷移
        binding.buttonGoToBattle.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToLobbyFragment()
            findNavController().navigate(action)
        }

        // デッキ編集画面に遷移
        binding.buttonGoToDeck.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToDeckFragment()
            findNavController().navigate(action)
        }

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}