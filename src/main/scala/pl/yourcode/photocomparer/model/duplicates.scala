package pl.yourcode.photocomparer.model

case class DuplicatesRequest(directory: String)
case class DuplicatesResponse(links: Seq[Link])
case class DuplicatesJobsResponse(jobsUuid: Seq[String], links: Seq[Link])
