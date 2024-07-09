package  com.example.dasnet;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class service_report extends AppCompatActivity {

    private Button scanBtnV, scanBtnC;
    private TextView textView, textViewInfo;
    private String buttonTag;
    private String name,lname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_service_report);

        // Set status bar color
        StatusBarUtil.setStatusBarColor(this, R.color.statusBarColor);

        initializeUIElements();
        initializeAnimations();
        initializeOnClickListeners();

        name = getIntent().getStringExtra("Name");
        lname = getIntent().getStringExtra("LastName");
        textViewInfo.append(" " + name.split(" ")[0]+" "+lname);
    }

    private void initializeUIElements() {
        scanBtnV = findViewById(R.id.scanner);
        scanBtnC = findViewById(R.id.scannerC);
        textView = findViewById(R.id.textView);
        textViewInfo = findViewById(R.id.qr_info);
    }

    private void initializeAnimations() {
        LottieAnimationView animationView = findViewById(R.id.lottieAnimationView);
        animationView.setAnimation(R.raw.view);
        animationView.playAnimation();

        LottieAnimationView animationView2 = findViewById(R.id.lottieAnimationView2);
        animationView2.setAnimation(R.raw.create);
        animationView2.playAnimation();
    }

    private void initializeOnClickListeners() {
        View.OnClickListener scanButtonClickListener = v -> {
            buttonTag = v.getId() == R.id.scanner ? "scanBtnV" : "scanBtnC";
            startScanning();
        };

        scanBtnV.setOnClickListener(scanButtonClickListener);
        scanBtnC.setOnClickListener(scanButtonClickListener);
    }

    private void startScanning() {
        IntentIntegrator intentIntegrator = new IntentIntegrator(service_report.this);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setPrompt("Scan QR code");
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            handleScanResult(intentResult);
        }
    }

    private void handleScanResult(IntentResult intentResult) {
        String contents = intentResult.getContents();
        if (contents != null) {
            if ("scanBtnV".equals(buttonTag)) {
                navigateToActivity(qr_information.class, "resultadoQR", contents);
            } else if ("scanBtnC".equals(buttonTag)) {
                navigateToActivity(Insert_Report.class, "SerialNumber", contents, "Name", name,"LastName",lname);
            }
        } else {
            textView.setText("No se encontró ningún contenido en el código QR escaneado.");
        }
    }

    private void navigateToActivity(Class<?> destinationClass, String... extras) {
        Intent intent = new Intent(getApplicationContext(), destinationClass);
        for (int i = 0; i < extras.length; i += 2) {
            intent.putExtra(extras[i], extras[i + 1]);
        }
        TaskManager.getInstance().startActivity(this, intent);
    }
}
