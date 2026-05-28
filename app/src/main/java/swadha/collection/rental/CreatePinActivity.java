package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class CreatePinActivity
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

        btnUnlock.setText(
                "Set PIN"
        );

        btnUnlock.setOnClickListener(v -> {

            String pin =

                    etPin.getText()
                            .toString()
                            .trim();

            if(pin.length() != 4){

                Toast.makeText(

                        this,

                        "Enter 4 digit PIN",

                        Toast.LENGTH_SHORT

                ).show();

                return;
            }

            SharedPreferences pref =

                    getSharedPreferences(
                            "session",
                            MODE_PRIVATE
                    );

            pref.edit()

                    .putString(
                            "pin",
                            pin
                    )

                    .apply();

            startActivity(

                    new Intent(
                            this,
                            DashboardActivity.class
                    )
            );

            finish();
        });
    }
}