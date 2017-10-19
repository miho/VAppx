# VAppx

[ ![Download](https://api.bintray.com/packages/miho/Appx/VAppx/images/download.svg) ](https://bintray.com/miho/Appx/VAppx/_latestVersion)

Appx utility library to generate Appx installation packages

## Basic Usage

Creating an `.appx` package:

```java
        VAppx.createPackage(new File("FolderToPack"),
                new File("MyApp.appx")).print();
```

Creating a signed `.appx` package:

```java
        VAppx.createPackage(new File("FolderToPack"),
                new File("MyApp.appx"),
                new File("certificate.pfx")).print();
```

Calling `appx` directly:

```java
        VAppx.execute(new File("tmp"),"-h").print();
```
