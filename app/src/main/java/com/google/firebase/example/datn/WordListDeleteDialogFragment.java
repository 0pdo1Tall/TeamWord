package com.google.firebase.example.datn;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class WordListDeleteDialogFragment extends DialogFragment {
    public static final String TAG = "WordListDeleteDialog";
    private String mWordListId;
    private String mGroupId;

    interface WordListDeleteListener {

        void onWordListDelete(DocumentReference wordListRef); // Pass to here
    }

    private WordListDeleteListener mWordListDeleteListener;

    @SuppressLint("LongLogTag")
    @OnClick(R.id.word_list_delete_button)
    public void onSubmitClicked(View v){
        Log.d(TAG, "onDeleteClicked");
        DocumentReference wordListRef = FirebaseFirestore.getInstance().document("group/" + mGroupId + "/wordList/" + mWordListId);
        if(mWordListDeleteListener != null){
            mWordListDeleteListener.onWordListDelete(wordListRef); // As well
        }
        dismiss();
    }

    @SuppressLint("LongLogTag")
    @OnClick(R.id.word_list_delete_cancel)
    public void onDeleteCancelClicked(View v){
        Log.d(TAG, "onDeleteCancelClicked");
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_delete_word_list, container, false);
        ButterKnife.bind(this, view);
        mWordListId = getArguments().getString("wordListId");
        mGroupId = getArguments().getString("groupId");
//        Toast.makeText(getContext(), "wordListID: " + mWordListId, Toast.LENGTH_SHORT).show();
        // Get Word List ID Here
        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if(context instanceof WordListDeleteListener){
            mWordListDeleteListener = (WordListDeleteListener) context;
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
