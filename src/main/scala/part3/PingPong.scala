package part3
import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try
object PingPong extends App {
  val actorSystem=ActorSystem("PingPong")
  val ping=actorSystem.actorOf(Props[PingActor],"ping")
  case class Message(message:String)
  case class End(receivedPings:Int)
  case class GetPongSum(pongsum:Option[Int])
  case class ThrowException()
  case object PongMessage
  case object PingMessage
  class PingActor extends Actor with ActorLogging {
    var sum=0
    def increment{sum+=1}
    val pong = context.actorOf(Props[PongActor], "pong")
    override def receive: Receive = {
      case PongMessage => increment; //sender ! PingMessage
      case Message => for {
        _ <- 1 to 10000
      } yield pong ! PingMessage
      case End(s) => println(s"sum:$s")
       pong ! GetPongSum(None)
      case GetPongSum(s)=>println(s)
      sender ! ThrowException()
      sender ! GetPongSum(None)
      sender ! GetPongSum(Some(sum))
    }
  }
  class PongActor extends Actor with ActorLogging {
    var sum=0
    def doWork(): Int = {
      1
    }
    override def receive: Receive = {
      case End(sum)=>println(s"counter:$sum")
      case PingMessage=>
        val newSum=Future{
        sum+=doWork()
      }
        Await.result(newSum,Duration.Inf)
        if (sum<10000) ping ! PongMessage
        else if(sum==10000) {
          sender ! End(sum)
          sender ! GetPongSum(Some(sum))
        }
      case GetPongSum(n)=>println(n)
      case ThrowException() =>println(Try {throw new Exception()})
    }
  }
  ping ! Message
}
