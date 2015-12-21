package pl.yourcode.photocomparer

import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import akka.io.IO
import pl.yourcode.photocomparer.model._
import pl.yourcode.photocomparer.service._
import pl.yourcode.photocomparer.web.{CORSDirective, ExceptionsHandler}
import spray.can.Http
import spray.routing.{HttpServiceActor, HttpServiceBase, Route}

import scala.concurrent.ExecutionContext

object Boot {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("api-system")
    val api = system.actorOf(Props[Api])
    val bindListener = system.actorOf(Props[BindListener])
    val bind = Http.Bind(api, "0.0.0.0", port = 8080)
    IO(Http)(system).tell(bind, bindListener)
  }
}

class BindListener extends Actor with Logging {
  override def receive: Receive = {
    case bound: Http.Bound =>
      log.info("Bound to {}", bound.localAddress)
      context.stop(self)
    case error =>
      log.error(s"$error")
      context.system.terminate()
  }
}

class Api extends HttpServiceActor with CORSDirective with ExceptionsHandler {

  private implicit val executionContext = actorRefFactory.dispatcher

  private val uuidGenerator = UuidGenerator
  private val fileDeduplicatorService = new FileDeduplicatorService
  private val duplicatesWorker = actorRefFactory.actorOf(DuplicatesWorker.props(fileDeduplicatorService))
  private val duplicatesWorkerService = new DuplicatesWorkerService(duplicatesWorker)
  private val duplicatesApi = new DuplicatesApi(uuidGenerator, duplicatesWorkerService)

  private val filesOperationsApi = new FilesOperationsApi

  def receive: Receive = runRoute {
    handleExceptionsFilter {
      corsFilter {
        pathPrefix("api") {
          duplicatesApi.route ~ filesOperationsApi.route
        }
      }
    }
  }
}

class DuplicatesApi(uuidGenerator: UuidGenerator, duplicatesWorkerService: DuplicatesWorkerService)(implicit ec: ExecutionContext) extends HttpServiceBase {

  import pl.yourcode.photocomparer.marshaller._

  def route: Route = findDuplicates ~ getDuplicateJob ~ getDuplicateJobs

  private def findDuplicates: Route = post {
    path("duplicates") {
      entity(as[DuplicatesRequest]) { duplicatesRequest =>
        complete {
          val uuid = uuidGenerator.generateUuid
          duplicatesWorkerService.fireJob(uuid, duplicatesRequest.directory)
          DuplicatesResponse(Seq(Link("self", "/api/duplicates"), Link("job", s"/api/duplicates/$uuid")))
        }
      }
    }
  }

  private def getDuplicateJob: Route = get {
    path("duplicates" / Segment) { jobUuid =>
      complete {
        duplicatesWorkerService.checkJobStatus(jobUuid)
      }
    }
  }

  private def getDuplicateJobs: Route = get {
    path("duplicates") {
      complete {
        duplicatesWorkerService.getAllJobsUuids.map(_.toSeq).map { jobsUuids =>
          val links = jobsUuids.map { jobUuid => Link("job", s"/api/duplicates/$jobUuid") }
          DuplicatesJobsResponse(jobsUuids, links)
        }
      }
    }
  }
}

class FilesOperationsApi extends HttpServiceBase {

  import pl.yourcode.photocomparer.marshaller._

  def route: Route = deleteFiles

  private def deleteFiles: Route = post {
    path("files" / "deletions") {
      entity(as[Seq[String]]) { filesNames =>
        val files = filesNames.map { fileName => new File(fileName) }.filter { file => file.exists() }
        FileOperations.deleteAll(files)
        complete {
          Deletions(s"Deleted ${files.size} file(s).")
        }
      }
    }
  }
}
