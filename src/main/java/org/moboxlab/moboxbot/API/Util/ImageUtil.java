package org.moboxlab.moboxbot.API.Util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * 插件图片渲染工具
 * 把标题和文本行渲染成 PNG，并转换为 OneBot 可用的 base64 URI。
 */
public class ImageUtil {
    private static final int WIDTH = 760;
    private static final int PADDING = 28;
    private static final int TITLE_HEIGHT = 54;
    private static final int LINE_HEIGHT = 34;

    public static byte[] renderText(String title,List<String> lines) {
        int lineCount = lines == null ? 0 : lines.size();
        int height = PADDING * 2 + TITLE_HEIGHT + Math.max(0,lineCount) * LINE_HEIGHT;
        if (height < 120) height = 120;

        BufferedImage image = new BufferedImage(WIDTH,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            graphics.setColor(new Color(245,247,250));
            graphics.fillRect(0,0,WIDTH,height);

            graphics.setColor(new Color(29,78,216));
            graphics.fillRoundRect(PADDING,PADDING - 10,6,TITLE_HEIGHT - 8,6,6);

            graphics.setFont(new Font("Microsoft YaHei",Font.BOLD,28));
            graphics.setColor(new Color(17,24,39));
            graphics.drawString(title == null ? "" : title,PADDING + 20,PADDING + 30);

            graphics.setColor(new Color(209,213,219));
            graphics.drawLine(PADDING,PADDING + TITLE_HEIGHT - 8,WIDTH - PADDING,PADDING + TITLE_HEIGHT - 8);

            graphics.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
            graphics.setColor(new Color(31,41,55));
            if (lines != null) {
                int y = PADDING + TITLE_HEIGHT + 18;
                for (String line : lines) {
                    graphics.drawString(line == null ? "" : line,PADDING + 20,y);
                    y += LINE_HEIGHT;
                }
            }
        } finally {
            graphics.dispose();
        }

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image,"png",output);
            return output.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 转换成 OneBot 图片消息可用的 base64 URI。
     */
    public static String toBase64Uri(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) return "";
        return "base64://"+Base64.getEncoder().encodeToString(imageBytes);
    }
}
