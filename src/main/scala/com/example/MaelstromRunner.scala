package com.example

import cats.effect.IO
import fs2.io.stdinUtf8
import io.circe.syntax.*
import io.circe.Json

object MaelstromRunner {

  def run(handler: Json => Json): IO[Unit] = {
    stdinUtf8[IO](4096)
      .through(fs2.text.lines)
      .map { str =>
        io.circe.parser.parse(str).toOption.get
      }
      .map(handler(_))
      .map(_.asJson.noSpaces + "\n")
      .through(fs2.text.utf8.encode)
      .through(fs2.io.writeOutputStream(IO(System.out)))
      .compile
      .drain
  }
}
