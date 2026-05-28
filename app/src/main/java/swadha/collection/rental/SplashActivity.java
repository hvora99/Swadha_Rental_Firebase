package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import android.text.Editable;
import android.text.TextWatcher;
public class SplashActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_splash
        );

        UpdateChecker.check(

                this,

                new UpdateChecker.UpdateCallback() {

                    @Override
                    public void onNoUpdate() {

                        openNextScreen();
                    }

                    @Override
                    public void onUpdateShown() {

                    }
                }
        );
    }

    private void openNextScreen(){

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

        finish();
    }
}