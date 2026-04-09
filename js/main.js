/* ============================================
   TheBrewingMarket Wiki — Main JS
   ============================================ */

(function () {
  'use strict';

  /* --- Copy button for code blocks --- */
  document.querySelectorAll('.code-block').forEach(function (block) {
    var btn = block.querySelector('.copy-btn');
    var pre = block.querySelector('pre');
    if (!btn || !pre) return;
    btn.addEventListener('click', function () {
      var text = pre.textContent;
      navigator.clipboard.writeText(text).then(function () {
        btn.textContent = 'Copied!';
        setTimeout(function () { btn.textContent = 'Copy'; }, 2000);
      });
    });
  });

  /* --- YAML syntax highlighting ---
     Works from textContent to avoid innerHTML corruption.
     Builds HTML from scratch per line.
  */
  function esc(s) {
    return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
  }

  document.querySelectorAll('pre[data-lang="yaml"]').forEach(function (pre) {
    var lines = pre.textContent.split('\n');

    var html = lines.map(function (raw) {
      var inSingle = false;
      var splitAt = -1;
      for (var i = 0; i < raw.length; i++) {
        if (raw[i] === "'" && !inSingle) { inSingle = true; continue; }
        if (raw[i] === "'" && inSingle) { inSingle = false; continue; }
        if (raw[i] === '#' && !inSingle) { splitAt = i; break; }
      }

      var code = splitAt >= 0 ? raw.substring(0, splitAt) : raw;
      var comment = splitAt >= 0 ? raw.substring(splitAt) : '';

      code = esc(code);
      comment = esc(comment);

      code = code.replace(/'([^']*)'/g, function (m, inner) {
        return "'<span class=ss>" + inner + "</span>'";
      });
      code = code.replace(/:\s*(true|false)\b/g, ': <span class=sb>$1</span>');
      code = code.replace(/:\s*(\d+\.?\d*)\b/g, ': <span class=sn>$1</span>');
      code = code.replace(/^(\s*)([\w-]+)(\s*:)/, '$1<span class=sk>$2</span>$3');
      code = code.replace(/^(\s*)(- )/, '$1<span class=sd>$2</span>');

      if (comment) {
        comment = '<span class=sc>' + comment + '</span>';
      }

      return code + comment;
    }).join('\n');

    pre.innerHTML = html;
  });
})();