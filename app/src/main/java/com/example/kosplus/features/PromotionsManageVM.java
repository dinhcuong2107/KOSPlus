package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.databinding.CustomDialogCreatePromotionBinding;
import com.example.kosplus.model.Promotions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PromotionsManageVM extends ViewModel {
    CustomDialogCreatePromotionBinding binding;

    public PromotionsManageVM() {

    }

    public void onDialogCreatePromotion(View view) {

        ObservableField<String> startDate = new ObservableField<>();
        ObservableField<String> endDate = new ObservableField<>();
        ObservableField<String> type = new ObservableField<>();

        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = CustomDialogCreatePromotionBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        binding.promotion.setVisibility(View.GONE);

        binding.percent.setOnClickListener(v -> {
            type.set("percent");
            binding.amount.setMaxLines(2);
        });
        binding.amount.setOnClickListener(v -> {
            type.set("amount");
        });

        binding.startDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            DatePickerDialog pickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    startDate.set(simpleDateFormat.format(calendar.getTime()));
                    binding.startDate.setText(simpleDateFormat.format(calendar.getTime()));
                }
            }, year, month, day);
            pickerDialog.show();
        });

        binding.endDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            DatePickerDialog pickerDialog = new DatePickerDialog(view.getContext(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    endDate.set(simpleDateFormat.format(calendar.getTime()));
                    binding.endDate.setText(simpleDateFormat.format(calendar.getTime()));
                }
            }, year, month, day);
            pickerDialog.show();
        });

        binding.done.setOnClickListener(v -> {
            if (binding.title.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng nhập tiêu đề", LENGTH_LONG).show();
            } else if (binding.code.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng nhập mã KM", LENGTH_LONG).show();
            } else if (type.get() == null) {
                Toast.makeText(view.getContext(), "Vui lòng chọn loại khuyến mãi", LENGTH_LONG).show();
            } else if (binding.discount.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng nhập khuyến mãi", LENGTH_LONG).show();
            } else if (binding.startDate.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng chọn ngày bắt đầu", LENGTH_LONG).show();
            } else if (binding.endDate.getText().toString().isEmpty()) {
                Toast.makeText(view.getContext(), "Vui lòng chọn ngày kết thúc", LENGTH_LONG).show();
            } else {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus").child("Promotions");
                String UID = databaseReference.push().getKey();
                Promotions promotion = new Promotions(UID, binding.code.getText().toString(), binding.title.getText().toString(), type.get(), binding.startDate.getText().toString(), binding.endDate.getText().toString(), Integer.parseInt(binding.discount.getText().toString()), true);
                databaseReference.child(UID).setValue(promotion, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
//                            Intent intent = new Intent(view.getContext(), MainActivity.class);
//                            view.getContext().startActivity(intent);
                            Toast.makeText(view.getContext(), "Thêm thành công", LENGTH_LONG).show();
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

}