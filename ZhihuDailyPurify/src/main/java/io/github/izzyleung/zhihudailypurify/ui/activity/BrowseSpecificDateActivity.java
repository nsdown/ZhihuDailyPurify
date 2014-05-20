package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;
import io.github.izzyleung.zhihudailypurify.ui.fragment.NewsListFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BrowseSpecificDateActivity extends ActionBarActivity {
    private String displayDate, dateForFragment;
    private Calendar calendar = Calendar.getInstance();
    private Fragment displayFragment;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_specific_date);

        dateForFragment = getIntent().getStringExtra("date");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = new Bundle();
        bundle.putBoolean("first_page?", false);
        bundle.putBoolean("single?", true);
        bundle.putString("date", dateForFragment);

        displayFragment = new NewsListFragment();
        displayFragment.setArguments(bundle);

        try {
            calendar.setTime(DateUtils.simpleDateFormat.parse(dateForFragment));
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            displayDate =
                    new SimpleDateFormat(getString(R.string.display_format)).
                            format(calendar.getTime());
            getSupportActionBar().setTitle(displayDate);
        } catch (ParseException ignored) {

        }

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        if (pref.getBoolean("accelerate_server_hint", true)) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this).setCancelable(false);
            dialog.setTitle(getString(R.string.accelerate_server_hint_dialog_title));
            dialog.setMessage(getString(R.string.accelerate_server_hint_dialog_message));
            dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    pref.edit().putBoolean("using_accelerate_server?", true).commit();

                    if (savedInstanceState == null) {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.container, displayFragment)
                                .commit();
                    }

                    pref.edit().putBoolean("accelerate_server_hint", false).commit();
                }
            });

            dialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (savedInstanceState == null) {
                        getSupportFragmentManager().beginTransaction()
                                .add(R.id.container, displayFragment)
                                .commit();
                    }

                    pref.edit().putBoolean("accelerate_server_hint", false).commit();
                }
            });

            dialog.show();
        } else {
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, displayFragment)
                        .commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("date", DateUtils.simpleDateFormat.format(calendar.getTime()));
        intent.setClass(BrowseSpecificDateActivity.this, PickDateActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browse_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.forward:
                Calendar tempCalendar = Calendar.getInstance();
                if (DateUtils.isSameDay(tempCalendar, calendar)) {
                    Crouton.makeText(this,
                            getString(R.string.this_is_today),
                            Style.INFO).show();
                    return true;
                }
                updateFields(1);
                updateView();
                return true;
            case R.id.back:
                if (DateUtils.isSameDay(DateUtils.birthDay, calendar)) {
                    Crouton.makeText(this,
                            getString(R.string.this_is_birthday),
                            Style.INFO).show();
                    return true;
                }
                updateFields(2);
                updateView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateFields(int which) {
        if (which == 1) {
            calendar.add(Calendar.DAY_OF_YEAR, 2);
            dateForFragment = DateUtils.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        } else {
            dateForFragment = DateUtils.simpleDateFormat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }
    }

    private void updateView() {
        Bundle bundle = new Bundle();
        bundle.putBoolean("auto_refresh?", true);
        bundle.putBoolean("single?", true);
        bundle.putString("date", dateForFragment);

        if (DateUtils.isSameDay(calendar, Calendar.getInstance())) {
            bundle.putBoolean("first_page?", true);
        } else {
            bundle.putBoolean("first_page?", false);
        }

        displayFragment = new NewsListFragment();
        displayFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, displayFragment)
                .commit();

        displayDate = new SimpleDateFormat(getString(R.string.display_format)).
                format(calendar.getTime());

        getSupportActionBar().setTitle(displayDate);
    }
}