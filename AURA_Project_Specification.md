# AURA — Immersive Adaptive Music Experience App
## Full Project Specification (for Android Studio + AI Code Generation)

> This document is the single source of truth for building AURA. It is written so it can be fed directly to an AI coding assistant (Claude Code, Cursor, etc.) section by section to generate the actual Kotlin/Compose codebase. It consolidates the original concept documents plus every architecture decision made during planning.

---

## 1. Product Summary

AURA is a mobile music streaming app where every song automatically becomes a living audiovisual world — no separate "Experience Mode" needs to be activated. Playing the song *is* the experience.

- **Two layers**: (1) a full conventional music platform (search, playlists, library, artist profiles), and (2) an Immersive Experience Engine that activates only on the Now Playing screen.
- **Signature elements**: a persistent animated cat companion, mood-driven background environments, a music-reactive kaleidoscope, particles, and emotionally resonant lyric highlighting.
- **AI role**: interprets user mood text and song metadata/lyrics into a structured "Atmosphere Profile" that drives all visuals. AI is the *director*, not the *renderer* — it outputs data, the app's own rendering systems draw the frames.
- **Target audience**: global, ages ~15–35, not positioned as a mental-health app or a local-music-only app.

---

## 2. Confirmed Architecture Decisions (from planning discussion)

These decisions override anything more generic implied by the original concept documents:

| Area | Decision |
|---|---|
| Visual engine split | **Rive** handles illustrated/character content (the cat, environment scenery). **Custom Jetpack Compose Canvas** handles all data-driven/audio-reactive visuals (particles, kaleidoscope, waveforms, equalizer bars, reactive gradients). |
| Home screen | Stays neutral/plain, **no environment animation**. Immersion is achieved through typography, spacing, color accents, soft glass-like depth, and micro-interactions — not full-scene animation. Only the mini-player and "Current Song" card get a subtle color tint / breathing glow from the current Atmosphere Profile. |
| Now Playing screen | The only place the full Immersive Experience Engine renders (Rive scenery + Rive cat + Canvas particles + Canvas kaleidoscope + reactive lighting + lyrics). |
| Color identity | "Aura" palette — soft pearl/mist neutral base with a signature muted violet accent, not stark dark-mode, not pure white. Defined fully in Section 6. |
| Local persistence | **Room** — caches song/catalog metadata and user data locally. Not used to store audio files. |
| Audio file hosting | **Cloudflare R2** (S3-compatible, free egress) — not Firebase Storage (bandwidth-limited free tier) and not on-device MediaStore (rejected — creates a permanent two-path architecture split between local and uploaded songs). |
| Song catalog source of truth | Single unified pipeline: all songs (seed content + artist uploads) live in R2 + a server-side catalog database. The app always streams via URL; Room only caches catalog metadata for fast offline browsing. |
| Server-side database | Postgres via **Supabase** (free tier) for the song/artist/user catalog — separate from Room, which is local-only. |
| AI provider | **OpenAI** (user has an API key), called **only from a backend proxy**, never directly from the Android client (key-security requirement). |
| AI's job | (1) Convert free-text mood input into structured atmosphere JSON. (2) Combine that with fetched song genre/lyrics to produce the full Atmosphere Profile. AI does not pick literal songs from nothing — the backend queries the real catalog using AI-derived target values (valence, energy, genre, mood tags). |
| Lyrics | Not embedded in audio files — fetched via a lyrics/metadata lookup step (Genius API or Musixmatch API, free tier) before the OpenAI atmosphere call, so resonant-lyric-highlighting has real text to work with. |
| Demo audio legal approach | Real/recognizable songs are bundled **locally only**, for the live demo device, never uploaded to R2 or committed to the repo. The R2 bucket and submitted project are stocked exclusively with royalty-free/Creative Commons tracks (Free Music Archive, Jamendo, YouTube Audio Library). |
| Audio playback | Media3 (ExoPlayer + MediaSession), streaming URLs from the catalog API. |
| Dev environment | Android Studio, **Empty Activity** template (Compose-based, not the legacy Views template), Kotlin DSL (`build.gradle.kts`), Gradle version catalog (`libs.versions.toml`). |

---

## 3. Tech Stack & Pinned Versions

Pin these exactly to avoid the classic Kotlin/Compose-compiler/AGP mismatch build errors. Verify against the Compose-to-Kotlin compatibility map before bumping any single one in isolation — they move together as a set.

| Component | Version | Notes |
|---|---|---|
| Kotlin | `2.2.20` | Compose Compiler is now a separate Gradle plugin tied 1:1 to the Kotlin version (post Kotlin 2.0 model) |
| Android Gradle Plugin (AGP) | `9.2.0` or newer | Required floor for Compose 1.12 / compileSdk 37 |
| Gradle | Latest matching AGP requirement (check `gradle/wrapper/gradle-wrapper.properties` compatibility table for AGP 9.2) |
| compileSdk / targetSdk | `37` | |
| minSdk | `26` (Android 8.0) | Good reach vs. modern API balance; raise only if a specific media/graphics API requires it |
| Jetpack Compose BOM | `androidx.compose:compose-bom:2026.08.00` | Manages all Compose library versions together — never version individual Compose libraries manually |
| Compose Compiler Gradle Plugin | `org.jetbrains.kotlin.plugin.compose` version-matched to Kotlin `2.2.20` | Replaces the old `kotlinCompilerExtensionVersion` field |
| KSP | Matched to Kotlin `2.2.20` (e.g. `2.2.20-x.x`) | Use KSP, not `kapt`, for Hilt/Room annotation processing — faster builds |
| Hilt | `2.57.x` (latest stable) | Dependency injection |
| Room | `2.7.x` stable line | **Do not** use `Room3`/`androidx.room3` — it is alpha as of this writing; stay on the stable `androidx.room` artifact |
| Media3 (ExoPlayer + MediaSession) | `1.8.x` stable | Playback engine |
| Retrofit | `3.0.0` | REST calls to your backend catalog API |
| OkHttp | Latest stable compatible with Retrofit 3.0.0 | |
| kotlinx.coroutines | `1.10.x` | |
| kotlinx.serialization or Moshi | Either is fine; Moshi shown in examples below | JSON parsing for Atmosphere Profile / catalog responses |
| Navigation Compose | Latest stable matching Compose BOM | |
| Rive Android runtime | `app.rive:rive-android:11.7.0` (check Maven Central for anything newer before starting) | Use the **new Compose API**, not the legacy View-based API — Rive's own docs mark the legacy API as being phased out |
| Coil | Latest v3.x | Image loading (album art, artist images) |
| Lifecycle / ViewModel-Compose | Latest stable matching Compose BOM | |

**Before starting Phase 1**, run a fresh empty Compose project through Android Studio's own upgrade assistant once to confirm no newer patch versions have shipped, since Google ships Compose updates roughly monthly.

---

## 4. Module & Folder Structure

Use a **multi-module** setup so the app scales cleanly and build times stay reasonable as features grow.

```
AURA/
├── app/                                  # Thin app shell — DI graph root, MainActivity, Navigation host
│   ├── src/main/java/com/aura/app/
│   │   ├── AuraApplication.kt            # @HiltAndroidApp entry point
│   │   ├── MainActivity.kt               # Single Activity, hosts Compose NavHost
│   │   └── navigation/
│   │       ├── AuraNavHost.kt            # Top-level NavHost wiring all feature graphs
│   │       └── Destinations.kt           # Sealed class / route constants
│   └── build.gradle.kts
│
├── core/
│   ├── designsystem/                     # The "Aura" visual identity — shared, no feature logic
│   │   ├── theme/
│   │   │   ├── Color.kt                  # Palette from Section 6
│   │   │   ├── Type.kt                   # Typography scale
│   │   │   ├── Shape.kt
│   │   │   └── AuraTheme.kt              # MaterialTheme wrapper
│   │   └── components/
│   │       ├── AuraButton.kt
│   │       ├── GlassCard.kt              # Soft glass/blur card used across Home
│   │       └── MiniPlayerBar.kt
│   │
│   ├── model/                            # Pure Kotlin data classes, no Android deps
│   │   ├── Song.kt
│   │   ├── Artist.kt
│   │   ├── Album.kt
│   │   ├── Playlist.kt
│   │   ├── AtmosphereProfile.kt          # THE central data contract — see Section 7
│   │   ├── CatBehaviorState.kt           # enum: Idle, Sleeping, Walking, Playing, Running, Watching
│   │   └── EnvironmentType.kt            # enum: Heaven, Nature, Ocean, Dream, Romantic, Melancholic, Hopeful, Energetic
│   │
│   ├── data/                             # Repository contracts + implementations
│   │   ├── remote/
│   │   │   ├── CatalogApi.kt             # Retrofit interface — songs/artists/playlists
│   │   │   ├── AtmosphereApi.kt          # Retrofit interface — calls YOUR backend, not OpenAI directly
│   │   │   └── LyricsApi.kt              # Retrofit interface — Genius/Musixmatch lookup
│   │   ├── local/
│   │   │   ├── AuraDatabase.kt           # Room database class
│   │   │   ├── dao/
│   │   │   │   ├── SongDao.kt
│   │   │   │   ├── PlaylistDao.kt
│   │   │   │   └── CatalogCacheDao.kt
│   │   │   └── entity/                   # Room @Entity classes (mirror of core/model, DB-shaped)
│   │   └── repository/
│   │       ├── SongRepository.kt         # Single source of truth — Room cache + remote catalog
│   │       ├── AtmosphereRepository.kt   # Fetches/generates Atmosphere Profiles
│   │       ├── LyricsRepository.kt
│   │       └── PlaylistRepository.kt
│   │
│   ├── audio/
│   │   ├── AuraPlayer.kt                 # Wraps Media3 ExoPlayer + MediaSession
│   │   ├── PlaybackState.kt              # Sealed UI-facing playback state
│   │   └── AudioAnalyzer.kt              # Reads amplitude/beat data during playback (Android Visualizer API) for reactive visuals
│   │
│   └── common/
│       ├── di/                           # Hilt modules (NetworkModule, DatabaseModule, PlayerModule)
│       ├── util/
│       └── Result.kt                     # Generic sealed Result/AppError wrapper (per best-practices doc)
│
├── feature/
│   ├── home/
│   │   ├── HomeScreen.kt                 # Neutral/plain per Section 2 — greeting, mood tiles, recently played, trending
│   │   ├── HomeViewModel.kt
│   │   └── HomeUiState.kt
│   │
│   ├── search/
│   │   ├── SearchScreen.kt
│   │   ├── SearchViewModel.kt
│   │   └── SearchUiState.kt
│   │
│   ├── moodinput/
│   │   ├── MoodInputScreen.kt            # "How are you feeling?" text entry
│   │   ├── MoodInputViewModel.kt         # Calls AtmosphereRepository → backend → OpenAI
│   │   └── MoodInputUiState.kt
│   │
│   ├── nowplaying/                       # THE core immersive screen
│   │   ├── NowPlayingScreen.kt           # Composes all visual layers together
│   │   ├── NowPlayingViewModel.kt        # Combines PlaybackState + AtmosphereProfile + audio-analysis flow
│   │   ├── NowPlayingUiState.kt
│   │   └── visuals/
│   │       ├── EnvironmentBackground.kt  # Rive scenery layer, with flat-color placeholder fallback (Section 8)
│   │       ├── CatCompanion.kt           # Rive cat layer, state machine input wiring
│   │       ├── ParticleLayer.kt          # Compose Canvas — particles driven by AudioAnalyzer
│   │       ├── KaleidoscopeLayer.kt      # Compose Canvas — math-driven, BPM/beat/amplitude driven
│   │       ├── ReactiveGradientLayer.kt  # Compose Canvas — color shifts from AtmosphereProfile
│   │       └── LyricsOverlay.kt          # Synced lyrics + resonant-line highlighting
│   │
│   ├── library/
│   │   ├── LibraryScreen.kt
│   │   └── LibraryViewModel.kt
│   │
│   ├── playlist/
│   │   ├── PlaylistDetailScreen.kt
│   │   ├── PlaylistEditorScreen.kt
│   │   └── PlaylistViewModel.kt
│   │
│   ├── artist/
│   │   ├── ArtistProfileScreen.kt
│   │   ├── ArtistUploadScreen.kt         # Phase 2 — upload flow to backend → R2
│   │   └── ArtistViewModel.kt
│   │
│   └── settings/
│       ├── SettingsScreen.kt             # Reduced motion, visual intensity tiers, cat visibility toggle
│       └── SettingsViewModel.kt
│
├── gradle/
│   └── libs.versions.toml                # Single version catalog — see Section 3 for pinned values
│
├── settings.gradle.kts                   # Declares all modules above
└── build.gradle.kts                      # Root — plugin aliases only, no dependencies
```

**Why this shape**: `core/model` and `core/designsystem` have zero dependencies on any `feature/*` module, so features never accidentally depend on each other. `core/data`, `core/audio` sit beneath every feature. This mirrors the layering already specified in your Kotlin best-practices document (UI → ViewModel → Domain/Repository → Data), just expressed as real Gradle modules instead of just packages, which keeps build times sane as the project grows.

---

## 5. Screen-by-Screen Responsibility

| Screen | Immersive engine active? | Key responsibility |
|---|---|---|
| Home | No | Greeting, search entry, mood tiles (each tinted with its environment's accent color, static), recently played, trending, made-for-you. Mini-player pinned at bottom. |
| Search | No | Song/artist/album/playlist search + natural-language mood search box. |
| Mood Input | No (transitional) | Free-text mood entry → AtmosphereRepository call → routes to Search results or directly starts playback. |
| **Now Playing** | **Yes — full engine** | All 6 visual layers (Section 4) composed together, controls that fade on inactivity, synced lyrics, transition animation on song change. |
| Library | No | Liked Songs, Playlists, Albums, Artists, Downloads, History, "My Music Worlds." |
| Playlist Detail/Editor | No | Standard list UI; playlist card can carry a soft aggregate-mood tint. |
| Artist Profile | No | Bio, followers, popular songs, upload entry point for verified artists. |
| Settings | No | Visual intensity tier (Low/Medium/High), reduced motion, cat visibility, account/playback settings. |

---

## 6. "Aura" Color System

Confirmed direction: soft, warm, premium neutral — not stark dark mode, not clinical white. Sits between all environment mood colors without competing with any of them.

### Light (default) theme
| Role | Hex |
|---|---|
| Background | `#F4F1ED` (soft pearl mist) |
| Surface / Card | `#E9E4DE` (warm fog grey) |
| Primary text | `#211E1B` (deep warm charcoal, not pure black) |
| Secondary text | `#7A736B` (muted taupe) |
| **Signature accent — "Aura Violet"** | `#A79AC7` |

### Dark theme (same identity, inverted base)
| Role | Hex |
|---|---|
| Background | `#1C1A22` (warm near-black, violet undertone) |
| Surface / Card | `#26232D` |
| Primary text | `#F5F5F7` |
| Secondary text | `#A7AAB5` |
| **Signature accent — "Aura Violet"** | `#A79AC7` (unchanged — carries identity across themes) |

### Per-environment atmosphere palettes (used only within Now Playing / mood tiles, never as app-wide theme colors)
| Environment | Palette |
|---|---|
| Heaven | Pearl, Ivory, Lavender, Misty Blue, soft Gold |
| Nature/Calm | Sage, Forest Green, Teal, soft Blue, warm Beige |
| Oceanic | Aqua, Turquoise, Cyan, Deep Blue |
| Dreamy | Lavender, Periwinkle, Pale Blue, soft Pink |
| Romantic | Rose, Blush, Mauve, Pink, soft Violet |
| Melancholic | Indigo, Slate, Muted Violet, Deep Blue |
| Hopeful | Sky Blue → Lavender → Pink → Peach → Orange → Gold (progression) |
| Energetic | Orange, Red, Coral, Magenta, Pink, Violet |

---

## 7. Central Data Contract: `AtmosphereProfile`

Every visual layer reads from one object. This is the single most important type in the app — generate it first.

```kotlin
data class AtmosphereProfile(
    val songId: String,
    val emotion: String,                  // e.g. "melancholic-comforting"
    val energy: Float,                    // 0f–1f, drives kaleidoscope speed, particle density, cat expressiveness
    val valence: Float,                   // 0f–1f, negative-to-positive mood
    val environment: EnvironmentType,     // which Rive scenery file to load
    val primaryColor: String,             // hex
    val secondaryColors: List<String>,    // hex list, for gradients
    val lightingStyle: LightingStyle,     // Soft, Bright, Dramatic, Fading
    val particleStyle: ParticleStyle,     // Dust, Rain, Bubbles, Petals, Stars, Sparks
    val kaleidoscopeStyle: KaleidoscopeStyle, // symmetry count, shape style, base speed
    val catBehavior: CatBehaviorState,    // current default behavior for this song
    val transitionStyle: TransitionStyle, // how to morph from previous song's profile
    val sectionProfiles: List<SongSectionProfile> = emptyList() // optional intro/verse/chorus/bridge/outro overrides
)

data class SongSectionProfile(
    val section: SongSection,             // Intro, Verse, PreChorus, Chorus, Bridge, FinalChorus, Outro
    val startTimeMs: Long,
    val intensityMultiplier: Float        // scales energy/particles/kaleidoscope for this section
)
```

This is produced by `AtmosphereRepository`, which calls your backend, which calls OpenAI + the lyrics lookup, per the flow in Section 9.

---

## 8. Rive Integration Plan

### Assets needed (9 total `.riv` files)
| File | Contains |
|---|---|
| `cat_companion.riv` | Single rigged cat, state machine inputs: `energy` (float 0–1), `behaviorState` (enum trigger: Idle/Sleeping/Walking/Playing/Running/Watching) |
| `env_heaven.riv` | Sky, cloud layers, light rays — inputs: `energy`, `intensity` |
| `env_nature.riv` | Tree, meadow, mountain silhouette — inputs: `energy`, `intensity` |
| `env_ocean.riv` | Water/wave shape, horizon, coast — inputs: `energy`, `intensity` |
| `env_dream.riv` | Floating islands, star field silhouette — inputs: `energy`, `intensity` |
| `env_romantic.riv` | Garden/flower field, sunset horizon — inputs: `energy`, `intensity` |
| `env_melancholic.riv` | Window frame, rain streaks, city skyline — inputs: `energy`, `intensity` |
| `env_hopeful.riv` | Horizon that brightens — extra input: `progress` (0–1, tied to song playback position) |
| `env_energetic.riv` | Abstract geometric/light-tunnel shapes — inputs: `energy`, `intensity` |

All environment files share the same input naming convention (`energy`, `intensity`) so `EnvironmentBackground.kt` can drive any of them identically regardless of which one is loaded.

### Code integration pattern (placeholder-first, so app code isn't blocked on art)

```kotlin
@Composable
fun EnvironmentBackground(
    profile: AtmosphereProfile,
    audioEnergy: Float, // live value from AudioAnalyzer, updated every frame/tick
    modifier: Modifier = Modifier
) {
    val riveAssetPath = profile.environment.riveAssetPathOrNull()

    if (riveAssetPath != null) {
        RiveEnvironmentView(
            assetPath = riveAssetPath,
            energyInput = audioEnergy,
            intensityInput = profile.energy,
            modifier = modifier.fillMaxSize()
        )
    } else {
        // Placeholder used until the real .riv file for this environment exists
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color(android.graphics.Color.parseColor(profile.primaryColor)))
        )
    }
}
```

Build every screen against this placeholder path first. Swap in real assets one environment at a time once each `.riv` file is ready — this should never require touching `NowPlayingScreen.kt` itself, only the mapping in `EnvironmentType.riveAssetPathOrNull()`.

### Cat integration
Same pattern — `CatCompanion.kt` wraps the Rive Compose view, exposes a function to push `behaviorState` transitions and a continuous `energy` value sourced from `AudioAnalyzer`.

---

## 9. AI / Backend Flow

**Never call OpenAI, Genius/Musixmatch, or R2 directly from the Android app.** All three go through a thin backend you control (serverless function — Cloudflare Workers or Firebase Functions are both fine — or a small Ktor server). The Android app only ever talks to your backend's own REST API (`CatalogApi`, `AtmosphereApi`, `LyricsApi` in Section 4).

```
User types mood text ("I feel lonely tonight")
        │
        ▼
Android app → POST /atmosphere/mood  (your backend)
        │
        ▼
Backend → OpenAI Chat Completions
        │   Prompt instructs OpenAI to return ONLY structured JSON:
        │   { mood, targetValence, targetEnergy, suggestedGenres[], atmosphereHint }
        ▼
Backend queries your own song catalog (Supabase/Postgres)
using targetValence/targetEnergy/genre as filters
        │
        ▼
Backend picks a candidate song → fetches its lyrics
(Genius/Musixmatch API, cached after first fetch)
        │
        ▼
Backend → OpenAI again: combine (user mood + song lyrics + genre)
→ returns full AtmosphereProfile JSON (Section 7 shape)
        │
        ▼
Backend returns { song, atmosphereProfile } to the Android app
        │
        ▼
NowPlayingViewModel receives it → all 6 visual layers render from atmosphereProfile
```

Cache lyrics and Atmosphere Profiles per song server-side (Supabase table) after the first generation — no need to re-call OpenAI every time the same song plays again.

---

## 10. Song & Artist Upload Pipeline

Single pipeline for both seed content and real artist uploads — no separate code paths.

1. Artist selects a file in `ArtistUploadScreen` → app sends it to backend (never directly to R2 from the client)
2. Backend uploads the audio file to the Cloudflare R2 bucket, returns a streamable URL
3. Backend writes metadata (title, artist, genre, uploader ID, R2 URL, optional artist-provided visual metadata per Section 38 of the original concept) into the Supabase catalog table
4. All apps query `CatalogApi` for the song list; Room caches the catalog response locally for fast browsing; playback always streams the R2 URL via Media3

For prototype seeding: manually upload 5–10 royalty-free tracks into R2 and insert their metadata directly into Supabase — don't wait on the upload UI being finished to have testable content.

---

## 11. Accessibility & Performance Settings

Implement early — these directly control which visual layers render, and it's easier to build the layers with these toggles in mind from the start than retrofit them later.

- **Reduced motion**: dampens/disables particle motion, kaleidoscope rotation speed, camera/parallax movement, cat animation speed, transition length. The atmosphere (colors, static scenery) stays visible — only motion is reduced.
- **Visual intensity tiers** (`Low` / `Medium` / `High`): controls particle count, kaleidoscope complexity, whether the Rive scenery layer renders at all (Low = flat gradient only), lighting layer complexity.
- **Cat visibility toggle**: on by default per the original spec.
- Standard: adjustable text size, high contrast option, screen-reader labels on all interactive controls.

---

## 12. Build Order (Phased)

**Phase 1 — Skeleton**
1. Create project: Android Studio → Empty Activity → Kotlin → Compose → Kotlin DSL
2. Set up all Gradle modules from Section 4 + `libs.versions.toml` with pinned versions from Section 3
3. Implement `AuraTheme` with the palette from Section 6 (light + dark)
4. Build navigation shell across all screens with placeholder/empty content
5. Get Media3 playback working end-to-end with one hardcoded local test file

**Phase 2 — Data + AI**
6. Implement `AtmosphereProfile` and all `core/model` types
7. Stand up the backend proxy (Cloudflare Workers/Firebase Functions/Ktor) with the OpenAI + lyrics-lookup flow from Section 9
8. Wire `MoodInputScreen` → `AtmosphereRepository` → backend, end-to-end, rendering results as flat colored placeholders only (no Rive/Canvas yet) — validates the data pipeline in isolation

**Phase 3 — Visual Engine**
9. Build `ParticleLayer`, `KaleidoscopeLayer`, `ReactiveGradientLayer` in Compose Canvas, driven by `AudioAnalyzer` + `AtmosphereProfile`, tested against placeholder backgrounds
10. Integrate `cat_companion.riv` once ready, wire its state machine inputs
11. Integrate environment `.riv` files one at a time, swapping out placeholders per Section 8's pattern
12. Build `LyricsOverlay` with resonant-line highlighting once `LyricsRepository` is live

**Phase 4 — Platform completeness**
13. Library, Playlists, Artist Profiles, Settings screens
14. Artist upload flow → R2 pipeline (Section 10)
15. Song-to-song transition animation (Section on transitions in the original concept doc — 2–5s morph between environments, cat "surviving" the transition)

**Phase 5 — Polish**
16. Reduced motion + performance tiers (Section 11) applied across every visual layer
17. Mini-player color tinting, Home screen micro-interactions (breathing glow on Current Song card, tap scale/glow on mood tiles)
18. Performance pass on real hardware (not just emulator) — profile GPU/battery usage with High tier active

---

## 13. Best-Practice Rules to Enforce Throughout (from the team's Kotlin standards)

- Strict MVVM: Composables render state and forward events only, no business logic in the UI layer.
- Expose immutable `StateFlow<UiState>` from every ViewModel; model UI state as a sealed interface (`Loading`/`Success`/`Error`/etc.), never as multiple independent booleans.
- Inject dispatchers (`AppDispatchers`), never hardcode `Dispatchers.IO` deep in reusable code.
- Repositories are the single source of truth; ViewModels never call Retrofit/Room directly.
- Never hardcode API keys or R2/Supabase credentials in the Android app — all secrets live only in the backend.
- Enforce HTTPS everywhere; no cleartext traffic exceptions.
- Set `android:exported="false"` on all components unless explicitly required otherwise.
- Use Hilt for all dependency injection — no manual singleton construction scattered through the app.
- Unit test ViewModels, repositories, and the Atmosphere Profile mapping logic; UI-test the critical flows (search → play → Now Playing renders correctly, mood input → recommendation).
- CI baseline: `assembleDebug → lint → detekt/ktlint → test → connectedAndroidTest`.
- R8/shrinking + mapping file upload for any release build, even an internal demo build.

---

## 14. How to Use This Document With an AI Coding Assistant

Feed this document in this order for best results:
1. Sections 1–4 first → have the assistant scaffold the Gradle modules, version catalog, and empty package structure exactly as laid out.
2. Section 6–7 next → generate `AuraTheme` and the `AtmosphereProfile`/model layer.
3. Section 12 (Phase 1 items) → generate navigation shell + Media3 playback skeleton.
4. Proceed phase by phase through Section 12, pasting in the corresponding sub-sections (8, 9, 10, 11) as each phase is reached, rather than asking for the whole app in one shot — this keeps generated code consistent with what's already been built and avoids the AI inventing conflicting structure.
