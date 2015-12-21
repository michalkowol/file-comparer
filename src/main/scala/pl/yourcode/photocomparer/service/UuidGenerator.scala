package pl.yourcode.photocomparer.service

import java.util.UUID

trait UuidGenerator {
  def generateUuid: String = UUID.randomUUID().toString
}
object UuidGenerator extends UuidGenerator
