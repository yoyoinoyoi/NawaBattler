package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.example.nawabattler.*
import com.example.nawabattler.databinding.FragmentBattleBinding
import kotlin.math.roundToInt

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private val args: BattleFragmentArgs by navArgs()

    private val battleViewModel = BattleViewModel(args.opponentNumber, requireContext())

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        val view = binding.root

        /**
         * GridLayout のマスを生成する
         */

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
            v.setOnClickListener { battleViewModel.onClickGrid(i) }
        }

        battleViewModel.readDeckFile()

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        binding.cardbutton1.setOnClickListener { battleViewModel.onClickCard(0) }
        binding.cardbutton2.setOnClickListener { battleViewModel.onClickCard(1) }
        binding.cardbutton3.setOnClickListener { battleViewModel.onClickCard(2) }

        binding.rotatebutton.setOnClickListener { battleViewModel.onClickRotate() }
        binding.passbutton.setOnClickListener { battleViewModel.onClickPass() }

        /**
         * observer を設定する
         */

        battleViewModel.nowTurnCount.observe(viewLifecycleOwner, Observer {
            battleViewModel.updateGame()
        })
        battleViewModel.selectGridCoordinates.observe(viewLifecycleOwner, Observer {
            battleViewModel.preview()
        })

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

/* 以下、プライベート関数 */


}
