package com.google.firebase.example.datn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserDeleteDialogFragment extends DialogFragment {

    public static final String TAG = "UserDeleteDialog";
    public static final String USER = "User";
    private String user;

    interface UserDeleteListener {
        void onUserDelete(String user);
    }

    private UserDeleteListener mUserDeleteListener;

    @OnClick(R.id.user_delete_button)
    public void onSubmitClicked(View view){

        if(mUserDeleteListener != null)
        {
            mUserDeleteListener.onUserDelete(user);
        }
        dismiss();
    }

    @OnClick(R.id.user_delete_cancel_button)
    public void onCancelClicked(View view){
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_delete_user, container, false);
        user = getArguments().getString(UserDeleteDialogFragment.USER);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof UserDeleteDialogFragment.UserDeleteListener) {
            mUserDeleteListener = (UserDeleteDialogFragment.UserDeleteListener) context;
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
