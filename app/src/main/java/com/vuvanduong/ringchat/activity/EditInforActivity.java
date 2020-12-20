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
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.MD5;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class EditInforActivity extends AppCompatActivity {
    private User user;
    Calendar calendar = Calendar.getInstance();
    private Date birthday = null;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    ImageView imageAvatarChangeInfor,btnBackToChangeInfor;
    TextView txtNameChangeInfor,txtErrorChangeInfor,txtBirthdayChangeInfor;
    EditText txtLastnameEditInfor,txtFirstnameEditInfor;
    Button btnEditInfor;
    LinearLayout layoutBirthdayEditInfor;
    String id="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_infor);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("user_login");
        setControl();
        setEvent();
    }

    private void setEvent() {
        layoutBirthdayEditInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(Calendar.YEAR, year);
                        calendar.set(Calendar.MONTH, month);
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        birthday = calendar.getTime();
                        txtBirthdayChangeInfor.setText(Constant.sdf.format(calendar.getTime()));
                        txtBirthdayChangeInfor.setTextColor(Color.BLACK);
                    }
                };
                DatePickerDialog datePickerDialog = new DatePickerDialog(EditInforActivity.this, callBack,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis() - 1000);
                datePickerDialog.show();
            }
        });

        btnEditInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtBirthdayChangeInfor.getText().toString().trim().equalsIgnoreCase("") || txtBirthdayChangeInfor.getText().toString().trim().equalsIgnoreCase("Birthday")){
                    txtErrorChangeInfor.setText(R.string.err_emty_birthday);
                }
                else if(txtFirstnameEditInfor.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorChangeInfor.setText(R.string.err_emty_firstname);
                }
                else if(txtLastnameEditInfor.getText().toString().trim().equalsIgnoreCase("")){
                    txtErrorChangeInfor.setText(R.string.err_emty_lastname);
                }
                else {
                    if (NetworkUtil.getConnectivityStatusString(EditInforActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                        Toast.makeText(EditInforActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    user.setFirstname(txtFirstnameEditInfor.getText().toString().trim());
                    user.setLastname(txtLastnameEditInfor.getText().toString().trim());
                    user.setBirthday(txtBirthdayChangeInfor.getText().toString());
                    user.setId(null);
                    users.child(id).setValue(user);
                    Toast.makeText(EditInforActivity.this,R.string.edit_infor_success, Toast.LENGTH_SHORT).show();
                    user.setId(id);
                    Intent intent=new Intent();
                    intent.putExtra("userEdit", (Serializable) user);
                    setResult(Constant.GET_NEW_USER_INFO,intent);
                    finish();
                }
            }
        });

        btnBackToChangeInfor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void setControl() {
        imageAvatarChangeInfor = findViewById(R.id.imageAvatarChangeInfor);
        txtNameChangeInfor = findViewById(R.id.txtNameChangeInfor);
        txtErrorChangeInfor = findViewById(R.id.txtErrorChangeInfor);
        txtBirthdayChangeInfor = findViewById(R.id.txtBirthdayChangeInfor);
        txtLastnameEditInfor = findViewById(R.id.txtLastnameEditInfor);
        txtFirstnameEditInfor = findViewById(R.id.txtFirstnameEditInfor);
        layoutBirthdayEditInfor = findViewById(R.id.layoutBirthdayEditInfor);
        btnBackToChangeInfor = findViewById(R.id.btnBackToChangeInfor);
        id = user.getId();
        btnEditInfor = findViewById(R.id.btnEditInfor);
        txtNameChangeInfor.setText(UserUtil.getFullName(user));
        txtBirthdayChangeInfor.setText(user.getBirthday());
        txtFirstnameEditInfor.setText(user.getFirstname());
        txtLastnameEditInfor.setText(user.getLastname());
        Picasso.get()
                .load(user.getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(imageAvatarChangeInfor);
    }
}
