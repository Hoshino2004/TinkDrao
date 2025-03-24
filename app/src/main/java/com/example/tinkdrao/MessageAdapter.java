package com.example.tinkdrao;

import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messageList;
    private final String hostId;
    private final String role;
    private final String selectedUserId;

    public MessageAdapter(List<Message> messageList, String hostId, String role, String selectedUserId) {
        this.messageList = messageList;
        this.hostId = hostId;
        this.role = role;
        this.selectedUserId = selectedUserId;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Message message = messageList.get(viewType);
        String messageType = determineMessageType(message);
        return MessageViewHolderFactory.create(parent, messageType);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        String senderId = message.getSenderId();
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams();

        boolean isRightMessage = (role.equals("Admin") && senderId.equals("Admin")) || senderId.equals(hostId);
        if (isRightMessage) {
            params.gravity = Gravity.END;
            holder.messageText.setBackgroundResource(R.drawable.bg_sender);
        } else {
            params.gravity = Gravity.START;
            holder.messageText.setBackgroundResource(R.drawable.bg_receiver);
        }
        holder.messageContainer.setLayoutParams(params);

        holder.bind(message, isRightMessage);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private String determineMessageType(Message message) {
        if (message.getVideoUrl() != null && !message.getVideoUrl().isEmpty()) return "video";
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) return "image";
        return "text";
    }

    // Factory class
    public static class MessageViewHolderFactory {
        public static MessageViewHolder create(ViewGroup parent, String messageType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.message_item, parent, false);
            switch (messageType) {
                case "text":
                    return new TextMessageViewHolder(view);
                case "image":
                    return new ImageMessageViewHolder(view);
                case "video":
                    return new VideoMessageViewHolder(view);
                default:
                    throw new IllegalArgumentException("Unknown message type: " + messageType);
            }
        }
    }

    // Base ViewHolder
    public static abstract class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        TextView messageText, timestampText, messageStatus;
        ImageView messageImage;
        VideoView messageVideo;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            messageStatus = itemView.findViewById(R.id.message_status);
            messageImage = itemView.findViewById(R.id.message_image);
            messageVideo = itemView.findViewById(R.id.message_video);
        }

        abstract void bind(Message message, boolean isRightMessage);
    }

    // Text Message ViewHolder
    public static class TextMessageViewHolder extends MessageViewHolder {
        TextMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(Message message, boolean isRightMessage) {
            messageText.setVisibility(View.VISIBLE);
            messageText.setText(message.getMessageText());
            timestampText.setText(formatTimestamp(message.getTimestamp()));
            messageStatus.setVisibility(isRightMessage ? View.VISIBLE : View.GONE);
            messageImage.setVisibility(View.GONE);
            messageVideo.setVisibility(View.GONE);
            if (isRightMessage) messageStatus.setText(message.getStatus().equals("seen") ? "Đã xem" : "Đã gửi");
        }
    }

    // Image Message ViewHolder
    public static class ImageMessageViewHolder extends MessageViewHolder {
        ImageMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(Message message, boolean isRightMessage) {
            messageText.setVisibility(View.GONE);
            messageImage.setVisibility(View.VISIBLE);
            Glide.with(itemView.getContext()).load(message.getImageUrl()).into(messageImage);
            timestampText.setText(formatTimestamp(message.getTimestamp()));
            messageStatus.setVisibility(isRightMessage ? View.VISIBLE : View.GONE);
            messageVideo.setVisibility(View.GONE);
            if (isRightMessage) messageStatus.setText(message.getStatus().equals("seen") ? "Đã xem" : "Đã gửi");

            messageImage.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra("imageUrl", message.getImageUrl());
                v.getContext().startActivity(intent);
            });
        }
    }

    // Video Message ViewHolder
    public static class VideoMessageViewHolder extends MessageViewHolder {
        VideoMessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        void bind(Message message, boolean isRightMessage) {
            messageText.setVisibility(View.GONE);
            messageVideo.setVisibility(View.VISIBLE);
            messageVideo.setVideoPath(message.getVideoUrl());
            messageVideo.seekTo(1);
            timestampText.setText(formatTimestamp(message.getTimestamp()));
            messageStatus.setVisibility(isRightMessage ? View.VISIBLE : View.GONE);
            messageImage.setVisibility(View.GONE);
            if (isRightMessage) messageStatus.setText(message.getStatus().equals("seen") ? "Đã xem" : "Đã gửi");

            messageVideo.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), FullScreenMediaActivity.class);
                intent.putExtra("videoUrl", message.getVideoUrl());
                v.getContext().startActivity(intent);
            });
        }
    }

    private static String formatTimestamp(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}