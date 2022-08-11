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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.example.datn.adapter.WordListAdapter;
import com.google.firebase.example.datn.model.WordList;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordListActivity extends AppCompatActivity implements
        WordListDialogFragment.WordListAddedListener,
        WordListAdapter.OnWordListSelectedListener,
        WordListDeleteDialogFragment.WordListDeleteListener {

    public static final String TAG = "WordListActivity";
    public static final String ADMIN = "admin";

    private static final int LIMIT = 100;

    @BindView(R.id.word_list_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_word_list)
    RecyclerView mWordListRecycler;

    @BindView(R.id.word_list_view_empty)
    ViewGroup mWordListEmptyView;;

    @BindView(R.id.word_list_bottom_navigation)
    BottomNavigationView bottomNavigationView;

    String groupId,admin;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    //
    private WordListDialogFragment mWordListDialogFragment;
    //
    private WordListDeleteDialogFragment mWordListDeleteDialogFragment;
    private WordListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        //
        groupId = getIntent().getStringExtra(WordListActivity.TAG);
        admin = getIntent().getStringExtra(WordListActivity.ADMIN);

        bottomNavigationView.setSelectedItemId(R.id.menu_word_list);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.menu_word:
                        Intent word_intent = new Intent(getApplicationContext(), MainActivity.class);
                        word_intent.putExtra(MainActivity.TAG, groupId);
                        word_intent.putExtra(MainActivity.ADMIN, admin);
                        startActivity(word_intent);
                        return true;

                    case R.id.menu_word_list:
                        return true;

                    case R.id.menu_chat:
                        Intent chat_intent = new Intent(getApplicationContext(), ChatActivity.class);
                        chat_intent.putExtra(ChatActivity.TAG, groupId);
                        chat_intent.putExtra(ChatActivity.ADMIN, admin);
                        startActivity(chat_intent);
                        return true;
                }
                return false;
            }
        });


        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        initFirestore();
        initRecyclerListView();

        // Word Dialog
        mWordListDialogFragment = new WordListDialogFragment();
        mWordListDeleteDialogFragment = new WordListDeleteDialogFragment();
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra(WordListActivity.TAG);
        // Get the 50 highest rated words
        mQuery = mFirestore.collection("group/" + groupId + "/wordList")
                .limit(LIMIT);
    }

    private void initRecyclerListView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerListView");
        }

        mAdapter = new WordListAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mWordListRecycler.setVisibility(View.GONE);
                    mWordListEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mWordListRecycler.setVisibility(View.VISIBLE);
                    mWordListEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mWordListRecycler.setLayoutManager(new LinearLayoutManager(this));
        //
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.wordlist_divider));
        mWordListRecycler.addItemDecoration(itemDecorator);
        mWordListRecycler.setAdapter(mAdapter);

        // Hide Bottom navigation when scrolling
        mWordListRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy > 0 && bottomNavigationView.isShown()) {
                bottomNavigationView.setVisibility(View.GONE);
            } else if (dy < 0 ) {
                bottomNavigationView.setVisibility(View.VISIBLE);

            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }});
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
        getMenuInflater().inflate(R.menu.menu_word_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_word_list:
                onAddItemsOneByOne();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    //
    private void onAddItemsOneByOne() {
        mWordListDialogFragment.show(getSupportFragmentManager(), WordListDialogFragment.TAG);
    }

    @Override
    public void onWordListSelected(DocumentSnapshot wordList) {
        // Go to the details page for the selected word
        Intent intent = new Intent(this, WordListDetailActivity.class);
        String owner = wordList.getString("owner");
        intent.putExtra(WordListDetailActivity.OWNER, owner);
        intent.putExtra(WordListDetailActivity.TAG, "group/" + groupId + "/wordList/" + wordList.getId());
        startActivity(intent);
    }

    @Override
    public void onWordListLongSelected(DocumentSnapshot wordList,String owner) {
        //
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = user.getEmail();
        if(name.equals(owner)) {
            Bundle bundle = new Bundle();
            bundle.putString("wordListId", wordList.getId());
            bundle.putString("groupId", groupId);
            mWordListDeleteDialogFragment.setArguments(bundle);
            mWordListDeleteDialogFragment.show(getSupportFragmentManager(), WordListDeleteDialogFragment.TAG);
        }
    }

    private void showTodoToast() {
    }

    //
    @Override
    public void onWordListAdded(WordList wordL) {
        // Get a reference to the words collection
        CollectionReference wordList = mFirestore.collection("group/" + groupId + "/wordList");

        // Add a new document to the words collection
        wordList.add(wordL);
    }

    private Task<Void> deleteWordList(final DocumentReference wordListRef) {
        Toast.makeText(this, "Deleting", Toast.LENGTH_SHORT).show();
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException{
                transaction.delete(wordListRef);
                return null;
            }
        });
    }

    @Override
    public void onWordListDelete(DocumentReference wordListRef) {
        Toast.makeText(this, "Deletion", Toast.LENGTH_SHORT).show();
        deleteWordList(wordListRef).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(WordListActivity.this, "Delete Successfully", Toast.LENGTH_SHORT).show();
                hideKeyboard();
            }
        }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WordListActivity.this, "Delete Failed", Toast.LENGTH_SHORT).show();
                hideKeyboard();
                Snackbar.make(findViewById(android.R.id.content), "Failed to delete word list",
                        Snackbar.LENGTH_SHORT).show();
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
}
