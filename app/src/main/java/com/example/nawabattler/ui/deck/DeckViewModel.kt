package com.example.nawabattler.ui.deck

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.nawabattler.data.DECK_CONTENT
import java.io.File

class DeckViewModel(
    context: Context
):ViewModel() {

    // 暫定のデッキ内容
    val tmpDeck = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    // 選択された持っているカードid
    private var selectOwnCard = -1
    // 選択されたデッキのカードid
    val selectDeckCard: LiveData<Int>
        get() = _selectDeckCard
    private val _selectDeckCard = MutableLiveData(0)
    // カード選択フラグ
    private var cardFlag = false

    private val internal = context.filesDir

    // コンストラクタに引数を持たせるための工夫
    class Factory(
        private val context: Context,
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel> create(modelClass: Class<T>) =
            DeckViewModel(context) as T
    }

    init{
        val file = File(internal, DECK_CONTENT)

        // ファイルが無ければ作成する
        if (!file.exists()){
            val bufferedWriter = file.bufferedWriter()
            val fileContent = "0\n1\n2\n3\n4\n5\n6\n7"
            bufferedWriter.write(fileContent)
            bufferedWriter.close()
            println("Create New Deck")
        }

        val bufferedReader = file.bufferedReader()
        var cardIndex = 0
        bufferedReader.readLines().forEach {
            // ファイルに記載されているid を一時データに保存
            val cardId = it.toInt()
            tmpDeck[cardIndex] = cardId
            cardIndex++
        }

    }

    // デッキのカードをクリックした場合の処理
    fun onClickDeckCard(cardId: Int) {
        if (!cardFlag) {
            return
        }

        // 内部ファイルも更新を行う
        tmpDeck[cardId] = selectOwnCard
        val file = File(internal, DECK_CONTENT)
        val bufferedWriter = file.bufferedWriter()
        tmpDeck.onEach{
            println(it.toString())
            bufferedWriter.write(it.toString())
            bufferedWriter.newLine()
        }
        bufferedWriter.close()

        cardFlag = false
        selectOwnCard = -1
        _selectDeckCard.postValue(cardId)
        return
    }

    // 持っているカードをクリックしたときの処理
    fun onClickOwnCard(cardId: Int) {
        if (selectOwnCard == cardId){
            cardFlag = false
            selectOwnCard = -1
            return
        }
        selectOwnCard = cardId
        cardFlag = true
    }
}