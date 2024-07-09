package com.example.dasnet;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class NFCHandler {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] writingTagFilters;
    private Context context;
    private String url;
    private Activity activity;

    public NFCHandler(Activity activity, Context context, String url) {
        this.activity = activity;
        this.context = context;
        this.url = url;

        initializeNFCAdapter();
        initializePendingIntent();
        initializeIntentFilters();
    }

    private void initializeNFCAdapter() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(context);
        if (nfcAdapter == null) {
            showToast("Este dispositivo no admite NFC");
            activity.finish();
        }
    }

    private void initializePendingIntent() {
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flags |= PendingIntent.FLAG_MUTABLE;
        }
        pendingIntent = PendingIntent.getActivity(context, 0,
                new Intent(context, activity.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), flags);
    }

    private void initializeIntentFilters() {
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writingTagFilters = new IntentFilter[]{tagDetected};
    }

    public void enableForegroundDispatch() {
        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, writingTagFilters, null);
    }

    public void disableForegroundDispatch() {
        nfcAdapter.disableForegroundDispatch(activity);
    }

    public void handleNfcIntent(Intent intent) {
        if (isNfcIntent(intent)) {
            NdefMessage[] msgs = getNdefMessages(intent);
            handleNfcMessage(msgs);
        }
    }

    private boolean isNfcIntent(Intent intent) {
        String action = intent.getAction();
        return NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action);
    }

    private NdefMessage[] getNdefMessages(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            NdefMessage[] msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
            return msgs;
        }
        return null;
    }

    private void handleNfcMessage(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) {
            showToast("No hay contenido NFC");
            return;
        }
        String hexFromCard = extractHexFromNfcTag(msgs);
        verifyNfcTag(hexFromCard);
    }

    private String extractHexFromNfcTag(NdefMessage[] msgs) {
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        Log.d("NFC_TAG", "Raw Payload: " + new String(payload));

        int start = (payload[0] == 0x02) ? 1 : 0;
        byte[] actualData = new byte[payload.length - start];
        System.arraycopy(payload, start, actualData, 0, payload.length - start);
        String extractedHex = bytesToHex(actualData);
        Log.d("NFC_TAG", "Extracted Hex: " + extractedHex);
        return extractedHex;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    private void verifyNfcTag(final String hexData) {
        final ProgressDialog progressDialog = showProgressDialog("Verificando NFC...");

        StringRequest request = createVerificationRequest(hexData, progressDialog);
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        requestQueue.add(request);
    }

    private StringRequest createVerificationRequest(final String hexData, final ProgressDialog progressDialog) {
        return new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        handleVerificationResponse(response, hexData);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        showToast("Error de conexión: " + error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("NFC", hexData);
                return params;
            }
        };
    }

    private void handleVerificationResponse(String response, String hexData) {
        Log.d("NFCHandler", "Server response: " + response);
        if (!response.equalsIgnoreCase("no pudo ingresar")) {
            try {
                // Parse the JSON response
                JSONArray jsonArray = new JSONArray(response);
                String name = jsonArray.getString(0);
                String lastName = jsonArray.getString(1);

                navigateToServiceReport(name, lastName);
                showToast("Welcome: " + name + " " + lastName);

            } catch (JSONException e) {
                e.printStackTrace();
                showToast("Error parsing response");
                Log.e("NFCHandler", "Error parsing response: " + response, e);
            }
        } else {
            Log.d("NFC_TAG", "Hex Data: " + hexData);
            showToast("Authentication failed");
        }
    }

    private void navigateToServiceReport(String name, String lastName) {
        Intent intent = new Intent(context, service_report.class);
        intent.putExtra("Name", name);
        intent.putExtra("LastName", lastName);
        TaskManager.getInstance().startActivity(activity, intent);
    }


    private ProgressDialog showProgressDialog(String message) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(message);
        progressDialog.show();
        return progressDialog;
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}