package com.example.nawabattler.ui.battle

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import androidx.core.view.ViewCompat.setSystemGestureExclusionRects
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.nawabattler.*
import com.example.nawabattler.data.FIELD_COLUMN
import com.example.nawabattler.data.FIELD_ROW
import com.example.nawabattler.data.OpponentData
import com.example.nawabattler.databinding.FragmentBattleBinding
import com.example.nawabattler.structure.Condition
import kotlin.math.roundToInt

class BattleFragment : Fragment() {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private val args: BattleFragmentArgs by navArgs()

    private val battleViewModel: BattleViewModel by viewModels { BattleViewModel.Factory(requireContext(), args.opponentNumber) }

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

        for (i in 0 until FIELD_COLUMN * FIELD_ROW) {
            // GridLayoutを使用するので、rowとcolumnを指定
            val dp = resources.displayMetrics.density
            val params = GridLayout.LayoutParams().also {
                it.rowSpec = GridLayout.spec(i / FIELD_ROW)
                it.columnSpec = GridLayout.spec(i % FIELD_ROW)
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

        /**
         * 対戦相手の画像を反映させる
         */

        binding.opponentImage.setBackgroundResource( OpponentData[args.opponentNumber].Image )

        /**
         * 各ボタンごとにクリックイベントを設定
         */

        binding.playerCard1.setOnClickListener { battleViewModel.onClickCard(0) }
        binding.playerCard2.setOnClickListener { battleViewModel.onClickCard(1) }
        binding.playerCard3.setOnClickListener { battleViewModel.onClickCard(2) }

        binding.rotateButton.setOnClickListener { battleViewModel.onClickRotate() }
        binding.passButton.setOnClickListener { battleViewModel.onClickPass() }

        /**
         * viewModel の値を監視する
         */

        // 実際に手を決めた時の動作
        battleViewModel.nowTurnCount.observe(viewLifecycleOwner) {
            battleViewModel.updateScore()
            binding.playerScore.text = "${battleViewModel.player1Score}"
            binding.opponentScore.text = "${battleViewModel.player2Score}"
            binding.Turn.text = turnText()
            binding.playerCard1.setBackgroundResource(battleViewModel.deck1.handCard[0].Image)
            binding.playerCard2.setBackgroundResource(battleViewModel.deck1.handCard[1].Image)
            binding.playerCard3.setBackgroundResource(battleViewModel.deck1.handCard[2].Image)
            binding.opponentCard1.setBackgroundResource(battleViewModel.deck2.handCard[0].Image)
            binding.opponentCard2.setBackgroundResource(battleViewModel.deck2.handCard[1].Image)
            binding.opponentCard3.setBackgroundResource(battleViewModel.deck2.handCard[2].Image)
            // ゲーム終了時にダイアログを表示
            if (it > battleViewModel.totalTurn){
                battleViewModel.gameSet()
                CustomDialog.Builder(this)
                    .setTitle(resultText())
                    .setMessage("${battleViewModel.player1Score} vs ${battleViewModel.player2Score}")
                    .setPositiveButton("わかった") {
                        val action = BattleFragmentDirections.actionBattleFragmentToHomeFragment()
                        findNavController().navigate(action)
                    }
                    .build()
                    .show(childFragmentManager, CustomDialog::class.simpleName)
            }
        }

        // 選択した盤面の座標に変更があった場合
        battleViewModel.updateFlag.observe(viewLifecycleOwner) {
            for (i in 0 until FIELD_ROW * FIELD_COLUMN) {
                val v = binding.fieldGrid.getChildAt(i)
                val cond = battleViewModel.fieldSub[i / FIELD_ROW][i % FIELD_ROW]
                v.setBackgroundResource(conditionToImage(cond))
            }
        }

        return view
    }

    // 終わったら破棄を忘れない
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * 表示に関する関数
     */

    private fun resultText() : String{
        val player1Score = battleViewModel.player1Score
        val player2Score = battleViewModel.player2Score
        return if (player1Score > player2Score){
            "YOU WIN!!"
        } else if (player1Score < player2Score){
            "YOU LOSE..."
        } else {
            "-DRAW-"
        }
    }

    private fun turnText() : String {
        val nowTurnCount = battleViewModel.nowTurnCount.value
        val totalTurnCount = battleViewModel.totalTurn
        return "$nowTurnCount / $totalTurnCount"
    }

    private fun conditionToImage(condition : Condition) : Int{
        val image = when(condition){
            Condition.Empty -> R.drawable.gray
            Condition.Player1 -> R.drawable.blue
            Condition.Player2 -> R.drawable.yellow
            Condition.Wall -> R.drawable.wall
            Condition.TentativeOK -> R.drawable.tentative_blue
            Condition.TentativeNG -> R.drawable.tentative_gray
            Condition.TentativeCenterOK -> R.drawable.tentative_blue_core
            Condition.TentativeCenterNG -> R.drawable.tentative_gray_core
            Condition.TentativeCenterEmpty -> R.drawable.tentative_core
        }
        return image
    }
}