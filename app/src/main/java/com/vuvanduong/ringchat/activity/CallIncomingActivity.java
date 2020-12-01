package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.service.LinphoneService;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import org.linphone.core.Call;
import org.linphone.core.CallParams;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.tools.Log;

import java.io.Serializable;

public class CallIncomingActivity extends AppCompatActivity {

    TextView txtv_caller,txtTypeCall;
    ImageButton btn_accept;
    ImageButton btn_decline;
    private CoreListenerStub mListenerCallIn;
    private Call mCall;
    private boolean mAlreadyAcceptedOrDeclinedCall;
    public static boolean isCallIncommingActivity = false;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference conversationLastMessage, conversationMessages;
    private DatabaseReference users = dbReference.child("users");
    boolean isFirstAdd;
    String chatRoom;
    ImageView image_caller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);
        btn_decline = findViewById(R.id.btn_decline);
        btn_accept = findViewById(R.id.btn_accept);
        txtv_caller = findViewById(R.id.txtv_caller);
        txtTypeCall = findViewById(R.id.txtTypeCall);
        image_caller = findViewById(R.id.image_caller);

        isFirstAdd = true;
        String idUserCall = LinphoneService.getCore().getCurrentCall().getRemoteAddress().getUsername();
        Query getUser = users.orderByKey().equalTo(idUserCall);
        getUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    User userCall = item.getValue(User.class);
                    assert userCall != null;
                    userCall.setId(item.getKey());
                    txtv_caller.setText(UserUtil.getFullName(userCall));
                    try {
                        Picasso.with(CallIncomingActivity.this)
                                .load(userCall.getImage())
                                .placeholder(R.drawable.user)
                                .transform(new CircleTransform())
                                .into(image_caller);
                    }catch (NullPointerException ignored){

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        for (Call call : LinphoneService.getCore().getCalls()) {
            mCall = call;
            break;
        }
        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlreadyAcceptedOrDeclinedCall) return;
                mAlreadyAcceptedOrDeclinedCall = true;
                CallParams params = LinphoneService.getCore().createCallParams(mCall);
                params.enableVideo(true);
                mCall.acceptWithParams(params);
            }
        });
        btn_decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAlreadyAcceptedOrDeclinedCall) return;
                mAlreadyAcceptedOrDeclinedCall = true;
                if (LinphoneService.getCore().getCallsNb() > 0) {
                    Call call = LinphoneService.getCore().getCurrentCall();
                    if (call == null) {
                        call = LinphoneService.getCore().getCalls()[0];
                    }
                    call.terminate();
                }
            }
        });
        mListenerCallIn = new CoreListenerStub() {
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                if (core.getCallsNb() == 0) {
                    finish();
                }
                if (call == mCall) {
                    if (state == Call.State.Connected) {
                        isCallIncommingActivity = true;
                        startActivity(new Intent(CallIncomingActivity.this, CallOutgoingActivity.class));
                        finish();
                    } else if (state == Call.State.End || state == Call.State.Released) {
                        // Once call is finished (end state), terminate the activity
                        // We also check for released state (called a few seconds later) just in case
                        // we missed the first one
                        if (isFirstAdd) {
                            pushToDatabase(call);
                            isFirstAdd = false;
                        }
                        call.terminate();
                    }
                }
            }
        };
    }

    private void pushToDatabase(Call call) {
        Call.Status callstatus = call.getCallLog().getStatus();
        String callFrom = call.getCallLog().getFromAddress().getUsername();
        String callTo = call.getCallLog().getToAddress().getUsername();

        chatRoom = DBUtil.getChatRoomByTwoUserId(callFrom, callTo);
        conversationMessages = dbReference.child("conversationMessages/" + chatRoom);
        conversationLastMessage = dbReference.child("conversationLastMessage/" + chatRoom);

        Message chatMessage = new Message();
        chatMessage.setContext(callstatus.toString());
        chatMessage.setType(callstatus.toString());
        chatMessage.setDatetime(DBUtil.getStringDateTimeChatRoom());
        chatMessage.setUserID(callFrom);
        // no Connected situation because when the state is Connected, user is navigated to CallOutgoingActivity
        if (callstatus.equals(Call.Status.Declined)) {
            chatMessage.setContext("✆ "+getString(R.string.miss_call));
            conversationLastMessage.setValue(chatMessage);
            chatMessage.setDatetime(DBUtil.getStringDateTime());
            conversationMessages.push().setValue(chatMessage);
        } else if (callstatus.equals(Call.Status.Missed)) {
            chatMessage.setContext("✆ "+getString(R.string.miss_call));
            conversationLastMessage.setValue(chatMessage);
            chatMessage.setDatetime(DBUtil.getStringDateTime());
            conversationMessages.push().setValue(chatMessage);
        } else if (callstatus.equals(Call.Status.AcceptedElsewhere) || callstatus.equals(Call.Status.DeclinedElsewhere)) {
            finish();
        }
    }

    // only allow 1 call to ring
    private void lookupCurrentCall() {
        if (LinphoneService.getCore() != null) {
            for (Call call : LinphoneService.getCore().getCalls()) {
                if (Call.State.IncomingReceived == call.getState()
                        || Call.State.IncomingEarlyMedia == call.getState()) {
                    mCall = call;

                    if (mCall.getRemoteParams().videoEnabled()){
                        txtTypeCall.setText(R.string.video_call);
                    }else {
                        txtTypeCall.setText(R.string.voice_call);
                    }
                    Log.i(mCall.getRemoteParams().videoEnabled(), "video enabled");
                    break;
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (LinphoneService.isReady()
                && (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            mCall.terminate();
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LinphoneService.getCore().removeListener(mListenerCallIn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Core core = LinphoneService.getCore();
        if (core != null) {
            core.addListener(mListenerCallIn);
        }

        mAlreadyAcceptedOrDeclinedCall = false;
        mCall = null;

        // only allow 1 call to ring
        lookupCurrentCall();
        if (mCall == null) {
            Log.d("Couldn't find incoming call");
            finish();
        }

    }
}
