package net.nyx.printerclient;

import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import net.nyx.printerclient.aop.SingleClick;
import net.nyx.printerservice.print.IPrinterService;
import net.nyx.printerservice.print.PrintTextFormat;
import timber.log.Timber;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.nyx.printerclient.Result.msg;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    protected Button btnVer;
    protected Button btnPaper;
    protected Button btn1;
    protected Button btn2;
    protected Button btn3;
    protected Button btnScan;
    protected TextView tvLog;

    private static final int RC_SCAN = 0x99;
    public static String PRN_TEXT;
    protected Button btn4;
    protected Button btnLbl;
    protected Button btnLblLearning;

    private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler();
    String[] version = new String[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        initView();
        bindService();
        registerQscScanReceiver();
        Timber.plant(new Timber.DebugTree());
        PRN_TEXT = getString(R.string.print_text);
    }


    private IPrinterService printerService;
    private ServiceConnection connService = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            showLog("printer service disconnected, try reconnect");
            printerService = null;
            // 尝试重新bind
            handler.postDelayed(() -> bindService(), 5000);
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Timber.d("onServiceConnected: %s", name);
            printerService = IPrinterService.Stub.asInterface(service);
            getVersion();
        }
    };


    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("net.nyx.printerservice");
        intent.setAction("net.nyx.printerservice.IPrinterService");
        bindService(intent, connService, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {
        unbindService(connService);
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_ver) {
            getVersion();
        } else if (view.getId() == R.id.btn_paper) {
            paperOut();
        } else if (view.getId() == R.id.btn1) {
            printText();
        } else if (view.getId() == R.id.btn2) {
            printBarcode();
        } else if (view.getId() == R.id.btn3) {
            printQrCode();
        } else if (view.getId() == R.id.btn4) {
            printBitmap();
        } else if (view.getId() == R.id.btn_scan) {
            scan();
        } else if (view.getId() == R.id.btn_lbl) {
            printLabel();
        } else if (view.getId() == R.id.btn_lbl_learning) {
            printLabelLearning();
        }
    }

    private final BroadcastReceiver qscReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.android.NYX_QSC_DATA".equals(intent.getAction())) {
                String qsc = intent.getStringExtra("qsc");
                showLog("qsc scan result: %s", qsc);
                printText("qsc-quick-scan-code\n" + qsc);
            }
        }
    };

    private void registerQscScanReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.android.NYX_QSC_DATA");
        registerReceiver(qscReceiver, filter);
    }

    private void unregisterQscReceiver() {
        unregisterReceiver(qscReceiver);
    }

    private void getVersion() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.getPrinterVersion(version);
                    showLog("Version: " + msg(ret) + "  " + version[0]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void paperOut() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    printerService.paperOut(80);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printText() {
        printText(PRN_TEXT);
    }

    private void printText(String text) {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    PrintTextFormat textFormat = new PrintTextFormat();
                    // textFormat.setTextSize(32);
                    // textFormat.setUnderline(true);
                    int ret = printerService.printText(text, textFormat);
                    showLog("Print text: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printBarcode() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.printBarcode("123456789", 300, 160, 1, 1);
                    showLog("Print text: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printQrCode() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.printQrCode("123456789", 300, 300, 1);
                    showLog("Print barcode: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printBitmap() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.printBitmap(BitmapFactory.decodeStream(getAssets().open("bmp.png")), 1, 1);
                    showLog("Print bitmap: " + msg(ret));
                    if (ret == 0) {
                        paperOut();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printLabel() {
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = printerService.labelLocate(240, 16);
                    if (ret == 0) {
                        PrintTextFormat format = new PrintTextFormat();
                        printerService.printText("\nModel:\t\tNB55", format);
                        printerService.printBarcode("1234567890987654321", 320, 90, 2, 0);
                        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        printerService.printText("Time:\t\t" + date, format);
                        ret = printerService.labelPrintEnd();
                    }
                    showLog("Print label: " + msg(ret));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void printLabelLearning() {
        if (version[0] != null && Float.parseFloat(version[0]) < 1.10) {
            showLog(getString(R.string.res_not_support));
            return;
        }
        singleThreadExecutor.submit(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                try {
                    if (!printerService.hasLabelLearning()) {
                        // label learning
                        ret = printerService.labelDetectAuto();
                    }
                    if (ret == 0) {
                        ret = printerService.labelLocateAuto();
                        if (ret == 0) {
                            PrintTextFormat format = new PrintTextFormat();
                            printerService.printText("\nModel:\t\tNB55", format);
                            printerService.printBarcode("1234567890987654321", 320, 90, 2, 0);
                            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                            printerService.printText("Time:\t\t" + date, format);
                            printerService.labelPrintEnd();
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                showLog("Label learning print: " + msg(ret));
            }
        });
    }

    private void scan() {
        if (!existApp("net.nyx.scanner")) {
            showLog("未安装scanner app");
            return;
        }
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("net.nyx.scanner",
                "net.nyx.scanner.ScannerActivity"));
        // set the capture activity actionbar title
        intent.putExtra("TITLE", "Scan");
        // show album icon, default true
        // intent.putExtra("SHOW_ALBUM", true);
        // play beep sound when get the scan result, default true
        // intent.putExtra("PLAY_SOUND", true);
        // play vibrate when get the scan result, default true
        // intent.putExtra("PLAY_VIBRATE", true);
        startActivityForResult(intent, RC_SCAN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_SCAN && resultCode == RESULT_OK && data != null) {
            String result = data.getStringExtra("SCAN_RESULT");
            showLog("Scanner result: " + result);
        }
    }

    boolean existApp(String pkg) {
        try {
            return getPackageManager().getPackageInfo(pkg, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService();
        unregisterQscReceiver();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_item_clear) {
            clearLog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        btnVer = (Button) findViewById(R.id.btn_ver);
        btnVer.setOnClickListener(MainActivity.this);
        btnPaper = (Button) findViewById(R.id.btn_paper);
        btnPaper.setOnClickListener(MainActivity.this);
        btn1 = (Button) findViewById(R.id.btn1);
        btn1.setOnClickListener(MainActivity.this);
        btn2 = (Button) findViewById(R.id.btn2);
        btn2.setOnClickListener(MainActivity.this);
        btn3 = (Button) findViewById(R.id.btn3);
        btn3.setOnClickListener(MainActivity.this);
        btnScan = (Button) findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(MainActivity.this);
        tvLog = (TextView) findViewById(R.id.tv_log);
        btn4 = (Button) findViewById(R.id.btn4);
        btn4.setOnClickListener(MainActivity.this);
        btnLbl = (Button) findViewById(R.id.btn_lbl);
        btnLbl.setOnClickListener(MainActivity.this);
        btnLblLearning = (Button) findViewById(R.id.btn_lbl_learning);
        btnLblLearning.setOnClickListener(MainActivity.this);
    }

    void showLog(String log, Object... args) {
        if (args != null && args.length > 0) {
            log = String.format(log, args);
        }
        String res = log;
        Log.e(TAG, res);
        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tvLog.getLineCount() > 100) {
                    tvLog.setText("");
                }
                tvLog.append((dateFormat.format(new Date()) + ":" + res + "\n"));
                tvLog.post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView) tvLog.getParent()).fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    void clearLog() {
        tvLog.setText("");
    }

}
