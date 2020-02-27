# Examples

Example parsers using the ParserKt library :heart:

## Dependency config for ParserKt

__Import JitPack.io repository first__:

```groovy
repositories {
  maven { url "https://jitpack.io" }
}
```

ParserKt is a Kotlin-multiplatform library, you can use it in Kotlin JVM/JS project simply:

```groovy
dependencies {
  implementation "com.github.parserkt.parserkt:parserkt-jvm:$pktVersion"
  //^ or parserkt-js for Kotlin/JS

  implemeentation "com.github.parserkt.parserkt:parserkt-ext-jvm:$pktVersion"
  //^ for org.parserkt.pat.ext.*
}
```

### ...for multiplatform projects

Since we are extracting the same part from more platforms, __it's a little more complicated__, for using multiplatform library in multiplatform projects.

```groovy
dependencies {
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-metadata:$ver"
  //^ for Kotlin-multiplatform metadata
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-util-metadata:$ver"
  commonMainImplementation "com.github.parserkt.parserkt:parserkt-ext-metadata:$ver"
  // NOTE: com.github.parserkt.parserkt:parserkt-ext without metadata is also available

  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-jvm:$ver"
  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-util-jvm:$ver"
  jvmMainImplementation "com.github.parserkt.parserkt:parserkt-ext-jvm:$ver"
  //... jsMainImplementation for nearly-the-same package coordinates
}
```

Too much boilerplate code! But you can also add `enableFeaturePreview("GRADLE_METADATA")` to root project's `settings.gradle`, then just add a single dependency:

```kotlin
dependencies {
  commonMainImplementation "com.github.parserkt:parserkt:$pktVersion"
}
```

> NOTE: Gradle metadata is a experimental feature, using even future release of Gradle != `5.6.2` may fail.<br>
If build configuration acts wrong with single-dependency, use old-style with `forEach` instead.

```kotlin
dependencies {
  ["", "-util", "-ext"].forEach {
    commonMainImplementation "com.github.parserkt.parserkt:parserkt$it-metadata:$pktVersion"
    jvmMainImplementation "com.github.parserkt.parserkt:parserkt$it-jvm:$pktVersion"
    jsMainImplementation "com.github.parserkt.parserkt:parserkt$it-js:$pktVersion"
  }
}
```
