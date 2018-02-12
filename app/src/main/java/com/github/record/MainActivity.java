package com.github.record;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.record.widget.RecordLayout;

public class MainActivity extends AppCompatActivity implements RecordLayout.IRecordListener {

    private RecordLayout recordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordLayout = (RecordLayout) findViewById(R.id.record_layout);
        recordLayout.setRecordListener(this);
    }

    @Override
    public void onRecordStart() {
        Log.d("RecordLayout", "---onRecordStart---");
    }

    @Override
    public void onRecordEnd() {
        Log.d("RecordLayout", "---onRecordEnd---");
    }
}
