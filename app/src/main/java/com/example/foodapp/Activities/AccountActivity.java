package com.example.foodapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.Authentication.LoginActivity;
import com.example.foodapp.Model.UserModel;
import com.example.foodapp.PreferenceManager;
import com.example.foodapp.databinding.ActivityAccountBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class AccountActivity extends AppCompatActivity {

    ActivityAccountBinding binding;
    DatabaseReference reference;
    FirebaseUser user;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        reference = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        preferenceManager = new PreferenceManager(this);

        setSupportActionBar(binding.toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.toolbar.setNavigationOnClickListener(v -> finish());


        binding.myOrdersLyt.setOnClickListener(view ->
                startActivity(new Intent(AccountActivity.this,OrdersActivity.class)));

        binding.editLyt.setOnClickListener(view ->
                startActivity(new Intent(AccountActivity.this,EditProfileActivity.class)));

        binding.logoutLyt.setOnClickListener(view -> {
            AuthUI.getInstance().signOut(getApplicationContext()).addOnSuccessListener(unused -> {
                Intent intent = new Intent(AccountActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
                preferenceManager.clear();
            }).addOnFailureListener(e ->
                    Toast.makeText(AccountActivity.this, "Failed: "+e.getMessage(), Toast.LENGTH_SHORT).show());
        });


        binding.aboutLyt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                builder.setTitle("About app");
                builder.setCancelable(false);
                builder.setMessage("This is s simple ecommerce app");
                builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        getUseDetails();


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
                        binding.fullName.setText(model.getUsername());
                        binding.email.setText(model.getEmail());
                        binding.phone.setText(model.getPhone());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}