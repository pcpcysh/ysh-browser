package com.ysh.browser;

import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

public class TabManager {
    public static List<Tab> tabs = new ArrayList<>();
    public static int currentIndex = 0;

    public static class Tab {
        public WebView webView;
        public String url;
        public String title;
        public android.graphics.Bitmap thumbnail;

        public Tab(WebView wv, String url, String title) {
            this.webView = wv;
            this.url = url;
            this.title = title;
        }
    }
}
