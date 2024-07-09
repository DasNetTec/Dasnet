package com.example.dasnet;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class RT_RecyclerViewAdapter_Create extends RecyclerView.Adapter<RT_RecyclerViewAdapter_Create.MyViewHolder> {
    private final RecyclerViewInterface_Create recyclerViewInterfaceCreate;
    private final Context context;
    private final ArrayList<Report_model_Create> reportModels;

    public RT_RecyclerViewAdapter_Create(Context context, ArrayList<Report_model_Create> reportModels, RecyclerViewInterface_Create recyclerViewInterfaceCreate) {
        this.context = context;
        this.reportModels = reportModels;
        this.recyclerViewInterfaceCreate = recyclerViewInterfaceCreate;
        fetchDataFromServer();
    }

    private void fetchDataFromServer() {
        new FetchDataTask().execute();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, ArrayList<Report_model_Create>> {
        @Override
        protected ArrayList<Report_model_Create> doInBackground(Void... params) {
            ArrayList<Report_model_Create> dataList = new ArrayList<>();
            try {
                URL url = new URL("http://"+ Config.BASE_IP + "/dasnet2/Insignum.php");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                String json = stringBuilder.toString();
                Log.d("ServerResponse", "Response from server: " + json);

                JSONObject jsonObject = new JSONObject(json);

                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String description = jsonObject.getString(key);
                    dataList.add(new Report_model_Create(key, description));
                }
            } catch (IOException | JSONException e) {
                Log.e("FetchDataTask", "Error fetching data: " + e.getMessage(), e);
            }
            return dataList;
        }

        @Override
        protected void onPostExecute(ArrayList<Report_model_Create> dataList) {
            super.onPostExecute(dataList);
            reportModels.clear();
            reportModels.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_row_create, parent, false);
        return new MyViewHolder(view, recyclerViewInterfaceCreate);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Report_model_Create report = reportModels.get(position);
        holder.stepDescriptionTextView.setText(report.getStepDescription());
        holder.imageView.setImageResource(R.drawable.viewfix);

        // Establecer el estado del CheckBox basado en el modelo de datos
        holder.checkBox.setOnCheckedChangeListener(null); // Desactivar el listener antes de actualizar el estado
        holder.checkBox.setChecked(report.isChecked());

        // Configurar el listener para actualizar el estado del CheckBox en el modelo de datos
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> report.setChecked(isChecked));
    }

    @Override
    public int getItemCount() {
        return reportModels.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView stepDescriptionTextView;
        CheckBox checkBox;

        public MyViewHolder(@NonNull View itemView, RecyclerViewInterface_Create recyclerViewInterfaceCreate) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            stepDescriptionTextView = itemView.findViewById(R.id.textView);
            checkBox = itemView.findViewById(R.id.checkBox);

            itemView.setOnClickListener(v -> {
                if (recyclerViewInterfaceCreate != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        recyclerViewInterfaceCreate.onItemClick(pos);
                    }
                }
            });
        }
    }
}
