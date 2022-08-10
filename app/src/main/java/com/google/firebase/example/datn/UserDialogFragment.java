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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserDialogFragment extends DialogFragment {

    public static final String TAG = "UserDialog";
    @BindView(R.id.user_name_text)
    EditText mUserName;

    interface UserAddedListener {

        void onUserAdded(String user);

    }

    private UserAddedListener mUserAddedListener;

    @OnClick(R.id.user_add_button)
    public void onSubmitClicked(View view){
        Toast.makeText(getContext(), "User Added", Toast.LENGTH_SHORT).show();
        String name = mUserName.getText().toString();
        if(mUserAddedListener != null)
        {
            mUserAddedListener.onUserAdded(name);
        }
        dismiss();
    }

    @OnClick(R.id.user_cancel_button)
    public void onCancelClicked(View view){
        Toast.makeText(getContext(), "User Add Cancel", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_user, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof UserDialogFragment.UserAddedListener) {
            mUserAddedListener = (UserDialogFragment.UserAddedListener) context;
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
