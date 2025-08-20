package com.example.kosplus.features;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.kosplus.R;
import com.example.kosplus.databinding.ActivityProductEditBinding;

public class ProductEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityProductEditBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_product_edit);

        ProductEditVM viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                return (T) new ProductEditVM(getApplication(), getActivityResultRegistry());
            }
        }).get(ProductEditVM.class);

        Intent intent = getIntent();
        String ID = intent.getStringExtra("ID");

        viewModel.setProduct(ID);

        binding.setProductEdit(viewModel);
        binding.executePendingBindings();

    }
}