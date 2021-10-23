package com.example.musicplayer.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.musicplayer.R;
import com.example.musicplayer.dao.MusicData;
import com.example.musicplayer.tool.WriteCof;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {
    private List<MusicData> musicList;
    private WriteCof writeCof;
    Context context;
    private int mSelect = -1;
    private OnItemClickListener listener;
    private OnTvClickListener listenertv;
    private OnItemLongClickListener longClickListener;

    public MusicAdapter(List<MusicData> getMusicList, Context mucontext) {
        this.musicList = getMusicList;
        writeCof = new WriteCof();
        context = mucontext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(itemView);
        return viewHolder;
    }

    public void changeSelected(int positon) {
        mSelect = positon;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        if (mSelect == position) {
            holder.musicName.setTextColor(Color.RED);
            holder.musicImg.setVisibility(View.VISIBLE);
            Bitmap bitmap = writeCof.getAlbumPicture(musicList.get(position).getPath());
            if (bitmap != null) {
                holder.musicImg.setImageBitmap(bitmap);
            }
            holder.partext.getLayoutParams().height = 170;
        } else {
            holder.musicName.setTextColor(Color.BLACK);
            holder.musicImg.setVisibility(View.INVISIBLE);
            holder.partext.getLayoutParams().height = 140;
        }

        holder.musicName.setText(musicList.get(position).getMusicName());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onClick(position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (longClickListener != null) {
                    longClickListener.onClick(position);
                }
                return true;
            }
        });

        holder.btn_item_test2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listenertv != null) {
                    listenertv.onClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    //定义Item被点击时接口
    public interface OnItemClickListener {
        void onClick(int position);
    }

    //定义btn_item_test2被点击时接口
    public interface OnTvClickListener {
        void onClick(int position);
    }

    //定义长按Item时接口
    public interface OnItemLongClickListener {
        void onClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOntvClickListener(OnTvClickListener listenertv) {
        this.listenertv = listenertv;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView musicName;
        ImageView musicImg;
        TextView btn_item_test2;
        LinearLayout partext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.musicName = itemView.findViewById(R.id.btn_item_test);
            this.musicImg = itemView.findViewById(R.id.text_item_img);
            this.btn_item_test2 = itemView.findViewById(R.id.btn_item_test2);
            this.partext = itemView.findViewById(R.id.partext);
        }
    }
}
