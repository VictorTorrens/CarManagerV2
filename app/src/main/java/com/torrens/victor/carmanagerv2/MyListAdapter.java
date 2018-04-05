package com.torrens.victor.carmanagerv2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class MyListAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> versiones;

    public MyListAdapter(Context context, List<ListItem> vers) {
        super();
        this.mInflater = LayoutInflater.from(context);
        this.versiones = (ArrayList<ListItem>) vers;
    }

    public int getCount() {
        return versiones.size();
    }

    public ListItem getItem(int position) {
        return versiones.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item, null);
            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.textViewName);
            holder.number = convertView.findViewById(R.id.textViewNumber);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final ListItem version = getItem(position);
        holder.name.setText(version.getName());
        holder.number.setText(version.getNumber());
        return convertView;
    }

    class ViewHolder {
        TextView name;
        TextView number;


    }
}
