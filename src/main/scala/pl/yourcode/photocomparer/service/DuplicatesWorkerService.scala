package pl.yourcode.photocomparer.service

import java.io.File

import akka.actor.Status.Failure
import akka.actor.{ActorRef, Props}
import akka.pattern._
import com.paypal.cascade.akka.actor.ServiceActor
import pl.yourcode.photocomparer.exceptions.NoSuchJobException
import pl.yourcode.photocomparer.service.DuplicatesWorker.FindDuplicates

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object DuplicatesWorker {
  case class FindDuplicates(uuid: String, directory: String)
  case class JobStatus(uuid: String)
  sealed trait Status
  case class Pending(uuid: String, status: String = "In progress.") extends Status
  case class Completed(duplicates: Seq[Seq[File]]) extends Status
  case object GetAllJobsUuids
  case class Jobs(uuids: Set[String])
  def props(fileDeduplicatorService: FileDeduplicatorService): Props = Props(new DuplicatesWorker(fileDeduplicatorService))
}

class DuplicatesWorker(fileDeduplicatorService: FileDeduplicatorService) extends ServiceActor {

  import DuplicatesWorker._
  import context.dispatcher

  private var jobs = Map.empty[String, Future[Seq[Seq[File]]]]

  override def receive: Receive = {
    case FindDuplicates(uuid, directory) =>
      val duplicates = fileDeduplicatorService.findDuplicates(directory)
      jobs = jobs.updated(uuid, duplicates)
    case JobStatus(uuid) =>
      jobs.get(uuid) match {
        case Some(duplicates) if duplicates.isCompleted => duplicates.map(Completed) pipeTo sender()
        case Some(duplicates) => sender() ! Pending(uuid)
        case None => sender() ! Failure(new NoSuchJobException(s"No such job $uuid."))
      }
    case GetAllJobsUuids =>
      sender() ! Jobs(jobs.keySet)
  }
}

class DuplicatesWorkerService(duplicatesWorker: ActorRef)(implicit ec: ExecutionContext) {

  def fireJob(uuid: String, directory: String): Unit = {
    duplicatesWorker ! FindDuplicates(uuid, directory)
  }

  def checkJobStatus(uuid: String): Future[DuplicatesWorker.Status] = {
    duplicatesWorker.ask(DuplicatesWorker.JobStatus(uuid))(1.seconds).mapTo[DuplicatesWorker.Status]
  }

  def getAllJobsUuids: Future[Set[String]] = {
    duplicatesWorker.ask(DuplicatesWorker.GetAllJobsUuids)(1.seconds).mapTo[DuplicatesWorker.Jobs].map(_.uuids)
  }
}
