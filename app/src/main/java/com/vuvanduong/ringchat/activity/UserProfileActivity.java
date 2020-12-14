package com.vuvanduong.ringchat.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.model.Message;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.DBUtil;
import com.vuvanduong.ringchat.util.ImageUtils;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.UserUtil;

import org.linphone.core.tools.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

public class UserProfileActivity extends AppCompatActivity {
    ImageView imgProfileAvatar, imgProfileBack, btnProfileChat, btnProfileUnfriend, btnProfileAddFriend;
    TextView txtNameUserProfile, txtUserProfileInfo;
    User userLogin, userScan;
    boolean isFriend, isUserLogin;

    private static final int PERMISSION_CODE = 3;
    private static final int PICK_IMAGE = 3;
    int rotationInDegrees = 0, rotation = 0;
    ProgressDialog dialog;

    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");
    private DatabaseReference userContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        setControl();
        setEvent();
    }

    private void setEvent() {
        imgProfileBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        imgProfileAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //requestPermission();
                if (userLogin.getImage() != null) {
                    Intent viewImage = new Intent(UserProfileActivity.this, ViewImageActivity.class);
                    viewImage.putExtra("url", userScan.getImage());
                    startActivity(viewImage);
                }
            }
        });
        btnProfileChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent conversation = new Intent(UserProfileActivity.this, ConversationActivity.class);
                conversation.putExtra("userLogin", (Serializable) userLogin);
                conversation.putExtra("friend", (Serializable) userScan);
            }
        });

        btnProfileUnfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (NetworkUtil.getConnectivityStatusString(UserProfileActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                                    Toast.makeText(UserProfileActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                userContacts.child(userScan.getId()).removeValue();
                                btnProfileUnfriend.setVisibility(View.GONE);
                                Toast.makeText(UserProfileActivity.this, R.string.remove_friend_success, Toast.LENGTH_SHORT).show();
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

        btnProfileAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(UserProfileActivity.this) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    Toast.makeText(UserProfileActivity.this, getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                userContacts.child(userScan.getId()).setValue(userScan.getId());
                v.setVisibility(View.GONE);
                Toast.makeText(UserProfileActivity.this, R.string.add_friend_success, Toast.LENGTH_SHORT).show();
            }
        });

        btnProfileAddFriend.setVisibility(View.GONE);
        btnProfileUnfriend.setVisibility(View.GONE);
        if (isUserLogin) {
            btnProfileChat.setVisibility(View.GONE);
            btnProfileAddFriend.setVisibility(View.GONE);
            btnProfileUnfriend.setVisibility(View.GONE);
        }else {
            if (isFriend) {
                btnProfileAddFriend.setVisibility(View.GONE);
            } else {
                btnProfileChat.setVisibility(View.GONE);
                btnProfileUnfriend.setVisibility(View.GONE);
            }
            btnProfileChat.setVisibility(View.VISIBLE);
        }
    }

    private void setControl() {
        imgProfileAvatar = findViewById(R.id.imgProfileAvatar);
        imgProfileBack = findViewById(R.id.imgProfileBack);
        btnProfileChat = findViewById(R.id.btnProfileChat);
        btnProfileUnfriend = findViewById(R.id.btnProfileUnfriend);
        btnProfileAddFriend = findViewById(R.id.btnProfileAddFriend);
        txtNameUserProfile = findViewById(R.id.txtNameUserProfile);
        txtUserProfileInfo = findViewById(R.id.txtUserProfileInfo);

        Intent intent = getIntent();
        userLogin = (User) intent.getSerializableExtra("user_login");
        userScan = (User) intent.getSerializableExtra("user_scan");
        isFriend = intent.getBooleanExtra("isScanFriend", false);
        isUserLogin = intent.getBooleanExtra("isUserLogin", false);
        userContacts = dbReference.child("contacts/" + userLogin.getId() + "/");

        txtNameUserProfile.setText(UserUtil.getFullName(userScan));
        String info = getResources().getString(R.string.firstname) + " : " + userScan.getFirstname() + "\n"
                + getResources().getString(R.string.lastname) + " : " + userScan.getLastname() + "\n"
                + getResources().getString(R.string.birthday) + " : " + userScan.getBirthday() + "\n";
        txtUserProfileInfo.setText(info);
        if (userScan.getImage() != null && !userScan.getImage().equalsIgnoreCase("")) {
            Picasso.with(this)
                    .load(userScan.getImage())
                    .placeholder(R.drawable.user)
                    .transform(new CircleTransform())
                    .into(imgProfileAvatar);
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission
                (Objects.requireNonNull(UserProfileActivity.this),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(
                    Objects.requireNonNull(UserProfileActivity.this),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );
        }
    }

    public void accessTheGallery() {
        Intent i = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery();
            } else {
                Toast.makeText(Objects.requireNonNull(UserProfileActivity.this), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            try {
                dialog = ProgressDialog.show(UserProfileActivity.this, "",
                        "", true);
                dialog.show();
                ExifInterface exif = new ExifInterface(getRealPathFromUri(data.getData(), Objects.requireNonNull(UserProfileActivity.this)));
                rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                rotationInDegrees = ImageUtils.exifToDegrees(rotation);
                InputStream inputStream = Objects.requireNonNull(UserProfileActivity.this).getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
                new ImageProcessUpload().execute(inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getRealPathFromUri(Uri imageUri, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(imageUri, null, null, null, null);
        if (cursor == null) {
            return imageUri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }

    private class ImageProcessUpload extends AsyncTask<InputStream, Void, Void> {
        protected Void doInBackground(InputStream... inputStream) {
            try {
                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream[0]);
                Bitmap liteImage = ImageUtils.getResizedBitmap(imgBitmap, ImageUtils.AVATAR_WIDTH);
                liteImage = ImageUtils.cropToSquare(liteImage);
                Matrix matrix = new Matrix();
                if (rotation != 0f) {
                    matrix.preRotate(rotationInDegrees);
                }
                rotation = 0;
                rotationInDegrees = 0;
                liteImage = Bitmap.createBitmap(liteImage, 0, 0, liteImage.getWidth(), liteImage.getHeight(), matrix, true);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                liteImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                uploadByteToCloudinary(byteArray);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    private void uploadByteToCloudinary(final byte[] image) {
        try {
            MediaManager.get().upload(image)
                    .option("tags", "profile")
                    .option("folder", "profile")
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                            RoundedBitmapDrawable roundImage = ImageUtils.roundedImage(Objects.requireNonNull(UserProfileActivity.this), bitmap);
                            imgProfileAvatar.setImageDrawable(roundImage);

                            users = dbReference.child("users/" + userLogin.getId());
                            users.child("image").setValue(Objects.requireNonNull(resultData.get("url")).toString());
                            userLogin.setImage(Objects.requireNonNull(resultData.get("url")).toString());
                            Intent intent = new Intent();
                            intent.putExtra("avatar", resultData.get("url").toString());
                            setResult(Constant.GET_GET_AVATAR, intent);
                            Toast.makeText(UserProfileActivity.this, getString(R.string.set_avatar_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Log.e("upload_image", error.toString());
                            dialog.dismiss();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {
                        }
                    }).dispatch();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}