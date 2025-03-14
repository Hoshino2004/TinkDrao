package com.example.tinkdrao.adapter;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.tinkdrao.DrinkDetailActivity;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;

import java.util.List;

public class ImageSliderAdapter extends RecyclerView.Adapter<ImageSliderAdapter.ViewHolder> {
    private List<Drink> drinkList;
    private Context context;

    public ImageSliderAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.slider_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drink drink = drinkList.get(position);
        Glide.with(context).load(drink.getImageUrl()).into(holder.imageView);
        // Thêm sự kiện click để chuyển sang DrinkDetailActivity
        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DrinkDetailActivity.class);
            intent.putExtra("id", drink.getId()); // Truyền ID của sản phẩm
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}
