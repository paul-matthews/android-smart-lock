package com.google.arthurthompson.smartlockcodelab;

import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class ChangeCredActivity extends AppCompatActivity {

    private TextInputLayout mUsername1TextInputLayout;
    private TextInputLayout mPassword1TextInputLayout;
    private TextInputLayout mUsername2TextInputLayout;
    private TextInputLayout mPassword2TextInputLayout;
    private TextInputLayout mUsername3TextInputLayout;
    private TextInputLayout mPassword3TextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_cred);

        mUsername1TextInputLayout = (TextInputLayout) findViewById(R.id.username1TextInputLayout);
        mPassword1TextInputLayout = (TextInputLayout) findViewById(R.id.password1TextInputLayout);
        mUsername2TextInputLayout = (TextInputLayout) findViewById(R.id.username2TextInputLayout);
        mPassword2TextInputLayout = (TextInputLayout) findViewById(R.id.password2TextInputLayout);
        mUsername3TextInputLayout = (TextInputLayout) findViewById(R.id.username3TextInputLayout);
        mPassword3TextInputLayout = (TextInputLayout) findViewById(R.id.password3TextInputLayout);

        mUsername1TextInputLayout.getEditText().setText(UsernamesAndPasswords.username1);
        mPassword1TextInputLayout.getEditText().setText(UsernamesAndPasswords.password1);
        mUsername2TextInputLayout.getEditText().setText(UsernamesAndPasswords.username2);
        mPassword2TextInputLayout.getEditText().setText(UsernamesAndPasswords.password2);
        mUsername3TextInputLayout.getEditText().setText(UsernamesAndPasswords.username3);
        mPassword3TextInputLayout.getEditText().setText(UsernamesAndPasswords.password3);

        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsernamesAndPasswords.username1 = mUsername1TextInputLayout.getEditText().getText().toString();
                UsernamesAndPasswords.password1 = mPassword1TextInputLayout.getEditText().getText().toString();
                UsernamesAndPasswords.username2 = mUsername2TextInputLayout.getEditText().getText().toString();
                UsernamesAndPasswords.password2 = mPassword2TextInputLayout.getEditText().getText().toString();
                UsernamesAndPasswords.username3 = mUsername3TextInputLayout.getEditText().getText().toString();
                UsernamesAndPasswords.password3 = mPassword3TextInputLayout.getEditText().getText().toString();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_cred, menu);
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
