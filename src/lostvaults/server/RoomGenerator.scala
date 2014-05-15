package lostvaults.server
import scala.util.Random
import java.util.Calendar

class RoomGenerator {
  val seed = 10 //System.currentTimeMillis
  val Rnd = new Random(seed)
  val Width = 10
  val Height = 10
  var rooms = new Array[Room](Width * Height)
  def coordToIndex(x: Int, y: Int): Int = {
    if (x < 0 || y < 0 || x >= Width || y >= Height)
      0
    else
      x + (y * Width)
  }

  def coordToIndex(c: (Int, Int)): Int = {
    val x = c._1
    val y = c._2
    if (x < 0 || y < 0 || x >= Width || y >= Height)
      0
    else
      x + (y * Width)
  }
  def outOfBounds(c: (Int, Int)): Boolean = {
    (c._1 < 0 || c._1 >= Width || c._2 < 0 || c._2 >= Height)
  }
  def rand(min: Int, max: Int) = {
    (min + (Rnd.nextFloat() * (max - min))).asInstanceOf[Int]
  }
  val MAXDEPTH = 10
  var startRoom: (Int, Int) = (rand(1, Width - 1), rand(1, Height - 1))
  def findEmptyRoom(depth: Int): (Int, Int) = {
    val x = rand(0, Width)
    val y = rand(0, Height)
    (x, y)
  }

  def pickDirection(from: (Int, Int)): (Int, Int) = {
    val dirX = if (from._1 - startRoom._1 < 0) -1 else if (from._1 - startRoom._1 > 0) 1 else 0 // if 1 we are to the right. If -1 we are to the left. If 0 we are in the same column.
    val dirY = if (from._2 - startRoom._2 < 0) -1 else if (from._2 - startRoom._2 > 0) 1 else 0 // if 1 we are below. If -1 we are to the left if 0 we are in the same row.
    var chanceNorth = 0.0
    var chanceEast = 0.0
    var chanceSouth = 0.0
    var chanceWest = 0.0
    var directions = new Array[Int](8)
    if (dirX == -1 && dirY == -1) {
      directions(0) = 0
      directions(1) = 1
      directions(2) = 2
      directions(3) = 2
      directions(4) = 2
      directions(5) = 3
      directions(6) = 3
      directions(7) = 3
    } else if (dirX == 0 && dirY == -1) {
      directions(0) = 0
      directions(1) = 1
      directions(2) = 3
      directions(3) = 2
      directions(4) = 2
      directions(5) = 2
      directions(6) = 2
      directions(7) = 2
    } else if (dirX == 1 && dirY == -1) {
      directions(0) = 0
      directions(1) = 3
      directions(2) = 2
      directions(3) = 2
      directions(4) = 2
      directions(5) = 1
      directions(6) = 1
      directions(7) = 1

    } else if (dirX == -1 && dirY == 0) {
      directions(0) = 0
      directions(1) = 2
      directions(2) = 3
      directions(3) = 1
      directions(4) = 1
      directions(5) = 1
      directions(6) = 1
      directions(7) = 1
    } else if (dirX == 1 && dirY == 0) {
      directions(0) = 0
      directions(1) = 1
      directions(2) = 2
      directions(3) = 3
      directions(4) = 3
      directions(5) = 3
      directions(6) = 3
      directions(7) = 3
    } else if (dirX == -1 && dirY == 1) {
      directions(0) = 2
      directions(1) = 3
      directions(2) = 0
      directions(3) = 0
      directions(4) = 0
      directions(5) = 1
      directions(6) = 1
      directions(7) = 1
    } else if (dirX == 0 && dirY == 1) {
      directions(0) = 1
      directions(1) = 2
      directions(2) = 3
      directions(3) = 0
      directions(4) = 0
      directions(5) = 0
      directions(6) = 0
      directions(7) = 0
    } else {
      directions(0) = 2
      directions(1) = 1
      directions(2) = 0
      directions(3) = 0
      directions(4) = 0
      directions(5) = 3
      directions(6) = 3
      directions(7) = 3
    }
    val dir = rand(0, 7)
    directions(dir) match {
      case 0 => (from._1, from._2 - 1)
      case 1 => (from._1 + 1, from._2)
      case 2 => (from._1, from._2 + 1)
      case _ => (from._1 - 1, from._2)
    }
  }

  def dirBetween(from: (Int, Int), to: (Int, Int)): Int = {
    val fx = from._1
    val fy = from._2
    val tx = to._1
    val ty = to._2
    if (fx == tx && fy > ty)
      0
    else if (fx > tx && fy == ty)
      3
    else if (fx == tx && fy < ty)
      2
    else if (fx < tx && fy == ty)
      1
    else // Should never happen!
      -1
  }
  def dirOpposite(from: Int) = {
    from match {
      case 0 => 2
      case 1 => 3
      case 2 => 0
      case 3 => 1
    }
  }
  def generateDungeon(): Array[Room] = {
    var created: List[(Int, Int)] = List()
    // Create a new array of Width * Height rooms
    var x = 0
    var y = 0
    for (x <- 0 until Width) {
      for (y <- 0 until Height) {
        rooms(coordToIndex(x, y)) = new Room
      }
    }
    rooms.foreach(c => { c.created = false; c.connected = false })
    rooms(coordToIndex(startRoom)).created = true
    rooms(coordToIndex(startRoom)).connected = true
    rooms(coordToIndex(startRoom)).startRoom = true
    var roomsCreated = 0
    var findRoomTries = 0
    do {
      findRoomTries = 0
      created = List()
      var nextCoord = (0, 0)
      do {
        nextCoord = findEmptyRoom(0)
        findRoomTries += 1
      } while (rooms(coordToIndex(nextCoord)).created == true && findRoomTries < 40)
      if (findRoomTries >= 40) {
        roomsCreated = 40
      } else {
        var next = rooms(coordToIndex(nextCoord))
        created = nextCoord :: created
        next.created = true
        var tested = Array[Boolean](false, false, false, false)
        var dir = (-1, -1)
        var success = false
        var tests = 0
        do {
          dir = pickDirection(nextCoord)
          if (rooms(coordToIndex(dir)).created || outOfBounds(dir)) {
            if (!outOfBounds(dir) && rooms(coordToIndex(dir)).connected) {
              created = dir::created
              success = true
            } else {
              tested(dirBetween(nextCoord, dir)) = true
              tests += 1
              if ((tested(0) && tested(1) && tested(2) && tested(3)) || tests >= 8) {
                tests = 0
                created = created.tail
                if (created isEmpty) {
                  success = true
                } else {
                  val r = rooms(coordToIndex(nextCoord))
                  r.created = false
                  nextCoord = created.head
                }
              }
            }
          } else {
            rooms(coordToIndex(dir)).created = true
            created = dir::created
            nextCoord = dir
          }
        } while (!success)
        created.foreach(c => { rooms(coordToIndex(c)).created = true; rooms(coordToIndex(c)).connected = true })
        var head = (0, 0)
        var lastCoord = (-1, -1)
        while (!(created isEmpty)) {
          head = created.head
          if (lastCoord != (-1, -1)) {
            val r1 = rooms(coordToIndex(head))
            val r2 = rooms(coordToIndex(lastCoord))
            val d = dirBetween(head, lastCoord)
            r1.exits(d) = true
            r2.exits(dirOpposite(d)) = true
          }
          lastCoord = head
          created = created.tail
        }
        roomsCreated += created.size + 1
      }
    } while (roomsCreated < 40)
    // Finally return the generated array of rooms.
    rooms
  }
}