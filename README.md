# Luneth

> This is a java library that makes storing Key/Value objects with serialization simple and reliable.

### Supported Storage Methods
- [x] Redis (In-Memory)
- [x] Caffeine (In-Memory)
- [x] SQL (Supports most drivers)
- [ ] MondoDB (Coming Soon)
- [ ] Scylla (Coming Soon)

### Installation
<details>
<summary>Gradle (Kotlin)</summary>

```kts
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.Summiner:Luneth:1.0.0")
}
```
</details>

<details>
<summary>Gradle (Groovy)</summary>

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Summiner:Luneth:1.0.0'
}
```
</details>

<details>
<summary>Maven</summary>

```xml
<repository>
  <id>jitpack.io</id>
  <url>https://jitpack.io</url>
</repository>

<dependency>
  <groupId>com.github.Summiner</groupId>
  <artifactId>Luneth</artifactId>
  <version>1.0.0</version>
</dependency>
```
</details>

### Usage

<details>
<summary>Java Example</summary>

```java
LunethManager lunethManager = new LunethManager.Builder()
        .setStorageMode(LunethManager.StorageModes.SQL)
        .setConnectionURL("jdbc:h2:D:/Code/Luneth/build/storage")
        .build();
TestStorageObject object = new TestStorageObject(69, "Hello");
System.out.println(lunethManager.setObject(object).join());
System.out.println(object.decodeKey(object.encodeKey())+" | "+ lunethManager.getObject(object).join());
```
</details>