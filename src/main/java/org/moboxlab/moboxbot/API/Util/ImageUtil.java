package org.moboxlab.moboxbot.API.Util;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

/**
 * 插件图片渲染工具
 * 浅色金色主题，轻量、干净，适合 QQ 图片消息。
 */
public class ImageUtil {
    private static final int WIDTH = 760;
    private static final int PADDING = 28;
    private static final int TITLE_HEIGHT = 54;
    private static final int LINE_HEIGHT = 34;

    public static byte[] renderText(String title,List<String> lines) {
        int lineCount = lines == null ? 0 : lines.size();
        int height = PADDING * 2 + TITLE_HEIGHT + 20 + Math.max(0,lineCount) * LINE_HEIGHT;
        if (height < 180) height = 180;

        BufferedImage image = new BufferedImage(WIDTH,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            //浅金渐变背景
            graphics.setPaint(new GradientPaint(
                    0,0,new Color(255,255,255),
                    0,height,new Color(255,247,230)));
            graphics.fillRect(0,0,WIDTH,height);

            //浅金色边框
            graphics.setColor(new Color(253,230,138));
            graphics.drawRoundRect(PADDING - 12,PADDING - 12,WIDTH - (PADDING - 12) * 2,height - (PADDING - 12) * 2,18,18);

            graphics.setColor(new Color(251,191,36));
            graphics.fillRoundRect(PADDING,PADDING - 10,6,TITLE_HEIGHT - 8,6,6);

            graphics.setFont(new Font("Microsoft YaHei",Font.BOLD,28));
            graphics.setColor(new Color(180,83,9));
            graphics.drawString(title == null ? "" : title,PADDING + 20,PADDING + 30);

            graphics.setColor(new Color(253,230,138));
            graphics.drawLine(PADDING,PADDING + TITLE_HEIGHT - 8,WIDTH - PADDING,PADDING + TITLE_HEIGHT - 8);

            int textWidth = WIDTH - PADDING * 2 - 40;
            graphics.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
            graphics.setColor(new Color(51,65,85));
            if (lines != null) {
                int y = PADDING + TITLE_HEIGHT + 18;
                for (String line : lines) {
                    graphics.drawString(fitText(graphics,line == null ? "" : line,textWidth),PADDING + 20,y);
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

    public static String toBase64Uri(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) return "";
        return "base64://"+Base64.getEncoder().encodeToString(imageBytes);
    }

    private static String fitText(Graphics2D graphics,String text,int maxWidth) {
        FontMetrics metrics = graphics.getFontMetrics();
        if (metrics.stringWidth(text) <= maxWidth) return text;
        String ellipsis = "...";
        int end = text.length();
        while (end > 0 && metrics.stringWidth(text.substring(0,end)+ellipsis) > maxWidth) {
            end--;
        }
        if (end <= 0) return ellipsis;
        return text.substring(0,end)+ellipsis;
    }
}
