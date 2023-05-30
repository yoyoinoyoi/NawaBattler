package com.example.nawabattler.ai

import com.example.nawabattler.structure.Card
import com.example.nawabattler.structure.FieldManager

abstract class Agent(){

    abstract fun play(
        fm: FieldManager,
        selfHandCard: MutableList<Card>,
        opponentHandCard: MutableList<Card>
    ): Triple<IntArray, Array<IntArray>, Int>

    protected fun rotateRange(range: Array<IntArray>): Array<IntArray>{
        val newList = Array(5){ intArrayOf(0, 0, 0, 0, 0) }
        for (i in range.indices){
            for (j in 0 until range[0].size){
                newList[i][j] = range[-j +4][i]
            }
        }
        return newList
    }
}