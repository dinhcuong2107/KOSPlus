package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogCreateShopBinding;
import com.example.kosplus.model.Shops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopsManageVM extends ViewModel {
    private final MutableLiveData<List<Shops>> list = new MutableLiveData<>();

    public ShopsManageVM() {
        loadShop();
    }

    public LiveData<List<Shops>> getShopList() {
        return list;
    }

    private void loadShop() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus");
        databaseReference.child("Shops").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Shops> shopList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Shops shop = dataSnapshot.getValue(Shops.class);
                    shopList.add(shop);
                }
                if (shopList.size()==1) {
                    databaseReference.child("ShopDefault").setValue(shopList.get(0).id);
                }
                list.setValue(shopList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Shop Manage", "Lỗi: " + error.getMessage());
            }
        });
    }


    public void onDialogRegisterShop (View view) {

        ObservableField<String> ob_bankName = new ObservableField<>();
        ObservableField<String> ob_bankCode = new ObservableField<>();

        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        CustomDialogCreateShopBinding binding = CustomDialogCreateShopBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        Map<String, String> banks = new HashMap<>();
        banks.put("Vietcombank", "970436");
        banks.put("Techcombank", "970407");
        banks.put("BIDV", "970418");
        banks.put("MB Bank", "970422");
        banks.put("VietinBank", "970415");
        banks.put("Agribank", "970417");
        banks.put("ACB", "970416");
        banks.put("Sacombank", "970403");
        banks.put("VPBank", "970432");
        banks.put("VIB", "970441");
        banks.put("SHB", "970443");
        banks.put("HDBank", "970437");
        banks.put("TPBank", "970423");
        banks.put("Eximbank", "970431");
        banks.put("SCB", "970429");
        banks.put("SeABank", "970440");
        banks.put("ABBank", "970425");
        banks.put("OceanBank", "970414");
        banks.put("NCB", "970419");
        banks.put("PVcomBank", "970412");
        banks.put("SaigonBank", "970426");
        banks.put("VietABank", "970427");
        banks.put("VietCapitalBank", "970428");
        banks.put("BaoVietBank", "970438");

        List<String> bankNames = new ArrayList<>(banks.keySet());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_item, bankNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(adapter);

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBankName = bankNames.get(position);
                String bankCode = banks.get(selectedBankName);
                Log.d("BANK_SELECTED", "Tên: " + selectedBankName + " - Mã: " + bankCode);
                ob_bankName.set(selectedBankName);
                ob_bankCode.set(bankCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.done.setOnClickListener(v -> {
            if (TextUtils.isEmpty(binding.code.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật mã cơ sở", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.address.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật địa chỉ", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.hotline.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật số điện thoại", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(ob_bankName.get())) {
                Toast.makeText(view.getContext(), "Số điện thoại không hợp lệ!", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.bank.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật số tài khoản", LENGTH_LONG).show();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus");

                Shops shop = new Shops(binding.code.getText().toString(),
                        binding.address.getText().toString(),
                        binding.hotline.getText().toString(),ob_bankCode.get(), ob_bankName.get(), binding.bank.getText().toString(), binding.radioOpen.isChecked());

                databaseReference.child("Shops").child(binding.code.getText().toString()).
                        setValue(shop, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                if (error == null) {

                                    Toast.makeText(view.getContext(), "Cập nhật thành công", LENGTH_LONG).show();

                                    if (binding.checkbox.isChecked()) {
                                        databaseReference.child("ShopDefault").setValue(binding.code.getText().toString());
                                    }
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
