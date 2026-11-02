/* Sprintly service worker.
   - Network-first for the app files, so a new version you push to GitHub shows up
     on the next open (cache is only a fallback when offline).
   - Handles push + notification taps.
   If you already have your own sw.js with push logic, keep yours instead of this one. */
const CACHE = 'sprintly-shell';

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (e) => e.waitUntil(self.clients.claim()));

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET' || new URL(req.url).origin !== location.origin) return;
  e.respondWith(
    fetch(req)
      .then((res) => {
        const copy = res.clone();
        caches.open(CACHE).then((c) => c.put(req, copy)).catch(() => {});
        return res;
      })
      .catch(() => caches.match(req).then((r) => r || caches.match('./')))
  );
});

self.addEventListener('push', (e) => {
  let d = {};
  try { d = e.data ? e.data.json() : {}; } catch (_) { d = { title: 'Sprintly', body: e.data && e.data.text() }; }
  e.waitUntil(self.registration.showNotification(d.title || 'Sprintly', {
    body: d.body || '',
    icon: 'icon-192.png',
    badge: 'icon-192.png',
    tag: d.tag,
    requireInteraction: true,
    vibrate: [300, 150, 300],
    data: { url: d.url || './' }
  }));
});

self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  e.waitUntil(self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((list) => {
    for (const c of list) { if ('focus' in c) return c.focus(); }
    return self.clients.openWindow((e.notification.data && e.notification.data.url) || './');
  }));
});
