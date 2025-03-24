package com.example.tinkdrao;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ChatRepository {
    private final DatabaseReference messagesRef;
    private final StorageReference storageRef;

    public ChatRepository() {
        messagesRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Messages");

        storageRef = FirebaseStorage.getInstance().getReference();
    }

    public void sendMessage(String chatKey, Message message) {
        messagesRef.child(chatKey).push().setValue(message);
    }

    public void uploadMedia(Uri fileUri, String folder, OnUploadListener listener) {
        StorageReference fileRef = storageRef.child(folder + "/" + System.currentTimeMillis() + (folder.equals("Images") ? ".jpg" : ".mp4"));
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> listener.onSuccess(uri.toString())))
                .addOnFailureListener(listener::onFailure);
    }

    public void loadMessages(String chatKey, ValueEventListener listener) {
        messagesRef.child(chatKey).addValueEventListener(listener);
    }

    public void updateMessagesToSeen(String chatKey, String currentUserId) {
        messagesRef.child(chatKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null && message.getStatus().equals("sent") && !message.getSenderId().equals(currentUserId)) {
                        // Tin nhắn chưa xem và không phải do người dùng hiện tại gửi
                        messageSnapshot.getRef().child("status").setValue("seen");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu cần
            }
        });
    }

    public interface OnUploadListener {
        void onSuccess(String url);
        void onFailure(Exception e);
    }
}