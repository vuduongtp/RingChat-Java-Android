package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.vuvanduong.ringchat.util.MD5;

import java.util.Calendar;
import java.util.Date;

public class RegisterActivity extends AppCompatActivity {
    ImageView backToLogin;
    EditText txtUsernameRegister,txtPasswordRegister,
            txtConfirmPasswordRegister,txtLastnameRegister,
            txtFirstnameRegister;
    TextView txtErrorRegister,txtBirthdayRegister;
    Button btnRegister;
    LinearLayout layoutBirthday;
    Calendar calendar = Calendar.getInstance();
    private Date birthday = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    String email="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        addControl();
        addEvent();
    }

    private void addControl() {
        backToLogin=findViewById(R.id.btnBackToLogin);
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
                if(txtUsernameRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_email);
                }
                else if(!checkEmailAddress(txtUsernameRegister.getText().toString().trim())){
                    txtErrorRegister.setText(R.string.err_email_invalid);
                }
                else if(txtPasswordRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_pass);
                }
                else if(txtConfirmPasswordRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_confirm_pass);
                }
                else if(txtBirthdayRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_birthday);
                }
                else if(txtFirstnameRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_firstname);
                }
                else if(txtLastnameRegister.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorRegister.setText(R.string.err_emty_lastname);
                }
                else if(!txtConfirmPasswordRegister.getText().toString().trim().equalsIgnoreCase(txtPasswordRegister.getText().toString().trim())){
                    txtErrorRegister.setText(R.string.err_pass_not_match);
                }
                else {
                    email=txtUsernameRegister.getText().toString().trim();
                    ValueEventListener getUser = new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot item : dataSnapshot.getChildren()) {
                                User user = item.getValue(User.class);
                                assert user != null;
                                if (user.getEmail().equalsIgnoreCase(email)) {
                                    txtErrorRegister.setText(R.string.err_user_exist);
                                    return;
                                }
                            }
                            User userRegister = new User();
                            userRegister.setEmail(email);
                            userRegister.setPassword(MD5.getMd5(txtPasswordRegister.getText().toString().trim()));
                            userRegister.setFirstname(txtFirstnameRegister.getText().toString().trim());
                            userRegister.setLastname(txtLastnameRegister.getText().toString().trim());
                            userRegister.setBirthday(birthday);
                            users.push().setValue(userRegister);
                            Toast.makeText(RegisterActivity.this,R.string.register_success, Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            System.err.println("db_err: "+databaseError);
                        }
                    };
                    users.addListenerForSingleValueEvent(getUser);
                    users.removeEventListener(getUser);
                }
            }
        });

    }

    public static boolean checkEmailAddress(String email){
    String[] arrCheck = email.split("@");
        return arrCheck.length == 2;
    }


}
