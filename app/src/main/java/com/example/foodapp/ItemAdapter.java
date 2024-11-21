package com.example.foodapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Model.Products;
import com.example.foodapp.databinding.ItemProductBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    private Context context;
    public static ArrayList<Products> productsList;

    ItemProductBinding binding;

    public ItemAdapter(Context context, ArrayList<Products> list) {
        this.context = context;
        this.productsList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = ItemProductBinding.inflate(LayoutInflater.from(context));

        return  new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position){
        Products model = productsList.get(position);
    if(model!=null){


        if(model.getStock()){
            binding.imgSold.setVisibility(View.GONE);
        }else{
            binding.imgSold.setVisibility(View.VISIBLE);
        }
        binding.productTitle.setText(model.getName());
        binding.amount.setText("₹"+model.getPrice()+".0");
        try{
            Picasso.get().load(model.getImage()).placeholder(R.drawable.food1)
                    .into(binding.productImage);
        }catch (Exception e){
            e.getMessage();
        }


        holder.itemView.setOnClickListener(v ->{
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("pos",position);
            context.startActivity(intent);
        });

    }

    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ViewHolder(@NonNull ItemProductBinding productBinding) {
            super(productBinding.getRoot());
            binding = productBinding;
        }
    }

}
