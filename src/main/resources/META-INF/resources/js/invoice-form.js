(function () {
  var form = document.getElementById('invoice-form');
  if (!form) { return; }
  var box = document.getElementById('rg-rows'), sel = document.getElementById('clientId');
  var nf = new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
  var IVA = 0.19, RETE = 0.15, MAX = 50;

  function money(v) { return '$ ' + nf.format(v); }
  function r2(v) { return Math.round((v + Number.EPSILON) * 100) / 100; }
  function set(id, txt) { var e = document.getElementById(id); if (e) { e.textContent = txt; } }
  function line(left, right) {
    var d = document.createElement('div'); d.className = 'ln';
    var a = document.createElement('span'); a.textContent = left;
    var b = document.createElement('span'); b.textContent = right;
    d.appendChild(a); d.appendChild(b);
    return d;
  }

  function calc() {
    var sub = 0, list = document.getElementById('pv-lines');
    list.textContent = '';
    box.querySelectorAll('.rg-itemrow').forEach(function (row, i) {
      row.querySelector('.n').textContent = i + 1;
      var name = row.querySelector('[name=productName]').value.trim();
      var price = parseFloat(row.querySelector('[name=unitPrice]').value) || 0;
      var qty = parseInt(row.querySelector('[name=quantity]').value, 10) || 0;
      var s = r2(price * qty);
      row.querySelector('.sub').textContent = money(s);
      if (name || price) {
        sub += s;
        list.appendChild(line(qty + ' x ' + (name || '—'), money(s)));
      }
    });
    if (!list.children.length) { list.appendChild(line('Esperando productos...', '')); }
    var opt = sel.options[sel.selectedIndex];
    var picked = !!opt && opt.value !== '';
    var agent = picked && opt.getAttribute('data-agent') === 'true';
    var iva = r2(sub * IVA), rete = agent ? r2(iva * RETE) : 0, total = r2(sub + iva - rete);
    set('pv-sub', money(sub));
    set('pv-iva', money(iva));
    set('pv-rete', '- ' + money(rete));
    set('pv-total', money(total));
    set('pv-client', picked ? opt.getAttribute('data-name') : 'SIN CLIENTE');
    set('pv-agent', picked ? (agent ? 'AGENTE RETENEDOR' : 'NO RETENEDOR') : '—');
  }

  function newRow() {
    var d = document.createElement('div');
    d.className = 'rg-itemrow';
    d.innerHTML =
      '<span class="n"></span>' +
      '<div class="name"><input type="text" name="productName" class="rg-in" maxlength="150" placeholder="Producto o servicio" aria-label="Producto"></div>' +
      '<div><input type="number" name="unitPrice" class="rg-in" min="0.01" step="0.01" placeholder="0,00" aria-label="Precio unitario"></div>' +
      '<div><input type="number" name="quantity" class="rg-in" min="1" max="100000" value="1" aria-label="Cantidad"></div>' +
      '<span class="sub">$ 0,00</span>' +
      '<button type="button" class="rg-x" aria-label="Quitar línea" title="Quitar">×</button>';
    return d;
  }

  box.addEventListener('input', calc);
  sel.addEventListener('change', calc);
  box.addEventListener('click', function (e) {
    var x = e.target.closest('.rg-x');
    if (!x) { return; }
    var row = x.closest('.rg-itemrow');
    if (box.querySelectorAll('.rg-itemrow').length > 1) {
      row.remove();
    } else {
      row.querySelectorAll('input').forEach(function (i) { i.value = i.name === 'quantity' ? '1' : ''; });
    }
    calc();
  });
  document.getElementById('add-row').addEventListener('click', function () {
    if (box.querySelectorAll('.rg-itemrow').length >= MAX) { return; }
    var row = newRow();
    box.appendChild(row);
    calc();
    row.querySelector('[name=productName]').focus();
  });

  /* NUEVO: Buscador de clientes por NIT / CC o nombre */
  var q = document.getElementById('client-q');
  if (q) {
    // Guardamos todas las opciones (ignorando la primera que dice "Seleccione un cliente")
    var all = Array.prototype.slice.call(sel.options, 1).map(function (o) {
      return { o: o, t: o.textContent.toLowerCase() };
    });
    
    q.addEventListener('input', function () {
      var v = q.value.trim().toLowerCase(), shown = [];
      all.forEach(function (x) {
        var m = !v || x.t.indexOf(v) > -1;
        x.o.hidden = !m;
        x.o.disabled = !m;
        if (m) { shown.push(x.o); }
      });
      // Si solo queda 1 resultado, lo seleccionamos automáticamente y recalculamos
      if (v && shown.length === 1) {
        sel.value = shown[0].value;
        calc();
      }
    });
  }

  form.addEventListener('submit', function (e) {
    var ok = false;
    box.querySelectorAll('.rg-itemrow').forEach(function (row) {
      if (row.querySelector('[name=productName]').value.trim() && row.querySelector('[name=unitPrice]').value) { ok = true; }
    });
    var msg = document.getElementById('form-msg');
    if (!ok) {
      e.preventDefault();
      msg.textContent = 'Agrega al menos un producto con su precio.';
      msg.classList.remove('rg-shake'); void msg.offsetWidth; msg.classList.add('rg-shake');
    }
  });
  calc();
})();