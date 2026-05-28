package swadha.collection.rental;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.FileProvider;

import java.io.File;

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

                            boolean updating =

                                    pref.getBoolean(
                                            "update_in_progress",
                                            false
                                    );

                            if(updating){
                                return;
                            }
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

                                                                pref.edit()

                                                                        .putBoolean(
                                                                                "update_in_progress",
                                                                                true
                                                                        )

                                                                        .apply();

                                                                downloadAndInstall(
                                                                        context,
                                                                        apkUrl
                                                                );
                                                            })
                                                    .setNegativeButton(

                                                            "Later",

                                                            (d,w) -> d.dismiss()
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

                        }catch (Exception ignored){
                        }
                    }
                });


    }
    public interface UpdateCallback{

        void onNoUpdate();

        void onUpdateShown();
    }


    private static void downloadAndInstall(

            Context context,

            String apkUrl
    ){
        File oldFile =

                new File(

                        context.getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS
                        ),

                        "swadha_update.apk"
                );

        if(oldFile.exists()){

            oldFile.delete();
        }

        DownloadManager.Request request =

                new DownloadManager.Request(
                        Uri.parse(apkUrl)
                );

        request.setTitle(
                "Swadha Update"
        );

        request.setDescription(
                "Downloading latest version..."
        );

        request.setNotificationVisibility(

                DownloadManager.Request

                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        );

        File apkFile =

                new File(

                        context.getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS
                        ),

                        "swadha_update.apk"
                );

        request.setDestinationUri(
                Uri.fromFile(apkFile)
        );

        DownloadManager manager =

                (DownloadManager)

                        context.getSystemService(
                                Context.DOWNLOAD_SERVICE
                        );

        long downloadId =
                manager.enqueue(request);

        BroadcastReceiver receiver =

                new BroadcastReceiver() {

                    @Override
                    public void onReceive(
                            Context ctx,
                            Intent intent
                    ) {

                        long id =

                                intent.getLongExtra(

                                        DownloadManager
                                                .EXTRA_DOWNLOAD_ID,

                                        -1
                                );

                        if(id == downloadId){
                            File file =

                                    new File(

                                            context.getExternalFilesDir(
                                                    Environment.DIRECTORY_DOWNLOADS
                                            ),

                                            "swadha_update.apk"
                                    );

                            Uri uri =

                                    FileProvider.getUriForFile(

                                            context,

                                            context.getPackageName()
                                                    + ".provider",

                                            file
                                    );

                            Intent installIntent =

                                    new Intent(
                                            Intent.ACTION_VIEW
                                    );

                            installIntent.setDataAndType(

                                    uri,

                                    "application/vnd.android.package-archive"
                            );

                            installIntent.addFlags(

                                    Intent.FLAG_ACTIVITY_NEW_TASK
                            );

                            installIntent.addFlags(

                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                            );

                            context.startActivity(
                                    installIntent
                            );
                        }
                    }
                };

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){

            context.registerReceiver(

                    receiver,

                    new IntentFilter(
                            DownloadManager.ACTION_DOWNLOAD_COMPLETE
                    ),

                    Context.RECEIVER_NOT_EXPORTED
            );

        }else{

            context.registerReceiver(

                    receiver,

                    new IntentFilter(
                            DownloadManager.ACTION_DOWNLOAD_COMPLETE
                    )
            );
        }
    }
}