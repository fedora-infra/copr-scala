package org.fedoraproject.coprscala
import monocle.Macro._

case class Repo(
  yumRepos: Map[String, String],
  additionalRepos: String,
  instructions: String,
  name: String,
  description: String,
  lastModified: Option[Long])

object Repo {
   val yumRepos        = mkLens[Repo, Map[String, String]]("yumRepos")
   val additionalRepos = mkLens[Repo, String]("additionalRepos")
   val instructions    = mkLens[Repo, String]("instructions")
   val name            = mkLens[Repo, String]("name")
   val description     = mkLens[Repo, String]("description")
   val lastModified    = mkLens[Repo, Option[Long]]("lastModified")
 }
