/*
 * Copyright 2011-2023 GatlingCorp (https://gatling.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.gatling.charts.config

import java.nio.file.{ Path, Paths }

import io.gatling.charts.FileNamingConventions
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.config.GatlingFiles

private[charts] object ChartsFiles {
  private val AllSessionsFile = "all_sessions.js"
  private val StatsJsFile = "stats.js"
  private val StatsJsonFile = "stats.json"
  private val GlobalStatsJsonFile = "global_stats.json"
  private val AssertionsJsonFile = "assertions.json"
  private val AssertionsJUnitFile = "assertions.xml"
  val GlobalPageName: String = "Global Information"
  val AllRequestLineTitle: String = "All Requests"

  val CommonJsFiles: Seq[String] = Seq(
    "jquery-3.5.1.min.js",
    "bootstrap.min.js",
    "gatling.js",
    "menu.js",
    "ellipsis.js",
    AllSessionsFile,
    StatsJsFile
  )

  private val GatlingJsFolder: Path = Paths.get("js")
  private val GatlingStyleFolder: Path = Paths.get("style")
  private val GatlingAssetsPackage: Path = Paths.get("io", "gatling", "charts", "assets")
  val GatlingAssetsJsPackage: Path = GatlingAssetsPackage.resolve(GatlingJsFolder)
  val GatlingAssetsStylePackage: Path = GatlingAssetsPackage.resolve(GatlingStyleFolder)
}

private[charts] class ChartsFiles(runUuid: String, configuration: GatlingConfiguration) {
  import ChartsFiles._

  private val resultDirectory = GatlingFiles.resultDirectory(runUuid, configuration)

  val jsDirectory: Path = resultDirectory.resolve(GatlingJsFolder)

  val styleDirectory: Path = resultDirectory.resolve(GatlingStyleFolder)

  val allSessionsFile: Path = jsDirectory.resolve(AllSessionsFile)

  val globalFile: Path = resultDirectory.resolve("index.html")

  def requestFile(requestName: String): Path =
    resultDirectory.resolve(requestName.toRequestFileName + ".html")

  def groupFile(requestName: String): Path =
    resultDirectory.resolve(requestName.toGroupFileName + ".html")

  val statsJsFile: Path = jsDirectory.resolve(StatsJsFile)

  val statsJsonFile: Path = jsDirectory.resolve(StatsJsonFile)

  val globalStatsJsonFile: Path = jsDirectory.resolve(GlobalStatsJsonFile)

  val assertionsJsonFile: Path = jsDirectory.resolve(AssertionsJsonFile)

  val assertionsJUnitFile: Path = jsDirectory.resolve(AssertionsJUnitFile)
}
