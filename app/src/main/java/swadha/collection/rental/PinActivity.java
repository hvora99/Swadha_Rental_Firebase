package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class PinActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_pin
        );

        EditText etPin =
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

        btnUnlock.setOnClickListener(v -> {

            String enteredPin =

                    etPin.getText()
                            .toString()
                            .trim();

            if(enteredPin.equals(savedPin)){

                startActivity(

                        new Intent(
                                this,
                                DashboardActivity.class
                        )
                );

                finish();

            }else{

                Toast.makeText(

                        this,

                        "Invalid PIN",

                        Toast.LENGTH_SHORT

                ).show();
            }
        });

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