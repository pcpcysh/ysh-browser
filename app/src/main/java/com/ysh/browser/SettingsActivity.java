package com.ysh.browser;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

        final Switch historySwitch = findViewById(R.id.historySwitch);
        final Switch cookieSwitch = findViewById(R.id.cookieSwitch);
        final Switch incognitoSwitch = findViewById(R.id.incognitoSwitch);
        final Button saveBtn = findViewById(R.id.saveSettingsButton);

        final RadioGroup searchGroup = findViewById(R.id.searchRadioGroup);
        final EditText customSearchEdit = findViewById(R.id.customSearchEdit);

        historySwitch.setChecked(prefs.getBoolean("history_enabled", true));
        cookieSwitch.setChecked(prefs.getBoolean("cookie_enabled", true));
        incognitoSwitch.setChecked(prefs.getBoolean("incognito", false));

        historySwitch.setEnabled(!incognitoSwitch.isChecked());
        cookieSwitch.setEnabled(!incognitoSwitch.isChecked());

        String engine = prefs.getString("search_engine", "https://www.google.com/search?q=");
        if (engine.contains("google")) {
            searchGroup.check(R.id.radioGoogle);
        } else if (engine.contains("bing")) {
            searchGroup.check(R.id.radioBing);
        } else if (engine.contains("yandex")) {
            searchGroup.check(R.id.radioYandex);
        } else {
            searchGroup.check(R.id.radioCustomSearch);
            customSearchEdit.setVisibility(View.VISIBLE);
            customSearchEdit.setText(engine);
        }

        searchGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    customSearchEdit.setVisibility(checkedId == R.id.radioCustomSearch ? View.VISIBLE : View.GONE);
                }
            });

        incognitoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton b, boolean checked) {
                    if (checked) {
                        historySwitch.setChecked(false);
                        cookieSwitch.setChecked(false);
                    }
                    historySwitch.setEnabled(!checked);
                    cookieSwitch.setEnabled(!checked);
                }
            });

        saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor e = prefs.edit();
                    boolean incog = incognitoSwitch.isChecked();

                    e.putBoolean("dark_mode", true);
                    e.putBoolean("incognito", incog);
                    e.putBoolean("history_enabled", !incog && historySwitch.isChecked());
                    e.putBoolean("cookie_enabled", !incog && cookieSwitch.isChecked());

                    int id = searchGroup.getCheckedRadioButtonId();
                    String eng;
                    if (id == R.id.radioGoogle) {
                        eng = "https://www.google.com/search?q=";
                    } else if (id == R.id.radioBing) {
                        eng = "https://www.bing.com/search?q=";
                    } else if (id == R.id.radioYandex) {
                        eng = "https://yandex.com/search/?text=";
                    } else {
                        eng = customSearchEdit.getText().toString().trim();
                        if (eng.isEmpty()) {
                            Toast.makeText(SettingsActivity.this, "请输入自定义搜索引擎URL", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    e.putString("search_engine", eng);
                    e.apply();

                    Toast.makeText(SettingsActivity.this, "设置已保存", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
