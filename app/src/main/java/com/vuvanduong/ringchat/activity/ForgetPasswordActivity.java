package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.GMailSender;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

public class ForgetPasswordActivity extends AppCompatActivity {
    ImageView backToLogin;
    EditText txtUsernameForgetPass;
    TextView txtErrorRegister;
    Button btnForgetPass;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    ProgressDialog dialog;
    User user;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        addControl();
        addEvent();
    }

    private void addControl() {
        backToLogin = findViewById(R.id.btnBackToLogin2);
        txtUsernameForgetPass = findViewById(R.id.txtUsernameForgetPass);
        txtErrorRegister = findViewById(R.id.txtErrorForgetPass);
        btnForgetPass = findViewById(R.id.btnForgetPass);
    }

    private void addEvent() {
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnForgetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(ForgetPasswordActivity.this, "",
                        getString(R.string.loading), true);
                if (txtUsernameForgetPass.getText().toString().trim().equalsIgnoreCase("")){
                    txtUsernameForgetPass.setText(getString(R.string.err_emty_email));
                    dialog.dismiss();
                }else if (!checkEmailAddress(txtUsernameForgetPass.getText().toString().trim())){
                    txtUsernameForgetPass.setText(getString(R.string.err_email_invalid));
                    dialog.dismiss();
                }else {
                    if (NetworkUtil.getConnectivityStatusString(ForgetPasswordActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Toast.makeText(ForgetPasswordActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ValueEventListener getUser = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                User userGet = item.getValue(User.class);
                                assert userGet != null;
                                userGet.setId(item.getKey());
                                if (userGet.getEmail().equalsIgnoreCase(txtUsernameForgetPass.getText().toString().trim())) {
                                    user = userGet;
                                    code = DBUtil.getAlphaNumericString(6);

                                    String tContents = "";
                                    try {
                                        InputStream stream = getAssets().open("confirmemail.html");

                                        int size = stream.available();
                                        byte[] buffer = new byte[size];
                                        stream.read(buffer);
                                        stream.close();
                                        tContents = new String(buffer);
                                    } catch (IOException e) {
                                        System.err.println(e.toString());
                                    }
                                    String replace = tContents.replace("%", "phantram");
                                    replace = replace.replace("phantrams", "%s");
                                    String noiDung = String.format(replace, UserUtil.getFullName(user), code);
                                    noiDung = noiDung.replace("phantram", "%");

                                    String tieuDe = "Xác nhận đổi lại mật khẩu RingChat";
                                    String emailTo = txtUsernameForgetPass.getText().toString().trim();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("tieuDe", tieuDe);
                                    bundle.putString("noiDung", noiDung);
                                    bundle.putString("emailTo", emailTo);

                                    guiMail myTask = new guiMail();
                                    myTask.execute(bundle);
                                    return;
                                }
                            }
                            if (user==null){
                                txtErrorRegister.setText(getString(R.string.user_not_found));
                                dialog.dismiss();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            System.err.println("db_err: " + databaseError);
                        }
                    };
                    users.addListenerForSingleValueEvent(getUser);
                    users.removeEventListener(getUser);
                }
            }
        });
    }

    public static boolean checkEmailAddress(String email) {
        String[] arrCheck = email.split("@");
        return arrCheck.length == 2;
    }

    class guiMail extends AsyncTask<Bundle, Void, Void> {

        @Override
        protected Void doInBackground(Bundle... bundles) {
            Bundle b = bundles[0];
            String tieuDe = b.getString("tieuDe");
            String noiDung = b.getString("noiDung");
            String emailTo = b.getString("emailTo");
            //System.out.println(tieuDe+"/"+noiDung+"/"+emailTo);
            try {
                // System.out.println("chay vo day");
                GMailSender sender = new GMailSender(Constant.EMAIL_SENDER, Constant.PASS_EMAIL_SENDER);
                sender.sendMail(tieuDe,
                        noiDung,
                        Constant.EMAIL_SENDER,
                        emailTo);
                dialog.dismiss();
                Intent confirm = new Intent(ForgetPasswordActivity.this, ConfirmCodeActivity.class);
                confirm.putExtra("user_forget", (Serializable) user);
                confirm.putExtra("isFromRegister", false);
                confirm.putExtra("code", code);
                System.out.println("code:" +code);
                startActivity(confirm);
                finish();
                //System.out.println("chay vo day");
            } catch (Exception e) {
                Log.e("SendMail", e.getMessage(), e);
                dialog.dismiss();
            }
            return null;
        }
    }
}
