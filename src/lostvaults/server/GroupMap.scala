package lostvaults.server
import akka.actor.{ Actor, ActorRef, Props }
import scala.collection.mutable.HashMap
/**
 * GMapMsg serves as the base trait for all GroupMap related messages.
 */
sealed trait GMapMsg

/**
 * GMapJoin is a request to add one player to the group of another. If the player we wish to join is not currently in a group,
 * a new group is created for both players.
 * @param joinee The name of the player who wishes to join a new group.
 * @param group The name of the player whose group the joinee wishes to join.
 */
case class GMapJoin(joinee: String, group: String) extends GMapMsg

/**
 * GMapLeave is a request to remove a player from his current group. Afterwards, the player who wishes to leave is no longer part of
 * any group.
 * @param name The name of the player who wishes to leave their current group.
 */
case class GMapLeave(name: String) extends GMapMsg

/**
 * GMapSendGameMessage is a request to send a game message to the group of a named player. If the player is not in a group, the message is sent
 * only to the player whose name is provided.
 * @param name The name of the player whose group should receive a message.
 * @param msg The Game Message to be relayed to the group.
 */
case class GMapSendGameMessage(name: String, msg: GameMsg) extends GMapMsg

/**
 * GMapGetPlayerList is a request to retrieve a list of players in a given player's group. If the player is not in a group, a list containing
 * only the requested player's name is returned. This request returns a GMapGetPlayerListResposne message to the sender.
 * @param name The name of the player whose group's player list is to be returned.
 */
case class GMapGetPlayerList(name: String) extends GMapMsg

/**
 * GMapGetPlayerListResponse is a response to a GetPlayerList request.
 * @param list A list of string containing the player names in the requested group.
 */
case class GMapGetPlayerListResponse(list: List[String]) extends GMapMsg

/**
 * GMapGetPlayerCount is a request to retreive the number of players currently in a group. The message GMapGetPlayerCountResponse is returned to sender
 * @param name The name of a player whose group player count we wish to find out.
 */
case class GMapGetPlayerCount(name: String) extends GMapMsg

/**
 * GMapGetPlayerCountResponse is a resonse to a GetPlayerCount request.
 * @param count The number of players in the requested group.
 */
case class GMapGetPlayerCountResponse(count: Int) extends GMapMsg

class GroupMap extends Actor {
  val PMap = Main.PMap.get
  var groupMap: HashMap[String, PlayerGroup] = HashMap()
  def _FindName(name: String): Option[PlayerGroup] = {
    val e = groupMap.find(c => c._1 == name)
    if (e isEmpty)
      None
    else
      Some(e.get._2)
  }
  def receive() = {
    case GMapJoin(joinee, group) => {
      val groupOp = _FindName(group)
      if (!groupOp.isEmpty) {
        groupMap += Tuple2(joinee, groupOp.get)
      } else {
        var join = new PlayerGroup
        join.addPlayer(joinee)
        join.addPlayer(group)
        groupMap += Tuple2(joinee, join)
        groupMap += Tuple2(group, join)
      }
    }
    case GMapLeave(name) => {
      val groupOp = _FindName(name)
      if(!groupOp.isEmpty) {
        groupOp.get.removePlayer(name)
        groupMap -= name
      }
    }
    case GMapSendGameMessage(name, msg) => {
    	val groupOp = _FindName(name)
    	if(!groupOp.isEmpty) {
    	  groupOp.get.groupSendMessage(msg)
    	}
    	else {
    	  PMap ! PMapSendGameMessage(name, msg) 
    	}
    }
    case GMapGetPlayerList(name) => {
    	val groupOp = _FindName(name)
    	if(!groupOp.isEmpty) {
    	  sender ! GMapGetPlayerListResponse(groupOp.get.listPlayers)
    	} else {
    	  sender ! GMapGetPlayerListResponse(List(name))
    	}
    }
    case GMapGetPlayerCount(name) => {
      val groupOp = _FindName(name)
      if(!groupOp.isEmpty) {
        sender ! GMapGetPlayerCountResponse(groupOp.get.playerCount)
      }
      	sender ! GMapGetPlayerCountResponse(1)
    }
  }
}