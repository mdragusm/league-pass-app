# league-pass-app

Installable web app (PWA) that shows the daily "League Pass Menu": every NBA game on tonight's slate, rated 1-10 for watchability, Spurs games highlighted.

## How it works

- A scheduled Claude task (League Pass Menu, 3:20 PM Monterrey daily) rates the slate.
- The task is meant to write the result to `menu.json` in this repo (not wired up yet).
- The app (`index.html`) fetches `menu.json` on open, sorts by score and renders it. It caches the last menu for offline use.

## `menu.json` format

```
{ "date": "YYYY-MM-DD", "date_label": "Monday, Nov 09", "generated_at": ISO time,
  "games": [ { "score": 8, "spurs": true,
               "away": {"name","record"}, "home": {"name","record"},
               "tip": "6:00 PM", "notes": "..." } ],
  "tiers": "...", "sources": "..." }
```

## Schedule and box scores

The **Schedule** tab reads ESPN's public NBA JSON straight from the page (no server, nothing stored in the repo):
a team's full regular season (default: Spurs) or all games for any date. Tap a finished or live game for its box score.
Today's games show the rating from `menu.json`. Fetched data is compacted and cached in `localStorage` for offline use.
ESPN's API is unofficial; if it changes, the Schedule tab is what breaks, not the daily menu.

## Android app

`android/` is a tiny native shell (about 12 KB): it opens the site full-screen and, after 3 PM Monterrey time, posts the
`push` text from `menu.json` as a notification. The GitHub Actions workflow builds a signed APK on every change under
`android/` and publishes it at `releases/latest/download/league-pass-menu.apk`.

## Run locally

```
python -m http.server 8000
```
Open http://localhost:8000. A service worker needs HTTPS or localhost.

## Install on a phone

Host the folder over HTTPS (GitHub Pages), open the URL in Chrome on Android, menu > Add to Home screen.
