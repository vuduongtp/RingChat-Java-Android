package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    ImageView backToLogin;
    EditText txtUsernameRegister, txtPasswordRegister,
            txtConfirmPasswordRegister, txtLastnameRegister,
            txtFirstnameRegister;
    TextView txtErrorRegister, txtBirthdayRegister;
    Button btnRegister;
    LinearLayout layoutBirthday;
    Calendar calendar = Calendar.getInstance();
    private Date birthday = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    String email = "";
    ProgressDialog dialog;
    User user;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        addControl();
        addEvent();
    }

    private void addControl() {
        backToLogin = findViewById(R.id.btnBackToLogin);
        txtUsernameRegister = findViewById(R.id.txtUsernameRegister);
        txtPasswordRegister = findViewById(R.id.txtPasswordRegister);
        txtConfirmPasswordRegister = findViewById(R.id.txtConfirmPasswordRegister);
        txtLastnameRegister = findViewById(R.id.txtLastnameRegister);
        txtFirstnameRegister = findViewById(R.id.txtFirstnameRegister);
        txtBirthdayRegister = findViewById(R.id.txtBirthdayRegister);
        txtErrorRegister = findViewById(R.id.txtErrorRegister);
        btnRegister = findViewById(R.id.btnRegister);
        layoutBirthday = findViewById(R.id.layoutBirthday);
    }

    private void addEvent() {
        backToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        layoutBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        birthday = calendar.getTime();
                        txtBirthdayRegister.setText(Constant.sdf.format(calendar.getTime()));
                        txtBirthdayRegister.setTextColor(Color.WHITE);
                        //Toast.makeText(RegisterActivity.this, birthday.toString(), Toast.LENGTH_SHORT).show();
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(RegisterActivity.this, callBack,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = ProgressDialog.show(RegisterActivity.this, "",
                        getString(R.string.loading), true);
                if (txtUsernameRegister.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorRegister.setText(R.string.err_emty_email);
                    dialog.dismiss();
                } else if (!checkEmailAddress(txtUsernameRegister.getText().toString().trim())) {
                    txtErrorRegister.setText(R.string.err_email_invalid);
                    dialog.dismiss();
                } else if (txtPasswordRegister.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorRegister.setText(R.string.err_emty_pass);
                    dialog.dismiss();
                } else if (txtConfirmPasswordRegister.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorRegister.setText(R.string.err_emty_confirm_pass);
                    dialog.dismiss();
                } else if (birthday == null || txtBirthdayRegister.getText().toString().trim().equalsIgnoreCase("") || txtBirthdayRegister.getText().toString().trim().equalsIgnoreCase("Birthday")) {
                    txtErrorRegister.setText(R.string.err_emty_birthday);
                    dialog.dismiss();
                } else if (txtFirstnameRegister.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorRegister.setText(R.string.err_emty_firstname);
                    dialog.dismiss();
                } else if (txtLastnameRegister.getText().toString().trim().equalsIgnoreCase("")) {
                    txtErrorRegister.setText(R.string.err_emty_lastname);
                    dialog.dismiss();
                } else if (!txtConfirmPasswordRegister.getText().toString().trim().equalsIgnoreCase(txtPasswordRegister.getText().toString().trim())) {
                    txtErrorRegister.setText(R.string.err_pass_not_match);
                    dialog.dismiss();
                } else {
                    email = txtUsernameRegister.getText().toString().trim();
                    ValueEventListener getUser = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                User user = item.getValue(User.class);
                                assert user != null;
                                if (user.getEmail().equalsIgnoreCase(email)) {
                                    txtErrorRegister.setText(R.string.err_user_exist);
                                    dialog.dismiss();
                                    return;
                                }
                            }
                            User userRegister = new User();
                            userRegister.setEmail(email);
                            userRegister.setPassword(MD5.getMd5(txtPasswordRegister.getText().toString().trim()));
                            userRegister.setFirstname(txtFirstnameRegister.getText().toString().trim());
                            userRegister.setLastname(txtLastnameRegister.getText().toString().trim());
                            userRegister.setBirthday(DBUtil.convertDatetimeToString(birthday));
                            user = userRegister;
//                            users.push().setValue(userRegister);
//                            Toast.makeText(RegisterActivity.this,R.string.register_success, Toast.LENGTH_SHORT).show();
//                            dialog.dismiss();
//                            finish();
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
                            code = DBUtil.getAlphaNumericString(6);
                            String replace = tContents.replace("%", "phantram");
                            replace = replace.replace("phantrams", "%s");
                            String noiDung = String.format(replace, UserUtil.getFullName(user), code);
                            noiDung = noiDung.replace("phantram", "%");

                            String tieuDe = "Xác nhận đăng ký tài khoản RingChat";
                            String emailTo = txtUsernameRegister.getText().toString().trim();
                            Bundle bundle = new Bundle();
                            bundle.putString("tieuDe", tieuDe);
                            bundle.putString("noiDung", noiDung);
                            bundle.putString("emailTo", emailTo);

                            guiMail myTask = new guiMail();
                            myTask.execute(bundle);
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
                Intent confirm = new Intent(RegisterActivity.this, ConfirmCodeActivity.class);
                confirm.putExtra("user_register", (Serializable) user);
                confirm.putExtra("isFromRegister", true);
                confirm.putExtra("code", code);
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
