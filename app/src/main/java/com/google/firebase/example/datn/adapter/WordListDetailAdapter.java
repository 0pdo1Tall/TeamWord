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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.example.datn.R;
import com.google.firebase.example.datn.model.WordListDetail;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WordListDetailAdapter extends FirestoreAdapter<WordListDetailAdapter.ViewHolder>{

    public interface OnWordListDetailLongSelectedListener {

        void onWordListDetailLongSelected(DocumentSnapshot word);
    }

    private OnWordListDetailLongSelectedListener mListener;

    public WordListDetailAdapter(Query query, OnWordListDetailLongSelectedListener listener) {
        super(query);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_word_list_detail, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position), mListener);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.word_list_detail_item_image)
        ImageView imageView;

        @BindView(R.id.word_list_detail_item_name)
        TextView nameView;

        @BindView(R.id.word_list_detail_item_category)
        TextView categoryView;

        @BindView(R.id.word_list_detail_item_meaning)
        TextView meaningView;

        @BindView(R.id.word_list_detail_button_back)
        ImageView deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(final DocumentSnapshot snapshot,
                         final OnWordListDetailLongSelectedListener listener) {

            WordListDetail wordListDetail = snapshot.toObject(WordListDetail.class);

            Resources resources = itemView.getResources();

            // Load image
            Glide.with(imageView.getContext())
                    .load(wordListDetail.getPhoto())
                    .into(imageView);

            nameView.setText(wordListDetail.getName());
            meaningView.setText(wordListDetail.getMeaning());
            categoryView.setText(wordListDetail.getCategory());

            deleteButton.setImageResource(R.drawable.ic_close_white_24px);
            // Click listener
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        listener.onWordListDetailLongSelected(snapshot);
                    }
                }
            });
        }
    }
}