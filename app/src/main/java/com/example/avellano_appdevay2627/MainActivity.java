package com.example.avellano_appdevay2627;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.avellano_appdevay2627.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_STATION_CODE = "extra_station_code";
    public static final String EXTRA_STATUS = "extra_status";
    public static final String EXTRA_ISSUE_NOTE = "extra_issue_note";

    // Lab maintenance contact (replace with the real number)
    private static final String SUPPORT_PHONE = "tel:+631234567890";

    // Enhancement 1: sample stations the user can cycle through
    private final String[] stationCodes = {"PC-07", "PC-08", "PC-09"};
    private final String[] stationSoftware = {
            "Android Studio SDK", "Android Studio SDK", "Android Studio SDK"};
    private final String[] stationSpecs = {
            "16GB RAM \u2022 Emulator KVM \u2022 USB Port Checked",
            "8GB RAM \u2022 Emulator KVM \u2022 USB Port Checked",
            "16GB RAM \u2022 Emulator KVM \u2022 Network Checked"};
    private final String[] stationStatuses = {"Needs Check", "Ready", "Ready"};
    private final String[] stationNotes = {
            "Emulator slow to boot", "No issues reported", "No issues reported"};

    private int currentIndex = 0;

    private ActivityMainBinding binding;

    private final ActivityResultLauncher<Intent> detailLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String status = result.getData().getStringExtra(EXTRA_STATUS);
                    String note = result.getData().getStringExtra(EXTRA_ISSUE_NOTE);
                    stationStatuses[currentIndex] = status;
                    stationNotes[currentIndex] = note;
                    refreshBanner();
                    Toast.makeText(this,
                            "Updated " + stationCodes[currentIndex] + " -> " + status,
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        refreshBanner();

        // Explicit Intent: open StationDetailActivity for the currently selected station
        binding.btnInspectStation.setOnClickListener(v -> {
            Intent intent = new Intent(this, StationDetailActivity.class);
            intent.putExtra(EXTRA_STATION_CODE, stationCodes[currentIndex]);
            intent.putExtra(EXTRA_STATUS, stationStatuses[currentIndex]);
            intent.putExtra(EXTRA_ISSUE_NOTE, stationNotes[currentIndex]);
            detailLauncher.launch(intent);
        });

        // Implicit Intent: share the station summary
        binding.btnShareReport.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "LabCheck Report: " + stationCodes[currentIndex]);
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Station: " + stationCodes[currentIndex]
                            + "\nStatus: " + stationStatuses[currentIndex]
                            + "\nNote: " + stationNotes[currentIndex]);
            startActivity(Intent.createChooser(shareIntent, "Share Station Status"));
        });

        // Enhancement 1: cycle PC-07 -> PC-08 -> PC-09 -> PC-07 ...
        binding.btnSwitchStation.setOnClickListener(v -> {
            currentIndex = (currentIndex + 1) % stationCodes.length;
            refreshBanner();
            Toast.makeText(this, "Selected " + stationCodes[currentIndex],
                    Toast.LENGTH_SHORT).show();
        });

        // Enhancement 2: implicit Intent (ACTION_DIAL) to call lab maintenance
        binding.btnCallSupport.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(SUPPORT_PHONE));
            try {
                startActivity(dialIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "No dialer app available on this device.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Re-draws the RelativeLayout banner for the selected station. */
    private void refreshBanner() {
        String status = stationStatuses[currentIndex];
        binding.tvStationCode.setText(
                stationCodes[currentIndex] + " \u2022 " + stationSoftware[currentIndex]);
        binding.tvStationStatus.setText(status);
        binding.tvStationStatus.setTextColor(statusColor(status));
        binding.tvStationSpec.setText("Note: " + stationNotes[currentIndex]
                + "\n" + stationSpecs[currentIndex]);
    }

    private int statusColor(String status) {
        if ("Ready".equals(status)) return Color.parseColor("#2E7D32");
        if ("Issue Logged".equals(status)) return Color.parseColor("#B26A00");
        return Color.parseColor("#B26A00"); // Needs Check
    }
}