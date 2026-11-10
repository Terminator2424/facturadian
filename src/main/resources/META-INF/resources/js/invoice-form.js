(function () {
  var form = document.getElementById('invoice-form');
  if (!form) { return; }
  var box = document.getElementById('rg-rows'), sel = document.getElementById('clientId');
  var q = document.getElementById('product-q'), results = document.getElementById('product-results');
  var nf = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  var IVA = 0.19, RETE = 0.15, MAX = 50, MAX_QTY = 100000;
  var lines = [], found = [], active = -1, shownTerm = '', seq = 0, timer = null;

  function money(v) { return '$ ' + nf.format(v); }
  function r2(v) { return Math.round((v + Number.EPSILON) * 100) / 100; }
  function set(id, txt) { var e = document.getElementById(id); if (e) { e.textContent = txt; } }
  function el(tag, cls, txt) {
    var e = document.createElement(tag);
    if (cls) { e.className = cls; }
    if (txt !== undefined) { e.textContent = txt; }
    return e;
  }
  function line(left, right) {
    var d = el('div', 'ln');
    d.appendChild(el('span', '', left));
    d.appendChild(el('span', '', right));
    return d;
  }
  function msg(text) {
    var m = document.getElementById('form-msg');
    m.textContent = text;
    m.classList.remove('rg-shake'); void m.offsetWidth; m.classList.add('rg-shake');
  }

  /* Totales y pantalla del cliente */
  function calc() {
    var sub = 0, list = document.getElementById('pv-lines');
    list.textContent = '';
    lines.forEach(function (l) {
      var s = r2(l.price * l.qty);
      if (l.subEl) { l.subEl.textContent = money(s); }
      sub += s;
      list.appendChild(line(l.qty + ' x ' + l.name, money(s)));
    });
    if (!lines.length) { list.appendChild(line('Esperando productos...', '')); }
    var opt = sel.options[sel.selectedIndex];
    var picked = !!opt && opt.value !== '';
    var agent = picked && opt.getAttribute('data-agent') === 'true';
    var iva = r2(sub * IVA), rete = agent ? r2(iva * RETE) : 0, total = r2(sub + iva - rete);
    set('pv-sub', money(sub));
    set('pv-iva', money(iva));
    set('pv-rete', '- ' + money(rete));
    set('pv-total', money(total));
    set('pv-client', picked ? opt.getAttribute('data-name') : 'SIN CLIENTE');
    set('pv-agent', picked ? (agent ? 'AGENTE RETENEDOR' : 'NO RETENEDOR') : '\u2014');
  }

  /* Líneas de la factura */
  function render() {
    box.textContent = '';
    lines.forEach(function (l, i) {
      var row = el('div', 'rg-itemrow');
      row.appendChild(el('span', 'n', String(i + 1)));

      var name = el('div', 'name');
      name.appendChild(el('b', '', l.name));
      name.appendChild(el('small', 'd-block text-muted', l.code));
      var hid = document.createElement('input');
      hid.type = 'hidden'; hid.name = 'productId'; hid.value = l.id;
      name.appendChild(hid);
      row.appendChild(name);

      row.appendChild(el('div', 'pos-price', money(l.price)));

      var qwrap = el('div');
      var qin = document.createElement('input');
      qin.type = 'number'; qin.name = 'quantity'; qin.className = 'rg-in';
      qin.min = 1; qin.max = MAX_QTY; qin.value = l.qty;
      qin.setAttribute('aria-label', 'Cantidad de ' + l.name);
      qin.addEventListener('input', function () {
        var v = parseInt(qin.value, 10);
        l.qty = v > 0 ? Math.min(v, MAX_QTY) : 0;
        calc();
      });
      qwrap.appendChild(qin);
      row.appendChild(qwrap);

      l.subEl = el('span', 'sub', '$ 0,00');
      row.appendChild(l.subEl);

      var x = el('button', 'rg-x', '\u00d7');
      x.type = 'button'; x.title = 'Quitar'; x.setAttribute('aria-label', 'Quitar l\u00ednea');
      x.addEventListener('click', function () {
        lines.splice(lines.indexOf(l), 1);
        render();
        q.focus();
      });
      row.appendChild(x);
      box.appendChild(row);
    });
    calc();
  }

  function pick(p) {
    var existing = lines.filter(function (l) { return l.id === p.id; })[0];
    if (existing) {
      existing.qty = Math.min(existing.qty + 1, MAX_QTY);
    } else {
      if (lines.length >= MAX) { msg('M\u00e1ximo 50 l\u00edneas por factura.'); return; }
      lines.push({ id: p.id, code: p.code, name: p.name, price: Number(p.unitPrice), qty: 1 });
    }
    document.getElementById('form-msg').textContent = '';
    q.value = '';
    hideList();
    render();
    q.focus();
  }

  /* Buscador de productos: código de barras (escáner) o inicio del nombre */
  function hideList() {
    results.hidden = true; results.textContent = '';
    found = []; active = -1; shownTerm = '';
  }
  function showList(items, term) {
    found = items; active = items.length ? 0 : -1; shownTerm = term;
    results.textContent = '';
    if (!items.length) {
      results.appendChild(el('li', 'pos-empty', 'Sin resultados'));
      results.hidden = false;
      return;
    }
    items.forEach(function (p, i) {
      var li = el('li', i === 0 ? 'is-active' : '');
      li.setAttribute('role', 'option');
      li.appendChild(el('span', '', p.name));
      li.appendChild(el('small', '', p.code + ' \u00b7 ' + money(p.unitPrice)));
      li.addEventListener('mousedown', function (e) { e.preventDefault(); pick(p); });
      results.appendChild(li);
    });
    results.hidden = false;
  }
  function search(term, done) {
    var mine = ++seq;
    fetch('/productos/buscar?q=' + encodeURIComponent(term), { headers: { 'Accept': 'application/json' } })
      .then(function (r) { return r.ok ? r.json() : []; })
      .then(function (data) { if (mine === seq) { done(data); } })
      .catch(function () { if (mine === seq) { done([]); } });
  }

  q.addEventListener('input', function () {
    clearTimeout(timer);
    var term = q.value.trim();
    if (!term) { hideList(); return; }
    timer = setTimeout(function () { search(term, function (data) { showList(data, term); }); }, 150);
  });
  q.addEventListener('keydown', function (e) {
    if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
      if (!found.length) { return; }
      e.preventDefault();
      active = (active + (e.key === 'ArrowDown' ? 1 : -1) + found.length) % found.length;
      Array.prototype.forEach.call(results.children, function (li, i) { li.classList.toggle('is-active', i === active); });
    } else if (e.key === 'Escape') {
      hideList();
    } else if (e.key === 'Enter') {
      e.preventDefault();
      var term = q.value.trim();
      if (!term) { return; }
      clearTimeout(timer);
      if (!results.hidden && found.length && shownTerm === term && active >= 0) { pick(found[active]); return; }
      search(term, function (data) {
        var exact = data.filter(function (p) { return p.code.toLowerCase() === term.toLowerCase(); })[0];
        if (exact) { pick(exact); }
        else if (data.length === 1) { pick(data[0]); }
        else { showList(data, term); }
      });
    }
  });
  q.addEventListener('blur', function () { setTimeout(hideList, 150); });

  /* Buscador de clientes por NIT / CC o nombre */
  var cq = document.getElementById('client-q');
  sel.addEventListener('change', calc);
  if (cq) {
    var all = Array.prototype.slice.call(sel.options, 1).map(function (o) {
      return { o: o, t: o.textContent.toLowerCase() };
    });
    cq.addEventListener('input', function () {
      var v = cq.value.trim().toLowerCase(), shown = [];
      all.forEach(function (x) {
        var m = !v || x.t.indexOf(v) > -1;
        x.o.hidden = !m;
        x.o.disabled = !m;
        if (m) { shown.push(x.o); }
      });
      if (v && shown.length === 1) {
        sel.value = shown[0].value;
        calc();
      }
    });
  }

  form.addEventListener('submit', function (e) {
    if (!lines.length) {
      e.preventDefault();
      msg('Agrega al menos un producto.');
    }
  });

  render();
  q.focus();
})();