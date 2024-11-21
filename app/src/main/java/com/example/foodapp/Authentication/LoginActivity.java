//Kushwanth23
package com.example.foodapp.Authentication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.foodapp.MainActivity;
import com.example.foodapp.PreferenceManager;
import com.example.foodapp.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {


    ActivityLoginBinding binding;

    FirebaseAuth auth;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        if (preferenceManager.getBoolean("loggedIn")){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }

        init();
        initAction();

    }

    private void init(){
        auth = FirebaseAuth.getInstance();

    }


    private void initAction(){


        binding.btnSignIn.setOnClickListener(v -> {
            String email = binding.inputEmail.getText().toString();
            String password = binding.inputPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()){
                Toast.makeText(LoginActivity.this, "This fields can't be empty", Toast.LENGTH_SHORT).show();

            }else {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnSignIn.setVisibility(View.GONE);

                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignIn.setVisibility(View.VISIBLE);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(intent);
                        preferenceManager.putBoolean("loggedIn",true);

                        finish();

                    }else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.btnSignIn.setVisibility(View.VISIBLE);
                        Toasty.error(getApplicationContext(),"Error: "+task.getException().getMessage(),
                                Toasty.LENGTH_SHORT).show();

                    }
                });
            }
        });


        binding.txtSignUp.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class).
                        addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)));




    }


}
