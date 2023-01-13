package com.example.nawabattler.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nawabattler.CustomDialog
import com.example.nawabattler.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root
//
//        // バトル準備画面に遷移
//        binding.battleButton.setOnClickListener {
//            val action = MainFragmentDirections.actionMainFragmentToDeckFragment()
//            findNavController().navigate(action)
//        }

        // バトル準備画面に遷移
        binding.galleryButton.setOnClickListener {
            CustomDialog.Builder(this)
                .setTitle("カスタムタイトル")
                .setMessage("カスタムメッセージ")
                .setPositiveButton("はい") {  }
                .build()
                .show(childFragmentManager, CustomDialog::class.simpleName)
        }
        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
