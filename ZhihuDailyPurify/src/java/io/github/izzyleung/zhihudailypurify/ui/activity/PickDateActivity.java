package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.Toast;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.support.util.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PickDateActivity extends ActionBarActivity {
    private Calendar pickedDate = Calendar.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_date);

        try {
            pickedDate.setTime(DateUtils.simpleDateFormat.parse(getIntent().getStringExtra("date")));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        DatePicker datePicker = (DatePicker) findViewById(R.id.date_picker);
        datePicker.init(pickedDate.get(Calendar.YEAR),
                pickedDate.get(Calendar.MONTH),
                pickedDate.get(Calendar.DAY_OF_MONTH),
                new DatePicker.OnDateChangedListener() {

                    @Override
                    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        pickedDate.set(year, monthOfYear, dayOfMonth);
                    }
                }
        );

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pick_date, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_go_to_date:
                if (pickedDate.after(Calendar.getInstance())) {
                    Toast.makeText(PickDateActivity.this, getString(R.string.not_coming), Toast.LENGTH_SHORT).show();
                } else {
                    if (pickedDate.after(DateUtils.birthDay)
                            || DateUtils.isSameDay(pickedDate, DateUtils.birthDay)) {
                        pickedDate.add(Calendar.DAY_OF_YEAR, 1);

                        String date = DateUtils.simpleDateFormat.
                                format(pickedDate.getTime());

                        //Recover time
                        pickedDate.add(Calendar.DAY_OF_YEAR, -1);

                        String displayDate = new SimpleDateFormat(getString(R.string.display_format)).
                                format(pickedDate.getTime());

                        Intent intent = new Intent();
                        intent.setClass(PickDateActivity.this, PortalActivity.class);
                        intent.putExtra("date", date);
                        intent.putExtra("display_date", displayDate);
                        startActivity(intent);
                        this.finish();
                    } else {
                        Toast.makeText(PickDateActivity.this, getString(R.string.not_born), Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}