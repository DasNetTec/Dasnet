package com.example.dasnet;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class Insert_Report extends AppCompatActivity implements RecyclerViewInterface_Create {

    private TextView qrInfo;
    private String serialNumber = "";
    private static final String URL = "http://"+ Config.BASE_IP + "/dasnet2/Qr_Information.php?";
    private final ArrayList<Report_model_Create> reportModels = new ArrayList<>();
    private RT_RecyclerViewAdapter_Create adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.list_steps);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);

        serialNumber = getIntent().getStringExtra("SerialNumber");


        //                              Recycle View
        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);
        adapter = new RT_RecyclerViewAdapter_Create(this, reportModels, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //                                  UI
        qrInfo = findViewById(R.id.Machine_info);
        fetchQrInformation();


        // Configurar el listener del botón de enviar
        findViewById(R.id.button).setOnClickListener(v -> Sign());
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(Insert_Report.this, Description.class);
        intent.putExtra("StepDescription", reportModels.get(position).getStepDescription());
        TaskManager.getInstance().startActivity(this, intent);
    }

    // Método para recolectar y enviar los datos a la siguiente pantalla
    private void Sign() {
        if (allCheckBoxesChecked()) {
            String serialNumber = getIntent().getStringExtra("SerialNumber");
            String name = getIntent().getStringExtra("Name");
            String lname = getIntent().getStringExtra("LastName");
            String type = "Preventivo"; // Por ahora solo preventivo, se puede agregar pantallas para seleccionar el tipo de informe

            if (serialNumber == null || serialNumber.isEmpty() || name == null || name.isEmpty()) {
                Toast.makeText(this, "Datos incompletos. Por favor, verifica la información.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getApplicationContext(), Signature.class); // Cambiar a la actividad correcta
            intent.putExtra("SerialNumber", serialNumber);
            intent.putExtra("Name", name);
            intent.putExtra("LastName", lname);
            intent.putExtra ("Type", type);
            TaskManager.getInstance().startActivity(this, intent);
            finish();
        } else {
            Toast.makeText(this, "Por favor, seleccione todas las opciones antes de enviar los datos.", Toast.LENGTH_SHORT).show();
        }
    }

    // Método para verificar si todos los CheckBox están marcados
    private boolean allCheckBoxesChecked() {
        for (Report_model_Create report : reportModels) {
            if (!report.isChecked()) {
                return false;
            }
        }
        return true;
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
                params.put("SerialNumber", serialNumber );
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
