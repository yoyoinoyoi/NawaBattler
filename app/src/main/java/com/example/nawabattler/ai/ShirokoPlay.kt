package com.example.nawabattler.ai

import com.example.nawabattler.R
import com.example.nawabattler.structure.Card
import com.example.nawabattler.structure.Condition
import com.example.nawabattler.structure.FieldManager

class ShirokoAgent: Agent() {

    override fun play(
        fm: FieldManager,
        selfHandCard: MutableList<Card>,
        opponentHandCard: MutableList<Card>
    ): Triple<IntArray, Array<IntArray>, Int> {

        // 手札からまずカードを決める
        val candidates = mutableListOf<Triple<IntArray, Array<IntArray>, Int>>()
        for (choiceCardId in opponentHandCard.indices){
            // 回転させる
            val choiceCard = opponentHandCard[choiceCardId]
            if (choiceCard.Image == R.drawable.empty){
                continue
            }
            val choiceRange1 = choiceCard.Range
            val choiceRange2 = rotateRange(choiceRange1)
            val choiceRange3 = rotateRange(choiceRange2)
            val choiceRange4 = rotateRange(choiceRange3)

            for (i in 0 until fm.field.size){
                for (j in 0 until fm.field[0].size){
                    if (fm.canSet(intArrayOf(i, j), choiceRange1, Condition.Player2)){
                        candidates.add(Triple(intArrayOf(i, j), choiceRange1, choiceCardId))
                    }
                    if (fm.canSet(intArrayOf(i, j), choiceRange2, Condition.Player2)){
                        candidates.add(Triple(intArrayOf(i, j), choiceRange2, choiceCardId))
                    }
                    if (fm.canSet(intArrayOf(i, j), choiceRange3, Condition.Player2)){
                        candidates.add(Triple(intArrayOf(i, j), choiceRange3, choiceCardId))
                    }
                    if (fm.canSet(intArrayOf(i, j), choiceRange4, Condition.Player2)){
                        candidates.add(Triple(intArrayOf(i, j), choiceRange4, choiceCardId))
                    }
                }
            }
        }
        if (candidates.isNotEmpty()){
            val randomNum = (candidates.indices).random()
            return candidates[randomNum]
        }

        // 置けなくなったら適当に選んで捨てる
        for (choiceCardId in opponentHandCard.indices){
            val choiceCard = opponentHandCard[choiceCardId]
            if (choiceCard.Image != R.drawable.empty){
                return Triple(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)}, choiceCardId)
            }
        }

        // たぶんここまでいかない
        return Triple(intArrayOf(0, 0), Array(5){ intArrayOf(0, 0, 0, 0, 0)}, -1)
    }

}