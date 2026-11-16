# Changelog

All notable changes to BrewMaster are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Edit a saved recipe** — My Recipes opens the saved cup, and Edit loads it back onto the brew screen. Update revises that recipe instead of saving another copy.
- **Ready countdown** — a new brew shows Be Ready and counts 3, 2, 1 with a beep before the clock starts. Those seconds are not part of the recipe.
- **Save to journal from the coach** — rate the cup 1–5 after Drawdown done and it goes to the journal with the bean, technique, grind, temperature, taste note, grinder dial, and the actual brew time.
- **Journal dial and time** — journal entries show the grinder and dial used and the brew time (database v9).
- **Step sound** — a speaker button on the timer turns a short beep on or off for each step and for the target time. The choice is remembered. Off by default.
- **Pour timeline** — the brew timer paints a block only while water is going in.
  Gaps are waits and drawdown, with a playhead on the current second.
- **George Peng 2025** — Brewers Cup recipe at 1:14, 96°C, medium-fine. On 15 g /
  210 g: bloom 30 g in 6s, wait 24s, circle-pour to 120 g in 10s, wait 30s,
  Melodrip the rest at 80°C, wait 25s. Finish 2:15. Other doses keep 30/90/90.
- **Selene** — card recipe at 1:19, 91–92°C, medium. On 10 g / 190 g: bloom 30 g
  for 1 minute, pour to 110 g and wait until the bed dries, then 40 g + 40 g the
  same way. Finish 3:30–3:45. Other doses keep that 30/80/40/40 split.
- **Ryan Wibawa** — double immersion on a Switch, 15 g / 220 g, medium. Closed
  bloom 100 g at 86°C, open 5 s, closed pour to 220 g at 92°C in 20 s, open
  through 2:00. Other doses keep 100/120. Valve-open steps are orange on the
  pour timeline; pours stay blue.
- **Devil's Recipe** — Kasuya's improved Switch version, 20 g / 300 g. Closed
  bloom 50 g at 92°C, open pours to 200 g, closed pour to 300 g at 70°C, open
  through 3:30. The last pour step says 70°C. Comandante start is 28 clicks.
- **Comandante C40 in Grind Convert** — 30 per click on the bridge, so 28 clicks
  is 840 and lands near K-Ultra 7.9. That number is the converter unit, not a
  measured particle size.
- **Micron to dial** — Grind Convert can start from a bridge micron and show the
  dial for a chosen grinder, as well as grinder to grinder.
- **ZP6 Special** — lined up with its factory chart (pour-over about 4.0–4.6,
  French press about 5.0–6.0) on the same bridge as K-Ultra 8.5 and 9.5. The
  22 µm burr step is not used.
- **Brew water** — the cheat sheet shows the SCAA 2009 TDS target (150 ppm,
  acceptable 75–250). Amidis 0 ppm and Cleo under 10 ppm are factory claims.
  Nestlé Pure Life, Aqua, and Le Minerale have no single published TDS, so the
  card lists the published readings instead of a match score.

### Changed
- **Name** — the app is called Homebrew (launcher, notification, dashboard, share text). The package id stays `com.brewmaster`.
- **Share** — a shared recipe is plain text. The `brewmaster://` link is gone.
- **Target profiles** — More Acidity now grinds one step coarser and brews 1°C cooler with a bit more water. It used to grind finer and hotter, which pulls more and mutes acidity. More Body grinds one step finer with a stronger ratio at the same temperature, instead of coarser and cooler.
- **Dashboard order** — Hot/Ice sits right after coffee weight and ratio, since it changes the water amounts.
- **Grind chip moves the dial** — picking a grind size, bean, or profile sets the dial wheel to the middle of that size on the selected grinder instead of clearing it.
- **C40 wheel** — goes to 36 clicks so Medium-Coarse to Extra-Coarse can be picked.
- **K-Ultra dial category** — the number under the filter wheel follows the dial
  bands (7.3 Medium-Fine, 8.5 Medium, 8.6 Medium-Coarse). Burr-gap clicks no
  longer mark a V60 dial as Extra-Coarse.
- **Hario Switch Hybrid** — short sweet immersion: bloom 3× dose, valve closed
  until 1:20 at ~7.3× dose (110 g on 15 g, same as half the kettle at 1:14.5),
  then two open pours. Default 1:14.5, 93°C, medium-fine, finish 2:40. At 1:17
  and 1:18 the extra water goes into the open pours and drawdown gains 10s per
  ratio point above 15 (3:00 and 3:10).
- **App icon** — launcher uses the Homebrew dripper + carafe mark (paper
  background, wordmark kept inside the adaptive-icon safe zone).
- **Brew timer redesign** — ring now shows progress of the current step while the
  centre keeps counting total time up; footer points to the next step
  (`next: Pour 2 at 1:15`). Headline `ADD 100 g → 150 g` with the step
  instruction, a Poured / Target / Rate row (target g/s from the recipe), and
  the phase bar labels each pour with its cumulative grams. Controls are two
  wide Pause / Next step buttons with Prev step · Reset as text actions.

### Fixed
- **Recipe card temperature** — the temperature reads on the same line as the other facts, wrapping to the next line when it does not fit, instead of a vertical stack of letters.
- **Brew temperature** — the preview temperature has − and + and that exact range is what gets brewed and saved. Changing technique, bean, or profile picks a new suggestion.
- **Recipe grinder** — the grinder chosen on the brew screen is stored with the recipe and shown with the grind size and dial. Notes stay free text.
- **Brew timer** — rotating the phone, dark mode, font scale, or split-screen no longer restarts the brew. System back stops the foreground timer. Pause, reset, next, and previous update the same clock as the notification, using elapsed realtime. The screen stays on during a brew. Android 13+ asks for notification permission; if it is denied the on-screen timer still runs. The timer can run past the target, and Drawdown done records the actual time.
- **Post-brew coach** — a brew with no taste selected no longer defaults to "stale beans or water that's too cool". Fast and slow notes use the time Drawdown done recorded.
- **Stale results** — clearing coffee weight, or entering 0, clears the brew numbers so Start cannot use the previous cup. An invalid grind-convert dial or micron clears the previous conversion.
- **Custom technique** — "12,5" is 12.5%, not a silent 20%. An invalid duration is rejected instead of becoming 30 seconds. Steps that do not add up to about 100% water, or an empty step list, hide Start and say why.
- **Grind category** — C40 28 clicks and K-Ultra 7.9 both read Medium-Fine. K-Ultra Coarse is labeled 9.2–9.5, matching its band. The convert slope is unchanged.
- **Timer after drawdown** — Drawdown done stops the foreground service, so no paused notification is left behind. Prev or Reset after drawdown starts a paused clock again.
- **Timer without the screen** — the service gets the full step schedule. The notification step name and the step vibration keep working when Android closes the screen in the background.
- **Target reached** — one vibration when the clock passes the recipe time, since the stop alert now waits for Drawdown done.
- **Forgotten timer** — the clock pauses itself 5 minutes after the target if Drawdown done is never tapped, and the screen is allowed to sleep.
- **Process death** — the brew and its clock are saved with the screen. After Android kills the app, the timer comes back at the right time instead of an empty screen. The clock uses elapsed realtime, so time spent killed is counted.
- **Double taps** — a second tap on START or Back while the screen is leaving is ignored. START no longer opens two timers, and Back no longer lands on a blank screen.
- **C40 setting labels** — labels follow the dial bands (Medium-Fine 24–28 clicks, Medium 29–30). Picking Medium-Fine no longer suggests 20 clicks, which the band calls Fine.
- **Saved recipes** — a recipe keeps its grinder, target profile, and custom steps (database v8). Loading matches the bean by name instead of the first bean with the same process, so the roast offset and temperature are the saved ones. A dial from an older recipe is only kept if that dial exists on the current grinder, so C40 28 is not put on a K-Ultra.
- **Grind Convert limits** — a dial past a grinder's range is marked "finest it goes" or "coarsest it goes" in red instead of a plain number (350 µm on ZP6 was shown as 0.0). A typed source dial past its range says which dial was used. Switching the source grinder keeps the same grind instead of jumping to the middle dial.
- **Ratio field** — technique ratios show as 14.67, not 14.666666666666666.
- **Recipe list order** — newest first.
- **Sample beans** — added once when the database is created, so they stay gone after you delete them.
- **Timer service restart** — the service no longer restarts on its own after the app is killed (`START_NOT_STICKY`).
- **One language** — the stop alert, the bean section, and the cheat sheet notes are in English like the rest of the app. The stop alert says "Remove the dripper" for every method and uses grams, not ml.
- **Contrast** — muted text is #8E8E8E (4.5:1 or better on every dark surface) instead of #666666.
- **Pour timeline** — a phase was painted as pouring for its whole clock, so Kasuya
  showed 45s of water. The clock marks stay. The blue block is only the pour
  (bloom ~5 g/s, a normal pulse ~8 g/s, a rapid pulse ~10 g/s) and the rest of
  the phase is a Wait step. Continuous pours stay solid: Hoffmann's two main
  pours, Rao's single pour, the osmotic stream, the single-cup main pour, and
  Peng's Melodrip.
- **B3** Room no longer falls back to wiping the DB. Added 1/2/3→4 table-create
  migrations (history starts at v4) and `exportSchema` so later upgrades can be
  validated. `fallbackToDestructiveMigration()` removed.
- **B2** `GrindSize.fromStored()` — a bad/unknown grind string in recipes,
  process presets, or brew logs maps to Medium instead of crashing the Flow.
- **B1** ICE mode caps ice so hot water stays ≥ 8× dose (bloom + remaining pour,
  including Bypass concentrate). Pour amounts no longer go negative; the stored
  ice weight is the capped value.
- **B7** Timer step-transition events are emitted *after* `_uiState.update`, not
  inside the updater lambda (avoids duplicate haptic / skip events).

## [1.3.0] - 2026-08-26

### Added
- **Grind Convert** — convert a dial from any listed grinder to the others via
  one estimated-particle micron bridge (FM 120).
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
