//Kushwanth23
package com.example.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodapp.Activities.AccountActivity;
import com.example.foodapp.Adpeters.ProductAdapter;
import com.example.foodapp.CartSystem.CartActivity;
import com.example.foodapp.Listeners.ICartLoadListener;
import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.Products;
import com.example.foodapp.Model.UserModel;
import com.example.foodapp.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements ICartLoadListener  {

    ActivityMainBinding binding;

    ArrayList<Products> list = new ArrayList<>();
    ProductAdapter adapter;

    DatabaseReference reference;
    ICartLoadListener cartLoadListener;

    FirebaseUser user;


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
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference();
        binding.progressBar.setVisibility(View.VISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();

        new Handler().postDelayed(this::getProducts,1000);
        cartLoadListener = this;

        checkCartStatus();

        binding.cartItemLyt.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this,CartActivity.class)));

        binding.accountLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AccountActivity.class));

            }
        });
        getUseDetails();

    }

    private void getProducts(){

        binding.recyclerView.setHasFixedSize(true);


        reference.child("Products").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
//                    binding.dataLyt.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        Products products = dataSnapshot.getValue(Products.class);
                        assert products != null;
                        list.add(products);
                    }
                    LinearLayoutManager manager = new GridLayoutManager(MainActivity.this,3);
                    binding.recyclerView.setLayoutManager(manager);

                    adapter = new ProductAdapter(list);
                    binding.recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCartLoadListener(ArrayList<CartModel> list) {
        int cartSum = 0;
        for (CartModel cartModel : list){
            cartSum +=cartModel.getQuantity();

        }
        binding.badgeCount.setText(String.valueOf(cartSum));

    }

    private void checkCartStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<CartModel> cartList = new ArrayList<>();
        assert user != null;
        reference.child("Cart").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            binding.badgeLyt.setVisibility(View.VISIBLE);
                            binding.cartItemLyt.setVisibility(View.VISIBLE);
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                CartModel model = dataSnapshot.getValue(CartModel.class);
                                cartList.add(model);
                            }

                            cartLoadListener.onCartLoadListener(cartList);
                        }else {
                            binding.badgeLyt.setVisibility(View.GONE);
                            binding.cartItemLyt.setVisibility(View.GONE);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUseDetails(){
        reference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model !=null){
                        String letter = String.valueOf(model.getUsername().charAt(0));
                        binding.firstLetter.setText(letter);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }



}