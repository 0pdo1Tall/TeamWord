package com.google.firebase.example.datn;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.example.datn.model.Word;
import com.google.firebase.example.datn.util.WordUtil;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WordDialogFragment extends DialogFragment {

    public static final String TAG = "WordDialog";

    @BindView(R.id.word_name_text)
    EditText mWordName;

    @BindView(R.id.word_meaning_text)
    EditText mWordMeaning;

    @BindView(R.id.word_category_text)
    EditText mWordCategory;

    interface WordAddedListener {

        void onWordAdded(Word word);

    }

    private WordAddedListener mWordAddedListener;

    @OnClick(R.id.word_add_button)
    public void onSubmitClicked(View view){
        Random random = new Random();
        Word word = new Word();
        word.setName(mWordName.getText().toString());
        word.setMeaning(mWordMeaning.getText().toString());
        word.setCategory(WordUtil.getCleanWord(mWordCategory.getText().toString()));
        word.setPhoto(WordUtil.getRandomImageUrl(random));
        word.setPrice(1);
        word.setAvgRating((double)0.0);
        word.setNumRatings(0);
        //
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        word.setOwner(user.getEmail());

        if(mWordAddedListener != null){
            mWordAddedListener.onWordAdded(word);
        }
        mWordName.setText("");
        mWordMeaning.setText("");
        mWordCategory.setText("");
        dismiss();
    }

    @OnClick(R.id.word_add_cancel)
    public void onCancelClicked(View view){
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_word, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

            if (context instanceof WordAddedListener) {
                mWordAddedListener = (WordAddedListener) context;
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
