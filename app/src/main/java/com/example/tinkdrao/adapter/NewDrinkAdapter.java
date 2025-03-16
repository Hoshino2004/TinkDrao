package com.example.tinkdrao.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.DrinkDetailActivity;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class NewDrinkAdapter extends RecyclerView.Adapter<NewDrinkAdapter.NewDrinkViewHolder> {
    private List<Drink> drinkList;
    private Context context;
    private DecimalFormat decimalFormat; // Định dạng giá tiền
    private SimpleDateFormat dateFormat; // Để parse createdAt

    public NewDrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
        // Khởi tạo DecimalFormat với dấu chấm ngăn cách hàng nghìn
        decimalFormat = new DecimalFormat("#,###"); // Dấu chấm: 12.000
        decimalFormat.setDecimalSeparatorAlwaysShown(false); // Không hiển thị phần thập phân
        // Định dạng ngày giờ từ chuỗi createdAt
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Sắp xếp danh sách theo createdAt ngay khi khởi tạo
        sortByCreatedAt();
    }

    @NonNull
    @Override
    public NewDrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_new_drink, parent, false);
        return new NewDrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewDrinkViewHolder holder, int position) {
        Drink drink = drinkList.get(position);

        holder.drinkName.setText(drink.getName());
        // Hiển thị giá gốc nếu có giảm giá
        if (drink.getDiscount() > 0) {
            double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
            holder.drinkDiscountedPercent.setText("-" + decimalFormat.format(drink.getDiscount()) + "%");
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) discountedPrice) + "₫");
            holder.drinkOriginalPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
            holder.drinkOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.drinkOriginalPrice.setVisibility(View.GONE);
            holder.drinkDiscountedPercent.setVisibility(View.GONE);
            holder.drinkDiscountedPrice.setText(decimalFormat.format((int) drink.getPrice()) + "₫");
        }

        if(drink.getQuantity() == 0 )
        {
            holder.soldOutText.setVisibility(View.VISIBLE);
        }

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(drink.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);

        // Thêm sự kiện click để chuyển sang DrinkDetailActivity
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DrinkDetailActivity.class);
            intent.putExtra("id", drink.getId()); // Truyền ID của sản phẩm
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return drinkList.size();
    }

    // Hàm sắp xếp danh sách theo createdAt (mới nhất lên đầu)
    private void sortByCreatedAt() {
        Collections.sort(drinkList, new Comparator<Drink>() {
            @Override
            public int compare(Drink d1, Drink d2) {
                try {
                    Date date1 = dateFormat.parse(d1.getCreatedAt());
                    Date date2 = dateFormat.parse(d2.getCreatedAt());
                    return date2.compareTo(date1); // Giảm dần: mới nhất lên đầu
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // Nếu lỗi, giữ nguyên thứ tự
                }
            }
        });
        notifyDataSetChanged();
    }

    // Hàm cập nhật danh sách từ bên ngoài (nếu lấy từ Firebase)
    public void updateList(List<Drink> newList) {
        drinkList.clear();
        drinkList.addAll(newList);
        sortByCreatedAt(); // Sắp xếp lại sau khi cập nhật
    }

    public static class NewDrinkViewHolder extends RecyclerView.ViewHolder {
        ImageView drinkImage;
        TextView drinkName;
        TextView drinkOriginalPrice;
        TextView drinkDiscountedPrice;
        TextView drinkDiscountedPercent;
        TextView soldOutText;

        public NewDrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.newDrinkImage);
            drinkName = itemView.findViewById(R.id.newDrinkName);

            drinkOriginalPrice = itemView.findViewById(R.id.newDrinkOriginalPrice);
            drinkOriginalPrice.setPaintFlags(drinkOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            drinkDiscountedPrice = itemView.findViewById(R.id.newDrinkDiscountedPrice);
            drinkDiscountedPercent = itemView.findViewById(R.id.newDrinkDiscountPercent);
            soldOutText = itemView.findViewById(R.id.soldOutText);
        }
    }
}
