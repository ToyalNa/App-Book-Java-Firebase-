package com.example.bookapp.activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bookapp.MyApplication;
import com.example.bookapp.R;
import com.example.bookapp.databinding.ActivityEditProfileBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;

    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private Uri imgUri = null;
    private String name;

    private static final String TAG = "EDIT_PROFILE_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.please_wait);
        progressDialog.setCanceledOnTouchOutside(false); // don't dismiss while click outside of progress dialog

        firebaseAuth = FirebaseAuth.getInstance();
        loadUserInfo();

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.profileIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageAttachMenu();
            }
        });

        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private void validateData() {
        name = binding.edtName.getText().toString().trim();

        if (TextUtils.isEmpty(name)){
            Toast.makeText(this, R.string.name, Toast.LENGTH_SHORT).show();
        }
        else {
            if (imgUri == null){
                updateProfile("");
            }
            else {
                uploadImage();
            }
        }
    }

    private void updateProfile(String imageUrl) {
        Log.d(TAG, "updateProfile: Updating user profile");
        progressDialog.setMessage(getString(R.string.update_user_profile));
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", "" + name);
        if (imgUri != null){
            hashMap.put("profileImage", ""+ imageUrl);
        }

        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference("Users");
        dataRef.child(firebaseAuth.getUid())
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Profile updated ... ");
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, R.string.profile_updated, Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to update db due to" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, R.string.profile_updated_failed + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImage() {
        Log.d(TAG, "uploadImage: Uploading profile image ...");
        progressDialog.setMessage(getString(R.string.updating_images));
        progressDialog.show();

        String file = "ProfileImages/" + firebaseAuth.getUid();

        StorageReference storageRf = FirebaseStorage.getInstance().getReference(file);
        storageRf.putFile(imgUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "onSuccess: Profile image uploaded");
                        Log.d(TAG, "onSuccess: Getting url of uploaded image");
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String uploadedImageUrl = "" + uriTask.getResult();

                        Log.d(TAG, "onSuccess: Uploaded Image URL: " + uploadedImageUrl);
                        updateProfile(uploadedImageUrl);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Failed to upload image due to" + e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(EditProfileActivity.this, R.string.failed +e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showImageAttachMenu() {
        PopupMenu popupMenu = new PopupMenu(this,binding.profileIv);
        popupMenu.getMenu().add(Menu.NONE,0,0,R.string.camera);
        popupMenu.getMenu().add(Menu.NONE,1,1,R.string.gallery);

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                int which = menuItem.getItemId();
                if (which == 0){
                    pickImageCamera();
                }
                else if (which == 1){
                    pickImageGallery();
                }

                return false;
            }
        });

    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private void pickImageCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pick");
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        imgUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: Picked From Camera" + imgUri);
                        Intent data = result.getData();

                        binding.profileIv.setImageURI(imgUri);
                    }
                    else {
                        Toast.makeText(EditProfileActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Log.d(TAG, "onActivityResult: " + imgUri);
                        Intent data = result.getData();
                        imgUri = data.getData();
                        Log.d(TAG, "onActivityResult: Picked From Gallery " +imgUri );

                        binding.profileIv.setImageURI(imgUri);
                    }
                    else {
                        Toast.makeText(EditProfileActivity.this, R.string.cancelled, Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void loadUserInfo() {
        Log.d(TAG, "loadUserInfo: Loading user info of user " + firebaseAuth.getUid());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String email = "" + snapshot.child("email").getValue();
                        String name = "" + snapshot.child("name").getValue();
                        String profileImage = "" + snapshot.child("profileImage").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String uid = "" + snapshot.child("uid").getValue();
                        String userType = "" + snapshot.child("userType").getValue();

                        binding.edtName.setText(name);

                        Glide.with(EditProfileActivity.this)
                                .load(profileImage)
                                .placeholder(R.drawable.ic_person_white)
                                .into(binding.profileIv);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}