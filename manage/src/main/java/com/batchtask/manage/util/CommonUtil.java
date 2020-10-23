package com.batchtask.manage.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 * 通用工具类
 * @author yh
 */
public class CommonUtil {

    /**
     * 将异常堆栈转换为字符串
     * @param throwable 异常堆栈
     * @return 详细的堆栈信息
     */
    public static String getStackTrace(Throwable throwable) {
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        throwable.printStackTrace(printWriter);
        return result.toString();
    }
}
