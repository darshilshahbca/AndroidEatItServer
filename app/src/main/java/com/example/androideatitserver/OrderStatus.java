package com.example.androideatitserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Interface.ItemClickListener;
import com.example.androideatitserver.Model.Request;
import com.example.androideatitserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jaredrummler.materialspinner.MaterialSpinner;

public class OrderStatus extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    FirebaseDatabase db;
    DatabaseReference requests;

    MaterialSpinner spinner;


    private void deleteOrder(String key) {
        requests.child(key).removeValue();
//        adapter.notifyDataSetChanged();
        loadOrders();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showUpdateDialog(String key, final Request item) {
        final AlertDialog.Builder alertDialog =
                new AlertDialog.Builder(OrderStatus.this);
        alertDialog.setTitle("Update Order");
        alertDialog.setMessage("Please choose status");

        LayoutInflater inflater = this.getLayoutInflater();
        final View view = inflater.inflate(R.layout.order_update_layout, null);

        spinner = (MaterialSpinner)view.findViewById(R.id.statusSpinner);
        spinner.setItems("Placed", "On my Way", "Shipped");

        alertDialog.setView(view);

        final String localKey = key;
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                   dialog.dismiss();
                   item.setStatus(String.valueOf(spinner.getSelectedIndex()));

                   requests.child(localKey).setValue(item);
                   adapter.notifyDataSetChanged(); //Add to Update Item Size
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        //Firebase
        db = FirebaseDatabase.getInstance();
        requests = db.getReference("Requests");

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders();
    }

    private void loadOrders() {
        FirebaseRecyclerOptions<Request> options =
                new FirebaseRecyclerOptions.Builder<Request>()
                        .setQuery(requests, Request.class)
                        .build();


        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder viewHolder, final int position, @NonNull final Request model) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderStatus.setText(Common.convertCodeToStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                //New Event Button
                viewHolder.btnEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });

                viewHolder.btnRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });

                viewHolder.btnDetail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent orderDetail = new Intent(OrderStatus.this, OrderDetail.class);
                        Common.currentRequest = model;
                        orderDetail.putExtra("OrderId", adapter.getRef(position).getKey());
                        startActivity(orderDetail);
                    }
                });

            }


            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
                return new OrderViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }


}
