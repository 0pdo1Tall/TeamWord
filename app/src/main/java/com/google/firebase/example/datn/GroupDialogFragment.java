package com.google.firebase.example.datn;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.example.datn.model.Group;
import com.google.firebase.example.datn.util.WordUtil;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDialogFragment extends DialogFragment {

    public static final String TAG = "GroupDialog";
    @BindView(R.id.group_name_text)
    EditText mGroupName;

    interface GroupAddedListener {

        void onGroupAdded(Group group);

    }

    private GroupAddedListener mGroupAddedListener;

    @OnClick(R.id.group_add_button)
    public void onSubmitClicked(View view){
        Toast.makeText(getContext(), "Group Added", Toast.LENGTH_SHORT).show();
        String name = mGroupName.getText().toString();
        String photo = WordUtil.getRandomImageUrl(new Random());
        String admin = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        Group group = new Group(name, photo, admin, user);
        if(mGroupAddedListener != null)
        {
            mGroupAddedListener.onGroupAdded(group);
        }
        dismiss();
    }

    @OnClick(R.id.group_cancel_button)
    public void onCancelClicked(View view){
        Toast.makeText(getContext(), "Group Add Cancel", Toast.LENGTH_SHORT).show();
        dismiss();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_group, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof GroupDialogFragment.GroupAddedListener) {
            mGroupAddedListener = (GroupDialogFragment.GroupAddedListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }


}
