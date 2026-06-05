# Changelog

All notable changes to BrewMaster are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

[1.1.0]: https://github.com/kendi666/Homebrew/releases/tag/v1.1.0
[1.0.0]: https://github.com/kendi666/Homebrew/releases/tag/v1.0.0
