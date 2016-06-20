import _root_.org.lwjgl._
import _root_.org.lwjgl.glfw._
import _root_.org.lwjgl.opengl._
import _root_.org.lwjgl.glfw.GLFW._
import _root_.org.lwjgl.opengl.GL11._
import _root_.org.lwjgl.opengl.GL13._
import _root_.org.lwjgl.opengl.GL15._
import _root_.org.lwjgl.opengl.GL20._
import _root_.org.lwjgl.opengl.GL30._
import _root_.org.lwjgl.opengl.GL32._
import _root_.org.lwjgl.system.MemoryUtil._

class TextureLocal {
  var textureID:Int = -1
  var loader:TextureLoad = new TextureLoad()
  
  def loadTexture(filename:String)
  {
    textureID = loader.newTexture(filename)
  }
  
  def bindTexture()
  {
    glActiveTexture(GL_TEXTURE0)
    glBindTexture(GL_TEXTURE_2D, textureID)
  }
  
}