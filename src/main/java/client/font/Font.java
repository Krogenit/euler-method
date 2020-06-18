package client.font;

import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;
import static org.lwjgl.opengl.GL11.*;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import client.Texture;
import client.TextureLoader;

public class Font {

    private final Map<Character, Glyph> glyphs;
    private final Texture texture;

    private int fontHeight;

    public Font() {
        this(new java.awt.Font(MONOSPACED, PLAIN, 32), true);
    }

    public Font(java.awt.Font font, boolean antiAlias) {
        glyphs = new HashMap<>();
        texture = createFontTexture(font, antiAlias);
    }

    private Texture createFontTexture(java.awt.Font font, boolean antiAlias) {
        int imageWidth = 0;
        int imageHeight = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }
            char c = (char) i;
            BufferedImage ch = createCharImage(font, c, antiAlias);
            if (ch == null) {
                continue;
            }

            imageWidth += ch.getWidth();
            imageHeight = Math.max(imageHeight, ch.getHeight());
        }

        fontHeight = imageHeight;
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        int x = 0;

        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                continue;
            }
            char c = (char) i;
            BufferedImage charImage = createCharImage(font, c, antiAlias);
            if (charImage == null) {
                continue;
            }

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();
            Glyph ch = new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight, 0f);
            g.drawImage(charImage, x, 0, null);
            x += ch.width;
            glyphs.put(c, ch);
        }

        AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
        transform.translate(0, -image.getHeight());
        AffineTransformOp operation = new AffineTransformOp(transform,
                                                            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        image = operation.filter(image, null);

        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int pixel = pixels[i * width + j];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

		Texture fontTexture = TextureLoader.generateTexture(width, height, buffer);
        MemoryUtil.memFree(buffer);
        return fontTexture;
    }

    private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();

        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();

        if (charWidth == 0) {
            return null;
        }

        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();
        return image;
    }

    public int getHeight(CharSequence text) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                height += lineHeight;
                lineHeight = 0;
                continue;
            }
            if (c == '\r') {
                continue;
            }
            Glyph g = glyphs.get(c);
            lineHeight = Math.max(lineHeight, g.height);
        }
        height += lineHeight;
        return height;
    }

    public void render(CharSequence text, float x, float y, Vector4f color) {
        int textHeight = getHeight(text);

        float drawX = x;
        float drawY = y;
        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight;
        }
        glBindTexture(GL_TEXTURE_2D, texture.getId());
        glColor4f(color.x, color.y, color.z, color.w);
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                drawY -= fontHeight;
                drawX = x;
                continue;
            }
            else if (ch == '\r') {
                continue;
            }
            Glyph g = glyphs.get(ch);
            
            float s1 = g.x / (float)texture.getWidth();
            float t1 = g.y / (float)texture.getHeight();
            float s2 = (g.x + g.width) / (float)texture.getWidth();
            float t2 = (g.y + g.height) / (float)texture.getHeight();
            
            glPushMatrix();
            glTranslatef(drawX, drawY, 0);
            glScalef(g.width, g.height, 1);
            glBegin(GL_QUADS);
    		glTexCoord2d(s1, t2);
    		glVertex2d(0, 0);
    		glTexCoord2d(s1, t1);
    		glVertex2d(0, 1);
    		glTexCoord2d(s2, t1);
    		glVertex2d(1, 1);
    		glTexCoord2d(s2, t2);
    		glVertex2d(1, 0);
            glEnd();
            glPopMatrix();
            drawX += g.width;
        }
    }

    public void render(CharSequence text, float x, float y) {
    	render(text, x, y, new Vector4f(1,1,1,1));
    }

    public void dispose() {
        texture.delete();
    }

}