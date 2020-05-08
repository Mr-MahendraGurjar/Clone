package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class  RegisterActivity extends AppCompatActivity {

    private EditText registerEmail, registerPassowrd;
    private TextView ALreadyAccound;
    private Button RegisterButton;
    private ProgressDialog loadingbar;
    private FirebaseAuth mAuth;
    private DatabaseReference Rootref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        Rootref = FirebaseDatabase.getInstance().getReference();
        registerEmail = findViewById(R.id.register_mail);
        registerPassowrd = findViewById(R.id.register_password);
        RegisterButton = findViewById(R.id.register_btn);
        ALreadyAccound = findViewById(R.id.already_have_account);
        loadingbar = new ProgressDialog(this);

        ALreadyAccound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK );
                startActivity(intent);
                finish();
            }
        });

        RegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccound();
            }
        });
    }

    private void CreateNewAccound() {
        String email = registerEmail.getText().toString();
        String password = registerPassowrd.getText().toString();

        if (TextUtils.isEmpty(email)){
            registerEmail.setError("insert");
        }
        if (TextUtils.isEmpty(password)){
            registerPassowrd.setError("insert");
        }else {
            loadingbar.setTitle("Creating New Account");
            loadingbar.setMessage("Please Wait....");
            loadingbar.show();

            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()){

                        String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        String currentUserId = mAuth.getCurrentUser().getUid();
                        Rootref.child("Users").child(currentUserId).setValue("");

                        Rootref.child("Users").child(currentUserId).child("device_token")
                                .setValue(deviceToken);


                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        Toast.makeText(RegisterActivity.this, "Account Create Successfully", Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error"+ message, Toast.LENGTH_SHORT).show();
                        loadingbar.dismiss();
                    }
                }
            });
        }
    }
}
