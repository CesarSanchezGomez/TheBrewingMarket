/* ============================================
   TheBrewingMarket Wiki — Navigation
   Generates sidebar + header from a single source.
   ============================================ */

(function () {
  'use strict';

  var NAV = [
    {
      title: 'Start',
      links: [
        { href: 'index', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 576 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M575.8 255.5c0 18-15 32.1-32 32.1l-32 0 .7 160.2c0 2.7-.2 5.4-.5 8.1l0 16.2c0 22.1-17.9 40-40 40l-16 0c-1.1 0-2.2 0-3.3-.1c-1.4 .1-2.8 .1-4.2 .1L416 512l-24 0c-22.1 0-40-17.9-40-40l0-24 0-64c0-17.7-14.3-32-32-32l-64 0c-17.7 0-32 14.3-32 32l0 64 0 24c0 22.1-17.9 40-40 40l-24 0-31.9 0c-1.5 0-3-.1-4.5-.2c-1.2 .1-2.4 .2-3.6 .2l-16 0c-22.1 0-40-17.9-40-40l0-112c0-.9 0-1.9 .1-2.8l0-69.7-32 0c-18 0-32-14-32-32.1c0-9 3-17 10-24L266.4 8c7-7 15-8 22-8s15 2 21 7L564.8 231.5c8 7 12 15 11 24z"/></svg>', text: 'Home' },
        { href: 'installation', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M349.4 44.6c5.9-13.7 1.5-29.7-10.6-38.5s-28.6-8-39.9 1.8l-256 224c-10 8.8-13.6 22.9-8.9 35.3S50.7 288 64 288l111.5 0L98.6 467.4c-5.9 13.7-1.5 29.7 10.6 38.5s28.6 8 39.9-1.8l256-224c10-8.8 13.6-22.9 8.9-35.3s-16.6-20.7-30-20.7l-111.5 0L349.4 44.6z"/></svg>', text: 'Installation' },
        { href: 'commands', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M392.8 1.2c-17-4.9-34.7 5-39.6 22l-128 448c-4.9 17 5 34.7 22 39.6s34.7-5 39.6-22l128-448c4.9-17-5-34.7-22-39.6zm80.6 120.1c-12.5 12.5-12.5 32.8 0 45.3L562.7 256l-89.4 89.4c-12.5 12.5-12.5 32.8 0 45.3s32.8 12.5 45.3 0l112-112c12.5-12.5 12.5-32.8 0-45.3l-112-112c-12.5-12.5-32.8-12.5-45.3 0zm-306.7 0c-12.5-12.5-32.8-12.5-45.3 0l-112 112c-12.5 12.5-12.5 32.8 0 45.3l112 112c12.5 12.5 32.8 12.5 45.3 0s12.5-32.8 0-45.3L77.3 256l89.4-89.4c12.5-12.5 12.5-32.8 0-45.3z"/></svg>', text: 'Commands &amp; Permissions' },
        { href: 'troubleshooting', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M256 512A256 256 0 1 0 256 0a256 256 0 1 0 0 512zm0-384c13.3 0 24 10.7 24 24l0 112c0 13.3-10.7 24-24 24s-24-10.7-24-24l0-112c0-13.3 10.7-24 24-24zm-32 224a32 32 0 1 1 64 0 32 32 0 1 1 -64 0z"/></svg>', text: 'Troubleshooting' }
      ]
    },
    {
      title: 'Configuration',
      links: [
        { href: 'configuration', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M495.9 166.6c3.2 8.7 .5 18.4-6.4 24.6l-43.3 39.4c1.1 8.3 1.7 16.8 1.7 25.4s-.6 17.1-1.7 25.4l43.3 39.4c6.9 6.2 9.6 15.9 6.4 24.6c-4.4 11.9-9.7 23.3-15.8 34.3l-4.7 8.1c-6.6 11-14 21.4-22.1 31.2c-5.9 7.2-15.7 9.6-24.5 6.8l-55.7-17.7c-13.4 10.3-28.2 18.9-44 25.4l-12.5 57.1c-2 9.1-9 16.3-18.2 17.8c-13.8 2.3-28 3.5-42.5 3.5s-28.7-1.2-42.5-3.5c-9.2-1.5-16.2-8.7-18.2-17.8l-12.5-57.1c-15.8-6.5-30.6-15.1-44-25.4L83.1 425.9c-8.8 2.8-18.6 .3-24.5-6.8c-8.1-9.8-15.5-20.2-22.1-31.2l-4.7-8.1c-6.1-11-11.4-22.4-15.8-34.3c-3.2-8.7-.5-18.4 6.4-24.6l43.3-39.4C64.6 273.1 64 264.6 64 256s.6-17.1 1.7-25.4L22.4 191.2c-6.9-6.2-9.6-15.9-6.4-24.6c4.4-11.9 9.7-23.3 15.8-34.3l4.7-8.1c6.6-11 14-21.4 22.1-31.2c5.9-7.2 15.7-9.6 24.5-6.8l55.7 17.7c13.4-10.3 28.2-18.9 44-25.4l12.5-57.1c2-9.1 9-16.3 18.2-17.8C227.3 1.2 241.5 0 256 0s28.7 1.2 42.5 3.5c9.2 1.5 16.2 8.7 18.2 17.8l12.5 57.1c15.8 6.5 30.6 15.1 44 25.4l55.7-17.7c8.8-2.8 18.6-.3 24.5 6.8c8.1 9.8 15.5 20.2 22.1 31.2l4.7 8.1c6.1 11 11.4 22.4 15.8 34.3zM256 336a80 80 0 1 0 0-160 80 80 0 1 0 0 160z"/></svg>', text: 'General' },
        { href: 'prices', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M320 96L192 96 144.6 24.9C137.5 14.2 145.1 0 157.9 0L354.1 0c12.8 0 20.4 14.2 13.3 24.9L320 96zM192 128l128 0c3.8 2.5 8.1 5.3 13 8.4C389.7 172.7 512 250.9 512 416c0 53-43 96-96 96L96 512c-53 0-96-43-96-96C0 250.9 122.3 172.7 179 136.4c0 0 0 0 0 0s0 0 0 0c4.8-3.1 9.2-5.9 13-8.4zm84 88c0-11-9-20-20-20s-20 9-20 20l0 14c-7.6 1.7-15.2 4.4-22.2 8.5c-13.9 8.3-25.9 22.8-25.8 43.9c.1 20.3 12 33.1 24.7 40.7c11 6.6 24.7 10.8 35.6 14l1.7 .5c12.6 3.8 21.8 6.8 28 10.7c5.1 3.2 5.8 5.4 5.9 8.2c.1 5-1.8 8-5.9 10.5c-5 3.1-12.9 5-21.4 4.7c-11.1-.4-21.5-3.9-35.1-8.5c-2.3-.8-4.7-1.6-7.2-2.4c-10.5-3.5-21.8 2.2-25.3 12.6s2.2 21.8 12.6 25.3c1.9 .6 4 1.3 6.1 2.1c0 0 0 0 0 0s0 0 0 0c8.3 2.9 17.9 6.2 28.2 8.4l0 14.6c0 11 9 20 20 20s20-9 20-20l0-13.8c8-1.7 16-4.5 23.2-9c14.3-8.9 25.1-24.1 24.8-45c-.3-20.3-11.7-33.4-24.6-41.6c-11.5-7.2-25.9-11.6-37.1-15c0 0 0 0 0 0l-.7-.2c-12.8-3.9-21.9-6.7-28.3-10.5c-5.2-3.1-5.3-4.9-5.3-6.7c0-3.7 1.4-6.5 6.2-9.3c5.4-3.2 13.6-5.1 21.5-5c9.6 .1 20.2 2.2 31.2 5.2c10.7 2.8 21.6-3.5 24.5-14.2s-3.5-21.6-14.2-24.5c-6.5-1.7-13.7-3.4-21.1-4.7l0-13.9z"/></svg>', text: 'Prices' },
        { href: 'layout', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M384 96l0 128-128 0 0-128 128 0zm0 192l0 128-128 0 0-128 128 0zM192 224L64 224 64 96l128 0 0 128zM64 288l128 0 0 128L64 416l0-128zM64 32C28.7 32 0 60.7 0 96L0 416c0 35.3 28.7 64 64 64l320 0c35.3 0 64-28.7 64-64l0-320c0-35.3-28.7-64-64-64L64 32z"/></svg>', text: 'GUI Layout' },
        { href: 'sell-buttons', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M64 0C46.3 0 32 14.3 32 32l0 64c0 17.7 14.3 32 32 32l80 0 0 32-57 0c-31.6 0-58.5 23.1-63.3 54.4L1.1 364.1C.4 368.8 0 373.6 0 378.4L0 448c0 35.3 28.7 64 64 64l384 0c35.3 0 64-28.7 64-64l0-69.6c0-4.8-.4-9.6-1.1-14.4L488.2 214.4C483.5 183.1 456.6 160 425 160l-217 0 0-32 80 0c17.7 0 32-14.3 32-32l0-64c0-17.7-14.3-32-32-32L64 0zM96 48l160 0c8.8 0 16 7.2 16 16s-7.2 16-16 16L96 80c-8.8 0-16-7.2-16-16s7.2-16 16-16zM64 432c0-8.8 7.2-16 16-16l352 0c8.8 0 16 7.2 16 16s-7.2 16-16 16L80 448c-8.8 0-16-7.2-16-16zm48-168a24 24 0 1 1 0-48 24 24 0 1 1 0 48zm120-24a24 24 0 1 1 -48 0 24 24 0 1 1 48 0zM160 344a24 24 0 1 1 0-48 24 24 0 1 1 0 48zM328 240a24 24 0 1 1 -48 0 24 24 0 1 1 48 0zM256 344a24 24 0 1 1 0-48 24 24 0 1 1 0 48zM424 240a24 24 0 1 1 -48 0 24 24 0 1 1 48 0zM352 344a24 24 0 1 1 0-48 24 24 0 1 1 0 48z"/></svg>', text: 'Sell Buttons' },
        { href: 'icons', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M500.3 7.3C507.7 13.3 512 22.4 512 32l0 144c0 26.5-28.7 48-64 48s-64-21.5-64-48s28.7-48 64-48l0-57L352 90.2 352 208c0 26.5-28.7 48-64 48s-64-21.5-64-48s28.7-48 64-48l0-96c0-15.3 10.8-28.4 25.7-31.4l160-32c9.4-1.9 19.1 .6 26.6 6.6zM74.7 304l11.8-17.8c5.9-8.9 15.9-14.2 26.6-14.2l61.7 0c10.7 0 20.7 5.3 26.6 14.2L213.3 304l26.7 0c26.5 0 48 21.5 48 48l0 112c0 26.5-21.5 48-48 48L48 512c-26.5 0-48-21.5-48-48L0 352c0-26.5 21.5-48 48-48l26.7 0zM192 408a48 48 0 1 0 -96 0 48 48 0 1 0 96 0zM478.7 278.3L440.3 368l55.7 0c6.7 0 12.6 4.1 15 10.4s.6 13.3-4.4 17.7l-128 112c-5.6 4.9-13.9 5.3-19.9 .9s-8.2-12.4-5.3-19.2L391.7 400 336 400c-6.7 0-12.6-4.1-15-10.4s-.6-13.3 4.4-17.7l128-112c5.6-4.9 13.9-5.3 19.9-.9s8.2 12.4 5.3 19.2zm-339-59.2c-6.5 6.5-17 6.5-23 0L19.9 119.2c-28-29-26.5-76.9 5-103.9c27-23.5 68.4-19 93.4 6.5l10 10.5 9.5-10.5c25-25.5 65.9-30 93.9-6.5c31 27 32.5 74.9 4.5 103.9l-96.4 99.9z"/></svg>', text: 'Icons &amp; Items' },
        { href: 'database', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M448 80l0 48c0 44.2-100.3 80-224 80S0 172.2 0 128L0 80C0 35.8 100.3 0 224 0S448 35.8 448 80zM393.2 214.7c20.8-7.4 39.9-16.9 54.8-28.6L448 288c0 44.2-100.3 80-224 80S0 332.2 0 288L0 186.1c14.9 11.8 34 21.2 54.8 28.6C99.7 230.7 159.5 240 224 240s124.3-9.3 169.2-25.3zM0 346.1c14.9 11.8 34 21.2 54.8 28.6C99.7 390.7 159.5 400 224 400s124.3-9.3 169.2-25.3c20.8-7.4 39.9-16.9 54.8-28.6l0 85.9c0 44.2-100.3 80-224 80S0 476.2 0 432l0-85.9z"/></svg>', text: 'Database' },
        { href: 'language', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 640 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M0 128C0 92.7 28.7 64 64 64l192 0 48 0 16 0 256 0c35.3 0 64 28.7 64 64l0 256c0 35.3-28.7 64-64 64l-256 0-16 0-48 0L64 448c-35.3 0-64-28.7-64-64L0 128zm320 0l0 256 256 0 0-256-256 0zM178.3 175.9c-3.2-7.2-10.4-11.9-18.3-11.9s-15.1 4.7-18.3 11.9l-64 144c-4.5 10.1 .1 21.9 10.2 26.4s21.9-.1 26.4-10.2l8.9-20.1 73.6 0 8.9 20.1c4.5 10.1 16.3 14.6 26.4 10.2s14.6-16.3 10.2-26.4l-64-144zM160 233.2L179 276l-38 0 19-42.8zM448 164c11 0 20 9 20 20l0 4 44 0 16 0c11 0 20 9 20 20s-9 20-20 20l-2 0-1.6 4.5c-8.9 24.4-22.4 46.6-39.6 65.4c.9 .6 1.8 1.1 2.7 1.6l18.9 11.3c9.5 5.7 12.5 18 6.9 27.4s-18 12.5-27.4 6.9l-18.9-11.3c-4.5-2.7-8.8-5.5-13.1-8.5c-10.6 7.5-21.9 14-34 19.4l-3.6 1.6c-10.1 4.5-21.9-.1-26.4-10.2s.1-21.9 10.2-26.4l3.6-1.6c6.4-2.9 12.6-6.1 18.5-9.8l-12.2-12.2c-7.8-7.8-7.8-20.5 0-28.3s20.5-7.8 28.3 0l14.6 14.6 .5 .5c12.4-13.1 22.5-28.3 29.8-45L448 228l-72 0c-11 0-20-9-20-20s9-20 20-20l52 0 0-4c0-11 9-20 20-20z"/></svg>', text: 'Language' }
      ]
    },
    {
      title: 'Integrations',
      links: [
        { href: 'placeholders', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M0 80C0 53.5 21.5 32 48 32l96 0c26.5 0 48 21.5 48 48l0 16 192 0 0-16c0-26.5 21.5-48 48-48l96 0c26.5 0 48 21.5 48 48l0 96c0 26.5-21.5 48-48 48l-96 0c-26.5 0-48-21.5-48-48l0-16-192 0 0 16c0 1.7-.1 3.4-.3 5L272 288l112 0 0-16c0-26.5 21.5-48 48-48l96 0c26.5 0 48 21.5 48 48l0 96c0 26.5-21.5 48-48 48l-96 0c-26.5 0-48-21.5-48-48l0-16-112 0c-26.5 0-48-21.5-48-48l0-77c-1.6 .2-3.3 .3-5 .3l-16 0 0 96c0 26.5-21.5 48-48 48l-96 0c-26.5 0-48-21.5-48-48l0-96c0-26.5 21.5-48 48-48l96 0c1.7 0 3.4 .1 5 .3L160 144l-16 0c-26.5 0-48-21.5-48-48l0-16z"/></svg>', text: 'PlaceholderAPI' }
      ]
    },
    {
      title: 'References',
      links: [
        { href: 'references', icon: '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 384 512"><!--!Font Awesome Free v6.7.2 by @fontawesome - https://fontawesome.com License - https://fontawesome.com/license/free Copyright 2026 Fonticons, Inc.--><path fill="rgb(255, 255, 255)" d="M64 0C28.7 0 0 28.7 0 64L0 448c0 35.3 28.7 64 64 64l256 0c35.3 0 64-28.7 64-64l0-288-128 0c-17.7 0-32-14.3-32-32L224 0 64 0zM256 0l0 128 128 0L256 0zM112 256l160 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-160 0c-8.8 0-16-7.2-16-16s7.2-16 16-16zm0 64l160 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-160 0c-8.8 0-16-7.2-16-16s7.2-16 16-16zm0 64l160 0c8.8 0 16 7.2 16 16s-7.2 16-16 16l-160 0c-8.8 0-16-7.2-16-16s7.2-16 16-16z"/></svg>', text: 'Default Files' }
      ]
    }
  ];

  var isLocal = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
  var raw = location.pathname.replace(/\/$/, '').split('/').pop();
  var current = (raw || 'index').replace(/\.html$/, '');

  // On local dev servers add .html back to nav hrefs so Live Server works.
  if (isLocal) {
    NAV.forEach(function (section) {
      section.links.forEach(function (link) {
        if (link.href && !link.href.endsWith('.html')) {
          link.href = link.href + '.html';
        }
      });
    });
  }

  // Find current page name for breadcrumb
  var pageName = 'Home';
  NAV.forEach(function (section) {
    section.links.forEach(function (link) {
      if (link.href === current) {
        // Decode HTML entities for breadcrumb
        var tmp = document.createElement('span');
        tmp.innerHTML = link.text;
        pageName = tmp.textContent;
      }
    });
  });

  // Build sidebar HTML
  var sections = NAV.map(function (section) {
    var links = section.links.map(function (link) {
      var active = link.href === current ? ' class=active' : '';
      return '<a href="' + link.href + '"' + active + '>' +
        '<span class=nav-icon>' + link.icon + '</span> ' + link.text + '</a>';
    }).join('');

    return '<div class=nav-section>' +
      '<div class=nav-section-title>' + section.title + '</div>' +
      links + '</div>';
  }).join('');

  var githubSVG = '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 496 512"><path fill="currentColor" d="M165.9 397.4c0 2-2.3 3.6-5.2 3.6-3.3.3-5.6-1.3-5.6-3.6 0-2 2.3-3.6 5.2-3.6 3-.3 5.6 1.3 5.6 3.6zm-31.1-4.5c-.7 2 1.3 4.3 4.3 4.9 2.6 1 5.6 0 6.2-2s-1.3-4.3-4.3-5.2c-2.6-.7-5.5.3-6.2 2.3zm44.2-1.7c-2.9.7-4.9 2.6-4.6 4.9.3 2 2.9 3.3 5.9 2.6 2.9-.7 4.9-2.6 4.6-4.6-.3-1.9-3-3.2-5.9-2.9zM244.8 8C106.1 8 0 113.3 0 252c0 110.9 69.8 205.8 169.5 239.2 12.8 2.3 17.3-5.6 17.3-12.1 0-6.2-.3-40.4-.3-61.4 0 0-70 15-84.7-29.8 0 0-11.4-29.1-27.8-36.6 0 0-22.9-15.7 1.6-15.4 0 0 24.9 2 38.6 25.8 21.9 38.6 58.6 27.5 72.9 20.9 2.3-16 8.8-27.1 16-33.7-55.9-6.2-112.3-14.3-112.3-110.5 0-27.5 7.6-41.3 23.6-58.9-2.6-6.5-11.1-33.3 2.6-67.9 20.9-6.5 69 27 69 27 20-5.6 41.5-8.5 62.8-8.5s42.8 2.9 62.8 8.5c0 0 48.1-33.6 69-27 13.7 34.7 5.2 61.4 2.6 67.9 16 17.7 25.8 31.5 25.8 58.9 0 96.5-58.9 104.2-114.8 110.5 9.2 7.9 17 22.9 17 46.4 0 33.7-.3 75.4-.3 83.6 0 6.5 4.6 14.4 17.3 12.1C428.2 457.8 496 362.9 496 252 496 113.3 390.9 8 244.8 8z"/></svg>';

  var sidebarHTML =
    '<div class=sidebar-brand>' +
      '<img class=logo src="assets/images/TBM_Logo_128x128.webp" alt="TheBrewingMarket">' +
      '<h1>TheBrewingMarket <span class=version>v2.2.2</span></h1>' +
    '</div>' +
    '<nav class=sidebar-nav>' + sections + '</nav>' +
    '<div class=sidebar-footer>' +
      '<a href="https://github.com/CesarSanchezGomez/TheBrewingMarket" target="_blank" class=github-link>' +
        '<span class=github-icon>' + githubSVG + '</span>GitHub' +
      '</a>' +
    '</div>';

  var headerHTML =
    '<button class=menu-toggle aria-label="Toggle menu">&#9776;</button>' +
    '<div class=breadcrumb>TheBrewingMarket / <span>' + pageName + '</span></div>';

  // Inject
  var sidebar = document.querySelector('.sidebar');
  var header = document.querySelector('.header');
  if (sidebar) sidebar.innerHTML = sidebarHTML;
  if (header) header.innerHTML = headerHTML;

  // Mobile menu toggle (re-bind after inject)
  var toggle = document.querySelector('.menu-toggle');
  var overlay = document.querySelector('.sidebar-overlay');

  // Move overlay inside .wrapper so it shares the same stacking context as
  // the sidebar. Without this, .wrapper (z-index:1) is below the overlay
  // (z-index:90 in root), making the overlay paint over the sidebar too.
  var wrapper = document.querySelector('.wrapper');
  if (overlay && wrapper) {
    wrapper.insertBefore(overlay, wrapper.firstChild);
  }

  if (toggle && sidebar) {
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('open');
      if (overlay) overlay.classList.toggle('show');
    });
  }
  if (overlay) {
    overlay.addEventListener('click', function () {
      sidebar.classList.remove('open');
      overlay.classList.remove('show');
    });
  }

  // Close sidebar when a nav link is tapped on mobile (let href navigate normally)
  if (sidebar) {
    var navLinks = sidebar.querySelectorAll('.sidebar-nav a');
    Array.prototype.forEach.call(navLinks, function (a) {
      a.addEventListener('click', function () {
        sidebar.classList.remove('open');
        if (overlay) overlay.classList.remove('show');
      });
    });
  }

  // Favicon
  var favicon = document.createElement('link');
  favicon.rel = 'icon';
  favicon.href = 'assets/images/TBM_Icon.ico';
  document.head.appendChild(favicon);

  // Background particles
  var flaskSVG =
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="currentColor"><path d="M15 2a1 1 0 0 1 0 2v5.674l0.062 0.03a7 7 0 0 1 3.85 5.174l0.037 0.262a7 7 0 0 1 -3.078 6.693 1 1 0 0 1 -0.553 0.167H8.683a1 1 0 0 1 -0.552 -0.166A7 7 0 0 1 8.938 9.7L9 9.672V4a1 1 0 1 1 0 -2h6zm-2 2h-2v6.34a1 1 0 0 1 -0.551 0.894l-0.116 0.049A5 5 0 0 0 7.413 14h9.172a5 5 0 0 0 -2.918 -2.715 1 1 0 0 1 -0.667 -0.943V4z" stroke-width="1"/></svg>';

  var bubbleIcons = [
    flaskSVG, '$', flaskSVG, '$', flaskSVG,
    '$', flaskSVG, '$', flaskSVG, '$',
    flaskSVG
  ];

  var bubblesDiv = document.createElement('div');
  bubblesDiv.className = 'bg-bubbles';
  bubbleIcons.forEach(function (icon) {
    var span = document.createElement('span');
    span.innerHTML = icon;
    bubblesDiv.appendChild(span);
  });
  document.body.insertAdjacentElement('afterbegin', bubblesDiv);

  // Keep bubble positions consistent across page navigations.
  // sessionStorage stores a "session start" timestamp; each page calculates
  // how many seconds have elapsed and uses (elapsed % duration) as a negative
  // delay, so every span is exactly where it would be if it had been running
  // since the session began.
  var bubbleStart = parseInt(sessionStorage.getItem('bubbleStart') || '0');
  if (!bubbleStart) {
    // First load: pick a random point in the past (0–60 s) so bubbles
    // already appear mid-flight rather than all rising from the bottom.
    bubbleStart = Date.now() - Math.floor(Math.random() * 60000);
    sessionStorage.setItem('bubbleStart', String(bubbleStart));
  }
  var elapsed = (Date.now() - bubbleStart) / 1000;
  Array.prototype.forEach.call(bubblesDiv.querySelectorAll('span'), function (span) {
    var dur = parseFloat(window.getComputedStyle(span).animationDuration) || 15;
    span.style.animationDelay = '-' + (elapsed % dur).toFixed(2) + 's';
  });

  // Strip .html from all internal links only on GitHub Pages.
  // Local dev servers (Live Server, etc.) don't support extensionless URLs.
  var isLocal = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
  if (!isLocal) {
    var allLinks = document.querySelectorAll('a[href$=".html"]');
    Array.prototype.forEach.call(allLinks, function (a) {
      var h = a.getAttribute('href');
      if (h && !/^https?:\/\//.test(h)) {
        a.setAttribute('href', h.replace(/\.html$/, ''));
      }
    });
  }
})();