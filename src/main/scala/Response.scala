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

case class Monitor(
  builds: List[MonitorBuildDetail],
  chroots: List[String],
  packages: List[MonitorPackage]
) extends Response
object Monitor {
  val builds   = mkLens[Monitor, List[MonitorBuildDetail]]("builds")
  val chroots  = mkLens[Monitor, List[String]]("chroots")
  val packages = mkLens[Monitor, List[MonitorPackage]]("packages")
}

case class MonitorBuildDetail(
  builtPackages: List[String],
  canceled: Boolean,
  endedOn: Long,
  id: Long,
  memoryReqs: Int,
  pkgVersion: Option[String],
  repos: String,
  results: String,
  srcPkg: String,
  startedOn: Long,
  state: String,
  status: Int,
  submittedOn: Long,
  timeout: Long
)
object MonitorBuildDetail {
  val builtPackages = mkLens[MonitorBuildDetail, List[String]]("builtPackages")
  val canceled      = mkLens[MonitorBuildDetail, Boolean]("canceled")
  val endedOn       = mkLens[MonitorBuildDetail, Long]("endedOn")
  val id            = mkLens[MonitorBuildDetail, Long]("id")
  val memoryReqs    = mkLens[MonitorBuildDetail, Int]("memoryReqs")
  val pkgVersion    = mkLens[MonitorBuildDetail, Option[String]]("pkgVersion")
  val repos         = mkLens[MonitorBuildDetail, String]("repos")
  val results       = mkLens[MonitorBuildDetail, String]("results")
  val srcPkg        = mkLens[MonitorBuildDetail, String]("srcPkg")
  val startedOn     = mkLens[MonitorBuildDetail, Long]("startedOn")
  val state         = mkLens[MonitorBuildDetail, String]("state")
  val status        = mkLens[MonitorBuildDetail, Int]("status")
  val submittedOn   = mkLens[MonitorBuildDetail, Long]("submittedOn")
  val timeout       = mkLens[MonitorBuildDetail, Long]("timeout")
}

case class MonitorPackage(
  pkgName: String,
  pkgVersion: Option[String],
  results: Map[String, (Long, String)]
)
object MonitorPackage {
  val pkgName    = mkLens[MonitorPackage, String]("pkgName")
  val pkgVersion = mkLens[MonitorPackage, Option[String]]("pkgVersion")
  val results    = mkLens[MonitorPackage, Map[String, (Long, String)]]("results")
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

  implicit def MonitorCodecJson: CodecJson[Monitor] =
    casecodec3(Monitor.apply, Monitor.unapply)("builds", "chroots", "packages")

  implicit def MonitorBuildDetailCodecJson: CodecJson[MonitorBuildDetail] =
    casecodec14(MonitorBuildDetail.apply, MonitorBuildDetail.unapply)("built_packages", "canceled", "ended_on", "id", "memory_reqs", "pkg_version", "repos", "results", "src_pkg", "started_on", "state", "status", "submitted_on", "timeout")

  implicit def MonitorPackageCodecJson: CodecJson[MonitorPackage] =
    casecodec3(MonitorPackage.apply, MonitorPackage.unapply)("pkg_name", "pkg_version", "results")
}
