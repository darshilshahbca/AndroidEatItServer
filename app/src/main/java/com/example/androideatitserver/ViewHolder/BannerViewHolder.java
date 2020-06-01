package com.example.androideatitserver.ViewHolder;

import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Interface.ItemClickListener;
import com.example.androideatitserver.R;

public class BannerViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

    public TextView banner_name;
    public ImageView banner_image;


    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);

        banner_name = (TextView)itemView.findViewById(R.id.banner_name);
        banner_image = (ImageView)itemView.findViewById(R.id.banner_image);

        itemView.setOnCreateContextMenuListener(this);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0, 0, getAdapterPosition(), Common.UPDATE);
        menu.add(0, 0, getAdapterPosition(), Common.DELETE);

    }
}
