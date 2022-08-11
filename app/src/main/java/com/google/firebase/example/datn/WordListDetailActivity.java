package com.google.firebase.example.datn;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.datn.adapter.WordListDetailAdapter;
import com.google.firebase.example.datn.model.WordListDetail;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WordListDetailActivity extends AppCompatActivity implements
        WordListDetailAdapter.OnWordListDetailLongSelectedListener,
        WordListDetailDialogFragment.WordListDetailAddedListener
{

    public static final String TAG = "WordListDetailActivity";
    public static final String OWNER = "Owner";

    @BindView(R.id.word_list_detail_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.word_list_detail_recycler_words)
    RecyclerView mWordListDetailRecycler;

    @BindView(R.id.word_list_detail_view_empty)
    ViewGroup mEmptyView;

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    String user;
    //
    String wordListId;
    private DocumentReference mWordListReference;
    private WordListDetailDialogFragment mWordListDetailDialogFragment;
    private WordListDetailAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list_detail);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Enable Firestore logging
        FirebaseFirestore.setLoggingEnabled(true);

        // Initialize Firestore and the main RecyclerView
        mFirestore = FirebaseFirestore.getInstance();
        wordListId = getIntent().getStringExtra(WordListDetailActivity.TAG);
        mWordListReference = mFirestore.document(wordListId);
        initFirestore();
        initRecyclerView();

        mWordListDetailDialogFragment = new WordListDetailDialogFragment();
        //
        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
    }

    private void initFirestore() {
        mFirestore = FirebaseFirestore.getInstance();
        mQuery = mWordListReference.collection("wordListDetail").limit(100);
    }

    private void initRecyclerView() {
        if (mQuery == null) {
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mAdapter = new WordListDetailAdapter(mQuery,this) {

            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mWordListDetailRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mWordListDetailRecycler.setVisibility(View.VISIBLE);
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

        mWordListDetailRecycler.setLayoutManager(new LinearLayoutManager(this));
        //
        DividerItemDecoration itemDecorator = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.divider));
        mWordListDetailRecycler.addItemDecoration(itemDecorator);
        mWordListDetailRecycler.setAdapter(mAdapter);
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
        getMenuInflater().inflate(R.menu.menu_word_list_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_add_word_list_detail:
                onAddItemsOneByOne();
                break;

            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //
    private void onAddItemsOneByOne() {
        mWordListDetailDialogFragment.show(getSupportFragmentManager(), WordListDetailDialogFragment.TAG);
    }

    @Override
    public void onWordListDetailAdded(WordListDetail wordListDetail){
        CollectionReference wordListDetailRef = mWordListReference.collection("wordListDetail");
        wordListDetailRef.add(wordListDetail);
    }

    @Override
    public void onWordListDetailLongSelected(DocumentSnapshot word) {
        String owner = word.getString("owner");
        if(user.equals(owner)){
            // delete this wordlist detail
            word.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "WordListDetail successfully deleted!");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error deleting WordListDetail", e);
                }
            });
        }else{
            Toast.makeText(this, "You can't delete this wordlist detail", Toast.LENGTH_SHORT).show();
        }
    }
}
