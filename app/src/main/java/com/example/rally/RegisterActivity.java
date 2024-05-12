package com.example.rally;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private static final String LOG_TAG = RegisterActivity.class.getName();

    private EditText usernameET;
    private EditText emailET;
    private EditText phoneET;
    private EditText passwordET;
    private EditText passwordConfirmET;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private CollectionReference mUserCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser != null) {
            Log.i(LOG_TAG, "User already signed in");
            goHome();
        }

        mFireStore = FirebaseFirestore.getInstance();
        mUserCollection = mFireStore.collection("Users");

        usernameET = findViewById(R.id.editTextDisplayName);
        emailET = findViewById(R.id.editTextRegisterEmail);
        phoneET = findViewById(R.id.editTextPhone);
        passwordET = findViewById(R.id.editTextRegisterPassword);
        passwordConfirmET = findViewById(R.id.editTextRegisterPasswordConfirm);
    }

    public void register(View view) {
        String username = usernameET.getText().toString();
        String email = emailET.getText().toString();
        String phone = phoneET.getText().toString();
        String password = passwordET.getText().toString();
        String passwordConfirm = passwordConfirmET.getText().toString();

        Log.i(LOG_TAG,
                "Username: " + username +
                " Email: " + email +
                " Phone: " + phone +
                " Password: " + password +
                " Password confirm: " + passwordConfirm);

        if (username.length() < 4) {
            Toast.makeText(this, "Username is too short!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Password is too short!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passwordConfirm)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "User registered");
                mUser = mAuth.getCurrentUser();
                if (mUser != null) {
                    String userId = mUser.getUid();
                    mUserCollection.document(userId).set(new AppUser(username, email, phone));
                }
                goHome();
            } else {
                Toast.makeText(this, "Registration unsuccessful: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void goHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
    }
}