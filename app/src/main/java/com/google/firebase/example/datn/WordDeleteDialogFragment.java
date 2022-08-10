package com.google.firebase.example.datn;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class WordDeleteDialogFragment extends DialogFragment {
    public static final String TAG = "WordUpdateDialog";

    interface WordDeleteListener {

        void onWordDelete();
    }

    private WordDeleteListener mWordDeleteListener;

    @OnClick(R.id.word_delete_button)
    public void onSubmitClicked(View v){
        Log.d(TAG, "onDeleteClicked");
        if(mWordDeleteListener != null){
            mWordDeleteListener.onWordDelete(); //
        }
        dismiss();
    }

    @OnClick(R.id.word_delete_cancel)
    public void onDeleteCancelClicked(View v){
        Log.d(TAG, "onDeleteCancelClicked");
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_word, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if(context instanceof WordDeleteListener){
            mWordDeleteListener = (WordDeleteListener) context;
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
