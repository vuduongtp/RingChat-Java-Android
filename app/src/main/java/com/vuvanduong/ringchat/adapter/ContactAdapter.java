package com.vuvanduong.ringchat.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.ConversationActivity;
import com.vuvanduong.ringchat.activity.UserProfileActivity;
import com.vuvanduong.ringchat.activity.WelcomeActivity;
import com.vuvanduong.ringchat.app.InitialApp;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
            holder.imgAvatarFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                    Intent user_profile = new Intent(v.getContext(), UserProfileActivity.class);
                    user_profile.putExtra("user_login", (Serializable) userLogin);
                    user_profile.putExtra("user_scan", (Serializable) users.get(position));
                    user_profile.putExtra("isScanFriend", true);
                    v.getContext().startActivity(user_profile);
                }
            });
            holder.txtNameFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Toast.makeText(context, fullName, Toast.LENGTH_SHORT).show();
                    Intent conversation = new Intent(v.getContext(), ConversationActivity.class);
                    conversation.putExtra("userLogin", (Serializable) userLogin);
                    conversation.putExtra("friend", (Serializable) users.get(position));
                    v.getContext().startActivity(conversation);
                }
            });
            holder.txtEmailFriend.setOnClickListener(new View.OnClickListener() {
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
                public void onClick(final View v) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    if (NetworkUtil.getConnectivityStatusString(v.getContext()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    userContacts.child(users.get(position).getId()).removeValue();
                                    v.setVisibility(View.GONE);
                                    Toast.makeText(context, R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
                                    users.remove(users.get(position));
                                    notifyDataSetChanged();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    dialog.dismiss();
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                    builder.setMessage(Objects.requireNonNull(v.getContext()).getString(R.string.confirm_remove_friend))
                            .setPositiveButton(Objects.requireNonNull(v.getContext()).getString(R.string.yes), dialogClickListener)
                            .setNegativeButton(Objects.requireNonNull(v.getContext()).getString(R.string.no), dialogClickListener).show();
                }
            });
        }else {
            holder.btnGoToChatFriend.setVisibility(View.GONE);
            holder.btnRemoveFriend.setVisibility(View.GONE);
            holder.btnAddNewFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkUtil.getConnectivityStatusString(v.getContext()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    userContacts.child(users.get(position).getId()).setValue(users.get(position).getId());
                    v.setVisibility(View.GONE);
                    Toast.makeText(context, R.string.add_friend_success, Toast.LENGTH_SHORT).show();
                }
            });
        }
        holder.txtNameFriend.setText(UserUtil.getFullName(users.get(position)));
        holder.txtEmailFriend.setText(users.get(position).getEmail());
        Picasso.get()
                .load(users.get(position).getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(holder.imgAvatarFriend);
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
        ImageView btnGoToChatFriend,btnAddNewFriend,btnRemoveFriend,imgAvatarFriend;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            btnAddNewFriend = itemView.findViewById(R.id.btnAddNewFriend);
            btnGoToChatFriend = itemView.findViewById(R.id.btnGoToChatFriend);
            txtStatusFriend = itemView.findViewById(R.id.txtStatusFriend);
            txtNameFriend = itemView.findViewById(R.id.txtNameFriend);
            txtEmailFriend = itemView.findViewById(R.id.txtEmailFriend);
            btnRemoveFriend = itemView.findViewById(R.id.btnRemoveFriend);
            imgAvatarFriend = itemView.findViewById(R.id.imgAvatarFriend);
        }
    }
}
