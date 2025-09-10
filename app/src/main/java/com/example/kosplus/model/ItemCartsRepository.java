package com.example.kosplus.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kosplus.DataBinderMapperImpl;
import com.example.kosplus.datalocal.AppDatabase;
import com.example.kosplus.datalocal.DataLocalManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ItemCartsRepository {
    private final ItemCartsDao itemCartsDao;
    private final Executor executor = Executors.newSingleThreadExecutor();


    public ItemCartsRepository(@NonNull Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        itemCartsDao = db.itemCartsDao();
    }
    public LiveData<List<ItemCarts>> getAllCartItems() {
        return itemCartsDao.getAllCartItems(DataLocalManager.getUid());
    }

    public void addToCart(String productId, Consumer<Boolean> callback) {
        executor.execute(() -> {
            ItemCarts existing = itemCartsDao.getCartItemByProductId(DataLocalManager.getUid(), productId);
            if (existing != null) {
                // Nếu sản phẩm đã tồn tại trong giỏ hàng
                callback.accept(false);
            } else {
                ItemCarts item = new ItemCarts();
                item.productId = productId;
                item.userId = DataLocalManager.getUid();
                item.quantity = 1;
                itemCartsDao.insert(item);
                // Nếu sản phẩm mới được thêm vào giỏ hàng
                callback.accept(true);
            }
        });
    }

    public void clearAll() {
        executor.execute(() -> itemCartsDao.clearAll(DataLocalManager.getUid()));
    }
    public void clearItem(String productId) {
        executor.execute(() -> itemCartsDao.clearItem(DataLocalManager.getUid(), productId));

    }
}