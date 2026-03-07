# URLDisplay <img src="./CS22URLDisplay.png" width="10%"/> 


web wrapper designed for CAT/Unifone S22 Flip's outer display, no root needed. 

<img src="./screenshots/externalscreen.gif" width="50%"/>
<img src="./screenshots/screenshot.jpg" width="50%"/>

## Installation
build using [Android Studio](https://developer.android.com/studio) API 30 (android 11+) or try [experimental debug apk](https://github.com/LitCastVlog/URLDisplay/releases/tag/v1.0) in releases (will prompt for KIOSK URL on first launch, clear app data to reset url)

- use volume key to initiate external screen video (same as VLC, haven't figured out complete autoplay yet)
- external display works while app is in background
- dpad content navigation (while screen opened)
- 
### enter desired URL, save
<img src="./screenshots/urlprompt.jpg" width="50%"/>

a static/animated page like [NetByMatt's WeatherStar4000/3000 web ports](https://github.com/netbymatt/ws4kp) works best (uses basic android webview)

## code
###### build using [Android Studio](https://developer.android.com/studio) API 30 (android 11+) 

- [MainActivity.kt](/app/src/main/java/com/litcast/URLDisplay/MainActivity.kt) is the main config 
- [UrlPresentation.kt](app/src/main/java/com/litcast/URLDisplay/UrlPresentation.kt) is the outer screen config
 
  you can replace the prompt with direct urls there if preferred
