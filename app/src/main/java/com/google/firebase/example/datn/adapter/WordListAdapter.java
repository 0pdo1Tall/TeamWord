package com.google.firebase.example.datn.adapter;

import android.content.res.Resources;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.example.datn.R;
import com.google.firebase.example.datn.model.WordList;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordListAdapter extends FirestoreAdapter<WordListAdapter.ViewHolder> {

    public interface OnWordListSelectedListener {

        void onWordListSelected(DocumentSnapshot wordList);
        void onWordListLongSelected(DocumentSnapshot wordList, String owner);
    }

    private OnWordListSelectedListener mListener;

    public WordListAdapter(Query query, OnWordListSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_word_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.word_list_item_image)
        ImageView imageView;

        @BindView(R.id.word_list_item_name)
        TextView nameView;

        @BindView(R.id.word_list_item_owner)
        TextView ownerView;

        @BindView(R.id.word_list_item_date)
        TextView dateView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnWordListSelectedListener listener) {

            WordList wordList = snapshot.toObject(WordList.class);
            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(wordList.getPhoto())
                    .into(imageView);

            nameView.setText(wordList.getName());
            ownerView.setText(wordList.getOwner());
            // set timestamp to dateView by format day/month/year

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onWordListSelected(snapshot);
                    }
                }
            });

            // Long click listener
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                   if(listener != null){
                       listener.onWordListLongSelected(snapshot,wordList.getOwner());
                   }
                   return true;
                }
            });
        }

    }
}
