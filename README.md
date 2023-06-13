[中文文档](README-ZH.md)

NyxPrinterClient
==========
### 
The demo for Android Studio has full functionality, such as printing text, printing barcodes, printing qr code, printing pictures and scanning. Please import project by Android Studio to get the detailed instructions for use.
###

## Printer SDK integration
Nyx Printer is using AIDL integration. About AIDL, please refer to [https://developer.android.com/guide/components/aidl](https://developer.android.com/guide/components/aidl)

### Integration file description
- [net.nyx.printerservice.print.IPrinterService.aidl](app/src/main/aidl/net/nyx/printerservice/print/IPrinterService.aidl) —— the aidl interface for all printer functions
- [net.nyx.printerservice.print.PrintTextFormat.aidl](app/src/main/aidl/net/nyx/printerservice/print/PrintTextFormat.aidl) —— the aidl bean class to set the print text style
- [net.nyx.printerservice.print.PrintTextFormat.java](app/src/main/java/net/nyx/printerservice/print/PrintTextFormat.java) —— the java bean class to set the print text style

### Integration
1. Add the above three files to the project and cannot modify the package path and package name
2. Add query tag in `AndroidManifest.xml` to adapt `android 11 package visibility` for Android 12 platform
```xml
<queries>
    <package android:name="net.nyx.printerservice"/>
</queries>
```
3. Bind printer AIDL service
```
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
```

## Printer

### Print text/bitmap/barcode
PrintTextFormat: the bean class to custom text style, like text size, alignment, line spacing, custom font.
```
try {
    PrintTextFormat textFormat = new PrintTextFormat();
    // textFormat.setTextSize(32);
    // textFormat.setUnderline(true);
    int ret = printerService.printText(text, textFormat);
    ret = printerService.printBarcode("123456789", 300, 160, 1, 1);
    ret = printerService.printQrCode("123456789", 300, 300, 1);
    if (ret == 0) {
        paperOut();
    }
} catch (RemoteException e) {
    e.printStackTrace();
}
```

For custom print font, **font path needs to be set as a public path**. Font placed in `assets` directory or application private directory will not take effect
```
try {
    PrintTextFormat textFormat = new PrintTextFormat();
    textFormat.setFont(5);
    textFormat.setPath("/sdcard/TLAsc.ttf");
    int ret = printerService.printText(text, textFormat);
} catch (RemoteException e) {
    e.printStackTrace();
}
```

### Print label
There are two ways to print label

#### 1. Know the exact dimensions of the label (pixels)
The content of the printed label needs to be included between `printerService.labelLocate()` and `printerService.labelPrintEnd()`

```
private void printLabel() {
    singleThreadExecutor.submit(new Runnable() {
        @Override
        public void run() {
            try {
                int ret = printerService.labelLocate(240, 16);
                if (ret == 0) {
                    PrintTextFormat format = new PrintTextFormat();
                    printerService.printText("/nModel:/t/tNB55", format);
                    printerService.printBarcode("1234567890987654321", 320, 90, 2, 0);
                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                    printerService.printText("Time:/t/t" + date, format);
                    ret = printerService.labelPrintEnd();
                }
                showLog("Print label: " + msg(ret));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });
}
```
#### 2. Label learning
Label learning will automatically output label paper for a certain distance to get the params about the label paper. After the interface returns successfully, include the printed content between `printerService.labelLocateAuto()` and `printerService.labelPrintEnd()`
- `printerService.hasLabelLearning()`: whether the system has already performed label learning
- `printerService.clearLabelLearning()`: clear the system storaged the label learning result
```
private void printLabelLearning() {
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
                        printerService.printText("/nModel:/t/tNB55", format);
                        printerService.printBarcode("1234567890987654321", 320, 90, 2, 0);
                        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                        printerService.printText("Time:/t/t" + date, format);
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
```

### Printer result
All the printer interfaces will return the integer result, please refer to [SdkResult.java](app/src/main/java/net/nyx/printerclient/SdkResult.java)

## Scanner
### Camera scanner
Just start a system activity to get built-in camera scanner. The capture surface cannot be customized.

```
private void scan() {
    Intent intent = new Intent();
    intent.setComponent(new ComponentName("net.nyx.scanner",
            "net.nyx.scanner.ScannerActivity"));
    // set the capture activity actionbar title
    //intent.putExtra("TITLE", "Scan");
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
```    

### Infrared scan
    
Register the system broadcast to get the infrared scan result
    
```
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
```

## NFC
NFC uses the Android general NFC module, the specific introduction can refer to [Android NFC](https://developer.android.google.cn/guide/topics/connectivity/nfc)

Card reading can refer to the following projects
- [MifareClassicTool](https://github.com/ikarus23/MifareClassicTool)
- [EMV-NFC-Paycard-Enrollment](https://github.com/devnied/EMV-NFC-Paycard-Enrollment)

