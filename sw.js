// Caches the app shell only. menu.json is always fetched fresh (index.html falls back to localStorage offline).
const CACHE = 'lpm-shell-v1';
const SHELL = ['./', 'index.html', 'manifest.webmanifest', 'icon.svg'];

self.addEventListener('install', e => e.waitUntil(caches.open(CACHE).then(c => c.addAll(SHELL))));
self.addEventListener('activate', e => e.waitUntil(
  caches.keys().then(ks => Promise.all(ks.filter(k => k !== CACHE).map(k => caches.delete(k))))
));
self.addEventListener('fetch', e => {
  const url = new URL(e.request.url);
  if (url.pathname.endsWith('menu.json')) return; // network only
  e.respondWith(caches.match(e.request).then(r => r || fetch(e.request)));
});
