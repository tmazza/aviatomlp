
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
import scala.util.control._
import scala.math._
import scala.util.control.Breaks._
import java.nio.DoubleBuffer
import scala.io.Source._

object Main {
  var quadarr: Array[Quad] = new Array[Quad](193)
  var texture: TextureLocal = new TextureLocal()

  var shader: Shader = new Shader()
  var turno = 1; // Jogador com turno ativo
  var projVoando = 0
  var angC1 = 45.0f; // Angulo canhão player 1
  var angC2 = 45.0f; // Angulo canhão player 2

  var quadProj: Quad = new Quad();
  var quadPly1: Quad = new Quad();
  var quadPly2: Quad = new Quad();

  var xiProj = 0.0f; // x inicial do projetil
  var yiProj = 0.0f;
  var velIniP1 = 2.0f;
  var velIniP2 = 2.0f;
  var trajeto = 0.0f;

  // Tipos de blocos
  val TipoProj = '0';
  val TipoPlay1 = '1';
  val TipoPlay2 = '2';
  val TipoSoloMeio = 'G';
  val TipoSoloEsq = 'F';
  val TipoSoloDir = 'H';
  val TipoSubSolo = 'T';
  val TipoSubSoloDir = 'U';
  val TipoSubSoloEsq = 'X';
  val TipoVazio = 'O';

  var window: Long = _;

  def main(args: Array[String]): Unit = {
    init();
    while (!glfwWindowShouldClose(window)) {
      update()
      render();
    }
  }

  def init() {
    initJanela();
    initShaders();
    initMapa();
  }

  /**
   * Configura e cria janela
   */
  def initJanela() {
    GLFWErrorCallback.createPrint(System.err).set(); // Callback de erro

    if (!glfwInit()) // Iniciar GLFW
      throw new IllegalStateException("Falha ao iniciar GLFW");

    var WIDTH = 800; var HEIGHT = 600;

    window = glfwCreateWindow(WIDTH, HEIGHT, "Aviato", NULL, NULL); // Criação da janela
    if (window == NULL)
      throw new RuntimeException("Falha ao criar janela GLFW");

    var vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor()); // Resolução do monitor
    glfwSetWindowPos( // Janela centralizada
      window,
      (vidmode.width() - WIDTH) / 2,
      (vidmode.height() - HEIGHT) / 2);
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);
    GL.createCapabilities();
  }

  /**
   * Inicializa shader
   */
  def initShaders() {
    shader.loadShader(shader.vertex_shader, shader.fragment_shader)
    shader.getUniformLocations()
  }

  /**
   * Aplica uma função em x e y para encontrar as coordenadas do novo quadrado
   */
  def calcCoordBloco(c:Char,x:Int,y:Int,numBloco:Int,posx:(Int) => Float,posy:(Int) => Float): Int = {
    if(c == '1') { 
      initEntity(1, posx(x), posy(y) - 0.08f, TipoPlay1)
      quadPly1 = quadarr(1)
    } else if(c == '2') {
      initEntity(2, posx(x), posy(y) - 0.08f, TipoPlay2)
      quadPly2 = quadarr(2)
    } else {
      initEntity(numBloco, posx(x), posy(y), c)
      return numBloco + 1
    }
    return numBloco
  }
  
  /**
   * Carrega e inicializa mapa com janela divida em x por y blocos
   */
  def initMapa() {
    var x, y = 0
    var numBloco = 3;
    val lines = fromFile("map1.txt").getLines
    for (l <- lines) {
      x = 0;
      for (c <- l) {
        numBloco = calcCoordBloco(c,x,y,numBloco,
            (x: Int) => ((2.6f / 23) * x - 1.3f), 
            (y: Int) => (-1.0f / 7.0f) * y );
        x += 1;
      }
      y += 1
    }
    initEntity(0, -0.5f, -0.5f, TipoProj)
    quadProj = quadarr(0)
    hideProjetil()

    glClearColor(0.41f, 0.57f, 0.94f, 0.0f)
    texture.loadTexture("all.png")
  }

  def update() {
    if (projVoando == 1) animaProjetil()
    else controleIteracoes(window)
  }

  /**
   * Renderiza as entidades do vetor quadarr
   */
  def render() {
    shader.useShader()
    val outer = new Breaks;
    outer.breakable {
      quadarr.map { q => renderEntity(q); }
    }
    shader.stopShader()

    drawVetorVelocidade();
    glfwSwapBuffers(window); // swap the color buffers
    glfwPollEvents();
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

  }

  def renderEntity(q: Quad) {
    shader.modelMatrix = Matrix4f
      .translate(q.position.x, q.position.y, 0.0f)
      .multiply(Matrix4f.scale(q.scale.x, q.scale.y, 1.0f))
    shader.update(q)
    glBindVertexArray(q.vao)
    glEnableVertexAttribArray(0)
    glBindBuffer(GL_ARRAY_BUFFER, q.vbo)
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
    glEnableVertexAttribArray(1)
    glBindBuffer(GL_ARRAY_BUFFER, q.tbo)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
    glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0)
    glDisableVertexAttribArray(0)
    glDisableVertexAttribArray(1)
    glBindBuffer(GL_ARRAY_BUFFER, 0)
  }

  /**
   *  Decompõe a velocidade nas componentes x e y de acordo com a velocidade inicial
   *  e o ângulo de lançamnto
   */
  def decompoeVelocidade(vi: Float)(ang: Float): (Float, Float) =
    (vi * cos(toRadians(ang)).asInstanceOf[Float],
     vi * sin(toRadians(ang)).asInstanceOf[Float])
  
  /**
   * Calcula a variação em x. Se Player um está jogando a variação é positivo
   * se o player 2 está jogando a variação é negativa
   */
  def variacaoEmX(turno: Int,a:Float,b:Float): Float = turno match {
    case 1 => a*b
    case 2 => -a*b
  }
     
  /**
   * Atualiza a posição do projétil a cada espaço de tempo t
   */
  def animaProjetil() {
    if (quadProj.position.y > -0.95f && quadProj.position.x > -1.5f && quadProj.position.x < 1.5f) { // Limites da tela
      var t = trajeto;

      var velIni = if (turno == 1) velIniP1 else velIniP2;
      var ang = if (turno == 1) angC1 else angC2;

      val componentesDaVelocidade = decompoeVelocidade(velIni)_
      var (vix, viy) = componentesDaVelocidade(ang);

      quadProj.position.x = xiProj + variacaoEmX(turno,vix,t);
      quadProj.position.y = yiProj + viy * t - (9.8 * (pow(t, 2.0f)) / 2).asInstanceOf[Float];

      var colisao = getColisao(quadarr);
      if (colisao != 'N') {
        if (colisao == '1' || colisao == '2')
          animacaoVitoria();
        else
          finalLancamentoProjetil()
      }
      trajeto += 0.01f;
    } else {
      finalLancamentoProjetil();
    }
  }
  
  /**
   * Nome do jogador com turno ativo
   */
  def nomePlayer(t: Int): String = t match {
    case 1 => "Player 1"
    case 2 => "Player 2"
    case _ => "John Doe"
  }
  
  def animacaoVitoria() {
    println(nomePlayer(turno) + " venceu");
    projVoando = 0;
  }

  def finalLancamentoProjetil() {
    hideProjetil();
    if (turno == 1)
      turno = 2
    else
      turno = 1
  }

  def getColisao(blocos: Array[Quad]): Char = {
    var raio = 0.05f;
    var TipoPlyAtivo = if (turno == 1) '1' else '2';

    // Verifica se ponto (x,y) está dentro do raio do projétil
    val noRaio = (x: Float, y: Float, raio: Float) =>
      (pow(quadProj.position.x - x, 2) + pow(quadProj.position.y - y, 2)) <= pow(raio, 2)

    blocos foreach { q =>
      if (q.tipo != TipoVazio &&
        q.tipo != TipoProj &&
        q.tipo != TipoPlyAtivo &&
        noRaio(q.position.x, q.position.y, raio)) {
        return q.tipo;
      }
    }
    return 'N';
  }

  // Player ou atira ou ajusta o angulo do tiro
  def controleIteracoes(window: Long) {
    var space = glfwGetKey(window, GLFW_KEY_SPACE);
    var up = glfwGetKey(window, GLFW_KEY_UP);
    var down = glfwGetKey(window, GLFW_KEY_DOWN);
    var right = glfwGetKey(window, GLFW_KEY_RIGHT);
    var left = glfwGetKey(window, GLFW_KEY_LEFT);

    if (space == 1) { // Iniciar tiro        
      trajeto = 0.0f;

      if (turno == 1) {
        xiProj = quadPly1.position.x + 0.01f;
        yiProj = quadPly1.position.y + 0.01f;
      } else {
        xiProj = quadPly2.position.x - 0.01f;
        yiProj = quadPly2.position.y - 0.01f;
      }

      quadProj.position.x = xiProj;
      quadProj.position.y = yiProj;

      showProjetil();
    } else if (up == 1 || down == 1) { // Ajustar angulo  
      var valorAjuste = 0.5f;
      var angMax = 89.0f;
      var angMin = 0.0f;

      if (turno == 1) {
        if (up == 1 && angC1 < angMax)
          angC1 += valorAjuste;
        else if (down == 1 && angC1 > angMin)
          angC1 -= valorAjuste;
      } else if (turno == 2) {
        if (up == 1 && angC2 < angMax)
          angC2 += valorAjuste;
        else if (down == 1 && angC2 > angMin)
          angC2 -= valorAjuste;
      }

    } else if (right == 1) {
      if (turno == 1 && velIniP1 < 8.0f)
        velIniP1 += 0.02f;
      else if (turno == 2 && velIniP2 < 8.0f)
        velIniP2 += 0.02f;
    } else if (left == 1) {
      if (turno == 1 && velIniP1 > 0.1f)
        velIniP1 -= 0.02f;
      else if (turno == 2 && velIniP2 > 0.1f)
        velIniP2 -= 0.02f;
    }
  }

  def hideProjetil() {
    projVoando = 0;
    quadProj.scale.x = 0;
    quadProj.scale.y = 0;
  }

  def showProjetil() {
    projVoando = 1;
    quadProj.scale.x = 1;
    quadProj.scale.y = 1;
  }

  def drawVetorVelocidade() {
    glLineWidth(2f);
    glColor3f(1.0f, 0.0f, 0.0f);
    glBegin(GL_LINES);
    var erroEmX = 0.3f;
    glVertex3f(quadPly1.position.x + erroEmX, quadPly1.position.y, 0.0f);
    glVertex3f(quadPly1.position.x + erroEmX + 0.1f * velIniP1 * cos(toRadians(angC1)).asInstanceOf[Float],
      quadPly1.position.y + 0.1f * velIniP1 * sin(toRadians(angC1)).asInstanceOf[Float],
      0.0f);
    glEnd();

    glLineWidth(2f);
    glColor3f(0.0f, 0.0f, 1.0f);
    glBegin(GL_LINES);
    erroEmX = +0.2f;
    glVertex3f(quadPly2.position.x - erroEmX, quadPly2.position.y, 0.0f);
    glVertex3f(quadPly2.position.x - erroEmX - 0.1f * velIniP2 * cos(toRadians(angC2)).asInstanceOf[Float],
      quadPly2.position.y + 0.1f * velIniP2 * sin(toRadians(angC2)).asInstanceOf[Float],
      0.0f);
    glEnd();
  }

  def getFloatBuffer(buffer: Array[Float]): java.nio.FloatBuffer =
    {
      var v: java.nio.FloatBuffer = BufferUtils.createFloatBuffer(buffer.length)
      v.put(buffer)
      v.flip()
      return v
    }

  def getIntBuffer(buffer: Array[Int]): java.nio.IntBuffer =
    {
      var v: java.nio.IntBuffer = BufferUtils.createIntBuffer(buffer.length)
      v.put(buffer)
      v.flip()
      return v
    }

  /**
   * Cria um elemento do jogo.
   * index: posição no vetor onde será armazenado
   * x,y: coordenadas
   * tipo: identificador do tipo de entidade
   */
  def initEntity(index: Int, x: Float, y: Float, tipo: Char) {
    var q: Quad = new Quad()

    var divisor: Float = 30.0f;
    q.verts = Array[Float](-1.0f / divisor, -1.0f / divisor, 1.0f / divisor, -1.0f / divisor, -1.0f / divisor, 1.0f / divisor, 1.0f / divisor, 1.0f / divisor)
    q.texCoord = Array[Float](0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
    q.inds = Array[Int](0, 1, 2, 2, 1, 3)

    q.vao = glGenVertexArrays()
    glBindVertexArray(q.vao)

    q.vbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, q.vbo)
    glBufferData(GL_ARRAY_BUFFER, getFloatBuffer(q.verts), GL_STATIC_DRAW)
    glEnableVertexAttribArray(0)
    glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0)
    glDisableVertexAttribArray(0)

    q.tbo = glGenBuffers()
    glBindBuffer(GL_ARRAY_BUFFER, q.tbo)
    glBufferData(GL_ARRAY_BUFFER, getFloatBuffer(q.texCoord), GL_STATIC_DRAW)
    glEnableVertexAttribArray(1)
    glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)
    glDisableVertexAttribArray(1)

    q.ebo = glGenBuffers()
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, q.ebo)
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, getIntBuffer(q.inds), GL_STATIC_DRAW)

    glBindBuffer(GL_ARRAY_BUFFER, 0)

    q.position.x = x;
    q.position.y = y;
    q.tipo = tipo;

    if (tipo == TipoProj) {
      q.texIndex = 3;
      q.calcAtlas();
    }
    if (tipo == TipoPlay1) {
      q.texIndex = 5;
      q.calcAtlas();
      q.scale.x = 2.5f;
      q.scale.y = 2.5f;
    }
    if (tipo == TipoPlay2) {
      q.texIndex = 4;
      q.calcAtlas();
      q.scale.x = 2.5f;
      q.scale.y = 2.5f;
    }

    var escala = 1.8f;
    var escalay = 0.4f;
    if (tipo == TipoSubSolo) {
      q.texIndex = 21;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }
    if (tipo == TipoSoloMeio) {
      q.texIndex = 11;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }
    if (tipo == TipoSoloEsq) {
      q.texIndex = 10;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }
    if (tipo == TipoSoloDir) {
      q.texIndex = 12;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }
    if (tipo == TipoSubSoloDir) {
      q.texIndex = 22;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }
    if (tipo == TipoSubSoloEsq) {
      q.texIndex = 20;
      q.calcAtlas();
      q.scale.x = escala;
      q.scale.y = escala + escalay;
    }

    quadarr(index) = q
  }

}