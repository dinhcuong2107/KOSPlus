package com.example.kosplus.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.kosplus.R;
import com.example.kosplus.databinding.ActivitySettingFragmentBinding;

public class SettingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ActivitySettingFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.activity_setting_fragment,container,false);

        SettingFragmentVM viewModel = new ViewModelProvider(requireActivity()).get(SettingFragmentVM.class);
        binding.setSettingFragment(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        return binding.getRoot();
    }
}