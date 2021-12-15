package part2
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import part2.PingPong.End
object PingPong extends App {
  val actorSystem=ActorSystem("PingPong")
  val ping=actorSystem.actorOf(Props[PingActor],"ping")
  case class Message(message:String)
  case class End(receivedPings:Int)
  class PingActor extends Actor with ActorLogging {
    var sum=0
    def increment{sum+=1}
    val pong = context.actorOf(Props[PongActor], "pong")
    override def receive: Receive = {
      case message:String=> increment                                   // On each pong increment sum
      case Message=> for{
        _ <- 1 to 10000                                                // sending 10000 ping message to pong
      }yield pong ! "ping"
      case End(s)=>println(s"sum:$s")                                 // on each End print sum
      sender ! End(sum)
    }
  }
  class PongActor extends Actor with ActorLogging {
    var sum=0
    def doWork(): Int = {
      Thread sleep 1000
      1
    }
    def increment{sum+=1}
    override def receive: Receive = {
      case End(sum)=>println(s"counter:$sum")                    // on each end print counter variable(9999)
      case message:String=>increment                             // on each pong increment sum variable
      if (sum<10000) ping !"pong"                                // Send pong to ping actor if sum <10000
      else if(sum==10000) sender ! End(sum)
    }
  }
  ping ! Message
}
