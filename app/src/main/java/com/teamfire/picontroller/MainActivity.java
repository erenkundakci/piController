package com.teamfire.picontroller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {

    Button btn_down, btn_up, btn_left, btn_right, btn_autodrive, btn_camera;
    WebView wb_liveFeed;
    EditText ipAddress;
    private static final int REQ_CODE = 123;
    public static String wifiModuleIP;
    public static int wifiModulePort;
    public static int CMD;
    public String newUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_down = findViewById(R.id.btn_down);
        btn_up = findViewById(R.id.btn_up);
        btn_left = findViewById(R.id.btn_left);
        btn_right = findViewById(R.id.btn_right);
        btn_autodrive = findViewById(R.id.btn_autodrive);
        btn_camera = findViewById(R.id.btn_camera);
        wb_liveFeed = findViewById(R.id.wb_liveFeed);
        ipAddress = findViewById(R.id.ipAddress);
        SharedPreferences prefs = getSharedPreferences("IP", MODE_PRIVATE);
        String lastIPAddress = prefs.getString("IP", null);
        ipAddress.setText(lastIPAddress);

        btn_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getIPandPort();
                wb_liveFeed.getSettings().setJavaScriptEnabled(true);
                wb_liveFeed.getSettings().setUseWideViewPort(true);
                wb_liveFeed.getSettings().setLoadWithOverviewMode(true);
                wb_liveFeed.setWebViewClient(new WebViewClient());
                newUrl = "http://" + wifiModuleIP + ":8000/index.html";
                wb_liveFeed.loadData("<iframe src='" + newUrl + "' style='border: 0; width: 100%; height: 100%'></iframe>", "text/html; charset=utf-8", "UTF-8");
            }
        });

        btn_autodrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AutoDriveActivity.class);
                intent.putExtra("IP_ADDRESS", ipAddress.getText().toString());
                startActivityForResult(intent, REQ_CODE);
            }
        });

        btn_up.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_up.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.forwardPressed));
                        btn_down.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_up.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.forward));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_down.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 8;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_down.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_down.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.backwardPressed));
                        btn_up.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_down.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.backward));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_up.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 2;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_right.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_right.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safePressed));
                        btn_left.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_right.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safe));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_left.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 6;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });

        btn_left.setOnTouchListener(new View.OnTouchListener() {
            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null) return true;
                        mHandler = new Handler();
                        btn_left.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safePressed));
                        btn_right.setEnabled(false);
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null) return true;
                        mHandler.removeCallbacks(mAction);
                        btn_left.setBackgroundTintList(ContextCompat.getColorStateList(MainActivity.this, R.color.safe));
                        mHandler = new Handler();
                        mHandler.post(mStop);
                        btn_right.setEnabled(true);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 4;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                    mHandler.postDelayed(this, 100);
                }
            };
            Runnable mStop = new Runnable() {
                @Override
                public void run() {
                    getIPandPort();
                    CMD = 0;
                    SocketAsyncTask cmd_increase_servo = new SocketAsyncTask();
                    cmd_increase_servo.execute();
                }
            };
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences.Editor editor = getSharedPreferences("IP", MODE_PRIVATE).edit();
        editor.putString("IP", ipAddress.getText().toString());
        editor.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQ_CODE): {
                if (resultCode == Activity.RESULT_OK) {
                    String newText = data.getStringExtra("IP_ADDRESS_FROM_AUTODRIVE");
                    ipAddress.setText(newText);
                }
                break;
            }
        }
    }

    public void getIPandPort() {
        String iPandPort = ipAddress.getText().toString();
        String temp[] = iPandPort.split(":");
        wifiModuleIP = temp[0];
        wifiModulePort = Integer.valueOf(temp[1]);
    }

    public class SocketAsyncTask extends AsyncTask<Void, Void, Void> {
        Socket socket;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                InetAddress inetAddress = InetAddress.getByName(MainActivity.wifiModuleIP);
                socket = new java.net.Socket(inetAddress, MainActivity.wifiModulePort);
                PrintStream printStream = new PrintStream(socket.getOutputStream());
                printStream.print(CMD);
                printStream.close();
                socket.close();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
