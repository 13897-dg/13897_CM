# Overview

**Application Purpose**
The purpose of the MIP2 Image App is to connect to a remote public API to retrieve images and display them to the user. It is built as an educational exercise for the "Assisted code generation" assignment, demonstrating planning-first AI-assisted development. 

**Target Users**
- Students evaluating the process of AI-assisted code generation.
- Users who want to view a feed of randomly curated beautiful photos.

**System Operation Idea**
The application launches, makes an asynchronous HTTP GET request to the Picsum Photos API to fetch an array of JSON objects containing image metadata. The app then parses this JSON and feeds it into an Android `RecyclerView` via an `Adapter`, using an image loading library to asynchronously fetch and render the actual images onto the screen. User interactions (like swipe-to-refresh) will trigger fresh API calls.
