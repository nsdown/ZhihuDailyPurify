package io.github.izzyleung.zhihudailypurify.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import io.github.izzyleung.zhihudailypurify.R;

public final class ApacheLicenseDialog {
    private Activity fActivity;

    public ApacheLicenseDialog(Activity context) {
        fActivity = context;
    }

    public void show() {
        final Dialog apacheLicenseDialog = new Dialog(fActivity);
        apacheLicenseDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        apacheLicenseDialog.setCancelable(true);
        apacheLicenseDialog.setContentView(R.layout.dialog_apache_license);

        TextView textView = (TextView) apacheLicenseDialog
                .findViewById(R.id.dialog_text);

        StringBuilder sb = new StringBuilder();
        sb.append(fActivity.getString(R.string.licences_header));

        String[] basedOnProjects = fActivity.getResources().getStringArray(
                R.array.apache_licensed_projects);

        for (String str : basedOnProjects) {
            sb.append("• ").append(str).append("\n");
        }

        sb.append("\n").append(
                fActivity.getString(R.string.licenses_subheader));

        textView.setText(sb.toString());

        apacheLicenseDialog.findViewById(R.id.close_dialog_button)
                .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        apacheLicenseDialog.dismiss();
                    }
                });

        apacheLicenseDialog.show();
    }
}
