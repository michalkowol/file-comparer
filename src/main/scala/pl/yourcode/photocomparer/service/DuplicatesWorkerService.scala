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
  case class Pending(progressPercent: Int, status: String = "In progress.") extends Status
  case class Completed(duplicates: Seq[Seq[File]]) extends Status
  case class Error(ex: Throwable) extends Status
  case object GetAllJobsUuids
  case class UpdateJob(uuid: String, status: Status)
  case class Jobs(uuids: Set[String])
  def props(fileDeduplicatorService: FileDeduplicatorService): Props = Props(new DuplicatesWorker(fileDeduplicatorService))
}

trait ProgressListener {
  def update(progressPercent: Int): Unit
}

object NoOpProgressListener extends ProgressListener {
  def update(progressPercent: Int): Unit = {
    // do nothing...
  }
}

class ActorProgressListener(uuid: String, duplicatesWorker: ActorRef) extends ProgressListener {
  override def update(progressPercent: Int): Unit = {
    duplicatesWorker ! DuplicatesWorker.UpdateJob(uuid, DuplicatesWorker.Pending(progressPercent, status = s"In progress: $progressPercent%."))
  }
}

class DuplicatesWorker(fileDeduplicatorService: FileDeduplicatorService) extends ServiceActor {

  import DuplicatesWorker._
  import context.dispatcher

  private var jobs = Map.empty[String, Status]

  override def receive: Receive = {
    case FindDuplicates(uuid, directory) =>
      val progressListener = new ActorProgressListener(uuid, self)
      val duplicates = fileDeduplicatorService.findDuplicates(directory, progressListener)
      duplicates.onComplete {
        case scala.util.Success(duplicates) => self ! UpdateJob(uuid, Completed(duplicates))
        case scala.util.Failure(ex) => self ! UpdateJob(uuid, Error(ex))
      }
      jobs = jobs.updated(uuid, Pending(0))
    case JobStatus(uuid) =>
      jobs.get(uuid) match {
        case Some(Error(ex)) => sender() ! Failure(ex)
        case Some(status) => sender() ! status
        case None => sender() ! Failure(new NoSuchJobException(s"No such job $uuid."))
      }
    case UpdateJob(uuid, status) =>
      jobs = jobs.updated(uuid, status)
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
