package org.fedoraproject.coprscala.test

import org.scalatest.FunSuite

import argonaut._, Argonaut._
import org.fedoraproject.coprscala._, Copr._
import monocle.function.At._
import monocle.std._
import monocle.syntax._
import io.Source
import scala.language.postfixOps
import scalaz.{ Lens => _, _ }
import scalaz.concurrent.Task
import scalaz.concurrent.Task._

class CoprTests extends FunSuite {

  // Holy crap this is terrible.
  val auth: Task[Option[(String, String)]] = Task {
    Option(System.getenv("USER")) map { u =>
      val List(login, token) =
        scala.io.Source.fromFile(s"/home/${u}/.config/copr")
        .getLines
        .filter(e => e.startsWith("login") || e.startsWith("token"))
        .map(_.split(" = ").last).toList
      (login, token)
    }
  }

  def config: Task[CoprConfig] =
    auth.map(a => CoprConfig("https://copr.fedoraproject.org/api/coprs", a))

  test("Can get a list of my coprs") {
    val r: Task[String \/ Coprs] = for {
      c <- config
      resp <- coprs(c)("codeblock")
    } yield resp

    r.map(_.fold(
      err => {
        println(err)
        throw new Exception(err)
      },
      coprs => {
          assertResult("ok", ".output")(coprs.output)
          assertResult(Some(true), "contains watchman")(coprs.repos.map(_.map(_.name).contains("watchman")))
      }
    )).run
  }

  test("Can get detail about one of my coprs") {
    val r: Task[String \/ CoprDetail] = for {
      c <- config
      resp <- coprDetail(c)("codeblock", "evalso")
    } yield resp

    r.map(_.fold(
      err => {
        println(err)
        throw new Exception(err)
      },
      copr => {
        assertResult("ok", ".output")(copr.output)
        assertResult(Some("evalso"), ".detail.name")(copr.detail.map(_.name))
      }
    )).run
  }

  test("Can get detail about one of my builds") {
    val r: Task[String \/ BuildDetail] = for {
      c <- config
      resp <- buildDetail(c)(1009)
    } yield resp

    r.map(_.fold(
      err => {
        println(err)
        throw new Exception(err)
      },
      build => {
        assertResult("ok", ".output")(build.output)
        assertResult(Some("succeeded"), "fedora-20-x86_64 chroot")(build.chroots.get("fedora-20-x86_64"))
        assertResult("codeblock", ".submittedBy")(build.submittedBy)
      }
    )).run
  }

  test("Can get detail about one of my builds using lenses") {
    import BuildDetail._
    val r: Task[String \/ BuildDetail] = for {
      c <- config
      resp <- buildDetail(c)(1009)
    } yield resp

    r.map(_.fold(
      err => {
        println(err)
        throw new Exception(err)
      },
      build => {
        assertResult("ok", ".output")(build |-> output get)
        assertResult(Some("succeeded"), "fedora-20-x86_64 chroot")(build |-> chroots |-> at("fedora-20-x86_64") get)
        assertResult("codeblock", ".submittedBy")(build |-> submittedBy get)
      }
    )).run
  }

  test("Can view (and parse) the monitor page") {
    val r: Task[String \/ Monitor] = for {
      c <- config
      resp <- monitor(c)("codeblock", "evalso")
    } yield resp

    r.map(_.fold(
      err => {
        println(err)
        throw new Exception(err)
      },
      mon =>
        assertResult(true, "chroots contains fedora-20-x86_64")(mon.chroots.contains("fedora-20-x86_64"))
    )).run
  }
}
