package com.vuvanduong.ringchat.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.ConversationActivity;
import com.vuvanduong.ringchat.activity.EditPasswordActivity;
import com.vuvanduong.ringchat.activity.ViewImageActivity;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int ME = 0, YOU = 1, LAST_MESSAGE = 2, GROUP = 3;

    private ArrayList<Message> messages;
    private Context context;
    private User userLogin;
    private User friend;
    private ArrayList<User> usersInRoom;
    private boolean isGroup;
    private int maxSizeImage;

    public MessageAdapter(ArrayList<Message> chatMessages, Context context, User userLogin, ArrayList<User> usersInRoom, boolean isGroup) {
        this.messages = chatMessages;
        this.context = context;
        this.userLogin = userLogin;
        this.usersInRoom = usersInRoom;
        this.isGroup = isGroup;
        if (!isGroup) {
            friend = usersInRoom.get(0);
        }
        maxSizeImage = Constant.getMaxWidthScreen(context);
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).getIdRoom() != null && messages.get(position).getIdRoom().length() > 21) {
            return LAST_MESSAGE;
        } else if (messages.get(position).getType().equalsIgnoreCase("group")) {
            return GROUP;
        } else if (messages.get(position).getUserID().equals(userLogin.getId())) {
            return ME;
        } else if (!messages.get(position).getUserID().equals(userLogin.getId())) {
            return YOU;
        }
        return -1;
    }

    public void addItem(Message message) {
        this.messages.add(message);
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
                        if (me.txtTimeMessageMe.getVisibility() == View.GONE) {
                            me.txtTimeMessageMe.setVisibility(View.VISIBLE);
                        } else {
                            me.txtTimeMessageMe.setVisibility(View.GONE);
                        }
                    }
                });

                ((HolderMe) holder).imgIconMessageMe.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent viewImage = new Intent(context, ViewImageActivity.class);
                        viewImage.putExtra("url", messages.get(position).getContext());
                        context.startActivity(viewImage);
                    }
                });
                break;
            case YOU:
                final HolderYou you = (HolderYou) holder;
                configureViewHolderYou(you, position);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (you.txtTimeMessageYou.getVisibility() == View.GONE) {
                            you.txtTimeMessageYou.setVisibility(View.VISIBLE);
                        } else {
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
                        conversation.putExtra("friend", (Serializable) getUser(getFriendId(messages.get(position).getIdRoom())));
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
                        if (groupMessage.txtTimeMessageGroup.getVisibility() == View.GONE) {
                            groupMessage.txtTimeMessageGroup.setVisibility(View.VISIBLE);
                        } else {
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
        String time = "";
        if (messages.get(position).getType().equalsIgnoreCase("image")){
            time = messages.get(position).getDatetime();
            me.txtTimeMessageMe.setText(time);
            me.imgIconMessageMe.setVisibility(View.VISIBLE);
            Log.e("image",messages.get(position).getContext());
            Picasso.with(this.context)
                    .load(userLogin.getImage())
                    .placeholder(R.drawable.user)
                    .transform(new CircleTransform())
                    .into(me.imgAvatarMeMessage);
            Picasso.with(this.context)
                    .load(R.drawable.emptyimage)
                    .resize(maxSizeImage,maxSizeImage)
                    .centerInside()
                    .into(me.imgIconMessageMe);
            Picasso.with(this.context)
                    .load(messages.get(position).getContext())
                    .resize(maxSizeImage,maxSizeImage)
                    .centerInside()
                    .into(me.imgIconMessageMe);

        }else {
            if (isGroup) {
                time = messages.get(position).getDatetime() + "\n" + getNameUser(messages.get(position).getUserID());
            } else {
                time = messages.get(position).getDatetime();
            }
            me.txtTimeMessageMe.setText(time);
            me.txtContextMessageMe.setText(messages.get(position).getContext());
            me.imgIconMessageMe.requestLayout();
            me.imgIconMessageMe.getLayoutParams().height = 0;
            me.imgIconMessageMe.getLayoutParams().width = 0;

            Picasso.with(this.context)
                    .load(userLogin.getImage())
                    .placeholder(R.drawable.user)
                    .transform(new CircleTransform())
                    .into(me.imgAvatarMeMessage);

        }
//        if (messages.get(position).getType()==null || messages.get(position).getType().equalsIgnoreCase("message")){
//            me.imgIconMessageMe.requestLayout();
//            me.imgIconMessageMe.getLayoutParams().height = 0;
//            me.imgIconMessageMe.getLayoutParams().width = 0;
//            me.imgIconMessageMe.setScaleType(ImageView.ScaleType.FIT_XY);
//        }else if (messages.get(position).getType().equalsIgnoreCase("Declined") || messages.get(position).getType().equalsIgnoreCase("Missed")){
//            me.imgIconMessageMe.setImageResource(R.drawable.ic_call_missed_black_24dp);
//        }else if (messages.get(position).getType().equalsIgnoreCase("Success")){
//            me.imgIconMessageMe.setImageResource(R.drawable.ic_phone_in_talk_black_24dp);
//        }
    }

    private void configureViewHolderYou(HolderYou you, int position) {
        String time = "";
        if (messages.get(position).getType().equalsIgnoreCase("image")){
            time = messages.get(position).getDatetime();
            you.txtTimeMessageYou.setText(time);
            you.imgIconMessageYou.setVisibility(View.VISIBLE);
            Log.e("image",messages.get(position).getContext());
            Picasso.with(this.context)
                    .load(friend.getImage())
                    .placeholder(R.drawable.user)
                    .transform(new CircleTransform())
                    .into(you.imgAvatarFriendYou);
            Picasso.with(this.context)
                    .load(R.drawable.emptyimage)
                    .resize(maxSizeImage,maxSizeImage)
                    .centerInside()
                    .into(you.imgIconMessageYou);
            Picasso.with(this.context)
                    .load(messages.get(position).getContext())
                    .resize(maxSizeImage,maxSizeImage)
                    .centerInside()
                    .into(you.imgIconMessageYou);

        }else {
            if (isGroup) {
                time = messages.get(position).getDatetime() + "\n" + getNameUser(messages.get(position).getUserID());
                Picasso.with(this.context)
                        .load(usersInRoom.get(position).getImage())
                        .placeholder(R.drawable.user)
                        .transform(new CircleTransform())
                        .into(you.imgAvatarFriendYou);
            } else {
                time = messages.get(position).getDatetime();
                Picasso.with(this.context)
                        .load(friend.getImage())
                        .placeholder(R.drawable.user)
                        .transform(new CircleTransform())
                        .into(you.imgAvatarFriendYou);
            }
            you.txtTimeMessageYou.setText(time);
            you.txtContextMessageYou.setText(messages.get(position).getContext());
            you.imgIconMessageYou.requestLayout();
            you.imgIconMessageYou.getLayoutParams().height = 0;
            you.imgIconMessageYou.getLayoutParams().width = 0;
            you.imgIconMessageYou.setScaleType(ImageView.ScaleType.FIT_XY);

        }

//        if (messages.get(position).getType()==null || messages.get(position).getType().equalsIgnoreCase("message")){
//            you.imgIconMessageYou.requestLayout();
//            you.imgIconMessageYou.getLayoutParams().height = 0;
//            you.imgIconMessageYou.getLayoutParams().width = 0;
//            you.imgIconMessageYou.setScaleType(ImageView.ScaleType.FIT_XY);
//        }
//        else if (messages.get(position).getType().equalsIgnoreCase("Declined")
//                || messages.get(position).getType().equalsIgnoreCase("Missed")
//                || messages.get(position).getContext().equalsIgnoreCase("Missed call.")
//                || messages.get(position).getContext().equalsIgnoreCase("Cuộc gọi nhỡ.")){
//            you.imgIconMessageYou.setImageResource(R.drawable.ic_call_missed_black_24dp);
//        }else if (messages.get(position).getType().equalsIgnoreCase("Success")){
//            you.imgIconMessageYou.setImageResource(R.drawable.ic_phone_in_talk_black_24dp);
//        }
    }

    private void configureViewHolderLastMessage(HolderLastMessage lastMessage, int position) {
        String time = DBUtil.revertDatetimeMessage(messages.get(position).getDatetime());
        lastMessage.txtTimeMessageLast.setText(time);
        String context = "";
        if (userLogin.getId().equalsIgnoreCase(messages.get(position).getUserID())) {
            context = this.context.getString(R.string.you) + ": " + messages.get(position).getContext();
        } else {
            context = getFirstNameUser(messages.get(position).getUserID()) + ": " + messages.get(position).getContext();
        }
        if (context.length() > 32) {
            context = context.substring(0, 32) + "...";
        }
        lastMessage.txtContextMessageHome.setText(context);
        lastMessage.txtNameFriendHome.setText(getNameUser(getFriendId(messages.get(position).getIdRoom())));
        if (messages.get(position).getType().equalsIgnoreCase("message")) {
            lastMessage.imgIconMessageLast.requestLayout();
            lastMessage.imgIconMessageLast.getLayoutParams().height = 0;
            lastMessage.imgIconMessageLast.getLayoutParams().width = 0;
            lastMessage.imgIconMessageLast.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        Picasso.with(this.context)
                .load(getUser(getFriendId(messages.get(position).getIdRoom())).getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(lastMessage.imgFriendHome);
    }

    private void configureViewHolderGroup(HolderGroupMessage groupMessage, int position) {
        String time = messages.get(position).getDatetime() + "\n" + getNameUser(messages.get(position).getUserID());
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

    private String getNameUser(String id) {
        String fullName = "";
        if (id == null) {
            return fullName;
        } else {
            for (int i = 0; i < usersInRoom.size(); i++) {
                if (usersInRoom.get(i).getId().equalsIgnoreCase(id)) {
                    fullName = UserUtil.getFullName(usersInRoom.get(i));
                }
            }
        }
        return fullName;
    }

    private User getUser(String id) {
        User user = null;
            for (int i = 0; i < usersInRoom.size(); i++) {
                if (usersInRoom.get(i).getId().equalsIgnoreCase(id)) {
                    user = usersInRoom.get(i);
                    break;
                }
            }
        return user;
    }

    private String getFriendId(String roomId) {
        String[] usersId = roomId.split("&");
        String idfriend = "";
        if (usersId[0].equalsIgnoreCase(userLogin.getId())) {
            idfriend = usersId[1];
        } else {
            idfriend = usersId[0];
        }
        return idfriend;
    }

    private String getFirstNameUser(String id) {
        String fullName = "";
        if (id == null) {
            return fullName;
        } else {
            for (int i = 0; i < usersInRoom.size(); i++) {
                if (usersInRoom.get(i).getId().equalsIgnoreCase(id)) {
                    fullName = usersInRoom.get(i).getFirstname();
                }
            }
        }
        return fullName;
    }
}
