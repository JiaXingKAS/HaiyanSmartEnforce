package smartenforce.widget;


import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogUtil {
    private static ProgressDialog progressDialog = null;

    public static void show(Context c, String msg) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(c);
            progressDialog.setCancelable(false);
        }
        progressDialog.setMessage(msg);
        progressDialog.show();
    }


    public static void updateProgress(int progress) {
        if (progressDialog != null) {
            progressDialog.setProgress(progress);
        }
    }


    public static void hide() {
        if (progressDialog != null)
            progressDialog.dismiss();
        progressDialog = null;
    }



}
