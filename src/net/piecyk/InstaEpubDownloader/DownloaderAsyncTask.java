package net.piecyk.InstaEpubDownloader;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DownloaderAsyncTask extends AsyncTask<Void, Long, Long> {
    private TextView statusMessage;
    private String fileName;
    private String login;
    private String password;
    private String path;

    private String message;

    public static final String TAG = "DownloaderAsyncTask";

    public DownloaderAsyncTask(String login, String password, TextView statusMessage, String path) {
        this.login = login;
        this.password = password;
        this.statusMessage = statusMessage;
        this.path = path;
    }

    @Override
    protected Long doInBackground(Void... voids) {

        DefaultHttpClient httpclient = new DefaultHttpClient();

        HttpPost httpost = new HttpPost("https://www.instapaper.com/user/login");

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("username", login));
        nvps.add(new BasicNameValuePair("password", password));

        try {
            httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

            HttpResponse response = httpclient.execute(httpost);
            HttpEntity entity = response.getEntity();

            Log.d(TAG, "Login form get: " + response.getStatusLine());
            if (entity != null) {
                entity.consumeContent();
            }

            response = httpclient.execute(new HttpGet("https://www.instapaper.com/epub"));
            StatusLine statusLine = response.getStatusLine();

            Header[] headers = response.getHeaders("Content-Disposition");

            if (headers.length <= 0) {
                // if something went wrong
                // TODO: better error handling would be nice e.g. "no valid password" etc.
                message = "Cannot download EPUB file";
                return 0L;
            }
            fileName = headers[0].getElements()[0].getParameterByName("filename").getValue();

            FileOutputStream outputFile = new FileOutputStream(new File(path + fileName));

            //response.getEntity().writeTo(outputFile); //getContent() from entity
            InputStream input = response.getEntity().getContent();
            Log.d(TAG, "File size: " + response.getEntity().getContentLength());

            byte[] buffer = new byte[1024*32];
            int len;
            long downloadedBytes = 0;

            while((len = input.read(buffer)) > 0) {
                outputFile.write(buffer, 0, len);
                downloadedBytes += len;
                publishProgress(downloadedBytes / 1024);
                Log.d(TAG, "Downloaded " + downloadedBytes);
            }


            outputFile.close();

            httpclient.getConnectionManager().shutdown();

            message = (downloadedBytes / 1024) +" kB downloaded \n" +
                      "File saved to " + path + fileName;
            return downloadedBytes;

        } catch (Exception e) {
            message = "Error occurred: \n" + e;
            Log.w(TAG, "Exception occurred: " + e);
            return 0L;
        }
    }

    protected void onPreExecute() {
        statusMessage.setText("Connecting...");
    }

    @Override
    protected void onProgressUpdate(Long... progress) {
        statusMessage.setText(progress[0] +" kB downloaded");
    }

    @Override
    protected void onPostExecute(Long result) {
        statusMessage.setText(message);
    }
}

