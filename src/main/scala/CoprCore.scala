package org.fedoraproject.coprscala

import argonaut._, Argonaut._
import java.io.{ DataOutputStream, InputStream }
import java.net.{ HttpURLConnection, URL }
import scalaz._, Scalaz._
import scalaz.concurrent.Task

object CoprCore extends ResponseInstances {
  private def base64(s: String): String = {
    val letters = ('A' to 'Z').toList |+| ('a' to 'z').toList |+| ('0' to '9').toList |+| List('+', '/')
    def pickLetter(b: String) = {
      (b.size % 3) match {
        case 0 => letters(Integer.parseInt(b, 2))
        case x => letters(Integer.parseInt(b + ("00" * x), 2)) + ("=" * x)
      }
    }

    (s.getBytes.toList ∘ (((_: Byte).toInt) >>>
      Integer.toBinaryString _ >>>
        (b => "0" * (8 - b.length) |+| b)))
      .mkString
      .grouped(6)
      .map(pickLetter)
      .mkString
  }

  def apiPostIO(config: CoprConfig, path: String, json: String): Task[InputStream] = Task {
    val connection: HttpURLConnection =
      new URL(config.baseurl + path).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    config.authentication.map(a =>
      connection.setRequestProperty("Authorization", "Basic " |+| base64(a._1 |+| ":" |+| a._2)))
    val os = new DataOutputStream(connection.getOutputStream)
    os.writeBytes(json)
    os.close
    connection.getInputStream
  }

  def apiGetIO(config: CoprConfig, path: String): Task[InputStream] = Task {
    val connection: HttpURLConnection =
      new URL(config.baseurl + path).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    config.authentication.map(a =>
      connection.setRequestProperty("Authorization", "Basic " |+| base64(a._1 |+| ":" |+| a._2)))
    connection.getInputStream
  }
}
