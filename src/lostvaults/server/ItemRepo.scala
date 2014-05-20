package lostvaults.server
import scala.util.Random
import scala.collection.mutable.Queue
import scala.concurrent.duration._
import scala.slick.jdbc.{ GetResult, StaticQuery => Q }
import scala.slick.jdbc.meta.MTable
import scala.slick.jdbc.JdbcBackend
import scala.slick.driver.SQLiteDriver.simple._
import Q.interpolation

object ItemRepo {
  import JdbcBackend.Database
  case class ItemData(id: Int, name: String, attack: Int, defense: Int, speed: Int, rating: Int, price: Int, itemType: String)
  implicit val getItemDataResult = GetResult(r => ItemData(r.nextInt, r.nextString, r.nextInt, r.nextInt, r.nextInt, r.nextInt, r.nextInt, r.nextString))

  var itemArray: Array[ItemData] = Array()
  def populateArray() {
    Database.forURL("jdbc:sqlite:lostvaults.db", driver = "org.sqlite.JDBC") withSession {
      implicit session =>
        Q.queryNA("SELECT * FROM Items") foreach (c => itemArray = itemArray :+ c)
    }
  }
  def getAllByType(Type: String): Array[ItemData] = {
    var ret = Array[ItemData]()
    itemArray foreach (item => if (item.itemType.compareToIgnoreCase(Type) == 0) ret = ret :+ item)
    ret
  }
  def getAllByRating(Rating: Int): Array[ItemData] = {
    var ret = Array[ItemData]()
    itemArray foreach (item => if (item.rating <= Rating) ret = ret :+ item)
    ret
  }
  def getById(ID: Int): Item = {
    val itemOp = itemArray.find(item => item.id == ID)
    if (itemOp == None)
      new Item(-4, "Invalid Item", 0, 0, 0, 0, "Invalid")
    else {
      val item = itemOp.get
      val ret: Item = new Item(item.id, item.name, item.attack, item.defense, item.speed, item.price, item.itemType)
      ret
    }
  }
  def getItem(Index: Int): Item = {
    var item = itemArray(0)
    if (Index < 0 || Index >= itemArray.length)
      new Item(-4, "Invalid Item", 0, 0, 0, 0, "Invalid")
    item = itemArray(Index)
   val ret: Item = new Item(item.id, item.name, item.attack, item.defense, item.speed, item.price, item.itemType)
    ret
  }
}