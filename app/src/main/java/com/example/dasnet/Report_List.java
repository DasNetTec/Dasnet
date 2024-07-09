package com.example.dasnet;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Report_List extends AppCompatActivity implements RecyclerViewInterface {

    private final ArrayList<Report_model> reportModels = new ArrayList<>();
    private RT_RecyclerViewAdapter adapter;

    private String serialNumber;
    private String endDate;
    private String startDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_list);
        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);
        initializeIntentData();
        setupRecyclerView();
    }

    private void initializeIntentData() {

        serialNumber = getIntent().getStringExtra("SerialNumber");
        startDate=getIntent().getStringExtra("StartDate");
        endDate=getIntent().getStringExtra("EndDate");


    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);
        adapter = new RT_RecyclerViewAdapter(this, reportModels, startDate,endDate, serialNumber, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(Report_List.this, ReportDetail.class);
        intent.putExtra("Date", reportModels.get(position).getDate());
        intent.putExtra("Report ID", reportModels.get(position).getReportId());
        intent.putExtra("SerialNumber", serialNumber);
        TaskManager.getInstance().startActivity(this, intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        TaskManager.getInstance().handleBackNavigation(this);
    }
}