package me.elrod.copr.test

import org.scalatest.FunSuite

import argonaut._, Argonaut._
import me.elrod.copr._, Copr._
import io.Source
import scalaz._, Scalaz._
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
    auth.map(a => CoprConfig("http://copr.fedoraproject.org/api/coprs", a))

  test("Can get a list of my coprs") {
    val r: Task[String \/ Coprs] = for {
      c <- config
      resp <- coprs(c, "codeblock")
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
      resp <- coprDetail(c, "codeblock", "evalso")
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
}
