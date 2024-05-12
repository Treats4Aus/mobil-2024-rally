package com.example.rally;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    private static final String LOG_TAG = HomeActivity.class.getName();

    private LinearLayout groupLayout;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseFirestore mFireStore;
    private CollectionReference mUserCollection;
    private CollectionReference mGroupCollection;
    private CollectionReference mMembershipCollection;

    private AppUser currentUser;
    private Map<String, AppGroup> myGroups;
    private AppGroup[] filteredGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

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

        groupLayout = findViewById(R.id.groupLayout);

        String userId = mUser.getUid();
        mUserCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot record = task.getResult();
                if (record.exists()) {
                    currentUser = record.toObject(AppUser.class);
                    loadMyGroups();
                }
            }
        });
    }

    private void loadMyGroups() {
        myGroups = new HashMap<>();
        mMembershipCollection.whereEqualTo("userId", currentUser.getEmail()).
                get().addOnSuccessListener(queryDocumentSnapshots -> {
                    GroupMembership membership;
                    for (QueryDocumentSnapshot item : queryDocumentSnapshots) {
                        membership = item.toObject(GroupMembership.class);
                        GroupMembership finalMembership = membership;
                        mGroupCollection.document(membership.getGroupId()).get().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                DocumentSnapshot record = task.getResult();
                                if (record.exists()) {
                                    myGroups.put(finalMembership.getGroupId(), record.toObject(AppGroup.class));
                                    listGroups();
                                }
                            }
                        });
                    }
                });
    }

    private void listGroups() {
        groupLayout.removeAllViews();

        RelativeLayout groupCard;
        TextView groupNameText;
        TextView groupMembersText;
        TextView eventTimeText;
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        long counter = 0;

        for (Map.Entry<String, AppGroup> group : myGroups.entrySet()) {
            groupCard = (RelativeLayout) getLayoutInflater().inflate(R.layout.group_item, groupLayout, false);
            groupNameText = groupCard.findViewById(R.id.groupName);
            groupMembersText = groupCard.findViewById(R.id.groupMembers);
            eventTimeText = groupCard.findViewById(R.id.eventTime);
            groupNameText.setText(group.getValue().getName());
            eventTimeText.setText(group.getValue().getEventDate());
            TextView finalGroupMembersText = groupMembersText;
            mMembershipCollection.whereEqualTo("groupId", group.getKey()).count().get(AggregateSource.SERVER)
                    .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    AggregateQuerySnapshot result = task.getResult();
                    finalGroupMembersText.setText(String.valueOf(result.getCount()) + " members");
                }
            });
            groupLayout.addView(groupCard);
            fadeIn.setStartOffset(100 * counter);
            counter++;
            groupCard.startAnimation(fadeIn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.home_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.searchBtn);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.i(LOG_TAG, newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.newGroupBtn) {
            Log.i(LOG_TAG, "New group button pressed");
            Intent intent = new Intent(this, GroupCreatorActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.profileBtn) {
            Log.i(LOG_TAG, "Profile button pressed");
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.logoutBtn) {
            Log.i(LOG_TAG, "Logout button pressed");
            logoutUser();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    private void logoutUser() {
        if (mUser != null) {
            mAuth.signOut();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}