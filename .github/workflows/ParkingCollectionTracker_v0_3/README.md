# Parking Collection Tracker v0.3

Android driver prototype for Stuttgart Route 12330.

## Vehicles
- 3980
- 3982

## Driver workflow
Select vehicle → select predefined collection location → choose Forward/Reverse →
choose Left/Right → choose Car/Wall/Empty → tap `MANEUVER COMPLETED`.

The app counts totals and distributions and supports `UNDO LAST MANEUVER`.

## Stored per maneuver
Only:
- vehicle
- predefined location
- scenario

No timestamp, session ID, GPS history or driver identity.

## Network / TISAX-oriented prototype behavior
The manifest deliberately has **no INTERNET permission**. The runtime app therefore
cannot send collection records to OpenAI, Firebase, Analytics or a backoffice server.
There are no analytics, advertising, telemetry or OpenAI SDKs.

This is a technical privacy property of v0.3, not by itself a TISAX certification.

## Reference material
The five predefined locations and their coordinates/requirements are transcribed from
the screenshots supplied for this prototype. The actual customer reference images are
not embedded in v0.3; they should be added from the original approved source files
rather than cropped phone screenshots.

## Build
Open in Android Studio, sync Gradle and choose:
Build > Build App Bundle(s) / APK(s) > Build APK(s).

## v0.3 UI
- Added an original in-app parking instruction graphic (not copied from customer screenshots).
- Added the key collection instructions directly in the driver screen.
- Kept vehicles 3980 / 3982, predefined Stuttgart locations, counters and undo.
- Still no INTERNET permission.
