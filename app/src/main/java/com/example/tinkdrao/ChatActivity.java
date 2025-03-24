package com.example.tinkdrao;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private TextView selectedUserTextView;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton, selectImageButton, cancelImageButton, selectVideoButton, backButton;
    private RecyclerView previewRecyclerView;
    private VideoView videoPreview;
    private MessageAdapter messageAdapter;
    private PreviewImageAdapter previewImageAdapter;
    private List<Message> messageList;
    private ChatRepository chatRepository;
    FirebaseUser mUser = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference usersRef;
    private String hostId = mUser.getUid();
    private String selectedUser;
    private String role;
    private List<Uri> imageUris = new ArrayList<>();
    private Uri videoUri;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        selectedUserTextView = findViewById(R.id.selectedUserTextView);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        previewRecyclerView = findViewById(R.id.previewRecyclerView);
        videoPreview = findViewById(R.id.videoPreview);
        selectImageButton = findViewById(R.id.selectImageButton);
        selectVideoButton = findViewById(R.id.selectVideoButton);
        cancelImageButton = findViewById(R.id.cancelImageButton);

        // Hiển thị nút Back trên ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        chatRepository = new ChatRepository();
        usersRef = FirebaseDatabase.getInstance().getReference("TinkDrao/Users");

        selectedUser = getIntent().getStringExtra("selectedUser");
        if (selectedUser == null || selectedUser.isEmpty()) {
            selectedUser = "Phòng tư vấn";
        }
        Log.d("DEBUG_SELECTED_USER", "selectedUser nhận được từ Intent: " + selectedUser);

        fetchUserName(selectedUser);
        fetchUserRole();

        previewImageAdapter = new PreviewImageAdapter(imageUris);
        previewRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        previewRecyclerView.setAdapter(previewImageAdapter);

        sendButton.setOnClickListener(v -> sendMessage());
        selectImageButton.setOnClickListener(v -> openFileChooser(PICK_IMAGE_REQUEST));
        selectVideoButton.setOnClickListener(v -> openFileChooser(PICK_VIDEO_REQUEST));
        cancelImageButton.setOnClickListener(v -> clearMediaPreview());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUris.clear();
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageUris.add(imageUri);
                }
                if (!imageUris.isEmpty()) {
                    previewRecyclerView.setVisibility(View.VISIBLE);
                    previewImageAdapter.notifyDataSetChanged();
                    videoPreview.setVisibility(View.GONE);
                    cancelImageButton.setVisibility(View.VISIBLE);
                }
            } else if (requestCode == PICK_VIDEO_REQUEST && data.getData() != null) {
                videoUri = data.getData();
                videoPreview.setVisibility(View.VISIBLE);
                videoPreview.setVideoURI(videoUri);
                videoPreview.start();
                previewRecyclerView.setVisibility(View.GONE);
                cancelImageButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchUserName(String userId) {
        if ("Phòng tư vấn".equals(userId)) {
            selectedUserTextView.setText("Phòng tư vấn");
            return;
        }
        usersRef.child(userId).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.getValue(String.class);
                selectedUserTextView.setText(userName != null ? userName : "Người dùng không tồn tại");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                selectedUserTextView.setText("Lỗi tải tên người dùng");
            }
        });
    }

    private void fetchUserRole() {
        usersRef.child(hostId).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                role = snapshot.getValue(String.class);
                if (role == null) role = "user";

                Log.d("DEBUG_ROLE", "Role của hostId (" + hostId + "): " + role);

                if ("user".equals(role)) selectedUser = "Phòng tư vấn";

                fetchUserName(selectedUser);
                setupAdapter();
                loadMessages();
                updateMessagesToSeen(); // Cập nhật trạng thái khi mở chat
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi lấy role từ Users!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapter() {
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, hostId, role, selectedUser);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);
    }

    private void loadMessages() {
        String chatKey = "Admin".equals(role) ? selectedUser : hostId;
        chatRepository.loadMessages(chatKey, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                    Message message = messageSnapshot.getValue(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Lỗi tải tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateMessagesToSeen() {
        String chatKey = "Admin".equals(role) ? selectedUser : hostId;
        chatRepository.updateMessagesToSeen(chatKey, hostId);
    }

    private void openFileChooser(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(requestCode == PICK_IMAGE_REQUEST ? "image/*" : "video/*");
        if (requestCode == PICK_IMAGE_REQUEST) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Chọn tệp"), requestCode);
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty() || !imageUris.isEmpty() || videoUri != null) {
            if (!imageUris.isEmpty()) {
                for (Uri imageUri : imageUris) {
                    chatRepository.uploadMedia(imageUri, "Images", new ChatRepository.OnUploadListener() {
                        @Override
                        public void onSuccess(String url) {
                            sendMessageToDatabase(null, url, null);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(ChatActivity.this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else if (videoUri != null) {
                chatRepository.uploadMedia(videoUri, "Videos", new ChatRepository.OnUploadListener() {
                    @Override
                    public void onSuccess(String url) {
                        sendMessageToDatabase(null, null, url);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(ChatActivity.this, "Tải lên thất bại!", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                sendMessageToDatabase(messageText, null, null);
            }
            messageInput.setText("");
            clearMediaPreview();
        }
    }

    private void sendMessageToDatabase(String text, String imageUrl, String videoUrl) {
        String senderId, receiverId, chatKey;
        if ("Admin".equals(role)) {
            senderId = "Admin";
            receiverId = selectedUser;
            chatKey = selectedUser;
        } else {
            senderId = hostId;
            receiverId = "Admin";
            chatKey = hostId;
        }

        Message message = new Message.Builder(senderId, receiverId)
                .messageText(text)
                .imageUrl(imageUrl)
                .videoUrl(videoUrl)
                .timestamp(System.currentTimeMillis())
                .status("sent")
                .build();
        chatRepository.sendMessage(chatKey, message);
    }

    private void clearMediaPreview() {
        imageUris.clear();
        videoUri = null;
        previewRecyclerView.setVisibility(View.GONE);
        videoPreview.setVisibility(View.GONE);
        cancelImageButton.setVisibility(View.GONE);
        previewImageAdapter.notifyDataSetChanged();
    }

    private class PreviewImageAdapter extends RecyclerView.Adapter<PreviewImageAdapter.ImageViewHolder> {
        private List<Uri> uris;

        PreviewImageAdapter(List<Uri> uris) {
            this.uris = uris;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(200, 200));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            Glide.with(ChatActivity.this).load(uris.get(position)).into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return uris.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(@NonNull ImageView itemView) {
                super(itemView);
                this.imageView = itemView;
            }
        }

    }
    @Override
    public boolean onOptionsItemSelected(@androidx.annotation.NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Khi bấm nút Back, quay về Activity trước đó
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}