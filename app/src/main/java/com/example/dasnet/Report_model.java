package com.example.dasnet;

public class Report_model {
    private final String date;
    private final String reportId;
    private final String machine;

    public Report_model(String date, String reportId, String machine) {
        this.date = date;
        this.reportId = reportId;
        this.machine = machine;
    }

    public String getDate() {
        return date;
    }

    public String getReportId() {
        return reportId;
    }

    public String getMachine() {
        return machine;
    }
}

