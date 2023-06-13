NyxPrinterClient
==========
###
该Demo详细展示了Nyx Pos主要功能，包含：
- 打印文本
- 打印条形码/二维码
- 打印标签
- 标签学习
- 摄像头扫码
- 红外线扫码
###

## 打印SDK集成
Demo使用AIDL进行集成。有关AIDL，请参考 [https://developer.android.com/guide/components/aidl](https://developer.android.com/guide/components/aidl)

### 主要文件说明
- [net.nyx.printerservice.print.IPrinterService.aidl](app/src/main/aidl/net/nyx/printerservice/print/IPrinterService.aidl) —— the aidl interface for all printer functions
- [net.nyx.printerservice.print.PrintTextFormat.aidl](app/src/main/aidl/net/nyx/printerservice/print/PrintTextFormat.aidl) —— the aidl bean class to set the print text style
- [net.nyx.printerservice.print.PrintTextFormat.java](app/src/main/java/net/nyx/printerservice/print/PrintTextFormat.java) —— the java bean class to set the print text style

### 集成
1. 在项⽬中添加上述三个⽂件且不能修改包路径和包名
2. Android12在`AndroidManifest.xml`添加标签以适配 `android 11 package visibility`
```xml
<queries>
    <package android:name="net.nyx.printerservice"/>
</queries>
```
3. 绑定AIDL service
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
4. 使用`printerService`调用 AIDL接口中定义的⽅法进行打印

## Printer

### 打印文本/图片/条码
[PrintTextFormat](app/src/main/java/net/nyx/printerservice/print/PrintTextFormat.java): java bean类，设置打印文本样式，包括文本大小、文本样式、文本对齐方式、文本字库等
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

自定义打印字库，**路径需要设置为公有路径**，字库放在`assets`目录或应用私有目录将不会生效
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

### 打印标签

打印标签有两种方式
#### 1. 清楚标签的具体尺寸（像素）
打印标签内容需要被包含在 `printerService.labelLocate()` 和 `printerService.labelPrintEnd()` 中间
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
#### 2. 标签自动检测，即标签学习
标签学习会自动走出一段距离标签纸，用于检测标签纸尺寸等相关参数，待接口返回成功后，即可将打印内容包含在`printerService.labelLocateAuto()` 和 `printerService.labelPrintEnd()`中间
- `printerService.hasLabelLearning()`：判断系统是否已经进行过相关学习
- `printerService.clearLabelLearning()`：可以清除系统对标签相关参数的存储
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

### 打印结果
所有打印接口都返回int类型结果，参考 [SdkResult.java](app/src/main/java/net/nyx/printerclient/SdkResult.java) 对打印结果进行相关处理

## 扫码

### 摄像头扫描
只需要启动系统活动即可获得内置相机扫描仪。该方式无法自定义扫码界面。

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

### 红外线扫码

注册系统广播以获得红外扫码结果    
    
```
private final BroadcastReceiver qscReceiver = new BroadcastReceiver() {
                                          
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("com.android.NYX_QSC_DATA".equals(intent.getAction())) {
            String qsc = intent.getStringExtra("qsc");
            showLog("qsc scan result: %s", qsc);
            printText("qsc-quick-scan-code/n" + qsc);
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
NFC使用Android通用NFC模块，具体介绍可参考[Android NFC](https://developer.android.google.cn/guide/topics/connectivity/nfc)

读卡相关可以参考以下项目
- [MifareClassicTool](https://github.com/ikarus23/MifareClassicTool)
- [EMV-NFC-Paycard-Enrollment](https://github.com/devnied/EMV-NFC-Paycard-Enrollment)
