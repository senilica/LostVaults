package lostvaults.client
/**
 * TCPClient.scala
 * @Author Felix Färsjö, Jimmy Holm, Fredrik Larsson, Anna Nilsson, Philip Åkerfeldt
 * @Version 1.0
 */
import akka.io.{ IO, Tcp }
import akka.actor.{ Actor, ActorRef, Props }
import java.net.InetSocketAddress
import scala.concurrent.duration._
import lostvaults.Parser
import akka.util.ByteString

sealed trait MyMsg
case class Print(msg: String) extends MyMsg
case object ConnClosed extends MyMsg
case object ShutDown extends MyMsg
case class ConnectTo(ip: InetSocketAddress) extends MyMsg
case object Ok extends MyMsg
object TCPClient {
  def props(listener: ActorRef): Props = Props(new TCPClient(listener))
}

class TCPClient(listener: ActorRef) extends Actor {
  import Tcp._
  import context.{ system, become }
  val manager = IO(Tcp)
  var connection: Option[ActorRef] = None

  def receive = {
    case ConnectTo(ipAddress) => {
      println("Connecting to " + ipAddress)
      manager ! Connect(ipAddress)
      become({
        case CommandFailed(_: Connect) => {
          listener ! "Connect failed"
          context stop self
        }
        case c @ Connected(remote, local) => {
          listener ! "Connected"
          connection = Some(sender)
          sender ! Register(self)
        }
        case Received(c) => {
          connection.get ! Write(ByteString.apply("ACK", java.nio.charset.StandardCharsets.UTF_8.name()))
          val msg = c.decodeString(java.nio.charset.StandardCharsets.UTF_8.name())
          println("Received message from server: " + msg)
          listener ! msg
        }
        case msg: String => {
          println("Sending: " + msg)
          connection.get ! Write(ByteString.apply(msg, java.nio.charset.StandardCharsets.UTF_8.name()))
        }

        case x: ConnectionClosed => {
          println("Connection closed - shutting down.")
          listener ! x.getErrorCause
          self ! ShutDown
        }
        case _ =>
          println("other")
      })
    }
    case _ => {
      // Do nothing.
    }
  }
}
