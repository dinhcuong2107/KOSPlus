package com.example.kosplus.adapter;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.app.Application;
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
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.CustomDialogConnectPromotionBinding;
import com.example.kosplus.databinding.CustomDialogProductBinding;
import com.example.kosplus.databinding.ItemProductBinding;
import com.example.kosplus.databinding.ItemProductVerticalBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.ProductEditActivity;
import com.example.kosplus.func.Utils;
import com.example.kosplus.livedata.ItemCartsLiveData;
import com.example.kosplus.livedata.ProductsLiveData;
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
    boolean isShowing = true;
    private boolean isExpanded = false; // mặc định thu gọn
    private DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
    public ProductVerticalAdapter(List<Products> list, boolean isShowing) {
        this.list = list;
        this.isShowing = isShowing;
        notifyDataSetChanged();
    }

    public void updateData(List<Products> list, boolean isShowing) {
        this.list = list;
        this.isShowing = isShowing;
        notifyDataSetChanged();
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
        if (isShowing) {
            holder.binding.position.setText(String.valueOf(position % 10 + 1));

            if (position % 10 == 0) {
                holder.binding.position.setTextColor(Color.RED);
            }
            if (position % 10 == 1) {
                holder.binding.position.setTextColor(Color.BLUE);
            }
            if (position % 10 == 2) {
                holder.binding.position.setTextColor(Color.GREEN);
            }
            if (position % 10 > 2) {
                holder.binding.position.setVisibility(View.GONE);
            }
        } else {
            holder.binding.position.setVisibility(View.GONE);
        }

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

                                        if (!promotion.status || internetTime < promotion.start_date || internetTime > promotion.end_date) {
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
            ItemCartsLiveData itemCartsLiveData = new ItemCartsLiveData(new Application());
            itemCartsLiveData.addToCart(product.id, isSuccess ->{
                ((Activity) view.getContext()).runOnUiThread(() -> {
                    if (isSuccess) {
                        Toast.makeText(view.getContext(), "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(view.getContext(), "Sản phẩm đã có trong giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });
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
                        showDialogDetailProduct(v, product, internetTime);
                    });
                }
            }).start();
        });
    }

    private void showDialogDetailProduct(View v, Products product, Long timeNow) {
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

        // Quan sát dữ liệu từ LiveData
        ProductsLiveData liveData = ViewModelProviders.of((FragmentActivity) v.getContext()).get(ProductsLiveData.class);
        liveData.getSoldQuantityByProduct(product.id).observe((LifecycleOwner) v.getContext(), quantity -> {
            if (quantity != null && quantity > 0) {
                productDetailBinding.quantity.setText("Đã bán: " + quantity);
            } else {
                productDetailBinding.quantity.setText("New");
            }
        });

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
                productDetailBinding.promotiondisconnect.setVisibility(GONE);
            }

            productDetailBinding.promotion.setVisibility(GONE);
            productDetailBinding.promotionStatus.setVisibility(GONE);
            productDetailBinding.price.setVisibility(GONE);
            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(product.price));
        } else {
            if (DataLocalManager.getRole().equals("Admin")) {
                productDetailBinding.promotiondisconnect.setVisibility(VISIBLE);
                productDetailBinding.promotionconnect.setVisibility(GONE);
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
                    if (promotion.status && timeNow >= promotion.start_date && timeNow <= promotion.end_date)
                    {
                        if (promotion.type.equals("amount")) {
                            productDetailBinding.promotion.setText(" - " + promotion.discount + " VNĐ");
                            productDetailBinding.promotionStatus.setText(Utils.longToTimeString(promotion.start_date).substring(9) + " - " + Utils.longToTimeString(promotion.end_date).substring(9));

                            productDetailBinding.price.setText(Utils.formatCurrencyVND(product.price));
                            productDetailBinding.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            int finalPrice = product.price - promotion.discount;
                            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                        } else {
                            productDetailBinding.promotion.setText(" - " + promotion.discount+"%");
                            productDetailBinding.promotionStatus.setText(Utils.longToTimeString(promotion.start_date).substring(9) + " - " + Utils.longToTimeString(promotion.end_date).substring(9));

                            productDetailBinding.price.setText(Utils.formatCurrencyVND(product.price));
                            productDetailBinding.price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);

                            int finalPrice = product.price - product.price * promotion.discount/100;
                            productDetailBinding.finalPrice.setText(Utils.formatCurrencyVND(finalPrice));
                        }

                    } else {
                        productDetailBinding.promotion.setVisibility(GONE);
                        if (timeNow < promotion.start_date) {
                            productDetailBinding.promotionStatus.setText("Chương trình khuyến mãi chưa bắt đầu: " + Utils.longToTimeString(promotion.start_date).substring(9));
                        }
                        if (timeNow > promotion.end_date) {
                            productDetailBinding.promotionStatus.setText("Chương trình khuyến mãi đã hết hạn: " + Utils.longToTimeString(promotion.end_date).substring(9));
                        }
                        if (promotion.status == false) {
                            productDetailBinding.promotionStatus.setText("Chương trình khuyến mãi đã tạm thời bị khóa");
                        }

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
            public void onClick(View view) {
                ItemCartsLiveData itemCartsLiveData = new ItemCartsLiveData(new Application());
                itemCartsLiveData.addToCart(product.id, isSuccess ->{
                    ((Activity) view.getContext()).runOnUiThread(() -> {
                        if (isSuccess) {
                            Toast.makeText(view.getContext(), "Thêm vào giỏ hàng thành công", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(view.getContext(), "Sản phẩm đã có trong giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                connectPromotionBinding.startDate.setText(Utils.longToTimeString(promotionList.get(positionCode).start_date));
                connectPromotionBinding.endDate.setText(Utils.longToTimeString(promotionList.get(positionCode).end_date));
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
            Utils.showVerificationDialog(view.getContext(), "Verification", "Bạn có muốn xóa khuyến mãi không?","Xác nhận xóa khuyến mãi ", () -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus")
                        .child("Products").child(product.id).child("promotion");
                databaseReference.setValue("");
                dialog.dismiss();
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

    // gọi khi click nút bên ngoài
    public void toggleExpand() {
        isExpanded = !isExpanded;
        notifyDataSetChanged();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    @Override
    public int getItemCount() {
        if (list != null){
            if (isExpanded) {
                return list.size(); // hiển thị tất cả
            } else {
                return Math.min(list.size(), 3); // chỉ hiển thị 3
            }
        }
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
