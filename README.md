# AURA — Android Project (Phase 1 Skeleton)

This is the **Phase 1** skeleton
described in that document's Section 12: Gradle module structure, the "Aura"
theme, the central `AtmosphereProfile` data model, Room + Retrofit + Hilt wiring,
and every screen's navigation shell — all compiling against **placeholder**
visuals (flat colors instead of real `.riv` files) so the rest of the app isn't
blocked on art being finished.

## How to open this project

1. Open Android Studio (latest stable — this project targets compileSdk 37 /
   AGP 9.2.0, so use an Android Studio version that supports those; update
   Android Studio itself first if it complains).
2. **File → Open** and select this `AURA/` folder (the one containing
   `settings.gradle.kts`).
3. Let Gradle sync. First sync will take a while — it's pulling ~14 modules'
   worth of dependencies.
4. Before building, edit `core/data/src/main/java/com/aura/core/data/di/NetworkModule.kt`
   and replace `BASE_URL` with your actual backend URL (see
   `AURA_Project_Specification.md` Section 9 — you need a small backend proxy
   deployed before any network call in the app will succeed; Retrofit calls
   will fail until then, which is expected at this stage).
5. Run on a **physical device** where possible — the immersive visual layers
   (once Rive assets are added) render much more representatively on real
   hardware than on the emulator.

## What's real vs. placeholder in this skeleton

| Piece | Status |
|---|---|
| Module structure, Gradle config, version catalog | Real, matches Section 3/4 of the spec |
| `AuraTheme` (Section 6 palette) | Real |
| `AtmosphereProfile` + all core models | Real |
| Room database, DAOs, entities | Real |
| Retrofit API interfaces + Hilt network/database modules | Real (needs your real `BASE_URL`) |
| Repositories | Real |
| `AuraPlayer` (Media3) | Real playback wiring; `PlaybackState` mapping from ExoPlayer's internal state still has a `TODO` |
| `AudioAnalyzer` | **Stub** — `TODO Phase 3`, needs `android.media.audiofx.Visualizer` wired to `AuraPlayer.audioSessionId()` |
| `EnvironmentBackground`, `CatCompanion` | **Placeholder** — flat color / plain circle. Swap in real `.riv` files per the pattern documented inline once assets exist (Section 8) |
| `ParticleLayer`, `KaleidoscopeLayer`, `ReactiveGradientLayer` | Real, functioning Compose Canvas implementations |
| `LyricsOverlay` | Real, basic version (no environment-specific styling yet) |
| Home / Search / MoodInput / Library / Playlist / Artist / Settings screens | Minimal placeholder UI, wired to real ViewModels/repositories where applicable |
| Artist upload → R2 pipeline | **Not implemented** — Phase 4 per the spec |

## Next steps (see `AURA_Project_Specification.md` Section 12)

- **Phase 2**: stand up the backend (Cloudflare Workers/Firebase Functions/Ktor)
  implementing `/atmosphere/mood`, `/atmosphere/song/{id}`, `/lyrics/{id}`,
  and `/catalog/*` to match the Retrofit interfaces in `core/data/remote/`.
- **Phase 3**: implement `AudioAnalyzer`, then start swapping real `.riv`
  files into `EnvironmentBackground` / `CatCompanion`.
- Seed your Cloudflare R2 bucket + Supabase catalog with 5–10 royalty-free
  tracks so `CatalogApi` calls return real data during development.

This project was not compiled in the generation environment (no Android SDK
available there) — run a Gradle sync in Android Studio as the first
verification step, and check the version catalog in `gradle/libs.versions.toml`
against Maven Central for anything newer before your first real build, since
Compose/AGP/Hilt/Room all ship monthly-ish updates.
