package com.commonsware.android.arXiv;

import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by fedor on 12.08.17.
 */

public class Utils {
    static String removeSomeCharacters(String title){
        String filename = title.replace(":", "");
        filename = filename.replace("?", "");
        filename = filename.replace("*", "");
        filename = filename.replace("/", "");
        filename = filename.replace(". ", "");
        filename = filename.replace("`", "");
        filename = filename + ".pdf";
        return filename;
    }

    static String getRedirectUrl(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        boolean redirect = false;
        int status = conn.getResponseCode();
        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP
                    || status == HttpURLConnection.HTTP_MOVED_PERM
                    || status == HttpURLConnection.HTTP_SEE_OTHER)
                redirect = true;
        }
        String newUrl;
        if (redirect) {
            // get redirect url from "location" header field
            newUrl = conn.getHeaderField("Location");
        }
        else{
            // no need to redirect
            newUrl = url;
        }

        return newUrl;
    }

    static String downloadFile(String url,
                               String filepath,
                               String title,
                               boolean vLoop,
                               ProgressBar progBar) throws Exception{
        // download file with http get request while simultaneously drawing status in progress bar
        try {
            // get connection from url with possible redirect
            url = getRedirectUrl(url);
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u
                    .openConnection();
            c.setRequestMethod("GET");
            c.connect();

            final long ifs = c.getContentLength();
            InputStream in = c.getInputStream();

            String filename = Utils.removeSomeCharacters(title);

            Boolean vdownload = true;
            File futureFile = new File(filepath, filename);
            if (futureFile.exists()) {
                final long itmp = futureFile.length();
                if (itmp == ifs && itmp != 0) {
                    vdownload = false;
                }
            }

            if (vdownload) {
                FileOutputStream f = new FileOutputStream(futureFile);

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long i = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    if (!vLoop) {
                        break;
                    }
                    f.write(buffer, 0, len1);
                    i += len1;
                    long jt = 100 * i / ifs;
                    final int j = (int) jt;
                    progBar.post(new Runnable() {
                        public void run() {
                            progBar.setProgress(j);
                        }
                    });
                }
                f.close();
            } else {
                progBar.post(new Runnable() {
                                    public void run() {
                                        progBar.setProgress(100);
                                    }
                                });
            }
            return futureFile.getPath();
        } catch (Exception e) {
            Log.d("arxiv","error "+e);
            e.printStackTrace();
            throw e;
        }
    }
};
