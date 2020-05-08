package com.example.whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerification, Verifybtn;
    private EditText InputphoneNumber, InputVerifyCode;
    private TextView txt,txt2;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingbar;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();
        sendVerification = findViewById(R.id.send_Verification_code);
        Verifybtn = findViewById(R.id.btn_verify);
        InputphoneNumber = findViewById(R.id.phonenumber);
        InputVerifyCode = findViewById(R.id.verify);
        txt = findViewById(R.id.txt);
        txt2 = findViewById(R.id.txt2);
        loadingbar = new ProgressDialog(this);


        sendVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String phoneNumber = "+91"+InputphoneNumber.getText().toString();
                if (TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please Enter number...", Toast.LENGTH_SHORT).show();
                }else if (phoneNumber.length() != 13){
                    Toast.makeText(PhoneLoginActivity.this, "Please enter 10 digit number...", Toast.LENGTH_SHORT).show();
                }


                else {
                    loadingbar.setTitle("Phone Verification");
                    loadingbar.setMessage("Please Wait.....");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });


        Verifybtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendVerification.setVisibility(View.GONE);
                InputphoneNumber.setVisibility(View.GONE);
                txt.setVisibility(View.GONE);
                txt2.setVisibility(View.VISIBLE);

                String verificationcode = InputVerifyCode.getText().toString();
                if (TextUtils.isEmpty(verificationcode)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter Code First", Toast.LENGTH_SHORT).show();
                }else {

                    loadingbar.setTitle("Verifing Your Code");
                    loadingbar.setMessage("Please Wait.....");
                    loadingbar.setCanceledOnTouchOutside(false);
                    loadingbar.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationcode);
                    signInWithPhoneAuthCredential(credential);
                }

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingbar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number", Toast.LENGTH_SHORT).show();

            }

            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;
                loadingbar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code has been send, Please check,,", Toast.LENGTH_SHORT).show();

                sendVerification.setVisibility(View.VISIBLE);
                InputphoneNumber.setVisibility(View.VISIBLE);
                txt.setVisibility(View.VISIBLE);
                Verifybtn.setVisibility(View.GONE);
                InputVerifyCode.setVisibility(View.GONE);
                txt2.setVisibility(View.GONE);

            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingbar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(PhoneLoginActivity.this, MainActivity.class));

                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
