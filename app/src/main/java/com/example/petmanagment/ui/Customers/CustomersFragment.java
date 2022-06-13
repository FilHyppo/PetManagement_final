package com.example.petmanagment.ui.Customers;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petmanagment.R;
import com.example.petmanagment.databinding.FragmentCustomersBinding;
import com.example.petmanagment.login.Customer;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomersFragment extends Fragment {

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private EditText firstname, lastname, address, mobile, email;
    private Button cancel, confirm;
    private DatabaseReference dataref;
    private FragmentCustomersBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CustomersViewModel customersViewModel =
                new ViewModelProvider(this).get(CustomersViewModel.class);

        ArrayList<String> customers = new ArrayList<>();
        ArrayList<String> flag = new ArrayList<>();

        customers.add("ciao");
        customers.add("luca");
        customers.add("riccardogay");
        customers.add("francesco");
        customers.add("marco");


        binding = FragmentCustomersBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        EditText searchCustomer = (EditText) root.findViewById(R.id.search_customer_editText);
        final RecyclerView recyclerView = root.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        recyclerView.setAdapter(new ListAdapter(customers));
        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            if (!searchCustomer.getText().toString().isEmpty()) {
                for (String l : customers) {
                    if (l.startsWith(searchCustomer.getText().toString())) {
                        flag.add(l);
                    }
                }

                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new ListAdapter(flag));
            }
            else {
                recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
                recyclerView.setAdapter(new ListAdapter(customers));
            }

        };


        searchCustomer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                handler.post(runnable);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                handler.removeCallbacksAndMessages(runnable);
                flag.clear();
            }
        });


        return root;
    }

    public void addUser(){

        dialogBuilder = new AlertDialog.Builder(getContext());
        final View popup = getLayoutInflater().inflate(R.layout.popwindow,null);
        firstname = (EditText) popup.findViewById(R.id.firstname);
        lastname = (EditText) popup.findViewById(R.id.lastname);
        mobile = (EditText) popup.findViewById(R.id.phone);
        email = (EditText) popup.findViewById(R.id.email);

        confirm = (Button) popup.findViewById(R.id.cbutton);

        dialogBuilder.setView(popup);
        dialog = dialogBuilder.create();
        dialog.show();

        confirm.setOnClickListener(view -> {
            Customer c = new Customer(firstname.getText().toString(), lastname.getText().toString(), email.getText().toString(), mobile.getText().toString());
           // writeNewUser(c);
            dialog.dismiss();

        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}