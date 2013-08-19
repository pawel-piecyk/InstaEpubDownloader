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
    
    // GUI elements
    private EditText loginField;
    private EditText passwordField;
    private EditText filesPathField;
    private CheckBox saveLoginAndPasswordCheckbox;
    private TextView statusMessage;
    private String filesPath;
    
    // settings fields identifiers
    private final String LOGIN = "login";
    private final String PASSWORD = "password";
    
    // preferences identifier
    private final String PREFS = "preferences";

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

        // other fields
        statusMessage = (TextView) findViewById(R.id.statusMessage);

        // save login and password checkbox
        saveLoginAndPasswordCheckbox = (CheckBox) findViewById(R.id.saveLoginAndPasswordCheckbox);

        // ensure that we've got slash at the end of path
        if (!filesPath.endsWith("/")) filesPathField.append("/");

        loadLoginAndPassword();
    }

    public void downloadArticles(View view) {

        if (saveLoginAndPasswordCheckbox.isChecked()) {
            saveLoginAndPassword();
        } else {
            deleteLoginAndPassword();
        }

        new DownloaderAsyncTask(loginField.getText().toString(),
                passwordField.getText().toString(),
                statusMessage, filesPath).execute();
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

        if (filesInDirectory.length == 0) {
            statusMessage.setText("Downloads folder is empty");
        } else {
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
    }

    public void saveLoginAndPassword() {
        SharedPreferences settings = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.putString(LOGIN, loginField.getText().toString());
        settingsEditor.putString(PASSWORD, passwordField.getText().toString());
        settingsEditor.commit();
    }

    public void deleteLoginAndPassword() {
        SharedPreferences settings = getSharedPreferences(PREFS, MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = settings.edit();
        settingsEditor.remove(LOGIN);
        settingsEditor.remove(PASSWORD);
        settingsEditor.commit();
    }

    public void loadLoginAndPassword() {
        SharedPreferences settings =
                getApplicationContext().getSharedPreferences(PREFS, MODE_PRIVATE);
        String login = settings.getString(LOGIN, "");
        String password = settings.getString(PASSWORD, "");

        if (!login.equals("")) {
            loginField.setText(login);
            saveLoginAndPasswordCheckbox.setChecked(true);
        }

        if (!password.equals("")) {
            passwordField.setText(password);
        }
    }
}