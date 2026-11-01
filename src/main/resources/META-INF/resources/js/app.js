(function () {
  'use strict';
  var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  var nf = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  function p(n) { return String(n).padStart(2, '0'); }
  function money(v) { return '$ ' + nf.format(v); }
  function ymd(d) { return d.getFullYear() + '-' + p(d.getMonth() + 1) + '-' + p(d.getDate()); }

  /* Reloj y pantalla LCD del encabezado */
  var clock = document.getElementById('rg-clock');
  if (clock) {
    (function tick() {
      var n = new Date();
      clock.textContent = p(n.getHours()) + ':' + p(n.getMinutes()) + ':' + p(n.getSeconds());
      setTimeout(tick, 1000);
    })();
  }
  var screen = document.getElementById('rg-screen');
  if (screen) {
    var text = '> ' + document.title.split('|')[0].trim().toUpperCase();
    if (reduce) {
      screen.textContent = text;
    } else {
      var i = 2;
      screen.textContent = '> ';
      var timer = setInterval(function () {
        i++;
        screen.textContent = text.slice(0, i);
        if (i >= text.length) { clearInterval(timer); }
      }, 45);
    }
  }

  /* Tecla activa del menú */
  var path = location.pathname.replace(/\/+$/, '') || '/', best = null;
  document.querySelectorAll('.rg-key[data-match]').forEach(function (k) {
    var m = k.getAttribute('data-match');
    var hit = path === m || path.indexOf(m + '/') === 0;
    if (hit && (!best || m.length > best.getAttribute('data-match').length)) { best = k; }
  });
  if (best) { best.classList.add('is-on'); best.setAttribute('aria-current', 'page'); }

  /* Avisos: los de éxito se cierran solos; se limpia la URL para que F5 no los repita */
  document.querySelectorAll('.rg-alert.alert-success').forEach(function (a) {
    setTimeout(function () {
      if (window.bootstrap && document.body.contains(a)) { bootstrap.Alert.getOrCreateInstance(a).close(); }
    }, 6000);
  });
  var qs = new URLSearchParams(location.search);
  if (qs.has('success') || qs.has('error')) {
    qs.delete('success'); qs.delete('error');
    var rest = qs.toString();
    history.replaceState(null, '', location.pathname + (rest ? '?' + rest : '') + location.hash);
  }

  /* Filas que se "imprimen" una tras otra */
  document.querySelectorAll('.rg-table tbody tr').forEach(function (tr, idx) {
    tr.style.setProperty('--i', Math.min(idx, 24));
  });

  /* Contadores animados */
  function countTo(el, target, kind) {
    var from = parseFloat(el.getAttribute('data-cur') || '0') || 0, t0 = null, dur = 650;
    function paint(v) { el.textContent = kind === 'money' ? money(v) : String(Math.round(v)); }
    if (reduce) { paint(target); el.setAttribute('data-cur', target); return; }
    function step(ts) {
      if (!t0) { t0 = ts; }
      var k = Math.min(1, (ts - t0) / dur), e = 1 - Math.pow(1 - k, 3);
      paint(from + (target - from) * e);
      if (k < 1) { requestAnimationFrame(step); } else { el.setAttribute('data-cur', target); }
    }
    requestAnimationFrame(step);
  }
  document.querySelectorAll('[data-count]').forEach(function (el) {
    countTo(el, parseFloat(el.getAttribute('data-count')) || 0, el.getAttribute('data-fmt'));
  });

  /* Contadores del listado de facturas (según las filas visibles) */
  var kpiBox = document.querySelector('[data-kpi]');
  function renderKpis() {
    if (!kpiBox) { return; }
    var count = 0, total = 0, paid = 0, pending = 0;
    document.querySelectorAll('#inv-table tbody tr[data-status]').forEach(function (tr) {
      if (tr.hidden) { return; }
      var st = tr.getAttribute('data-status'), v = parseFloat(tr.getAttribute('data-total')) || 0;
      count++;
      if (st !== 'CANCELLED') { total += v; }
      if (st === 'PAID') { paid += v; }
      if (st === 'ISSUED') { pending += v; }
    });
    countTo(document.getElementById('k-count'), count, 'int');
    countTo(document.getElementById('k-total'), total, 'money');
    countTo(document.getElementById('k-paid'), paid, 'money');
    countTo(document.getElementById('k-pending'), pending, 'money');
  }
  renderKpis();

  /* Buscador instantáneo de tablas */
  document.querySelectorAll('[data-filter]').forEach(function (input) {
    var table = document.querySelector(input.getAttribute('data-filter'));
    if (!table) { return; }
    input.addEventListener('input', function () {
      var q = input.value.trim().toLowerCase(), visible = 0;
      table.querySelectorAll('tbody tr').forEach(function (tr) {
        if (tr.classList.contains('rg-noresults') || tr.querySelector('td[colspan]') && !tr.hasAttribute('data-status') && !tr.hasAttribute('data-row')) { return; }
        var show = !q || tr.textContent.toLowerCase().indexOf(q) !== -1;
        tr.hidden = !show;
        if (show) { visible++; }
      });
      var none = table.querySelector('.rg-noresults');
      if (none) { none.hidden = !(q && visible === 0); }
      renderKpis();
    });
  });

  /* Reportes: atajos de rango, dona y barras */
  var rangeForm = document.getElementById('range-form');
  document.querySelectorAll('[data-range]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var now = new Date(), from = new Date(now), kind = btn.getAttribute('data-range');
      if (kind === 'week') { from.setDate(now.getDate() - 6); }
      if (kind === 'month') { from = new Date(now.getFullYear(), now.getMonth(), 1); }
      if (kind === 'year') { from = new Date(now.getFullYear(), 0, 1); }
      document.getElementById('from').value = ymd(from);
      document.getElementById('to').value = ymd(now);
      if (rangeForm) { rangeForm.submit(); }
    });
  });
  var donut = document.querySelector('.rg-donut');
  if (donut) {
    var paidV = parseFloat(donut.getAttribute('data-paid')) || 0, pendV = parseFloat(donut.getAttribute('data-pending')) || 0;
    var tot = paidV + pendV, pct = tot ? Math.round(paidV / tot * 100) : 0, lab = donut.querySelector('span');
    if (lab) { lab.textContent = tot ? pct + '%' : '—'; }
    if (!tot) { donut.classList.add('is-empty'); }
    setTimeout(function () { donut.style.setProperty('--p', pct); }, 200);
  }
  var bars = document.querySelectorAll('.rg-bar[data-total]'), maxV = 0;
  bars.forEach(function (b) { maxV = Math.max(maxV, parseFloat(b.getAttribute('data-total')) || 0); });
  bars.forEach(function (b, idx) {
    var v = parseFloat(b.getAttribute('data-total')) || 0, fill = b.querySelector('i');
    fill.style.setProperty('--w', maxV ? (v / maxV * 100).toFixed(1) : 0);
    fill.style.animationDelay = (idx * 70 + 400) + 'ms';
  });

  /* Imprimir, confirmaciones y doble envío */
  document.querySelectorAll('[data-print]').forEach(function (b) { b.addEventListener('click', function () { window.print(); }); });
  document.querySelectorAll('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!window.confirm(f.getAttribute('data-confirm'))) { e.preventDefault(); } });
  });
  document.querySelectorAll('form').forEach(function (f) {
    f.addEventListener('submit', function (e) {
      var btn = f.querySelector('button[type=submit]');
      if (btn) { setTimeout(function () { if (!e.defaultPrevented) { btn.disabled = true; } }, 0); }
    });
  });
  window.addEventListener('pageshow', function () {
    document.querySelectorAll('button[type=submit]:disabled').forEach(function (b) { b.disabled = false; });
  });
})();