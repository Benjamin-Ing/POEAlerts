# POE Alerts

Add alerts when clicking items that match rules contained in `alerts.json`.

Demo: https://www.youtube.com/watch?v=E_o9zcrb-K0

# Installation

Install a JDK.\
Tested on: https://adoptopenjdk.net/ \
JDK 14 with OpenJ9 VM.

Download a release from: https://github.com/Benjamin-Ing/POEAlerts/releases \
Unzip.

# Run

Double-click POEAlerts.jar\
Close app from System Tray icon.

# alerts.json - simple config

For simple configuration you can use default `alerts.json` file. Just change value of field `enabled` to either `true` or `false` to match your current build.

For example: physical gladiator build should enable rules:

-   RIP
-   Dangerous to Most Builds
-   Dangerous to Physical Builds
-   Dangerous to Max Block Builds

While also disabling rules:

-   Dangerous to Elemental Builds
-   Dangerous to Max Dodge Builds

# alerts.json - advanced config

You can define as many rules as you want. Those rules are executed from top to bottom, first one that matches will trigger sound effect, remaining will be ignored. This program can also be used to trigger sounds for alerting you before you vendor good identified item.

## Parameters:

| Param    | Value                   | Description                                             |
| -------- | ----------------------- | ------------------------------------------------------- |
| comment  | text                    | Unused by the program. Use it as your notepad.          |
| enabled  | true or false           | You can disable rule without deleting it from the file. |
| matchAll | regular expression list | All rules in this list must be true for alert to trigger. In the example configuration it is used to detect if selected item is a map `"^Map Tier: \\d+$"` or to detect if selected item is a red tier map `"^Map Tier: (10|11|12|13|14|15)$"` . |
| matchAny | regular expression list | Any rule in this list must be true for the alert to trigger. This list can also be empty to ignore this condition. |
| sound | folder/example.wav | A relative path to audio file. If this variable is missing than default beep sound will be played. This file must be a `.wav` file. |
