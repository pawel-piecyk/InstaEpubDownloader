package net.piecyk.InstaEpubDownloader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private EditText loginField;
    private EditText passwordField;
    private EditText filesPathField;
    private CheckBox saveLoginAndPasswordCheckbox;
    private String filesPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // getting login and password fields
        loginField = (EditText) findViewById(R.id.login);
        passwordField = (EditText) findViewById(R.id.password);

        // getting files location path and field containing it
        filesPathField = (EditText) findViewById(R.id.filesPath);
        filesPath = filesPathField.getText().toString();

        // save login and password checkbox
        saveLoginAndPasswordCheckbox = (CheckBox) findViewById(R.id.saveLoginAndPasswordCheckbox);

        // ensure that we've got slash at the end of path
        if (!filesPath.endsWith("/")) filesPathField.append("/");

        loadLoginAndPassword();
    }

    public void downloadArticles(View view) {

        if (saveLoginAndPasswordCheckbox.isChecked()) {
            saveLoginAndPassword();
        }

        new DownloaderAsyncTask(loginField.getText().toString(),
                passwordField.getText().toString(),
                (TextView) findViewById(R.id.status_message), filesPath).execute();
    }

    public void openLastFile(View view) {

        // finding newest file in downloads folder

        File downloadsDirectory = new File(filesPath);
        File[] filesInDirectory = downloadsDirectory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getPath().endsWith(".epub");
            }
        });

        long maxModifiedTime = 0;
        File newestFile = filesInDirectory[0];
        for (File singleFile : filesInDirectory) {
            if (singleFile.lastModified() > maxModifiedTime) {
                maxModifiedTime = singleFile.lastModified();
                newestFile = singleFile;
            }
        }

        // starting intent to open ebook file in default epub reader
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(newestFile), "application/epub+zip");
        startActivity(intent);
    }

    public void saveLoginAndPassword() {
        SharedPreferences settings = getSharedPreferences("preferences", MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString("login", loginField.getText().toString());
        settingsEditor.putString("password", passwordField.getText().toString());
        settingsEditor.commit();
    }

    public void loadLoginAndPassword() {
        SharedPreferences settings =
                getApplicationContext().getSharedPreferences("preferences", MODE_PRIVATE);
        String login = settings.getString("login", "");
        String password = settings.getString("password", "");

        if (!login.equals("")) {
            loginField.setText(login);
        }

        if (!password.equals("")) {
            passwordField.setText(password);
        }
    }
}