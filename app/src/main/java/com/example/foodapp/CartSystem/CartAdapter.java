package com.example.foodapp.CartSystem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodapp.Listeners.MyUpdateCartEvent;
import com.example.foodapp.Model.CartModel;
import com.example.foodapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    private ArrayList<CartModel> list;
    private String from;

    public CartAdapter(Context context, ArrayList<CartModel> list, String from) {
        this.context = context;
        this.list = list;
        this.from = from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (from.equals("cart")){
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false));

        }
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_order_details, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartModel model = list.get(position);
        assert model !=null;

        holder.price.setText(new StringBuilder("₹").append(model.getPrice()));
        holder.productName.setText(model.getName());
        try {
            Picasso.get().load(model.getImage()).placeholder(R.drawable.fruits)
                    .into(holder.productImage);
        }catch (Exception e){
            e.printStackTrace();
        }


        if (from.equals("cart")){
            holder.quantity.setText(new StringBuilder().append(model.getQuantity()));
            holder.btnMinus.setOnClickListener(v -> {
                if (model.getQuantity() > 1){
                    model.setQuantity(model.getQuantity() - 1);
                    model.setTotalPrice(model.getQuantity() * model.getPrice());
//                model.setRealPrice((int) (model.getTotalPrice() - model.getRealPrice()));


                    holder.quantity.setText(new StringBuilder().append(model.getQuantity()));
                    updateFirebase(model);


                }

                else {
                    deleteDialogue(position,model);
                }
            });

            holder.btnPlus.setOnClickListener(v -> {
                if (model.getQuantity() < 6){
                    model.setQuantity(model.getQuantity() + 1);
                    model.setTotalPrice(model.getQuantity() * model.getPrice());
//                model.setRealPrice(model.getQuantity() * model.getRealPrice());

                    holder.quantity.setText(new StringBuilder().append(model.getQuantity()));
                    updateFirebase(model);
                }else {
                    Toast.makeText(context, "Only six can be served!", Toast.LENGTH_SHORT).show();
                }

            });

            holder.imgClose.setOnClickListener(v -> deleteDialogue(position,model));
        }else {

            holder.quantity.setText("QTY: " + model.getQuantity());

        }

    }

    private void deleteFromFirebase(CartModel model) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        FirebaseDatabase.getInstance().getReference()
                .child("Cart")
                .child(user.getUid())
                .child(model.getKey())
                .removeValue();
        EventBus.getDefault().postSticky(new MyUpdateCartEvent());
    }


    private void updateFirebase(CartModel model) {
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
                        Toast.makeText(context, "Error updating...", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void deleteDialogue(int position,CartModel model){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Delete item");
        builder.setMessage("Are you sure want to delete cart item?");
        builder.setCancelable(false);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                notifyItemRemoved(position);
                deleteFromFirebase(model);

                dialog.dismiss();
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

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView productName,quantity,price;
        ImageView imgClose,productImage,btnPlus,btnMinus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.productName);
            quantity = itemView.findViewById(R.id.itemCount);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            price = itemView.findViewById(R.id.price);
            imgClose = itemView.findViewById(R.id.imageClose);
            productImage = itemView.findViewById(R.id.productImage);



        }
    }
}
