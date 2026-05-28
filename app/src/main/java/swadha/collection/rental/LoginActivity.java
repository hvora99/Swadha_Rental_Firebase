package swadha.collection.rental;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity
        extends AppCompatActivity {

    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_login
        );

        etEmail =
                findViewById(R.id.etEmail);

        etPassword =
                findViewById(R.id.etPassword);

        Button btnLogin =
                findViewById(R.id.btnLogin);

        TextView tvForgotPassword =
                findViewById(
                        R.id.tvForgotPassword
                );

        tvForgotPassword.setOnClickListener(v -> {

            String email =

                    etEmail.getText()
                            .toString()
                            .trim();

            if(email.isEmpty()){

                etEmail.setError(
                        "Enter email first"
                );

                return;
            }

            FirebaseAuth
                    .getInstance()

                    .sendPasswordResetEmail(
                            email
                    )

                    .addOnSuccessListener(unused -> {

                        Toast.makeText(

                                this,

                                "Password reset email sent",

                                Toast.LENGTH_LONG

                        ).show();
                    })

                    .addOnFailureListener(e -> {

                        Toast.makeText(

                                this,

                                e.getMessage(),

                                Toast.LENGTH_LONG

                        ).show();
                    });
        });

        btnLogin.setOnClickListener(v -> {

            login();
        });
    }

    private void login(){

        String email =
                etEmail.getText().toString().trim();

        String password =
                etPassword.getText().toString().trim();

        if(email.isEmpty()
                ||
                password.isEmpty()){

            Toast.makeText(
                    this,
                    "Enter email/password",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        FirebaseAuth.getInstance()

                .signInWithEmailAndPassword(
                        email,
                        password
                )

                .addOnSuccessListener(auth -> {

                    String uid =

                            auth.getUser().getUid();

                    FirebaseFirestore
                            .getInstance()

                            .collection("users")

                            .document(uid)

                            .get()

                            .addOnSuccessListener(doc -> {

                                if(!doc.exists()){

                                    Toast.makeText(
                                            this,
                                            "User record missing",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                Boolean active =
                                        doc.getBoolean("active");

                                if(active == null
                                        ||
                                        !active){

                                    Toast.makeText(
                                            this,
                                            "Account disabled",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    return;
                                }

                                String role =
                                        doc.getString("role");

                                String name =
                                        doc.getString("name");

                                SharedPreferences pref =

                                        getSharedPreferences(
                                                "session",
                                                MODE_PRIVATE
                                        );

                                pref.edit()

                                        .putString(
                                                "role",
                                                role
                                        )

                                        .putString(
                                                "name",
                                                name
                                        )

                                        .apply();


                                String pin =
                                        pref.getString(
                                                "pin",
                                                null
                                        );

                                if(pin == null){

                                    startActivity(

                                            new Intent(
                                                    this,
                                                    CreatePinActivity.class
                                            )
                                    );

                                }else{

                                    startActivity(

                                            new Intent(
                                                    this,
                                                    DashboardActivity.class
                                            )
                                    );
                                }

                                finish();
                            });

                })
                .addOnFailureListener(e -> {

                    String error =

                            e.getMessage() == null
                                    ?
                                    ""
                                    :
                                    e.getMessage();

                    if(error.contains("password is invalid")
                            ||
                            error.contains("INVALID_LOGIN_CREDENTIALS")){

                        etPassword.setError(
                                "Incorrect password"
                        );

                        etPassword.requestFocus();

                    }else if(error.contains("no user record")
                            ||
                            error.contains("EMAIL_NOT_FOUND")){

                        etEmail.setError(
                                "Account not found"
                        );

                        etEmail.requestFocus();

                    }else if(error.contains("badly formatted")){

                        etEmail.setError(
                                "Invalid email"
                        );

                        etEmail.requestFocus();

                    }else{

                        Toast.makeText(

                                this,

                                "Login failed",

                                Toast.LENGTH_LONG

                        ).show();
                    }
                });
    }
}