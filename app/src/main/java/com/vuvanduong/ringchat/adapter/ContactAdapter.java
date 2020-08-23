package com.vuvanduong.ringchat.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.data.DataHolder;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.ConversationActivity;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private ArrayList<User> users ;
    private Context context;
    private boolean isAddFriend;
    private User userLogin;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference userContacts;

    public ContactAdapter(ArrayList<User> users, Context context, boolean isAddFriend, User user) {
        this.users = users;
        this.context = context;
        this.isAddFriend = isAddFriend;
        this.userLogin = user;
        userContacts = dbReference.child("contacts/"+user.getId()+"/");
    }

    public void updateList(List<User> list){
        this.users = (ArrayList<User>) list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_contact,parent,false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactAdapter.ViewHolder holder, final int position) {
        if (!isAddFriend) {
            if (users.get(position).getStatus()==null || users.get(position).getStatus().equalsIgnoreCase("" )
                    || users.get(position).getStatus().equalsIgnoreCase("Offline")){
                holder.txtStatusFriend.setText("Offline");
                holder.txtStatusFriend.setTextColor(ContextCompat.getColor(context, R.color.red));
            }else {
                holder.txtStatusFriend.setText(users.get(position).getStatus());
            }
            holder.btnAddNewFriend.setVisibility(View.GONE);
            holder.btnGoToChatFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                    Intent conversation = new Intent(v.getContext(), ConversationActivity.class);
                    conversation.putExtra("userLogin", (Serializable) userLogin);
                    conversation.putExtra("friend", (Serializable) users.get(position));
                    v.getContext().startActivity(conversation);
                }
            });
            holder.btnRemoveFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userContacts.child(users.get(position).getId()).removeValue();
                    v.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
                    users.remove(users.get(position));
                    notifyDataSetChanged();
                }
            });
        }else {
            holder.btnGoToChatFriend.setVisibility(View.GONE);
            holder.btnRemoveFriend.setVisibility(View.GONE);
            holder.btnAddNewFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userContacts.child(users.get(position).getId()).setValue(users.get(position).getId());
                    v.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.add_friend_success, Toast.LENGTH_SHORT).show();
                }
            });
        }
        holder.txtNameFriend.setText(UserUtil.getFullName(users.get(position)));
        holder.txtEmailFriend.setText(users.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        if (users == null){
            return 0;
        }else {
            return users.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatusFriend,txtNameFriend,txtEmailFriend;
        ImageView btnGoToChatFriend,btnAddNewFriend,btnRemoveFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddNewFriend = itemView.findViewById(R.id.btnAddNewFriend);
            btnGoToChatFriend = itemView.findViewById(R.id.btnGoToChatFriend);
            txtStatusFriend = itemView.findViewById(R.id.txtStatusFriend);
            txtNameFriend = itemView.findViewById(R.id.txtNameFriend);
            txtEmailFriend = itemView.findViewById(R.id.txtEmailFriend);
            btnRemoveFriend = itemView.findViewById(R.id.btnRemoveFriend);
        }
    }
}
