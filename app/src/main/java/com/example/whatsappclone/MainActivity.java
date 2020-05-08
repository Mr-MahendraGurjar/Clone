package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.whatsappclone.Adapter.TabLayoutAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mtoolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabLayoutAdapter tabLayoutAdapter;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mtoolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle("WhatsApp");

        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();

        tabLayout = findViewById(R.id.main_tabs);
        viewPager = findViewById(R.id.main_viewpager);
        tabLayoutAdapter = new TabLayoutAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabLayoutAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onStart() {
        super.onStart();

       FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null){
            SendUserToLoginActivity();
        }else {

            updateOnlineStatus("online");

            varifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            updateOnlineStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null){
            updateOnlineStatus("offline");
        }

    }

    private void varifyUserExistance() {
        String currentUserId = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.child("name").exists())){
                   // Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |  Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void SendUserToLoginActivity() {
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.option_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.main_find_freinds){
            startActivity(new Intent(MainActivity.this,FindFriendActivity.class));
        }
        if (item.getItemId() == R.id.main_setting){
            startActivity(new Intent(MainActivity.this,SettingActivity.class));

        }

        if (item.getItemId() == R.id.main_create_group){
            RequestNewGroup();
        }

        if (item.getItemId() == R.id.main_logout){

            updateOnlineStatus("offline");

            mAuth.signOut();
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }
        return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name:");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("e.g. College Friend Masti");
        builder.setView(groupNameField);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupname = groupNameField.getText().toString();
                if (TextUtils.isEmpty(groupname)){
                    Toast.makeText(MainActivity.this, "write your group name", Toast.LENGTH_SHORT).show();
                }else {
                    CreateNewGroup(groupname);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            dialogInterface.cancel();
            }
        });
            builder.show();
    }

    private void CreateNewGroup(final String groupname) {
        RootRef.child("Groups").child(groupname).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(MainActivity.this, groupname+" group is created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateOnlineStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserId = mAuth.getCurrentUser().getUid();

        RootRef.child("Users").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);



    }
}
