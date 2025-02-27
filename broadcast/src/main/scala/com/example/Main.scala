package com.example

import cats.effect.{IO, IOApp}
import io.circe.Json

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

object Main extends IOApp.Simple {
  def run: IO[Unit] = {
    MaelstromRunner.runMulti(handler)
  }

  private val nodeId = AtomicReference[String]("")
  private val msgId = AtomicInteger(0)
  private val id = AtomicInteger(0)
  private val messages = AtomicReference[List[Int]](List.empty)
  private val topology = AtomicReference[List[String]](List.empty)

  // https://fly.io/dist-sys/3a/
  // https://fly.io/dist-sys/3b/
  private def handler(msg: Json): List[Json] = {
    System.err.println(s"input = ${msg.noSpaces}")
    val obj = msg.asObject.get
    val src = obj.apply("src").get.asString.get
    val body = obj.apply("body").get.asObject.get
    val msgType = body.apply("type").flatMap(_.asString).getOrElse("")
    val res = if (msgType.equalsIgnoreCase("broadcast")) {
      val in_reply_to = body.apply("msg_id").get
      val message = body.apply("message").get.asNumber.get.toInt.get
      messages.updateAndGet(list => message +: list)
      val newBody = Json.obj(
        ("msg_id", Json.fromInt(msgId.incrementAndGet())),
        ("type", Json.fromString("broadcast_ok")),
        ("in_reply_to", body.apply("msg_id").get)
      )
      List(obj.add("body", newBody)
        .add("dest", Json.fromString(src))
        .add("src", Json.fromString(nodeId.get()))
        .toJson)
        ++ topology.get.map { nId =>
        obj
          .add("dest", Json.fromString(nId))
          .add("src", Json.fromString(nodeId.get()))
          .toJson
      }
    } else if (msgType.equalsIgnoreCase("broadcast_ok")) {
      List.empty
    } else if (msgType.equalsIgnoreCase("read")) {
      val in_reply_to = body.apply("msg_id").get
      val newBody = Json.obj(
        ("msg_id", Json.fromInt(msgId.incrementAndGet())),
        ("type", Json.fromString("read_ok")),
        ("in_reply_to", body.apply("msg_id").get),
        ("messages", Json.fromValues(messages.get().map(Json.fromInt)))
      )
      List(obj.add("body", newBody)
        .add("dest", Json.fromString(src))
        .add("src", Json.fromString(nodeId.get()))
        .toJson)
    } else if (msgType.equalsIgnoreCase("topology")) {
      val newTopology: List[String] = body.apply("topology").get.asObject.get
        .apply(nodeId.get())
        .map(_.asArray.get.map(_.asString.get).toList)
        .getOrElse(List.empty)
      topology.set(newTopology)
      val in_reply_to = body.apply("msg_id").get
      val newBody = Json.obj(
        ("msg_id", Json.fromInt(msgId.incrementAndGet())),
        ("type", Json.fromString("topology_ok")),
        ("in_reply_to", body.apply("msg_id").get)
      )
      List(obj.add("body", newBody)
        .add("dest", Json.fromString(src))
        .add("src", Json.fromString(nodeId.get()))
        .toJson)
    } else if (msgType.equalsIgnoreCase("init")) {
      val newNodeId = body.apply("node_id").get.asString.get
      nodeId.set(newNodeId)
      System.err.println(s"Initialized node $newNodeId")

      val newBody = Json.obj(
        ("msg_id", Json.fromInt(msgId.incrementAndGet())),
        ("type", Json.fromString("init_ok")),
        ("in_reply_to", body.apply("msg_id").get)
      )
      List(Json.obj(("body", newBody)).asObject.get
        .add("dest", Json.fromString(src))
        .add("src", Json.fromString(nodeId.get()))
        .toJson)
    } else {
      throw new IllegalArgumentException(s"msgType not found: $msgType")
    }
    System.err.println(s"output = ${res.map(_.noSpaces).mkString("\n,")}")
    res
  }
}


