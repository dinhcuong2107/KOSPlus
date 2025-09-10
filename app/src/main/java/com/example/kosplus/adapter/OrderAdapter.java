package com.example.kosplus.adapter;

import android.app.Application;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
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
import androidx.databinding.ObservableField;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogOrderBinding;
import com.example.kosplus.databinding.ItemOrderBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.func.Utils;
import com.example.kosplus.livedata.OrderItemsLiveData;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.Shops;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ItemOrderViewHolder> {
    private List<Orders> list, list_search;
    public OrderAdapter(List<Orders> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }
    public void updateData(List<Orders> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }

    public void filter (String keySearch) {
        if (keySearch == null || keySearch.isEmpty()) {
            list = list_search;
        } else {
            List<Orders> ordersList = new ArrayList<>();
            for (Orders orders : list_search) {
                if (orders.address.toLowerCase().contains(keySearch.toLowerCase()) ||
                        orders.userId.contains(keySearch)
                        || orders.id.contains(keySearch)) {
                    ordersList.add(orders);
                }
            }
            list = ordersList;
        }
        if (list != null) {
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ItemOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new OrderAdapter.ItemOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemOrderViewHolder holder, int position) {
        Orders order = list.get(position);

        holder.binding.textOrderId.setText("Mã đơn: #" + order.id);
        holder.binding.textOrderTime.setText("Ngày đặt: " + Utils.longToTimeString(order.createdTime));
        holder.binding.textOrderAddress.setText("Địa chỉ: " + order.address);
        holder.binding.textOrderTotal.setText("Tổng tiền: " + Utils.formatCurrencyVND(order.total));


        if (order.canceledTime != null && order.canceledTime != 0) {
            holder.binding.textOrderStatus.setText("Đã hủy");
            holder.binding.textOrderStatus.setTextColor(Color.parseColor("#FF0000"));
        } else if (order.completedTime != null && order.completedTime != 0) {
            holder.binding.textOrderStatus.setText("Đã hoàn thành");
            holder.binding.textOrderStatus.setTextColor(Color.parseColor("#0099D9"));
        } else if (order.deliveryTime != null && order.deliveryTime != 0) {
            holder.binding.textOrderStatus.setText("Đang giao");
            holder.binding.textOrderStatus.setTextColor(Color.parseColor("#0099D9"));
        } else if (order.confirmedTime != null && order.confirmedTime != 0) {
            holder.binding.textOrderStatus.setText("Đã xác nhận");
            holder.binding.textOrderStatus.setTextColor(Color.parseColor("#0099D9"));
        } else {
            holder.binding.textOrderStatus.setText("Chờ xác nhận");
            holder.binding.textOrderStatus.setTextColor(Color.parseColor("#0099D9"));
        }

        holder.binding.textOrderDetail.setOnClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            CustomDialogOrderBinding orderDetailBinding = CustomDialogOrderBinding.inflate(LayoutInflater.from(v.getContext()));
            dialog.setContentView(orderDetailBinding.getRoot());

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams win = window.getAttributes();
            win.gravity = Gravity.CENTER;
            window.setAttributes(win);
            dialog.setCancelable(false);

            orderDetailBinding.textOrderAddress.setText(order.address);
            orderDetailBinding.textOrderId.setText("Mã đơn hàng: "+order.id);
            orderDetailBinding.textTotal.setText(Utils.formatCurrencyVND(order.total));
            orderDetailBinding.textPaymentMenthod.setText(order.paymentMethod);

            if (order.note != null && !order.note.isEmpty()) {
                orderDetailBinding.textNote.setText("Ghi chú: " + order.note);
            } else {
                orderDetailBinding.textNote.setVisibility(View.GONE);
            }

            orderDetailBinding.textOrderId.setOnClickListener(view -> {
                Utils.showQRDialog(v.getContext(), order.id);
            });

            orderDetailBinding.textCreated.setText("Đã đặt hàng:\n" + Utils.longToTimeString(order.createdTime));

            if (order.confirmedTime == 0 && order.deliveryTime == 0 && order.completedTime == 0) {
                orderDetailBinding.textConfirmed.setClickable(true);

                orderDetailBinding.textDelivery.setClickable(false);
                orderDetailBinding.textCompleted.setClickable(false);
            }

            if (order.confirmedTime > 0 && order.deliveryTime == 0 && order.completedTime == 0) {
                orderDetailBinding.textConfirmed.setText("Đã xác nhận:\n" + Utils.longToTimeString(order.confirmedTime));
                orderDetailBinding.textConfirmed.setClickable(false);

                orderDetailBinding.textCompleted.setClickable(false);
            }

            if (order.deliveryTime > 0 ) {
                orderDetailBinding.textDelivery.setText("Đang giao:\n" + Utils.longToTimeString(order.deliveryTime));
                orderDetailBinding.textDelivery.setClickable(false);
            }

            if (order.completedTime > 0) {
                orderDetailBinding.textCompleted.setText("Đã hoàn thành:\n" + Utils.longToTimeString(order.completedTime));
                orderDetailBinding.textCompleted.setClickable(false);
            }

            if (order.canceledTime > 0) {
                orderDetailBinding.textCancel.setText("Đã hủy:\n" + Utils.longToTimeString(order.canceledTime));
                orderDetailBinding.textcancelReason.setText("Lý do: " + order.canceledReason);

                orderDetailBinding.spinner.setVisibility(View.GONE);
                orderDetailBinding.cancelOrder.setVisibility(View.GONE);
            } else {
                orderDetailBinding.textCancel.setVisibility(View.GONE);
                orderDetailBinding.textcancelReason.setVisibility(View.GONE);

                if (DataLocalManager.getRole().equals("Customer")) {
                    if (order.confirmedTime == 0) {
                        orderDetailBinding.spinner.setVisibility(View.VISIBLE);
                        orderDetailBinding.cancelOrder.setVisibility(View.VISIBLE);
                    } else {
                        orderDetailBinding.spinner.setVisibility(View.GONE);
                        orderDetailBinding.cancelOrder.setVisibility(View.GONE);
                    }
                } else {
                    if (order.confirmedTime == 0) {
                        orderDetailBinding.spinner.setVisibility(View.VISIBLE);
                        orderDetailBinding.cancelOrder.setVisibility(View.VISIBLE);
                    } else if (order.deliveryTime > 0 && order.completedTime == 0) {
                        orderDetailBinding.spinner.setVisibility(View.VISIBLE);
                        orderDetailBinding.cancelOrder.setVisibility(View.VISIBLE);
                    } else {
                        orderDetailBinding.spinner.setVisibility(View.GONE);
                        orderDetailBinding.cancelOrder.setVisibility(View.GONE);
                    }
                }
            }

            List<String> cancelReasonList = Arrays.asList(
                    "Khách không nhận hàng",
                    "Thay đổi địa chỉ giao hàng",
                    "Thay đổi sản phẩm, số lượng",
                    "Đặt nhầm",
                    "Lý do khác"
            );
            ArrayAdapter<String> adapterReason = new ArrayAdapter<>(v.getContext(), android.R.layout.simple_spinner_item, cancelReasonList);
            adapterReason.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            orderDetailBinding.spinner.setAdapter(adapterReason);

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(order.userId);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);
                    orderDetailBinding.textUserName.setText(users.fullname);
                    orderDetailBinding.textUserPhone.setText(users.phone);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            // Cấu hình RecyclerView
            orderDetailBinding.recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext(), RecyclerView.VERTICAL, false));
            orderDetailBinding.recyclerView.setHasFixedSize(true);

            OrderItemsLiveData liveData = new OrderItemsLiveData((Application) v.getContext().getApplicationContext());
            liveData.getLiveDataByOrderId(order.id).observe((LifecycleOwner) v.getContext(), orderItems -> {
                OrderItemAdapter adapter = new OrderItemAdapter(orderItems);
                orderDetailBinding.recyclerView.setAdapter(adapter);
            });

            orderDetailBinding.textConfirmed.setOnClickListener(view -> {
                if (!DataLocalManager.getRole().equals("Customer")) {
                    if (order.confirmedTime == null || order.confirmedTime == 0) {
                        Utils.showVerificationDialog(v.getContext(), "veryfication","Xác nhận đơn hàng", "" + order.id,  () -> {
                            DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("KOS Plus")
                                    .child("Orders")
                                    .child(order.id)
                                    .child("confirmedTime");
                            new Thread(() -> {
                                long internetTime = Utils.getInternetTimeMillis();
                                if (internetTime > 0) {
                                    String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                                            .format(new Date(internetTime));
                                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);
                                    orderReference.setValue(internetTime);

                                    Utils.pushNotification("", "Đơn hàng đã được xác nhận", "Mã đơn hàng: " + order.id, order.userId ,internetTime);
                                    OneSignalNotification.sendNotificationToUser(order.userId, "Đơn hàng đã được xác nhận", "Thời gian xác nhận" + timeString);
                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(view.getContext(), "Lỗi lấy thời gian", Toast.LENGTH_SHORT).show();
                                }
                            }).start();
                        });
                    }
                }

            });

            orderDetailBinding.textDelivery.setOnClickListener(view -> {
                if (!DataLocalManager.getRole().equals("Customer")) {
                    if (order.deliveryTime == null || order.deliveryTime == 0) {
                        Utils.showVerificationDialog(v.getContext(), "veryfication","Xác nhận đang giao hàng", "" + order.id,  () -> {
                            DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("KOS Plus")
                                    .child("Orders")
                                    .child(order.id)
                                    .child("deliveryTime");
                            new Thread(() -> {
                                long internetTime = Utils.getInternetTimeMillis();
                                if (internetTime > 0) {
                                    String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                                            .format(new Date(internetTime));
                                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);
                                    orderReference.setValue(internetTime);

                                    Utils.pushNotification("", "Đơn hàng đang được giao", "Mã đơn hàng: " + order.id, order.userId ,internetTime);
                                    OneSignalNotification.sendNotificationToUser(order.userId, "Đơn hàng đang được giao", "Thời gian xác nhận" + timeString);

                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(view.getContext(), "Lỗi lấy thời gian", Toast.LENGTH_SHORT).show();
                                }
                            }).start();
                        });
                    }
                }

            });

            orderDetailBinding.textCompleted.setOnClickListener(view -> {

                ObservableField<Integer> quantityTicket = new ObservableField<>();

                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Ticket").child(order.userId);
                ref2.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            quantityTicket.set(snapshot.getValue(Integer.class));
                        } else {
                            ref2.setValue(0);
                            quantityTicket.set(0);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                if (!DataLocalManager.getRole().equals("Customer")) {
                    if (order.completedTime == null || order.completedTime == 0) {
                        Utils.showVerificationDialog(v.getContext(), "veryfication","Xác nhận hoàn thành đơn hàng", "" + order.id,  () -> {
                            DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("KOS Plus")
                                    .child("Orders")
                                    .child(order.id)
                                    .child("completedTime");
                            new Thread(() -> {
                                long internetTime = Utils.getInternetTimeMillis();
                                if (internetTime > 0) {
                                    String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                                            .format(new Date(internetTime));
                                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);
                                    orderReference.setValue(internetTime);

                                    // Nếu tổng tiền > 300.000 thì cộng thêm 1 vé quay
                                    if (order.total > 300000) {
                                        ref2.setValue(quantityTicket.get() + 1);

                                        Utils.pushNotification("", "Bạn nhận được 1 vé quay thưởng", "Quay thưởng ngay để nhận quà tặng", order.userId ,internetTime);
                                        OneSignalNotification.sendNotificationToUser(order.userId, "Bạn nhận được 1 vé quay thưởng", "Quay thưởng ngay để nhận quà tặng");
                                    }

                                    Utils.pushNotification("", "Đơn hàng đã hoàn thành", "Mã đơn hàng: " + order.id, order.userId ,internetTime);
                                    OneSignalNotification.sendNotificationToUser(order.userId, "Đơn hàng đã hoàn thành", "Thời gian xác nhận" + timeString);

                                    dialog.dismiss();
                                }else {
                                    dialog.dismiss();
                                    Toast.makeText(view.getContext(), "Lỗi lấy thời gian", Toast.LENGTH_SHORT).show();
                                }
                            }).start();
                        });
                    }
                }
            });

            orderDetailBinding.cancelOrder.setOnClickListener(view -> {
                if (order.deliveryTime != null || order.deliveryTime != 0 || order.completedTime != null || order.completedTime != 0 || order.confirmedTime != null || order.confirmedTime != 0) {
                    String selectedReason = orderDetailBinding.spinner.getSelectedItem().toString();

                    if (orderDetailBinding.spinner.getSelectedItemPosition() == AdapterView.INVALID_POSITION) {
                        Toast.makeText(view.getContext(), "Vui lòng chọn lý do hủy đơn", Toast.LENGTH_SHORT).show();
                    } else {
                        Utils.showVerificationDialog(view.getContext(), "veryfication", "Xác nhận hủy đơn hàng", "" + selectedReason, () -> {
                            DatabaseReference orderReference = FirebaseDatabase.getInstance().getReference("KOS Plus")
                                    .child("Orders")
                                    .child(order.id);

                            new Thread(() -> {
                                long internetTime = Utils.getInternetTimeMillis();
                                if (internetTime > 0) {
                                    String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                                            .format(new Date(internetTime));
                                    Log.d("REAL_TIME", "Thời gian Internet: " + timeString);
                                    orderReference.child("canceledTime").setValue(internetTime);
                                    orderReference.child("cancelReason").setValue(selectedReason);
                                    orderReference.child("status").setValue(false);

                                    Utils.pushNotification("", "Đơn hàng đã được hủy", "Mã đơn hàng: " + order.id + "\nLý do " + selectedReason, order.userId, internetTime);
                                    OneSignalNotification.sendNotificationToUser(order.userId, "Đơn hàng đã được hủy", "Thời gian hủy" + timeString + "\nLý do " + selectedReason);

                                    dialog.dismiss();
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(view.getContext(), "Lỗi lấy thời gian", Toast.LENGTH_SHORT).show();
                                }
                            }).start();
                        });

                    }
                } else {
                    Toast.makeText(view.getContext(), "Không thể hủy đơn hàng", Toast.LENGTH_SHORT).show();
                }

            });

            if(order.paymentMethod.equals("Wallet")) {
                orderDetailBinding.imageView.setVisibility(View.GONE);
            } else {
                String prefix = order.address.split("-")[0];
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Shops");
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Shops shop = dataSnapshot.getValue(Shops.class);
                                if (shop != null && shop.id.equals(prefix)) {
                                    // Nếu shop.id trùng với prefix từ selectedCode (VD: "CS1")

                                    String baseUrl = "https://img.vietqr.io/image/";
                                    String qrUrl = baseUrl + shop.bankCode + "-" + shop.bankNumber + "-compact2.png" +
                                            "?amount=" + order.total +
                                            "&addInfo=" + Uri.encode(order.id);  // encode để tránh lỗi dấu cách

                                    Log.d("QR_URL", qrUrl);
                                    Picasso.get()
                                            .load(qrUrl)
                                            .placeholder(R.drawable.rounded_qr_code_24) // ảnh tạm khi tải
                                            .error(R.drawable.rounded_warning_24)             // ảnh khi lỗi
                                            .into(orderDetailBinding.imageView);                // hoặc findViewById(R.id.qrImageView)
                                    break; // Dừng vòng lặp khi tìm được shop phù hợp
                                }
                            }
                        } else {
                            Picasso.get().load(R.drawable.rounded_warning_24).into(orderDetailBinding.imageView);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("Firebase", "Lỗi truy vấn Shop: " + error.getMessage());
                    }
                });
            }
            orderDetailBinding.cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            dialog.show();
        });
    }


    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class ItemOrderViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;
        public ItemOrderViewHolder(@NonNull ItemOrderBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}

