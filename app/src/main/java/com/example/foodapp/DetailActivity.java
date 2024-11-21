//Kushwanth23
package com.example.foodapp;

import static com.example.foodapp.ItemAdapter.productsList;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.foodapp.Listeners.ICartLoadListener;
import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.Products;
import com.example.foodapp.Model.SliderModel;
import com.example.foodapp.Model.UserModel;
import com.example.foodapp.databinding.ActivityDetailBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements ICartLoadListener {

    public static final String TAG="Database";

    ActivityDetailBinding binding;

    Products model;
    int position;

    DatabaseReference reference;
    FirebaseUser user;

    String email,username,phone;
    ICartLoadListener cartLoadListener;

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


    ArrayList<SliderModel> list = new ArrayList<>();
    SliderAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        cartLoadListener = this;

        position = getIntent().getIntExtra("pos",0);

        model = productsList.get(position);

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        getProductsDat();
        getUserData();


    }

    private void getProductsDat(){
        reference.child("Products").child(model.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    Products model = snapshot.getValue(Products.class);
                    if (model !=null){


                        if(model.getStock()){
                            binding.txtSold.setVisibility(View.GONE);
                            binding.btnPurchase.setVisibility(View.VISIBLE);

                            checkCartStatus();
                        }else{

                            binding.txtSold.setVisibility(View.VISIBLE);
                            binding.btnPurchase.setVisibility(View.GONE);


                        }
                        binding.name.setText(model.getName());
                        binding.price.setText("₹"+model.getPrice()+".0");

                        binding.description.setText(model.getDescription());

                        binding.category.setText(model.getCategory());
                        binding.txtQuantity.setText(model.getQuantity());

                        binding.btnPurchase.setOnClickListener(v -> {
                            addToCart(model);
                        });
                        getSliderImage(model.getId());
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getSliderImage(String id){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Images")
                .child(id);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    list.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        SliderModel model = dataSnapshot.getValue(SliderModel.class);
                        list.add(model);
                    }
                    adapter = new SliderAdapter(DetailActivity.this,list);
                    binding.viewPager.setAdapter(adapter);
                    binding.dotsIndicator.setViewPager(binding.viewPager);
                    binding.viewPager.addOnPageChangeListener(viewPagerPageChangeListener);

                    new Handler().postDelayed(() -> {
                        int current = getItem();
                        if (current < list.size()) {
                            // move to next screen
                            binding.viewPager.setCurrentItem(current);
                        }
                    },5000);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private int getItem() {
        return binding.viewPager.getCurrentItem() + 1;
    }
    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            // changing the next button text 'NEXT' / 'GOT IT'
            if (position == list.size() - 1) {
                // last page. make button text to GOT IT

                new Handler().postDelayed(() -> binding.viewPager.setCurrentItem(0),5000);


            }else {
                new Handler().postDelayed(() -> {
                    int current = getItem();
                    if (current < list.size()) {
                        // move to next screen
                        binding.viewPager.setCurrentItem(current);
                    }
                },5000);

            }
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };


    private void addToCart(Products products){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cart")
                .child(user.getUid());

        CartModel model = new CartModel();
        model.setKey(products.getId());
        model.setQuantity(1);
        model.setName(products.getName());
        model.setImage(list.get(0).getLinks());
        model.setPrice(products.getPrice());
        model.setTotalPrice(products.getPrice());
        model.setPublisher(products.getPublisher());



        reference.child(products.getId()).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    binding.btnPurchase.setVisibility(View.GONE);
                    binding.txtAdded.setVisibility(View.VISIBLE);

                    Toast.makeText(DetailActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
                }else {

                    binding.btnPurchase.setVisibility(View.VISIBLE);
                    binding.txtAdded.setVisibility(View.GONE);
                    Toast.makeText(DetailActivity.this, "Failed: "+
                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });


        EventBus.getDefault().postSticky(new MyUpdateCartEvent());


    }

    private void checkCartStatus(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        ArrayList<CartModel> cartList = new ArrayList<>();
        assert user != null;
        reference.child("Cart").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child(model.getId()).hasChildren()){
                            binding.btnPurchase.setVisibility(View.GONE);
                            binding.txtSold.setVisibility(View.GONE);
                            binding.txtAdded.setVisibility(View.VISIBLE);
                            binding.cartItemCount.setVisibility(View.VISIBLE);

                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                CartModel model = dataSnapshot.getValue(CartModel.class);
                                cartList.add(model);
                            }

                            if (cartList.size() > 0){
                                binding.cartItemCount.setVisibility(View.VISIBLE);

                            }else {
                                binding.cartItemCount.setVisibility(View.GONE);

                            }

                            cartLoadListener.onCartLoadListener(cartList);
                        }else {
                            binding.btnPurchase.setVisibility(View.VISIBLE);

                            binding.txtSold.setVisibility(View.GONE);
                            binding.txtAdded.setVisibility(View.GONE);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(DetailActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCartLoadListener(ArrayList<CartModel> list) {
        int cartSum = 0;
        for (CartModel cartModel : list){
            cartSum +=cartModel.getQuantity();

        }
        binding.cartItemCount.setText(String.valueOf(cartSum));


    }



//    private void showOrderDialogue(Products model) {
//        DialogueDdressBinding binding = DialogueDdressBinding.inflate(getLayoutInflater());
//        Dialog dialog = new Dialog(DetailActivity.this);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(binding.getRoot());
//        dialog.setCancelable(true);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//
//
//        binding.btnSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String address = binding.inputAddress.getText().toString();
//                if (address.isEmpty()){
//                    Toast.makeText(DetailActivity.this, "Enter address to continue!", Toast.LENGTH_SHORT).show();
//                }else {
//                    dialog.dismiss();
//                    saveOrder(model);
//                }
//            }
//        });
//
//        dialog.show();
//    }
//
//    private void saveOrder(Products model) {
//        ProgressDialog progressDialog = new ProgressDialog(DetailActivity.this);
//        progressDialog.setMessage("Order Placing...");
//        progressDialog.setCancelable(false);
//        progressDialog.show();
//
//        HashMap<String,Object> map = new HashMap<>();
//        map.put("username",username);
//        map.put("email",email);
//        map.put("phone",phone);
//        map.put("price",model.getPrice());
//        map.put("productName",model.getName());
//        map.put("timestamp",System.currentTimeMillis());
//
//
//        reference.child("Orders").push().setValue(map)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()){
//                        progressDialog.dismiss();
//                        finish();
//                        Toast.makeText(DetailActivity.this, "Order has been sent!", Toast.LENGTH_SHORT).show();
//                    }else {
//                        progressDialog.dismiss();
//                        Toast.makeText(DetailActivity.this, "Failed: "+
//                                task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//
//                    }
//                });
//
//
//
//
//
//    }

    private void getUserData(){
        reference.child("Users").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model !=null){
                        email = model.getEmail();
                        phone = model.getPhone();
                        username = model.getUsername();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DetailActivity.this, "Error: "+
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}