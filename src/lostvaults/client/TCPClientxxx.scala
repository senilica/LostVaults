package lostvaults.client
import akka.io.{ IO, Tcp }
import akka.actor.{ Actor, ActorRef, Props }
import java.net.InetSocketAddress
import scala.concurrent.duration._
import lostvaults.Parser
sealed trait MyMsg
case class Print(msg: String) extends MyMsg
case object ConnClosed extends MyMsg
case object ShutDown extends MyMsg
case object Ok extends MyMsg

object TCPClientxxx {
  def props(listener: ActorRef): Props = Props(new TCPClientxxx(listener))
}

class TCPClientxxx(listener: ActorRef) extends Actor {
  import Tcp._
  import context.system
  val manager = IO(Tcp)
  var connection: Option[ActorRef] = None
  
  
  override def preStart() = {
    //manager ! Connect(new InetSocketAddress("localhost", 51234))
    // Ändra localhost i slutversionen till IP'n för Servern.
  }

  def receive = {

    case CommandFailed(_: Connect) =>
       listener ! "connect failed"
      context stop self

    case c @ Connected(remote, local) =>
      listener ! "Connected"
      connection = Some(sender)
      sender ! Register(self)
      context become {
        case d: String =>
          val firstWord = Parser.findWord(d, 0)
          firstWord match {

            case "LoginOK" =>
            listener ! "LoginOK"

            case "LoginFail" =>
            // Do Something

            case "Say" =>
            // Do Something

            case "Bye" =>
            // Do Something

            case "Whisper" =>
            // Do Something

            case "System" =>
            // Do Something

          }

      }
    case _ =>
      println("other")
  }
}