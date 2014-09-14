package org.fedoraproject.coprscala

import argonaut._, Argonaut._
import java.io.{ DataOutputStream, InputStream }
import java.net.{ HttpURLConnection, URL, URLEncoder }
import monocle.Lenser
import scala.io.{ Codec, Source }
import scalaz._, Scalaz._
import scalaz.concurrent.Task
import scalaz.concurrent.Task._

case class CoprConfig(baseurl: String, authentication: Option[(String, String)])

case class Repo(
  yumRepos: Map[String, String],
  additionalRepos: String,
  instructions: String,
  name: String,
  description: String,
  lastModified: Option[Long])

object Repo {
  val lenser          = Lenser[Repo]
  val yumRepos        = lenser(_.yumRepos)
  val additionalRepos = lenser(_.additionalRepos)
  val instructions    = lenser(_.instructions)
  val name            = lenser(_.name)
  val description     = lenser(_.description)
  val lastModified    = lenser(_.lastModified)
 }

sealed trait Response
case class Coprs(output: String, repos: Option[List[Repo]], error: Option[String]) extends Response

object Coprs {
  val lenser = Lenser[Coprs]
  val output = lenser(_.output)
  val repos  = lenser(_.repos)
  val error  = lenser(_.error)
}

case class CoprDetail(output: String, detail: Option[Repo]) extends Response

object CoprDetail {
  val lenser = Lenser[CoprDetail]
  val output = lenser(_.output)
  val detail = lenser(_.detail)
}

case class BuildDetail(
  status: String, /* TODO: sum type? */
  project: String,
  owner: String,
  results: String,
  builtPkgs: List[String],
  srcVersion: String,
  chroots: Map[String, String], /* TODO: value -> sum type? */
  submittedOn: Long, /* TODO: some date object */
  startedOn: Long, /* TODO: some date object */
  endedOn: Long, /* TODO: some date object */
  srcPkg: String,
  submittedBy: String,
  output: String /* TODO: sum type? */
) extends Response

object BuildDetail {
  val lenser      = Lenser[BuildDetail]
  val status      = lenser(_.status)
  val project     = lenser(_.project)
  val owner       = lenser(_.owner)
  val results     = lenser(_.results)
  val builtPkgs   = lenser(_.builtPkgs)
  val srcVersion  = lenser(_.srcVersion)
  val chroots     = lenser(_.chroots)
  val submittedOn = lenser(_.submittedOn)
  val startedOn   = lenser(_.startedOn)
  val endedOn     = lenser(_.endedOn)
  val srcPkg      = lenser(_.srcPkg)
  val submittedBy = lenser(_.submittedBy)
  val output      = lenser(_.output)
}

trait ResponseInstances {
  implicit def RepoCodecJson: CodecJson[Repo] =
    casecodec6(Repo.apply, Repo.unapply)("yum_repos", "additional_repos", "instructions", "name", "description", "last_modified")

  implicit def CoprsCodecJson: CodecJson[Coprs] =
    casecodec3(Coprs.apply, Coprs.unapply)("output", "repos", "error")

  implicit def CoprDetailCodecJson: CodecJson[CoprDetail] =
    casecodec2(CoprDetail.apply, CoprDetail.unapply)("output", "detail")

  implicit def BuildDetailCodecJson: CodecJson[BuildDetail] =
    casecodec13(BuildDetail.apply, BuildDetail.unapply)("status", "project", "owner", "results", "built_pkgs", "src_version", "chroots", "submitted_on", "started_on", "ended_on", "src_pkg", "submitted_by", "output")
}

object Copr extends ResponseInstances {
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

  def coprs(config: CoprConfig)(username: String): Task[String \/ Coprs] =
    apiGetIO(config, s"/${username}/") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[Coprs])

  def coprDetail(config: CoprConfig)(username: String, copr: String): Task[String \/ CoprDetail] =
    apiGetIO(config, s"/${username}/${copr}/detail") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[CoprDetail])

  def buildDetail(config: CoprConfig)(buildId: Int): Task[String \/ BuildDetail] =
    apiGetIO(config, s"/build_detail/${buildId}/") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[BuildDetail])
}
