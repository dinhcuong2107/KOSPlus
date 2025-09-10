package com.example.kosplus.adapter;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.CustomDialogCreateShopBinding;
import com.example.kosplus.databinding.ItemShopBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Shops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {
    List<Shops> list;

    public ShopAdapter(List<Shops> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Shops> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemShopBinding binding = ItemShopBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ShopAdapter.ShopViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shops shop = list.get(position);

        holder.binding.textviewID.setText(shop.id);
        holder.binding.textviewAdd.setText(shop.address);
        holder.binding.textviewHot.setText(shop.phone);

        if (shop.status) {
            holder.binding.textStatus.setText("Mở");
        } else {
            holder.binding.textStatus.setText("Đóng");
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("ShopDefault");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists() && snapshot.getValue(String.class).equals(shop.id)) {
                    holder.binding.textDefault.setVisibility(View.VISIBLE);
                } else {
                    holder.binding.textDefault.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.textStatus.setOnClickListener(view -> {
            if (shop.status) {
                Utils.showVerificationDialog(view.getContext(), "verification", ""+shop.id, "Bạn có muốn Đóng cửa?", () -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus");
                    reference.child("Shops").child(shop.id).child("status").setValue(false);
                });
            } else {
                Utils.showVerificationDialog(view.getContext(), "verification", ""+shop.id, "Bạn có muốn Mở cửa?", () -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus");
                    reference.child("Shops").child(shop.id).child("status").setValue(true);
                });
            }

        });

        holder.binding.layout.setOnClickListener(view -> {
            onDialogRegisterShop(view, shop);
        });

    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class ShopViewHolder extends RecyclerView.ViewHolder {
        ItemShopBinding binding;
        public ShopViewHolder(@NonNull ItemShopBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
    public void onDialogRegisterShop (View view, Shops shop) {
        ObservableField<String> ob_bankName = new ObservableField<>();
        ObservableField<String> ob_bankCode = new ObservableField<>();

        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogCreateShopBinding binding = CustomDialogCreateShopBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        binding.code.setEnabled(false);
        binding.code.setText(shop.id);

        binding.address.setText(shop.address);
        binding.hotline.setText(shop.phone);
        binding.bank.setText(shop.bankNumber);


        if (shop.status) {
            binding.radioOpen.setChecked(true);
        } else {
            binding.radioClose.setChecked(true);
        }

        binding.checkbox.setOnClickListener(v -> {
            if (!binding.checkbox.isChecked()) {
                Utils.showVerificationDialog(v.getContext(), "verification", ""+binding.code.getText().toString(), "Bạn có muốn chọn cơ sở này làm mặc định?", () -> {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("ShopDefault");
                    databaseReference.setValue(shop.id);
                });
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("ShopDefault");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.getValue(String.class).equals(shop.id)) {
                        binding.checkbox.setChecked(true);
                    } else {
                        binding.checkbox.setChecked(false);
                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ob_bankName.set(shop.bankName);
        ob_bankCode.set(shop.bankCode);

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

        Log.d("BANK_SELECTED", "Tên: " + ob_bankName.get() + " - Mã: " + ob_bankCode.get());
        binding.spinner.setSelection(bankNames.indexOf(ob_bankName.get()));

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
            } else if (TextUtils.isEmpty(ob_bankCode.get())) {
                Toast.makeText(view.getContext(), "Cập nhật tên ngân hàng", LENGTH_LONG).show();
            } else if (TextUtils.isEmpty(binding.bank.getText().toString())) {
                Toast.makeText(view.getContext(), "Cập nhật số tài khoản", LENGTH_LONG).show();
            } else {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus");
                reference.child("Shops").child(shop.id).
                        setValue(new Shops(
                                shop.id,
                                binding.address.getText().toString(),
                                binding.hotline.getText().toString(), ob_bankCode.get(), ob_bankName.get(),  binding.bank.getText().toString(),binding.radioOpen.isChecked()), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {

                                if (binding.checkbox.isChecked())
                                {
                                    reference.child("ShopDefault").setValue(shop.id);
                                }
                                Toast.makeText(view.getContext(), "Cập nhật thành công", LENGTH_LONG).show();
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
}
