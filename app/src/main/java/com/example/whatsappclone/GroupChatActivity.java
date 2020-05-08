package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView sendbtn;
    private EditText editmsg;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName, currentUserId, currentUserName, currentDate, currentTime;

    FirebaseAuth mAuth;
    DatabaseReference userref, groupRef, groupMessageKeyRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userref = FirebaseDatabase.getInstance().getReference().child("Users");
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        toolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendbtn = findViewById(R.id.send_btn);
        editmsg = findViewById(R.id.text_message);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        mScrollView = findViewById(R.id.my_mascroll_layout);


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = editmsg.getText().toString();
                String messageKey = groupRef.push().getKey();


                if (TextUtils.isEmpty(msg)) {
                    Toast.makeText(GroupChatActivity.this, "please enter some message", Toast.LENGTH_SHORT).show();
                } else {
                    Calendar calfordate = Calendar.getInstance();
                    SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM,yy");
                    currentDate = currentDateFormat.format(calfordate.getTime());


                    Calendar calfortime = Calendar.getInstance();
                    SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
                    currentTime = currentTimeFormat.format(calfortime.getTime());

                    HashMap<String, Object> groupMessageKey = new HashMap<>();
                    groupRef.updateChildren(groupMessageKey);

                    groupMessageKeyRef = groupRef.child(messageKey);

                    HashMap<String, Object> messageInfoMap = new HashMap<>();
                    messageInfoMap.put("name", currentUserName);
                    messageInfoMap.put("message", msg);
                    messageInfoMap.put("date", currentDate);
                    messageInfoMap.put("time", currentTime);

                    groupMessageKeyRef.updateChildren(messageInfoMap);

                    editmsg.setText("");

                    mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

                }
            }
        });


        userref.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        groupRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                if (dataSnapshot.exists()){
                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()){
                        String chatDate = (String)((DataSnapshot)iterator.next()).getValue();
                        String chatMessage = (String)((DataSnapshot)iterator.next()).getValue();
                        String chatName = (String)((DataSnapshot)iterator.next()).getValue();
                        String chatTime = (String)((DataSnapshot)iterator.next()).getValue();

                        displayTextMessages.append(chatName + ": " + chatMessage + "\n" + chatTime + "\n\n");

                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
