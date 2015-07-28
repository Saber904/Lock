package com.example.eleven.lock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.File;


public class MainActivity extends ActionBarActivity {

    private Button btn_setlock;
    private Button btn_unlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("app", "onCreate");
        setContentView(R.layout.activity_main);

        btn_setlock = (Button) findViewById(R.id.reset);
        btn_unlock = (Button) findViewById(R.id.tryunlock);

        File file1 = new File("/data/data/" + getPackageName() + "/files/firstMfcc.dat");
        File file2 = new File("/data/data/" + getPackageName() + "/files/secondMfcc.dat");

        if (!file1.exists() || !file2.exists()) {
            btn_unlock.setVisibility(View.GONE);
            btn_setlock.setText(R.string.set_lock);
        }

        btn_setlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SetlockActivity.class);
                startActivity(intent);
            }
        });

        btn_unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, UnlockActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
