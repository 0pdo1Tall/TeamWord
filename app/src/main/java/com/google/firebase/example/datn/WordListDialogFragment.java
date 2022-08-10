package com.google.firebase.example.datn;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.example.datn.model.WordList;
import com.google.firebase.example.datn.util.WordUtil;

import java.util.Calendar;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WordListDialogFragment extends DialogFragment {

    public static final String TAG = "WordListDialog";

    @BindView(R.id.word_list_name_text)
    EditText mWordListName;

    interface WordListAddedListener {

        void onWordListAdded(WordList word);

    }

    private WordListAddedListener mWordListAddedListener;

    @OnClick(R.id.word_list_add_button)
    public void onSubmitClicked(View view){
        Random random = new Random();
        WordList wordList = new WordList();
        wordList.setName(mWordListName.getText().toString());
        wordList.setPhoto(WordUtil.getRandomImageUrl(random));
        //
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            if(TextUtils.isEmpty(user.getDisplayName())) {
                wordList.setOwner(user.getEmail());
            }else{
                wordList.setOwner(user.getDisplayName());
            }
        }

        if(mWordListAddedListener != null){
            mWordListAddedListener.onWordListAdded(wordList);
        }

        dismiss();
    }

    @OnClick(R.id.word_list_add_cancel)
    public void onCancelClicked(View view){
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_word_list, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof WordListAddedListener) {
            mWordListAddedListener = (WordListAddedListener) context;
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
