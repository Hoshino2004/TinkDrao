package com.example.tinkdrao.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Drink;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SearchDrinkAdapter extends RecyclerView.Adapter<SearchDrinkAdapter.DrinkViewHolder> {
    private List<Drink> drinkList;
    private List<Drink> filteredList; // Danh sách sau khi lọc
    private Context context;
    private DecimalFormat decimalFormat; // Định dạng giá tiền

    public SearchDrinkAdapter(Context context, List<Drink> drinkList) {
        this.context = context;
        this.drinkList = drinkList;
        this.filteredList = new ArrayList<>(drinkList); // Khởi tạo danh sách lọc
        // Khởi tạo DecimalFormat với dấu chấm ngăn cách hàng nghìn
        decimalFormat = new DecimalFormat("#,###"); // Dấu chấm: 12.000
        decimalFormat.setDecimalSeparatorAlwaysShown(false); // Không hiển thị phần thập phân
    }

    @NonNull
    @Override
    public DrinkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_drink, parent, false);
        return new DrinkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrinkViewHolder holder, int position) {
        Drink drink = filteredList.get(position);

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

        // Load ảnh bằng Glide
        Glide.with(context)
                .load(drink.getImageUrl())
                .placeholder(R.drawable.loading)
                .error(R.drawable.loading)
                .into(holder.drinkImage);
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    // Hàm lọc danh sách theo tên (hỗ trợ không dấu)
    public void filter(String query, float minPrice, float maxPrice, List<String> selectedDrinkType) {
        filteredList.clear();

        if (query.isEmpty() && (selectedDrinkType == null || selectedDrinkType.contains("--Tất cả--") || selectedDrinkType.isEmpty()) && minPrice == 0 && maxPrice == 500000) {
            filteredList.addAll(drinkList);
        } else {
            String queryNoDiacritics = removeDiacritics(query).toLowerCase();
            for (Drink drink : drinkList) {
                String drinkNameNoDiacritics = removeDiacritics(drink.getName()).toLowerCase();

                boolean matchesDrinkType = selectedDrinkType == null || selectedDrinkType.contains("--Tất cả--")
                        || selectedDrinkType.isEmpty() || selectedDrinkType.contains(drink.getDrinkType());

                double discountedPrice = drink.getPrice() * (100 - drink.getDiscount()) / 100;
                boolean matchesLocGia = discountedPrice >= minPrice && discountedPrice <= maxPrice;

                if (drinkNameNoDiacritics.contains(queryNoDiacritics) && matchesDrinkType && matchesLocGia) {
                    filteredList.add(drink);
                }
            }
        }

        notifyDataSetChanged();
    }

    public static String removeDiacritics(String str) {
        if (str == null) return "";
        str = str.toLowerCase();
        str = str.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a");
        str = str.replaceAll("[èéẹẻẽêềếệểễ]", "e");
        str = str.replaceAll("[ìíịỉĩ]", "i");
        str = str.replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o");
        str = str.replaceAll("[ùúụủũưừứựửữ]", "u");
        str = str.replaceAll("[ỳýỵỷỹ]", "y");
        str = str.replaceAll("đ", "d");
        return str;
    }

    // Hàm cập nhật danh sách gốc từ Firebase
    public void updateList(List<Drink> newList) {
        drinkList.clear();
        drinkList.addAll(newList);
        filteredList.clear();
        filteredList.addAll(drinkList); // Đồng bộ filteredList với drinkList
        notifyDataSetChanged();
    }

    public static class DrinkViewHolder extends RecyclerView.ViewHolder {
        ImageView drinkImage;
        TextView drinkName;
        TextView drinkOriginalPrice;
        TextView drinkDiscountedPrice;
        TextView drinkDiscountedPercent;

        public DrinkViewHolder(@NonNull View itemView) {
            super(itemView);
            drinkImage = itemView.findViewById(R.id.drinkSearchImage);
            drinkName = itemView.findViewById(R.id.drinkSearchName);

            drinkOriginalPrice = itemView.findViewById(R.id.drinkSearchOriginalPrice);
            drinkOriginalPrice.setPaintFlags(drinkOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            drinkDiscountedPrice = itemView.findViewById(R.id.drinkSearchDiscountedPrice);

            drinkDiscountedPercent = itemView.findViewById(R.id.drinkSearchDiscountPercent);
        }
    }
}