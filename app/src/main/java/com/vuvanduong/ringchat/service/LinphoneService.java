package com.vuvanduong.ringchat.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vuvanduong.ringchat.R;
import com.vuvanduong.ringchat.activity.CallIncomingActivity;
import com.vuvanduong.ringchat.activity.CallOutgoingActivity;
import com.vuvanduong.ringchat.activity.HomeActivity;
import com.vuvanduong.ringchat.config.Constant;

import org.linphone.core.Call;
import org.linphone.core.ChatMessage;
import org.linphone.core.ChatRoom;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.Factory;
import org.linphone.core.LogCollectionState;
import org.linphone.core.ProxyConfig;
import org.linphone.core.RegistrationState;
import org.linphone.core.tools.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

public class LinphoneService extends Service {
    private static final String START_LINPHONE_LOGS = " ==== Device information dump ====";
    // Keep a static reference to the Service so we can access it from anywhere in the app
    private static LinphoneService sInstance;
    private Handler mHandle;
    private Timer mTimer;
    public String username;
    private Core mCore;
    private CoreListenerStub mCoreListener;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference dbReference = database.getReference();
    DatabaseReference users = dbReference.child("users");
    private NotificationManager notifyManager;
    private boolean isGroup = false;
    private String groupName;
    static final int NOTIFY_ID = 1103;
    // There are hardcoding only for show it's just strings
    String name = "RingChat";
    String id = "RingChat"; // The user-visible name of the channel.
    String description = "RingChat"; // The user-visible description of the channel.

    public static boolean isReady() {
        return sInstance != null;
    }

    public static LinphoneService getInstance() {
        return sInstance;
    }

    public static Core getCore() {
        return sInstance.mCore;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // The first call to liblinphone SDK MUST BE to a Factory method
        // So let's enable the library debug logs & log collection
        String basePath = getFilesDir().getAbsolutePath();
        Factory.instance().setLogCollectionPath(basePath);
        Factory.instance().enableLogCollection(LogCollectionState.Enabled);
        Factory.instance().setDebugMode(true, getString(R.string.app_name));
        // Dump some useful information about the device we're running on
        createNotificationChannel();
        mHandle = new Handler();
        // This will be our main Core listener, it will change activities depending on events
        mCoreListener = new CoreListenerStub() {
            @Override
            public void onRegistrationStateChanged(Core lc, ProxyConfig cfg, RegistrationState cstate, String message) {
                final String username1 = cfg.getContact().getUsername();
                if (cstate == RegistrationState.Ok) {
                    try {
                        username=username1;
                        users = dbReference.child("users/"+username1);
                        users.child("status").setValue("Online");

                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                    }
                } else if (cstate == RegistrationState.Failed) {
                    Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
                } else if (cstate == RegistrationState.Cleared) {
                    try {
                        users = dbReference.child("users/"+username);
                        users.child("status").setValue("Offline");

                    } catch (Exception ex) {
                        System.err.println(ex.toString());
                    }
                    Intent intent = new Intent(LinphoneService.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }

            @Override
            public void onMessageReceived(Core lc, ChatRoom room, ChatMessage message) {
                if (room != null) {
                   System.out.println("coi mess "+message.getCustomHeader("group")+"/"+message.getFromAddress().getUsername());
                    if (message.hasTextContent()) {
                        if (message.getCustomHeader("group") == null) {
                            isGroup = false;
                        } else {
                            groupName = message.getCustomHeader("group");
                            isGroup = true;
                        }

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), id);

                        Intent intent = new Intent(getApplicationContext(), getClass());
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);

                        if (isGroup) {
                            builder.setContentTitle(groupName)  // required
                                    .setSmallIcon(R.drawable.send) // required
                                    .setContentText(getString(R.string.someone)+": "
                                            + message.getTextContent())  // required
                                    .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                    .setAutoCancel(true)
                                    .setContentIntent(pendingIntent);
                        } else if (!message.getFromAddress().getUsername().equals(username)) {
                            if (message.getCustomHeader("group") == null) {
                                builder.setContentTitle(message.getCustomHeader("fullNameFriend"))  // required
                                        .setSmallIcon(R.drawable.send) // required
                                        .setContentText(message.getTextContent())  // required
                                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                                        .setAutoCancel(true)
                                        .setContentIntent(pendingIntent);
                            }
                        }
                        Notification notification = builder.build();
                        notifyManager.notify(NOTIFY_ID, notification);
                    }
                }
            }


            @SuppressLint("LongLogTag")
            @Override
            public void onCallStateChanged(Core core, Call call, Call.State state, String message) {
                //Toast.makeText(LinphoneService.this, message, Toast.LENGTH_SHORT).show();

                if (sInstance == null) {
                    Log.i(
                            "[Service] Service not ready, discarding call state change to ",
                            state.toString());
                    return;
                }

                if (state == Call.State.IncomingReceived) {
                    onIncomingCallReceive();
                } else if (state == Call.State.OutgoingInit) {
                    // This stats means the call has been established, let's start the call activity
                    Intent intent = new Intent().setClass(LinphoneService.this, CallOutgoingActivity.class);
                    // As it is the Service that is starting the activity, we have to give this flag
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else if (state == Call.State.End
                        || state == Call.State.Released
                        || state == Call.State.Error) {
                    mCore.getCalls()[0].terminate();
                }
            }
        };

        try {
            // Let's copy some RAW resources to the device
            // The default config file must only be installed once (the first time)
            copyIfNotExist(R.raw.linphonerc_default, basePath + "/.linphonerc");
            // The factory config is used to override any other setting, let's copy it each time
            copyFromPackage(R.raw.linphonerc_factory, "linphonerc");
        } catch (IOException ioe) {
            Log.e(ioe);
        }

        // Create the Core and add our listener
        mCore = Factory.instance()
                .createCore(basePath + "/.linphonerc", basePath + "/linphonerc", this);
        mCore.addListener(mCoreListener);
        // Core is ready to be configured
        configureCore();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // If our Service is already running, no need to continue
        if (sInstance != null) {
            return START_STICKY;
        }

        // Our Service has been started, we can keep our reference on it
        // From now one the Launcher will be able to call onServiceReady()
        sInstance = this;

        // Core must be started after being created and configured
        mCore.start();
        // We also MUST call the iterate() method of the Core on a regular basis
        TimerTask lTask =
                new TimerTask() {
                    @Override
                    public void run() {
                        mHandle.post(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mCore != null) {
                                            mCore.iterate();
                                        }
                                    }
                                });
                    }
                };
        mTimer = new Timer("Linphone scheduler");
        mTimer.schedule(lTask, 0, 20);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //set offline
        if (mCore.getDefaultProxyConfig() != null) {
            try {
                String username = mCore.getDefaultProxyConfig().getContact().getUsername();
                users = dbReference.child("users/"+username);
                users.child("status").setValue("Offline");

            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }

        // Deregister when kill app
        if (mCore.getDefaultProxyConfig() != null) {
            ProxyConfig cfg = mCore.getDefaultProxyConfig();
            cfg.edit();
            cfg.setExpires(0);
            cfg.enableRegister(false);
            cfg.done();
            mCore.removeListener(mCoreListener);
            mCore.setDefaultProxyConfig(null);
            mCore.stop();
        }
        if (mTimer != null)
            mTimer.cancel();
        if (notifyManager != null)
            notifyManager = null;

        // A stopped Core can be started again
        // To ensure resources are freed, we must ensure it will be garbage collected
        mCore = null;
        // Don't forget to free the singleton as well
        sInstance = null;

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // For this sample we will kill the Service at the same time we kill the app
        stopSelf();
        super.onTaskRemoved(rootIntent);
    }

    private void configureCore() {
        // We will create a directory for user signed certificates if needed
        String basePath = getFilesDir().getAbsolutePath();
        String userCerts = basePath + "/user-certs";
        File f = new File(userCerts);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e(userCerts + " can't be created.");
            }
        }
        mCore.setUserCertificatesPath(userCerts);

        String[] DnsServer = {Constant.SIP_SERVER, "8.8.8.8"};
        mCore.setDnsServersApp(DnsServer);
    }

    private void copyIfNotExist(int ressourceId, String target) throws IOException {
        File lFileToCopy = new File(target);
        if (!lFileToCopy.exists()) {
            copyFromPackage(ressourceId, lFileToCopy.getName());
        }
    }

    private void copyFromPackage(int ressourceId, String target) throws IOException {
        FileOutputStream lOutputStream = openFileOutput(target, 0);
        InputStream lInputStream = getResources().openRawResource(ressourceId);
        int readByte;
        byte[] buff = new byte[8048];
        while ((readByte = lInputStream.read(buff)) != -1) {
            lOutputStream.write(buff, 0, readByte);
        }
        lOutputStream.flush();
        lOutputStream.close();
        lInputStream.close();
    }

    private void createNotificationChannel() {
        if (notifyManager == null) {
            notifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            assert notifyManager != null;
            NotificationChannel mChannel = notifyManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, name, importance);
                mChannel.setDescription(description);
                mChannel.setLightColor(Color.GREEN);
                notifyManager.createNotificationChannel(mChannel);
            }
        }
    }

    private void onIncomingCallReceive() {
        Intent intent = new Intent().setClass(this, CallIncomingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
