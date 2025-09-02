package com.example.kosplus.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.CustomDialogConnectPromotionBinding;
import com.example.kosplus.databinding.CustomDialogProductBinding;
import com.example.kosplus.databinding.ItemProductBinding;
import com.example.kosplus.databinding.ItemProductVerticalBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.ProductEditActivity;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.Products;
import com.example.kosplus.model.Promotions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductVerticalAdapter extends RecyclerView.Adapter<ProductVerticalAdapter.ProductViewHolder> {
    List<Products> list;
    private Map<String, Integer> soldMap = new HashMap<>();
    private DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
    public ProductVerticalAdapter(List<Products> list) {
        this.list = list;
        loadSoldData();
        notifyDataSetChanged();
    }

    public void updateData(List<Products> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    // 🔹 Load toàn bộ số lượng đã bán của tất cả sản phẩm
    private void loadSoldData() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                soldMap.clear();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Orders order = orderSnap.getValue(Orders.class);

                    if (order != null && order.completedTime != null && !order.completedTime.isEmpty()) {

                        // duyệt qua danh sách sản phẩm trong đơn hàng
                        for (int i = 0; i < order.productId.size(); i++) {
                            String productId = order.productId.get(i);
                            int quantity = order.quantity.get(i);

                            soldMap.put(productId, soldMap.getOrDefault(productId, 0) + quantity);
                        }
                    }
                }
                notifyDataSetChanged(); // 🔥 cập nhật lại danh sách sau khi load xong
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductVerticalBinding binding = ItemProductVerticalBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new ProductVerticalAdapter.ProductViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Products product = list.get(position);
        Picasso.get().load(product.imageUrl).into(holder.binding.imageView);
        holder.binding.name.setText(product.name);
        holder.binding.description.setText(product.description);

        if (product.promotion == null || product.promotion.isEmpty() || product.promotion.equals("")) {
            holder.binding.price.setVisibility(GONE);
            holder.binding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
        } else {
            new Thread(() -> {
                long internetTime = Utils.getInternetTimeMillis();
                if (internetTime > 0) {
                    //HH:mm:ss dd/MM/yyyy
                    String timeString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(internetTime));
                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);

                    // Mở dialog trong UI thread sau khi đã lấy giờ
                    new Handler(Looper.getMainLooper()).post(() -> {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions").child(product.promotion);
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        Promotions promotion = snapshot.getValue(Promotions.class);

                                        if (!promotion.status || !Utils.checkTime(timeString, promotion.start_date, promotion.end_date)) {
                                            holder.binding.price.setText("CT KM kết thúc");
                                            holder.binding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
                                        } else {
                                            holder.binding.price.setText(Utils.formatCurrencyVND(product.price));
                                            holder.binding.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                                            if (promotion.type.equals("amount")) {
                                                int finalPrice = product.price - promotion.discount;
                                                holder.binding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                                            } else {
                                                int finalPrice = product.price - (product.price * promotion.discount / 100);
                                                holder.binding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                                            }
                                        }
                                    }
                                    else {
                                        holder.binding.price.setVisibility(GONE);
                                        holder.binding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                    });
                }
            }).start();
        }
        holder.binding.done.setOnClickListener(view -> {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Carts");

            // Lấy UID hiện tại
            String currentUserId = DataLocalManager.getUid();
            String currentProductId = product.id;

            // Truy vấn kiểm tra trùng
            databaseReference.orderByChild("userId").equalTo(currentUserId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            boolean isDuplicate = false;

                            for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                                ItemCarts existingItem = itemSnapshot.getValue(ItemCarts.class);
                                if (existingItem != null && currentProductId.equals(existingItem.productId)) {
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            if (isDuplicate) {
                                Toast.makeText(view.getContext(), "Sản phẩm đã có trong giỏ hàng", Toast.LENGTH_SHORT).show();
                            } else {
                                // Không trùng, thêm mới
                                String SID = databaseReference.push().getKey();
                                ItemCarts itemCart = new ItemCarts(SID, currentProductId, currentUserId, true);

                                databaseReference.child(SID).setValue(itemCart, (error, ref) -> {
                                    if (error == null) {
                                        Toast.makeText(view.getContext(), "Thêm vào giỏ hàng thành công", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(view.getContext(), "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(view.getContext(), "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        holder.binding.layout.setOnClickListener(v -> {
            new Thread(() -> {
                long internetTime = Utils.getInternetTimeMillis();
                if (internetTime > 0) {
                    //HH:mm:ss dd/MM/yyyy
                    String timeString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(internetTime));
                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);

                    // Mở dialog trong UI thread sau khi đã lấy giờ
                    new Handler(Looper.getMainLooper()).post(() -> {
                        showDialogDetailProduct(v,product,timeString);
                    });
                }
            }).start();
        });
    }

    private void showDialogDetailProduct(View v, Products product, String timeNow) {
        Dialog dialog = new Dialog(v.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogProductBinding productDetailBinding = CustomDialogProductBinding.inflate(LayoutInflater.from(v.getContext()));
        dialog.setContentView(productDetailBinding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        Picasso.get().load(product.imageUrl).into(productDetailBinding.imageView);
        productDetailBinding.name.setText(product.name);
        productDetailBinding.description.setText("[ "+product.type+" ]"+ "\n" +product.description);
        productDetailBinding.category.setText(product.category);

        // 🔹 Lấy số lượng đã bán từ Map
        int soldQuantity = soldMap.getOrDefault(product.id, 0);
        if (soldQuantity == 0) {
            productDetailBinding.quantity.setText("New");
        }else {
            productDetailBinding.quantity.setText("Đã bán: " + soldQuantity);
        }

        if (product.status) {
            productDetailBinding.status.setText("Còn hàng");
        } else {
            productDetailBinding.status.setText("Hết hàng");
        }

        if (!DataLocalManager.getRole().equals("Admin")) {
            productDetailBinding.promotionconnect.setVisibility(GONE);
            productDetailBinding.promotiondisconnect.setVisibility(GONE);
            productDetailBinding.fix.setVisibility(GONE);
        }
        if (product.promotion == null || product.promotion.isEmpty()) {
            if (DataLocalManager.getRole().equals("Admin"))
            {
                productDetailBinding.promotionconnect.setVisibility(VISIBLE);
            }


            productDetailBinding.promotion.setVisibility(GONE);
            productDetailBinding.promotionStatus.setVisibility(GONE);
            productDetailBinding.price.setVisibility(GONE);
            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
            productDetailBinding.promotionconnect.setVisibility(GONE);
        } else {
            if (DataLocalManager.getRole().equals("Admin"))
            {
                productDetailBinding.promotiondisconnect.setVisibility(VISIBLE);
            }
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions").child(""+product.promotion);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        productDetailBinding.price.setVisibility(GONE);
                        productDetailBinding.promotion.setVisibility(GONE);
                        productDetailBinding.promotionStatus.setVisibility(GONE);
                        productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
                        Toast.makeText(v.getContext(), "Không tìm thấy khuyến mãi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Promotions promotion = snapshot.getValue(Promotions.class);
                    Log.e("checkTime: ", "Time now" + timeNow );
                    Log.e("checkTime: ", "Time s" + promotion.start_date);
                    Log.e("checkTime: ", "Time e" + promotion.end_date);
                    if (promotion.status && Utils.checkTime(timeNow, promotion.start_date, promotion.end_date))
                    {
                        Log.e("checkTime: ", "Time e" + Utils.checkTime(timeNow, promotion.start_date, promotion.end_date) );
                        if (promotion.type.equals("amount")) {
                            productDetailBinding.promotion.setText(" - " + promotion.discount + " VNĐ");
                            productDetailBinding.promotionStatus.setText(promotion.start_date + " - " + promotion.end_date);

                            productDetailBinding.price.setText(Utils.formatCurrencyVND(product.price));
                            productDetailBinding.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            int finalPrice = product.price - promotion.discount;
                            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                        } else {
                            productDetailBinding.promotion.setText(" - " + promotion.discount+"%");
                            productDetailBinding.promotionStatus.setText(promotion.start_date + " - " + promotion.end_date);

                            productDetailBinding.price.setText(Utils.formatCurrencyVND(product.price));
                            productDetailBinding.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                            int finalPrice = product.price - product.price * promotion.discount/100;
                            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                        }

                    } else {
                        productDetailBinding.promotion.setText("Chương trình khuyến mãi");
                        productDetailBinding.promotionStatus.setText("Chương trình khuyến mãi đã hết hạn: " + promotion.end_date);

                        productDetailBinding.price.setVisibility(GONE);
                        productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        productDetailBinding.fix.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ProductEditActivity.class);
                intent.putExtra("ID", product.id);
                view.getContext().startActivity(intent);
        });
        productDetailBinding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Carts");
                String SID = databaseReference.push().getKey();

                ItemCarts itemCart = new ItemCarts(SID, product.id, DataLocalManager.getUid(), true);

                databaseReference.child(SID).setValue(itemCart, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                        if (error == null) {
                            Intent intent = new Intent(v.getContext(), CartsActivity.class);
                            v.getContext().startActivity(intent);
                        } else {
                            Toast.makeText(v.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        productDetailBinding.promotionconnect.setOnClickListener(view -> {
            ObservableField<String> promotion_select = new ObservableField<>();

            Dialog dialog_promotion = new Dialog(v.getContext());
            dialog_promotion.requestWindowFeature(Window.FEATURE_NO_TITLE);
            CustomDialogConnectPromotionBinding connectPromotionBinding = CustomDialogConnectPromotionBinding.inflate(LayoutInflater.from(v.getContext()));
            dialog_promotion.setContentView(connectPromotionBinding.getRoot());

            Window window_promotion = dialog_promotion.getWindow();
            window_promotion.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
            window_promotion.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams win_promotion = window_promotion.getAttributes();
            win_promotion.gravity = Gravity.CENTER;
            window_promotion.setAttributes(win_promotion);
            dialog_promotion.setCancelable(false);

            List<Promotions> promotionList = new ArrayList<>();
            List<String> codeList = new ArrayList<>();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_dropdown_item_1line, codeList);
            connectPromotionBinding.promotion.setAdapter(adapter);
            connectPromotionBinding.promotion.setThreshold(1);  // Bắt đầu gợi ý sau 1 ký tự

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    promotionList.clear();
                    codeList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        Promotions promotion = dataSnapshot.getValue(Promotions.class);
                        if (promotion.status) {
                            codeList.add(promotion.code);
                            promotionList.add(promotion);
                        }
                    }
                    adapter.notifyDataSetChanged();  // Cập nhật adapter để hiện gợi ý
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Lỗi tải khuyến mãi: " + error.getMessage());
                }
            });

            connectPromotionBinding.promotion.setOnItemClickListener((parent, itemview, positionCode, id) -> {
                String selectedCode = adapter.getItem(positionCode);
                promotion_select.set(promotionList.get(positionCode).id);
                Toast.makeText(view.getContext(), "Chọn mã: " + selectedCode, Toast.LENGTH_SHORT).show();

                connectPromotionBinding.title.setText(promotionList.get(positionCode).title);
                connectPromotionBinding.code.setText(promotionList.get(positionCode).code);
                connectPromotionBinding.discount.setText("" + promotionList.get(positionCode).discount);
                if (promotionList.get(positionCode).type.equals("amount")) {
                    connectPromotionBinding.percent.setChecked(false);
                    connectPromotionBinding.amount.setChecked(true);
                } else {
                    connectPromotionBinding.percent.setChecked(true);
                    connectPromotionBinding.amount.setChecked(false);
                }
                connectPromotionBinding.startDate.setText(promotionList.get(positionCode).start_date);
                connectPromotionBinding.endDate.setText(promotionList.get(positionCode).end_date);
            });

            connectPromotionBinding.done.setOnClickListener(view1 -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus")
                        .child("Products").child(product.id).child("promotion");
                databaseReference.setValue(promotion_select.get());
                dialog_promotion.dismiss();
                dialog.dismiss();
            });

            connectPromotionBinding.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_promotion.dismiss();
                    dialog.dismiss();
                }
            });

            dialog_promotion.show();
        });

        productDetailBinding.promotiondisconnect.setOnClickListener(view -> {
            Utils.showVerificationDialog(view.getContext(), "Verification", "Bạn có muốn xóa khuyến mãi không?","Xác nhận xóa khuyến mãi "+productDetailBinding.promotion, () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus")
                        .child("Products").child(product.id).child("promotion");
                databaseReference.setValue("");
            });
        });

        productDetailBinding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        ItemProductVerticalBinding binding;
        public ProductViewHolder(@NonNull ItemProductVerticalBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
