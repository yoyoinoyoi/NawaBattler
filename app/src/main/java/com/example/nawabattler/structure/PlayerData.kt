package com.example.nawabattler.structure

data class PlayerData (val Image: Int, val Name: String, val DeckId: IntArray){
    // Image: 画像
    // Name: プレイヤー名
    // DeckId: デッキを構築するカードのID(0-indexed)
}