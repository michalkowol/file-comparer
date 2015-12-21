package pl.yourcode.photocomparer.exceptions

class NoSuchJobException(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)
class OtherException(message: String, cause: Option[Throwable] = None) extends Exception(message, cause.orNull)
