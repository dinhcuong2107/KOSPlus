package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Activity;
import android.app.Application;
import android.app.DatePickerDialog;
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
import android.widget.DatePicker;
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
import com.example.kosplus.LoginActivity;
import com.example.kosplus.MainActivity;
import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogPasswordBinding;
import com.example.kosplus.databinding.CustomDialogUploadBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Users;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.UUID;

public class ProfileEditVM extends AndroidViewModel {
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    public ObservableField<Users> users= new ObservableField<>();
    public ObservableField<String> avatar = new ObservableField<>();
    public ObservableField<String> sex = new ObservableField<>();
    public ObservableField<String> dob = new ObservableField<>();
    public ObservableField<String> phone = new ObservableField<>();
    public ObservableField<Boolean> admin = new ObservableField<>();


    public ProfileEditVM(@NonNull Application application, @NonNull ActivityResultRegistry registry) {
        super(application);

        if (DataLocalManager.getRole().equals("Admin")) {
            admin.set(true);
        }else {
            admin.set(false);
        }

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

    public void onclickMale() {
        sex.set("male");
    }
    public void onclickFemale() {
         sex.set("female");
    }

    public void setUser (String userID) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(userID);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    users.set(snapshot.getValue(Users.class));
                    dob.set(users.get().dob);
                    avatar.set(users.get().imageUrl);
                    phone.set(users.get().phone);
                    sex.set(users.get().sex);
                } else {
                    Toast.makeText(context.getValue(), "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void onclickBirthday(View view) {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        DatePickerDialog pickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                dob.set(simpleDateFormat.format(calendar.getTime()));
            }
        }, year, month, day);
        pickerDialog.show();
    }
    public void onProfileUpdate(View view) {

        if (TextUtils.isEmpty(users.get().fullname) || users.get().fullname.trim().split("\\s+").length < 2) {
            Toast.makeText(view.getContext(), "Họ và Tên phải có ít nhất 2 từ!", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(sex.get())) {
            sex.set(users.get().sex);
        } else if (TextUtils.isEmpty(dob.get())) {
            dob.set(users.get().dob);
            Toast.makeText(view.getContext(), "Cập nhật ngày sinh", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(phone.get()) || !Utils.isValidPhoneNumber(phone.get())) {
            Toast.makeText(view.getContext(), "Số điện thoại không hợp lệ!", LENGTH_LONG).show();
        } else {
            Utils.checkPhoneExists(phone.get(), exists -> {
                if (exists) {
                    if (phone.get().equals(users.get().phone))
                    {
                        completeUpdate(view);
                    } else {
                        Toast.makeText(view.getContext(), "Số điện thoại đã được sử dụng",LENGTH_LONG).show();}
                } else {
                    completeUpdate(view);
                }
            });
        }
    }
    private void completeUpdate(View view) {

        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false); // Không cho bấm ra ngoài để tắt
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
        Users usersUpdate = new Users(users.get().id, avatar.get(), users.get().fullname, sex.get(), dob.get(), phone.get(), users.get().password, users.get().role, users.get().time, users.get().token,users.get().status);

        databaseReference.setValue(usersUpdate, new DatabaseReference.CompletionListener() {
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

    public void onPasswordUpdate (View view) {
        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogPasswordBinding binding = CustomDialogPasswordBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        binding.done.setOnClickListener(v -> {
            // Lấy reference đến user dựa vào userID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);

            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Lấy mật khẩu cũ từ Firebase
                        String storedPassword = snapshot.child("password").getValue(String.class);

                        // Lấy dữ liệu từ EditText
                        String currentPasswordInput = binding.password.getText().toString().trim();
                        String newPassword = binding.passwordNew.getText().toString().trim();
                        String confirmPassword = binding.passwordAgain.getText().toString().trim();

                        // Kiểm tra nếu mật khẩu cũ không đúng
                        if (storedPassword == null || !storedPassword.equals(currentPasswordInput)) {
                            Toast.makeText(v.getContext(), "Mật khẩu cũ không đúng!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Kiểm tra mật khẩu mới
                        if (TextUtils.isEmpty(newPassword) || !Utils.isValidPassword(newPassword)) {
                            Toast.makeText(v.getContext(),"Mật khẩu phải có ít nhất 8 ký tự \n Chữ viết hoa, chữ thường, số và ký tự đặc biệt!",LENGTH_LONG).show();
                            return;
                        }

                        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp không
                        if (!newPassword.equals(confirmPassword)) {
                            Toast.makeText(v.getContext(), "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Cập nhật mật khẩu mới lên Firebase
                        databaseReference.child("password").setValue(newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(v.getContext(), "Mật khẩu đã được cập nhật!", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(v.getContext(), LoginActivity.class);
                                    v.getContext().startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(v.getContext(), "Lỗi khi cập nhật mật khẩu!", Toast.LENGTH_SHORT).show();
                                });

                    } else {
                        Toast.makeText(view.getContext(), "Người dùng không tồn tại!", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(v.getContext(), "Lỗi truy vấn dữ liệu!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    public void onLock(View view){
        Utils.showVerificationDialog(view.getContext(), "Verification","Xác nhận khóa tài khoản", "Bạn có muốn khóa tài khoản này không?", () -> {
            // Lấy reference đến user dựa vào userID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);

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
        Utils.showVerificationDialog(view.getContext(), "Verification","Xác nhận mở khóa tài khoản", "Bạn có muốn mở khóa tài khoản này không?", () -> {
            // Lấy reference đến user dựa vào userID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);

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

    public void onUpRole(View view){
        if (users.get().role.equals("Admin")) {
            Toast.makeText(view.getContext(), "Không thể cập nhật", Toast.LENGTH_SHORT).show();
        }
        if (users.get().role.equals("Manager")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Nâng cấp tài khoản", "Admin", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Admin");
            });
        }
        if (users.get().role.equals("Staff")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Nâng cấp tài khoản", "Manager", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Manager");
            });

        }
        if (users.get().role.equals("Customer")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Nâng cấp tài khoản", "Staff", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Staff");
            });

        }
    }

    public void onDownRole(View view){
        if (users.get().role.equals("Admin")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Giảm cấp tài khoản", "Manager", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Manager");
            });
        }
        if (users.get().role.equals("Manager")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Giảm cấp tài khoản", "Staff", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Staff");
            });
        }
        if (users.get().role.equals("Staff")) {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Giảm cấp tài khoản", "Customer", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.get().id);
                databaseReference.child("role").setValue("Customer");
            });
        }
        if (users.get().role.equals("Customer")) {
            Toast.makeText(view.getContext(), "Không thể cập nhật", Toast.LENGTH_SHORT).show();
        }

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
