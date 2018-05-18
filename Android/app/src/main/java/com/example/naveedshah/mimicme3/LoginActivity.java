package com.example.naveedshah.mimicme3;

// Libraries and files to import
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
import android.preference.*;
import android.content.Intent;
import org.json.*;
import android.support.design.widget.Snackbar;
import android.content.SharedPreferences;


public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mNameView; // Username
    private EditText mPasswordView; // Password

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Connect to activity_login.xml

        // Set up the login form.
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        // Get username from the name field
        mNameView = (AutoCompleteTextView) findViewById(R.id.name);

        // Find Login Button
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);

        // On click of the Login Button, start the attemptLogin() activity
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        // Find the Sign Up Button
        Button buttonOne = (Button) findViewById(R.id.goToSignUp);

        // On click of the Sign up Button, change activity to the Sign Up Activity
        buttonOne.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(myIntent);
            }
        });


    }

    private void attemptLogin() {

        // Reset errors.
        mPasswordView.setError(null);
        mNameView.setError(null);

        // Store values at the time of the login attempt.
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

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // There was no error with front-end validations. Attempt to login with
            // the backend
            new Thread() {

               public void run() {

                   HttpURLConnection conn = null;
                    try {

                        // port to connect to server

                        String urlString = "http://159.65.38.56:8000/user/login";

                        URL url = new URL(urlString);

                        conn = (HttpURLConnection) url.openConnection();

                        // Connect to the server and send a POST request
                        conn.setRequestMethod("POST");

                        // Send information user entered as a JSON object to the backend
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("password", password);
                            jsonObject.put("name", name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        // This sends the JSON object to the server
                        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                        wr.writeBytes(jsonObject.toString());
                        wr.flush();
                        wr.close();

                        // The server responds with response codes. 500 is an error, 200 means a succesful login
                        if (conn.getResponseCode() == 500) {
                            Snackbar mySnackbar;
                            mySnackbar = Snackbar.make(findViewById(R.id.login_view),"Error. Incorrect user/password",3000);
                            mySnackbar.show();

                        } else if (conn.getResponseCode() == 200) {

                            // Check the response received from the server
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                            // the string builder object builds the full response from the server
                            StringBuilder sb = new StringBuilder();
                            String line;

                            while ((line = br.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            br.close();

                            try {

                                // The server gives back the UID which needs to be saved in shared preferences
                                JSONObject json_obj=new JSONObject(sb.toString());
                                Integer value1=json_obj.getInt("uid");

                                // Utilize shared preferenes to save the uid as it will be needed later on in the ChatRoom
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                SharedPreferences.Editor editor = preferences.edit();

                                // save the UID and the username
                                editor.putInt("uid",value1);
                                editor.putString("username",name);

                                editor.apply();

                            } catch (JSONException e) {
                                // Throw an exception if found
                                Log.d("JSON Exception: ", e.toString());
                            }

                            // Finally change the activity to the recycler activity which contains the list of chatrooms
                            Intent myIntent = new Intent(LoginActivity.this, RecyclerActivity.class);
                            startActivity(myIntent);
                        }

                    } catch (IOException e) {
                        // Throw an exception if any found for the connection
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

    // Checks if the inputted email contains a "@" symbol
    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    // Checks to ensure that password is atleast 6 characters long
    private boolean isPasswordValid(String password) {
        // Password must be atleast 6 characters
        return password.length() >= 6;
    }


}

