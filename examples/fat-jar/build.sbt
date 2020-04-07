name := "fat-jar"

autoScalaLibrary := false

sources in (Compile, doc) := Seq.empty

publishArtifact in (Compile, packageDoc) := false
