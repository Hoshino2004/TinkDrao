package com.example.tinkdrao.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tinkdrao.R;
import com.example.tinkdrao.model.Order;

import java.text.DecimalFormat;
import java.util.List;

public class History_Order_Adapter extends ArrayAdapter {
    private Activity mContext;
    List<Order> htcList;
    public History_Order_Adapter(Activity mContext,List<Order> htcList){
        super(mContext, R.layout.items4,htcList);
        this.mContext=mContext;
        this.htcList = htcList;
    }

    DecimalFormat formatter = new DecimalFormat("###,###,###");

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View listItemView = inflater.inflate(R.layout.items4,null,true);

        TextView hcName = listItemView.findViewById(R.id.hcName);
        TextView hcPhoneNumber = listItemView.findViewById(R.id.hcPhoneNumber);
        TextView hcAddress = listItemView.findViewById(R.id.hcAddress);
        TextView hcStatus = listItemView.findViewById(R.id.hcStatus);
        TextView hcTotal = listItemView.findViewById(R.id.hcTotal);
        TextView hcDate = listItemView.findViewById(R.id.hcDate);
        TextView hcStatusPay = listItemView.findViewById(R.id.hcStatusPay);

        Order htc = htcList.get(position);
        hcName.setText(htc.getNameUser());
        hcPhoneNumber.setText(htc.getPhoneNo());
        hcAddress.setText(htc.getAddress());
        hcStatus.setText(htc.getStatusOrder());
        hcTotal.setText(String.valueOf(formatter.format(htc.getTotal()))+"đ");
        hcStatusPay.setText(htc.getStatusPay());
        hcDate.setText(htc.getCreatedAt());
        return listItemView;
    }
}
