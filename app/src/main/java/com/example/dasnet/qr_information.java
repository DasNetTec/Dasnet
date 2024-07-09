package com.example.dasnet;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class qr_information extends AppCompatActivity {

    private TextView qrInfo;
    private String serialNumber = "";
    private Button dateBtn;

    private static final String URL = "http://"+ Config.BASE_IP + "/dasnet2/Qr_Information.php?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_information);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);

        initializeUI();
        setupLottieAnimation();
        serialNumber = getIntent().getStringExtra("resultadoQR");

        fetchQrInformation();

        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDatePicker<Pair<Long, Long>> materialDatePicker =MaterialDatePicker.Builder.dateRangePicker().setSelection(new Pair<>(
                        MaterialDatePicker.thisMonthInUtcMilliseconds(),
                        MaterialDatePicker.todayInUtcMilliseconds()
                )).build();
                materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                    @Override
                    public void onPositiveButtonClick(Pair<Long, Long> selection) {
                        String startDate= new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection.first));
                        String endDate= new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(selection.second));

                        navigateToReportList(startDate,endDate);
                    }
                });
                materialDatePicker.show(getSupportFragmentManager(),"tag");

            }
        });
    }

    private void initializeUI() {
        qrInfo = findViewById(R.id.qr_info);
        dateBtn = findViewById(R.id.pickTime);

        RoundedImageView imageView4 = findViewById(R.id.imageView4);
        Glide.with(this)
                .load(R.drawable.technologycenter) // Ensure this matches your file in res/drawable
                .apply(RequestOptions.bitmapTransform(new CustomRoundedCornersTransformation(40))) // Adjust the radius as needed
                .into(imageView4);
    }

    private void setupLottieAnimation() {
        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.qrinfo);
        animationView.playAnimation();
    }



    private void navigateToReportList(String startDate, String endDate) {
        Intent intent = new Intent(this, Report_List.class);
        intent.putExtra("StartDate", startDate);
        intent.putExtra("EndDate", endDate);
        intent.putExtra("SerialNumber", serialNumber);
        startActivity(intent);
        finish();
    }

    private void fetchQrInformation() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Por favor espera...");
        progressDialog.show();

        StringRequest request = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                handleQrInformationResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showToast(error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("SerialNumber", serialNumber);
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(request);
    }

    private void handleQrInformationResponse(String response) {
        if (!response.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(response);
                JSONObject jsonObject = jsonArray.getJSONObject(0);

                String manufacture = jsonObject.getString("manufacturer");
                String type = jsonObject.getString("machine_type");
                String serialNumber = jsonObject.getString("serial_number");
                String idMachine = jsonObject.getString("machine_id");
                String id_line =jsonObject.getString("line_id");

                String formattedResponse = String.format("Manufacture: %s\nType: %s\nSerial Number: %s\nID Machine: %s \nLinea: %s",
                        manufacture, type, serialNumber, idMachine,id_line);

                qrInfo.setText(formattedResponse);
            } catch (JSONException e) {
                e.printStackTrace();
                showToast("Error parsing JSON");
            }
        } else {
            showToast("No se encontraron máquinas");
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
