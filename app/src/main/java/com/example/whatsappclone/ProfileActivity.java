package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    String recieving_User_Id, SenderUserID, Current_State;
    private CircularImageView userProfileIMage;
    private TextView userProfileName, userProfileStatus;
    private Button sendReqBtn, declineReqBtn;
    private DatabaseReference UserRef, ChatRequestRef, ContactsRef, NotificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        NotificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");
        userProfileIMage = findViewById(R.id.profile_image);
        userProfileName = findViewById(R.id.User_name);
        userProfileStatus = findViewById(R.id.person_status);
        sendReqBtn = findViewById(R.id.send_request);
        declineReqBtn = findViewById(R.id.decline_request);
        recieving_User_Id = getIntent().getExtras().getString("visit_user_id").toString();
        SenderUserID = mAuth.getCurrentUser().getUid();
        Current_State = "new";

        UserRef.child(recieving_User_Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {

                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profilelogo).into(userProfileIMage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);
                    manageRequest();
                } else {

                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void manageRequest() {

        ChatRequestRef.child(SenderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(recieving_User_Id)){
                    String request_type = dataSnapshot.child(recieving_User_Id).child("request_type").getValue().toString();
                    if (request_type.equals("sent")){
                        Current_State = "request_sent";
                        sendReqBtn.setText("Cancel Request");
                    }else if (request_type.equals("received")){
                        Current_State = "request_received";
                        sendReqBtn.setText("Accept Request");
                        declineReqBtn.setVisibility(View.VISIBLE);
                        declineReqBtn.setEnabled(true);
                        declineReqBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                cancelChatRequest();
                            }
                        });
                    }
                }

                else {
                    ContactsRef.child(SenderUserID)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(recieving_User_Id)){
                                        Current_State = "friends";
                                        sendReqBtn.setText("Remove");
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (!SenderUserID.equals(recieving_User_Id)) {
            sendReqBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendReqBtn.setEnabled(false);
                    if (Current_State.equals("new")) {

                        ChatRequestRef.child(SenderUserID).child(recieving_User_Id)
                                .child("request_type").setValue("sent")
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            ChatRequestRef.child(recieving_User_Id).child(SenderUserID)
                                                    .child("request_type").setValue("received")
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {

                                                                HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                                chatNotificationMap.put("from", SenderUserID);
                                                                chatNotificationMap.put("type", "request");

                                                                NotificationRef.child(recieving_User_Id).push().setValue(chatNotificationMap)
                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()){

                                                                                    sendReqBtn.setEnabled(true);
                                                                                    Current_State = "request_sent";
                                                                                    sendReqBtn.setText("Cancel Request");

                                                                                }

                                                                            }
                                                                        });


                                                            }

                                                        }
                                                    });

                                        }

                                    }
                                });
                    }

                    if (Current_State.equals("request_sent")){
                        cancelChatRequest();
                    }
                    if (Current_State.equals("request_received")){
                        AcceptRequest();
                    }
                    if (Current_State.equals("friends")){
                        removeSpecificContact();
                    }

                }
            });

        } else {
            sendReqBtn.setVisibility(View.GONE);
        }
    }


    //RemoveContact

    private void removeSpecificContact() {

        ContactsRef.child(SenderUserID).child(recieving_User_Id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            ContactsRef.child(recieving_User_Id).child(SenderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendReqBtn.setEnabled(true);
                                                Current_State = "new";
                                                sendReqBtn.setText("Send Request");
                                                declineReqBtn.setVisibility(View.GONE);
                                                declineReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });


    }

    //Accept Request

    private void AcceptRequest() {

        ContactsRef.child(SenderUserID).child(recieving_User_Id).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            ContactsRef.child(recieving_User_Id).child(SenderUserID).child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){

                                                ChatRequestRef.child(SenderUserID).child(recieving_User_Id)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {

                                                                if (task.isSuccessful()){

                                                                    ChatRequestRef.child(recieving_User_Id).child(SenderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {

                                                                                    sendReqBtn.setEnabled(true);
                                                                                    Current_State = "friends";
                                                                                    sendReqBtn.setText("Remove");
                                                                                    declineReqBtn.setVisibility(View.GONE);
                                                                                    declineReqBtn.setEnabled(false);

                                                                                }
                                                                            });

                                                                }

                                                            }
                                                        });

                                            }

                                        }
                                    });

                        }

                    }
                });

    }



    //Cancel Request

    private void cancelChatRequest() {
        ChatRequestRef.child(SenderUserID).child(recieving_User_Id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()){

                            ChatRequestRef.child(recieving_User_Id).child(SenderUserID).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()){
                                                sendReqBtn.setEnabled(true);
                                                Current_State = "new";
                                                sendReqBtn.setText("Send Request");
                                                declineReqBtn.setVisibility(View.GONE);
                                                declineReqBtn.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
