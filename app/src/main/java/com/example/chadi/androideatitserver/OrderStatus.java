package com.example.chadi.androideatitserver;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.chadi.androideatitserver.Common.Common;
import com.example.chadi.androideatitserver.Interface.ItemClickListener;
import com.example.chadi.androideatitserver.Model.Request;
import com.example.chadi.androideatitserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.jaredrummler.materialspinner.MaterialSpinnerAdapter;

import java.util.ArrayList;

public class OrderStatus extends AppCompatActivity  {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
        FirebaseDatabase db;
    DatabaseReference requests;
    FirebaseRecyclerAdapter<Request,OrderViewHolder>adapter;
    MaterialSpinner   spinner;

    CardView cardView;

    private static final String[] Status = {"Placed","on my way","shipped"};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        db=FirebaseDatabase.getInstance();
        requests=db.getReference("Requests");
        //init
        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
         spinner = (MaterialSpinner) findViewById(R.id.spinner);

        loadOrder();
    }

    private void loadOrder() {

        adapter =new FirebaseRecyclerAdapter<Request, OrderViewHolder>(
                Request.class,
                R.layout.order_layout,
                OrderViewHolder.class,
                requests

        ) {
            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, final Request model, int position) {
                viewHolder.txtOrderId.setText(adapter.getRef(position).getKey());
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderStatus.setText(Common.convertToCodeStatus(model.getStatus()));
                viewHolder.txtOrderAddress.setText(model.getAddress());
                viewHolder.txtOrderPhone.setText(model.getPhone());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent intentToTracking = new Intent(OrderStatus.this , TrackingOrder.class );
                        Common.currentRequest = model;
                        startActivity(intentToTracking);

                    }
                });

            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }


    @Override

    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(Common.UPDATE)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE)){
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }


    private void deleteOrder(String key) {
        requests.child(key).removeValue();
    }


    private void showUpdateDialog(String key, final Request item) {

        try {

            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OrderStatus.this);
            alertDialog.setTitle("Update Order");
            alertDialog.setMessage("Please choose a status");

            LayoutInflater inflater = this.getLayoutInflater();
            final View view = inflater.inflate(R.layout.update_order_layout, null);
            ArrayList<String> countryArrayList = new ArrayList<>();
            String country = new String("Placed");
            countryArrayList.add(country);
            String country1 = new String("On my Way");
            countryArrayList.add(country1);
            String country2 = new String("Shipped");
            countryArrayList.add(country2);

            MaterialSpinnerAdapter<String > countyMaterialSpinnerAdapter;
            countyMaterialSpinnerAdapter = new MaterialSpinnerAdapter<String>(this.getBaseContext(),countryArrayList);
            final MaterialSpinner countrySpinner = view.findViewById(R.id.spinner);
            countrySpinner.setAdapter(countyMaterialSpinnerAdapter);

            countrySpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                                                         @Override
                                                         public void onItemSelected(MaterialSpinner view, int position, long id, String country) {
                                                             Snackbar.make(view, "Clicked", Snackbar.LENGTH_LONG).show();
                                                         }
                                                     });




            alertDialog.setView(view);
            final String localKey = key;

            alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    item.setStatus(String.valueOf(countrySpinner.getSelectedIndex()));
                    requests.child(localKey).setValue(item);
                }
            });

            alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            alertDialog.show();

        }catch (Exception e){
            Toast.makeText(this, "Exception"+e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }




}
