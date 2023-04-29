package com.example.nawabattler.structure

class DeckManager() {
    // デッキ内のカード情報
    var deck: MutableList<Card> = mutableListOf<Card>()
    // 手札のカード情報
    var handCard: MutableList<Int>
    // 山札のカード情報
    private var stackCard: MutableList<Int>

    init {
        this.handCard = mutableListOf<Int>()
        this.stackCard = mutableListOf<Int>()
    }

    /**
     * デッキの内容から手札と山札を生成する
     */
    fun setUp(){
        this.stackCard = mutableListOf<Int>()
        for (i in 0 until this.deck.size){
            this.stackCard.add(i)
        }

        // 初めは手札は何もない(-1 は何もないという意味)
        this.handCard = mutableListOf(-1, -1, -1)
        for (i in 0 until 3){
            val selectedCard = this.stackCard[(0 until this.stackCard.size).random()]
            this.stackCard.remove(selectedCard)
            this.handCard[i] = selectedCard
        }
    }

    /**
     * handCard[index] をキャストした際の処理
     */
    fun castCard(index: Int){
        // 山札が尽きたら 何もない(-1) を手札に加える
        if (this.stackCard.size == 0){
            this.handCard[index] = -1
        } else {
            val selectedCard = this.stackCard[(0 until this.stackCard.size).random()]
            this.stackCard.remove(selectedCard)
            this.handCard[index] = selectedCard
        }
    }

    fun addCard(card: Card){
        this.deck.add(card)
    }

    fun handCard(index: Int): Card{
        return deck[handCard[index]]
    }

}