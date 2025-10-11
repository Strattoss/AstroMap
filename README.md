# AstroMap (placeholder name)

## Goal

The goal of the project is to create an Android application inspired by 
[Stellarium](https://play.google.com/store/apps/details?id=com.noctuasoftware.stellarium_free&hl=en-US&pli=1) 
and [Sky Map](https://play.google.com/store/search?q=sky%20map&c=apps&hl=en-US). 
It should allow users to see the night sky in real time through an interactive 3D view.

## Features

* Displays a real-time star map based on the user’s location and current time.
* Automatically detects geographical coordinates and time, with an option to enter them manually.
* (optional) Lets the user point the phone toward the sky to see which stars and constellations are currently visible in that direction.

## Technologies

* Platform: Android
* Language: Kotlin
* Graphics: OpenGL (3D scene rendering)
* IDE: Android Studio

## Used data

Astronomical data comes from [D3 Celestial](https://github.com/ofrohn/d3-celestial).
Astronomical [GeoJSONs here](https://github.com/ofrohn/d3-celestial/blob/master/data/readme.md)

For now the `stars.6.json` and `constellations.lines.json` are sufficient, 
but there is much more interesting data to explore.