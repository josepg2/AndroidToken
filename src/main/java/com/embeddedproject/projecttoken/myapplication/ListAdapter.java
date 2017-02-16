package com.embeddedproject.projecttoken.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by George_Joseph02 on 2/16/2017.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {

    Context context;
    List<TokenData> tokenList = new ArrayList<>();
    LayoutInflater inflator;
    Listener listener;

    public ListAdapter (Context context, List<TokenData> tokenList1){
        this.context = context;
        this.tokenList = tokenList1;
        this.listener = (Listener) context;
        inflator = LayoutInflater.from(context);
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = inflator.inflate(R.layout.list_row, parent, false);
        ListViewHolder viewHolder = new ListViewHolder(convertView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, int position) {
        holder.token_number.setText((CharSequence) Integer.toString(tokenList.get(position).tokenNumber));
        holder.check_box.setChecked(tokenList.get(position).tokenStatus);
    }

    @Override
    public int getItemCount() {
        return tokenList.size();
    }

    public void addElementToTokenList(int token, boolean status){
        TokenData tokenData = new TokenData();
        tokenData.tokenNumber = token;
        tokenData.tokenStatus = status;
        tokenList.add(tokenData);
        notifyDataSetChanged();
    }

    public void clearAllTokens(){
        tokenList.clear();
        notifyDataSetChanged();
    }


    public class ListViewHolder extends RecyclerView.ViewHolder{

        TextView token_number;
        CheckBox check_box;

        public ListViewHolder(View itemView) {
            super(itemView);

            token_number = (TextView) itemView.findViewById(R.id.token_number);
            check_box = (CheckBox) itemView.findViewById(R.id.check_box);

        }
    }
}
