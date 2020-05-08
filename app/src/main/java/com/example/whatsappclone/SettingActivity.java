package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Picasso;
import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    private Button btnupdateProfile;
    private EditText username, status;
    private CircularImageView profileimage;
    private String currentuserId;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private static final int Gallerypic = 1;
    ProgressDialog loadingbar;
    Uri imageUri;
    String downloadUrl;
    private StorageReference UserProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        mAuth = FirebaseAuth.getInstance();
        currentuserId = mAuth.getCurrentUser().getUid();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        btnupdateProfile = findViewById(R.id.update_setting_button);
        username = findViewById(R.id.set_user_name);
        status = findViewById(R.id.set_profile_status);
        profileimage = findViewById(R.id.profile_picture);
        loadingbar = new ProgressDialog(this);


        profileimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setAction(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");
                startActivityForResult(i, Gallerypic);
            }
        });

        retriveUserInfo();

        btnupdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });
    }

    private void saveData() {
        final String getUserName = username.getText().toString();
        final String getUserStatus = status.getText().toString();

        if (imageUri == null){
            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).hasChild("image")){
                        saveInfo();

                    }else {

                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }else if (getUserName.equals("")){
            Toast.makeText(this, "User Name Is Need", Toast.LENGTH_SHORT).show();
        }else if (getUserStatus.equals("")){
            Toast.makeText(this, "Status is Requiered", Toast.LENGTH_SHORT).show();
        }else {

            loadingbar.setTitle("Uploading");
            loadingbar.setMessage("Please Wait...");
            loadingbar.show();


            final StorageReference filepath = UserProfileImagesRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            UploadTask uploadTask = filepath.putFile(imageUri);
            uploadTask .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();

                    }
                    downloadUrl = filepath.getDownloadUrl().toString();

                    return filepath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        downloadUrl = task.getResult().toString();

                        HashMap<String, Object> profileMap = new HashMap<>();
                        profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        profileMap.put("name", getUserName);
                        profileMap.put("status", getUserStatus);
                        profileMap.put("image", downloadUrl);

                        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if (task.isSuccessful()){
                                    startActivity(new Intent(SettingActivity.this,MainActivity.class));
                                    finish();
                                    loadingbar.dismiss();
                                    Toast.makeText(SettingActivity.this, "Profile Updateed Successfully", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
                    }
                }
            });
        }
    }

    private void saveInfo() {

        final String getUserName = username.getText().toString();
        final String getUserStatus = status.getText().toString();


        if (getUserName.equals("")){
            Toast.makeText(this, "User Name Is Need", Toast.LENGTH_SHORT).show();
        }else if (getUserStatus.equals("")){
            Toast.makeText(this, "Status is Requiered", Toast.LENGTH_SHORT).show();
        }else {

            loadingbar.setTitle("Uploading");
            loadingbar.setMessage("Please Wait...");
            loadingbar.show();

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());
            profileMap.put("name", getUserName);
            profileMap.put("status", getUserStatus);

            userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    if (task.isSuccessful()) {
                        startActivity(new Intent(SettingActivity.this, MainActivity.class));
                        finish();
                        Toast.makeText(SettingActivity.this, "Profile Updateed Successfully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }

                }
            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallerypic && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            profileimage.setImageURI(imageUri);

        }

    }

    private void retriveUserInfo(){

        userRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image"))) {
                    String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                    String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                    String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();

                    username.setText(retrieveUserName);
                    status.setText(retrieveStatus);
                    Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profilelogo).into(profileimage);

                }




                else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                   // String imageDb = dataSnapshot.child("image").getValue().toString();
                    String nameDb = dataSnapshot.child("name").getValue().toString();
                    String statusDb = dataSnapshot.child("status").getValue().toString();

                    username.setText(nameDb);
                    status.setText(statusDb);
                 //   Picasso.get().load(imageDb).placeholder(R.drawable.profilelogo).into(profileimage);

                }

                else {
                    Toast.makeText(SettingActivity.this, "Please set & Update Profile info....", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
