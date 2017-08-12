package com.commonsware.android.arXiv;

import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
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

    static String downloadFile(String url,
                               String filepath,
                               String title,
                               boolean vLoop,
                               ProgressBar progBar) throws Exception{
        // download file with http get request while simultaneously drawing status in progress bar
        try {
            URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u
                    .openConnection();
            c.setRequestMethod("GET");
            //c.setDoOutput(true);
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
