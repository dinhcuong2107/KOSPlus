package com.example.kosplus;

import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.app.Application;
import android.app.DatePickerDialog;
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
import com.example.kosplus.databinding.CustomDialogUploadBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Users;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class RegisterVM extends AndroidViewModel {
    private MutableLiveData<AppCompatActivity> context = new MutableLiveData<>();
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;
    public ObservableField<String> avatar = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> dob = new ObservableField<>();
    public ObservableField<String> sex = new ObservableField<>();
    public ObservableField<String> timeactivity = new ObservableField<>();
    public ObservableField<String> phone = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public ObservableField<String> passwordagain = new ObservableField<>();
    public RegisterVM(@NonNull Application application, @NonNull ActivityResultRegistry registry) {
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
                Log.d("KOS Plus", "RegisterVM Cropped Image URI: " + croppedUri);

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
                // Android 13+ dùng quyền mới
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("KOS Plus", "RegisterVM pickImage: Permission not granted");

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                } else {
                    // Đã có quyền
                    Log.d("KOS Plus", "RegisterVM pickImage: Permission granted");
                    pickImageLauncher.launch("image/*");
                }
            } else {
                // Android < 13
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(activity,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                } else {
                    // Đã có quyền
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

    public void onClickRegister(View view) {

        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime > 0) {
                String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(internetTime));
                Log.d("REAL_TIME", "Thời gian Internet: " + timeString);

                timeactivity.set(timeString);
            }
        }).start();

        if (TextUtils.isEmpty(avatar.get())) {
            Toast.makeText(view.getContext(), "Cập nhật ảnh đại diện", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(name.get()) || name.get().trim().split("\\s+").length < 2) {
            Toast.makeText(view.getContext(), "Họ và Tên phải có ít nhất 2 từ!", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(sex.get())) {
            Toast.makeText(view.getContext(), "Cập nhật giới tính", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(dob.get())) {
            Toast.makeText(view.getContext(), "Cập nhật ngày sinh", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(phone.get()) || !Utils.isValidPhoneNumber(phone.get())) {
            Toast.makeText(view.getContext(), "Số điện thoại không hợp lệ!", LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(password.get()) || !Utils.isValidPassword(password.get())) {
            Toast.makeText(view.getContext(),"Mật khẩu phải có ít nhất 8 ký tự \n Chữ viết hoa, chữ thường, số và ký tự đặc biệt!",LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(passwordagain.get()) || !Utils.isPasswordConfirmed(password.get(), passwordagain.get())) {
            Toast.makeText(view.getContext(),"Xác nhận mật khẩu không khớp!",LENGTH_LONG).show();
        }else {
            Utils.checkPhoneExists(phone.get(), exists -> {
                if (exists) {
                    Toast.makeText(view.getContext(), "Số điện thoại đã được sử dụng",LENGTH_LONG).show();
                } else {
                    // Register
                    completeRegister(view);
                }
            });
        }
    }

    private void completeRegister(View view) {
        String token = OneSignal.getUser().getPushSubscription().getId();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users");
        String UID = databaseReference.push().getKey();
        DataLocalManager.setUid(UID);
        Users users = new Users(UID, avatar.get(), name.get(), sex.get(), dob.get(), phone.get(), password.get(), "Customer",timeactivity.get(), token, true);

        databaseReference.child(UID).setValue(users, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Utils.pushNotification("", "Wellcome to KOS Plus", "Chúc mừng bạn đã tạo tài khoản thành công!", users.id, timeactivity.get());
                    OneSignalNotification.sendNotificationToUser(users.token, "Wellcome to KOS Plus", "Chúc mừng bạn đã tạo tài khoản thành công!");

                    createWallet(UID);

                    Intent intent = new Intent(view.getContext(), MainActivity.class);
                    view.getContext().startActivity(intent);
                } else {
                    Toast.makeText(view.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void createWallet(String id){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Wallets").child(id);
        databaseReference.setValue(0);
    }

    public void onNextIntentLogin(View view){
        Intent intent = new Intent(view.getContext(),LoginActivity.class);
        view.getContext().startActivity(intent);
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
