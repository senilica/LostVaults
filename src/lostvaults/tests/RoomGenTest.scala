package lostvaults.tests
import akka.actor.{ Actor, ActorRef, Props, ActorSystem }
import lostvaults.server.{ Room, RoomGenerator }
import org.scalatest._
class RoomGenTest extends FunSuite {

  test("Testing room generation.") {
   /* val gen = new RoomGenerator
    var system = new ActorSystem
    val rooms = gen.generateDungeon(system)
    var y = 0
    for (y <- 0 until gen.Height) {
      for (x <- 0 until gen.Width) {
        print(rooms(gen.coordToIndex(x, y)))
      }
      print("|\n")
    }
  }
  test("Testing direction function.") {
    val gen = new RoomGenerator
    assertResult(2) {
      gen.dirOpposite(0)
    }
    assertResult(3) {
      gen.dirOpposite(1)
    }
    assertResult(0) {
      gen.dirOpposite(2)
    }
    assertResult(1) {
      gen.dirOpposite(3)
    }
    assertResult(0) {
      gen.dirBetween((1, 1), (1, 0))
    }
    assertResult(1) {
      gen.dirBetween((1, 1), (2, 1))
    }
    assertResult(2) {
      gen.dirBetween((1, 1), (1, 2))
    }
    assertResult(3) {
      gen.dirBetween((1, 1), (0, 1))
    }
    * 
    */
  }
}