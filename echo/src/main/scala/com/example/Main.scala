package com.example

import cats.effect.{IO, IOApp}
import io.circe.Json

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    MaelstromRunner.run(handler)
  }

  private val nodeId = AtomicReference[String]("")
  private val msgId = AtomicInteger(0)

  // https://github.com/jepsen-io/maelstrom/blob/main/doc/02-echo/index.md
  private def handler(msg: Json): Json = {
    System.err.println(s"input = ${msg.noSpaces}")
    val obj = msg.asObject.get
    val src = obj.apply("src").get.asString.get
    val body = obj.apply("body").get.asObject.get
    val msgType = body.apply("type").flatMap(_.asString).getOrElse("")
    val res1 = if (msgType.equalsIgnoreCase("echo")) {
      val in_reply_to = body.apply("msg_id").get
      val newBody = body
        .add("msg_id", Json.fromInt(msgId.incrementAndGet()))
        .add("type", Json.fromString("echo_ok"))
        .add("in_reply_to", in_reply_to)
        .toJson
      obj.add("body", newBody)
    } else if (msgType.equalsIgnoreCase("init")) {
      val newNodeId = body.apply("node_id").get.asString.get
      nodeId.set(newNodeId)
      System.err.println(s"Initialized node $newNodeId")

      val newBody = Json.obj(
        ("msg_id", Json.fromInt(msgId.incrementAndGet())),
        ("type", Json.fromString("init_ok")),
        ("in_reply_to", body.apply("msg_id").get)
      )
      Json.obj(("body", newBody)).asObject.get
    } else {
      throw new IllegalArgumentException(s"msgType not found: $msgType")
    }
    val res2 = res1
      .add("dest", Json.fromString(src))
      .add("src", Json.fromString(nodeId.get()))
      .toJson
    System.err.println(s"output = ${res2.noSpaces}")
    res2
  }
}


