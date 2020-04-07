enablePlugins(GraalVMNativeImagePlugin, AutomateHeaderPlugin)

name := "jar2container"

scalaVersion := "2.13.1"

resolvers += Resolver.mavenLocal

libraryDependencies += "io.github.vigoo" %% "clipp-zio" % "0.3.1"
libraryDependencies += "com.google.cloud.tools" % "jib-core" % "0.13.1"

libraryDependencies += "dev.zio" %% "zio-test-sbt" % "1.0.0-RC18-2" % "test"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-explaintypes",
  "-feature",
  "-Xcheckinit",
  //"-Xlog-implicits",
  "-Xfatal-warnings",
  "-Xlint:adapted-args",
  "-Xlint:constant",
  "-Xlint:delayedinit-select",
  "-Xlint:doc-detached",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:nullary-override",
  "-Xlint:nullary-unit",
  "-Xlint:option-implicit",
  "-Xlint:package-object-classes",
  "-Xlint:poly-implicit-overload",
  "-Xlint:private-shadow",
  "-Xlint:stars-align",
  "-Xlint:type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-extra-implicit",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused:implicits",
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
)

javacOptions ++= Seq("-source", "11", "-target", "11")

//fork := true

//javaOptions += "-agentpath:/home/jw/bin/native-image-svm/lib/libnative-image-agent.so=config-output-dir=/tmp"

scalacOptions += "-target:jvm-11"

initialize := {
  val _ = initialize.value
  val javaVersion = sys.props("java.specification.version")
  if (javaVersion != "11")
    sys.error("Java 11 is required for this project. Found " + javaVersion + " instead")
}

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false

graalVMNativeImageGraalVersion := Some("20.0.0-java11")

graalVMNativeImageOptions ++= Seq(
  "--verbose",
  //"-Djavax.net.debug=all",
  "--no-server",
  "--no-fallback",
  //"--allow-incomplete-classpath",
  //"--enable-all-security-services",
  //"-H:EnableURLProtocols=http,https",
  "--enable-http",
  "--enable-https",
  "--report-unsupported-elements-at-runtime",
  "-H:ReflectionConfigurationFiles=stage/resources/reflect.json",
  "-H:+ReportExceptionStackTraces",
  "-H:+ReportUnsupportedElementsAtRuntime",
  "-H:+TraceClassInitialization",
  "-H:+PrintClassInitialization",
  //"-H:+JNI",
  "--initialize-at-build-time=scala.runtime.Statics$VM",
  "--initialize-at-build-time=org.apache.http.HttpClientConnection",
  "--initialize-at-build-time=org.apache.http.conn.HttpClientConnectionManager",
  "--initialize-at-build-time=org.apache.http.conn.routing.HttpRoute",
  "--initialize-at-build-time=org.apache.http.pool.ConnPoolControl",
  "--initialize-at-build-time=org.apache.http.protocol.HttpContext",
  "--initialize-at-build-time=org.apache.http.conn.ssl.SSLConnectionSocketFactory",
  "--initialize-at-build-time=org.apache.commons.logging",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$1",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$2",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$3",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$4",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$5",
//  "--initialize-at-build-time=org.apache.commons.logging.LogFactory$6",
//  "--initialize-at-build-time=org.apache.commons.logging.impl.LogFactoryImpl",
//  "--initialize-at-build-time=org.apache.commons.logging.impl.WeakHashtable$Referenced",
//  "--initialize-at-build-time=org.apache.commons.logging.impl.WeakHashtable$WeakKey",
//  "--initialize-at-build-time=org.apache.commons.logging.impl.WeakHashtable",
  "--initialize-at-build-time=org.apache.commons.logging.impl.SimpleLog",
  //"--initialize-at-build-time=com.fasterxml.jackson.annotation.JsonProperty$Access",
  //"--initialize-at-build-time=com.fasterxml.jackson.annotation.JsonIgnoreProperties$Value",
)


// license header stuff
organizationName := "Google LLC"
startYear := Some(java.time.Year.now.getValue)
licenses += "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.txt")
