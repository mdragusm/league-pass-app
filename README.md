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

The current `menu.json` is sample data.

## Run locally

```
python -m http.server 8000
```
Open http://localhost:8000. A service worker needs HTTPS or localhost.

## Install on a phone

Host the folder over HTTPS (GitHub Pages), open the URL in Chrome on Android, menu > Add to Home screen.
