package com.example.dasnet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignatureView extends View {

    private static final String TAG = "SignatureView";
    private static final int DESIRED_WIDTH = 800;
    private static final int DESIRED_HEIGHT = 100;
    private static final int MAX_CHARACTERS_PER_LINE = 30;

    private Paint paint;
    private Path path;
    private Bitmap bitmap;
    private Canvas canvas;
    private EditText editText;

    public SignatureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializePaint();
        setupEditText();
    }

    private void initializePaint() {
        paint = new Paint();
        path = new Path();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(5f);
    }

    private void setupEditText() {
        editText = new EditText(getContext());
        editText.setMaxLines(3);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                editText.removeTextChangedListener(this);

                // Auto-insert newline every MAX_CHARACTERS_PER_LINE characters
                int length = s.length();
                for (int i = MAX_CHARACTERS_PER_LINE; i < length; i += MAX_CHARACTERS_PER_LINE + 1) {
                    if (s.charAt(i) != '\n') {
                        s.insert(i, "\n");
                        length = s.length(); // Update length of text
                    }
                }

                editText.addTextChangedListener(this);
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initializeCanvas(w, h);
    }

    private void initializeCanvas(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        handleTouchEvent(event.getAction(), x, y);
        invalidate();
        return true;
    }

    private void handleTouchEvent(int action, float x, float y) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                break;
            default:
                break;
        }
    }

    public void clear() {
        path.reset();
        bitmap.eraseColor(Color.WHITE);
        invalidate();
        Log.d(TAG, "Canvas cleared");
    }

    private Bitmap resizeBitmap(Bitmap original, int width, int height) {
        return Bitmap.createScaledBitmap(original, width, height, true);
    }

    public boolean saveToFile(Context context, String fileName) {
        Bitmap resizedBitmap = resizeBitmap(bitmap, DESIRED_WIDTH, DESIRED_HEIGHT);
        return saveBitmapToFile(context, fileName, resizedBitmap);
    }

    private boolean saveBitmapToFile(Context context, String fileName, Bitmap bitmap) {
        File file = new File(context.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d(TAG, "File saved successfully:" + file.getAbsolutePath());
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error saving file:" + e.getMessage(), e);
            return false;
        }
    }

    public boolean saveAndUploadFile(Context context, String fileName) {
        if (saveToFile(context, fileName)) {
            Log.d(TAG, "File saved, proceeding to upload:" + fileName);
            new UploadFileTask(context).execute(fileName);
            return true;
        } else {
            Log.e(TAG, "File save failed,aborting upload:" + fileName);
            return false;
        }
    }

    private class UploadFileTask extends AsyncTask<String, Void, Boolean> {
        private Context context;

        public UploadFileTask(Context context) {
            this.context = context;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            return uploadFile(params[0]);
        }

        private boolean uploadFile(String fileName) {
            File file = new File(context.getFilesDir(), fileName);
            Log.d(TAG, "Preparing to upload file: " + file.getAbsolutePath());

            OkHttpClient client = new OkHttpClient();
            RequestBody fileBody = RequestBody.create(file, MediaType.parse("image/png"));
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url("http://"+ Config.BASE_IP + "/dasnet2/impagen.php")
                    .post(requestBody)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Upload failed with response code:" + response.code());
                    throw new IOException("Unexpected code" + response);
                }
                Log.d(TAG, "File uploaded successfully");
                return true;
            } catch (IOException e) {
                Log.e(TAG, "Error uploading file: " + e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            String message = success ? "File uploaded successfully" : "File uploaded failed";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}