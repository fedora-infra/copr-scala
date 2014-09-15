package org.fedoraproject.coprscala

import argonaut._, Argonaut._
import scala.io.{ Codec, Source }
import scalaz._, Scalaz._
import scalaz.concurrent.Task

object Copr {
  import CoprCore._

  def coprs(config: CoprConfig)(username: String): Task[String \/ Coprs] =
    apiGetIO(config, s"/${username}/") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[Coprs])

  def coprDetail(config: CoprConfig)(username: String, copr: String): Task[String \/ CoprDetail] =
    apiGetIO(config, s"/${username}/${copr}/detail") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[CoprDetail])

  def buildDetail(config: CoprConfig)(buildId: Int): Task[String \/ BuildDetail] =
    apiGetIO(config, s"/build_detail/${buildId}/") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[BuildDetail])

  def monitor(config: CoprConfig)(username: String, copr: String): Task[String \/ Monitor] =
    apiGetIO(config, s"/${username}/${copr}/monitor/") ∘
    (Source.fromInputStream(_)(Codec.UTF8).mkString.decodeEither[Monitor])
}
