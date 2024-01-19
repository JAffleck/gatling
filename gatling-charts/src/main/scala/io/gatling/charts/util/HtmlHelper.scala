/*
 * Copyright 2011-2024 GatlingCorp (https://gatling.io)
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

package io.gatling.charts.util

import java.{ lang => jl }

import scala.io.{ Codec, Source }
import scala.util.Using

import io.github.metarank.cfor._

private[charts] object HtmlHelper {
  private val charToHtml: Map[Char, String] =
    Using.resource(Source.fromResource("html-entities.properties")(Codec.UTF8)) { source =>
      source
        .getLines()
        .collect {
          case line if !line.startsWith("#") && line.nonEmpty =>
            val Array(entityName, code) = line.split("=", 2)
            val entity = s"&$entityName;"
            val char = code.toInt.toChar
            char -> entity
        }
        .toMap
    }

  implicit final class HtmlRichString(val string: String) extends AnyVal {
    def htmlEscape: String = {
      val sb = new jl.StringBuilder(string.length)
      cfor(0 until string.length) { i =>
        val char = string.charAt(i)
        charToHtml.get(char) match {
          case Some(entity) => sb.append(entity)
          case _            => sb.append(char)
        }
      }
      sb.toString
    }
  }
}
