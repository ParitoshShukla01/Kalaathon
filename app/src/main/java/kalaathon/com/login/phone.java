package kalaathon.com.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.TimeUnit;

import kalaathon.com.R;
import kalaathon.com.home.homeactivity;

public class phone extends AppCompatActivity implements View.OnClickListener {

    private String mVerificationId="-1";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;

    private EditText mobilenum,otp;
    private Button sendotp,verify;
    private TextView resendotp;
    private Context mContext;
    public String phone_number;
    private TextView timer;
    public int counter=59;
    private FirebaseDatabase mFirebaseDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        mContext = phone.this;
        mobilenum=findViewById(R.id.mobilenum_otp);
        otp=findViewById(R.id.otp);
        sendotp=findViewById(R.id.sendotp);
        resendotp=findViewById(R.id.resendotp);
        verify=findViewById(R.id.verifyotp);
        timer=findViewById(R.id.counter_otp);

        sendotp.setOnClickListener(this);
        verify.setOnClickListener(this);
        resendotp.setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();

        // Initialize phone auth callbacks  (FOR VERIFICATION, NOT ENTERING CODE YET, TO GET TEXT SEND TO DEVICE)
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification
                signInWithPhoneAuthCredential(credential);
                otp.setText(credential.getSmsCode());
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    mobilenum.setError("Invalid Request.");
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    Toast.makeText(getApplicationContext(), "Quota exceeded", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
    }

    // GET TEXT CODE SENT SO YOU CAN USE IT TO SIGN IN
    private void startPhoneNumberVerification(String phoneNumber) {
        Toast.makeText(this, "Generating O.T.P ", Toast.LENGTH_LONG).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    // ENTERED CODE AND MANUALLY SIGNING IN WITH THAT CODE.
    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        if(!phone_number.equals("+91"+mobilenum.getText().toString())){
            Toast.makeText(mContext, " Re-enter contact number and generate otp.", Toast.LENGTH_SHORT).show();
            return;
        }
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    // USE TEXT TO SIGN IN
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Toast.makeText(mContext, "Verifying your number ", Toast.LENGTH_SHORT).show();
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            //look for existing phone number
                            Query phone_numberQuery=FirebaseDatabase.getInstance().getReference().child("users").orderByChild("phone_number").equalTo(phone_number);
                            phone_numberQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.getChildrenCount()>0) {
                                        FirebaseMessaging.getInstance().subscribeToTopic(mAuth.getCurrentUser().getUid());
                                        Intent i = new Intent(phone.this, homeactivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                    else
                                    {
                                        Intent intent=new Intent(phone.this,register.class);
                                        intent.putExtra("phone_number",phone_number);
                                        startActivity(intent);
                                        finish();
                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    Toast.makeText(mContext, "Invalid request!", Toast.LENGTH_SHORT).show();

                                }
                            });

                        } else {
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                otp.setError("Invalid code.");
                            }
                        }
                    }
                });
    }

    private boolean validatePhoneNumber() {
        if (TextUtils.isEmpty(phone_number)) {
            mobilenum.setError("Invalid phone number ");
            return false;
        }
        return true;
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        Toast.makeText(this, "Generating O.T.P ", Toast.LENGTH_LONG).show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    /*
    ------------------------------------database----------------------------------------------------
   */

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sendotp:
                phone_number="+91"+mobilenum.getText().toString();
                if (!validatePhoneNumber()) {
                    return;
                }
                if(phone_number.length()==13) {
                    sendotp.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);

                    counter = 59;
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            String txt = "00:" + counter;
                            timer.setText(txt);
                            counter--;
                        }

                        @Override
                        public void onFinish() {
                            timer.setVisibility(View.GONE);
                            resendotp.setVisibility(View.VISIBLE);
                        }
                    }.start();
                    startPhoneNumberVerification(phone_number);
                    break;
                }
            case R.id.verifyotp:
                String code = otp.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    otp.setError("Cannot be empty.");
                    return;
                }
                if(mVerificationId.equals("-1"))
                {
                    Toast.makeText(this, "Invalid Request!", Toast.LENGTH_SHORT).show();
                    return;
                }
                verifyPhoneNumberWithCode(mVerificationId, code);
                break;
            case R.id.resendotp:
                phone_number="+91"+mobilenum.getText().toString();
                if (!validatePhoneNumber()) {
                    return;
                }
                if(phone_number.length()==13) {
                    resendotp.setVisibility(View.GONE);
                    timer.setVisibility(View.VISIBLE);

                    counter = 59;
                    new CountDownTimer(60000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            String txt = "00:" + counter;
                            timer.setText(txt);
                            counter--;
                        }

                        @Override
                        public void onFinish() {
                            timer.setVisibility(View.GONE);
                            resendotp.setVisibility(View.VISIBLE);
                        }
                    }.start();
                    resendVerificationCode(phone_number, mResendToken);
                    break;
                }
        }
    }
    @Override
    protected void onPause() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onPause();
    }

    @Override
    protected void onResume() {
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        super.onResume();
    }
}


