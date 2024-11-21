package com.example.foodapp.CartSystem;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.Activities.OrdersActivity;
import com.example.foodapp.Listeners.ICartLoadListener;
import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.Address;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.OrderModel;
import com.example.foodapp.Model.UserModel;
import com.example.foodapp.R;
import com.example.foodapp.databinding.ActivityCartBinding;
import com.example.foodapp.databinding.AddressDialogueBinding;
import com.example.foodapp.databinding.DialogueDdressBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import es.dmoral.toasty.Toasty;


public class CartActivity extends AppCompatActivity implements ICartLoadListener, PaymentResultListener {


    ActivityCartBinding binding;
    ICartLoadListener cartLoadListener;

    DatabaseReference reference;
    FirebaseUser user;

    ArrayList<CartModel> list = new ArrayList<>();
    private double sum = 0;

    String email,username,phone;
    private boolean isAddressAvailable = false;
    private String strAddress;

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
        loadCartItems();
    }







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.dataLyt.setVisibility(View.GONE);

        reference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        binding.recyclerView.setHasFixedSize(true);

        cartLoadListener = CartActivity.this;
        loadCartItems();




        getUserData();
        checkAddress();

        binding.btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddressDialogue();
            }
        });


    }



    private void showAddressDialogue(){
        AddressDialogueBinding dialogueBinding = AddressDialogueBinding.inflate(getLayoutInflater());
        Dialog dialog = new Dialog(CartActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(dialogueBinding.getRoot());
        dialog.setCancelable(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        if (isAddressAvailable){
            dialogueBinding.selectAddressLyt.setVisibility(View.VISIBLE);
            dialogueBinding.newAddressLyt.setVisibility(View.GONE);

            dialogueBinding.fullAddress.setText(strAddress);

            dialogueBinding.btnChangeAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(CartActivity.this,AddressActivity.class)
                            .putExtra("purpose","update"));
                }
            });

            dialogueBinding.btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startPayment();
                }
            });

        }else {
            dialogueBinding.selectAddressLyt.setVisibility(View.GONE);
            dialogueBinding.newAddressLyt.setVisibility(View.VISIBLE);

            dialogueBinding.btnAddAddress.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(CartActivity.this,AddressActivity.class)
                            .putExtra("purpose","add"));
                }
            });
        }

        dialog.show();







    }

    private void checkAddress(){
        reference.child("Address").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    isAddressAvailable = true;
                    Address address = snapshot.getValue(Address.class);
                    assert address !=null;

                    strAddress = String.format("%s\n%s, %s\n%s, %s\n%s",
                            address.getHouseNo(), address.getLandmark(), address.getArea(), address.getCity(),
                            address.getPinCode(), address.getCountry());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onCartLoadListener(ArrayList<CartModel> list) {
        sum = 0;
        int count =0;

        for (CartModel cartModel : list){
            sum += cartModel.getTotalPrice();
            count +=cartModel.getQuantity();
        }
        binding.totalAmountText.setText(new StringBuilder("₹ ").append(sum));

        binding.textItemsCount.setText(new StringBuilder().append("Items (").append(count).append(")").toString());

        binding.recyclerView.setNestedScrollingEnabled(false);
        CartAdapter adapter = new CartAdapter(this,list,"cart");
        binding.recyclerView.setAdapter(adapter);



    }


    private void loadCartItems(){


        reference.child("Cart").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            list.clear();
                            binding.progressBar.setVisibility(View.GONE);
                            binding.dataLyt.setVisibility(View.VISIBLE);


                            for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                                CartModel model = dataSnapshot.getValue(CartModel.class);
                                list.add(model);

                            }



                            checkIfNoData();
                            cartLoadListener.onCartLoadListener(list);
                        }else {
                            binding.dataLyt.setVisibility(View.GONE);
                            binding.progressBar.setVisibility(View.GONE);
                            binding.layout.root.setVisibility(View.VISIBLE);

                            binding.layout.noText.setText("No cart items!");
                            binding.layout.noImage.setImageResource(R.drawable.cart_icon);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.dataLyt.setVisibility(View.GONE);
                        Toast.makeText(CartActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkIfNoData(){
        if (list.size() > 0){
            binding.dataLyt.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.layout.root.setVisibility(View.GONE);
        }else {
            binding.dataLyt.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.layout.root.setVisibility(View.VISIBLE);

            binding.layout.noText.setText("No cart items!");
            binding.layout.noImage.setImageResource(R.drawable.cart_icon);
        }
    }


//    private void showOrderDialogue() {
//        DialogueDdressBinding binding = DialogueDdressBinding.inflate(getLayoutInflater());
//        Dialog dialog = new Dialog(CartActivity.this);
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
//                    Toast.makeText(CartActivity.this, "Enter address to continue!", Toast.LENGTH_SHORT).show();
//                }else {
//                    dialog.dismiss();
//                    saveOrder(address);
//                }
//            }
//        });
//
//        dialog.show();
//    }

    private void saveOrder(String address) {
        ProgressDialog progressDialog = new ProgressDialog(CartActivity.this);
        progressDialog.setMessage("Order Placing...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String id = String.valueOf(new Random().nextInt(1000));

        OrderModel model = new OrderModel();
        model.setUserId(user.getUid());
        model.setTimestamp(System.currentTimeMillis());
        model.setStatus("Placed");
        model.setTotalAmount((int) sum);
        model.setOrderId(id);
        model.setItems(list);
        model.setAddressId(address);

//        HashMap<String,Object> map = new HashMap<>();
//        map.put("username",username);
//        map.put("email",email);
//        map.put("phone",phone);
//        map.put("orderId",id);
//        map.put("items",list);
//        map.put("price",sum);
//        map.put("timestamp",System.currentTimeMillis());


        reference.child("Orders").child(user.getUid()).child(id).setValue(model)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        progressDialog.dismiss();
                        reference.child("Cart").child(user.getUid())
                                .removeValue();
                        Intent intent = new Intent(CartActivity.this, OrdersActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                        Toasty.success(getApplicationContext(),"Order has been placed!",Toasty.LENGTH_SHORT).show();
                    }else {
                        progressDialog.dismiss();
                        Toasty.error(getApplicationContext(),"Error: "+task.getException()
                                .getMessage(),Toasty.LENGTH_SHORT).show();


                    }
                });





    }

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
                Toast.makeText(CartActivity.this, "Error: "+
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startPayment(){

        ProgressDialog progressDialog = new ProgressDialog(CartActivity.this);
        progressDialog.setMessage("Payment processing..");
        progressDialog.setCancelable(false);
        progressDialog.show();

        String price = String.valueOf(sum);
        int amount = Math.round(Float.parseFloat(price) * 100);

        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_bGcjcsPmBRmPPt");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name",getResources().getString(R.string.app_name));
            jsonObject.put("amount",amount);
            jsonObject.put("currency","INR");
            jsonObject.put("description","Payment for medicines.");
            jsonObject.put("send_sms_hash", true);


            JSONObject prefill = new JSONObject();
            prefill.put("email",email);
            prefill.put("contact", phone);

            jsonObject.put("prefill",prefill);

            checkout.open(CartActivity.this,jsonObject);

        } catch (JSONException e) {
            progressDialog.dismiss();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        new Handler().postDelayed(progressDialog::dismiss,4000);

    }

    @Override
    public void onPaymentSuccess(String s) {
        Toasty.success(getApplicationContext(),"Payment success",Toasty.LENGTH_SHORT).show();
        saveOrder(strAddress);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Error: "+s, Toast.LENGTH_SHORT).show();
    }
}