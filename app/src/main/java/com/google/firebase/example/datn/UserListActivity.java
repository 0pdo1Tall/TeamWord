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

import com.google.firebase.auth.FirebaseAuth;
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
        EventListener<DocumentSnapshot>, UserDialogFragment.UserAddedListener,
        UserDeleteDialogFragment.UserDeleteListener {

    public static final String TAG = "UserListActivity";
    public static final String ADMIN = "Admin";
    // List View object
    ListView listView;

    // Define array adapter for ListView
    ArrayAdapter<String> adapter;

    // Define array List for List View data
    ArrayList<String> mylist;
    //
    String admin,user;

    @BindView(R.id.user_toolbar)
    Toolbar mToolbar;

    //
    String groupId;
    private DocumentReference mGroupRef;
    private ListenerRegistration mGroupRegistration;
    private UserDialogFragment mUserDialogFragment;
    private UserDeleteDialogFragment mUserDeleteDialogFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        groupId = getIntent().getStringExtra(TAG);
        mGroupRef = FirebaseFirestore.getInstance().document("group/" + groupId);
        mUserDialogFragment = new UserDialogFragment();
        mUserDeleteDialogFragment = new UserDeleteDialogFragment();

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

        user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        admin = getIntent().getStringExtra(UserListActivity.ADMIN);

        if(user.equals(admin)){
            listView.setOnItemClickListener((parent, view, position, id) -> {
                String user = (String) parent.getItemAtPosition(position);
                if(!user.equals(admin)){
                    // delete user
                    Bundle bundle = new Bundle();
                    bundle.putString(UserDeleteDialogFragment.USER, user);
                    mUserDeleteDialogFragment.setArguments(bundle);
                    mUserDeleteDialogFragment.show(getSupportFragmentManager(), UserDeleteDialogFragment.TAG);
                }else{
                    Toast.makeText(this, "Admin account cannot be deleted", Toast.LENGTH_SHORT).show();
                }
            });
        }
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
                if(user.equals(admin)) {
                    mUserDialogFragment.show(getSupportFragmentManager(), UserDialogFragment.TAG);
                }
                else{
                    Toast.makeText(this,"You are not authorized to add user", Toast.LENGTH_SHORT).show();
                }
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
        Toast.makeText(this, "User added", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onUserDelete(String user){
        deleteUser(user);
    }

    private void addUser(String user) {
        mGroupRef.update("user", FieldValue.arrayUnion(user));
    }

    private void deleteUser(String user){
        mGroupRef.update("user", FieldValue.arrayRemove(user));
        Toast.makeText(this, "User removed", Toast.LENGTH_SHORT).show();
    }

}
