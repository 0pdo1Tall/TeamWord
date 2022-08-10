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

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.example.datn.adapter.RatingAdapter;
import com.google.firebase.example.datn.model.Rating;
import com.google.firebase.example.datn.model.Word;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class WordDetailActivity extends AppCompatActivity
        implements EventListener<DocumentSnapshot>, RatingDialogFragment.RatingListener,
        WordUpdateDialogFragment.WordUpdateListener, WordDeleteDialogFragment.WordDeleteListener {

    private static final String TAG = "WordDetail";
    public static final String GROUP_ID = "GROUP_ID";

    public static final String KEY_WORD_ID = "key_word_id";

    @BindView(R.id.word_image)
    ImageView mImageView;

    @BindView(R.id.word_name)
    TextView mNameView;

    @BindView(R.id.word_rating)
    MaterialRatingBar mRatingIndicator;

    @BindView(R.id.word_num_ratings)
    TextView mNumRatingsView;

    @BindView(R.id.word_meaning)
    TextView mMeaningView;

    @BindView(R.id.word_category)
    TextView mCategoryView;

    @BindView(R.id.word_price)
    TextView mPriceView;

    @BindView(R.id.view_empty_ratings)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_ratings)
    RecyclerView mRatingsRecycler;

    //
    @BindView(R.id.word_owner)
    TextView mOwnerView;


    private RatingDialogFragment mRatingDialog;
    //
    private WordUpdateDialogFragment mWordUpdateDialog;
    private WordDeleteDialogFragment mWordDeleteDialog;

    private FirebaseFirestore mFirestore;
    private DocumentReference mWordRef;
    private ListenerRegistration mWordRegistration;

    String wordId;
    String groupId;

    private RatingAdapter mRatingAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_detail);
        ButterKnife.bind(this);

        // Get word ID from extras
        wordId = getIntent().getExtras().getString(KEY_WORD_ID);
        groupId = getIntent().getExtras().getString(GROUP_ID);
        if (wordId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_WORD_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the word
        mWordRef = mFirestore.collection("group/" + groupId + "/word").document(wordId);

        // Get ratings
        Query ratingsQuery = mWordRef
                .collection("ratings")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mRatingAdapter = new RatingAdapter(ratingsQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mRatingsRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mRatingsRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };

        mRatingsRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRatingsRecycler.setAdapter(mRatingAdapter);

        mRatingDialog = new RatingDialogFragment();
        mWordUpdateDialog = new WordUpdateDialogFragment();
        mWordDeleteDialog = new WordDeleteDialogFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        mRatingAdapter.startListening();
        mWordRegistration = mWordRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        mRatingAdapter.stopListening();

        if (mWordRegistration != null) {
            mWordRegistration.remove();
            mWordRegistration = null;
        }
    }

    private Task<Void> addRating(final DocumentReference wordRef,
                                 final Rating rating) {
        // Create reference for new rating, for use inside the transaction
        final DocumentReference ratingRef = wordRef.collection("ratings")
                .document();

        // In a transaction, add the new rating and update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {

                Word word = transaction.get(wordRef)
                        .toObject(Word.class);

                // Compute new number of ratings
                int newNumRatings = word.getNumRatings() + 1;

                // Compute new average rating
                double oldRatingTotal = word.getAvgRating() *
                        word.getNumRatings();
                double newAvgRating = (oldRatingTotal + rating.getRating()) /
                        newNumRatings;

                // Set new word info
                word.setNumRatings(newNumRatings);
                word.setAvgRating(newAvgRating);

                // Commit to Firestore
                transaction.set(wordRef, word);
                transaction.set(ratingRef, rating);

                return null;
            }
        });
    }

    private Task<Void> updateWord(final DocumentReference mWordRef, String name, String meaning, String category){

        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {
                transaction.update(mWordRef, "name", name, "meaning", meaning, "category", category);
                return null;
            }
        });
    }

    private Task<Void> deleteWord(final DocumentReference mWordRef){
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction)
                    throws FirebaseFirestoreException {
                transaction.delete(mWordRef);
                return null;
            }
        });
    }

    @Override
    public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
        if (e != null) {
            Log.w(TAG, "word:onEvent", e);
            return;
        }

        onWordLoaded(snapshot.toObject(Word.class));
    }

    private void onWordLoaded(Word word) {
        mNameView.setText(word.getName());
        mRatingIndicator.setRating((float) word.getAvgRating());
        mNumRatingsView.setText(getString(R.string.fmt_num_ratings, word.getNumRatings()));
        mMeaningView.setText(word.getMeaning());
        mCategoryView.setText(word.getCategory());

        //
        mOwnerView.setText(word.getOwner());

        // Background image
        Glide.with(mImageView.getContext())
                .load(word.getPhoto())
                .into(mImageView);

    }

    @OnClick(R.id.word_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_show_rating_dialog)
    public void onAddRatingClicked(View view) {
        mRatingDialog.show(getSupportFragmentManager(), RatingDialogFragment.TAG);
    }

    //

    @OnClick(R.id.word_button_edit)
    public void onEditClicked(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = user.getEmail();
        String owner = mOwnerView.getText().toString();
        if(currentUser.equals(owner)){
            Log.d(TAG, "onEditClicked: " + " user can edit this page");
            mWordUpdateDialog.show(getSupportFragmentManager(), WordUpdateDialogFragment.TAG);
        }else{
            Log.d(TAG, "onEditClicked: " + " user cannot edit this page");
        }
    }

    @OnClick(R.id.word_button_delete)
    public void onDeleteClicked(View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String currentUser = user.getEmail();
        String owner = mOwnerView.getText().toString();
        if(currentUser.equals(owner)){
            Log.d(TAG, "onDeleteClicked: " + " user can delete this page");
            mWordDeleteDialog.show(getSupportFragmentManager(), WordDeleteDialogFragment.TAG);
        }else{
            Log.d(TAG, "onDeleteClicked: " + " user cannot delete this page");
        }
    }

    @Override
    public void onRating(Rating rating) {
        // In a transaction, add the new rating and update the aggregate totals
        addRating(mWordRef, rating)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Rating added");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Add rating failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to add rating",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    //
    @Override
    public void onWordUpdate(String name,String meaning,String category) {
        Log.d(TAG, "onWordUpdate: " + " word updated");
        updateWord(mWordRef, name, meaning, category)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "word updated successfully");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Update word failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to update word",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onWordDelete() {
        Log.d(TAG, "onWordDelete: " + " word deleted");
        deleteWord(mWordRef)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Word deleted successfully");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mRatingsRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Delete word failed", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Failed to delete word",
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
