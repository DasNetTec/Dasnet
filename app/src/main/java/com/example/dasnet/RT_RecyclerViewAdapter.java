package com.example.dasnet;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class RT_RecyclerViewAdapter extends RecyclerView.Adapter<RT_RecyclerViewAdapter.MyViewHolder> {
    private final RecyclerViewInterface recyclerViewInterface;
    private final Context context;
    private final ArrayList<Report_model> reportModels;

    public RT_RecyclerViewAdapter(Context context, ArrayList<Report_model> reportModels, String startDate, String endDate, String serialNumber, RecyclerViewInterface recyclerViewInterface) {
        this.context = context;
        this.reportModels = reportModels; // Assign the provided list
        this.recyclerViewInterface = recyclerViewInterface;
        // Here you can call the method to fetch data from the server
        fetchDataFromServer(serialNumber, startDate, endDate);
    }

    private void fetchDataFromServer(String serialNumber, String startDate, String endDate) {
        // Perform asynchronous task to fetch data from the server here
        new FetchDataTask().execute(serialNumber, startDate, endDate);
    }

    private class FetchDataTask extends AsyncTask<String, Void, ArrayList<Report_model>> {
        @Override
        protected ArrayList<Report_model> doInBackground(String... params) {
            ArrayList<Report_model> dataList = new ArrayList<>();
            try {
                String serialNumber = params[0];
                String startDate = params[1];
                String endDate = params[2];

                // URL of the PHP script that returns JSON
                URL url = new URL("http://"+ Config.BASE_IP + "/dasnet2/php/Qr_Date_Range.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Build request parameters
                String postData = "SerialNumber=" + serialNumber + "&StartDate=" + startDate + "&EndDate=" + endDate;

                // Write parameters to the connection
                connection.getOutputStream().write(postData.getBytes());

                // Connect and get response from the server
                connection.connect();
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                String json = stringBuilder.toString();
                JSONArray jsonArray = new JSONArray(json);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String report_id = jsonObject.getString("report_id");
                    String machine = jsonObject.getString("machine_type");
                    String dateResult = jsonObject.getString("Date");

                    // Assuming the image is always the same
                    int image = R.drawable.baseline_content_paste_24;
                    dataList.add(new Report_model(dateResult, report_id, machine));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return dataList;
        }

        @Override
        protected void onPostExecute(ArrayList<Report_model> dataList) {
            super.onPostExecute(dataList);
            // Update adapter data with the ones obtained from the server
            reportModels.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public RT_RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout of the RecyclerView row
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_row, parent, false);
        return new MyViewHolder(view, recyclerViewInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull RT_RecyclerViewAdapter.MyViewHolder holder, int position) {
        // Set data to the views in the RecyclerView row
        Report_model report = reportModels.get(position);
        holder.tvDate.setText(report.getDate());
        holder.tvRID.setText(report.getReportId());
        holder.tvMachine.setText(report.getMachine());
        holder.imageView.setImageResource(R.drawable.baseline_content_paste_24);
    }

    @Override
    public int getItemCount() {
        // Return the number of items in the list
        return reportModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // Views in the RecyclerView row
        ImageView imageView;
        TextView tvDate, tvRID, tvMachine;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface recyclerViewInterface) {
            super(itemView);
            // Initialize views
            imageView = itemView.findViewById(R.id.imageView);
            tvDate = itemView.findViewById(R.id.textView);
            tvRID = itemView.findViewById(R.id.textView2);
            tvMachine = itemView.findViewById(R.id.textView3);

            // Set click on the RecyclerView row
            itemView.setOnClickListener(v -> {
                if (recyclerViewInterface != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterface.onItemClick(pos);
                    }
                }
            });
        }
    }
}
