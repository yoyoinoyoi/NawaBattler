package com.example.nawabattler.structure

import com.example.nawabattler.ai.Agent

data class PlayerData (val Image: Int, val Name: String, val DeckId: IntArray, val Agent: Agent){
    // Image: 画像
    // Name: プレイヤー名
    // DeckId: デッキを構築するカードのID(0-indexed)
    // Agent: 使用するエージェント名
}