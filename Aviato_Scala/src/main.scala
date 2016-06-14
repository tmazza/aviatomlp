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
  var quadarr:Array[Quad] = new Array[Quad](10)
  
  def getFloatBuffer(buffer: Array[Float]) : java.nio.FloatBuffer =
  {
    var v:java.nio.FloatBuffer = BufferUtils.createFloatBuffer(buffer.length)
    v.put(buffer)
    v.flip()
    return v
  } 
  
  def initArray(qq:Array[Quad], index:Int)
  {  
    var q:Quad = new Quad()
    q.verts = Array[Float](-1.0f, -1.0f, 0.0f, 1.0f, 1.0f, -1.0f)
    
    q.vao = glGenVertexArrays()
    glBindVertexArray(q.vao)
    
    q.vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, q.vbo)
    glBufferData(GL_ARRAY_BUFFER, getFloatBuffer(q.verts), GL_STATIC_DRAW)
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
    //glBindBuffer(GL_ARRAY_BUFFER, 0)
    
    qq(index) = q
  }
  
  def render()
  {
    glBindVertexArray(quadarr(0).vao)
    glEnableVertexAttribArray(0)
    glDrawArrays(GL_TRIANGLES, 0, 3)
    glDisableVertexAttribArray(0)
    glBindVertexArray(0)
  }
  
  def loop(window: Long){
    GL.createCapabilities()
    initArray(quadarr, 0)
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

