import _root_.org.lwjgl._
import _root_.org.lwjgl.glfw._
import _root_.org.lwjgl.opengl._
import _root_.org.lwjgl.glfw.GLFW._
import _root_.org.lwjgl.opengl.GL11._
import _root_.org.lwjgl.opengl.GL15._
import _root_.org.lwjgl.opengl.GL20._
import _root_.org.lwjgl.opengl.GL30._
import _root_.org.lwjgl.opengl.GL32._
import _root_.org.lwjgl.system.MemoryUtil._
import _root_.java.nio.Buffer._
import _root_.java.nio.FloatBuffer._

object Example{
  
  var vert:Array[Float] = new Array[Float](6)
  var vbo:Int = -1;
  var vao:Int = -1;
  
  def getFloatBuffer(buffer: Array[Float]) : java.nio.FloatBuffer =
  {
    var v:java.nio.FloatBuffer = BufferUtils.createFloatBuffer(buffer.length)
    v.put(buffer)
    v.flip()
    return v
  } 
  
  def initArray()
  {
    vert(0) = -1.0f;
    vert(1) = -1.0f;
    vert(2) = 0.0f;
    vert(3) = 1.0f;
    vert(4) = 1.0f;
    vert(5) = -1.0f;
    
    vao = glGenVertexArrays()
    glBindVertexArray(vao)
    
    vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, vbo)
    glBufferData(GL_ARRAY_BUFFER, getFloatBuffer(vert), GL_STATIC_DRAW)
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
    //glBindBuffer(GL_ARRAY_BUFFER, 0)
  }
  
  def render()
  {
    glBindVertexArray(vao)
    glEnableVertexAttribArray(0)
    glDrawArrays(GL_TRIANGLES, 0, 3)
    glDisableVertexAttribArray(0)
    glBindVertexArray(0)
  }
  
  def loop(window: Long){
    GL.createCapabilities()
    initArray()
    glClearColor(0.3f, 0.3f, 0.3f, 1.0f)

    while(glfwWindowShouldClose(window) == GLFW_FALSE)
    {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT)
      
      // Render
      render()
      
      glfwSwapBuffers(window)
      glfwPollEvents()
    }
  }
  def main(args:Array[String]): Unit = {
    if(glfwInit() != GLFW_TRUE)
      throw new IllegalStateException("Unable to start GLFW")
    glfwDefaultWindowHints()
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
    glfwWindowHint(GLFW_RESIZABLE, GL_TRUE)
    val width = 800
    val height = 600
    val window = glfwCreateWindow(width, height, "Aviato", NULL, NULL)
    if(window == NULL)
      throw new RuntimeException("Failed to create window")
    glfwMakeContextCurrent(window)
    glfwSwapInterval(1)
    glfwShowWindow(window)
    loop(window)
  }
}

