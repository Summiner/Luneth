# Luneth

> This is a java library that makes storing Key/Object data with serialization simple and reliable.

### Supported Storage Methods
- [x] Redis (In-Memory)
- [x] Caffeine (In-Memory)
- [x] SQL (Supports most drivers)
- [ ] MongoDB (Coming Soon)
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
    implementation("com.github.Summiner:Luneth:1.1.0")
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
    implementation 'com.github.Summiner:Luneth:1.1.0'
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
  <version>1.1.0</version>
</dependency>
```
</details>

### Usage

<details>
<summary>Java Example</summary>

```java
public static void main(String[] args) {
    LunethManager manager = new LunethManager.Builder()
            .setStorageMode(LunethManager.StorageModes.CAFFEINE)
            .build();

    UUID uuid = UUID.randomUUID();
    ExampleObject object = new ExampleObject(uuid, "Example");

    System.out.println(manager.put(object).join()); // Write object to Luneth
    System.out.println(manager.get(ExampleObject.class, uuid).join()); // Read object from Luneth
}

@LunethSerializer(identifier = "ExampleObject")
public static class ExampleObject implements StorageObject {
    @LunethField(key = true)
    public final UUID player;

    @LunethField(id = 1)
    public final String username;

    public ExampleObject(UUID player, String username) {
        this.player = player;
        this.username = username;
    }

    public String toString() {
        return "ExampleObject{UUID player="+player+", String username="+username+"}";
    }
}
```
</details>
