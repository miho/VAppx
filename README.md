# VAppx

[ ![Download](https://api.bintray.com/packages/miho/Appx/VAppx/images/download.svg) ](https://bintray.com/miho/Appx/VAppx/_latestVersion)

Appx utility library to generate Appx installation packages for the Windows 10 App Store.

## Basic Usage

To use this library add the binary [appx-dist](https://github.com/miho/appx-dist) dependency: [ ![Download](https://api.bintray.com/packages/miho/Appx/appx-dist/images/download.svg) ](https://bintray.com/miho/Appx/appx-dist/_latestVersion)

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
