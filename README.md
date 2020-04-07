jar2container
-------------

Build example jars
```
(cd examples/fat-jar; ../../sbt assembly)
(cd examples/launcher-jar; ../../sbt stage)
```

Use sbt to run jar2container against the fat-jar and launcher-jar examples
```
./sbt "run examples/fat-jar/target/scala-2.12/fat-jar-assembly-0.1.0-SNAPSHOT.jar fat-jar"
./sbt "run examples/launcher-jar/target/scala-2.12/default.launcher-jar-0.1.0-SNAPSHOT-launcher.jar launcher-jar"
```

Run the example images
```
docker run --rm fat-jar
docker run --rm launcher-jar
```

Build the native image
```
./sbt graalvm-native-image:packageBin
```

Run the native image executable with the fat-jar and launcher-jar examples
```
target/graalvm-native-image/jar2container examples/fat-jar/target/scala-2.12/fat-jar-assembly-0.1.0-SNAPSHOT.jar far-jar
target/graalvm-native-image/jar2container examples/launcher-jar/target/scala-2.12/default.launcher-jar-0.1.0-SNAPSHOT-launcher.jar launcher-jar
```
