package com.example.kosplus.livedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.kosplus.model.ProductSales;
import com.example.kosplus.model.Products;
import com.example.kosplus.model.ProductsRepository;

import java.util.List;

public class ProductsLiveData extends AndroidViewModel {
    private final ProductsRepository repository;
    private final LiveData<List<Products>> liveData;
    private final LiveData<List<Products>> liveDataFood;
    private final LiveData<List<Products>> liveDataDrink;

    public ProductsLiveData(@NonNull Application application) {
        super(application);
        repository = new ProductsRepository(application);
        liveData = repository.getAllProducts();
        liveDataFood = repository.getProductsByType("Food");
        liveDataDrink = repository.getProductsByType("Drink");
    }
    public LiveData<List<Products>> getLiveData() {
        return liveData;
    }
    public LiveData<List<Products>> getLiveDataFood() {
        return liveDataFood;
    }
    public LiveData<List<Products>> getLiveDataDrink() {
        return liveDataDrink;
    }

    public LiveData<Integer> getSoldQuantityByProduct(String productId) {
        return repository.getSoldQuantityByProduct(productId);
    }

    public LiveData<List<Products>> getTopProducts() {
        return repository.getTop10Products();
    }
    public LiveData<List<Products>> getTop10BestSellingProductsOfTime(long start, long end) {
        return repository.getTop10BestSellingProductsOfTime(start, end);
    }
    public LiveData<List<Products>> getActivePromotions() {
        return repository.getActivePromotions();
    }
    public LiveData<List<Products>> getRandomProducts() {
        return repository.getRandomProducts();
    }

}
