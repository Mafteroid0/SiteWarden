const $ = id => document.getElementById(id)
const monitors = '/api/config/monitors'
const results = '/api/results'
const fmt = new Intl.DateTimeFormat('ru-RU', { dateStyle: 'short', timeStyle: 'medium' })
const time = value => value ? fmt.format(new Date(value)) : '—'
const escape = value => String(value ?? '—').replace(/[&<>"']/g, c =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' })[c])
const empty = message => '<p class="empty">' + escape(message) + '</p>'
let selected = null, kind = 'checks', listVersion = 0, detailVersion = 0

function notice(id, message = '') {
  $(id).textContent = message
  $(id).hidden = !message
}

async function api(path, { method = 'GET', data } = {}) {
  const response = await fetch(path, {
    method, signal: AbortSignal.timeout(7000),
    headers: data ? { 'Content-Type': 'application/json' } : {},
    body: data ? JSON.stringify(data) : undefined
  })
  if (!response.ok) {
    const problem = await response.json().catch(() => ({}))
    throw new Error(problem.detail || problem.message || 'HTTP ' + response.status)
  }
  return response.status === 204 ? null : response.json()
}

function table(headers, rows) {
  return '<table><thead><tr>' + headers.map(h => '<th scope="col">' + h + '</th>').join('') +
    '</tr></thead><tbody>' + rows.join('') + '</tbody></table>'
}
function badge(status) {
  const [label, cls] = ({ UP: ['Доступен', 'up'], DOWN: ['Недоступен', 'down'],
    UNKNOWN: ['Нет проверок', ''] })[status] ?? ['Нет данных', '']
  return '<span class="badge ' + cls + '">' + label + '</span>'
}

async function refreshList() {
  const version = ++listVersion
  $('refresh').setAttribute('aria-busy', 'true')
  try {
    const items = await api(monitors)
    const rows = await Promise.all(items.map(async m => {
      const s = await api(results + '/' + m.id + '/status').catch(() => null)
      return { m, s }
    }))
    if (version !== listVersion) return
    if (selected && !items.some(m => m.id === selected)) selectMonitor(null)
    const active = document.activeElement?.dataset.monitor
    const html = rows.length ? table(['Монитор', 'Доступность', 'Последняя проверка', 'Расписание'],
      rows.map(({ m, s }) => '<tr' + (m.id === selected ? ' class="selected"' : '') + '>' +
        '<td class="monitor"><button data-monitor="' + escape(m.id) + '">' + escape(m.name) +
        '</button><small>' + escape(m.url) + '</small></td><td>' + badge(s?.status) +
        '</td><td>' + time(s?.lastCheckedAt) + '</td><td>' +
        (m.enabled ? 'Каждые ' + escape(m.intervalSeconds) + ' с' : 'На паузе') + '</td></tr>'))
      : empty('Мониторов пока нет.')
    $('list').innerHTML = html
    if (active) $('list').querySelector('[data-monitor="' + CSS.escape(active) + '"]')?.focus({ preventScroll: true })
    $('total').textContent = items.length
    $('sync').textContent = 'Обновлено в ' + new Date().toLocaleTimeString('ru-RU')
    notice('list-error', rows.some(row => !row.s) ? 'Не удалось получить некоторые статусы из сервиса инцидентов.' : '')
  } catch (error) {
    if (version !== listVersion) return
    notice('list-error', 'Не удалось загрузить мониторы: ' + error.message)
    if ($('total').textContent === '—') $('list').replaceChildren()
  } finally {
    if (version === listVersion) $('refresh').removeAttribute('aria-busy')
  }
}

function selectMonitor(id) {
  selected = id
  detailVersion++
  $('details').hidden = !id
  if (!id) return
  kind = 'checks'
  $('detail-name').textContent = 'Загружаем…'
  $('detail-url').textContent = ''
  $('facts').replaceChildren()
  $('history').innerHTML = empty('Загружаем историю…')
  $('toggle').disabled = $('delete').disabled = true
  loadDetail()
}

async function loadDetail() {
  if (!selected) return
  const id = selected, version = ++detailVersion
  for (const key of ['checks', 'incidents']) $(key).setAttribute('aria-pressed', key === kind)
  const [info, history] = await Promise.allSettled([
    api(monitors + '/' + id), api(results + '/' + id + '/' + kind + '?limit=' + $('limit').value)
  ])
  if (version !== detailVersion) return
  if (info.status === 'fulfilled') {
    const m = info.value
    $('detail-name').textContent = m.name
    $('detail-url').textContent = m.url
    const facts = [['Тип', m.checkType], ['Интервал', m.intervalSeconds + ' с'], ['Таймаут', m.timeoutMs + ' мс'],
      ['Создан', time(m.createdAt)], ['Следующая проверка', m.enabled ? time(m.nextCheckAt) : 'На паузе']]
    $('facts').innerHTML = facts.map(([k, v]) => '<div><dt>' + k + '</dt><dd>' + escape(v) + '</dd></div>').join('')
    $('toggle').dataset.enabled = m.enabled
    $('toggle').textContent = m.enabled ? 'Поставить на паузу' : 'Возобновить'
    $('toggle').disabled = $('delete').disabled = false
    notice('detail-error')
  } else {
    $('toggle').disabled = $('delete').disabled = true
    notice('detail-error', 'Не удалось загрузить карточку: ' + info.reason.message)
  }
  if (history.status === 'rejected') {
    $('history').innerHTML = empty('Не удалось загрузить историю: ' + history.reason.message)
    return
  }
  const rows = history.value
  $('history').dataset.kind = kind
  if (!rows.length) {
    $('history').innerHTML = empty(kind === 'checks' ? 'Проверок пока нет.' : 'Инцидентов пока нет.')
  } else if (kind === 'checks') {
    const timeoutMs = info.value?.timeoutMs
    const meter = ms => Number.isFinite(timeoutMs) && timeoutMs > 0
      ? '<meter aria-hidden="true" min="0" max="' + timeoutMs + '" value="' + Math.min(ms, timeoutMs) + '"></meter>'
      : ''
    $('history').innerHTML = table(['Время', 'Результат', 'HTTP', 'Длительность', 'Ошибка'], rows.map(r =>
      '<tr><td>' + time(r.completedAt) + '</td><td>' + badge(r.successful ? 'UP' : 'DOWN') +
      '</td><td>' + escape(r.httpStatus) + '</td><td><span class="duration">' + escape(r.durationMs) +
      ' мс' + meter(r.durationMs) + '</span></td><td>' + escape(r.errorCode) + '</td></tr>'))
  } else {
    $('history').innerHTML = table(['Начало', 'Завершение', 'Состояние'], rows.map(r =>
      '<tr><td>' + time(r.openedAt) + '</td><td>' + time(r.resolvedAt) + '</td><td>' +
      (r.resolvedAt ? 'Закрыт' : '<span class="badge down">Открыт</span>') + '</td></tr>'))
  }
}

function refresh() { return Promise.all([refreshList(), loadDetail()]) }
$('refresh').onclick = refresh
$('list').onclick = e => {
  const id = e.target.closest('[data-monitor]')?.dataset.monitor
  if (!id) return
  selectMonitor(id)
  for (const row of $('list').querySelectorAll('tbody tr')) row.classList.toggle('selected', row.contains(e.target))
  $('details').scrollIntoView({ block: 'nearest' })
}
$('close-detail').onclick = () => { selectMonitor(null); $('list').querySelector('.selected')?.classList.remove('selected') }
for (const key of ['checks', 'incidents']) $(key).onclick = () => { kind = key; loadDetail() }
$('limit').onchange = loadDetail

$('add').onclick = () => { notice('form-error'); $('new-monitor').showModal() }
$('cancel-create').onclick = () => $('new-monitor').close()
$('create').onsubmit = async e => {
  e.preventDefault()
  const form = e.currentTarget, data = Object.fromEntries(new FormData(form))
  const submit = form.querySelector('.primary')
  submit.disabled = true
  try {
    data.url = data.url.trim()
    const url = new URL(data.url)
    if (!['https:', 'http:'].includes(url.protocol) || url.username || url.password)
      throw new Error('Укажите HTTP(S)-адрес без логина и пароля.')
    data.name = data.name.trim()
    if (!data.name) throw new Error('Введите название монитора.')
    const m = await api(monitors, { method: 'POST', data: { ...data, checkType: 'HTTP',
      intervalSeconds: Number(data.intervalSeconds), timeoutMs: Number(data.timeoutMs) } })
    $('new-monitor').close()
    form.reset()
    selectMonitor(m.id)
    await refreshList()
  } catch (error) { notice('form-error', error.message) }
  finally { submit.disabled = false }
}
$('toggle').onclick = async () => {
  const id = selected, enabled = $('toggle').dataset.enabled !== 'true'
  $('toggle').disabled = true
  try {
    await api(monitors + '/' + id, { method: 'PATCH', data: { enabled } })
    await refresh()
  } catch (error) {
    if (selected === id) { notice('detail-error', 'Не удалось изменить расписание: ' + error.message); $('toggle').disabled = false }
  }
}
$('delete').onclick = () => {
  notice('delete-error')
  $('delete-message').textContent = 'Новые проверки для «' + $('detail-name').textContent + '» больше не будут планироваться.'
  $('confirm-delete').dataset.id = selected
  $('confirm-delete').showModal()
}
$('cancel-delete').onclick = () => $('confirm-delete').close()
$('confirm').onclick = async () => {
  const id = $('confirm-delete').dataset.id
  $('confirm').disabled = true
  try {
    await api(monitors + '/' + id, { method: 'DELETE' })
    $('confirm-delete').close()
    if (selected === id) selectMonitor(null)
    await refreshList()
  } catch (error) { notice('delete-error', error.message) }
  finally { $('confirm').disabled = false }
}

refreshList()
setInterval(() => { if (!document.hidden && !document.querySelector('dialog[open]')) refresh() }, 15000)
