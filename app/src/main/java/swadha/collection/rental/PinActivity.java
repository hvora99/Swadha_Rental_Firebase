package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class PinActivity
        extends AppCompatActivity {
    EditText etPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_pin
        );

        etPin =
                findViewById(R.id.etPin);

        MaterialButton btnUnlock =
                findViewById(R.id.btnUnlock);

        TextView tvUsePassword =
                findViewById(R.id.tvUsePassword);

        SharedPreferences pref =

                getSharedPreferences(
                        "session",
                        MODE_PRIVATE
                );

        String savedPin =
                pref.getString(
                        "pin",
                        ""
                );

        etPin.addTextChangedListener(

                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {

                    }

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        String enteredPin =
                                s.toString().trim();

                        if(enteredPin.length() == 4){

                            if(enteredPin.equals(savedPin)){

                                startActivity(

                                        new Intent(
                                                PinActivity.this,
                                                DashboardActivity.class
                                        )
                                );

                                finish();

                            }else{

                                etPin.setText("");

                                Toast.makeText(

                                        PinActivity.this,

                                        "Invalid PIN",

                                        Toast.LENGTH_SHORT

                                ).show();
                            }
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {

                    }
                }
        );


        tvUsePassword.setOnClickListener(v -> {

            FirebaseAuth
                    .getInstance()
                    .signOut();

            pref.edit()
                    .remove("pin")
                    .apply();

            startActivity(

                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );

            finish();
        });
    }
}