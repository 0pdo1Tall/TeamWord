/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import com.google.firebase.example.datn.model.Word;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class WordAdapter extends FirestoreAdapter<WordAdapter.ViewHolder> {

    public interface OnWordSelectedListener {

        void onWordSelected(DocumentSnapshot word);

    }

    private OnWordSelectedListener mListener;

    public WordAdapter(Query query, OnWordSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_word, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.word_item_image)
        ImageView imageView;

        @BindView(R.id.word_item_name)
        TextView nameView;

        @BindView(R.id.word_item_rating)
        MaterialRatingBar ratingBar;

        @BindView(R.id.word_item_num_ratings)
        TextView numRatingsView;

//        @BindView(R.id.word_item_price)
//        TextView priceView;

        @BindView(R.id.word_item_category)
        TextView categoryView;

        @BindView(R.id.word_item_meaning)
        TextView meaningView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnWordSelectedListener listener) {

            Word word = snapshot.toObject(Word.class);

            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(word.getPhoto())
                    .into(imageView);

            nameView.setText(word.getName());
            ratingBar.setRating((float) word.getAvgRating());
            meaningView.setText("");
            categoryView.setText(word.getCategory());
            numRatingsView.setText(resources.getString(R.string.fmt_num_ratings,
                    word.getNumRatings()));

            // Click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onWordSelected(snapshot);
                    }
                }
            });
        }

    }
}
