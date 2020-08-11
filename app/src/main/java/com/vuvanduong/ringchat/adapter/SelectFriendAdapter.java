package com.vuvanduong.ringchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;

import java.util.ArrayList;

public class SelectFriendAdapter extends RecyclerView.Adapter<SelectFriendAdapter.ViewHolder> {
    ArrayList<User> dataContacts;
    private Context context;
    private ArrayList<Boolean> positionCheck;
    ArrayList<User> chosenContact;

    public SelectFriendAdapter(ArrayList<User> dataContacts, Context context) {
        this.context = context;
        this.dataContacts = dataContacts;
        positionCheck = new ArrayList<>();
        for (int i =0 ; i < dataContacts.size();i++){
            positionCheck.add(false);
        }
        chosenContact = new ArrayList<>();
    }

    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_select_friend, parent, false);

        return new ViewHolder(itemView);
    }

    @SuppressLint("ResourceAsColor")
    public void onBindViewHolder(@NonNull SelectFriendAdapter.ViewHolder holder, final int position) {
        holder.txtNameFriendSelectFriend.setText(dataContacts.get(position).getFullname());
        holder.txtEmailFriendSelectFriend.setText(dataContacts.get(position).getEmail());
        holder.chkSelectFriend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                positionCheck.set(position,isChecked);
                if (positionCheck.get(position)){
                    chosenContact.add(dataContacts.get(position));
                }else {
                    chosenContact.remove(dataContacts.get(position));
                }
            }
        });
    }

    public int getItemCount() {
        return dataContacts.size();
    }

    public ArrayList<User> getListFriendSelected() {
        return this.chosenContact;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox chkSelectFriend;
        ImageView imgAvatarSelectFriend;
        TextView txtNameFriendSelectFriend, txtEmailFriendSelectFriend;
        @SuppressLint("ResourceAsColor")
        ViewHolder(View itemView) {
            super(itemView);
            chkSelectFriend = itemView.findViewById(R.id.chkSelectFriend);
            imgAvatarSelectFriend = itemView.findViewById(R.id.imgAvatarSelectFriend);
            txtNameFriendSelectFriend = itemView.findViewById(R.id.txtNameFriendSelectFriend);
            txtEmailFriendSelectFriend = itemView.findViewById(R.id.txtEmailFriendSelectFriend);
        }

    }

}
