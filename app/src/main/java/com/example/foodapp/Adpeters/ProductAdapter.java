package com.example.foodapp.Adpeters;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Activities.ProductDetailsActivity;
import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.Model.Products;
import com.example.foodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {


    ArrayList<Products> pList;
    CartModel model;


    private ArrayList<String> idList = new ArrayList<>();

    public ProductAdapter(ArrayList<Products> list) {
        this.pList = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_vertical, parent, false));

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Products products = pList.get(position);
        assert products !=null;



        holder.productName.setText(products.getName());
        holder.price.setText("₹ "+products.getPrice());

        try {
            Picasso.get().load(products.getImage()).placeholder(R.drawable.fruits)
                    .into(holder.productImage);
        }catch (Exception e){
            e.printStackTrace();
        }



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailsActivity.class);
            intent.putExtra("id",products.getId());
            intent.putExtra("category",products.getCategory());

            v.getContext().startActivity(intent);
        });


        holder.btnAddToBag.setOnClickListener(v -> addToCart(products,holder));


        checkCartStatus(products,holder);
        getCartInformation(holder,position,products.getId());








    }
    private void addToCart(Products products, ViewHolder holder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cart")
                .child(user.getUid());

        CartModel model = new CartModel();
        model.setKey(products.getId());
        model.setQuantity(1);
        model.setName(products.getName());
        model.setImage(products.getImage());
        model.setPrice(products.getPrice());
        model.setTotalPrice(products.getPrice());
        model.setPublisher(products.getPublisher());



        reference.child(products.getId()).setValue(model);
        holder.btnAddToBag.setVisibility(View.INVISIBLE);
        holder.cartLyt.setVisibility(View.VISIBLE);
        holder.quantity.setText("1");

        //
        EventBus.getDefault().postSticky(new MyUpdateCartEvent());




    }

    private void checkCartStatus(Products products,ViewHolder holder){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cart")
                .child(user.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    idList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                        model = dataSnapshot.getValue(CartModel.class);
                        if (model !=null){
                            idList.add(model.getKey());
                        }
                    }

                }
//                EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                if (idList.contains(products.getId())){
                    holder.cartLyt.setVisibility(View.VISIBLE);
                    holder.btnAddToBag.setVisibility(View.INVISIBLE);
                }else {
                    holder.cartLyt.setVisibility(View.INVISIBLE);
                    holder.btnAddToBag.setVisibility(View.VISIBLE);
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.btnAddToBag.getContext(), "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getCartInformation(ViewHolder holder,int position,String id){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Cart")
                .child(user.getUid());

        reference.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){

                    model = snapshot.getValue(CartModel.class);
                    assert model != null;

                    holder.quantity.setText(String.valueOf(model.getQuantity()));


                    holder.btnPlus.setOnClickListener(v -> {
                        if (model.getQuantity() < 6){


                            model.setQuantity(model.getQuantity() + 1);
                            model.setTotalPrice(model.getQuantity() * model.getPrice());
//                                model.setRealPrice(model.getQuantity() * model.getRealPrice());


                            holder.quantity.setText(new StringBuilder().append(model.getQuantity()));
                            updateFirebase(model,holder);
                        }else {
                            Toast.makeText(holder.quantity.getContext(), "Only six can be added to cart!", Toast.LENGTH_SHORT).show();
                        }

                    });

                    holder.btnMinus.setOnClickListener(v -> {
                        if (model.getQuantity() > 1){
                            model.setQuantity(model.getQuantity() - 1);
                            model.setTotalPrice(model.getQuantity() * model.getPrice());
//                                model.setRealPrice((int) (model.getTotalPrice() - model.getRealPrice()));



                            holder.quantity.setText(new StringBuilder().append(model.getQuantity()));
                            updateFirebase(model,holder);


                        }else {
                            deleteDialogue(position,model,holder);
                        }
                    });





                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(holder.quantity.getContext(), "Error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateFirebase(CartModel model,ViewHolder holder) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        FirebaseDatabase.getInstance().getReference()
                .child("Cart")
                .child(user.getUid())
                .child(model.getKey())
                .setValue(model)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
                    }else {
                        Toast.makeText(holder.quantity.getContext(), "Error updating...", Toast.LENGTH_SHORT).show();
                    }
                });
        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
    }

    private void deleteDialogue(int position,CartModel model,ViewHolder holder){
        AlertDialog.Builder builder = new AlertDialog.Builder(holder.quantity.getContext());
        builder.setTitle("Delete item");
        builder.setMessage("Are you sure want to delete cart item?");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteFromFirebase(model,position);
                dialog.dismiss();
                holder.cartLyt.setVisibility(View.INVISIBLE);
                holder.btnAddToBag.setVisibility(View.VISIBLE);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void deleteFromFirebase(CartModel model, int position) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        FirebaseDatabase.getInstance().getReference()
                .child("Cart")
                .child(user.getUid())
                .child(model.getKey())
                .removeValue();
        notifyItemChanged(position);

        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
    }




    @Override
    public int getItemCount() {
        if (pList!= null)
            return pList.size();
        else
            return 0;
    }




    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView price, txtBy,productName;
        ImageView productImage,btnPlus,btnMinus;
        TextView btnAddToBag,quantity;
        LinearLayout cartLyt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtBy = itemView.findViewById(R.id.txtBy);
            productName = itemView.findViewById(R.id.productName);
            productImage= itemView.findViewById(R.id.productImage);
            btnAddToBag= itemView.findViewById(R.id.btnAddToBag);
            cartLyt= itemView.findViewById(R.id.cartLyt);
            btnPlus= itemView.findViewById(R.id.btnPlus);
            btnMinus= itemView.findViewById(R.id.btnMinus);
            quantity = itemView.findViewById(R.id.itemCount);
            price = itemView.findViewById(R.id.itemPrice);


        }
    }
}
