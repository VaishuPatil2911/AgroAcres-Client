package com.example.foodapp.CartSystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.Model.Address;
import com.example.foodapp.Model.UserModel;
import com.example.foodapp.databinding.ActivityAddressBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

public class AddressActivity extends AppCompatActivity {

    ActivityAddressBinding binding;
    DatabaseReference reference;
    FirebaseUser user;

    String purpose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        purpose = getIntent().getStringExtra("purpose");

        reference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        getUserData();

        if (purpose.equals("add")){
            binding.btnSave.setText("Save");
            binding.btnSave.setOnClickListener(v -> {
                addAddress("Adding...");


            });


        }else {
            binding.btnSave.setText("Update");

            getAddress();

            binding.btnSave.setOnClickListener(v ->
                    addAddress("Updating..."));
        }

    }
    private void getAddress() {
        reference.child("Address").child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){

                            Address address = snapshot.getValue(Address.class);
                            assert address !=null;

                            binding.inputHouse.setText(address.getHouseNo());
                            binding.inputCity.setText(address.getCity());
                            binding.inputArea.setText(address.getArea());
                            binding.inputLandMark.setText(address.getLandmark());
                            binding.inputPinCode.setText(address.getPinCode());
                        }else {
                            Toast.makeText(AddressActivity.this, "Error data", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                        Toast.makeText(AddressActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserData(){
        reference.child("Users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel model = snapshot.getValue(UserModel.class);
                    assert model !=null;

                    binding.inputPhone.setText(model.getPhone());
                    String name = model.getUsername();

                    binding.inputFirstName.setText(name);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddressActivity.this, "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAddress(String message){
        String fName = binding.inputFirstName.getText().toString();
        String pNumber = binding.inputPhone.getText().toString();
        String area = binding.inputArea.getText().toString();
        String houseNo = binding.inputHouse.getText().toString();
        String landmark = binding.inputLandMark.getText().toString();
        String pinCode  = binding.inputPinCode.getText().toString();
        String city = binding.inputCity.getText().toString();

        if (fName.isEmpty() || pNumber.isEmpty() | area.isEmpty() || houseNo.isEmpty() ||
                landmark.isEmpty() || pinCode.isEmpty() || city.isEmpty()){
            Toast.makeText(AddressActivity.this, "This fields can't be empty", Toast.LENGTH_SHORT).show();
        }else {
            ProgressDialog progressDialog = new ProgressDialog(AddressActivity.this);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Address address = new Address();
            address.setUserId(user.getUid());
            address.setArea(area);
            address.setCity(city);
            address.setLandmark(landmark);
            address.setCountry("India");
            address.setPinCode(pinCode);
            address.setHouseNo(houseNo);

            reference.child("Address").child(user.getUid()).setValue(address)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()){
                            progressDialog.dismiss();
                            onBackPressed();
                            Toast.makeText(AddressActivity.this, "Address added!", Toast.LENGTH_SHORT).show();
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(AddressActivity.this, "Failed: "+
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

//            EventBus.getDefault().postSticky(new Constants.AddressClass());

        }
    }
}