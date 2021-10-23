package com.example.musicplayer.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.List;
import java.util.Map;

public class FileAdapter extends SimpleAdapter {
    Context co;
    private View vt;
    //文件集合
    private List<? extends Map<String, ?>> data2;
    //文件路径
    private String pspath;
    //文件绝对路径
    private String fiops;
    //文件绝对路径集合
    private List<String> mlph;

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        vt = super.getView(position, convertView, parent);
        fiops = pspath + "/" + data2.get(position).get("text");
        return vt;
    }

    public FileAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to, String getpath, List<String> lph) {
        super(context, data, resource, from, to);
        co = context;
        data2 = data;
        pspath = getpath;
        mlph = lph;
    }
}