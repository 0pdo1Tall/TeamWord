package com.google.firebase.example.datn.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.example.datn.R;
import com.google.firebase.example.datn.model.Chat;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatAdapter extends FirestoreAdapter<ChatAdapter.ViewHolder>{

    public ChatAdapter(Query query) {
        super(query);
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ViewHolder(inflater.inflate(R.layout.item_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(getSnapshot(position));
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.chat_title)
        TextView titleView;

        @BindView(R.id.message_content)
        TextView messageView;

        public ViewHolder(View itemView){
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(DocumentSnapshot snapshot){
            Chat chat = snapshot.toObject(Chat.class);
            String pattern = "yyyy/MM/dd HH:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            String timestamp = simpleDateFormat.format(chat.getTimestamp());
            titleView.setText(timestamp + "\nBy: " + chat.getOwner());
            messageView.setText(chat.getMessage());
        }
    }
}
