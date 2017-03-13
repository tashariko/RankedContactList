package com.tashariko.rankedcontactlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Puru Chauhan on 29/11/16.
 */

public class CallTypeDetailAdapter extends ArrayAdapter<CallModel> {

    private LayoutInflater inflater;
    private ArrayList<CallModel> list;
    private View view;
    private ViewHolder holder;

    public CallTypeDetailAdapter(Context context, int resource, ArrayList<CallModel> adapterList) {
        super(context, resource);
        this.list=adapterList;
        inflater = LayoutInflater.from(context);

    }


    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if(view==null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.list_item_detail, null);
            holder.number = (TextView) view.findViewById(R.id.number);
            holder.name= (TextView) view.findViewById(R.id.name);
            holder.rank= (TextView) view.findViewById(R.id.rank);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        holder.number.setText(String.valueOf(list.get(position).getNumber()));
        holder.rank.setText(String.valueOf(list.get(position).getRank()));
        holder.name.setText(String.valueOf(list.get(position).getName()));

        return view;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    private class ViewHolder{
        TextView number,name,rank;
    }
}
