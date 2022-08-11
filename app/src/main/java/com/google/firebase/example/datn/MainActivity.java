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
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.datn.adapter.WordAdapter;
import com.google.firebase.example.datn.model.Word;
import com.google.firebase.example.datn.viewmodel.MainActivityViewModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        FilterDialogFragment.FilterListener, WordDialogFragment.WordAddedListener,
        WordAdapter.OnWordSelectedListener {

    public static final String TAG = "MainActivity";
    public static final String ADMIN = "Admin";

    private static final int RC_SIGN_IN = 9001;

    private static final int LIMIT = 100;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.text_current_search)
    TextView mCurrentSearchView;

    @BindView(R.id.text_current_sort_by)
    TextView mCurrentSortByView;

    @BindView(R.id.recycler_words)
    RecyclerView mWordRecycler;

    @BindView(R.id.view_empty)
    ViewGroup mEmptyView;;

    @BindView(R.id.word_bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    //
    String groupId;
    String admin;

    private FilterDialogFragment mFilterDialog;
    //
    private WordDialogFragment mWordDialogFragment;
    private WordAdapter mAdapter;
    private DocumentReference mGroupReference;

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        //

        bottomNavigationView.setSelectedItemId(R.id.menu_word);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.menu_word:
                        Toast.makeText(getApplicationContext(),"Word Menu", Toast.LENGTH_SHORT).show();
                        return true;

                    case R.id.menu_word_list:
                        //
                        Intent intent = new Intent(getApplicationContext(), WordListActivity.class);
                        intent.putExtra(WordListActivity.TAG, groupId);
                        startActivity(intent);
                        return true;

                    case R.id.menu_chat:
                        Intent chat_intent = new Intent(getApplicationContext(), ChatActivity.class);
                        chat_intent.putExtra(ChatActivity.TAG, groupId);
                        startActivity(chat_intent);
                        return true;
                }
                return false;
            }
        });

        // View model
        mViewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra(MainActivity.TAG);
        admin = getIntent().getStringExtra(MainActivity.ADMIN);
        mGroupReference = mFirestore.collection("group").document(groupId);
        initFirestore();
        initRecyclerView();

        // Filter Dialog
        mFilterDialog = new FilterDialogFragment();
        // Word Dialog
        mWordDialogFragment = new WordDialogFragment();
    }

    private void initFirestore() {
//         Get the 50 highest rated word
        mQuery = mFirestore.collection("group/" + mGroupReference.getId() + "/word").limit(LIMIT);
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new WordAdapter(mQuery, this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mWordRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mWordRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mWordRecycler.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mWordRecycler.getContext(),
                DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.word_divider));
        mWordRecycler.addItemDecoration(dividerItemDecoration);
        mWordRecycler.setAdapter(mAdapter);

        // Hide Bottom navigation when scrolling
        mWordRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();

//         Start sign in if necessary
        if (shouldStartSignIn()) {
            startSignIn();
            return;
        }

        // Apply filters
        onFilter(mViewModel.getFilters());

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
    public void onFilter(Filters filters) {
        // Construct query basic query
        Query query = mFirestore.collection("group/" + mGroupReference.getId() + "/word");

        //
        if(filters.hasOwner()){
            if(filters.getOwner().equals("Me")) query = query.whereEqualTo("owner", FirebaseAuth.getInstance().getCurrentUser().getEmail());
        }

        // Category (equality filter)
        if (filters.hasCategory()) {
            query = query.whereEqualTo("category", filters.getCategory());
        }

//        // City (equality filter)
//        if (filters.hasCity()) {
//            query = query.whereEqualTo("city", filters.getCity());
//        }

//        // Price (equality filter)
//        if (filters.hasPrice()) {
//            query = query.whereEqualTo("price", filters.getPrice());
//        }

        // Sort by (orderBy with direction)
        if (filters.hasSortBy()) {
            query = query.orderBy(filters.getSortBy(), filters.getSortDirection());
        }

        // Limit items
        query = query.limit(Integer.valueOf(filters.getLimit()));

        // Update the query
        mQuery = query;
        mAdapter.setQuery(query);

        // Set header
        mCurrentSearchView.setText(Html.fromHtml(filters.getSearchDescription(this)));
        mCurrentSortByView.setText(filters.getOrderDescription(this));

        // Save filters
        mViewModel.setFilters(filters);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_items_one_by_one:
                onAddItemsOneByOne();
                break;

            case R.id.word_list:
                onWordList();
                break;

            case R.id.menu_user_management:
                onUserManagement();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onUserManagement() {
        Intent intent = new Intent(this, UserListActivity.class);
        intent.putExtra(UserListActivity.TAG, groupId);
        intent.putExtra(UserListActivity.ADMIN, admin);
        startActivity(intent);
    }

    private void onWordList() {
        Intent intent = new Intent(this, WordListActivity.class);
        intent.putExtra(WordListActivity.TAG, mGroupReference.getId());
        startActivity(intent);
    }

    //
    private void onAddItemsOneByOne() {
        mWordDialogFragment.show(getSupportFragmentManager(), WordDialogFragment.TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            mViewModel.setIsSigningIn(false);
            if (resultCode != RESULT_OK && shouldStartSignIn()) {
                startSignIn();
            }
        }
    }

    @OnClick(R.id.filter_bar)
    public void onFilterClicked() {
        // Show the dialog containing filter options
        mFilterDialog.show(getSupportFragmentManager(), FilterDialogFragment.TAG);
    }

    @OnClick(R.id.button_clear_filter)
    public void onClearFilterClicked() {
        mFilterDialog.resetFilters();

        onFilter(Filters.getDefault());
    }

    @Override
    public void onWordSelected(DocumentSnapshot word) {
        // Go to the details page for the selected word
        Intent intent = new Intent(this, WordDetailActivity.class);
        intent.putExtra(WordDetailActivity.GROUP_ID, mGroupReference.getId());
        intent.putExtra(WordDetailActivity.KEY_WORD_ID, word.getId());
        startActivity(intent);
    }

    private boolean shouldStartSignIn() {
        return (!mViewModel.getIsSigningIn() && FirebaseAuth.getInstance().getCurrentUser() == null);
    }

    private void startSignIn() {
        // Sign in with FirebaseUI
//        Intent intent = AuthUI.getInstance().createSignInIntentBuilder()
//                .setAvailableProviders(Collections.singletonList(
//                        new AuthUI.IdpConfig.EmailBuilder().build()))
//                .setIsSmartLockEnabled(false)
//                .build();
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivityForResult(intent, RC_SIGN_IN);
        mViewModel.setIsSigningIn(true);
    }

    private void showTodoToast() {
        Toast.makeText(this, "TODO: Implement", Toast.LENGTH_SHORT).show();
    }

    //
    @Override
    public void onWordAdded(Word word) {
        // Get a reference to the words collection
        CollectionReference words = mGroupReference.collection("word");

        // Add a new document to the words collection
        words.add(word);
    }
}
