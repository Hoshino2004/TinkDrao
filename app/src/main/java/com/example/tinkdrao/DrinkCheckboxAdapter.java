package com.example.tinkdrao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tinkdrao.model.Drink;

import java.util.List;

public class DrinkCheckboxAdapter extends BaseAdapter {
    private Context context;
    private List<Drink> drinkList;
    private List<Drink> selectedDrinks;

    public DrinkCheckboxAdapter(Context context, List<Drink> drinkList, List<Drink> selectedDrinks) {
        this.context = context;
        this.drinkList = drinkList;
        this.selectedDrinks = selectedDrinks;
    }

    @Override
    public int getCount() {
        return drinkList.size();
    }

    @Override
    public Object getItem(int position) {
        return drinkList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_drink_checkbox_with_details, parent, false);
        }

        Drink drink = drinkList.get(position);
        ImageView ivDrink = convertView.findViewById(R.id.iv_drink);
        TextView tvName = convertView.findViewById(R.id.tv_drink_name);
        TextView tvPrice = convertView.findViewById(R.id.tv_drink_price);
        CheckBox cbSelect = convertView.findViewById(R.id.cb_select);

        // Hiển thị ảnh
        Glide.with(context).load(drink.getImageUrl()).into(ivDrink);
        tvName.setText(drink.getName());
        tvPrice.setText(String.format("Giá: %.2f", drink.getPrice()));
        cbSelect.setChecked(selectedDrinks.contains(drink));

        cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (!selectedDrinks.contains(drink)) {
                    selectedDrinks.add(drink);
                }
            } else {
                selectedDrinks.remove(drink);
            }
        });

        return convertView;
    }
}