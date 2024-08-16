package com.example.orf;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback;
import android.widget.Toast;
import android.view.View;

public class button6 extends AppCompatActivity {

    private WebView webView;
    private View overlay, dummy_button;
    private static final int REQUEST_CODE = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int FILE_CHOOSER_RESULT_CODE = 3;

    private static final String INITIAL_URL = "https://drive.google.com/drive/folders/1zC9f7lgPxxt0lULeior1SNJhUQmUO3hQ?usp";
    private static final String BLOCKED_URL = "https://drive.google.com/drive/my-drive";

    private String pendingUrl;
    private String pendingContentDisposition;
    private String pendingMimeType;
    private ValueCallback<Uri[]> uploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_button6);

        webView = findViewById(R.id.webView);
        overlay = findViewById(R.id.overlay);
        dummy_button = findViewById(R.id.dummy_button);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.equals(BLOCKED_URL)) {
                    Toast.makeText(button6.this, "Access to this URL is restricted.", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectJavaScript(view);
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");

                Intent chooserIntent = Intent.createChooser(intent, "File Chooser");
                startActivityForResult(chooserIntent, FILE_CHOOSER_RESULT_CODE);

                return true;
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                pendingUrl = url;
                pendingContentDisposition = contentDisposition;
                pendingMimeType = mimeType;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    downloadFile(url, contentDisposition, mimeType);
                } else if (ContextCompat.checkSelfPermission(button6.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(button6.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
                } else {
                    downloadFile(url, contentDisposition, mimeType);
                }
            }
        });

        webView.loadUrl(INITIAL_URL);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE && resultCode == RESULT_OK) {
            if (uploadMessage == null) {
                return;
            }
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;
        } else if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingUrl != null && pendingContentDisposition != null && pendingMimeType != null) {
                    downloadFile(pendingUrl, pendingContentDisposition, pendingMimeType);
                }
            } else {
                Toast.makeText(this, "Permission denied to write to external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void downloadFile(String url, String contentDisposition, String mimeType) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);
            String cookies = CookieManager.getInstance().getCookie(url);
            request.addRequestHeader("cookie", cookies);
            request.addRequestHeader("User-Agent", webView.getSettings().getUserAgentString());
            request.setDescription("Downloading file...");
            request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            } else {
                String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
            }

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(getApplicationContext(), "Downloading File", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("DownloadError", "Error downloading file: " + e.getMessage());
            Toast.makeText(getApplicationContext(), "Download failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void injectJavaScript(WebView view) {
        String script = "document.getElementById('identifierId').value = 'outreachteog@gmail.com';" +
                "document.querySelector('#identifierNext').click();" +
                "setTimeout(function() {" +
                "  document.querySelector('input[type=\"password\"]').value = 'Rose@123@';" +
                "  document.querySelector('#passwordNext').click();" +
                "}, 2000);";

        view.evaluateJavascript(script, null);
    }
}
