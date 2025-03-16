package com.example.tinkdrao.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Comment;

import java.util.ArrayList;

public class CommentAdapter extends ArrayAdapter<Comment> {

    public CommentAdapter(Context context, ArrayList<Comment> comments) {
        super(context, 0, comments);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_comment, parent, false);
        }

        Comment comment = getItem(position);

        TextView tvUsername = convertView.findViewById(R.id.tvUsername);
        TextView tvContent = convertView.findViewById(R.id.tvContent);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingBar);

        tvUsername.setText(comment.getUsername());
        tvContent.setText(comment.getContent());
        ratingBar.setRating(comment.getRateStar());

        return convertView;
    }
}