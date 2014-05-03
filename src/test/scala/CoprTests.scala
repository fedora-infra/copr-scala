package me.elrod.copr.test

import org.scalatest.FunSuite

import argonaut._, Argonaut._
import me.elrod.copr._, Copr._
import io.Source
import scalaz._, Scalaz._
import scalaz.effect._

class CoprTests extends FunSuite {

  // Holy crap this is terrible.
  val auth: IO[Option[(String, String)]] = IO {
    Option(System.getenv("USER")) map { u =>
      val List(login, token) =
        scala.io.Source.fromFile(s"/home/${u}/.config/copr")
        .getLines
        .filter(e => e.startsWith("login") || e.startsWith("token"))
        .map(_.split(" = ").last).toList
      (login, token)
    }
  }

  test("Can get a list of my coprs") {
    val r: IO[String \/ Coprs] = for {
      authOpt <- auth
      resp <- coprs(CoprConfig("http://copr.fedoraproject.org/api/coprs", authOpt), "codeblock")
    } yield resp

    r.map { rr =>
      rr match {
        case \/-(coprs) => {
          assertResult("ok", ".output")(coprs.output)
          assertResult(Some(true), "contains watchman")(coprs.repos.map(_.map(_.name).contains("watchman")))
        }
        case -\/(err) => println(err)
      }
    }.unsafePerformIO
  }
}
