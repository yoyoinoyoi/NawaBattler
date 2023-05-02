package com.example.nawabattler.structure

// カードの情報
data class Card(val Image: Int, val Range: Array<IntArray>){
    // Image: 画像
    // Range: カードの有効範囲
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (Image != other.Image) return false
        if (!Range.contentDeepEquals(other.Range)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Image
        result = 31 * result + Range.contentDeepHashCode()
        return result
    }

    fun cardSize(): Int{
        var ret = 0
        for (xArray in this.Range){ ret += xArray.sum() }
        return ret
    }
}
