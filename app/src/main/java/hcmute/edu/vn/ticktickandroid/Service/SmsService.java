package hcmute.edu.vn.ticktickandroid.Service;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

public class SmsService extends Service {
    private static final String SMS_SENT = "SMS_SENT";
    private static final String SMS_DELIVERED = "SMS_DELIVERED";

    private BroadcastReceiver sentReceiver, deliveredReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        sentReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(context, "Đã gửi tin nhắn thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Lỗi gửi tin nhắn", Toast.LENGTH_SHORT).show();
                }
            }
        };

        deliveredReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(context, "Người nhận đã nhận được tin!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        // SỬ DỤNG ContextCompat ĐỂ TỰ ĐỘNG XỬ LÝ CỜ BẢO MẬT (RECEIVER_NOT_EXPORTED)
        // Cách này tương thích từ API 24 đến API mới nhất (Android 14+)
        ContextCompat.registerReceiver(this, sentReceiver, new IntentFilter(SMS_SENT), ContextCompat.RECEIVER_NOT_EXPORTED);
        ContextCompat.registerReceiver(this, deliveredReceiver, new IntentFilter(SMS_DELIVERED), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String phoneNumber = intent.getStringExtra("PHONE_NUMBER");
            String message = intent.getStringExtra("MESSAGE");
            if (phoneNumber != null && message != null) {
                sendSms(phoneNumber, message);
            }
        }
        return START_NOT_STICKY;
    }

    private void sendSms(String phoneNumber, String message) {
        try {
            // Vì minSdk của bạn là 24, FLAG_IMMUTABLE luôn khả dụng (có từ API 23)
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

            PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SMS_SENT), flags);
            PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(SMS_DELIVERED), flags);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sentReceiver != null) unregisterReceiver(sentReceiver);
        if (deliveredReceiver != null) unregisterReceiver(deliveredReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
