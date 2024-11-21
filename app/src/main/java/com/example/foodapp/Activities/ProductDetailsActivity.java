package com.example.foodapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.CartSystem.CartActivity;
import com.example.foodapp.Listeners.ICartLoadListener;
import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.Products;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityProductDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;

public class ProductDetailsActivity extends AppCompatActivity implements ICartLoadListener {

    ActivityProductDetailsBinding binding;
    DatabaseReference reference;
    ICartLoadListener cartLoadListener;

    Products products;
    String id,category;


    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(MyUpdateCartEvent.class))
            EventBus.getDefault().removeStickyEvent(MyUpdateCartEvent.class);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onUpdateCart(MyUpdateCartEvent event){
        checkCartStatus();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        cartLoadListener = this;

        id = getIntent().getStringExtra("id");
        category = getIntent().getStringExtra("category");

        reference = FirebaseDatabase.getInstance().getReference();

        binding.dataLyt.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);

        getProductDetails();


        binding.btnAddToBag.setOnClickListener(v -> addToCart());
        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        binding.btnProceed.setOnClickListener(view ->
                startActivity(new Intent(ProductDetailsActivity.this, CartActivity.class)));



    }
    @Override
    public void onCartLoadListener(ArrayList<CartModel> list) {
        int cartSum = 0;
        for (CartModel cartModel : list){
            cartSum +=cartModel.getQuantity();

        }


    }


    private void getProductDetails(){
        reference.child("Products").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    products = snapshot.getValue(Products.class);

                    binding.dataLyt.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);

                    Products products = snapshot.getValue(Products.class);
                    assert products !=null;

                    category = products.getCategory();
                    binding.productName.setText(products.getName());
                    try {
                        Picasso.get().load(products.getImage()).placeholder(R.drawable.fruits)
                                .into(binding.productImage);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                    binding.description.setText(products.getDescription());
                    binding.sellPrice.setText("₹"+products.getPrice());

                    checkCartStatus();


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProductDetailsActivity.this, "Failed: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkCartStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<CartModel> cartList = new ArrayList<>();
        assert user != null;
        reference.child("Cart").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(id).hasChildren()){
                            binding.btnAddToBag.setVisibility(View.INVISIBLE);
                            binding.txtAdded.setVisibility(View.VISIBLE);

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                CartModel model = dataSnapshot.getValue(CartModel.class);
                                cartList.add(model);
                            }

                            cartLoadListener.onCartLoadListener(cartList);
                        }else {
                            binding.btnAddToBag.setVisibility(View.VISIBLE);

                            binding.txtAdded.setVisibility(View.GONE);



                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ProductDetailsActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addToCart() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cart")
                .child(user.getUid());

        CartModel model = new CartModel();
        model.setKey(products.getId());
        model.setQuantity(1);
        model.setName(products.getName());
        model.setImage(products.getImage());
        model.setPrice(products.getPrice());

        model.setTotalPrice(products.getPrice());
        model.setPublisher(products.getPublisher());

        reference.child(products.getId()).setValue(model);
        binding.btnAddToBag.setVisibility(View.INVISIBLE);
        binding.txtAdded.setVisibility(View.VISIBLE);

        //
        EventBus.getDefault().postSticky(new MyUpdateCartEvent());


    }
}