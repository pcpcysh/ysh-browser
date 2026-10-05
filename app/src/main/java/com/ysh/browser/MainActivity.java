package com.ysh.browser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class MainActivity extends Activity {

    private WebView webView;
    private EditText urlEditText;
    private String currentUrl = "about:blank";
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("browser_prefs", MODE_PRIVATE);

        webView = findViewById(R.id.webView);
        urlEditText = findViewById(R.id.urlEditText);
        Button goButton = findViewById(R.id.goButton);
        Button menuButton = findViewById(R.id.menuButton);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        applyCookieMode();

        webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("http://") || url.startsWith("https://")) {
                        view.loadUrl(url);
                        return true;
                    }
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {}
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    if (url != null && !url.contains("ysh.browser") && !url.equals("about:blank")) {
                        currentUrl = url;
                        urlEditText.setText(url);
                        saveHistory(view.getTitle(), url);
                    }
                    applyDarkModeCss(view);
                }
            });

        String loadUrl = getIntent().getStringExtra("load_url");
        if (loadUrl != null && !loadUrl.isEmpty()) {
            currentUrl = loadUrl;
        } else if (savedInstanceState != null) {
            String s = savedInstanceState.getString("currentUrl");
            if (s != null && !s.isEmpty()) currentUrl = s;
        }

        if (currentUrl == null || currentUrl.equals("about:blank")) {
            showHomePage();
        } else {
            webView.loadUrl(currentUrl);
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

    private void loadUrlOrSearch() {
        String input = urlEditText.getText().toString().trim();
        if (input.isEmpty()) return;

        boolean isUrl = (input.contains(".") && !input.contains(" ")) || input.startsWith("http");
        if (isUrl) {
            if (!input.startsWith("http")) input = "https://" + input;
            currentUrl = input;
            webView.loadUrl(currentUrl);
        } else {
            String engine = prefs.getString("search_engine", "https://www.google.com/search?q=");
            try {
                currentUrl = engine + URLEncoder.encode(input, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                currentUrl = engine + input;
            }
            webView.loadUrl(currentUrl);
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
            window.setWindowAnimations(R.style.MenuDialogAnimation);
        }

        final boolean incognito = prefs.getBoolean("incognito", false);

        Button bSettings = dialog.findViewById(R.id.menu_settings);
        Button bHistory = dialog.findViewById(R.id.menu_history);
        Button bBookmark = dialog.findViewById(R.id.menu_bookmark);
        Button bAddBookmark = dialog.findViewById(R.id.menu_add_bookmark);
        Button bShare = dialog.findViewById(R.id.menu_share);
        Button bShortcut = dialog.findViewById(R.id.menu_shortcut);
        Button bIncognito = dialog.findViewById(R.id.menu_incognito);
        Button bExit = dialog.findViewById(R.id.menu_exit);

        if (incognito) {
            bIncognito.setText("🕶️   关闭无痕模式");
        } else {
            bIncognito.setText("🕶️   开启无痕模式");
        }

        bSettings.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        bHistory.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });
        bBookmark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                startActivity(new Intent(MainActivity.this, BookmarkActivity.class));
            }
        });
        bAddBookmark.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                addBookmark();
            }
        });
        bShare.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                sharePage();
            }
        });
        bShortcut.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                addShortcut();
            }
        });
        bIncognito.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                toggleIncognito();
            }
        });
        bExit.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();
    }

    private void addBookmark() {
        String url = webView.getUrl();
        String title = webView.getTitle();
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
        String url = webView.getUrl();
        if (url == null) url = currentUrl;
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(intent, "分享网页"));
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String loadUrl = intent.getStringExtra("load_url");
        if (loadUrl != null && !loadUrl.isEmpty()) {
            webView.loadUrl(loadUrl);
            currentUrl = loadUrl;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentUrl", currentUrl);
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyCookieMode();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // ============================================================
    // 漂亮的首页（从书签动态生成）
    // ============================================================
    private void showHomePage() {
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
                        java.net.URL u = new java.net.URL(item.url);
                        host = u.getHost();
                    } catch (Exception e) {}
                    String favicon = "https://favicon.im/" + host;
                    cards.append("<a class='card' href='").append(item.url).append("'>");
                    cards.append("<img class='favicon' src='").append(favicon).append("'/>");
                    cards.append("<span class='name'>").append(escapeHtml(title)).append("</span>");
                    cards.append("</a>");
                }
            } else {
                cards.append(card("https://www.baidu.com", "https://www.baidu.com", "百度"));
                cards.append(card("https://www.bilibili.com", "https://www.bilibili.com", "哔哩哔哩"));
                cards.append(card("https://www.zhihu.com", "https://www.zhihu.com", "知乎"));
                cards.append(card("https://github.com", "https://github.com", "GitHub"));
            }
        } catch (Exception e) {
            cards.append(card("https://www.baidu.com", "https://www.baidu.com", "百度"));
        }

        String html = "<!DOCTYPE html>"
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
            + "  transition: all 0.2s; border: 1px solid rgba(255, 255, 255, 0.05);"
            + "  display: flex; flex-direction: column; align-items: center; gap: 6px;"
            + "  min-height: 80px;"
            + "}"
            + ".card:active {"
            + "  background: rgba(187, 134, 252, 0.2); border-color: #BB86FC;"
            + "  transform: scale(0.96);"
            + "}"
            + ".favicon {"
            + "  width: 32px; height: 32px; border-radius: 6px;"
            + "  object-fit: contain;"
            + "}"
            + ".name {"
            + "  font-size: 11px; text-align: center;"
            + "  white-space: nowrap; overflow: hidden;"
            + "  text-overflow: ellipsis; max-width: 100%;"
            + "  padding: 0 2px;"
            + "}"
            + ".footer {"
            + "  margin-top: auto; padding-top: 40px;"
            + "  font-size: 12px; color: #555;"
            + "}"
            + "</style>"
            + "</head><body>"
            + "<div class='logo'>Y</div>"
            + "<h1>YSH 浏览器</h1>"
            + "<p class='subtitle'>极致轻量 · Material 风格</p>"
            + "<div class='shortcuts'>"
            + cards.toString()
            + "</div>"
            + "<p class='footer'>© YSH Browser</p>"
            + "</body></html>";

        webView.loadDataWithBaseURL("https://ysh.browser", html, "text/html", "UTF-8", null);
    }

    private String card(String url, String host, String title) {
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

    private void applyDarkModeCss(WebView view) {
        String js = "(function(){"
            + "if(document.getElementById('ysh-dark')) return;"
            + "var s = document.createElement('style');"
            + "s.id = 'ysh-dark';"
            + "s.innerHTML = 'html,body{background:#121212 !important;color:#E0E0E0 !important;}'"
            + "+ 'a{color:#BB86FC !important;}'"
            + "+ 'input,textarea,select{background:#1E1E1E !important;color:#E0E0E0 !important;}'"
            + "+ '*{border-color:#333 !important;}';"
            + "document.head.appendChild(s);"
            + "})();";
        view.evaluateJavascript(js, null);
    }

    
    private void addShortcut() {
        String url = webView.getUrl();
        String title = webView.getTitle();
        if (url == null || url.isEmpty() || url.equals("about:blank")) {
            Toast.makeText(this, "当前页面无法创建快捷方式", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title == null || title.isEmpty()) title = url;

        // Android 8.0+ 使用 ShortcutManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                android.content.pm.ShortcutManager shortcutManager =
                        getSystemService(android.content.pm.ShortcutManager.class);

                if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported()) {
                    Toast.makeText(this, "当前桌面不支持创建快捷方式", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent shortcutIntent = new Intent(this, MainActivity.class);
                shortcutIntent.setAction(Intent.ACTION_MAIN);
                shortcutIntent.putExtra("load_url", url);

                android.content.pm.ShortcutInfo shortcutInfo =
                        new android.content.pm.ShortcutInfo.Builder(this, "sc_" + System.currentTimeMillis())
                                .setShortLabel(title.length() > 10 ? title.substring(0, 10) : title)
                                .setLongLabel(title)
                                .setIcon(android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_launcher))
                                .setIntent(shortcutIntent)
                                .build();

                shortcutManager.requestPinShortcut(shortcutInfo, null);
                Toast.makeText(this, "请确认添加到桌面", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "创建失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // Android 7.x 及以下用旧版广播
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
                Toast.makeText(this, "创建失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
@Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
