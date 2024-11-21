//Kushwanth23
package com.example.foodapp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.Model.UserModel;
import com.example.foodapp.PreferenceManager;
import com.example.foodapp.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Random;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    ActivityRegisterBinding binding;
    DatabaseReference reference;
    FirebaseAuth auth;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        init();
        initAction();




    }

    private void init(){
        preferenceManager = new PreferenceManager(this);

        auth = FirebaseAuth.getInstance();
        reference = FirebaseDatabase.getInstance().getReference().child("Users");


    }

    private void initAction(){

        binding.btnSignUp.setOnClickListener(v -> {
            String username = binding.inputUsername.getText().toString();
            String email = binding.inputEmail.getText().toString();
            String phone = binding.inputPhone.getText().toString();
            String password = binding.inputPassword.getText().toString();

            if (username.isEmpty() || email.isEmpty()  || password.isEmpty() ||
                    phone.isEmpty()){
                Toasty.info(getApplicationContext(),"",Toasty.LENGTH_SHORT).show();
            }else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toasty.info(getApplicationContext(),"Enter valid email address",Toasty.LENGTH_SHORT).show();
            }else if (password.length() < 6){
                Toasty.info(getApplicationContext(),"Password must be >6 characters",Toasty.LENGTH_SHORT).show();

            }else {
                binding.btnSignUp.setVisibility(View.INVISIBLE);
                binding.progressBar.setVisibility(View.VISIBLE);

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){

                        FirebaseUser user = auth.getCurrentUser();

                        if (user !=null){
                            UserModel model = new UserModel();
                            model.setUsername(username);
                            model.setEmail(email);
                            model.setPhone(phone);
                            model.setUid(user.getUid());
                            model.setImage("");

                            reference.child(user.getUid())
                                    .setValue(model)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()){
                                            binding.progressBar.setVisibility(View.INVISIBLE);
                                            binding.btnSignUp.setVisibility(View.GONE);

                                            Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            preferenceManager.putBoolean("loggedIn",false);
                                            preferenceManager.putString("name",username);
                                            preferenceManager.putString("email",email);
                                            preferenceManager.putString("phone",phone);
                                            finish();

                                        }else {

                                            binding.progressBar.setVisibility(View.INVISIBLE);
                                            binding.btnSignUp.setVisibility(View.VISIBLE);
                                            Toasty.error(getApplicationContext(),"Failed: "+ task1.getException().getMessage(),
                                                    Toasty.LENGTH_SHORT).show();


                                        }
                                    });
                        }


                    }else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignUp.setVisibility(View.VISIBLE);
                        Toasty.error(getApplicationContext(),"Failed: "+
                                        task.getException().getMessage(),Toasty.LENGTH_SHORT)
                                .show();

                    }
                });





            }

        });

        binding.txtSignIn.setOnClickListener(v -> onBackPressed());






}



}