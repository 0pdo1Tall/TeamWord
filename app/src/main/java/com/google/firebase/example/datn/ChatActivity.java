package com.google.firebase.example.datn;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.datn.adapter.ChatAdapter;
import com.google.firebase.example.datn.model.Chat;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChatActivity extends AppCompatActivity {

    public static final String TAG = "ChatActivity";
    public static final String ADMIN = "admin";
    String groupId, admin;

    private FirebaseFirestore mFirestore;
    DocumentReference groupRef;
    ChatAdapter mChatAdapter;
    private Query mQuery;
    private String mCurrentUser;

    @BindView(R.id.chat_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_chat)
    RecyclerView mRecyclerChat;

    @BindView(R.id.type_message_here)
    EditText mTypeMessageHere;

    @OnClick(R.id.send_message)
    public void sendMessage() {
        String message = mTypeMessageHere.getText().toString();
        String owner = mCurrentUser;
        Chat chat = new Chat(owner, message);
        chat.setTimestamp(Calendar.getInstance().getTime());
        mTypeMessageHere.setText("");
        hideKeyboard();
        mRecyclerChat.smoothScrollToPosition(0);
        groupRef.collection("chat").add(chat);
        mRecyclerChat.smoothScrollToPosition(0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        admin = getIntent().getStringExtra(ChatActivity.ADMIN);
        groupId = getIntent().getStringExtra(ChatActivity.TAG);
        mFirestore = FirebaseFirestore.getInstance();
        groupRef = mFirestore.document("group/" + groupId);
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        //
        initFirestore();
        initRecyclerView();
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.TAG, groupId);
            intent.putExtra(MainActivity.ADMIN, admin);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initFirestore(){
        // Get all the message
        mQuery = groupRef.collection("chat").orderBy("timestamp", Query.Direction.DESCENDING);
    }

    private void initRecyclerView(){
        if(mQuery == null){
            Log.w(TAG, "No query, not initializing RecyclerView");
        }

        mChatAdapter = new ChatAdapter(mQuery);
        //
        mRecyclerChat.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerChat.setAdapter(mChatAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if(mChatAdapter != null){
            mChatAdapter.startListening();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(mChatAdapter != null){
            mChatAdapter.stopListening();
        }
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}