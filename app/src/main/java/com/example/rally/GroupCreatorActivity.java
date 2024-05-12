package com.example.rally;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class GroupCreatorActivity extends AppCompatActivity {
    private static final String LOG_TAG = GroupCreatorActivity.class.getName();

    private EditText editTextGroupName;
    private EditText editTextGroupDescription;
    private EditText editTextGroupMembers;
    private EditText editTextEventDate;
    private EditText editTextEventTime;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private CollectionReference mUserCollection;
    private CollectionReference mGroupCollection;
    private CollectionReference mMembershipCollection;

    private AppUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_creator);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        if (mUser == null) {
            Log.i(LOG_TAG, "Unauthenticated user");
            finish();
        }

        mFireStore = FirebaseFirestore.getInstance();
        mUserCollection = mFireStore.collection("Users");
        mGroupCollection = mFireStore.collection("Groups");
        mMembershipCollection = mFireStore.collection("Memberships");

        editTextGroupName = findViewById(R.id.editTextGroupName);
        editTextGroupDescription = findViewById(R.id.editTextGroupDescription);
        editTextGroupMembers = findViewById(R.id.editTextGroupMembers);
        editTextEventDate = findViewById(R.id.editTextEventDate);
        editTextEventTime = findViewById(R.id.editTextEventTime);
    }

    public void createGroup(View view) {
        String groupName = editTextGroupName.getText().toString();
        String groupDescription = editTextGroupDescription.getText().toString();
        String[] groupMembers = editTextGroupMembers.getText().toString().split("\n");
        String eventDate = editTextEventDate.getText().toString();
        String eventTime = editTextEventTime.getText().toString();

        Log.i(LOG_TAG, "Group created with members: " + String.join(", ", groupMembers));

        if (groupName.length() < 4) {
            Toast.makeText(this, "Group name is too short!", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = String.valueOf((groupName + eventDate + eventTime).hashCode());
        String userId = mUser.getUid();
        mUserCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot record = task.getResult();
                if (record.exists()) {
                    currentUser = record.toObject(AppUser.class);
                }
            }
        });

        mGroupCollection.document(groupId).set(
                new AppGroup(groupName, groupDescription, eventDate + " " + eventTime)
        ).addOnCompleteListener(task -> {
            Log.d(LOG_TAG, "Group created");
            mMembershipCollection.add(new GroupMembership(currentUser.getEmail(), groupId)).addOnCompleteListener(task1 -> {
                for (String email : groupMembers) {
                    mMembershipCollection.add(new GroupMembership(email, groupId));
                }
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            });
        });
    }
}