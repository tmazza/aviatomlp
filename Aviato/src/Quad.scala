class Quad { 
  var verts:Array[Float] = new Array[Float](6) 
  var inds:Array[Int] = new Array[Int](6) 
  var texCoord:Array[Float] = new Array[Float](4) 
  var vao:Int = _ 
  var vbo:Int = _ 
  var tbo:Int = _ 
  var ebo:Int = _ 
  var position:Vector3f = new Vector3f(0,0,0) 
  var scale:Vector3f = new Vector3f(1,1,1) 
  var tipo:Char = _
  
  var texIndex:Int = _
  var numRows:Float = 10.0f
  var texture1:Float = _
  var texture2:Float = _
 
  
  def calcAtlas(){
     var collumn = (texIndex % numRows).asInstanceOf[Int];
     texture1 = collumn / numRows;
     var row = (texIndex / numRows).asInstanceOf[Int];
     texture2 = row / numRows;     
  }
    
}