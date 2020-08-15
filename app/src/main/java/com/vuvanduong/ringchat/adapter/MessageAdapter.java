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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.ConversationActivity;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ME = 0, YOU = 1, LAST_MESSAGE=2, GROUP=3;

    private ArrayList<Message> messages;
    private Context context;
    private User userLogin;
    private User friend;
    private ArrayList<User> usersInRoom;
    private boolean isGroup;

    public MessageAdapter(ArrayList<Message> chatMessages, Context context, User userLogin, ArrayList<User> usersInRoom, boolean isGroup) {
        this.messages = chatMessages;
        this.context = context;
        this.userLogin = userLogin;
        this.usersInRoom= usersInRoom;
        this.isGroup = isGroup;
        if (!isGroup){
            friend=usersInRoom.get(0);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getIdRoom() != null) {
            return LAST_MESSAGE;
        }else if (messages.get(position).getType().equalsIgnoreCase("group")) {
            return GROUP;
        }else if (messages.get(position).getUserID().equals(userLogin.getId())) {
            return ME;
        } else if (!messages.get(position).getUserID().equals(userLogin.getId())) {
            return YOU;
        }
        return -1;
    }

    public void addItem(Message message) {
        this.messages.add(0,message);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ME:
                View me = inflater.inflate(R.layout.item_message_me, parent, false);
                viewHolder = new HolderMe(me);
                break;
            case YOU:
                View you = inflater.inflate(R.layout.item_message_you, parent, false);
                viewHolder = new HolderYou(you);
                break;
            case LAST_MESSAGE:
                View lastMessage = inflater.inflate(R.layout.item_conversation, parent, false);
                viewHolder = new HolderLastMessage(lastMessage);
                break;
            case GROUP:
                View group = inflater.inflate(R.layout.item_group_message, parent, false);
                viewHolder = new HolderGroupMessage(group);
                break;
            default:
                break;
        }
        assert viewHolder != null;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case ME:
                final HolderMe me = (HolderMe) holder;
                configureViewHolderMe(me, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (me.txtTimeMessageMe.getVisibility()==View.GONE){
                            me.txtTimeMessageMe.setVisibility(View.VISIBLE);
                        }else {
                            me.txtTimeMessageMe.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case YOU:
                final HolderYou you = (HolderYou) holder;
                configureViewHolderYou(you, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (you.txtTimeMessageYou.getVisibility()==View.GONE){
                            you.txtTimeMessageYou.setVisibility(View.VISIBLE);
                        }else {
                            you.txtTimeMessageYou.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            case LAST_MESSAGE:
                final HolderLastMessage lastMessage = (HolderLastMessage) holder;
                configureViewHolderLastMessage(lastMessage, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent conversation = new Intent(v.getContext(), ConversationActivity.class);
                        conversation.putExtra("userLogin", (Serializable) userLogin);
                        conversation.putExtra("friend", (Serializable) usersInRoom.get(position));
                        System.out.println(usersInRoom.get(position).toString());
                        v.getContext().startActivity(conversation);
                    }
                });
                break;
            case GROUP:
                final HolderGroupMessage groupMessage = (HolderGroupMessage) holder;
                configureViewHolderGroup(groupMessage, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (groupMessage.txtTimeMessageGroup.getVisibility()==View.GONE){
                            groupMessage.txtTimeMessageGroup.setVisibility(View.VISIBLE);
                        }else {
                            groupMessage.txtTimeMessageGroup.setVisibility(View.GONE);
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private void configureViewHolderMe(HolderMe me, int position) {
        String time="";
        if (isGroup){
            time = messages.get(position).getDatetime()+"\n"+getNameUser(messages.get(position).getUserID());
        }else {
            time = messages.get(position).getDatetime();
        }
        me.txtTimeMessageMe.setText(time);
        me.txtContextMessageMe.setText(messages.get(position).getContext());
        if (messages.get(position).getType()==null || messages.get(position).getType().equalsIgnoreCase("message")){
            me.imgIconMessageMe.requestLayout();
            me.imgIconMessageMe.getLayoutParams().height = 0;
            me.imgIconMessageMe.getLayoutParams().width = 0;
            me.imgIconMessageMe.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void configureViewHolderYou(HolderYou you, int position) {
        String time="";
        if (isGroup){
            time = messages.get(position).getDatetime()+"\n"+getNameUser(messages.get(position).getUserID());
        }else {
            time = messages.get(position).getDatetime();
        }
        you.txtTimeMessageYou.setText(time);
        you.txtContextMessageYou.setText(messages.get(position).getContext());
        if (messages.get(position).getType()==null || messages.get(position).getType().equalsIgnoreCase("message")){
            you.imgIconMessageYou.requestLayout();
            you.imgIconMessageYou.getLayoutParams().height = 0;
            you.imgIconMessageYou.getLayoutParams().width = 0;
            you.imgIconMessageYou.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void configureViewHolderLastMessage(HolderLastMessage lastMessage, int position) {
        String time = DBUtil.revertDatetimeMessage(messages.get(position).getDatetime());
        lastMessage.txtTimeMessageLast.setText(time);
        String context = "";
        if (userLogin.getId().equalsIgnoreCase(messages.get(position).getUserID())) {
            context = this.context.getString(R.string.you) + ": "+messages.get(position).getContext();
        }else {
            context = usersInRoom.get(position).getFirstname() + ": "+messages.get(position).getContext();
        }
        if (context.length() > 32) {
            context = context.substring(0, 32) + "...";
        }
        lastMessage.txtContextMessageHome.setText(context);
        lastMessage.txtNameFriendHome.setText(usersInRoom.get(position).getFullname());
        if (messages.get(position).getType().equalsIgnoreCase("message")) {
            lastMessage.imgIconMessageLast.requestLayout();
            lastMessage.imgIconMessageLast.getLayoutParams().height = 0;
            lastMessage.imgIconMessageLast.getLayoutParams().width = 0;
            lastMessage.imgIconMessageLast.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }

    private void configureViewHolderGroup(HolderGroupMessage groupMessage, int position) {
        String time = messages.get(position).getDatetime()+"\n"+getNameUser(messages.get(position).getUserID());
        groupMessage.txtTimeMessageGroup.setText(time);
        groupMessage.txtContextGroupMessage.setText(messages.get(position).getContext());
    }

    private class HolderMe extends RecyclerView.ViewHolder {
        TextView txtTimeMessageMe;
        TextView txtContextMessageMe;
        ImageView imgIconMessageMe;
        ImageView imgAvatarMeMessage;

        public HolderMe(View itemView) {
            super(itemView);
            txtTimeMessageMe = itemView.findViewById(R.id.txtTimeMessageMe);
            txtContextMessageMe = itemView.findViewById(R.id.txtContextMessageMe);
            imgIconMessageMe = itemView.findViewById(R.id.imgIconMessageMe);
            imgAvatarMeMessage = itemView.findViewById(R.id.imgAvatarMeMessage);
        }

    }

   private class HolderYou extends RecyclerView.ViewHolder {
        TextView txtTimeMessageYou;
        TextView txtContextMessageYou;
        ImageView imgIconMessageYou;
        ImageView imgAvatarFriendYou;

        public HolderYou(View itemView) {
            super(itemView);
            txtTimeMessageYou = itemView.findViewById(R.id.txtTimeMessageYou);
            txtContextMessageYou = itemView.findViewById(R.id.txtContextMessageYou);
            imgIconMessageYou = itemView.findViewById(R.id.imgIconMessageYou);
            imgAvatarFriendYou = itemView.findViewById(R.id.imgAvatarFriendYou);
        }
    }

    private class HolderLastMessage extends RecyclerView.ViewHolder {
        TextView txtNameFriendHome;
        TextView txtTimeMessageLast;
        TextView txtContextMessageHome;
        ImageView imgFriendHome;
        ImageView imgIconMessageLast;

        public HolderLastMessage(@NonNull View itemView) {
            super(itemView);
            txtNameFriendHome = itemView.findViewById(R.id.txtNameFriendHome);
            txtTimeMessageLast = itemView.findViewById(R.id.txtTimeMessageLast);
            txtContextMessageHome = itemView.findViewById(R.id.txtContextMessageHome);
            imgFriendHome = itemView.findViewById(R.id.imgFriendHome);
            imgIconMessageLast = itemView.findViewById(R.id.imgIconMessageLast);
        }
    }

    private class HolderGroupMessage extends RecyclerView.ViewHolder {
        TextView txtTimeMessageGroup;
        TextView txtContextGroupMessage;

        public HolderGroupMessage(@NonNull View itemView) {
            super(itemView);
            txtTimeMessageGroup = itemView.findViewById(R.id.txtTimeMessageGroup);
            txtContextGroupMessage = itemView.findViewById(R.id.txtContextGroupMessage);
        }
    }

    private String getNameUser(String id){
        String fullName="";
        if (id==null){
            return fullName;
        }else {
            for (int i = 0; i < usersInRoom.size(); i++) {
                if (usersInRoom.get(i).getId().equalsIgnoreCase(id)) {
                    fullName = usersInRoom.get(i).getFullname();
                }
            }
        }
        return fullName;
    }
}
