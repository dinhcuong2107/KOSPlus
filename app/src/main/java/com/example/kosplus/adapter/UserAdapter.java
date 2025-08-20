package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemUserBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Users;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {
    List<Users> list, list_search;

    public UserAdapter(List<Users> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Users> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String keySearch = constraint.toString();
                if (keySearch.isEmpty()) {
                    list = list_search;
                } else {
                    List<Users> usersList = new ArrayList<>();
                    for (Users users : list_search) {
                        if (users.phone.toLowerCase().contains(keySearch.toLowerCase()) ||
                                users.fullname.toLowerCase().contains(keySearch.toLowerCase())
                                || users.id.contains(keySearch)) {
                            usersList.add(users);
                        }
                    }
                    list = usersList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<Users>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemUserBinding binding = ItemUserBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new UserAdapter.UserViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        Users users = list.get(position);

        Picasso.get().load(users.imageUrl).into(holder.binding.imageView);
        holder.binding.fullname.setText(users.fullname);
        holder.binding.phonenumber.setText(users.phone);

        holder.binding.layout.setOnClickListener(v -> {
            Utils.showProfileDialog(v, users);
        });

        holder.binding.imageQR.setOnClickListener(view -> {
            Utils.showQRDialog(view.getContext(), users.id);
        });

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        ItemUserBinding binding;

        public UserViewHolder(@NonNull ItemUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
