package com.example.nawabattler.structure

import com.example.nawabattler.R

class DeckManager() {
    // デッキ内のカード情報
    var deck: MutableList<Card> = mutableListOf<Card>()
    // 手札のカード情報
    var handCard: MutableList<Int>
    // 山札のカード情報
    var stackCard: MutableList<Int>

    init {
        this.handCard = mutableListOf<Int>()
        this.stackCard = mutableListOf<Int>()
    }

    /*
    * ゲーム開始までの一連の操作
     */
    fun deckSetUp(){
        deckReload()
        for (i in 0 until 3) {
            this.deckDraw(i)
        }
    }

    /*
    * 手札(handCard) のカードのImage を返す
     */
    fun deckImageList(): MutableList<Int>{
        val ret = mutableListOf<Int>()
        for (imageIndex in this.handCard){

            // -1(何もない) 場合には白の画像で代替する
            if (imageIndex == -1){
                ret.add(R.drawable.empty)
            } else {
                ret.add(this.deck[imageIndex].Image)
            }
        }
        return ret
    }

    /*
    * デッキを全て山札に移す
     */
    private fun deckReload(){
        this.stackCard = mutableListOf<Int>()
        for (i in 0 until this.deck.size){
            this.stackCard.add(i)
        }

        // 初めは手札は何もない(-1 は何もないという意味)
        this.handCard = mutableListOf(-1, -1, -1)

    }

    /*
    * handCard のindex番目 にカードを充てんする(ドローする)
     */
    fun deckDraw(index: Int){
        // 山札が尽きたら 何もない(-1) を手札に加える
        if (this.stackCard.size == 0){
            this.handCard[index] = -1
        } else {
            val selectedCard = this.stackCard[(0 until this.stackCard.size).random()]
            this.stackCard.remove(selectedCard)
            this.handCard[index] = selectedCard
        }
    }

}