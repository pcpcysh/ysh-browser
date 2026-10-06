package com.ysh.browser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    private FrameLayout webViewContainer;
    private EditText urlEditText;
    private Button tabButton;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

        webViewContainer = findViewById(R.id.webViewContainer);
        urlEditText = findViewById(R.id.urlEditText);
        Button goButton = findViewById(R.id.goButton);
        Button menuButton = findViewById(R.id.menuButton);
        tabButton = findViewById(R.id.tabButton);

        applyCookieMode();

        // 恢复已有标签
        if (TabManager.tabs.isEmpty()) {
            // 没有标签：加载空白页
            String loadUrl = getIntent().getStringExtra("load_url");
            if (loadUrl == null || loadUrl.isEmpty() || loadUrl.equals("about:blank")) {
                createNewTabWithHome();
            } else {
                createNewTab(loadUrl, true);
            }
        } else {
            for (TabManager.Tab t : TabManager.tabs) {
                if (t.webView.getParent() != null) {
                    ((android.view.ViewGroup) t.webView.getParent()).removeView(t.webView);
                }
                webViewContainer.addView(t.webView);
                t.webView.setVisibility(View.GONE);
            }
            if (TabManager.currentIndex >= TabManager.tabs.size()) {
                TabManager.currentIndex = TabManager.tabs.size() - 1;
            }
            if (TabManager.currentIndex < 0) TabManager.currentIndex = 0;
            switchToTab(TabManager.currentIndex);
        }

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUrlOrSearch();
                hideKeyboard();
            }
        });

        urlEditText.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(android.widget.TextView v, int actionId, android.view.KeyEvent event) {
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_GO) {
                    loadUrlOrSearch();
                    hideKeyboard();
                    return true;
                }
                return false;
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        tabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TabManager.currentIndex >= 0) updateThumbnail(TabManager.currentIndex);
                Intent intent = new Intent(MainActivity.this, TabsActivity.class);
                startActivityForResult(intent, 1001);
            }
        });
    }

    private void createNewTab(String url, boolean switchTo) {
        final WebView wv = new WebView(this);
        wv.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String u) {
                try {
                    if (u.startsWith("http://") || u.startsWith("https://")) {
                        view.loadUrl(u);
                        return true;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
                    startActivity(intent);
                } catch (Exception e) {}
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String u) {
                super.onPageFinished(view, u);
                try {
                    if (u != null && !u.contains("ysh.browser") && !u.equals("about:blank")) {
                        saveHistory(view.getTitle(), u);
                    }
                    // 更新 TabManager 里的 url/title
                    for (int i = 0; i < TabManager.tabs.size(); i++) {
                        if (TabManager.tabs.get(i).webView == view) {
                            TabManager.tabs.get(i).url = u;
                            TabManager.tabs.get(i).title = view.getTitle();
                            break;
                        }
                    }
                    if (TabManager.currentIndex < TabManager.tabs.size()
                            && TabManager.tabs.get(TabManager.currentIndex).webView == view) {
                        if (u == null || u.equals("about:blank")) {
                            urlEditText.setText("");
                        } else {
                            urlEditText.setText(u);
                        }
                    }
                    updateTabButton();
                } catch (Exception e) {}
            }
        });

        wv.loadUrl(url);
        webViewContainer.addView(wv);
        wv.setVisibility(View.GONE);

        TabManager.Tab tab = new TabManager.Tab(wv, url, "新标签");
        TabManager.tabs.add(tab);

        if (switchTo) {
            switchToTab(TabManager.tabs.size() - 1);
        }
        updateTabButton();
    }


    private void createNewTabWithHome() {
        final WebView wv = new WebView(this);
        wv.setLayoutParams(new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        WebSettings s = wv.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);

        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String u) {
                try {
                    if (u.startsWith("http://") || u.startsWith("https://")) {
                        view.loadUrl(u);
                        return true;
                    }
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(u));
                    startActivity(intent);
                } catch (Exception e) {}
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String u) {
                super.onPageFinished(view, u);
                try {
                    if (u != null && !u.contains("ysh.browser") && !u.equals("about:blank")) {
                        saveHistory(view.getTitle(), u);
                    }
                    for (int i = 0; i < TabManager.tabs.size(); i++) {
                        if (TabManager.tabs.get(i).webView == view) {
                            TabManager.tabs.get(i).url = u;
                            TabManager.tabs.get(i).title = view.getTitle();
                            break;
                        }
                    }
                    if (TabManager.currentIndex < TabManager.tabs.size()
                            && TabManager.tabs.get(TabManager.currentIndex).webView == view) {
                        if (u == null || u.equals("about:blank") || u.contains("ysh.browser")) {
                            urlEditText.setText("");
                        } else {
                            urlEditText.setText(u);
                        }
                    }
                    updateTabButton();
                } catch (Exception e) {}
            }
        });

        wv.loadDataWithBaseURL("about:blank", getHomeHtml(), "text/html", "UTF-8", null);

        webViewContainer.addView(wv);
        wv.setVisibility(View.GONE);

        TabManager.Tab tab = new TabManager.Tab(wv, "", "新标签");
        TabManager.tabs.add(tab);
        switchToTab(TabManager.tabs.size() - 1);
        updateTabButton();
    }

    private String getHomeHtml() {
        StringBuilder cards = new StringBuilder();
        try {
            BookmarkHelper helper = new BookmarkHelper(this);
            java.util.List<BookmarkHelper.Item> bookmarks = helper.getAll();
            helper.close();

            if (bookmarks != null && !bookmarks.isEmpty()) {
                int count = Math.min(bookmarks.size(), 8);
                for (int i = 0; i < count; i++) {
                    BookmarkHelper.Item item = bookmarks.get(i);
                    String title = item.title != null && !item.title.isEmpty() ? item.title : item.url;
                    String host = "";
                    try {
                        java.net.URL u2 = new java.net.URL(item.url);
                        host = u2.getHost();
                    } catch (Exception e) {}
                    String favicon = "https://favicon.im/" + host;
                    cards.append("<a class='card' href='").append(item.url).append("'>");
                    cards.append("<img class='favicon' src='").append(favicon).append("'/>");
                    cards.append("<span class='name'>").append(escapeHtml(title)).append("</span>");
                    cards.append("</a>");
                }
            } else {
                cards.append(homeCard("https://www.baidu.com", "https://www.baidu.com", "百度"));
                cards.append(homeCard("https://www.bilibili.com", "https://www.bilibili.com", "哔哩哔哩"));
                cards.append(homeCard("https://www.zhihu.com", "https://www.zhihu.com", "知乎"));
                cards.append(homeCard("https://github.com", "https://github.com", "GitHub"));
            }
        } catch (Exception e) {
            cards.append(homeCard("https://www.baidu.com", "https://www.baidu.com", "百度"));
        }

        return "<!DOCTYPE html>"
            + "<html><head>"
            + "<meta name='viewport' content='width=device-width,initial-scale=1'>"
            + "<style>"
            + "* { box-sizing: border-box; margin: 0; padding: 0; }"
            + "body {"
            + "  font-family: -apple-system, 'Roboto', 'Segoe UI', sans-serif;"
            + "  background: linear-gradient(180deg, #121212 0%, #1a1a2e 100%);"
            + "  color: #E0E0E0; min-height: 100vh;"
            + "  display: flex; flex-direction: column; align-items: center;"
            + "  padding: 40px 20px;"
            + "}"
            + ".logo {"
            + "  width: 80px; height: 80px; border-radius: 20px;"
            + "  background: linear-gradient(135deg, #BB86FC 0%, #03DAC6 100%);"
            + "  display: flex; align-items: center; justify-content: center;"
            + "  font-size: 40px; font-weight: bold; color: #000;"
            + "  box-shadow: 0 8px 32px rgba(187, 134, 252, 0.3);"
            + "  margin-bottom: 24px;"
            + "}"
            + "h1 {"
            + "  font-size: 32px; font-weight: 300; letter-spacing: 2px;"
            + "  background: linear-gradient(90deg, #BB86FC, #03DAC6);"
            + "  -webkit-background-clip: text; -webkit-text-fill-color: transparent;"
            + "  background-clip: text; margin-bottom: 8px;"
            + "}"
            + ".subtitle {"
            + "  color: #888; font-size: 14px; margin-bottom: 40px; letter-spacing: 1px;"
            + "}"
            + ".shortcuts {"
            + "  display: grid; grid-template-columns: repeat(4, 1fr);"
            + "  gap: 12px; width: 100%; max-width: 480px;"
            + "}"
            + ".card {"
            + "  background: rgba(30, 30, 30, 0.8); border-radius: 16px;"
            + "  padding: 12px 8px; text-decoration: none; color: #E0E0E0;"
            + "  transition: all 0.15s; border: 1px solid rgba(255, 255, 255, 0.05);"
            + "  display: flex; flex-direction: column; align-items: center; gap: 6px;"
            + "  min-height: 80px; -webkit-tap-highlight-color: transparent;"
            + "}"
            + ".card:active {"
            + "  background: rgba(187, 134, 252, 0.35) !important;"
            + "  border-color: #BB86FC !important;"
            + "  transform: scale(0.92);"
            + "}"
            + ".favicon {"
            + "  width: 32px; height: 32px; border-radius: 6px; object-fit: contain;"
            + "}"
            + ".name {"
            + "  font-size: 11px; text-align: center;"
            + "  white-space: nowrap; overflow: hidden;"
            + "  text-overflow: ellipsis; max-width: 100%; padding: 0 2px;"
            + "}"
            + ".footer {"
            + "  margin-top: auto; padding-top: 40px; font-size: 12px; color: #555;"
            + "}"
            + "</style></head><body>"
            + "<div class='logo'>Y</div>"
            + "<h1>YSH 浏览器</h1>"
            + "<p class='subtitle'>极致轻量 · Material 风格</p>"
            + "<div class='shortcuts'>" + cards.toString() + "</div>"
            + "<p class='footer'>© YSH Browser</p>"
            + "</body></html>";
    }

    private String homeCard(String url, String host, String title) {
        String favicon = "https://favicon.im/" + host;
        return "<a class='card' href='" + url + "'>"
             + "<img class='favicon' src='" + favicon + "'/>"
             + "<span class='name'>" + title + "</span>"
             + "</a>";
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }


    private android.graphics.Bitmap captureThumbnail(android.webkit.WebView wv) {
        try {
            if (wv == null) return null;
            int w = wv.getWidth();
            int h = wv.getHeight();
            if (w <= 0 || h <= 0) return null;
            int thumbW = 240;
            int thumbH = (int) ((float) h / w * thumbW);
            if (thumbH <= 0) thumbH = 160;
            android.graphics.Bitmap bmp = android.graphics.Bitmap.createBitmap(thumbW, thumbH, android.graphics.Bitmap.Config.ARGB_8888);
            android.graphics.Canvas canvas = new android.graphics.Canvas(bmp);
            canvas.drawColor(0xFF121212);
            float scale = (float) thumbW / w;
            canvas.scale(scale, scale);
            wv.draw(canvas);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    private void updateThumbnail(int index) {
        try {
            if (index < 0 || index >= TabManager.tabs.size()) return;
            TabManager.Tab tab = TabManager.tabs.get(index);
            tab.thumbnail = captureThumbnail(tab.webView);
        } catch (Exception e) {}
    }

    private void switchToTab(int index) {
        if (index < 0 || index >= TabManager.tabs.size()) return;

        // 保存当前标签的缩略图
        if (TabManager.currentIndex >= 0 && TabManager.currentIndex < TabManager.tabs.size()) {
            updateThumbnail(TabManager.currentIndex);
        }

        for (int i = 0; i < TabManager.tabs.size(); i++) {
            TabManager.tabs.get(i).webView.setVisibility(i == index ? View.VISIBLE : View.GONE);
        }
        TabManager.currentIndex = index;

        WebView cur = TabManager.tabs.get(index).webView;
        String u = cur.getUrl();
        if (u == null || u.equals("about:blank")) {
            urlEditText.setText("");
        } else {
            urlEditText.setText(u);
        }
        updateTabButton();
    }

    private void closeTabAt(int index) {
        if (index < 0 || index >= TabManager.tabs.size()) return;

        TabManager.Tab tab = TabManager.tabs.get(index);
        webViewContainer.removeView(tab.webView);
        tab.webView.destroy();
        TabManager.tabs.remove(index);

        if (TabManager.tabs.isEmpty()) {
            // 0 个标签：允许
            TabManager.currentIndex = -1;
            urlEditText.setText("");
            // 可以显示一个空状态
        } else {
            if (TabManager.currentIndex >= TabManager.tabs.size()) {
                TabManager.currentIndex = TabManager.tabs.size() - 1;
            }
            if (TabManager.currentIndex < 0) TabManager.currentIndex = 0;
            switchToTab(TabManager.currentIndex);
        }
        updateTabButton();
    }

    private void updateTabButton() {
        try {
            tabButton.setText(String.valueOf(TabManager.tabs.size()));
        } catch (Exception e) {}
    }

    private void applyCookieMode() {
        try {
            CookieManager cm = CookieManager.getInstance();
            boolean incognito = prefs.getBoolean("incognito", false);
            boolean cookie = prefs.getBoolean("cookie_enabled", true);
            cm.setAcceptCookie(!incognito && cookie);
        } catch (Exception e) {}
    }

    private void saveHistory(String title, String url) {
        try {
            boolean incognito = prefs.getBoolean("incognito", false);
            boolean historyOn = prefs.getBoolean("history_enabled", true);
            if (incognito || !historyOn) return;
            if (url == null || url.equals("about:blank")) return;

            HistoryHelper helper = new HistoryHelper(this);
            if (title == null || title.isEmpty()) title = url;
            helper.addHistory(title, url);
            helper.close();
        } catch (Exception e) {}
    }

    private WebView getCurrentWebView() {
        if (TabManager.currentIndex < 0 || TabManager.currentIndex >= TabManager.tabs.size()) return null;
        return TabManager.tabs.get(TabManager.currentIndex).webView;
    }

    private void loadUrlOrSearch() {
        WebView wv = getCurrentWebView();
        String input = urlEditText.getText().toString().trim();
        if (input.isEmpty()) return;

        boolean isUrl = (input.contains(".") && !input.contains(" ")) || input.startsWith("http");
        String finalUrl;
        if (isUrl) {
            if (!input.startsWith("http")) input = "https://" + input;
            finalUrl = input;
        } else {
            String engine = prefs.getString("search_engine", "https://www.google.com/search?q=");
            try {
                finalUrl = engine + URLEncoder.encode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                finalUrl = engine + input;
            }
        }

        if (wv != null) {
            wv.loadUrl(finalUrl);
        } else {
            // 没有标签：新建一个
            createNewTab(finalUrl, true);
        }
    }

    private void showMenu() {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_menu);

        final android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(0));
            android.view.WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = android.view.WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = android.view.WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            window.setGravity(android.view.Gravity.BOTTOM);
            try { window.setWindowAnimations(R.style.MenuDialogAnimation); } catch (Exception e) {}
            
        }

        final boolean incognito = prefs.getBoolean("incognito", false);

        // 绑定卡片：每个卡片是一个 LinearLayout，里面包含 icon + label
        View cardBookmark = dialog.findViewById(R.id.card_bookmark);
        View cardAddBookmark = dialog.findViewById(R.id.card_add_bookmark);
        View cardHistory = dialog.findViewById(R.id.card_history);
        View cardManageHistory = dialog.findViewById(R.id.card_manage_history);
        View cardSettings = dialog.findViewById(R.id.card_settings);
        View cardShare = dialog.findViewById(R.id.card_share);
        View cardIncognito = dialog.findViewById(R.id.card_incognito);
        View cardShortcut = dialog.findViewById(R.id.card_shortcut);
        View menuExit = dialog.findViewById(R.id.menu_exit);

        // 设置图标和文字（因为 include 复用同一个布局，运行时动态设）
        setupCard(dialog, R.id.card_bookmark, R.drawable.ic_bookmark, "书签");
        setupCard(dialog, R.id.card_add_bookmark, R.drawable.ic_add_bookmark, "添加书签");
        setupCard(dialog, R.id.card_history, R.drawable.ic_history, "历史");
        setupCard(dialog, R.id.card_manage_history, R.drawable.ic_manage_history, "管理历史");
        setupCard(dialog, R.id.card_settings, R.drawable.ic_settings, "设置");
        setupCard(dialog, R.id.card_share, R.drawable.ic_share, "分享网页");
        setupCard(dialog, R.id.card_incognito, R.drawable.ic_incognito, incognito ? "关闭无痕" : "无痕模式");
        setupCard(dialog, R.id.card_shortcut, R.drawable.ic_add_home, "添加桌面");

        // 点击事件
        if (cardBookmark != null) cardBookmark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, BookmarkActivity.class));
                }}, 400);
            }
        });
        if (cardAddBookmark != null) cardAddBookmark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    addBookmark();
                }}, 400);
            }
        });
        if (cardHistory != null) cardHistory.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                }}, 400);
            }
        });
        if (cardManageHistory != null) cardManageHistory.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, HistoryActivity.class));
                }}, 400);
            }
        });
        if (cardSettings != null) cardSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                }}, 400);
            }
        });
        if (cardShare != null) cardShare.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    sharePage();
                }}, 400);
            }
        });
        if (cardIncognito != null) cardIncognito.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    toggleIncognito();
                }}, 400);
            }
        });
        if (cardShortcut != null) cardShortcut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    addShortcut();
                }}, 400);
            }
        });
        if (menuExit != null) menuExit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                v.postDelayed(new Runnable() { @Override public void run() {
                    dialog.dismiss();
                    finish();
                }}, 400);
            }
        });

        dialog.show();
    }

    private void setupCard(android.app.Dialog dialog, int rootId, int iconRes, String label) {
        try {
            View card = dialog.findViewById(rootId);
            if (card == null) return;
            android.widget.ImageView icon = card.findViewById(R.id.menuIcon);
            android.widget.TextView text = card.findViewById(R.id.menuLabel);
            if (icon != null) icon.setImageResource(iconRes);
            if (text != null) text.setText(label);
        } catch (Exception e) {}
    }

    private void addBookmark() {
        WebView wv = getCurrentWebView();
        if (wv == null) {
            Toast.makeText(this, "没有活动标签", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = wv.getUrl();
        String title = wv.getTitle();
        if (url == null || url.equals("about:blank")) {
            Toast.makeText(this, "当前页面无法添加书签", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title == null || title.isEmpty()) title = url;
        try {
            BookmarkHelper h = new BookmarkHelper(this);
            h.addBookmark(title, url);
            h.close();
            Toast.makeText(this, "已添加书签", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void sharePage() {
        WebView wv = getCurrentWebView();
        String url = wv == null ? "" : wv.getUrl();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(intent, "分享网页"));
    }

    private void addShortcut() {
        WebView wv = getCurrentWebView();
        if (wv == null) return;
        String url = wv.getUrl();
        String title = wv.getTitle();
        if (url == null || url.isEmpty() || url.equals("about:blank")) {
            Toast.makeText(this, "当前页面无法创建快捷方式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title == null || title.isEmpty()) title = url;
        try {
            Intent shortcutIntent = new Intent(this, MainActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.putExtra("load_url", url);

            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            sendBroadcast(addIntent);
            Toast.makeText(this, "已创建快捷方式", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "创建失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleIncognito() {
        boolean cur = prefs.getBoolean("incognito", false);
        prefs.edit().putBoolean("incognito", !cur).apply();
        applyCookieMode();
        Toast.makeText(this, !cur ? "无痕模式已开启" : "无痕模式已关闭", Toast.LENGTH_SHORT).show();
    }

    private void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(urlEditText.getWindowToken(), 0);
        } catch (Exception e) {}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            String action = data.getStringExtra("action");
            int index = data.getIntExtra("index", -1);
            if ("switch".equals(action)) {
                switchToTab(index);
            } else if ("close".equals(action)) {
                closeTabAt(index);
            } else if ("new".equals(action)) {
                createNewTabWithHome();
            } else if ("closeAll".equals(action)) {
                for (int i = TabManager.tabs.size() - 1; i >= 0; i--) {
                    TabManager.Tab t = TabManager.tabs.get(i);
                    webViewContainer.removeView(t.webView);
                    t.webView.destroy();
                    TabManager.tabs.remove(i);
                }
                TabManager.currentIndex = -1;
                urlEditText.setText("");
                updateTabButton();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTabButton();
        try {
            WebView wv = getCurrentWebView();
            if (wv != null) {
                String u = wv.getUrl();
                if (u == null || u.equals("about:blank")) {
                    urlEditText.setText("");
                } else {
                    urlEditText.setText(u);
                }
            }
        } catch (Exception e) {}
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String loadUrl = intent.getStringExtra("load_url");
        if (loadUrl != null && !loadUrl.isEmpty()) {
            WebView wv = getCurrentWebView();
            if (wv != null) wv.loadUrl(loadUrl);
            else createNewTab(loadUrl, true);
        }
    }

    @Override
    public void onBackPressed() {
        WebView wv = getCurrentWebView();
        if (wv != null && wv.canGoBack()) {
            wv.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
