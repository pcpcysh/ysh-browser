package com.ysh.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class TabsActivity extends Activity {

    private GridView gridView;
    private TextView emptyText;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabs);

        gridView = findViewById(R.id.tabsGridView);
        emptyText = findViewById(R.id.emptyTabsText);
        Button closeAll = findViewById(R.id.closeAllTabsButton);
        ImageButton addBtn = findViewById(R.id.addTabButton);

        adapter = new BaseAdapter() {
            @Override public int getCount() { return TabManager.tabs.size(); }
            @Override public Object getItem(int p) { return TabManager.tabs.get(p); }
            @Override public long getItemId(int p) { return p; }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                View v;
                if (convertView == null) {
                    v = LayoutInflater.from(TabsActivity.this).inflate(R.layout.item_tab, parent, false);
                } else {
                    v = convertView;
                }

                TabManager.Tab tab = TabManager.tabs.get(position);

                TextView urlView = v.findViewById(R.id.tabUrl);
                TextView titleView = v.findViewById(R.id.tabTitle);
                ImageButton closeBtn = v.findViewById(R.id.tabCloseButton);
                ImageView thumbView = v.findViewById(R.id.tabThumbnail);

                String url = tab.url;
                if (url == null || url.isEmpty() || url.equals("about:blank") || url.contains("ysh.browser")) {
                    urlView.setText("新标签");
                } else {
                    String shortUrl = url.replace("https://", "").replace("http://", "").replace("www.", "");
                    if (shortUrl.length() > 40) shortUrl = shortUrl.substring(0, 40) + "...";
                    urlView.setText(shortUrl);
                }

                String title = tab.title == null || tab.title.isEmpty() ? "新标签" : tab.title;
                titleView.setText(title);

                if (tab.thumbnail != null) {
                    thumbView.setImageBitmap(tab.thumbnail);
                    thumbView.setVisibility(View.VISIBLE);
                } else {
                    thumbView.setImageBitmap(null);
                    thumbView.setVisibility(View.INVISIBLE);
                }

                v.setBackgroundResource(R.drawable.tab_card_bg);
                v.setAlpha((position == TabManager.currentIndex) ? 1.0f : 0.7f);

                // 点击整张卡片 → 切换标签（系统 RippleDrawable 自动响应）
                v.setClickable(true);
                v.setFocusable(true);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent result = new Intent();
                        result.putExtra("action", "switch");
                        result.putExtra("index", position);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                });

                // 关闭按钮
                closeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent result = new Intent();
                        result.putExtra("action", "close");
                        result.putExtra("index", position);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                });

                return v;
            }
        };
        gridView.setAdapter(adapter);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("action", "new");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        closeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                result.putExtra("action", "closeAll");
                setResult(RESULT_OK, result);
                finish();
            }
        });

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (TabManager.tabs.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            gridView.setVisibility(View.GONE);
        } else {
            emptyText.setVisibility(View.GONE);
            gridView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
