package org.moboxlab.moboxbot.Util;

import org.moboxlab.moboxbot.BasicInfo;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 配置文件安全写回
 * 只改目标键，保留注释、键顺序和换行风格。
 */
public class ConfigEditor {
    public static boolean writeString(String key,String value) {
        if (key == null || key.trim().isEmpty()) return false;
        String path = BasicInfo.runDir+"/config.yml";
        try {
            String text = new String(Files.readAllBytes(Paths.get(path)),StandardCharsets.UTF_8);
            String newline = text.contains("\r\n") ? "\r\n" : "\n";
            String[] lines = text.split("\n",-1);
            boolean replaced = false;
            for (int i = 0; i < lines.length; i++) {
                String raw = lines[i];
                String line = raw.endsWith("\r") ? raw.substring(0,raw.length() - 1) : raw;
                String trim = line.trim();
                if (!trim.startsWith(key+":")) continue;
                int keyIndex = line.indexOf(key+":");
                String indent = keyIndex > 0 ? line.substring(0,keyIndex) : "";
                int commentIndex = line.indexOf('#');
                String comment = commentIndex >= 0 ? line.substring(commentIndex).trim() : "";
                String newLine = indent+key+": \""+escape(value)+"\"";
                if (!comment.isEmpty()) newLine = newLine+" "+comment;
                lines[i] = newLine;
                replaced = true;
                break;
            }
            if (!replaced) {
                String newLine = key+": \""+escape(value)+"\"";
                StringBuilder builder = new StringBuilder(text);
                if (!text.endsWith("\n") && !text.endsWith("\r")) builder.append(newline);
                builder.append(newLine).append(newline);
                text = builder.toString();
            } else {
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    builder.append(lines[i]);
                    if (i < lines.length - 1) builder.append(newline);
                }
                text = builder.toString();
            }
            Files.write(Paths.get(path),text.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (Exception e) {
            BasicInfo.sendException(e);
            return false;
        }
    }

    private static String escape(String value) {
        String text = value == null ? "" : value;
        return text.replace("\\","\\\\").replace("\"","\\\"");
    }
}
