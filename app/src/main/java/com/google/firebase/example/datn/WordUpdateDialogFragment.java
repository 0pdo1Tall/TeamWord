package com.google.firebase.example.datn;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class WordUpdateDialogFragment extends DialogFragment {
    public static final String TAG = "WordUpdateDialog";

    @BindView(R.id.word_update_name_text)
    EditText mWordUpdateName;

    @BindView(R.id.word_update_meaning_text)
    EditText mWordUpdateMeaning;

    @BindView(R.id.word_update_category_text)
    EditText mWordUpdateCategory;

    interface WordUpdateListener {

        void onWordUpdate(String name, String meaning, String category);

    }

    private WordUpdateListener mWordUpdateListener;

    @OnClick(R.id.word_update_button)
    public void onSubmitClicked(View v){
        String name = mWordUpdateName.getText().toString();
        String meaning = mWordUpdateMeaning.getText().toString();
        String category = mWordUpdateCategory.getText().toString();

        if(mWordUpdateListener != null){
            mWordUpdateListener.onWordUpdate(name,meaning,category);
        }

        dismiss();
    }

    @OnClick(R.id.word_update_cancel)
    public void onCancelClicked(View v){
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_update_word, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        if(context instanceof WordUpdateListener){
            mWordUpdateListener = (WordUpdateListener) context;
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
