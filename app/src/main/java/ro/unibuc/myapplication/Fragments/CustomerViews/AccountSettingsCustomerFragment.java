package ro.unibuc.myapplication.Fragments.CustomerViews;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

import ro.unibuc.myapplication.AccountActivity;
import ro.unibuc.myapplication.CustomerActivity;
import ro.unibuc.myapplication.Dao.RestaurantDatabase;
import ro.unibuc.myapplication.Models.Customer;
import ro.unibuc.myapplication.Models.Passwords;
import ro.unibuc.myapplication.R;

public class AccountSettingsCustomerFragment extends Fragment {
    EditText name;
    EditText email;
    EditText creditCard;
    EditText password;
    Button saveBtn;
    Button logoutBtn;
    SharedPreferences.Editor editor;

    public AccountSettingsCustomerFragment() { super(R.layout.fragment_user_account_settings);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        name = view.findViewById(R.id.editCustomerName);
        email = view.findViewById(R.id.editCustomerMail);
        creditCard = view.findViewById(R.id.editCreditCard);
        password = view.findViewById(R.id.customerPass);
        saveBtn = view.findViewById(R.id.saveChanges);
        logoutBtn = view.findViewById(R.id.customerLogOut);
        editor = (SharedPreferences.Editor)
                AccountActivity.getSharedPreferencesInstance(requireContext()).edit();

        Bundle bundle = getArguments();
        if (bundle != null) {
            Customer customer = bundle.getParcelable(CustomerActivity.CUSTOMER_KEY);
            if (customer != null) {
                name.setText(customer.getName());
                email.setText(customer.getEmail());
                creditCard.setText(customer.getEmail());

                saveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String nameVal = name.getText().toString();
                        String emailVal = email.getText().toString();
                        String creditCardVal = creditCard.getText().toString();
                        String passwordVal = password.getText().toString();

                        editor.putString(AccountActivity.SPKEY_NAME, nameVal);
                        AccountActivity.setCurrentUsername(nameVal);

                        Customer newCustomer = new Customer(nameVal, creditCardVal, customer.getOrders(), emailVal);
                        newCustomer.setPassword(customer.getPassword());
                        if (!passwordVal.equals("") && passwordVal.length() > 5){
                            Passwords passwords = new Passwords(passwordVal, newCustomer.getName());
                            String newPass = passwords.calculateHash();
                            newCustomer.setPassword(newPass);
                        }

                        newCustomer.setUid(customer.getUid());
                        RestaurantDatabase.getInstance(requireContext()).customerDAO().updateCustomer(newCustomer);

                    }
                });
            }
        }

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Logout
                editor.clear().apply();
                Toast.makeText(view.getContext(), "Logged out", Toast.LENGTH_SHORT).show();

                FirebaseAuth.getInstance().signOut();
                requireActivity().finish();
            }
        });
    }
}
