package com.google.firebase.example.datn;

import androidx.annotation.Nullable;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.example.datn.model.Group;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends AppCompatActivity implements
        EventListener<DocumentSnapshot>, UserDialogFragment.UserAddedListener {

    public static final String TAG = "UserListActivity";

    // List View object
    ListView listView;

    // Define array adapter for ListView
    ArrayAdapter<String> adapter;

    // Define array List for List View data
    ArrayList<String> mylist;


    @BindView(R.id.user_toolbar)
    Toolbar mToolbar;

    //
    String groupId;
    private DocumentReference mGroupRef;
    private ListenerRegistration mGroupRegistration;
    private UserDialogFragment mUserDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        groupId = getIntent().getStringExtra(TAG);
        mGroupRef = FirebaseFirestore.getInstance().document("group/" + groupId);
        mUserDialogFragment = new UserDialogFragment();

        // initialise ListView with id
        listView = findViewById(R.id.userList);
        mylist = new ArrayList<>();
        mylist.add("test");

        // Set adapter to ListView
        adapter
                = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                mylist);
        listView.setAdapter(adapter);
    }


    @Override
    public void onStart() {
        super.onStart();
        mGroupRegistration = mGroupRef.addSnapshotListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mGroupRegistration != null) {
            mGroupRegistration.remove();
            mGroupRegistration = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_user:
                mUserDialogFragment.show(getSupportFragmentManager(), UserDialogFragment.TAG);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
        Group group = documentSnapshot.toObject(Group.class);
        adapter.clear();
        for (String userId : group.getUser()) {
            adapter.add(userId);
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onUserAdded(String user) {
        Toast.makeText(this, "Return OK ", Toast.LENGTH_SHORT).show();
        // get list string from adapter
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            list.add(adapter.getItem(i));
        }

        if(!list.contains(user)){
            addUser(user);
        }else{
            Toast.makeText(this, "User already exist", Toast.LENGTH_SHORT).show();
        }
    }

    private void addUser(String user) {
        mGroupRef.update("user", FieldValue.arrayUnion(user));
    }
}
