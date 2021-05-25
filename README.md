# RTSP-Server

- Modified to stream camera immediately after open app, without pressing any button or monitor
- Work well on embedded android devices
- Access camera via mpv:
```bash
sudo add-apt-repository ppa:mc3man/mpv-tests
sudo apt update && sudo apt install mpv

mpv rtsp:\\your.android.ip.address:1935
```

## Original repo
https://github.com/pedroSG94/RTSP-Server

[![Release](https://jitpack.io/v/pedroSG94/RTSP-Server.svg)](https://jitpack.io/#pedroSG94/RTSP-Server)

Plugin of rtmp-rtsp-stream-client-java to stream directly to RTSP player.

### Compile

To use this library in your project with gradle add this to your build.gradle:

```gradle
allprojects {
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
dependencies {
  implementation 'com.github.pedroSG94:RTSP-Server:1.0.8'
  implementation 'com.github.pedroSG94.rtmp-rtsp-stream-client-java:rtplibrary:2.0.2'
}

```
