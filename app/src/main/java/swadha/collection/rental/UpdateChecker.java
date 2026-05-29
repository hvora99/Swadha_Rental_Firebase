package swadha.collection.rental;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.app.ProgressDialog;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

public class UpdateChecker {

    public static void check(

            Context context,

            UpdateCallback callback
    ){

        String url =

                "https://api.github.com/repos/hvora99/Swadha_Rental_Firebase/releases/latest";

        OkHttpClient client =
                new OkHttpClient();

        Request request =
                new Request.Builder()
                        .url(url)
                        .build();

        client.newCall(request)

                .enqueue(new Callback() {

                    @Override
                    public void onFailure(
                            Call call,
                            IOException e
                    ) {

                        ((android.app.Activity) context)

                                .runOnUiThread(() -> {

                                    callback.onNoUpdate();
                                });
                    }

                    @Override
                    public void onResponse(
                            Call call,
                            Response response
                    ) throws IOException {

                        if(response.body() == null)
                            return;

                        try{

                            String json =
                                    response.body().string();

                            JSONObject obj =
                                    new JSONObject(json);

                            String latestVersion =

                                    obj.getString(
                                            "tag_name"
                                    ).trim();

                            String currentVersion =

                                    BuildConfig.VERSION_NAME
                                            .trim();


                            SharedPreferences pref =

                                    context.getSharedPreferences(
                                            "updater",
                                            Context.MODE_PRIVATE
                                    );


                            if(!latestVersion.equals(
                                    currentVersion
                            )){

                                String apkUrl =

                                        obj.getJSONArray(
                                                        "assets"
                                                )

                                                .getJSONObject(0)

                                                .getString(
                                                        "browser_download_url"
                                                );

                                ((android.app.Activity)
                                        context)

                                        .runOnUiThread(() -> {
                                            callback.onUpdateShown();

                                            new AlertDialog.Builder(
                                                    context
                                            )

                                                    .setTitle(
                                                            "Update Available"
                                                    )

                                                    .setMessage(
                                                            "New version available"
                                                    )

                                                    .setCancelable(true)
                                                    .setPositiveButton(

                                                            "Update",

                                                            (d,w) -> {

                                                                downloadAndInstall(
                                                                        context,
                                                                        apkUrl
                                                                );
                                                            })
                                                    .setNegativeButton(

                                                            "Later",

                                                            (d,w) -> {

                                                                d.dismiss();

                                                                callback.onUpdateDismissed();
                                                            }
                                                    )

                                                    .show();
                                        });
                            }
                            else{

                                ((android.app.Activity) context)

                                        .runOnUiThread(() -> {

                                            callback.onNoUpdate();
                                        });
                            }

                        }catch (Exception e){

                        ((android.app.Activity) context)

                                .runOnUiThread(() -> {

                                    callback.onNoUpdate();
                                });
                    }
                    }
                });


    }

    private static void downloadAndInstall(

            Context context,

            String apkUrl
    ){

        ProgressDialog progressDialog =

                new ProgressDialog(context);

        progressDialog.setTitle(
                "Downloading Update"
        );

        progressDialog.setProgressStyle(
                ProgressDialog.STYLE_HORIZONTAL
        );

        progressDialog.setCancelable(false);

        progressDialog.setMax(100);

        progressDialog.show();

        new Thread(() -> {

            try{

                File folder =

                        new File(

                                Environment
                                        .getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_DOCUMENTS
                                        ),

                                "Svadha"
                        );

                if(!folder.exists()){

                    folder.mkdirs();
                }

                File apkFile =

                        new File(
                                folder,
                                "swadha_latest.apk"
                        );

                OkHttpClient client =
                        new OkHttpClient();

                Request request =

                        new Request.Builder()

                                .url(apkUrl)

                                .build();

                Response response =

                        client.newCall(request)

                                .execute();

                if(response.body() == null){

                    throw new Exception(
                            "Download failed"
                    );
                }

                long fileSize =

                        response.body()
                                .contentLength();

                InputStream inputStream =

                        response.body()
                                .byteStream();

                FileOutputStream outputStream =

                        new FileOutputStream(
                                apkFile
                        );

                byte[] buffer =
                        new byte[8192];

                long totalBytes =
                        0;

                int count;

                while((count =
                        inputStream.read(buffer))
                        != -1){

                    totalBytes += count;

                    outputStream.write(
                            buffer,
                            0,
                            count
                    );

                    final int progress =

                            (int)(
                                    totalBytes
                                            * 100
                                            / fileSize
                            );

                    ((android.app.Activity)
                            context)

                            .runOnUiThread(() -> {

                                progressDialog.setProgress(
                                        progress
                                );
                            });
                }

                outputStream.flush();

                outputStream.close();

                inputStream.close();

                android.media.MediaScannerConnection
                        .scanFile(

                                context,

                                new String[]{
                                        apkFile.getAbsolutePath()
                                },

                                null,

                                null
                        );

                ((android.app.Activity)
                        context)

                        .runOnUiThread(() -> {

                            progressDialog.dismiss();

                            new AlertDialog.Builder(
                                    context
                            )

                                    .setTitle(
                                            "Download Complete"
                                    )

                                    .setMessage(

                                            "APK saved successfully.\n\n"

                                                    + apkFile
                                                    .getAbsolutePath()

                                                    + "\n\nPlease install manually from Documents/Svadha folder."
                                    )

                                    .setPositiveButton(

                                            "OK",

                                            (d,w) -> {

                                                ((android.app.Activity)
                                                        context)

                                                        .finish();
                                            }
                                    )

                                    .show();
                        });

            }catch (Exception e){

                ((android.app.Activity)
                        context)

                        .runOnUiThread(() -> {

                            progressDialog.dismiss();

                            Toast.makeText(

                                    context,

                                    "Download Failed : "
                                            + e.getMessage(),

                                    Toast.LENGTH_LONG

                            ).show();
                        });
            }

        }).start();
    }
    public interface UpdateCallback{

        void onNoUpdate();

        void onUpdateShown();

        void onUpdateDismissed();
    }


}