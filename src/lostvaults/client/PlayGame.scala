package lostvaults.client
/**
 * PlayGame.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
import akka.actor.{ Actor, ActorRef, Props }
import lostvaults.Parser
import java.net.InetSocketAddress
import java.security.MessageDigest

/**
 * The object playGame is responsible for creating an instance of playGame in a new actor.
 *
 */
object playGame {

  /**
   * Creates a new actor for the object playGame
   *
   */
  def props = Props(new playGame)
}

/**
 * The playGame class is responsible for setting up the game and the references to the current game.
 *
 */
class playGame extends Actor {
  val TCPActorRef = context.actorOf(TCPClient.props(self))
  val game = playGameCommunication
  game.setGame(this)

  /**
   * This method sends a message to an ActorRef in the TCP Client.
   * @param msg The message that will be sent.
   */
  def sendMessage(msg: String) {
    val action = Parser.findWord(msg, 0)
    val sendMsg = action.toUpperCase + " " + Parser.findRest(msg, 0)
    TCPActorRef ! sendMsg
  }

  /**
   * This method will print the IP given and it will also send a a connection request with the given IP to the TCP Clients ActorRef.
   * @param ip The IP that will be sent.
   */
  def sendIP(ip: String) {
    TCPActorRef ! ConnectTo(new InetSocketAddress(ip, 51234))
  }

  /**
   * This is where messages from the TCP Client are received.
   *
   * In this method we receive different messages from the TCP Client which all have a specific action connected to them.
   * All of the actions earlier mentioned will have some specific effect on the current game.
   */
  def receive = {
    case "Connect failed" => {
      game.updateDynamicInfo("Connect failed")
      context stop self
    }
    case "Connected" =>
      game.updateDynamicInfo("Connected")
      val name = game.getName()
      var pass = game.getPass()
      val md = MessageDigest.getInstance("SHA-256")
      md.update(pass.getBytes(java.nio.charset.StandardCharsets.UTF_8))
      //java.nio.charset.Charset
      val passBytes = md.digest()
      pass = ""
      /*for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }*/
      var hex = ""
      for (i <- 0 until passBytes.length) {
        hex = Integer.toHexString(0xff & passBytes(i))
        pass += hex
      }
      TCPActorRef ! "LOGIN " + name + " " + pass;
    case c: String => {
      println(c)
      val firstWord = Parser.findWord(c, 0)
      firstWord match {
        case "HEALTHSTATS" =>
          game.setHealthStats(Parser.findRest(c, 0))
        case "COMBATSTATS" =>
          game.setCombatStats(Parser.findRest(c, 0))
        case "DUNGEONLIST" =>
          game.setDungeonPlayers(Parser.findRest(c, 0))
        case "DUNGEONJOIN" =>
          game.addDungeonPlayer(Parser.findRest(c, 0))
        case "DUNGEONLEFT" =>
          game.removeDungeonPlayer(Parser.findRest(c, 0))
        case "ROOMLIST" =>
          game.setRoomPlayers(Parser.findRest(c, 0))
        case "ROOMJOIN" =>
          game.addRoomPlayer(Parser.findRest(c, 0))
        case "ROOMLEFT" =>
          game.removeRoomPlayer(Parser.findRest(c, 0))
        case "NPCLIST" =>
          game.setNPCs(Parser.findRest(c, 0))
        case "NPCJOIN" =>
          game.addNPC(Parser.findRest(c, 0))
        case "NPCLEFT" =>
          game.removeNPC(Parser.findRest(c, 0))
        case "ITEMLIST" =>
          game.setItems(Parser.findRest(c, 0))
        case "ITEMJOIN" =>
          game.addItem(Parser.findRest(c, 0))
        case "ITEMLEFT" => {
          println("_:_:_:__:__:_:_::")
          println(Parser.findRest(c, 0))
          println("_:_:_:_:_:_::_:_:::_:")
          game.removeItem(Parser.findRest(c, 0))
        }
        case "ROOMEXITS" =>
          game.setExits(Parser.findRest(c, 0))
        case "LOGINOK" =>
          game.updateDynamicInfo("You are logged in")
        case "LOGINFAIL" =>
          game.updateDynamicInfo("I'm sorry, but you cannot use that username")
        case "SAY" => {
          if (game.getName.equals(Parser.findWord(c, 1)))
            game.updateDynamicInfo("You say: " + Parser.findRest(c, 1))
          else
            game.updateDynamicInfo(Parser.findWord(c, 1) + " says: " + Parser.findRest(c, 1))
        }
        case "BYE" =>
          game.updateDynamicInfo("Bye bye, have a good day")
        case "WHISPER" =>
          game.updateDynamicInfo(Parser.findWord(c, 1) + " whispers to " + Parser.findWord(c, 2) + ": " + Parser.findRest(c, 2))
        case "SYSTEM" =>
          game.updateDynamicInfo(Parser.findRest(c, 0))
        case "GUICITY" => 
          game.setLabelCity
        case "GUIDUNGEON" => 
          game.setLabelDungeon
        case _ =>
          game.updateDynamicInfo(c)
      }
    }
    case _ =>
      println("A misstake has occurred")
  }
}
