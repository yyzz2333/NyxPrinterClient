package net.nyx.printerservice.print;

import android.graphics.Bitmap;
import net.nyx.printerservice.print.PrintTextFormat;

interface IPrinterService {

    String getServiceVersion();

    int getPrinterVersion(out String[] ver);

    int getPrinterModel(out String[] model);

    int getPrinterStatus();

    int paperOut(int px);

    int paperBack(int px);

    int printText(String text, in PrintTextFormat textFormat);

    /**
     * 打印文字
     *
     * @param text       文本内容
     * @param textFormat 文字样式
     * @param textWidth  最大文字宽度, <384px
     * @param align      最大文字宽度相对于384px打印纸的对齐方式, 默认0. 0--居左, 1--居中, 2--居右
     * @return 打印结果
     */
    int printText2(String text, in PrintTextFormat textFormat, int textWidth, int align);

    /**
     * 打印条码
     *
     * @param content      条码内容
     * @param width        条码宽度
     * @param height       条码高度
     * @param textPosition 文字位置, 默认0. 0--不打印文字, 1--文字在条码上方, 2--文字在条码下方, 3--条码上下方均打印
     * @param align        对齐方式, 默认0. 0--居左, 1--居中, 2--居右
     * @return 打印结果
     */
    int printBarcode(String content, int width, int height, int textPosition, int align);

    /**
     * 打印二维码
     *
     * @param content 二维码内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @param align   对齐方式, 默认0. 0--居左, 1--居中, 2--居右
     * @return 打印结果
     */
    int printQrCode(String content, int width, int height, int align);

    /**
     * 打印图片
     *
     * @param bitmap 图片bitmap对象
     * @param type   打印方式, 默认0. 0--黑白化图片, 1--灰度图片, 适合色彩丰富图片
     * @param align  对齐方式, 默认0. 0--居左, 1--居中, 2--居右
     * @return 打印结果
     */
    int printBitmap(in Bitmap bitmap, int type, int align);

    /**
     * 定位下一张标签
     *
     * @param labelHeight 标签纸高度, 单位px
     * @param labelGap    标签纸间距, 单位px
     * @return 定位结果
     */
    int labelLocate(int labelHeight, int labelGap);

    /**
     * 标签打印结束, 根据设置参数自动走纸至撕纸处
     *
     * @return 接口结果
     */
    int labelPrintEnd();

    /**
     * 自动定位标签
     *
     * @return 定位结果
     */
    int labelLocateAuto();

    /**
     * 标签自动检测
     *
     * @return 接口结果
     */
    int labelDetectAuto();

    /**
     * 已进行标签学习
     *
     * @return 接口结果
     */
    boolean hasLabelLearning();

    /**
     * 清除标签学习结果
     *
     * @return 接口结果
     */
    int clearLabelLearning();

}
