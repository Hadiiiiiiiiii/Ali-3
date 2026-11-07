package com.graphstudio.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView web;
    private int statusColor = Color.BLACK;
    private int navColor = Color.BLACK;
    private boolean barsDrivenByPage = false;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        web.setBackgroundColor(Color.BLACK);
        web.setOverScrollMode(View.OVER_SCROLL_NEVER);
        web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        setContentView(web);
        applyBars(statusColor, navColor);

        if ((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(false);
        s.setTextZoom(100);
        s.setMediaPlaybackRequiresUserGesture(false);

        web.addJavascriptInterface(new ThemeBridge(), "AndroidTheme");

        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("file".equals(u.getScheme())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));
                } catch (Exception ignored) { }
                return true;
            }
        });

        web.loadUrl("file:///android_asset/index.html");
    }

    /** Bridge used by the page: window.AndroidTheme.setBars(statusRgb, navRgb) / setLightMode(bool). */
    private class ThemeBridge {
        @JavascriptInterface
        public void setBars(final int statusRgb, final int navRgb) {
            runOnUiThread(() -> {
                barsDrivenByPage = true;
                applyBars(0xFF000000 | statusRgb, 0xFF000000 | navRgb);
            });
        }

        @JavascriptInterface
        public void setLightMode(final boolean light) {
            runOnUiThread(() -> {
                if (barsDrivenByPage) return;
                int c = light ? 0xFFF2F2F7 : 0xFF000000;
                applyBars(c, c);
            });
        }
    }

    private static boolean isLight(int color) {
        double r = Color.red(color) / 255.0, g = Color.green(color) / 255.0, b = Color.blue(color) / 255.0;
        return (0.2126 * r + 0.7152 * g + 0.0722 * b) > 0.5;
    }

    @SuppressWarnings("deprecation")
    private void applyBars(int status, int nav) {
        statusColor = status;
        navColor = nav;
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(status);
        w.setNavigationBarColor(nav);
        if (Build.VERSION.SDK_INT >= 28) w.setNavigationBarDividerColor(nav);
        if (Build.VERSION.SDK_INT >= 29) {
            w.setNavigationBarContrastEnforced(false);
            w.setStatusBarContrastEnforced(false);
        }

        boolean lightStatus = isLight(status);
        boolean lightNav = isLight(nav);

        WindowInsetsController controller = null;
        if (Build.VERSION.SDK_INT >= 30) controller = w.getInsetsController();
        if (controller != null) {
            controller.setSystemBarsAppearance(
                    lightStatus ? WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS : 0,
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS);
            controller.setSystemBarsAppearance(
                    lightNav ? WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS : 0,
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
        } else {
            View d = w.getDecorView();
            int f = d.getSystemUiVisibility();
            if (Build.VERSION.SDK_INT >= 23) {
                f = lightStatus ? (f | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR)
                                : (f & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
            if (Build.VERSION.SDK_INT >= 26) {
                f = lightNav ? (f | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR)
                             : (f & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            d.setSystemUiVisibility(f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (web != null) web.onResume();
        applyBars(statusColor, navColor);
    }

    @Override
    protected void onPause() {
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (web != null) {
            web.removeJavascriptInterface("AndroidTheme");
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
