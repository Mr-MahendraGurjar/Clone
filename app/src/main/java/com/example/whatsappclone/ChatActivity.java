package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.app.usage.StorageStats;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsappclone.Adapter.MessageAdapter;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    String messageReceiverID, messageReceiverName, messagereceiverImage, messageSenderId;
    TextView custom_name, custom_status;
    CircularImageView custom_image;
    EditText messageInput;
    ImageView sendMessageButton, sendFiles;
    FirebaseAuth mAuth;
    DatabaseReference RootRef;
    Toolbar toolbar;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    RecyclerView recyclerView;
    String saveCurrentTime, saveCurrentDate, checker = "", myUrl = "";
    Uri fileuri;
    StorageTask uploadTask;
    ProgressDialog loadingbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messagereceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();

        toolbar = findViewById(R.id.chat_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view  = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(view);

        loadingbar = new ProgressDialog(this);

        messageAdapter = new MessageAdapter(messagesList);
        recyclerView = findViewById(R.id.recycler_chat_view);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0,0);
        custom_name = findViewById(R.id.custom_name);
        custom_status = findViewById(R.id.custom_status);
        custom_image = findViewById(R.id.custom_image);
        messageInput = findViewById(R.id.message_text);
        sendMessageButton = findViewById(R.id.message_send);
        sendFiles = findViewById(R.id.send_files);

        sendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                  "Image","PDF","Doc"
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){

                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                        }
                        if (i == 1){
                            checker = "pdf";

                        }

                        if (i == 2){
                            checker = "doc";

                        }
                    }
                });
                builder.show();
            }
        });

        custom_name.setText(messageReceiverName);
        Picasso.get().load(messagereceiverImage).placeholder(R.drawable.profilelogo).into(custom_image);
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        displayLastseen();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    @Override
    protected void onStart() {
        super.onStart();
        messagesList.clear();
        RootRef.child("Message").child(messageSenderId).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messageAdapter.notifyDataSetChanged();

                recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount());

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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null){

            loadingbar.setTitle("Sending...");
            loadingbar.setMessage("Please Wait...");
            loadingbar.show();



            fileuri = data.getData();

            if (!checker.equals("image")){

            }else if (checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Message/" + messageSenderId + "/" + messageReceiverID;
                final String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverID).push();
               final String messagePushID = userMessageKeyRef.getKey();

                final StorageReference filepath = storageReference.child(messagePushID + "." + "jpg");
                uploadTask = filepath.putFile(fileuri);
                uploadTask.continueWith(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()){
                            throw  task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri dowloadUrl = task.getResult();
                            myUrl = dowloadUrl.toString();

                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("name", fileuri.getLastPathSegment());
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderId);
                            messageImageBody.put("to", messageReceiverID);
                            messageImageBody.put("messageID", messagePushID);
                            messageImageBody.put("time", saveCurrentTime);
                            messageImageBody.put("date", saveCurrentDate);

                            Map messageBodyDetail = new HashMap();
                            messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageImageBody);
                            messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageImageBody);

                            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                                    }else {
                                        loadingbar.dismiss();

                                    }
                                    messageInput.setText("");

                                }
                            });


                        }
                    }
                });



            }else {
                loadingbar.dismiss();
                Toast.makeText(this, "Nothing Selected", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void displayLastseen(){
        RootRef.child("Users").child(messageSenderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        custom_status.setText("online");
                    }else if (state.equals("offline")){
                        custom_status.setText("Last Seen: "+ date + " "+ time);
                    }

                }else {
                    custom_status.setText("offline");
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void sendMessage() {
        String messageTXT = messageInput.getText().toString();
        if (TextUtils.isEmpty(messageTXT)){
            Toast.makeText(this, "Enter Some Message", Toast.LENGTH_SHORT).show();
        }else {
            String messageSenderRef = "Message/" + messageSenderId + "/" + messageReceiverID;
            String messageReceiverRef = "Message/" + messageReceiverID + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = RootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverID).push();
            String messagePushID = userMessageKeyRef.getKey();
            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageTXT);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);

            Map messageBodyDetail = new HashMap();
            messageBodyDetail.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetail.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            RootRef.updateChildren(messageBodyDetail).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                    }else {

                    }
                    messageInput.setText("");

                }
            });
        }
    }
}
