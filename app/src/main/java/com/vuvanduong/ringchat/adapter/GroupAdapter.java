package com.vuvanduong.ringchat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.GroupConversationActivity;
import com.vuvanduong.ringchat.model.GroupChat;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.io.Serializable;
import java.util.ArrayList;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder>{
    private ArrayList<GroupChat> groupChats;
    private Context context;
    private User userLogin;

    public GroupAdapter(ArrayList<GroupChat> groupChats, Context context, User userLogin) {
        this.groupChats = groupChats;
        this.context = context;
        this.userLogin = userLogin;
    }

    public void addAllItem(ArrayList<GroupChat> groupChatList){
        if (this.groupChats!= null) this.groupChats.clear();
        this.groupChats.addAll(groupChatList);
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_conversation, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupAdapter.ViewHolder holder, final int position) {
        holder.imgFriendHome.setImageResource(R.drawable.group);
        String time = DBUtil.revertDatetimeMessage(groupChats.get(position).getDatetime());
        holder.txtTimeMessageLast.setText(time);
        String context = "";
        if (userLogin.getId().equalsIgnoreCase(groupChats.get(position).getUserID())) {
            context = this.context.getString(R.string.you) + ": "+groupChats.get(position).getContext();
        }else {
            context = this.context.getString(R.string.someone)+": "+groupChats.get(position).getContext();
        }
        if (context.length() > 32) {
            context = context.substring(0, 32) + "...";
        }
        holder.txtContextMessageHome.setText(context);
        holder.txtNameFriendHome.setText(groupChats.get(position).getGroupName());
        holder.imgIconMessageLast.requestLayout();
        holder.imgIconMessageLast.getLayoutParams().height = 0;
        holder.imgIconMessageLast.getLayoutParams().width = 0;
        holder.imgIconMessageLast.setScaleType(ImageView.ScaleType.FIT_XY);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent conversation = new Intent(v.getContext(), GroupConversationActivity.class);
                conversation.putExtra("userLogin", (Serializable) userLogin);
                conversation.putExtra("group",groupChats.get(position));
                v.getContext().startActivity(conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupChats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFriendHome,imgIconMessageLast;
        TextView txtTimeMessageLast, txtNameFriendHome, txtContextMessageHome;
        @SuppressLint("ResourceAsColor")
        ViewHolder(View itemView) {
            super(itemView);
            imgFriendHome = itemView.findViewById(R.id.imgFriendHome);
            txtTimeMessageLast = itemView.findViewById(R.id.txtTimeMessageLast);
            txtNameFriendHome = itemView.findViewById(R.id.txtNameFriendHome);
            txtContextMessageHome = itemView.findViewById(R.id.txtContextMessageHome);
            imgIconMessageLast = itemView.findViewById(R.id.imgIconMessageLast);
        }

    }
}
