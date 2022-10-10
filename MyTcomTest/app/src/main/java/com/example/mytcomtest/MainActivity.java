package com.example.mytcomtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telecom.Connection;
import android.telecom.DisconnectCause;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CallServiceConnection mServiceConnection = new CallServiceConnection();
    private MyTcomConnection mConnection;
    private MyTcomManager mMyTcomManager;
    private TextView mStateLabel;
    private static final String[] REQUIRED_PERMISSIONS;
    static {
        if (Build.VERSION.SDK_INT >= 31) {
            REQUIRED_PERMISSIONS = new String[] {
                Manifest.permission.READ_PHONE_NUMBERS
            };
        } else {
            REQUIRED_PERMISSIONS = new String[] {};
        }
    }
    private static final int REQUEST_INCOMING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        findViewById(R.id.drop_btn).setOnClickListener(l -> {
            Log.d(TAG, "drop_btn clicked");
            closeConnection();
        });
        findViewById(R.id.activate_btn).setOnClickListener(l -> {
            Log.d(TAG, "activate_btn clicked");
            Connection connection = mConnection;
            if (connection != null) {
                connection.setActive();
            } else {
                Toast.makeText(this, "there is no call", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.incoming_btn).setOnClickListener(l -> {
            Log.d(TAG, "incoming_btn clicked");
            onIncommingButton();
        });
        mStateLabel = findViewById(R.id.state_label);
        Log.d(TAG, "onCreate end");
    }

    private void onIncommingButton() {
        if (!hasPermissions()) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_INCOMING);
            return;
        }
        try {
            if (MainActivity.this.mConnection != null) {
                MainActivity.this.closeConnection();
            }

            if (MainActivity.this.getTcomManager().registerAccount()) {
                MainActivity.this.getTcomManager().addIncomingCall();
            } else {
                Toast.makeText(MainActivity.this.getApplicationContext(), (CharSequence)"account isn't registered", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception var5) {
            Toast.makeText(MainActivity.this.getApplicationContext(), (CharSequence)var5.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private final MyTcomManager getTcomManager() {
        if (mMyTcomManager == null) {
            mMyTcomManager = new MyTcomManager(this);
        }
        return mMyTcomManager;
    }

    protected void onStart() {
        Log.d(TAG, "onStart start");
        super.onStart();
        Intent var1 = new Intent((Context) this, MyCallService.class);
        this.bindService(var1, (ServiceConnection)this.mServiceConnection, BIND_AUTO_CREATE);
        Log.d(TAG, "onStart end");
    }

    protected void onStop() {
        Log.d(TAG, "onStop start");
        super.onStop();
        this.closeConnection();
        this.unbindService((ServiceConnection)this.mServiceConnection);
        Log.d(TAG, "onStop end");
    }

    private boolean hasPermissions() {
        boolean allGranted = true;
        for (String it : REQUIRED_PERMISSIONS) {
            if (checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
            }
        }
        return allGranted;
    }

    private void closeConnection() {
        MyTcomConnection var10000 = this.mConnection;
        if (var10000 != null) {
            MyTcomConnection var1 = var10000;
            if (!var1.isClosed()) {
                var1.setDisconnected(new DisconnectCause(4));
            }

            var1.setListener(null);
        }

        String stateText = "state: no call";
        TextView var5 = mStateLabel;
        var5.setText((CharSequence)stateText);
    }

    private final void addConnection(MyTcomConnection newConnection) {
        Log.d(TAG, "addConnection called, newConnection=" + newConnection);
        newConnection.setListener(it -> {
                String stateText = "state: " + Connection.stateToString((Integer) it);
                TextView var10000 = mStateLabel;
                var10000.setText((CharSequence)stateText);
            return it;
        });
        this.mConnection = newConnection;
        String stateText = "state: " + Connection.stateToString(newConnection.getState());
        TextView var3 = mStateLabel;
        var3.setText((CharSequence)stateText);
        //notificationEnqueue();
    }

    public final class CallServiceConnection implements ServiceConnection {
        private MyCallService callService;

        public void onServiceConnected(ComponentName name, IBinder binder) {
            Log.d(TAG, "CallServiceConnection onServiceConnected start");
            if (binder == null) {
                throw new NullPointerException("null cannot be cast to non-null type com.example.tcomtest.CallService.CallServiceBinder");
            } else {
                MyCallService.CallServiceBinder callSrvBinder = (MyCallService.CallServiceBinder) binder;
                MyCallService service = callSrvBinder.getCallService();
                service.setConnectionListener(l -> {
                    Log.d(TAG, "CallServiceConnection connection listener start");
                    MainActivity.this.addConnection((MyTcomConnection) l);
                    Log.d(TAG, "CallServiceConnection connection listener end");
                    return l;
                });
                this.callService = service;
            }
            Log.d(TAG, "CallServiceConnection onServiceConnected end");
        }

        public void onServiceDisconnected(ComponentName name) {
            MyCallService var10000 = this.callService;
            if (var10000 != null) {
                var10000.setConnectionListener(null);
            }

        }
    }


    private static final String CHANNEL_CALL_ID = "call";
    private void notificationEnqueue() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Uri ringtoneUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.acheron);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        Log.d(TAG, "RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE), ringtoneUri=" + ringtoneUri);
        // Android O 以降はチャンネルを実装する必要がある.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_CALL_ID, "title", NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            channel.setSound(ringtoneUri, audioAttributes);
            manager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_CALL_ID)
                .setSmallIcon(R.drawable.app_icon_small)  // ステータスバーで表示されるアプリアイコン
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.app_icon))
                .setContentTitle("title")  // 通知タイトル
                .setContentText("message")  // 通知メッセージ
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // プライオリティ (Android O 未満対応)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)  // タップしたら自動キャンセルする
                .setStyle(new NotificationCompat.BigTextStyle().bigText("message"))  // スタイル（通知領域内の通知で長い文字を表示する.）
                .setSound(ringtoneUri, AudioManager.STREAM_RING)
                .addAction(
                        R.drawable.refuse_button,
                        getColorString(this, R.string.button_refuse, R.color.colorRefuse),
                        refusePendingIntent(this, NotificationId.CALL_REFUSE.id)
                )
                .addAction(
                        R.drawable.accept_button,
                        getColorString(this, R.string.button_accept, R.color.colorAccept),
                        callPendingIntent(this, NotificationId.CALL_ACCEPT.id, true)
                )
                .setFullScreenIntent(callPendingIntent(this, NotificationId.CALL.id, false), true)
                .build();

        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_INSISTENT;
        manager.notify(NotificationId.CALL.id, notification);
    }

    public enum NotificationId {
        CALL(-1000),
        CALL_ACCEPT(-1001),
        CALL_REFUSE(-1010),
        ;

        private int id;
        NotificationId(int id) {
            this.id = id;
        }
    }

    private PendingIntent defaultPendingIntent(Context context, int notificationId) {
        return PendingIntent.getActivity(
                this,
                notificationId,
                getPackageManager().getLaunchIntentForPackage(getPackageName()),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT
        );
    }

    private PendingIntent callPendingIntent(Context context, int notificationId, boolean accepted) {
        return PendingIntent.getActivity(
                this,
                notificationId,
                new Intent(context, MainActivity.class).putExtra("CALL_ACCEPTED", accepted),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private PendingIntent refusePendingIntent(Context context, int notificationId) {
        return PendingIntent.getActivity(
                this,
                notificationId,
                new Intent(context, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    private CharSequence getColorString(Context context, int stringResId, int colorResId) {
        return HtmlCompat.fromHtml(
                String.format(
                        Locale.US, "<font color=\"%d\">%s</font>",
                        ContextCompat.getColor(context, colorResId),
                        context.getString(stringResId)
                ), HtmlCompat.FROM_HTML_MODE_LEGACY
        );
    }

}