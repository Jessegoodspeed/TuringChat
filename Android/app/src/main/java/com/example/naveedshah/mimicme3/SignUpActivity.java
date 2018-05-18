package com.example.naveedshah.mimicme3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.HttpURLConnection;
import java.io.*;
import android.util.*;
import java.net.*;
import android.content.Intent;
import org.json.*;
import android.support.design.widget.Snackbar;

/**
 * Created by NaveedShah on 4/14/18.
 */

public class SignUpActivity extends AppCompatActivity {

    // The information from the UI will be stored in these variables.
    private AutoCompleteTextView mEmailView;
    private AutoCompleteTextView mNameView;
    private EditText mPasswordView;

    // The signup form
    private View mSignupFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up_activity);
        // Set up the signup form and attain the data from the views
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });

        mNameView = (AutoCompleteTextView) findViewById(R.id.name);

        // on pressing the sign up button, run the attemptRegister function
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_up_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });

        mSignupFormView = findViewById(R.id.sign_up_form);
    }

    // Once the user presses Sign-up, this function is launched
    private void attemptRegister() {

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String name = mNameView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new Thread() {

                public void run() {

                    HttpURLConnection conn = null;
                    try {

                        String urlString = "http://159.65.38.56:8000/user/register";

                        URL url = new URL(urlString);

                        conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");

                        // Create JSON object to send to backend
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("email", email);
                            jsonObject.put("password", password);
                            jsonObject.put("name", name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(jsonObject.toString());
                        wr.flush();
                        wr.close();


                        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                        StringBuilder sb = new StringBuilder();
                        String line;

                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();


                        if (conn.getResponseCode() == 400) {
                            Log.d("from server:","400");
                            Snackbar mySnackbar;
                            mySnackbar = Snackbar.make(findViewById(R.id.sign_up_view),"Error. Please try again. Email may already in use.",3000);
                            mySnackbar.show();
                        } else if (conn.getResponseCode() == 201) {
                            Log.d("from server:","201");
                            Snackbar mySnackbar;
                            mySnackbar = Snackbar.make(findViewById(R.id.sign_up_view),"Success! Please Log in.",3000);
                            mySnackbar.show();

                            Intent myIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(myIntent);
                        }

                    } catch (IOException e) {
                        Log.e("MYAPP", "exception for connection:", e);
                    } finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
            }.start();
        }
    }



    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        // Password must be atleast 6 characters
        return password.length() >= 6;
    }


}
