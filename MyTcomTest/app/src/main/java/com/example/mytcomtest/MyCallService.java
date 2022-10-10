package com.example.mytcomtest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.telecom.DisconnectCause;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.text.HtmlCompat;

import java.util.Locale;

public class MyCallService extends Service {
    private static final String TAG = "MyCallService";
    private Function mConnectionListener;

    public final Function getmConnectionListener() {
        return mConnectionListener;
    }

    public void setConnectionListener(Function var1) {
        mConnectionListener = var1;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new MyCallService.CallServiceBinder();
    }

    public final void addConnection(MyTcomConnection newConnection) {
        Log.d(TAG, "addConnection start");
        this.mConnectionListener.apply(newConnection);
        //notificationEnqueue();
        Log.d(TAG, "addConnection end");
    }

    public final class CallServiceBinder extends Binder {
        public MyCallService getCallService() {
            return MyCallService.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Intent var1 = new Intent((Context) this, MyCallService.class);
        this.bindService(var1, (ServiceConnection)this.mServiceConnection, 1);
    }

    private CallServiceConnection mServiceConnection = new CallServiceConnection();
    private MyTcomConnection mConnection;

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
                    setConnection((MyTcomConnection) l);
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

    private void setConnection(MyTcomConnection newConnection) {
        this.mConnection = newConnection;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        Log.d(TAG, "do action : " + action);
        switch (action) {
            case "DEBUG_INCOMING":
                runDebugIncoming();
                break;
            case "DEBUG_DROP":
                runDebugDrop();
                break;
            default:
                Log.d(TAG, "unsupported action : " + action);
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void runDebugDrop() {
        closeConnection();
    }

    private void runDebugIncoming() {
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //startActivity(intent);
        //notificationEnqueue();
        if (getTcomManager().registerAccount()) {
            getTcomManager().addIncomingCall();
        }
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
    }

    private MyTcomManager mMyTcomManager;
    private final MyTcomManager getTcomManager() {
        if (mMyTcomManager == null) {
            mMyTcomManager = new MyTcomManager(this);
        }
        return mMyTcomManager;
    }


    private static final String CHANNEL_CALL_ID = "call";
    private void notificationEnqueue() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //Uri ringtoneUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ getApplicationContext().getPackageName() + "/" + R.raw.acheron);
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
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

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_CALL_ID);
        Notification notification = notificationBuilder
                .setSmallIcon(R.drawable.app_icon_small)  // ステータスバーで表示されるアプリアイコン
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))  // 通知領域内の通知で表示されるアイコン
                .setContentTitle("title")  // 通知タイトル
                .setContentText("message")  // 通知メッセージ
                .setPriority(NotificationCompat.PRIORITY_HIGH)  // プライオリティ (Android O 未満対応)
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setAutoCancel(true)  // タップしたら自動キャンセルする
                .setStyle(new NotificationCompat.BigTextStyle().bigText("message"))  // スタイル（通知領域内の通知で長い文字を表示する.）
                .setSound(ringtoneUri, AudioManager.STREAM_RING)
                .setFullScreenIntent(callPendingIntent(this, NotificationId.CALL.id, false), true)
                .addAction(R.drawable.refuse_button,
                        getColorString(this, R.string.button_refuse, R.color.colorRefuse),
                        refusePendingIntent(this, NotificationId.CALL_REFUSE.id))
                .addAction(R.drawable.accept_button,
                        getColorString(this, R.string.button_accept, R.color.colorAccept),
                        callPendingIntent(this, NotificationId.CALL_ACCEPT.id, true))
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
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT
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
