package org.moboxlab.moboxbot.API.Util;

import java.util.List;

/**
 * 插件可用的文本工具
 */
public class Text {
    public static String safe(String text) {
        return text == null ? "" : text;
    }

    public static boolean empty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static String join(List<String> list,String separator) {
        if (list == null || list.isEmpty()) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) builder.append(separator);
            builder.append(list.get(i));
        }
        return builder.toString();
    }
}
