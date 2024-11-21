package com.example.foodapp.Adpeters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Activities.OrderDetailsActivity;
import com.example.foodapp.Model.OrderModel;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ItemOrdersBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder>  {

    private ArrayList<OrderModel> list;

    ItemOrdersBinding binding;
    public OrderAdapter(ArrayList<OrderModel> list) {
        this.list = list;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ItemOrdersBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        OrderModel model = list.get(position);
        assert model !=null;

        binding.orderId.setText(String.format("order id: %s", model.getOrderId()));
        binding.totalOrderAmount.setText(String.format("Total Amount: ₹ %s", model.getTotalAmount()));

        //status
        String status = model.getStatus();
        binding.orderStatus.setText(status);


        if (status.equals("Shipped")){
            binding.orderStatus.setText(binding.getRoot().getResources().getString(R.string.order_shipped));
            binding.orderStatus.setTextColor(binding.orderDate.getContext().getResources().getColor(R.color.status_shipped));
        }else if (status.equals("Placed"))
        {

            binding.orderStatus.setText(binding.getRoot().getResources().getString(R.string.order_placed));
        }

        else if (status.equals("Delivered")){

            binding.orderStatus.setText(binding.getRoot().getResources().getString(R.string.order_delivered));
            binding.orderStatus.setTextColor(binding.orderDate.getContext().getResources().getColor(R.color.status_delivered));
        }else if (status.equals("Cancelled")){

            binding.orderStatus.setText(binding.getRoot().getResources().getString(R.string.order_cancelled));
            binding.orderStatus.setTextColor(binding.orderDate.getContext().getResources().getColor(R.color.status_rejected));


        }



        //date

        long timestamp = model.getTimestamp();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String date = dateFormat.format(timestamp);
        binding.orderDate.setText(String.format("Date: %s", date));


        holder.itemView.setOnClickListener(v -> v.getContext().startActivity(new Intent(v.getContext(),
                OrderDetailsActivity.class)
                .putExtra("orderId",model.getOrderId())));



        binding.btnOrderDetails.setOnClickListener(v ->
                v.getContext().startActivity(new Intent(v.getContext(), OrderDetailsActivity.class)
                        .putExtra("orderId",model.getOrderId())));




    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOrdersBinding binding;
        public ViewHolder( ItemOrdersBinding ordersBinding) {
            super(ordersBinding.getRoot());
            binding = ordersBinding;
        }
    }
}
