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
import java.nio.FloatBuffer

class Shader {
  var vertex_shader:String = 
  """
  #version 130
  
  in vec2 position;
  in vec2 texcoords;
  
  out vec3 color;
  out vec2 uvCoords;
  
  uniform mat4 model_m = mat4(1.0);
  uniform mat4 projection_m = mat4(1.0);
  
  uniform float texture_num_rows; 
  uniform float textu1;
  uniform float textu2;
        
  void main(){
    gl_Position = projection_m * model_m * vec4(position.x, position.y, -1.0, 1.0);    
    vec2 teste = vec2(textu1,textu2);
    uvCoords = vec2(texcoords.x, texcoords.y); 
    uvCoords = (uvCoords/texture_num_rows) + teste;
    
  }
  """
  var fragment_shader:String = 
  """
  #version 130
  
  in vec3 color;
  in vec2 uvCoords;
  
  out vec4 out_Color;
  
  uniform sampler2D texsampler;
  
  void main(){
    vec4 texturedColor = texture(texsampler, uvCoords);
    if(texturedColor.a == 0)
      discard;
    out_Color = texturedColor;
  }
  """
  
  var programID:Int = _
  var vsID:Int = _
  var fsID:Int = _
  
  var samplerLocation:Int = _
  var projectionLocation:Int = _
  var modelLocation:Int = _
  var textureOffsetLocation1:Int = _
  var textureOffsetLocation2:Int = _
  var textureNumRowsLocation:Int = _
  
  var modelMatrix:Matrix4f = _
  var projMatrix:Matrix4f = _
  
  def loadShader(vs:String, fs:String)
  {
    
    vsID = glCreateShader(GL_VERTEX_SHADER)
    fsID = glCreateShader(GL_FRAGMENT_SHADER)
    
    glShaderSource(vsID, vs)
    glShaderSource(fsID, fs)
    
    glCompileShader(vsID)
    if(glGetShaderi(vsID, GL_COMPILE_STATUS) == GL_FALSE)
    {
      println("Erro na compilacao do vertex shader" + glGetShaderInfoLog(vsID, 500))
    }
    
    glCompileShader(fsID)
    if(glGetShaderi(fsID, GL_COMPILE_STATUS) == GL_FALSE)
    {
      println("Erro na compilacao do vertex shader" + glGetShaderInfoLog(fsID, 500))
    }
    
    programID = glCreateProgram()
    glAttachShader(programID, vsID)
    glAttachShader(programID, fsID)
    
    glBindAttribLocation(programID, 0, "position")
    glBindAttribLocation(programID, 1, "texcoords")
    
    glLinkProgram(programID)
    glValidateProgram(programID)
    
    initMatrices(800.0f/600.0f)
  }
  
  def initMatrices(aspectRatio:Float)
  {
    projMatrix = Matrix4f.orthographic(-aspectRatio, aspectRatio, -1.0f, 1.0f, 0.1f, 100.0f)
    modelMatrix = new Matrix4f()
  }
  
  def update(q:Quad)
  { 
    glUniform1i(samplerLocation, 0)
    glUniformMatrix4fv(modelLocation, false, modelMatrix.getBuffer())
    glUniformMatrix4fv(projectionLocation, false, projMatrix.getBuffer())
    glUniform1f(textureOffsetLocation1, q.texture1);
    glUniform1f(textureOffsetLocation2, q.texture2);
    glUniform1f(textureNumRowsLocation, q.numRows);
  }
  
  def getUniformLocations()
  {
    samplerLocation = glGetUniformLocation(programID, "texsampler")
    projectionLocation = glGetUniformLocation(programID, "projection_m")
    modelLocation = glGetUniformLocation(programID, "model_m")
    
    textureOffsetLocation1 = glGetUniformLocation(programID, "textu1");
    textureOffsetLocation2 = glGetUniformLocation(programID, "textu2"); 
    textureNumRowsLocation = glGetUniformLocation(programID, "texture_num_rows");

  }
  
  def useShader()
  {
    glUseProgram(programID)
  }
  
  def stopShader()
  {
    glUseProgram(0)
  }
}