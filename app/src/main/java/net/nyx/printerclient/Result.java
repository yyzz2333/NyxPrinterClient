package net.nyx.printerclient;

public class Result {

    public static String msg(int code) {
        String s;
        switch (code) {
            case SdkResult.SDK_SENT_ERR:
                s = "数据发送错误";
                break;
            case SdkResult.SDK_RECV_ERR:
                s = "数据接收错误";
                break;
            case SdkResult.SDK_TIMEOUT:
                s = "通讯超时";
                break;
            case SdkResult.SDK_PARAM_ERR:
                s = "数据参数错误";
                break;
            case SdkResult.SDK_UNKNOWN_ERR:
                s = "未知异常";
                break;
            case SdkResult.DEVICE_NOT_CONNECT:
                s = "设备未连接";
                break;
            case SdkResult.DEVICE_DISCONNECT:
                s = "设备断开连接";
                break;
            case SdkResult.DEVICE_CONN_ERR:
                s = "设备连接错误";
                break;
            case SdkResult.DEVICE_CONNECTED:
                s = "设备已连接";
                break;
            case SdkResult.DEVICE_NOT_SUPPORT:
                s = "设备不支持";
                break;
            case SdkResult.DEVICE_NOT_FOUND:
                s = "设备未找到";
                break;
            case SdkResult.DEVICE_OPEN_ERR:
                s = "设备打开错误";
                break;
            case SdkResult.DEVICE_NO_PERMISSION:
                s = "设备无权限";
                break;
            case SdkResult.BT_NOT_OPEN:
                s = "蓝牙未打开";
                break;
            case SdkResult.BT_NO_LOCATION:
                s = "定位未开启";
                break;
            case SdkResult.BT_NO_BONDED_DEVICE:
                s = "无绑定设备";
                break;
            case SdkResult.BT_SCAN_TIMEOUT:
                s = "蓝牙扫描超时";
                break;
            case SdkResult.BT_SCAN_ERR:
                s = "蓝牙扫描错误";
                break;
            case SdkResult.BT_SCAN_STOP:
                s = "蓝牙扫描停止";
                break;
            case SdkResult.PRN_COVER_OPEN:
                s = "打印机仓盖未关闭";
                break;
            case SdkResult.PRN_PARAM_ERR:
                s = "打印参数错误";
                break;
            case SdkResult.PRN_NO_PAPER:
                s = "打印机缺纸";
                break;
            case SdkResult.PRN_OVERHEAT:
                s = "打印机过热";
                break;
            case SdkResult.PRN_UNKNOWN_ERR:
                s = "打印机未知异常";
                break;
            case SdkResult.PRN_PRINTING:
                s = "打印机正在打印";
                break;
            case SdkResult.PRN_NO_NFC:
                s = "打印机无NFC标签";
                break;
            case SdkResult.PRN_NFC_NO_PAPER:
                s = "打印机NFC标签没有剩余次数";
                break;
            case SdkResult.PRN_LOW_BATTERY:
                s = "打印机低电量";
                break;
            case SdkResult.PRN_LBL_LOCATE_ERR:
                s = "标签定位失败";
                break;
            case SdkResult.PRN_LBL_DETECT_ERR:
                s = "标签纸检测错误";
                break;
            case SdkResult.PRN_LBL_NO_DETECT:
                s = "未检测到标签纸";
                break;
            case SdkResult.PRN_UNKNOWN_CMD:
            case SdkResult.SDK_UNKNOWN_CMD:
                s = "未知指令";
                break;
            default:
                s = "" + code;
                break;
        }
        return s;
    }
}
