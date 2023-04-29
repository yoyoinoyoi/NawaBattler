package com.example.nawabattler.structure

/*
*
 */
class FieldManager(initMap: Array<Array<Condition>>) {
    var field: Array<Array<Condition>>
    init{
        this.field = initMap
        this.field[this.field.size-2][1] = Condition.Player1
        this.field[1][this.field[0].size-2] = Condition.Player2
    }

    /*
    * カードを設置するときに、最低1つ含んでいないといけない座標のList を返す
     */
    private fun limitation(player: Condition): MutableSet<Int>{
        // 返り値
        val ret = mutableSetOf<Int>()
        // 指定マスの上下左右斜めを確認するためrのベクトル
        val vec = arrayOf(
            intArrayOf(1, 0),
            intArrayOf(1, 1),
            intArrayOf(0, 1),
            intArrayOf(-1, 1),
            intArrayOf(-1, 0),
            intArrayOf(-1, -1),
            intArrayOf(0, -1),
            intArrayOf(1, -1)
        )

        for (i in 0 until this.field.size){
            for (j in 0 until this.field[0].size){
                for ((x, y) in vec){
                    val vx = x + i
                    val vy = y + j
                    // field の外なら除外
                    if ((vx < 0) || (vx >= this.field.size)){
                        continue
                    }
                    if ((vy < 0) || (vy >= this.field[0].size)){
                        continue
                    }

                    // (vx, vy) はEmpty でないなら設置できないので除外
                    if (this.field[vx][vy] !== Condition.Empty){
                        continue
                    }

                    // (i, j) がplayer のマスと隣接しているならば, (vx, vy) を追加
                    if (this.field[i][j] == player){
                        ret.add(this.convertPositionToId(intArrayOf(vx, vy)))
                    }
                }
            }
        }
        return ret
    }

    /*
    * range を, range[2][2](中心) を(0, 0) として変換する
     */
    private fun rangeToCoordinate(range: Array<IntArray>): MutableList<IntArray>{
        val ret = mutableListOf<IntArray>()
        for (i in range.indices){
            for (j in 0 until range[0].size){
                if (range[i][j] == 1){
                    ret.add(intArrayOf(i-2, j-2))
                }
            }
        }
        return ret
    }

    /*
    * position: (x, y) をInt 型で返す. 値はfield内では一意に定まると保証される
     */
    private fun convertPositionToId(position: IntArray): Int{
        return 10000 * position[0] + position[1]
    }

    /*
    * player が coordinates に range を置きたいときに、置けるかを判断する
    * 置ける場合true, そうでない場合false を返す
     */
    fun canSet(position: IntArray, range: Array<IntArray>, player: Condition): Boolean{
        // 少なくとも1つは含んでいないといけない座標のList
        val canSetPositions = this.limitation(player)
        // range を座標に変換
        val rangePositions = this.rangeToCoordinate(range)
        // canSetPositions を少なくとも含んでいるか
        var positionFlag = false

        for ((x, y) in rangePositions){
            //
            val vx = x + position[0]
            val vy = y + position[1]

            // マップの外に出てしまうため, 設置不可能
            if ((vx < 0) || (vx >= this.field.size)){
                return false
            }
            if ((vy < 0) || (vy >= this.field[0].size)){
                return false
            }

            val positionId = this.convertPositionToId(intArrayOf(vx, vy))

            if (positionId in canSetPositions){
                positionFlag = true
            }

            // 該当マスがすでに埋まっていたら置くことができない
            if (!canSetPoint(this.field[vx][vy])){
                return false
            }
        }

        return positionFlag
    }

    private fun canSetPoint(condition: Condition): Boolean{
        val ret = when(condition){
            Condition.Empty -> true
            Condition.Player1 -> false
            Condition.Player2 -> false
            Condition.Wall -> false
            Condition.TentativeOK -> true
            Condition.TentativeNG -> false
            Condition.TentativeCenterEmpty -> true
            Condition.TentativeCenterOK -> true
            Condition.TentativeCenterNG -> false
        }
        return ret
    }

    /*
    * player が coordinates に range を置く.
    * 置けない場合でも実行されるので, canSet で確認する
     */
    fun setColor(position: IntArray, range: Array<IntArray>, player: Condition){
        val rangeCoordinate = this.rangeToCoordinate(range)
        for ((x, y) in rangeCoordinate){
            this.field[x+position[0]][y+position[1]] = player
        }
    }
}