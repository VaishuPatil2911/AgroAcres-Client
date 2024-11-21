package com.example.foodapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.Adpeters.ProductAdapter;
import com.example.foodapp.CartSystem.CartAdapter;
import com.example.foodapp.Listeners.ICartLoadListener;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.OrderModel;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityOrderDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class OrderDetailsActivity extends AppCompatActivity implements ICartLoadListener {

    ActivityOrderDetailsBinding binding;
    DatabaseReference reference;
    private String orderId;

    ArrayList<CartModel> list = new ArrayList<>();
    CartAdapter adapter;
    ProductAdapter productAdapter;


    FirebaseUser user;

    ICartLoadListener cartLoadListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        user = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference().child("Orders")
                .child(user.getUid());
        orderId = getIntent().getStringExtra("orderId");

        binding.mainProgress.setVisibility(View.VISIBLE);
        binding.dataLyt.setVisibility(View.GONE);

        cartLoadListener = this;


        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        checkOrderStatus();

    }

    private void checkOrderStatus(){
        reference.child(orderId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    binding.mainProgress.setVisibility(View.GONE);
                    binding.dataLyt.setVisibility(View.VISIBLE);

                    OrderModel model = snapshot.getValue(OrderModel.class);

                    binding.orderId.setText("Order ID: "+model.getOrderId());

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                    String placedDate = dateFormat.format(model.getTimestamp());

                    binding.txtDate.setText(placedDate);


                    String status = model.getStatus();
                    binding.txtStatus.setText(model.getStatus());
                    switch (status){
                        case "Placed":
                            binding.txtStatus.setTextColor(getResources().getColor(R.color.blueColor));
                            break;
                        case "Shipped":
                            binding.txtStatus.setTextColor(getResources().getColor(R.color.status_shipped));
                            break;

                        case "Cancelled":
                            binding.txtStatus.setTextColor(getResources().getColor(R.color.status_rejected));
                            break;
                        case "Delivered":
                            binding.txtStatus.setTextColor(getResources().getColor(R.color.status_delivered));
                            break;
                    }


                    getItems();



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailsActivity.this, "Error: "+
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getItems() {
        binding.recyclerView.setHasFixedSize(true);
        reference.child(orderId).child("items").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    list.clear();
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        CartModel model = dataSnapshot.getValue(CartModel.class);
                        list.add(model);
                    }

                    cartLoadListener.onCartLoadListener(list);
                    adapter = new CartAdapter(OrderDetailsActivity.this,list,"order_details");
                    binding.recyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(OrderDetailsActivity.this, "Error: "+
                        error.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onCartLoadListener(ArrayList<CartModel> list) {
        double sum = 0;
        int count =0;
        double fullAmount = 0;
        for (CartModel cartModel : list){
            sum += cartModel.getTotalPrice();
            count +=cartModel.getQuantity();
            fullAmount +=cartModel.getQuantity() * cartModel.getPrice();
        }

        binding.sumTotal.setText(new StringBuilder("₹ ").append(sum));

        binding.sumRealPrice.setText(new StringBuilder("₹ ").append(fullAmount));
        binding.sumItems.setText(String.valueOf(count));

        //final discount

        double discount = fullAmount - sum;
        binding.sumDiscount.setText(new StringBuilder("₹ -").append(discount));

    }
}