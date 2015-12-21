package pl.yourcode.photocomparer

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait Logging {
  protected val log: Logger = Logger(LoggerFactory.getLogger(getClass.getName))
}
