package org.fedoraproject.coprscala

import argonaut._, Argonaut._
import monocle.Macro._
import scalaz._, Scalaz._

sealed trait Response

case class Coprs(output: String, repos: Option[List[Repo]], error: Option[String]) extends Response
object Coprs {
  val output = mkLens[Coprs, String]("output")
  val repos  = mkLens[Coprs, Option[List[Repo]]]("repos")
  val error  = mkLens[Coprs, Option[String]]("error")
}

case class CoprDetail(output: String, detail: Option[Repo]) extends Response
object CoprDetail {
  val output = mkLens[CoprDetail, String]("output")
  val detail = mkLens[CoprDetail, Option[Repo]]("detail")
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
  val status      = mkLens[BuildDetail, String]("status")
  val project     = mkLens[BuildDetail, String]("project")
  val owner       = mkLens[BuildDetail, String]("owner")
  val results     = mkLens[BuildDetail, String]("results")
  val builtPkgs   = mkLens[BuildDetail, List[String]]("builtPkgs")
  val srcVersion  = mkLens[BuildDetail, String]("srcVersion")
  val chroots     = mkLens[BuildDetail, Map[String, String]]("chroots")
  val submittedOn = mkLens[BuildDetail, Long]("submittedOn")
  val startedOn   = mkLens[BuildDetail, Long]("startedOn")
  val endedOn     = mkLens[BuildDetail, Long]("endedOn")
  val srcPkg      = mkLens[BuildDetail, String]("srcPkg")
  val submittedBy = mkLens[BuildDetail, String]("submittedBy")
  val output      = mkLens[BuildDetail, String]("output")
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
