package com.example.dasnet;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import java.util.Stack;

public class TaskManager {

    private static TaskManager instance;
    private Stack<Intent> intentStack;

    private TaskManager() {
        intentStack = new Stack<>();
    }

    public static TaskManager getInstance() {
        if (instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    public void pushIntent(Intent intent) {
        intentStack.push(intent);
    }

    public void popIntent() {
        if (!intentStack.isEmpty()) {
            intentStack.pop();
        }
    }

    public void startActivity(Activity activity, Intent intent) {
        pushIntent(intent);
        activity.startActivity(intent);
    }

    public void handleBackNavigation(Activity activity) {
        popIntent();
        if (!intentStack.isEmpty()) {
            Intent previousIntent = intentStack.peek();
            activity.startActivity(previousIntent);
        } else {
            activity.finish();
        }
    }
}
