package com.example.dasnet;

public class Report_model_Create {
    private final String stepNumber;
    private final String stepDescription;
    private boolean isChecked;

    public Report_model_Create(String stepNumber, String stepDescription) {
        this.stepNumber = stepNumber;
        this.stepDescription = stepDescription;
        this.isChecked = false;
    }

    public String getStepNumber() {
        return stepNumber;
    }

    public String getStepDescription() {
        return stepDescription;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        this.isChecked = checked;
    }
}
