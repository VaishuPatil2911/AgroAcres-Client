//Kushwanth23
package com.example.foodapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.foodapp.Model.Products;
import com.example.foodapp.databinding.ActivityVegetablesListBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VegetablesListActivity extends AppCompatActivity {

    ActivityVegetablesListBinding binding;
    DatabaseReference reference;

    ArrayList<Products> list = new ArrayList<>();
    ItemAdapter adapter;

    String category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVegetablesListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        category = getIntent().getStringExtra("cat");

        reference = FirebaseDatabase.getInstance().getReference().child("Products");
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setHasFixedSize(true);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getProducts();
            }
        },300);

    }
    private void getProducts(){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    binding.progressBar.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Products products = dataSnapshot.getValue(Products.class);
                        if (products.getCategory().equals(category)){
                            list.add(products);
                        }
                    }

                    GridLayoutManager gridLayoutManager = new GridLayoutManager(VegetablesListActivity.this,3);
                    binding.recyclerView.setLayoutManager(gridLayoutManager);

                    adapter = new ItemAdapter(VegetablesListActivity.this,list);
                    binding.recyclerView.setAdapter(adapter);
                }else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(VegetablesListActivity.this, "No products now!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(VegetablesListActivity.this, "Error: "+
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}