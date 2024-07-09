package com.example.dasnet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // UI elements
    private EditText userIdEditText, passwordEditText;
    private ProgressBar progressBar;

    // Network URL
    private static final String URL = "http://"+ Config.BASE_IP + "/dasnet2/login2.php?";

    // NFC handler
    private NFCHandler nfcHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setContentView(R.layout.contact_us);

        Log.d("MainActivity", "onCreate called");

        // Initialize the UI elements
        initializeUI();

        // Initialize the NFC handler
        initializeNFCHandler();

        // Find the TextView and set an OnClickListener
        TextView textViewSignUp = findViewById(R.id.tv_sign_up);
        textViewSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Contact.class);
            TaskManager.getInstance().startActivity(this, intent);
        });
    }

    /**
     * Initialize UI elements.
     */
    private void initializeUI() {
        // Find EditTexts for user ID and password
        userIdEditText = findViewById(R.id.et_user_id);
        passwordEditText = findViewById(R.id.et_password);

        // Initialize and start Lottie animation
        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.user2);
        animationView.playAnimation();

        // Find and hide the ProgressBar initially
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);


        RoundedImageView imageView2 = findViewById(R.id.imageView2);
        Glide.with(this)
                .load(R.drawable.technologycenter) // Ensure this matches your file in res/drawable
                .apply(RequestOptions.bitmapTransform(new CustomRoundedCornersTransformation(40))) // Adjust the radius as needed
                .into(imageView2);

        RoundedImageView imageView3 = findViewById(R.id.imageView3);
        Glide.with(this)
                .load(R.drawable.technologycenter) // Ensure this matches your file in res/drawable
                .apply(RequestOptions.bitmapTransform(new CustomRoundedCornersTransformation(40))) // Adjust the radius as needed
                .into(imageView3);


        Button btn_view_service_report = findViewById(R.id.btn_view_service_report);
        btn_view_service_report.setBackgroundResource(R.drawable.button_background);
    }

    /**
     * Initialize the NFC handler.
     *
     *     private void initializeNFCHandler() {
     *         nfcHandler = new NFCHandler(this, this, "http://192.168.../dasnet2/php/NFC.php?");
     *     }
     */
    private void initializeNFCHandler() {
        nfcHandler = new NFCHandler(this, this, "http://"+ Config.BASE_IP + "/dasnet2/php/NFC.php?");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);

        // Handle NFC intent
        nfcHandler.handleNfcIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Disable NFC foreground dispatch
        nfcHandler.disableForegroundDispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Enable NFC foreground dispatch
        nfcHandler.enableForegroundDispatch();
    }

    /**
     * Called when the login button is clicked.
     */
    public void login(View view) {
        if (areInputsValid()) {
            performLogin();
        } else {
            showToast("Please enter your credentials");
        }
    }

    /**
     * Check if the input fields are valid.
     *
     * @return true if both user ID and password are not empty, false otherwise
     */
    private boolean areInputsValid() {
        String userId = userIdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        return !userId.isEmpty() && !password.isEmpty();
    }

    /**
     * Perform the login operation.
     */
    private void performLogin() {
        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        String userId = userIdEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Create a string request for login
        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {
            // Hide the progress bar on response
            progressBar.setVisibility(View.GONE);
            handleLoginResponse(response);
        }, error -> {
            // Hide the progress bar on error
            progressBar.setVisibility(View.GONE);
            showToast(error.getMessage());
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user", userId);
                params.put("password", password);
                return params;
            }
        };

        // Add the request to the request queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(request);
    }

    /**
     * Handle the login response.
     *
     * @param response The response from the login request
     */
    private void handleLoginResponse(String response) {
        if (!response.contains("no pudo ingresar")) {
            try {
                // Parse the JSON response
                JSONArray jsonArray = new JSONArray(response);
                String name = jsonArray.getString(0);
                String lastName = jsonArray.getString(1);

                clearInputs();

                // Start the service report activity
                Intent intent = new Intent(MainActivity.this, service_report.class);
                intent.putExtra("Name", name);
                intent.putExtra("LastName", lastName);
                TaskManager.getInstance().startActivity(this, intent);
                showToast("Welcome: " + name + " " + lastName);

            } catch (JSONException e) {
                e.printStackTrace();
                showToast("Error parsing response");
            }
        } else {
            showToast(response);
        }
    }


    /**
     * Clear the input fields.
     */
    private void clearInputs() {
        userIdEditText.setText("");
        passwordEditText.setText("");
    }

    /**
     * Show a toast message.
     *
     * @param message The message to show
     */
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}