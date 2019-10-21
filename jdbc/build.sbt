libraryDependencies ++= Seq(
  "com.lucidchart" % "opentracing-thread-context" % "0.5",
  "io.opentracing" % "opentracing-api" % "0.31.0",
  "io.opentracing.contrib" % "opentracing-globaltracer" % "0.1.0",
  "p6spy" % "p6spy" % "3.0.0"
)

moduleName := s"opentracing-${name.value}"
publishTo := sonatypePublishToBundle.value
