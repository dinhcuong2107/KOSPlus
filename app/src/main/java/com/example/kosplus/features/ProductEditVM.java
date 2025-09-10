package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.app.ProgressDialog;
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
import com.example.kosplus.databinding.CustomDialogUploadBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Products;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class ProductEditVM extends AndroidViewModel {
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    public ObservableField<Products> product = new ObservableField<>();
    public ObservableField<String> avatar = new ObservableField<>();
    public ObservableField<String> type = new ObservableField<>();
    public ObservableField<String> price = new ObservableField<>();

    public ProductEditVM(@NonNull Application application, @NonNull ActivityResultRegistry registry) {
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
            CustomDialogUploadBinding binding = CustomDialogUploadBinding.inflate(LayoutInflater.from(this.getApplication()));
            dialog.setContentView(binding.getRoot());

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
                            avatar.set(downloadUrl.toString());
//                            Picasso.get().load(fastfood.image).into(bindingdialog.image);
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
                    binding.textPercentage.setText(speed + " %" );
                    binding.progessPercentage.setProgress((int) speed);
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

    public void setProduct (String ID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(ID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Products products = snapshot.getValue(Products.class);
                    product.set(products);
                    avatar.set(products.imageUrl);
                    type.set(products.type);
                    price.set(String.valueOf(products.price));
                } else {
                    Toast.makeText(context.getValue(), "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void onProductUpdate(View view) {
        if (avatar.get() == null || avatar.get().isEmpty()) {
            avatar.set(product.get().imageUrl);
        }
        if (type.get() == null || type.get().isEmpty()) {
            type.set(product.get().type);
        }
        if(TextUtils.isEmpty(product.get().name)) {
            Toast.makeText(view.getContext(), "Vui lòng nhập tên sản phẩm", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(product.get().description)) {
            Toast.makeText(view.getContext(), "Vui lòng nhập mô tả sản phẩm", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(product.get().category)) {
            Toast.makeText(view.getContext(), "Vui lòng nhập danh mục sản phẩm", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(product.get().type)) {
            Toast.makeText(view.getContext(), "Vui lòng nhập loại sản phẩm", Toast.LENGTH_SHORT).show();
        } else if (Integer.parseInt(price.get()) <= 0) {
            Toast.makeText(view.getContext(), "Vui lòng nhập giá sản phẩm", Toast.LENGTH_SHORT).show();
        } else {
            completeUpdate(view);
        }
    }
    private void completeUpdate(View view) {
        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false); // Không cho bấm ra ngoài để tắt
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(product.get().id);
        Products productUpdate = new Products(product.get().id, avatar.get(), product.get().name, product.get().description, product.get().category, product.get().type, product.get().promotion, Integer.parseInt(price.get()), product.get().status);
        databaseReference.setValue(productUpdate, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    view.getContext().startActivity(intent);
                } else {
                    Toast.makeText(view.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onSetType(String string){
        type.set(string);
    }

    public void onDelete(View view){
        Utils.showVerificationDialog(view.getContext(), "Verification","Xác nhận xóa", "Bạn có muốn xóa sản phẩm này không?", () -> {
            // Xóa sản phẩm
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(product.get().id);
            databaseReference.removeValue();
        });

    }
    public void onLock(View view){
        Utils.showVerificationDialog(view.getContext(), "Verification","Xác nhận ngừng bán", "Bạn có muốn ngừng bán sản phẩm này không?", () -> {
            // Lấy reference đến user dựa vào userID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(product.get().id);
            // Cập nhật status thành false
            databaseReference.child("status").setValue(false)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(view.getContext(), "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    });
        });

    }

    public void onUnLock(View view){
        Utils.showVerificationDialog(view.getContext(), "Verification","Xác nhận mở bán", "Bạn có muốn mở bán sản phẩm này không?", () -> {
            // Lấy reference đến user dựa vào userID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(product.get().id);

            // Cập nhật status thành true
            databaseReference.child("status").setValue(true)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(view.getContext(), "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(view.getContext(), "Lỗi khi cập nhật trạng thái!", Toast.LENGTH_SHORT).show();
                    });
        });

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
