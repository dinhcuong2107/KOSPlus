package com.example.kosplus.livedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Notifications;
import com.example.kosplus.model.NotificationsRepository;

import java.util.ArrayList;
import java.util.List;
public class NotificationsLiveData extends AndroidViewModel {

    private final NotificationsRepository repository;
    private final MediatorLiveData<List<Notifications>> liveData = new MediatorLiveData<>();
    private final LiveData<List<Notifications>> notificationsNew;
    private final LiveData<List<Notifications>> notificationsImportant;

    public NotificationsLiveData(@NonNull Application application) {
        super(application);
        repository = new NotificationsRepository(application);

        // Tải dữ liệu lần đầu
        repository.preloadNotifications();

        // Lấy 2 luồng dữ liệu
        notificationsImportant = repository.getAllNotifications("All", 7); // Important lên đầu
        notificationsNew = repository.getAllNotifications(DataLocalManager.getUid(), 30); // Thông báo của user

        // Gộp 2 nguồn dữ liệu, Important luôn lên đầu
        liveData.addSource(notificationsImportant, list -> mergeLists(notificationsNew.getValue(), list));
        liveData.addSource(notificationsNew, list -> mergeLists(list, notificationsImportant.getValue()));

        // Bật realtime sync
        repository.startRealtimeSync();
    }

    public LiveData<List<Notifications>> getLiveData() {
        return liveData;
    }

    public LiveData<Integer> getCountNotifications() {
        return repository.getUnreadCount(DataLocalManager.getUid());
    }

    /**
     * Gộp 2 list -> Important lên đầu, không loại bỏ trùng
     */
    private void mergeLists(List<Notifications> userList, List<Notifications> importantList) {
        List<Notifications> merged = new ArrayList<>();

        // Important luôn lên đầu
        if (importantList != null) merged.addAll(importantList);

        // User notifications tiếp theo
        if (userList != null) merged.addAll(userList);

        liveData.setValue(merged);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopRealtimeSync();
    }
}
