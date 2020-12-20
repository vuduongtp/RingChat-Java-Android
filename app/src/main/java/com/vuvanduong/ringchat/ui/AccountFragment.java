package com.vuvanduong.ringchat.ui;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.fragment.app.Fragment;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.squareup.picasso.Picasso;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.AboutActivity;
import com.vuvanduong.ringchat.activity.AddFriendActivity;
import com.vuvanduong.ringchat.activity.EditInforActivity;
import com.vuvanduong.ringchat.activity.EditPasswordActivity;
import com.vuvanduong.ringchat.activity.LoginActivity;
import com.vuvanduong.ringchat.activity.WelcomeActivity;
import com.vuvanduong.ringchat.app.InitialApp;
import com.vuvanduong.ringchat.config.Constant;
import com.vuvanduong.ringchat.database.UserLoginDB;
import com.vuvanduong.ringchat.model.User;
import com.vuvanduong.ringchat.service.LinphoneService;
import com.vuvanduong.ringchat.service.NetworkChangeService;
import com.vuvanduong.ringchat.util.CircleTransform;
import com.vuvanduong.ringchat.util.ImageUtils;
import com.vuvanduong.ringchat.util.NetworkUtil;
import com.vuvanduong.ringchat.util.SharedPrefs;
import com.vuvanduong.ringchat.util.UserUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AccountFragment extends Fragment {
    private View view;
    private User user;
    TextView txtNameAccount, txtEmailAccount;
    LinearLayout layoutEditInfo, layoutChangePass, layoutLanguage, layoutHelp, layoutAbout, layoutLogout, layoutChangeAvatar;
    ImageView btnSearchInAccount, imgMyAvatarAccount, imgQRGen;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference dbReference = database.getReference();
    private DatabaseReference users = dbReference.child("users");

    private static final int PERMISSION_CODE = 1;
    private static final int PICK_IMAGE = 1;
    int rotationInDegrees = 0, rotation = 0;
    ProgressDialog dialog;
    ProgressBar loadingQRCode;
    UserLoginDB userLoginDB;

    private Map<String, String> config = new HashMap<String, String>();

    private void configCloudinary() {
        config.put("cloud_name", "vuduongtp");
        config.put("api_key", "987439358416729");
        config.put("api_secret", "Uj9Jes5zUjtAnYLXd81uR5qnGts");
        try {
            MediaManager.init(Objects.requireNonNull(getActivity()), config);
        }catch (IllegalStateException ex){
            Log.e("IllegalStateException",ex.toString());
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission
                (Objects.requireNonNull(getActivity()),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
        ) {
            accessTheGallery();
        } else {
            ActivityCompat.requestPermissions(
                    Objects.requireNonNull(getActivity()),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_CODE
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                accessTheGallery();
            } else {
                Toast.makeText(Objects.requireNonNull(getActivity()), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
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

    private OnDataPass dataPasser;

    public interface OnDataPass {
        void onDataPass(User data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }

    public void passData(User data) {
        dataPasser.onDataPass(data);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_account, container, false);

        assert getArguments() != null;
        user = (User) getArguments().getSerializable("user_login");
        userLoginDB = new UserLoginDB(getActivity());

        configCloudinary();
        setControl(view);
        setEvent();
        return view;
    }

    private void setEvent() {
        layoutLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(getActivity()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(getActivity(), getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                try {
                                    users = dbReference.child("users/" + user.getId());
                                    users.child("status").setValue("Offline");

                                } catch (Exception ex) {
                                    System.err.println(ex.toString());
                                }
                                SharedPrefs.getInstance().put(Constant.IS_LOGIN, false);
                                SharedPrefs.getInstance().put(Constant.IS_SAVE_PASS, true);
                                Intent linphone = new Intent(getActivity(), LinphoneService.class);
                                getActivity().stopService(linphone);
                                Intent network = new Intent(getActivity(), NetworkChangeService.class);
                                getActivity().stopService(network);
                                userLoginDB.logout(user);

                                Intent login = new Intent(getActivity(), WelcomeActivity.class);
                                startActivity(login);
                                Objects.requireNonNull(getActivity()).finish();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Objects.requireNonNull(getActivity()).getString(R.string.confirm_logout))
                        .setPositiveButton(Objects.requireNonNull(getActivity()).getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(Objects.requireNonNull(getActivity()).getString(R.string.no), dialogClickListener).show();
            }
        });

        layoutEditInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editInfor = new Intent(getActivity(), EditInforActivity.class);
                editInfor.putExtra("user_login", (Serializable) user);
                startActivityForResult(editInfor, Constant.GET_NEW_USER_INFO);
            }
        });

        layoutChangePass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editPass = new Intent(getActivity(), EditPasswordActivity.class);
                editPass.putExtra("user_login", (Serializable) user);
                startActivityForResult(editPass, Constant.GET_NEW_USER_PASS);
            }
        });

        btnSearchInAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent search = new Intent(getActivity(), AddFriendActivity.class);
                search.putExtra("user_login", (Serializable) user);
                startActivity(search);
            }
        });

        imgMyAvatarAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(getActivity()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(getActivity(), getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPermission();
            }
        });

        layoutChangeAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (NetworkUtil.getConnectivityStatusString(getActivity()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    Toast.makeText(getActivity(), getString(R.string.network_disconnect), Toast.LENGTH_SHORT).show();
                    return;
                }
                requestPermission();
            }
        });

        imgQRGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog progressDialog=new ProgressDialog(getActivity());
                progressDialog.setMessage("Loading...");
                progressDialog.show();
                try {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            if (user.getId() == null || user.getId().equals("")) {
                                return;
                            }
                            Bitmap bitmap = null;
                            try {
                                bitmap = TextToImageEncode(user.getId());
                            } catch (WriterException e) {
                                e.printStackTrace();
                            }
                            final Bitmap finalBitmap = bitmap;
                            Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Dialog QRDialog = new Dialog(Objects.requireNonNull(getActivity()));
                                    Objects.requireNonNull(QRDialog.getWindow()).requestFeature(Window.FEATURE_NO_TITLE);
                                    QRDialog.setContentView(getLayoutInflater().inflate(R.layout.qrcode_dialog
                                            , null));
                                    QRDialog.show();
                                    ImageView imageQR = QRDialog.findViewById(R.id.imageQRDialog);
                                    ImageView imageQRAvatar = QRDialog.findViewById(R.id.QRDialogAvatar);
                                    TextView QRDialogFullName = QRDialog.findViewById(R.id.QRDialogFullName);
                                    Picasso.get()
                                            .load(user.getImage())
                                            .placeholder(R.drawable.user)
                                            .transform(new CircleTransform())
                                            .into(imageQRAvatar);

                                    imageQR.setImageBitmap(finalBitmap);
                                    QRDialogFullName.setText(UserUtil.getFullName(user));
                                    progressDialog.dismiss();
                                }
                            });

                        }
                    }, 100);

                } catch (Exception e) {
                    e.printStackTrace();
                    progressDialog.dismiss();
                }
            }
        });

        layoutAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about = new Intent(getActivity(), AboutActivity.class);
                startActivity(about);
            }
        });

        layoutHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                InitialApp.self().clearApplicationData();
                                try {
                                    users = dbReference.child("users/" + user.getId());
                                    users.child("status").setValue("Offline");

                                } catch (Exception ex) {
                                    System.err.println(ex.toString());
                                }
                                Intent login = new Intent(getActivity(), WelcomeActivity.class);
                                startActivity(login);
                                Objects.requireNonNull(getActivity()).finishAffinity();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                dialog.dismiss();
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(Objects.requireNonNull(getActivity()).getString(R.string.confirm_clear))
                        .setPositiveButton(Objects.requireNonNull(getActivity()).getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(Objects.requireNonNull(getActivity()).getString(R.string.no), dialogClickListener).show();
            }
        });

        layoutLanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent language = new Intent(getActivity(), DialogLanguage.class);
                language.putExtra("user_login", (Serializable) user);
                startActivity(language);
            }
        });

    }

    private Bitmap TextToImageEncode(String value) throws WriterException {
        Bitmap bmp = null;
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.rgb(7, 121, 228) : Color.WHITE);
                }
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }
        return bmp;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.GET_NEW_USER_INFO && data != null) {
            User newUser = (User) data.getSerializableExtra("userEdit");
            if (newUser != null) {
                user = newUser;
                setControl(view);
                passData(user);
            }
        }
        if (requestCode == Constant.GET_NEW_USER_PASS && data != null) {
            String password = data.getStringExtra("userPass");
            if (password != null) {
                user.setPassword(password);
                passData(user);
            }
        }

        if (requestCode == PICK_IMAGE && data != null) {
            try {
                ExifInterface exif = new ExifInterface(getRealPathFromUri(data.getData(), Objects.requireNonNull(getActivity())));
                rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                rotationInDegrees = ImageUtils.exifToDegrees(rotation);
                InputStream inputStream = Objects.requireNonNull(getActivity()).getContentResolver().openInputStream(Objects.requireNonNull(data.getData()));
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

        protected void onProgressUpdate() {
        }

        protected void onPostExecute() {
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
                            dialog = ProgressDialog.show(getActivity(), "",
                                    "", true);
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {
                        }

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
                            RoundedBitmapDrawable roundImage = ImageUtils.roundedImage(Objects.requireNonNull(getActivity()), bitmap);
                            imgMyAvatarAccount.setImageDrawable(roundImage);

                            users = dbReference.child("users/" + user.getId());
                            users.child("image").setValue(Objects.requireNonNull(resultData.get("url")).toString());
                            user.setImage(Objects.requireNonNull(resultData.get("url")).toString());
                            Toast.makeText(getActivity(),  getString(R.string.set_avatar_success), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
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

    private void setControl(View view) {

        txtNameAccount = view.findViewById(R.id.txtNameAccount);
        txtEmailAccount = view.findViewById(R.id.txtEmailAccount);
        layoutEditInfo = view.findViewById(R.id.layoutEditInfo);
        layoutChangePass = view.findViewById(R.id.layoutChangePass);
        layoutLanguage = view.findViewById(R.id.layoutLanguage);
        layoutHelp = view.findViewById(R.id.layoutHelp);
        layoutAbout = view.findViewById(R.id.layoutAbout);
        layoutLogout = view.findViewById(R.id.layoutLogout);
        btnSearchInAccount = view.findViewById(R.id.btnSearchInAccount);
        imgMyAvatarAccount = view.findViewById(R.id.imgMyAvatarAccount);
        layoutChangeAvatar = view.findViewById(R.id.layoutChangeAvatar);
        imgQRGen = view.findViewById(R.id.imgQRGen);
        loadingQRCode = view.findViewById(R.id.loadingQRCode);
        txtNameAccount.setText(UserUtil.getFullName(user));
        txtEmailAccount.setText(user.getEmail());
        Picasso.get()
                .load(user.getImage())
                .placeholder(R.drawable.user)
                .transform(new CircleTransform())
                .into(imgMyAvatarAccount);

    }
}
