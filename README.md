# NEXUS V1

Client-side utility and visual enhancement mod for **Minecraft 1.21.11**, **NeoForge 21.11.48**, and **Java 21**.

## Interface
- Press **Right Shift** to open the ClickGUI.
- Use the category navigation to toggle modules.
- **Settings** provides five accent colors, HUD scale (75–150%), HUD background, and HUD editor access.
- **Profiles** supports saving, loading, and deleting named profiles. Profile names accept 1–24 letters, numbers, underscores, or hyphens.
- The HUD editor lets you drag the preview to position the live HUD. Right-click the preview to reset its position.

## Modules
- **Combat information:** CPS display (local left-click rate) and crosshair target name/distance.
- **HUD:** watermark, FPS, coordinates, direction, and module list.
- **Movement:** speedometer, sprint status, and grounded/airborne/gliding state.
- **Player:** armor slots, armor durability, health, and food.
- **Render:** custom crosshair.
- **Misc:** session timer and biome name.

All modules are client-side information or presentation. No attack automation, player targeting automation, reach changes, packet manipulation, or server-side behavior is included.

## GitHub Actions build
Every push to `main`, pull request, or manual workflow dispatch runs the cloud build with Java 21 and Gradle 9.2.1. On success, open **Actions → Build NEXUS V1 → workflow run → Artifacts** and download the JAR. Put the JAR in your Minecraft instance's `mods` folder.

Local build: `gradle build` with Java 21 and Gradle 9.2.1.

## Configuration
Settings are stored under `config/nexus/`; named profiles are stored in `config/nexus/profiles/`.
