package com.mteam.eabaya;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ShopOwnerAddProductsFragment extends Fragment {
    private RecyclerView mUploadList;

    private List<String> fileNameList;
    private List<String> fileDoneList;
    private static final int RESULT_LOAD_IMAGE = 1;



    String[] permissions = {android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA};
    private final static int PERMISSION_ALL = 1;



    private ImageView mSelectBtn;
    private Button save;
    private int totalItemsSelected;
    private ImageButton openCamera;
    private ImageButton openGallery;

    // get Image Resource
    private static final int FROM_CAMERA = 1;
    private static final int SELECT_IMAGE = 2;
    private ImageView selectionImage;
    private Button accept_btn;
    private Button cancel_btn;
    private LinearLayout ll;
    private Uri imagePath;
    private ArrayList<String> fabricImagesArray;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private ArrayList<String> fabricDownloadImagePaths;
    private EditText productName;
    private EditText productDes;
    private EditText productPrice;

    public ShopOwnerAddProductsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shop_owner_add_products, container, false);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = FirebaseDatabase.getInstance().getReference("product");
        mSelectBtn = view.findViewById(R.id.imageSelection_iv);
        productName=view.findViewById(R.id.productName_ed);
        productDes=view.findViewById(R.id.productDesc_ed);
        productPrice=view.findViewById(R.id.productPrice_ed);
        ll = view.findViewById(R.id.imagesSelected_ll);
        save = view.findViewById(R.id.saveBtn);
        fabricImagesArray = new ArrayList<>();
        fabricDownloadImagePaths=new ArrayList<>();
        mSelectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!hasPermissions(getActivity(), permissions)) {
                    ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSION_ALL);
                    Toast.makeText(getActivity(), "Some Permissions Denied", Toast.LENGTH_LONG).show();
                }
                else {

                    getImageToUpload();
                }
            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage("Saving Information...!");
                dialog.show();

                for (int i = 0; i < fabricImagesArray.size(); i++) {
                    StorageReference fabricImage = mStorageRef.child("product/" + myRef.push().getKey()); //fabricImagesColorArray.get(i)
                    Uri myUri = Uri.parse(fabricImagesArray.get(i));
                    final int finalI = i;
                    fabricImage.putFile(myUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                 Task<Uri> downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl();
                                downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                           String photoLink = uri.toString();
                                            fabricDownloadImagePaths.add(photoLink);
//                                            photoLink = uri.toString();


                                        }
                                    });






                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), exception.toString(), Toast.LENGTH_SHORT).show();
                                }
                            });


                }
             for(int j=0;j<fabricDownloadImagePaths.size();j++)
                    addProductInformation(fabricDownloadImagePaths);
                    dialog.dismiss();
                    mSelectBtn.setImageResource(android.R.drawable.ic_menu_camera);
                    productName.setText("");
                    productDes.setText("");
                    productPrice.setText("");
                    fabricImagesArray.clear();
                    fabricDownloadImagePaths.clear();
                    ll.removeAllViews();
                    selectionImage.setImageResource(0);
                    dialog.dismiss();
                }

        });
        return view;
    }

    public void getImageToUpload() {
        final Dialog selectImageDialog = new Dialog(getActivity());
        selectImageDialog.setContentView(R.layout.activity_product_image_dialog);
        selectImageDialog.show();

        // To Select Image from Camera
        openCamera = selectImageDialog.findViewById(R.id.openCamera);

        openCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera, FROM_CAMERA);
                selectImageDialog.dismiss();

            }
        });
        // TO select image fromm Gallery
        openGallery = selectImageDialog.findViewById(R.id.openGallary);

        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), SELECT_IMAGE);
                selectImageDialog.dismiss();
            }
        });


            }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent SelectedImage) {

            super.onActivityResult(requestCode, resultCode, SelectedImage);



            if (requestCode == FROM_CAMERA && resultCode == RESULT_OK) {

                final Bitmap bitmap = (Bitmap) SelectedImage.getExtras().get("data");


                mSelectBtn.setImageBitmap(bitmap);
                final Dialog dailog = new Dialog(getActivity());
                dailog.setCancelable(false);
                dailog.show();
                dailog.setContentView(R.layout.product_image_dailog);
                selectionImage = dailog.findViewById(R.id.image_selcetion_dailog);

                accept_btn = dailog.findViewById(R.id.acceptImage);
                cancel_btn = dailog.findViewById(R.id.cancelImage_btn);
                selectionImage.setImageBitmap(bitmap);
                imagePath = SelectedImage.getData();

                accept_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //saveImageToSdcard(bitmap);
                        if (selectionImage.getParent() != null) {
                            ((ViewGroup) selectionImage.getParent()).removeView(selectionImage); // <- fix
                            //((ViewGroup) colorFabric.getParent()).removeView(colorFabric); // <- fix

                            dailog.dismiss();
                        }

                        ll.addView(selectionImage, 300, 300);


                        selectionImage.setScaleType(ImageView.ScaleType.FIT_XY);
                        selectionImage.setPadding(20, 0, 0, 0);


                        dailog.dismiss();

                        fabricImagesArray.add(String.valueOf(imagePath));
                    }
                });
                cancel_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dailog.dismiss();
                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "You did not Captured any Image", Toast.LENGTH_SHORT).show();
            }

            // check if request code = constant that we sent, check result is OK and check if intent is having any data
            else if (requestCode == SELECT_IMAGE && resultCode == RESULT_OK && SelectedImage != null) {
                try {
                    // creating bitmap to get Image from gallery ( parameters are activity CONTEXT, INTENT )
                    final Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), SelectedImage.getData());
                    // setting IMAGE IN THE IMAGE VIEW
                    final Dialog dailog = new Dialog(getActivity());
                    dailog.setCancelable(false);
                    dailog.show();
                    dailog.setContentView(R.layout.product_image_dailog);
                    selectionImage = dailog.findViewById(R.id.image_selcetion_dailog);

                    accept_btn = dailog.findViewById(R.id.acceptImage);
                    imagePath = SelectedImage.getData();
                    selectionImage.setImageBitmap(bitmap);
                    cancel_btn = dailog.findViewById(R.id.cancelImage_btn);

                    accept_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
//                        saveImageToSdcard(bitmap);
                            if (selectionImage.getParent() != null) {
                                ((ViewGroup) selectionImage.getParent()).removeView(selectionImage); // <- fix
                                dailog.dismiss();
                            }

                            ll.addView(selectionImage, 300, 300);

                            selectionImage.setScaleType(ImageView.ScaleType.FIT_XY);
                            selectionImage.setPadding(20, 0, 0, 0);
                            dailog.dismiss();

                            fabricImagesArray.add(String.valueOf(imagePath));


                        }
                    });
                    cancel_btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dailog.dismiss();
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getActivity(), "You did not Chose any Image", Toast.LENGTH_SHORT).show();
            }
        }
    public boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
    public void addProductInformation(ArrayList<String> imageURL) {

        String key = myRef.push().getKey();
        Product product = new Product();
        product.setProductId(key);
        product.setCompanyId("78787c8");
        product.setProductImage(imageURL);
        product.setProductName(productName.getText().toString().trim().toLowerCase());
        product.setProductDescription(productDes.getText().toString().trim().toLowerCase());
        product.setProductPrice(productDes.getText().toString());
        myRef.child(key).setValue(product);

    }

    }






