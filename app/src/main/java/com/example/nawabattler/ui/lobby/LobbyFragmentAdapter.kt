package com.example.nawabattler.ui.lobby

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nawabattler.R
import com.example.nawabattler.data.OpponentData

class LobbyFragmentAdapter():
    RecyclerView.Adapter<LobbyFragmentAdapter.ViewHolder>() {

    lateinit var listener: OnItemClickListener

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val playerName: TextView
        val playerResult: TextView
        val playerImage: ImageView
        val playerButton: Button

        init {
            playerName = view.findViewById(R.id.player_name)
            playerResult = view.findViewById(R.id.battle_result)
            playerImage = view.findViewById(R.id.player_image)
            playerButton = view.findViewById((R.id.player_button))
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.playerdata_view, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

        viewHolder.playerImage.setBackgroundResource(OpponentData[position].Image)
        viewHolder.playerName.text = OpponentData[position].Name
        viewHolder.playerButton.setOnClickListener {
            listener.onItemClickListener(it, position)
        }
    }

    // インターフェースの作成
    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    // サイズを返す関数
    override fun getItemCount() = OpponentData.size
}