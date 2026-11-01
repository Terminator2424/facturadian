(function () {
  var machine = document.getElementById('rc-machine');
  if (!machine) { return; }
  var l1 = document.getElementById('rc-l1'), l2 = document.getElementById('rc-l2');
  var state = machine.getAttribute('data-state') || 'idle';
  var greeting = machine.getAttribute('data-greeting') || 'BIENVENIDO';
  var typed = '';
  function p(n) { return String(n).padStart(2, '0'); }

  var d = new Date();
  var fecha = document.getElementById('rc-date'), nro = document.getElementById('rc-no');
  if (fecha) { fecha.textContent = p(d.getDate()) + '/' + p(d.getMonth() + 1) + '/' + d.getFullYear() + ' ' + p(d.getHours()) + ':' + p(d.getMinutes()); }
  if (nro) { nro.textContent = 'No. ' + p(d.getMonth() + 1) + p(d.getDate()) + p(d.getMinutes()) + p(d.getSeconds()); }

  function setL1(t) { l1.textContent = '> ' + t; }
  function base() {
    if (state === 'error') { return 'ERROR · REINTENTE'; }
    if (state === 'ok') { return 'CUENTA CREADA · INGRESE'; }
    return greeting;
  }
  function tick() {
    if (typed) { return; }
    var n = new Date();
    l2.textContent = p(n.getHours()) + ':' + p(n.getMinutes()) + ':' + p(n.getSeconds());
  }
  setL1(base());
  tick();
  setInterval(tick, 1000);

  /* La pantalla refleja lo que se escribe */
  document.querySelectorAll('[data-lcd]').forEach(function (input) {
    var label = input.getAttribute('data-lcd'), mask = input.hasAttribute('data-lcd-mask');
    function show() { setL1(label + ': ' + (mask ? '*'.repeat(input.value.length) : input.value)); }
    input.addEventListener('focus', show);
    input.addEventListener('input', show);
    input.addEventListener('blur', function () { setL1(base()); });
  });

  /* Teclado numérico de la caja */
  document.querySelectorAll('.rc-k').forEach(function (k) {
    k.addEventListener('mousedown', function () { k.classList.add('is-down'); });
    ['mouseup', 'mouseleave'].forEach(function (ev) { k.addEventListener(ev, function () { k.classList.remove('is-down'); }); });
    k.addEventListener('click', function () {
      var key = k.getAttribute('data-k');
      if (key === 'C') { typed = ''; tick(); return; }
      if (typed.length >= 12) { return; }
      typed = (typed === '0' ? '' : typed) + key;
      l2.textContent = '$ ' + typed.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
    });
  });

  /* Mostrar u ocultar contraseña */
  document.querySelectorAll('[data-toggle-password]').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var input = document.getElementById(btn.getAttribute('data-toggle-password'));
      var show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      btn.textContent = show ? 'Ocultar' : 'Mostrar';
    });
  });

  /* Medidor de fortaleza (registro) */
  var pw = document.getElementById('password'), bar = document.getElementById('strength-bar'), txt = document.getElementById('strength-text');
  if (pw && bar) {
    pw.addEventListener('input', function () {
      var v = pw.value, score = 0;
      if (v.length >= 10) { score++; }
      if (/[a-z]/.test(v) && /[A-Z]/.test(v)) { score++; }
      if (/[0-9]/.test(v)) { score++; }
      if (/[^A-Za-z0-9]/.test(v)) { score++; }
      var colors = ['#14110f', '#e5381f', '#c98a12', '#7aa62e', '#1f7a4d'];
      var labels = ['', 'MUY DÉBIL', 'DÉBIL', 'BUENA', 'FUERTE'];
      bar.style.width = (score * 25) + '%';
      bar.style.background = colors[score];
      if (txt) { txt.textContent = v ? labels[score] : ''; }
    });
  }

  /* Envío */
  var form = document.querySelector('form.rc-form');
  if (form) {
    form.addEventListener('submit', function (e) {
      var c = document.getElementById('confirm'), msg = document.getElementById('confirm-msg');
      if (pw && c && pw.value !== c.value) {
        e.preventDefault();
        if (msg) { msg.textContent = 'Las contraseñas no coinciden.'; }
        state = 'error';
        machine.setAttribute('data-state', 'error');
        setL1('NO COINCIDEN');
        c.focus();
        return;
      }
      setL1('PROCESANDO...');
      var b = form.querySelector('button[type=submit]');
      if (b) { setTimeout(function () { b.disabled = true; }, 0); }
    });
  }
  window.addEventListener('pageshow', function () {
    document.querySelectorAll('button[type=submit]:disabled').forEach(function (b) { b.disabled = false; });
  });
})();