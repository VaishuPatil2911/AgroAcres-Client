package com.example.foodapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.Adpeters.OrderAdapter;
import com.example.foodapp.Model.OrderModel;
import com.example.foodapp.databinding.ActivityOrdersBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

public class OrdersActivity extends AppCompatActivity {

    ActivityOrdersBinding binding;
    DatabaseReference reference;

    ArrayList<OrderModel> list = new ArrayList<>();
    OrderAdapter adapter;

    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrdersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference().child("Orders");
        user = FirebaseAuth.getInstance().getCurrentUser();


        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        new Handler().postDelayed(this::getOngoingOrders,600);


    }

    private void getOngoingOrders() {
        binding.recyclerView.setHasFixedSize(true);
        reference.child(user.getUid()).limitToFirst(70).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        OrderModel model = dataSnapshot.getValue(OrderModel.class);
                        assert model !=null;
                        list.add(model);


                    }

                    orderByDate();
                    adapter = new OrderAdapter(list);
                    binding.recyclerView.setAdapter(adapter);

                    if (list.size() > 0){
                        binding.layout.root.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                    }else {
                        binding.layout.root.setVisibility(View.VISIBLE);
                        binding.layout.noText.setText("No ongoing orders!");
                    }

                }else {
                    binding.layout.root.setVisibility(View.VISIBLE);
                    binding.layout.noText.setText("No ongoing orders!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(OrdersActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void orderByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();

        Collections.sort(list, (o1, o2) -> {
            calendar.setTimeInMillis( o1.getTimestamp());
            calendar1.setTimeInMillis(o2.getTimestamp());


            String d1 = dateFormat.format(calendar.getTime());
            String d2 = dateFormat.format(calendar1.getTime());


            return d2.compareTo(d1);

        });
    }
}