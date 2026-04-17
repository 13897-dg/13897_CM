# Feature Extensions

This file tracks new functionality requested post-plan, as per assignment rules.

*(Currently empty. Future items to investigate and implement:)*
- Loading indicator (progress bar vs swipe refresh indicator relative to images loaded)
- Implement Image Details Screen
- Support Favorite Items (FIFO queue of max 5)
- Cache up to 50 items (offline access) with +/- 10 threshold.
- Offline access caching.
- Graceful API error handling.

## Feature 1: Linear Loading Indicator

**Description:**
Replace the standard circular ProgressBar with a polished Material `LinearProgressIndicator` to serve as a sleek horizontal loading bar.

**Implementation Tasks:**
1. Update `activity_main.xml` to replace the `<ProgressBar>` with `<com.google.android.material.progressindicator.LinearProgressIndicator>`.
2. Constrain it to the very bottom of the screen.
3. Update `MainActivity.kt` binding properties to point to the new ID `loadingIndicator` and ensure it reflects network fetch operations reliably.

**Expected UI Changes:**
A horizontal blue Material loader will scroll smoothly along the bottom boundary of the screen whenever the system retrieves images from the API.

**Implementation Plan:**
1. Write XML definition inside Main Layout.
2. Update Kotlin code bindings.
3. Test using AntiGravity IDE.
