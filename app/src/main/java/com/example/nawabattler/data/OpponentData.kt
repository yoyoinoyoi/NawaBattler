package com.example.nawabattler.data

import com.example.nawabattler.R
import com.example.nawabattler.ai.ShirokoAgent
import com.example.nawabattler.structure.PlayerData

val OpponentData = arrayOf(
    PlayerData(
        R.drawable.shiroko,
        "shiroko",
        intArrayOf(0, 1, 2, 3, 4, 5, 6, 7),
        ShirokoAgent()
    ),
    PlayerData(
        R.drawable.iincho,
        "sakura",
        intArrayOf(8, 8, 8, 3, 3, 6, 6, 6),
        ShirokoAgent()
    ),
    PlayerData(
        R.drawable.sumire,
        "sumire",
        intArrayOf(6, 6, 11, 11, 10, 10, 2, 3),
        ShirokoAgent()
    )
)