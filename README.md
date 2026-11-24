# NEXUS V1

Responsive client-side utility mod for Minecraft **1.21.11**, NeoForge **21.11.48**, Java 21.

## Controls
- **Right Shift** opens/closes the ClickGUI.
- Select a category in the left navigation and click feature rows to toggle them.
- Feature states are saved to `config/nexus.properties`.
- The HUD includes optional watermark, FPS, coordinates, direction, speedometer, sprint state, armor count, player status, session timer, custom crosshair, and module list.

## Build on GitHub
Every push to `main`, pull request, or manual workflow dispatch runs `.github/workflows/build.yml` using Java 21 and Gradle 9.2.1. On a successful run, download the JAR from **Actions → workflow run → Artifacts** and put it in your Minecraft instance's `mods` folder.

Local build: `gradle build` with Java 21 and Gradle 9.2.1.

NEXUS is focused on client-side presentation and information. It does not automate gameplay or alter server packets.
