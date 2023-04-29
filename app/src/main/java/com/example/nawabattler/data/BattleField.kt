package com.example.nawabattler.data

import com.example.nawabattler.structure.Condition

val standard = arrayOf(
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 2, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
)
val mini = arrayOf(
    arrayOf(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1),
    arrayOf(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 2, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 1, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
    arrayOf(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1),
    arrayOf(-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)
)
fun transConditionArray(baseArray: Array<Array<Int>>): Array<Array<Condition>>{
    val ret = Array(12){Array(10){ Condition.Empty } }
    for (i in baseArray.indices){
        for (j in 0 until baseArray[0].size){
            ret[i][j] = when(baseArray[i][j]){
                -1 -> Condition.Wall
                0 -> Condition.Empty
                1 -> Condition.Player1
                2 -> Condition.Player2
                else -> Condition.Empty
            }
        }
    }
    return ret
}