package com.example.rally;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class ProfileActivity extends AppCompatActivity {
    private static final String LOG_TAG = ProfileActivity.class.getName();

    private TextView usernameText;
    private TextView emailText;
    private TextView phoneText;
    private EditText editTextNewDisplayName;
    private EditText editTextOldPassword;
    private EditText editTextNewPassword;
    private EditText editTextNewPasswordConfirm;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private CollectionReference mUserCollection;

    private AppUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Log.i(LOG_TAG, "Unauthenticated user");
            finish();
        }

        mFireStore = FirebaseFirestore.getInstance();
        mUserCollection = mFireStore.collection("Users");

        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
        phoneText = findViewById(R.id.phoneText);
        editTextNewDisplayName = findViewById(R.id.editTextNewDisplayName);
        editTextOldPassword = findViewById(R.id.editTextOldPassword);
        editTextNewPassword = findViewById(R.id.editTextNewPassword);
        editTextNewPasswordConfirm = findViewById(R.id.editTextNewPasswordConfirm);

        loadUserData();
    }

    private void loadUserData() {
        String userId = mUser.getUid();
        mUserCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot record = task.getResult();
                if (record.exists()) {
                    currentUser = record.toObject(AppUser.class);
                    usernameText.setText(currentUser.getDisplayName());
                    emailText.setText(currentUser.getEmail());
                    phoneText.setText(currentUser.getPhone());
                }
            }
        });
    }

    public void changeDisplayName(View view) {
        String username = editTextNewDisplayName.getText().toString();
        Log.i(LOG_TAG, "New display name: " + username);
        if (username.length() < 4) {
            Toast.makeText(this, "Username is too short!", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUser.setDisplayName(username);
        String userId = mUser.getUid();
        mUserCollection.document(userId).set(currentUser);
        loadUserData();
    }

    public void changePassword(View view) {
        String oldPassword = editTextOldPassword.getText().toString();
        String newPassword = editTextNewPassword.getText().toString();
        String newPasswordConfirm = editTextNewPasswordConfirm.getText().toString();
        Log.i(LOG_TAG, "Old password: " + oldPassword + " New password: " + newPassword + " - " + newPasswordConfirm);
        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password is too short!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPassword.equals(newPasswordConfirm)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
            return;
        }
        AuthCredential credentials = EmailAuthProvider.getCredential(currentUser.getEmail(), oldPassword);
        mUser.reauthenticate(credentials)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mUser.updatePassword(newPassword).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Password changed", Toast.LENGTH_SHORT).show();
                                loadUserData();
                            } else {
                                Toast.makeText(ProfileActivity.this, "Password couldn't be changed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
    }

    public void deleteAccount(View view) {
        Log.i(LOG_TAG, "Deleting account");
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Are you sure you want to delete your account?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        mUser.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                                startActivity(intent);
                            }
                        });
                    }})
                .setNegativeButton(android.R.string.no, null).show();
    }
}