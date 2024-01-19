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

package io.gatling.app.classloader

import java.io.InputStream
import java.net.{ URI, URL, URLConnection }
import java.nio.file.{ Files, Path, Paths }
import java.security.{ CodeSource, ProtectionDomain }
import java.security.cert.Certificate

import scala.collection.mutable

import io.gatling.shared.util.PathHelper

private final class FileSystemBackedClassLoader(root: Path, parent: ClassLoader) extends ClassLoader(parent) {
  private def classNameToPath(name: String): Path =
    if (name.endsWith(".class")) Paths.get(name)
    else Paths.get(name.replace('.', '/') + ".class")

  private def dirNameToPath(name: String): Path =
    Paths.get(name.replace('.', '/'))

  private def findPath(path: Path): Option[Path] = {
    val fullPath = root.resolve(path)
    Some(fullPath).filter(Files.exists(_))
  }

  @SuppressWarnings(Array("org.wartremover.warts.JavaNetURLConstructors"))
  override def findResource(name: String): URL =
    findPath(Paths.get(name)).map { path =>
      new URL(
        null,
        "repldir:" + path.toString,
        (url: URL) =>
          new URLConnection(url) {
            override def connect(): Unit = ()
            override def getInputStream: InputStream = Files.newInputStream(path)
          }
      )
    }.orNull

  override def getResourceAsStream(name: String): InputStream = findPath(Paths.get(name)) match {
    case Some(path) => Files.newInputStream(path)
    case _          => super.getResourceAsStream(name)
  }

  private def classAsStream(className: String): Option[InputStream] =
    Option(getResourceAsStream(className.replaceAll("""\.""", "/") + ".class"))

  private def classBytes(name: String): Array[Byte] = findPath(classNameToPath(name)) match {
    case Some(path) => Files.readAllBytes(path)
    case _ =>
      classAsStream(name) match {
        case Some(stream) => stream.readAllBytes()
        case _            => Array.empty
      }
  }

  override def findClass(name: String): Class[_] = {
    val bytes = classBytes(name)
    if (bytes.length == 0) throw new ClassNotFoundException(name)
    else defineClass(name, bytes, 0, bytes.length, protectionDomain)
  }

  private val pckgs = mutable.Map[String, Package]()

  private lazy val protectionDomain = {
    val cl = Thread.currentThread.getContextClassLoader
    val resource = cl.getResource("scala/runtime/package.class")
    if (resource == null || resource.getProtocol != "jar") null
    else {
      val s = resource.getPath
      val n = s.lastIndexOf('!')
      if (n < 0) null
      else {
        val path = s.substring(0, n)
        new ProtectionDomain(new CodeSource(new URI(path).toURL, null.asInstanceOf[Array[Certificate]]), null, this, null)
      }
    }
  }

  override def definePackage(
      name: String,
      specTitle: String,
      specVersion: String,
      specVendor: String,
      implTitle: String,
      implVersion: String,
      implVendor: String,
      sealBase: URL
  ): Package =
    throw new UnsupportedOperationException()

  override def getPackage(name: String): Package = findPath(dirNameToPath(name)) match {
    case None => super.getPackage(name)
    case _ =>
      pckgs.getOrElseUpdate(
        name, {
          val constructor = classOf[Package].getDeclaredConstructor(
            classOf[String],
            classOf[String],
            classOf[String],
            classOf[String],
            classOf[String],
            classOf[String],
            classOf[String],
            classOf[URL],
            classOf[ClassLoader]
          )
          constructor.setAccessible(true)
          constructor.newInstance(name, null, null, null, null, null, null, null, this)
        }
      )
  }

  override def getPackages: Array[Package] =
    PathHelper.deepDirs(root).map(path => getPackage(path.toString)).toArray
}
