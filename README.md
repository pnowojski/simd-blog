# simd-blog
Source code for SIMD benchmarks and experiments in Java. Results were originally published on [prestodb.rocks](http://prestodb.rocks/code/simd/) blog.

To compile and run:

```
mvn clean package
java -jar ./target/benchmarks.jar
```

Note "-XX:CompileCommand=print" will not print assembly without hsdis-amd64.so. To solve this under Ubuntu 16.04:

```
sudo apt-get install libhsdis0-fcml
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH=:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/amd64
java -jar ./target/benchmarks.jar
```
