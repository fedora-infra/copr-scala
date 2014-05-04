package me.elrod.copr

import argonaut._, Argonaut._
import java.io.{ DataOutputStream, InputStream }
import java.net.{ HttpURLConnection, URL, URLEncoder }
import scala.io.{ Codec, Source }
import scalaz._, Scalaz._
import scalaz.effect._

case class CoprConfig(baseurl: String, authentication: Option[(String, String)])

case class Repo(
  yumRepos: Map[String, String],
  additionalRepos: String,
  instructions: String,
  name: String,
  description: String,
  lastModified: Option[Long])

sealed trait Response
case class Coprs(output: String, repos: Option[List[Repo]], error: Option[String]) extends Response
case class CoprDetail(output: String, detail: Option[Repo]) extends Response

trait ResponseInstances {
  implicit def RepoCodecJson: CodecJson[Repo] =
    casecodec6(Repo.apply, Repo.unapply)("yum_repos", "additional_repos", "instructions", "name", "description", "last_modified")

  implicit def CoprsCodecJson: CodecJson[Coprs] =
    casecodec3(Coprs.apply, Coprs.unapply)("output", "repos", "error")

  implicit def CoprDetailCodecJson: CodecJson[CoprDetail] =
    casecodec2(CoprDetail.apply, CoprDetail.unapply)("output", "detail")
}

object Copr extends ResponseInstances {
  private def base64(s: String) = {
    val letters = ('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Vector('+', '/')
    def pickLetter(b: String) = {
      val x = b.size % 3
      if (x != 0) {
        letters(Integer.parseInt(b + ("00" * x), 2)) + ("=" * x)
      } else {
        letters(Integer.parseInt(b, 2))
      }
    }

    s.getBytes
      .map(((_: Byte).toInt) andThen Integer.toBinaryString _ andThen (b => "0" * (8 - b.length) + b))
      .mkString
      .grouped(6)
      .map(pickLetter)
      .mkString
  }

  def apiPostIO(config: CoprConfig, path: String, json: String): IO[InputStream] = IO {
    val connection: HttpURLConnection =
      new URL(config.baseurl + path).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("POST")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    config.authentication.map(a =>
      connection.setRequestProperty("Authorization", "Basic " + base64(a._1 + ":" + a._2)))
    val os = new DataOutputStream(connection.getOutputStream)
    os.writeBytes(json)
    os.close
    connection.getInputStream
  }

  def apiGetIO(config: CoprConfig, path: String): IO[InputStream] = IO {
    val connection: HttpURLConnection =
      new URL(config.baseurl + path).openConnection.asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setDoOutput(true)
    connection.setRequestProperty("Content-Type", "application/json")
    config.authentication.map(a =>
      connection.setRequestProperty("Authorization", "Basic " + base64(a._1 + ":" + a._2)))
    connection.getInputStream
  }

  def coprs(config: CoprConfig, username: String): IO[String \/ Coprs] =
    apiGetIO(config, s"/${username}/")
      .map(i => Source.fromInputStream(i)(Codec.UTF8).mkString.decodeEither[Coprs])

  def coprDetail(config: CoprConfig, username: String, copr: String): IO[String \/ CoprDetail] =
    apiGetIO(config, s"/${username}/${copr}/detail")
      .map(i => Source.fromInputStream(i)(Codec.UTF8).mkString.decodeEither[CoprDetail])
}
