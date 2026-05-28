package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        if(FirebaseAuth
                .getInstance()
                .getCurrentUser() != null){

            SharedPreferences pref =

                    getSharedPreferences(
                            "session",
                            MODE_PRIVATE
                    );

            String pin =
                    pref.getString(
                            "pin",
                            null
                    );

            if(pin != null){

                startActivity(

                        new Intent(
                                this,
                                PinActivity.class
                        )
                );

            }else{

                startActivity(

                        new Intent(
                                this,
                                CreatePinActivity.class
                        )
                );
            }

        }else{

            startActivity(

                    new Intent(
                            this,
                            LoginActivity.class
                    )
            );
        }

    }
}