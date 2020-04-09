# Baato-navigation Library

The Java library makes it easy to consume the Baato-navigation API into existing native android projects.

### Getting Started

 1.Open up your project's build.gradle file. Add the following code:

```
allprojects{
 repositories {
  maven { url 'https://jitpack.io' }
 }
}
```

2.Open up your application's build.gradle file. Add the following code:
```
android {
 compileOptions {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
    }
 }
```

```
dependencies {
   implementation 'org.kathmandulivinglabs.code.bhawak.baato-navigation:graphhopper-navigation-android:${latest-version}'
   implementation 'org.kathmandulivinglabs.code.bhawak.baato-navigation:graphhopper-navigation-android-ui:${latest-version}'
}
```

## Built With

* [Retrofit](https://github.com/square/retrofit) - Used to handle API requests
* [Maven](https://maven.apache.org/) - Dependency Management