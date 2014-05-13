package lostvaults.server
import akka.actor.{ Actor, ActorRef }
import akka.util.ByteString
import akka.io.{ Tcp }
import lostvaults.Parser
import scala.util.Random
import scala.collection.mutable.Queue
import scala.concurrent.duration._
sealed trait PlayerAction
case object PAttack extends PlayerAction
case object PDrinkPotion extends PlayerAction
case object PDecide extends PlayerAction

class Player extends Actor {
  val random = new Random
  import Tcp._
  import context.{ system, become, unbecome, dispatcher }
  var connection = self
  val PMap = Main.PMap.get
  var name = ""
  var dungeon = self
  var hp = 10
  var defense = 0
  var attack = 1
  var food = 0
  var speed = 3 //3 + random.nextInt(3)
  var knownRooms: List[Tuple2[Int, Int]] = List()
  val helpList: List[String] = List("General: \n", "Say \n", "Whisper \n", "LogOut \n\n", "Combat help: \n", "Attack [PLAYER] \n")
  var state: PlayerAction = PDecide
  var previousState: PlayerAction = PDecide
  var target = ""
  var battle: Option[ActorRef] = None
  var msgQueue: Queue[String] = Queue()
  var waitForAck: Boolean = false
  case object Ack extends Event
  case object SendNext

  def pushToNetwork(msg: String) {
    if (waitForAck) {
      msgQueue.enqueue(msg)
    } else {
      waitForAck = true
      connection ! Write(ByteString(msg), Ack)
    }
  }

  def receive = {
    case Received(msg) => {
      connection = sender
      val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
      println("(Player) Received message: " + decodedMsg)
      if (Parser.findWord(decodedMsg, 0) == "LOGIN") {
        name = Parser.findWord(decodedMsg, 1)
        PMap ! PMapIsOnline(name, decodedMsg)
      }
    }
    case PMapIsOnlineResponse(response, purpose) =>
      {
        if (Parser.findWord(purpose, 0) == "LOGIN") {
          if (response) {
            pushToNetwork("LOGINFAIL")
            context stop self
          } else {
            pushToNetwork("LOGINOK")
            /*var i = 0
            for (i <- 0 until 100) {
              pushToNetwork("SYSTEM test #" + (i + 1))
            }*/
            PMap ! PMapAddPlayer(name, self)
            dungeon = Main.City.get
            dungeon ! GameAddPlayer(name)
            become(LoggedIn)
          }
        }
      }

      // Här finns Receive satsen för servern - här tar vi emot alla användar-meddelanden från GUI:t

      def LoggedIn: Receive = {
        case Ack => {
          system.scheduler.scheduleOnce(10.milliseconds, self, SendNext)
        }
        case SendNext => {
          if (msgQueue isEmpty)
            waitForAck = false
          else {
            val msg = msgQueue.dequeue
            connection ! Write(ByteString(msg), Ack)
          }
        }
        case Received(msg) => {
          val decodedMsg = msg.decodeString(java.nio.charset.Charset.defaultCharset().name())
          println("(Player[" + name + "]) Received message: " + decodedMsg)
          val action = Parser.findWord(decodedMsg, 0).toUpperCase
          action match {
            case "SAY" => {
              dungeon ! GameSay(name, Parser.findRest(decodedMsg, 0))
            }
            case "WHISPER" => {
              PMap ! PMapGetPlayer(Parser.findWord(decodedMsg, 1), decodedMsg)
            }
            case "LOGOUT" => {
              pushToNetwork("Bye")
              connection ! Close
            }
            case "HELP" => {
              pushToNetwork(helpList.mkString)
            }
            case "ATTACK" => {
              if (Parser.findWord(decodedMsg, 1).equalsIgnoreCase(name)) {
                pushToNetwork("Don't hit yourself")
              } else {
                dungeon ! GameAttackPlayer(name, Parser.findWord(decodedMsg, 1))
                dungeon ! GameAttackPlayerInCombat(Parser.findWord(decodedMsg, 1))
                target = Parser.findWord(decodedMsg, 1)
                state = PAttack
              }
            }
            case "DRINKPOTION" => {
              if (battle != None) {
                battle.get ! DrinkPotion(name)
                state = PDrinkPotion
              } else {
                self ! GameDrinkPotion
              }
            }
            case "STOP" => {
              state = PDecide
            }
            case _ => {
              pushToNetwork("SYSTEM I have no idea what you're wanting to do.")
            }
          }
        }
        case GameAttackNotInRoom(_name) => {
          pushToNetwork(_name + " is not in room, so you cannot attack her/him")
        }
        case GamePlayerJoinBattle(_battle, enemy) => {
          battle = Some(_battle)
          _battle ! AddPlayer(name, speed, enemy)
        }
        case GameYourTurn => {
          state match {
            case PAttack => {
              if (battle != None) {
                battle.get ! AttackPlayer(name, target, attack)
                previousState = state
              }
            }
            case PDrinkPotion => {
              if (battle != None) {
                battle.get ! DrinkPotion
              }
            }
            case PDecide => {
              pushToNetwork("SYSTEM It's your turn")
              previousState = state
            }
          }
        }
        case GameDrinkPotion => {
          hp = hp + 10
          state = previousState
        }
        case GameDamage(from, strength) => {
          var damage = strength - defense
          if (damage < 0) { damage = 0 }
          hp = hp - damage
          if (hp <= 0) {
            dungeon ! GameNotifyDungeon("Player " + name + " has received " + damage + " damage from " + from + ". " + name + " has died.")
            if (battle != None) {
              battle.get ! RemovePlayer(name)
              dungeon ! GameRemovePlayer(name)
              battle = None
            }
          } else {
            dungeon ! GameNotifyDungeon("Player " + name + " has received " + damage + " damage from " + from + ".")
          }
          if (battle != None) {
            battle.get ! DamageAck
          }
        }
        case GameCombatWin => {
          pushToNetwork("You won that battle")
          battle = None
        }
        case GameMessage(msg) => {
          pushToNetwork(msg)
        }
        case GamePlayerLeft(playerName) => {
          pushToNetwork("DUNGEONLEFT " + playerName)
          if (playerName == target) {
            target = ""
            state = PDecide
          }
        }
        case GamePlayerEnter(name) => {
          pushToNetwork("DUNGEONJOIN " + name)
        }
        case GameSay(name, msg) => {
          pushToNetwork("SAY " + name + " " + msg)
        }
        case GameWhisper(from, to, msg) => {
          pushToNetwork("WHISPER " + from + " " + to + " " + msg)
        }
        case GameMoveToDungeon(dungeon) => {
          this.dungeon = dungeon
        }
        case GameSystem(msg) => {
          pushToNetwork("SYSTEM " + msg)
        }
        case PMapGetPlayerResponse(player, purpose) => {
          val action = Parser.findWord(purpose, 0).toUpperCase
          action match {
            case "WHISPER" => {
              if (!player.isEmpty) {
                val to = Parser.findWord(purpose, 1)
                val msg = Parser.findRest(purpose, 1)
                player.get ! GameWhisper(name, to, msg)
                self ! GameWhisper(name, to, msg)
              } else
                pushToNetwork("SYSTEM No Such Player Online")
            }
          }
        }
        case _: ConnectionClosed => {
          dungeon ! GameRemovePlayer(name)
          PMap ! PMapRemovePlayer(name)
          context stop self
        }
      }
  }
}