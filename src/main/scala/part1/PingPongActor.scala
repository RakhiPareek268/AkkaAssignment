package part1
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import java.time.Clock.system
import scala.sys.props
object PingPongActor extends App {
  val actorSystem=ActorSystem("PingPong")                       //creating actor system
  val ping=actorSystem.actorOf(Props[PingActor],"ping")  // creating instance of ping actor
  case class Message(message:String)
  class PingActor extends Actor with ActorLogging {
    val pong = context.actorOf(Props[PongActor], "pong") // create pong as a child of ping actor
    override def receive: Receive = {
      case message:String=>log.info{message}                   // using log.info we are printing the message send by pong actor as acknowledgement
      case Message=> pong ! "ping"
    }
  }
  class PongActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message:String=>log.info{message}
        ping !"pong"                                          // sending acknowledgment to ping actor as received message
    }
  }
  ping ! Message
}
