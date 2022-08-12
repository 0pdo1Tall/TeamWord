/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.example.datn;

import androidx.annotation.NonNull;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.datn.adapter.GroupAdapter;
import com.google.firebase.example.datn.model.Group;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GroupActivity extends AppCompatActivity implements
        GroupDialogFragment.GroupAddedListener,
            GroupAdapter.OnGroupSelectedListener,
        GroupDeleteDialogFragment.GroupDeleteListener {

    private static final String TAG = "GroupActivity";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 100;

    @BindView(R.id.group_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_group)
    RecyclerView mGroupRecycler;

    @BindView(R.id.group_view_empty)
    ViewGroup mGroupEmptyView;;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    //
    private GroupDialogFragment mGroupDialogFragment;
    //
    private GroupDeleteDialogFragment mGroupDeleteDialogFragment;
    private GroupAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerListView();

        // Group Dialog
        mGroupDialogFragment = new GroupDialogFragment();
        mGroupDeleteDialogFragment = new GroupDeleteDialogFragment();
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        String currentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        // Get the 50 highest rated groups
        mQuery = mFirestore.collection("group") .whereArrayContains("user", currentUser).limit(LIMIT);

    }

    private void initRecyclerListView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerListView");
        }

        mAdapter = new GroupAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mGroupRecycler.setVisibility(View.GONE);
                    mGroupEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mGroupRecycler.setVisibility(View.VISIBLE);
                    mGroupEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mGroupRecycler.setLayoutManager(new LinearLayoutManager(this));
        mGroupRecycler.setAdapter(mAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_group:
                addNewGroup();
                break;

            case R.id.menu_notification:
                startActivity(new Intent(this, DailyGoalActivity.class));
                break;

            case R.id.menu_sign_out:
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out
                                startActivity(new Intent(GroupActivity.this, LoginActivity.class));
                                finish();
                            }
                        });
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    private void addNewGroup() {
        mGroupDialogFragment.show(getSupportFragmentManager(), GroupDialogFragment.TAG);
    }

    private void showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show();
    }

    private Task<Void> deleteGroup(final DocumentReference groupRef) {
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException{
                transaction.delete(groupRef);
                return null;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onGroupDelete(DocumentReference groupRef) {
        deleteGroup(groupRef).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                hideKeyboard();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupActivity.this, "Delete Group Failed", Toast.LENGTH_SHORT).show();
                hideKeyboard();
                Snackbar.make(findViewById(android.R.id.content), "Failed to delete word list",
                        Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onGroupAdded(Group group) {
        mFirestore.collection("group").add(group);
    }

    @Override
    public void onGroupSelected(DocumentSnapshot group) {
        Intent intent = new Intent(GroupActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.TAG, group.getId());
        intent.putExtra(MainActivity.ADMIN, group.getString("admin"));
        startActivity(intent);
    }

    @Override
    public void onGroupLongSelected(DocumentSnapshot group) {
        //
        String groupId = group.getId();
        Bundle bundle = new Bundle();
        bundle.putString(GroupDeleteDialogFragment.TAG, groupId);
        mGroupDeleteDialogFragment.setArguments(bundle);
        mGroupDeleteDialogFragment.show(getSupportFragmentManager(), GroupDeleteDialogFragment.TAG);
    }
}
