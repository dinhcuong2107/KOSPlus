package com.example.kosplus.features;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.canhub.cropper.CropImageContract;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import com.example.kosplus.MainActivity;
import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogCreateBannerBinding;
import com.example.kosplus.databinding.CustomDialogUploadBinding;
import com.example.kosplus.model.Banners;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class BannersManageVM extends AndroidViewModel {
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    public ObservableField<String> imgUrl = new ObservableField<>();

    private CustomDialogCreateBannerBinding binding;
    public BannersManageVM(@NonNull Application application, @NonNull ActivityResultRegistry registry) {
        super(application);
        pickImageLauncher = registry.register("select_image", new ActivityResultContracts.GetContent(),
                result -> {
                    if (result != null) {
                        startCropActivity(result);
                    }
                });

        // Đăng ký launcher cắt ảnh
        cropImageLauncher = registry.register("crop_image", new CropImageContract(), result -> {
            if (result.isSuccessful()) {
                Uri croppedUri = result.getUriContent();
                Log.d("KOS Plus", "BannerManageViewModel Cropped Image URI: " + croppedUri);

                // upload file
                uploadfile(croppedUri);
            } else {
                Exception error = result.getError();
                Log.e("KOS Plus", "BannerManageViewModel Crop Error: ", error);
            }
        });
    }

    private void uploadfile(Uri uri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        final StorageReference reference = storageReference.child("image/"+ UUID.randomUUID().toString());
        if (uri != null){
            Dialog dialog = new Dialog(context.getValue());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            CustomDialogUploadBinding uploadBinding = CustomDialogUploadBinding.inflate(LayoutInflater.from(this.getApplication()));
            dialog.setContentView(uploadBinding.getRoot());

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams win = window.getAttributes();
            win.gravity = Gravity.CENTER;
            window.setAttributes(win);
            dialog.setCancelable(false);

            dialog.show();

            reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    dialog.dismiss();
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUrl = uri;
                            imgUrl.set(downloadUrl.toString());
                            Picasso.get().load(downloadUrl.toString()).into(binding.imageView);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context.getValue(), "Failed", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    int speed = (int) progress;
                    uploadBinding.textPercentage.setText(speed + " %" );
                    uploadBinding.progessPercentage.setProgress((int) speed);
                }
            });
        }
    }

    public void pickImage(View view) {
        AppCompatActivity activity = (AppCompatActivity) view.getContext();
        context.setValue(activity);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, 1);
            }else {
                pickImageLauncher.launch("image/*");
            }
        } else {
            if (ActivityCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            } else {
                pickImageLauncher.launch("image/*");
            }
        }
    }

    private void startCropActivity(Uri sourceUri) {

        CropImageOptions cropImageOptions = new CropImageOptions();
        cropImageOptions.guidelines = CropImageView.Guidelines.ON;
        cropImageOptions.aspectRatioX = 2;
        cropImageOptions.aspectRatioY = 1;
        cropImageOptions.fixAspectRatio = true; // Khóa tỷ lệ

        CropImageContractOptions options = new CropImageContractOptions(sourceUri, cropImageOptions);
        cropImageLauncher.launch(options);
    }

    private void complete(View view) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus").child("Banners");
        String UID = databaseReference.push().getKey();

        Banners banners= new Banners(UID, imgUrl.get(), binding.title.getText().toString(), binding.description.getText().toString(), binding.linkUrl.getText().toString(),0, true);

        databaseReference.child(UID).setValue(banners, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    view.getContext().startActivity(intent);
                } else {
                    Toast.makeText(view.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onDialogCreateBanner (View view) {

        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        binding = CustomDialogCreateBannerBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        binding.imageView.setOnClickListener(v -> {
            pickImage(v);
        });

        binding.done.setOnClickListener(v -> {

            if (TextUtils.isEmpty(imgUrl.get())) {
                Toast.makeText(view.getContext(), "Cập nhật ảnh", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.title.getText().toString())) {
                Toast.makeText(view.getContext(), "Thêm title", Toast.LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.description.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật description", Toast.LENGTH_LONG).show();
            } else {
                complete(view);
            }
        });

        binding.cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    @BindingAdapter({"android:src"})
    public static void setImageView(ImageView imageView, String imgUrl) {
        if (imgUrl == null) {
            imageView.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            Picasso.get().load(imgUrl).into(imageView);
        }
    }
}