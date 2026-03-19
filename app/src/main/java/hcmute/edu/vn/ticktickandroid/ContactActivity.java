package hcmute.edu.vn.ticktickandroid;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.ticktickandroid.Service.SmsService;

public class ContactActivity extends AppCompatActivity {

    private ListView lvContacts;
    private List<String> contactList = new ArrayList<>();
    private List<String> phoneList = new ArrayList<>();
    private String taskMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        taskMessage = getIntent().getStringExtra("TASK_CONTENT");
        lvContacts = findViewById(R.id.lv_contacts);

        loadContacts();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        lvContacts.setAdapter(adapter);

        lvContacts.setOnItemClickListener((parent, view, position, id) -> {
            String phoneNumber = phoneList.get(position);
            sendTaskViaSms(phoneNumber);
        });
    }

    private void loadContacts() {
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                
                if (nameIndex != -1 && phoneIndex != -1) {
                    String name = cursor.getString(nameIndex);
                    String phone = cursor.getString(phoneIndex);
                    contactList.add(name + "\n" + phone);
                    phoneList.add(phone);
                }
            }
            cursor.close();
        }
    }

    private void sendTaskViaSms(String phoneNumber) {
        Intent intent = new Intent(this, SmsService.class);
        intent.putExtra("PHONE_NUMBER", phoneNumber);
        intent.putExtra("MESSAGE", "Task từ TickTick: " + taskMessage);
        startService(intent);
        
        Toast.makeText(this, "Đang gửi SMS đến " + phoneNumber, Toast.LENGTH_SHORT).show();
        finish(); // Quay lại màn hình chính sau khi gửi
    }
}
