package com.vuvanduong.ringchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.User;

import java.util.ArrayList;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private ArrayList<User> users;
    private Context context;
    private boolean isAddFriend;

    public ContactAdapter(ArrayList<User> users, Context context, boolean isAddFriend) {
        this.users = users;
        this.context = context;
        this.isAddFriend = isAddFriend;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactAdapter.ViewHolder holder, int position) {
        final String fullName = users.get(position).getLastname()+users.get(position).getFirstname();
        if (!isAddFriend) {
            holder.txtStatusFriend.setText(users.get(position).getStatus());
            holder.btnAddNewFriend.setVisibility(View.GONE);
            holder.btnGoToChatFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            holder.btnGoToChatFriend.setVisibility(View.GONE);
            holder.btnAddNewFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                }
            });
        }
        holder.txtNameFriend.setText(fullName);
        holder.txtEmailFriend.setText(users.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatusFriend,txtNameFriend,txtEmailFriend;
        ImageView btnGoToChatFriend,btnAddNewFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddNewFriend = itemView.findViewById(R.id.btnAddNewFriend);
            btnGoToChatFriend = itemView.findViewById(R.id.btnGoToChatFriend);
            txtStatusFriend = itemView.findViewById(R.id.txtStatusFriend);
            txtNameFriend = itemView.findViewById(R.id.txtNameFriend);
            txtEmailFriend = itemView.findViewById(R.id.txtEmailFriend);
        }
    }
}
