package me.elrod.copr.test

import org.scalatest.FunSuite

import argonaut._, Argonaut._
import me.elrod.copr._, Copr._
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

    r.map { rr =>
      rr match {
        case \/-(coprs) => {
          assertResult("ok", ".output")(coprs.output)
          assertResult(Some(true), "contains watchman")(coprs.repos.map(_.map(_.name).contains("watchman")))
        }
        case -\/(err) => println(err)
      }
    }.run
  }

  test("Can get detail about one of my coprs") {
    val r: Task[String \/ CoprDetail] = for {
      c <- config
      resp <- coprDetail(c)("codeblock", "evalso")
    } yield resp

    r.map { rr =>
      rr match {
        case \/-(copr) => {
          assertResult("ok", ".output")(copr.output)
          assertResult(Some("evalso"), ".detail.name")(copr.detail.map(_.name))
        }
        case -\/(err) => println(err)
      }
    }.run
  }

  test("Can get detail about one of my builds") {
    val r: Task[String \/ BuildDetail] = for {
      c <- config
      resp <- buildDetail(c)(27256)
    } yield resp

    r.map { rr =>
      rr match {
        case \/-(build) => {
          assertResult("ok", ".output")(build.output)
          assertResult(Some("succeeded"), "epel-7-x86_64 chroot")(build.chroots.get("epel-7-x86_64"))
          assertResult("codeblock", ".submittedBy")(build.submittedBy)
        }
        case -\/(err) => println(err)
      }
    }.run
  }

  test("Can get detail about one of my builds using lenses") {
    import BuildDetail._
    val r: Task[String \/ BuildDetail] = for {
      c <- config
      resp <- buildDetail(c)(27256)
    } yield resp

    r.map { rr =>
      rr match {
        case \/-(build) => {
          assertResult("ok", ".output")(build |-> output get)
          assertResult(Some("succeeded"), "epel-7-x86_64 chroot")(build |-> chroots |-> at("epel-7-x86_64") get)
          assertResult("codeblock", ".submittedBy")(build |-> submittedBy get)
        }
        case -\/(err) => println(err)
      }
    }.run
  }
}
