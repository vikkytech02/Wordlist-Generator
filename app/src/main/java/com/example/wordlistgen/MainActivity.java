package com.example.wordlistgen;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    private EditText etTokens, etYears, etSymbols, etMaxComb, etMaxLines;
    private CheckBox cbLeet, cbReverse;
    private Button btnGenerate, btnStop;
    private TextView tvStatus, tvFilePath;
    private volatile boolean cancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etTokens = findViewById(R.id.etTokens);
        etYears = findViewById(R.id.etYears);
        etSymbols = findViewById(R.id.etSymbols);
        etMaxComb = findViewById(R.id.etMaxComb);
        etMaxLines = findViewById(R.id.etMaxLines);
        cbLeet = findViewById(R.id.cbLeet);
        cbReverse = findViewById(R.id.cbReverse);
        btnGenerate = findViewById(R.id.btnGenerate);
        btnStop = findViewById(R.id.btnStop);
        tvStatus = findViewById(R.id.tvStatus);
        tvFilePath = findViewById(R.id.tvFilePath);

        btnGenerate.setOnClickListener(v -> startGeneration());
        btnStop.setOnClickListener(v -> cancelled = true);
    }

    private void startGeneration() {
        cancelled = false;
        tvStatus.setText("Generating...");

        new Thread(() -> {
            try {
                // Parse and trim user inputs
                List<String> tokens = parseListTrimmed(etTokens.getText().toString());
                if (tokens.isEmpty()) {
                    runOnUiThread(() -> tvStatus.setText("❌ Enter tokens (e.g. Vikas, Yadav)"));
                    return;
                }

                List<String> yearsRaw = parseListTrimmed(etYears.getText().toString());   // e.g. 2001,2025
                List<String> symbolsRaw = parseListTrimmed(etSymbols.getText().toString()); // e.g. !,@,_

                int maxComb = parseInt(etMaxComb.getText().toString(), 2);
                long maxLines = parseLongOrDefault(etMaxLines.getText().toString(), 50000L);
                boolean leet = cbLeet.isChecked();
                boolean reverse = cbReverse.isChecked();

                // Prepare separators between token and year
                List<String> separators = new ArrayList<>();
                separators.add("");   // no separator: Vikas2001
                separators.add(" ");  // space: Vikas 2001
                separators.add("_");  // underscore: Vikas_2001
                // add any user-provided symbols as separators too
                for (String s : symbolsRaw) {
                    if (!s.isEmpty() && !separators.contains(s)) separators.add(s);
                }

                // Build year variants that include separators before the year.
                List<String> yearsVariants = buildYearVariants(yearsRaw, separators);

                // Keep symbols as suffixes (so Generator will produce v + yearVariant + symbolSuffix)
                List<String> symbolSuffixes = symbolsRaw.isEmpty() ? null : symbolsRaw;

                // Safety: if yearsVariants is empty set to null (Generator handles null)
                if (yearsVariants.isEmpty()) yearsVariants = null;

                // Create file via MediaStore (Documents/WordlistGen)
                Uri fileUri = createFile("custom_wordlist.txt");
                if (fileUri == null) {
                    runOnUiThread(() -> tvStatus.setText("❌ Could not create file"));
                    return;
                }

                try (OutputStream os = getContentResolver().openOutputStream(fileUri)) {
                    if (os == null) {
                        runOnUiThread(() -> tvStatus.setText("❌ Could not open output stream"));
                        return;
                    }

                    final OutputStream finalOs = os;
                    Generator.Callback cb = new Generator.Callback() {
                        private long produced = 0;

                        @Override
                        public void onWord(String w) {
                            if (cancelled) return;
                            try {
                                finalOs.write((w + "\n").getBytes(StandardCharsets.UTF_8));
                            } catch (Exception ignored) {}
                            produced++;
                            if (produced % 500 == 0) {
                                runOnUiThread(() -> tvStatus.setText("Produced: " + produced));
                            }
                        }

                        @Override
                        public boolean isCancelled() {
                            return cancelled;
                        }
                    };

                    long total = Generator.generate(tokens, yearsVariants, symbolSuffixes,
                            maxComb, leet, reverse, maxLines, cb);

                    os.flush();

                    long finalTotal = total;
                    runOnUiThread(() -> {
                        tvStatus.setText("✅ Done! Produced: " + finalTotal);
                        tvFilePath.setText("Saved in: Documents/WordlistGen/custom_wordlist.txt");
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> tvStatus.setText("❌ Error writing file: " + e.getMessage()));
                }

            } catch (Exception e) {
                runOnUiThread(() -> tvStatus.setText("❌ Error: " + e.getMessage()));
            }
        }).start();
    }

    private Uri createFile(String name) {
        try {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
            values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
            values.put(MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_DOCUMENTS + "/WordlistGen");
            Uri collection = MediaStore.Files.getContentUri("external");
            return resolver.insert(collection, values);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- Helper methods ---

    // Parse comma-separated list and trim items, ignore empty ones
    private List<String> parseListTrimmed(String s) {
        List<String> out = new ArrayList<>();
        if (s == null) return out;
        for (String p : s.split(",")) {
            String t = p.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    // Build year variants including separators before the year
    private List<String> buildYearVariants(List<String> years, List<String> separators) {
        List<String> out = new ArrayList<>();
        if (years == null || years.isEmpty()) return out;
        for (String y : years) {
            if (y == null || y.trim().isEmpty()) continue;
            String yy = y.trim();
            out.add(yy); // bare year
            for (String sep : separators) {
                if (sep == null || sep.isEmpty()) continue;
                out.add(sep + yy); // e.g. "_2001", " 2001", "@2001"
            }
        }
        return out;
    }

    private long parseLongOrDefault(String s, long def) {
        try { return Long.parseLong(s.trim()); }
        catch (Exception e) { return def; }
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return def; }
    }
}
