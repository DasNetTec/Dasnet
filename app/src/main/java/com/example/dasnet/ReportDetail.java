package com.example.dasnet;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.barteksc.pdfviewer.PDFView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReportDetail extends AppCompatActivity {
    private PDFView pdfView;
    private String date, serialNumber, reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_detail);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);
        pdfView = findViewById(R.id.pdfView);

        retrieveIntentExtras();

        String fileName = generateFileName();
        Log.d("FileNameDebug", "File Name: " + fileName);

        retrievePdfFromServer(fileName);
    }




    private void retrieveIntentExtras() {
        date = getIntent().getStringExtra("Date");
        serialNumber = getIntent().getStringExtra("SerialNumber");
        reportId = getIntent().getStringExtra("Report ID");
    }

    private String generateFileName() {
        return "ServiceReport_" + reportId + "_" + serialNumber + "_" + date + ".pdf";
    }

    private void retrievePdfFromServer(String fileName) {
        String apiUrl = "http://"+ Config.BASE_IP + "/dasnet2/Report/" + fileName;

        new Thread(() -> {
            try {
                HttpURLConnection connection = createHttpUrlConnection(apiUrl);
                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    byte[] pdfData = readInputStream(connection.getInputStream());
                    displayPdf(pdfData);
                } else {
                    handleUnsuccessfulResponse(responseCode);
                }
            } catch (IOException e) {
                handleNetworkError(e);
            }
        }).start();
    }

    private HttpURLConnection createHttpUrlConnection(String apiUrl) throws IOException {
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        return connection;
    }

    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        return outputStream.toByteArray();
    }

    private void displayPdf(byte[] pdfData) {
        runOnUiThread(() -> pdfView.fromBytes(pdfData).load());
    }

    private void handleUnsuccessfulResponse(int responseCode) {
        runOnUiThread(() -> Toast.makeText(ReportDetail.this, "Failed to retrieve PDF: " + responseCode, Toast.LENGTH_SHORT).show());
    }

    private void handleNetworkError(IOException e) {
        e.printStackTrace();
        runOnUiThread(() -> Toast.makeText(ReportDetail.this, "Network error", Toast.LENGTH_SHORT).show());
    }
}
