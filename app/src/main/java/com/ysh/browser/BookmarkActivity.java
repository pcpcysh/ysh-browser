package com.ysh.browser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BookmarkActivity extends Activity {
    private List<BookmarkHelper.Item> items = new ArrayList<>();
    private BookmarkHelper helper;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        ListView listView = findViewById(R.id.bookmarkListView);
        Button clearBtn = findViewById(R.id.clearBookmarkButton);
        helper = new BookmarkHelper(this);
        items = helper.getAll();

        adapter = new BaseAdapter() {
            @Override public int getCount() { return items.size(); }
            @Override public Object getItem(int p) { return items.get(p); }
            @Override public long getItemId(int p) { return items.get(p).id; }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, parent, false);
                }
                TextView t1 = convertView.findViewById(android.R.id.text1);
                TextView t2 = convertView.findViewById(android.R.id.text2);
                BookmarkHelper.Item it = items.get(position);
                t1.setText(it.title);
                t2.setText(it.url);
                t1.setTextColor(0xFFE0E0E0);
                t2.setTextColor(0xFF888888);
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                    Intent i = new Intent(BookmarkActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("load_url", items.get(pos).url);
                    startActivity(i);
                    finish();
                }
            });

        clearBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helper.clearAll();
                    items.clear();
                    adapter.notifyDataSetChanged();
                    Toast.makeText(BookmarkActivity.this, "已清空", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
