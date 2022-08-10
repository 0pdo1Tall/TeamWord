package com.google.firebase.example.datn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class GroupDeleteDialogFragment extends DialogFragment {
    public static final String TAG = "GroupDeleteDialog";
    private String mGroupId;

    interface GroupDeleteListener {

        void onGroupDelete(DocumentReference groupRef); // Pass to here
    }

    private GroupDeleteListener mGroupDeleteListener;

    @OnClick(R.id.group_delete_button)
    public void onSubmitClicked(View v){
        Toast.makeText(getContext(), "Group Deleted", Toast.LENGTH_SHORT).show();
        if(mGroupDeleteListener != null){
            mGroupDeleteListener.onGroupDelete(FirebaseFirestore.getInstance().collection("group").document(mGroupId)); // As well
        }
        dismiss();
    }

    @OnClick(R.id.group_cancel_button)
    public void onDeleteCancelClicked(View v){
        Toast.makeText(getContext(), "Group Delete Cancel", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_group, container, false);
        ButterKnife.bind(this, view);
        mGroupId = getArguments().getString(GroupDeleteDialogFragment.TAG);
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if(context instanceof GroupDeleteListener){
            mGroupDeleteListener = (GroupDeleteListener) context;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
