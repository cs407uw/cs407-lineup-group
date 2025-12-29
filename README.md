# LineUp - CS407 Group Project

## Intro
LineUp is a mobile application that provides real-time, location-based wait information such that users can see current wait times at nearby establishments. This helps customers decide when and where to go, which can improve customer flow and  reduce the amount of business lost to long lines and wait times.

Our natural users are primarily students, young professionals, and families who dine or go out frequently. For the purposes of our project and limited access to all natural users, we will focus primarily on individuals on the UW-Madison campus.

## Tech Stack
This app was implemented with Kotlin and Jetpack Compose, along with Firebase, SharedPreferences, Google Places API, FireStore Remote Config, and Gemini API. Our app has a clean, modern interface with intuitive navigation, a clear visual hierarchy, and smooth interactions from map to list. See “Design: Key Features” for a list of features we’ve implemented. In more detail, our restaurant list is easily scrollable, and the map is easily scrollable, with the restaurants list updating based on where you scroll (in addition to your chosen filters). Note specifically that our Android home-screen widget and the wait time AI Workflow are our out-of-class features. 

## Key Features
- **Dynamic Map Integration** - uses GPS and GooglePlaces API to provide list of nearby venues
- **AI Workflow** - builds call to Gemini API with uploaded image, returns JSON output with analyzed data of line
- **Profile Settings** - set home and favorite locations, filter and sort list of nearby venues
- **Details** - Users can see ratings, wait times, images, and open status on restaurant details screen, and can then capture the wait time of the app by taking picture of line
- **Home Screen Widget** - Our app also integrates with a widgets, displaying most recently  viewed wait time

## [Demo Video](https://drive.google.com/file/d/1qFRIvONKlPrpitOZGq1O2gL_XPBXD98H/view)
