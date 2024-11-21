package com.example.foodapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.foodapp.Model.UserModel;
import com.example.foodapp.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Objects;

public class EditProfileActivity extends AppCompatActivity {

    ActivityEditProfileBinding binding;
    DatabaseReference reference;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference().child("Users");
        user = FirebaseAuth.getInstance().getCurrentUser();

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        binding.btnUpdate.setOnClickListener(v -> {
            String username = binding.inputUsername.getText().toString();
            String email = binding.inputEmail.getText().toString();
            String phone = binding.inputPhone.getText().toString();

            if (username.isEmpty() | email.isEmpty() | phone.isEmpty()){
                Toast.makeText(EditProfileActivity.this, "All fields required!", Toast.LENGTH_SHORT).show();
            }else{
                updateData(username,phone);
            }
        });

        getUseDetails();
    }

    private void updateData(String username, String phone) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        HashMap<String,Object> map = new HashMap<>();
        map.put("username",username);
        map.put("phone",phone);


        reference.child(user.getUid()).updateChildren(map).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Profile updated!", Toast.LENGTH_SHORT).show();

            }
        });






    }

    private void getUseDetails(){
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    UserModel model = snapshot.getValue(UserModel.class);
                    if (model !=null){
                        binding.inputEmail.setText(model.getEmail());
                        binding.inputUsername.setText(model.getUsername());
                        binding.inputPhone.setText(model.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}