package com.embeddedproject.tokenManager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class ListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;

    public boolean isUnattentedSelected() {
        return isUnattentedSelected;
    }

    private static final int TYPE_ITEM = 1;

    private TokenData tokenHeader;
    private List<TokenData> tokenList = new ArrayList<>();
    private LayoutInflater inflator;
    private Listener listener;
    private IsSocketFree isSocketFree;
    private boolean isUnattentedSelected = true;

    ListAdapter (Context context, TokenData tokenHeader ,List<TokenData> tokenList1,IsSocketFree isSocketFree){
        this.tokenHeader = tokenHeader;
        this.tokenList = tokenList1;
        this.listener = (Listener) context;
        this.isSocketFree = isSocketFree;
        inflator = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == TYPE_HEADER){
            View convertView = inflator.inflate(R.layout.list_header_item, parent, false);
            HeaderViewHolder viewHolder = new HeaderViewHolder(convertView);
            return viewHolder;
        } else if (viewType == TYPE_ITEM){
            View convertView = inflator.inflate(R.layout.list_row, parent, false);
            ListViewHolder viewHolder = new ListViewHolder(convertView);
            return viewHolder;
        }
        throw new RuntimeException("View Type Error");
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0)
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderViewHolder){
            final HeaderViewHolder headerViewHolder = (HeaderViewHolder)holder;
            headerViewHolder.token_number.setText(String.valueOf(tokenHeader.tokenNumber));
            headerViewHolder.check_box.setChecked(tokenHeader.tokenStatus);
            headerViewHolder.check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    tokenHeader.setTokenStatus(isChecked);
                    if(listener.isTokenPresent(tokenHeader.tokenNumber)){
                        listener.updateTokenStatus(tokenHeader.tokenNumber, isChecked);
                    }
                }
            });
            headerViewHolder.list_all_tokens.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    isUnattentedSelected = false;
                    Button listAllButton = (Button)v;
                    listAllButton.setBackgroundResource(R.drawable.list_button_left_dark);
                    Button listUnattentedButton = (Button)headerViewHolder.list_unattented_tokens;
                    listUnattentedButton.setBackgroundResource(R.drawable.list_button_right);
                    listener.listAllTokens();
                }
            });
            headerViewHolder.list_unattented_tokens.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    isUnattentedSelected = true;
                    Button listUnattentedButton = (Button)v;
                    listUnattentedButton.setBackgroundResource(R.drawable.list_button_right_dark);
                    Button listAllButton = (Button)headerViewHolder.list_all_tokens;
                    listAllButton.setBackgroundResource(R.drawable.list_button_left);
                    listener.listUnattentedTokens();
                }
            });
            headerViewHolder.enter_token_number.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    listener.enterTokenNumber();
                }
            });
        }
        else if(holder instanceof ListViewHolder){
            final ListViewHolder listViewHolder = (ListViewHolder)holder;

            listViewHolder.token_number.setText((CharSequence) Integer.toString(tokenList.get(position-1).tokenNumber));
            //listViewHolder.check_box.setOnCheckedChangeListener(null);
            listViewHolder.check_box.setChecked(tokenList.get(position-1).tokenStatus);
            listViewHolder.check_box.setEnabled(false );
            //listViewHolder.check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            //    @Override
            //    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //        tokenList.get(listViewHolder.getAdapterPosition() - 1).setTokenStatus(isChecked);
            //        listener.updateTokenStatus(tokenList.get(listViewHolder.getAdapterPosition() - 1).tokenNumber, isChecked);
            //    }
            //});
        }

    }

    @Override
    public int getItemCount() {
        return tokenList.size() + 1;
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder{

        TextView token_number;
        CheckBox check_box;
        Button list_all_tokens, list_unattented_tokens, enter_token_number;

        public HeaderViewHolder(View itemView) {
            super(itemView);

            token_number = (TextView) itemView.findViewById(R.id.token_number);
            check_box = (CheckBox) itemView.findViewById(R.id.check_box);
            this.list_all_tokens = (Button) itemView.findViewById(R.id.list_all_tokens);
            this.list_unattented_tokens = (Button) itemView.findViewById(R.id.list_unattented_tokens);
            this.enter_token_number = (Button) itemView.findViewById(R.id.enter_token_number);
        }
    }

    public class ListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView token_number;
        CheckBox check_box;

        public ListViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            token_number = (TextView) itemView.findViewById(R.id.token_number);
            check_box = (CheckBox) itemView.findViewById(R.id.check_box);

        }

        @Override
        public void onClick(View v) {
            TokenData clickedToken = new TokenData();
            clickedToken.setTokenNumber(Integer.parseInt(token_number.getText().toString()));
            clickedToken.setTokenStatus(check_box.isChecked());
            if(isSocketFree.isSocketBusy()){
                return;
            }
            isSocketFree.blockSocket();
            listener.updateLastNonCalledToken(clickedToken.tokenNumber);
            listener.goToToken(clickedToken.tokenNumber);
        }
    }

    public void addElementToTokenList(TokenData tokenData){
        if(!tokenData.tokenStatus||!isUnattentedSelected){
            tokenList.add(tokenData);
            notifyItemChanged(tokenList.size());
        }

    }

    public void updateListOfTokens(List<TokenData> tokenList1){
        this.tokenList = tokenList1;
        notifyDataSetChanged();
    }

    public void clearAllTokens(){
        tokenList.clear();
        notifyDataSetChanged();
    }
}
