package pl.yourcode.photocomparer

import org.slf4j.LoggerFactory
import com.typesafe.scalalogging.Logger

trait Logging {
  protected val log: Logger = Logger(LoggerFactory.getLogger(getClass.getName))
}
