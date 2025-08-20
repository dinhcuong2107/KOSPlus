package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
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
import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogCreateProductBinding;
import com.example.kosplus.databinding.CustomDialogUploadBinding;
import com.example.kosplus.model.Products;
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

public class ProductsManageVM extends AndroidViewModel {
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    public ObservableField<String> imageUrl = new ObservableField<>();
    private CustomDialogCreateProductBinding binding;
    public ProductsManageVM(@NonNull Application application, @NonNull ActivityResultRegistry registry) {
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
                Log.d("MainViewModel", "Cropped Image URI: " + croppedUri);

                // upload file
                uploadfile(croppedUri);
            } else {
                Exception error = result.getError();
                Log.e("MainViewModel", "Crop Error: ", error);
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
            CustomDialogUploadBinding customDialogUploadBinding = CustomDialogUploadBinding.inflate(LayoutInflater.from(this.getApplication()));
            dialog.setContentView(customDialogUploadBinding.getRoot());

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
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
                            imageUrl.set(downloadUrl.toString());
                            Picasso.get().load(imageUrl.get()).into(binding.imageView);
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context.getValue(), "Failed", LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0*snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    int speed = (int) progress;
                    customDialogUploadBinding.textPercentage.setText(speed + " %" );
                    customDialogUploadBinding.progessPercentage.setProgress((int) speed);
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
        cropImageOptions.aspectRatioX = 1;
        cropImageOptions.aspectRatioY = 1;
        cropImageOptions.fixAspectRatio = true; // Khóa tỷ lệ

        CropImageContractOptions options = new CropImageContractOptions(sourceUri, cropImageOptions);
        cropImageLauncher.launch(options);
    }
    public void onCreateProduct (View view) {
        imageUrl = new ObservableField<>();
        ObservableField<String> type = new ObservableField<>();

        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        binding = CustomDialogCreateProductBinding.inflate(LayoutInflater.from(view.getContext()));
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

        binding.food.setOnClickListener(v -> type.set("Food"));
        binding.drink.setOnClickListener(v -> type.set("Drink"));

        binding.done.setOnClickListener(v -> {

            if (TextUtils.isEmpty(imageUrl.get())) {
                Toast.makeText(view.getContext(), "Cập nhật ảnh mẫu sản phẩm", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.name.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật tên sản phẩm", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.price.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật giá sản phẩm", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(type.get())) {
                Toast.makeText(view.getContext(), "Cập nhật loại sản phẩm", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.description.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật mô tả chi tiết", LENGTH_LONG).show();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus").child("Products");
                String UID = databaseReference.push().getKey();

                Products product = new Products(UID, imageUrl.get(),binding.name.getText().toString(),binding.description.getText().toString(), "", type.get(),"",Integer.parseInt(binding.price.getText().toString()), true);
                    databaseReference.child(UID).setValue(product, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {

                        } else {
                            Toast.makeText(view.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
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
            imageView.setImageResource(R.drawable.ic_launcher_background);
        } else {
            Picasso.get().load(imgUrl).into(imageView);
        }
    }
}