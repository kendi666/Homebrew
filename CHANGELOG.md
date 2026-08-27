# Changelog

All notable changes to BrewMaster are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.0] - 2026-08-26

### Added
- **Grind Convert** — convert a dial from any listed grinder to the others via
  one estimated-particle micron bridge (Hayati / FM 120).
- Calibrated: **FM 120**, **1Zpresso K-Ultra**, **Guerrero GR64 / 64 Pro**,
  **Timemore Sculptor** (`M = 75×FM + 50`; Sculptor `= 0.5×FM + 2`;
  K-Ultra `= M/100 − 0.5`; Guerrero `= (M − 150)/11`).
- Fitted onto the same µm: **Timemore C3**, **C5 / C5 Pro**, **1Zpresso JX-S / JX**.
- Dashboard quick action **Grind Convert**.

## [1.2.0] - 2026-07-15

### Added
- **Smart brew suggestions** — enter your filter dial / clicks plus process and
  target profile; the app ranks the best-matching techniques with a temperature
  window and a highlighted **BEST** pick.
- **Filter dial ruler** — birthday/height-picker style wheel limited to the pour-over
  range (e.g. K-Ultra / K-Pro **6.5–10.0**; finer espresso/moka settings are hidden).
- **Bypass technique** — brew a concentrate (~1:11) then dilute with hot water in
  the server for a strong but smoother cup.
- **Neo Kasuya 10** and **Matt Winton 5 Pour** technique engines on the dashboard.
- **Extra-Coarse grind size** for pulse / Kasuya-style recipes.
- **Post-brew coach** — after the timer finishes, pick a taste symptom (or use
  brew-time hints) and get one primary fix at a time (grind first).
- **Sticky brew CTA** — persistent bottom bar with a one-line summary
  (dose · ratio · temp · dial · ICE) plus **SAVE** / **START BREWING**.
- **Timer prev / next** — skip forward or restart / go back a step if you miss a
  pour, plus keep pause and reset.
- Recipes can store **grinder dial** and **manual water temp** (min/max), shown on
  save dialog, recipe cards, and share links.

### Changed
- **Ice default** uses ~40% of total brew water (Japanese iced / flash-chill style)
  when ice weight is left blank.
- **Process presets** refreshed (Honey, Anaerobic, Wet Hulled, Dark/Robusta-style
  temps and grinds); selecting a bean applies process grind + ratio.
- Grinder catalog expanded with clearer K-Ultra vs K-Pro/K-Max/K-Plus click maps
  and filter dial ranges.
- Database upgraded to **v7** (recipe `grinder_setting`, `temp_min`, `temp_max`).
- Build toolchain bumped to **AGP 8.13** / **Gradle 8.13**.

## [1.1.0] - 2026-06-05

### Added
- **Coffee process "Infused"** — for co-fermented / infused lots. Brews cooler
  (≈87–90 °C) with a medium grind, slightly higher ratio (1:16.5) and longer rest
  (25 days) to preserve the intense added aromatics.
- **v60 "Hybrid Immersion" technique** (Hario Switch / Clever style) with a
  dedicated engine: bloom and steep with the valve closed (immersion), then open
  the valve to percolate the remaining water for a clean finish. Adds two new
  brew step actions: *Immerse* and *Release*.
- **Roast-level temperature** — the selected bean's roast level now shifts the
  brew temperature: lighter roasts brew hotter, darker roasts cooler.
- **Grinder profiles** — pick your grinder (Comandante C40, 1Zpresso JX-Pro,
  Timemore C3, Baratza Encore, Kingrinder K6) on the dashboard to get an
  approximate click/step setting for the recommended grind size.
- **Troubleshooting wizard** — a taste-based guide: choose a symptom (too sour,
  bitter, weak, astringent, flat, uneven, slow/fast drawdown…) and get a diagnosis
  plus ordered, one-variable-at-a-time fixes.
- **Brew Journal** — log brews manually (bean, technique, grind, dose, ratio,
  iced, 1–5 star rating and tasting notes), with a brews-count / average-rating
  summary. Backed by a new local `brew_logs` table.
- **Share recipes** — send any saved recipe as a `brewmaster://` deep link through
  the Android share sheet; opening a link pre-fills it on the dashboard for saving.

### Changed
- **My Recipes** cards now show friendly technique and process names instead of
  raw ids (e.g. "Hybrid Immersion" and "Infused" rather than `hybrid_immersion`
  and `Process #8`).
- Database upgraded to **v6** with stepped, non-destructive migrations (4→5→6).
  Existing saved recipes are preserved across the upgrade.

### Fixed
- **My Recipes** — deleting a recipe now asks for confirmation first, so a stray
  swipe can no longer permanently delete a recipe by accident.

## [1.0.0] - 2025-05

### Added
- Initial release: V60 brew calculator with techniques (Hoffmann, Kasuya 4:6,
  Scott Rao, Osmotic Flow, Single Cup, Custom), hot/ice modes, target profiles,
  coffee process presets, foreground brew timer with step alerts, cheat sheet,
  and personal recipe storage.

[1.2.0]: https://github.com/kendi666/Homebrew/releases/tag/v1.2.0
[1.1.0]: https://github.com/kendi666/Homebrew/releases/tag/v1.1.0
[1.0.0]: https://github.com/kendi666/Homebrew/releases/tag/v1.0.0
