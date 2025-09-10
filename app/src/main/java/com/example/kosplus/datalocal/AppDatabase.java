package com.example.kosplus.datalocal;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.kosplus.func.Converters;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.ItemCartsDao;
import com.example.kosplus.model.Notifications;
import com.example.kosplus.model.NotificationsDao;
import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.OrderItemsDao;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.OrdersDao;
import com.example.kosplus.model.Products;
import com.example.kosplus.model.ProductsDao;
import com.example.kosplus.model.Promotions;
import com.example.kosplus.model.PromotionsDao;
import com.example.kosplus.model.Users;
import com.example.kosplus.model.UsersDao;

@Database(entities = {Users.class, Products.class, Orders.class, OrderItems.class, Notifications.class, ItemCarts.class, Promotions.class}, version = 7, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    public abstract UsersDao usersDao();
    public abstract NotificationsDao notificationsDao();
    public abstract ProductsDao productsDao();
    public abstract OrdersDao ordersDao();
    public abstract OrderItemsDao orderItemsDao();
    public abstract ItemCartsDao itemCartsDao();
    public abstract PromotionsDao promotionsDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "kosplus_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}