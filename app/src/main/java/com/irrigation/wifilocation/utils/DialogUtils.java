package com.irrigation.wifilocation.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import com.irrigation.wifilocation.R;

/**
 * Created by dalvendrakumar on 25/1/19.
 */

public class DialogUtils {


    public static AlertDialog.Builder showConfirmationDialog(Context context, String msg, final Runnable runnableSure){
        final AlertDialog.Builder builder= new AlertDialog.Builder(context,R.style.AlertDialogTheme);
        builder.setTitle(context.getString(R.string.app_name));
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setPositiveButton("Sure", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                runnableSure.run();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder;
    }
}
