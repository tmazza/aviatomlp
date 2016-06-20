import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public class TextureLoad {
	private static final int BYTES_PER_PIXEL = 4;
	public int textureID = -1;
	 
    public int newTexture(String filename)
    {

    	BufferedImage test = null;
		try {
			test = ImageIO.read(new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
    	/*
    	Graphics2D g2d = test.createGraphics();
    	g2d.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
        g2d.fillRect(0, 0, 128, 128); //A transparent white background
        
        g2d.setColor(Color.red);
        g2d.drawRect(0, 0, 127, 127); //A red frame around the image
        g2d.fillRect(10, 10, 10, 10); //A red box 
        
        g2d.setColor(Color.blue);
        g2d.drawString("Test image", 10, 64); //Some blue text
    	*/
    	test.flush();
    	textureID = loadTexture(test);
    	
    	return textureID;
    }
 
    private static int loadTexture(BufferedImage image){
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = BufferUtils.createByteBuffer(image.getWidth() * image.getHeight() * BYTES_PER_PIXEL); //4 for RGBA, 3 for RGB
         for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));    // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));     // Green component
                buffer.put((byte) (pixel & 0xFF));            // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }
        buffer.flip();

        int textureID = GL11.glGenTextures(); //Generate texture ID
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); //Bind texture ID
        //Setup wrap mode
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        //Setup texture scaling filtering
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        //Send texel data to OpenGL
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        //Return the texture ID so we can bind it later again
        return textureID;
    }
}
