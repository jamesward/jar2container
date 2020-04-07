/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File

import com.google.cloud.tools.jib.frontend.CredentialRetrieverFactory
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import com.google.cloud.tools.jib.api.{AbsoluteUnixPath, Containerizer, DockerDaemonImage, ImageReference, Jib, JibContainer, RegistryImage}
import com.google.cloud.tools.jib.json.JsonTemplateMapper
import com.google.cloud.tools.jib.registry.credentials.DockerCredentialHelper
import zio.{App, ZEnv, ZIO}
import zio.console._
import io.github.vigoo.clipp._
import io.github.vigoo.clipp.parsers._
import io.github.vigoo.clipp.syntax._
import io.github.vigoo.clipp.zio._

import scala.jdk.CollectionConverters._

object Main extends App {

  val defaultBaseImage = "gcr.io/distroless/java:8"

  case class Config(jar: File, image: String, baseImage: String, publish: Boolean)

  val configSpec = for {
    _ <- metadata("jar2container")
    jarfile <- parameter[File]("jarfile", "jarfile")
    image <- parameter[String]("image", "image")
    baseImage <- optional(namedParameter[String]("base image", "base image", 'b', "base"))
    publish <- flag("publish to container repo", 'p', "publish")
  } yield Config(jarfile, image, baseImage.getOrElse(defaultBaseImage), publish)

  def parseArgs(args: List[String]): ZIO[ClippEnv, ParserFailure, Config] = {
    Clipp.parseOrFail(args, configSpec).tapError { parserFailure =>
      Clipp.displayErrorsAndUsageInfo(configSpec)(parserFailure)
    }
  }

  def buildImage(config: Config): ZIO[Any, Throwable, JibContainer] = {
    ZIO.effect {
      val containerizer = if (config.publish) {
        val imageReference = ImageReference.parse(config.image)

        // todo: logger
        val factory = CredentialRetrieverFactory.forImage(imageReference, _ => ())

        val registryImage = RegistryImage.named(config.image)

        registryImage.addCredentialRetriever(factory.dockerConfig())
        registryImage.addCredentialRetriever(factory.wellKnownCredentialHelpers())
        registryImage.addCredentialRetriever(factory.googleApplicationDefaultCredentials())

        // todo: logger
        Containerizer.to(registryImage).setToolName("jar2container").addEventHandler(_ => ())
      }
      else {
        // todo: logger
        Containerizer.to(DockerDaemonImage.named(config.image)).addEventHandler(_ => ())
      }

      Jib.from(config.baseImage)
        .addLayer(List(config.jar.toPath).asJava, AbsoluteUnixPath.get("/app"))
        .setEntrypoint("java", "-jar", s"/app/${config.jar.getName}")
        .containerize(containerizer)
    }
  }

  def program(args: List[String]): ZIO[ClippEnv, Throwable, Unit] = {
    for {
      config <- parseArgs(args).mapError(parserFailure => new Exception(parserFailure.toString))
      container <- buildImage(config)
      action = if (config.publish) "Published image" else "Added image to local Docker"
      _ <- putStrLn(s"$action: ${container.getTargetImage.toStringWithTag}")
    } yield ()
  }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] = {
    program(args).foldM({ throwable =>
      throwable.printStackTrace()
      for {
        _ <- putStrLn(throwable.getMessage)
        exit <- ZIO.succeed(1)
      } yield exit
    }, { _ =>
      ZIO.succeed(0)
    })
  }

}
