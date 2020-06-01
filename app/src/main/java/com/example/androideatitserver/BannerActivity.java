package com.example.androideatitserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.androideatitserver.Common.Common;
import com.example.androideatitserver.Model.Banner;
import com.example.androideatitserver.Model.Food;
import com.example.androideatitserver.ViewHolder.BannerViewHolder;
import com.example.androideatitserver.ViewHolder.FoodViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import info.hoang8f.widget.FButton;

public class BannerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FloatingActionButton fab;

    RelativeLayout rootLayout;

    //Firebased
    FirebaseDatabase db;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    //Add New Banner
    MaterialEditText edtName, edtFoodId;
    FButton btnUpload, btnSelect;

    Banner newBanner;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
        banners = db.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init View
        recyclerView = (RecyclerView)findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout)findViewById(R.id.rootBannerLayout);

        fab = (FloatingActionButton) findViewById(R.id.banner_fab_food);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBanner();
            }
        });

        loadListBanner();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void loadListBanner() {
        FirebaseRecyclerOptions<Banner> allBanner = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(banners, Banner.class).build();

        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(allBanner) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.banner_name.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.banner_image);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.banner_layout, parent, false);
                return new BannerViewHolder(itemView);
            }
        };

        adapter.startListening();

        //Set Adapter
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);


    }

    private void showAddBanner() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add new Banner");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_food_banner = inflater.inflate(R.layout.add_new_banner, null);

        edtFoodId = add_food_banner.findViewById(R.id.edtBannerFoodId);
        edtName = add_food_banner.findViewById(R.id.edtBannerFoodName);

        btnSelect = add_food_banner.findViewById(R.id.btnBannerFoodSelect);
        btnUpload = add_food_banner.findViewById(R.id.btnBannerFoodUpload);

        //Set Event for Select Picture from Home
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Set event for Upload picture after Select
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPicture();
            }
        });


        alertDialog.setView(add_food_banner);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Set Button for Dialog
        alertDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(newBanner!=null)
                    banners.push()
                        .setValue(newBanner);

                loadListBanner();
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                newBanner = null;

                loadListBanner();
            }
        });

        alertDialog.show();



    }

    private void uploadPicture() {
        if(filePath != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/foods/"+imageName);
            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set Value for newCategory if Image Uploaded and we can get download LINK
                                    newBanner = new Banner();
                                    newBanner.setName(edtName.getText().toString());
                                    newBanner.setId(edtFoodId.getText().toString());
                                    newBanner.setImage(uri.toString());
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            //Don't Worry about this Erro
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " +progress+"%");

                        }
                    })      ;

        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null)
        {
            filePath = data.getData();
            btnSelect.setText("Image Selected !");

        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {


        if(item.getTitle().equals(Common.UPDATE))
        {
            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(Common.DELETE))
        {
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);

    }

    private void deleteBanner(String key) {
        banners.child(key).removeValue();
        Toast.makeText(this, "Banner Deleted!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Edit Banner");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View edit_banner = inflater.inflate(R.layout.add_new_banner, null);

        edtName = edit_banner.findViewById(R.id.edtBannerFoodName);
        edtFoodId = edit_banner.findViewById(R.id.edtBannerFoodId);

        //Set Default Value for View
        edtName.setText(item.getName());
        edtFoodId.setText(item.getId());


        btnSelect = edit_banner.findViewById(R.id.btnBannerFoodSelect);
        btnUpload = edit_banner.findViewById(R.id.btnBannerFoodUpload);

        //Event for Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage(); //Let user select Image from Gallery and Save URI of this image
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(edit_banner);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //SET Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //Here, Edit Food

                //Update Information
                item.setName(edtName.getText().toString());
                item.setId(edtFoodId.getText().toString());

                //Make Update
                Map<String,Object> update = new HashMap<>();
                update.put("id", item.getId());
                update.put("name", item.getName());
                update.put("image", item.getImage());



                banners.child(key)
                        .updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Snackbar.make(rootLayout, "Banner " + item.getName() + " was edited", Snackbar.LENGTH_SHORT)
                                        .show();
                                loadListBanner();
                            }
                        });

                loadListBanner();

            }


        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                loadListBanner();
            }
        });

        alertDialog.show();
    }

    private void changeImage(final Banner item) {
        if(filePath != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading...");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/foods/"+imageName);
            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //Set Value for newCategory if Image Uploaded and we can get download LINK
                                    item.setImage(uri.toString());

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })

                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            //Don't Worry about this Erro
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded " +progress+"%");

                        }
                    })      ;

        }
    }
}
