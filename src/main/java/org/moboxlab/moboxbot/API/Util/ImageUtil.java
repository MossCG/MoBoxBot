package org.moboxlab.moboxbot.API.Util;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;

/**
 * 插件图片渲染工具
 * 金色猫娘主题，右下角使用本地角色图作为底图。
 */
public class ImageUtil {
    private static final int WIDTH = 760;
    private static final int PADDING = 28;
    private static final int TITLE_HEIGHT = 54;
    private static final int LINE_HEIGHT = 34;
    private static final int IMAGE_SIZE = 300;

    private static BufferedImage characterImage;
    private static boolean characterLoaded = false;

    public static byte[] renderText(String title,List<String> lines) {
        int lineCount = lines == null ? 0 : lines.size();
        int contentHeight = TITLE_HEIGHT + 20 + Math.max(lineCount * LINE_HEIGHT,IMAGE_SIZE);
        int height = PADDING * 2 + contentHeight;
        if (height < 420) height = 420;

        BufferedImage image = new BufferedImage(WIDTH,height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            //金色渐变背景
            graphics.setPaint(new GradientPaint(
                    0,0,new Color(255,249,232),
                    0,height,new Color(253,230,138)));
            graphics.fillRect(0,0,WIDTH,height);

            //左侧金色强调条
            graphics.setColor(new Color(245,158,11));
            graphics.fillRoundRect(PADDING,PADDING - 10,6,TITLE_HEIGHT - 8,6,6);

            graphics.setFont(new Font("Microsoft YaHei",Font.BOLD,28));
            graphics.setColor(new Color(120,53,15));
            graphics.drawString(title == null ? "" : title,PADDING + 20,PADDING + 30);

            graphics.setColor(new Color(245,158,11,150));
            graphics.drawLine(PADDING,PADDING + TITLE_HEIGHT - 8,WIDTH - PADDING,PADDING + TITLE_HEIGHT - 8);

            int textWidth = WIDTH - PADDING * 2 - 20 - IMAGE_SIZE - 20;
            graphics.setFont(new Font("Microsoft YaHei",Font.PLAIN,20));
            graphics.setColor(new Color(74,44,10));
            if (lines != null) {
                int y = PADDING + TITLE_HEIGHT + 18;
                for (String line : lines) {
                    graphics.drawString(fitText(graphics,line == null ? "" : line,textWidth),PADDING + 20,y);
                    y += LINE_HEIGHT;
                }
            }

            drawCharacter(graphics,height);
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

    private static void drawCharacter(Graphics2D graphics,int height) {
        BufferedImage image = loadCharacter();
        if (image == null) return;
        int x = WIDTH - PADDING - IMAGE_SIZE;
        int y = height - PADDING - IMAGE_SIZE;
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.92f));
            copy.setClip(new RoundRectangle2D.Float(x,y,IMAGE_SIZE,IMAGE_SIZE,24,24));
            copy.drawImage(image.getScaledInstance(IMAGE_SIZE,IMAGE_SIZE,Image.SCALE_SMOOTH),x,y,null);
        } finally {
            copy.dispose();
        }
        graphics.setColor(new Color(245,158,11,220));
        graphics.setStroke(new java.awt.BasicStroke(3f));
        graphics.drawRoundRect(x,y,IMAGE_SIZE,IMAGE_SIZE,24,24);
    }

    private static BufferedImage loadCharacter() {
        if (characterLoaded) return characterImage;
        characterLoaded = true;
        InputStream input = null;
        try {
            input = ImageUtil.class.getResourceAsStream("/images/golden-neko.png");
            if (input != null) characterImage = ImageIO.read(input);
        } catch (Exception e) {
            characterImage = null;
        } finally {
            try {
                if (input != null) input.close();
            } catch (Exception ignored) {
            }
        }
        return characterImage;
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
