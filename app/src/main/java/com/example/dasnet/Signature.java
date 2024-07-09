package com.example.dasnet;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Signature extends AppCompatActivity {
    private SignatureView signatureView;
    private String serialNumber;
    private String name;
    private String lname;
    private String date;
    private String type ="Preventivo";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signature);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);


        initializeIntentData();
        initializeUIElements();
    }


    private void initializeIntentData(){
        serialNumber = getIntent().getStringExtra("SerialNumber");
        name = getIntent().getStringExtra("Name");
        date = getCurrentDate();
        lname = getIntent().getStringExtra("LastName");
    }

    private void initializeUIElements(){
        signatureView = findViewById(R.id.signatureView);
        Button btnClear = findViewById(R.id.btnClear);
        Button btnSave = findViewById(R.id.btnSend);
        EditText comments = findViewById(R.id.editTextText);

        btnClear.setOnClickListener(v -> signatureView.clear());

        btnSave.setOnClickListener(v -> handleSaveButtonClick(comments));
    }
    private void handleSaveButtonClick(EditText comments){
        if(comments.getText().toString().isEmpty()){
            comments.setText("sin comentarios");
        }

        boolean success = signatureView.saveAndUploadFile(Signature.this,name + ".JPEG");

        if (success){
            Toast.makeText(Signature.this,"File uploaded successfully",Toast.LENGTH_SHORT).show();
            sendReportDataToServer(comments);
        }
    }
    private void  sendReportDataToServer(EditText comments){
        String url = "http://"+ Config.BASE_IP + "/dasnet2/Report_Insert.php";

        StringRequest request = new StringRequest(Request.Method.POST,url,
                response -> handleServerResponse(),
                error -> handleServerError(error.toString())){
            @Override
            protected Map<String,String>getParams(){
                return createReportParams(comments);
            }
        };
        RequestQueue queue = Volley.newRequestQueue(Signature.this);
        queue.add(request);
    }

    private Map<String,String> createReportParams(EditText comments){
        Map<String,String> params=  new HashMap<>();
        params.put("Name", name);
        params.put("LastName",lname);
        params.put("serial_number", serialNumber);
        params.put("Date", date);
        params.put("Type", type);
        params.put("Comments", comments.getText().toString());
        return params;
    }
    private void handleServerResponse() {
        Toast.makeText(Signature.this, "Datos enviados correctamente.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(), service_report.class);
        intent.putExtra("Name", name);
        intent.putExtra("SerialNumber", serialNumber);
        finish();
        startActivity(intent);
    }

    private void handleServerError(String error) {
        Toast.makeText(Signature.this, "Error al enviar datos al servidor.", Toast.LENGTH_SHORT).show();
        Log.e("SEND_DATA_ERROR", "Error: " + error);
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

}