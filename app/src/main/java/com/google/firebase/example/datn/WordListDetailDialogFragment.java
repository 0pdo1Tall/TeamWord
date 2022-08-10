package com.google.firebase.example.datn;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.example.datn.model.WordListDetail;
import com.google.firebase.example.datn.util.WordUtil;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WordListDetailDialogFragment extends DialogFragment {

    public static final String TAG = "WordListDetailDialog";

    @BindView(R.id.word_list_detail_name_text)
    EditText mWordListDetailName;

    @BindView(R.id.word_list_detail_meaning_text)
    EditText mWordListDetailMeaning;

    @BindView(R.id.word_list_detail_category_text)
    EditText mWordListDetailCategory;

    interface WordListDetailAddedListener{
        void onWordListDetailAdded(WordListDetail wordListDetail);
    }

    private WordListDetailAddedListener mWordListDetailAddedListener;

    @OnClick(R.id.word_list_detail_add_button)
    public void onSubmitClicked(View view){
        Random random = new Random();
        WordListDetail wordListDetail = new WordListDetail();
        wordListDetail.setName(mWordListDetailName.getText().toString());
        wordListDetail.setCategory(WordUtil.getCleanWord(mWordListDetailCategory.getText().toString()));
        wordListDetail.setMeaning(mWordListDetailMeaning.getText().toString());
        wordListDetail.setPhoto(WordUtil.getRandomImageUrl(random));

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        wordListDetail.setOwner(user.getEmail());

        if(mWordListDetailAddedListener != null){
            mWordListDetailAddedListener.onWordListDetailAdded(wordListDetail);
        }

        dismiss();
    }

    @OnClick(R.id.word_list_detail_add_cancel)
    public void onCancelClicked(View view){
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_add_word_list_detail, container, false);
        ButterKnife.bind(this, v);

        return v;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if (context instanceof WordListDetailAddedListener) {
            mWordListDetailAddedListener = (WordListDetailAddedListener) context;
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
