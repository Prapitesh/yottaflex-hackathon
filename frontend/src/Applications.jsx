import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, ArrowRight, ClipboardList, FilePlus2, Search, ShieldCheck, X } from 'lucide-react'
import { apiRequest } from './api'
import './Applications.css'

const demoCandidates = [
  { id: 'CAND-DEMO001', fullName: 'Ananya Sharma', email: 'ananya.sharma@example.test' },
  { id: 'CAND-DEMO002', fullName: 'Rahul Kumar', email: 'rahul.kumar@example.test' },
  { id: 'CAND-DEMO003', fullName: 'Priya Mehta', email: 'priya.mehta@example.test' },
]
const demoRequisitions = [
  { id: 'REQ-2026-DEMO001', title: 'Senior Java Engineer', status: 'APPROVED' },
  { id: 'REQ-2026-DEMO002', title: 'Product Designer', status: 'APPROVED' },
]
const demoApplications = [
  { id: 'APP-DEMO-0001', requisitionId: 'REQ-2026-DEMO001', candidateId: 'CAND-DEMO001', stage: 'APPLIED', appliedAt: '2026-11-15T08:30:00Z' },
  { id: 'APP-DEMO-0002', requisitionId: 'REQ-2026-DEMO002', candidateId: 'CAND-DEMO002', stage: 'SCREENING', appliedAt: '2026-11-14T11:10:00Z' },
  { id: 'APP-DEMO-0003', requisitionId: 'REQ-2026-DEMO001', candidateId: 'CAND-DEMO003', stage: 'SHORTLISTED', appliedAt: '2026-11-13T09:20:00Z' },
]
const stages = ['APPLIED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN']
const labels = { APPLIED: 'Applied', SCREENING: 'Screening', SHORTLISTED: 'Shortlisted', INTERVIEW: 'Interview', SELECTED: 'Selected', OFFERED: 'Offer issued', HIRED: 'Hired', REJECTED: 'Rejected', WITHDRAWN: 'Withdrawn' }
const initialForm = { requisitionId: '', candidateId: '', resumeId: '' }

function dateLabel(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value))
}

export default function Applications() {
  const [items, setItems] = useState([])
  const [candidates, setCandidates] = useState([])
  const [requisitions, setRequisitions] = useState([])
  const [loading, setLoading] = useState(true)
  const [apiOnline, setApiOnline] = useState(false)
  const [query, setQuery] = useState('')
  const [stageFilter, setStageFilter] = useState('ALL')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(initialForm)
  const [busyId, setBusyId] = useState('')
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState('')
  const [screeningResult, setScreeningResult] = useState(null)

  async function loadData() {
    try {
      const [apps, people, roles] = await Promise.all([
        apiRequest('/applications'),
        apiRequest('/candidates'),
        apiRequest('/requisitions'),
      ])
      setItems(apps)
      setCandidates(people)
      setRequisitions(roles)
      setApiOnline(true)
      setNotice('')
    } catch {
      setItems(demoApplications)
      setCandidates(demoCandidates)
      setRequisitions(demoRequisitions)
      setApiOnline(false)
      setNotice('Backend is not connected. Showing sample applications in read-only demo mode.')
    } finally {
      setLoading(false)
    }
  }

  // This effect intentionally starts an asynchronous API synchronization on mount.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { loadData() }, [])

  const candidateMap = useMemo(() => new Map(candidates.map((item) => [item.id, item])), [candidates])
  const requisitionMap = useMemo(() => new Map(requisitions.map((item) => [item.id, item])), [requisitions])
  const filtered = useMemo(() => items.filter((item) => {
    const candidate = candidateMap.get(item.candidateId)
    const role = requisitionMap.get(item.requisitionId)
    const text = `${candidate?.fullName || ''} ${candidate?.email || ''} ${role?.title || ''} ${item.id}`.toLowerCase()
    return text.includes(query.toLowerCase()) && (stageFilter === 'ALL' || item.stage === stageFilter)
  }), [items, candidateMap, requisitionMap, query, stageFilter])

  const activeCount = items.filter((item) => !['HIRED', 'REJECTED', 'WITHDRAWN'].includes(item.stage)).length
  const screeningCount = items.filter((item) => item.stage === 'SCREENING').length
  const shortlistedCount = items.filter((item) => item.stage === 'SHORTLISTED').length

  function updateForm(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function createApplication(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const created = await apiRequest('/applications', {
        method: 'POST',
        body: JSON.stringify({ ...form, resumeId: form.resumeId.trim() || null }),
      })
      setItems((current) => [created, ...current])
      setShowCreate(false)
      setForm(initialForm)
      setNotice('Application created and stage history recorded.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  async function screenApplication(application) {
    setBusyId(application.id)
    try {
      const result = await apiRequest(`/applications/${application.id}/screen`, { method: 'POST' })
      setItems((current) => current.map((item) => item.id === application.id ? { ...item, stage: result.stage } : item))
      setScreeningResult({ ...result, candidateName: candidateMap.get(application.candidateId)?.fullName || application.candidateId })
      setNotice(`Screening completed: ${result.decision} · ${result.score}/100`)
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  async function decideApplication(application, targetStage) {
    const actor = window.prompt('Enter recruiter / actor identity:')
    if (!actor?.trim()) return
    const reason = window.prompt(`Reason for ${targetStage.toLowerCase()}:`)
    if (!reason?.trim()) return
    setBusyId(application.id)
    try {
      const updated = await apiRequest(`/applications/${application.id}/decision`, {
        method: 'POST',
        body: JSON.stringify({ targetStage, actor: actor.trim(), reason: reason.trim() }),
      })
      setItems((current) => current.map((item) => item.id === application.id ? updated : item))
      setNotice(`Application moved to ${labels[targetStage]}.`)
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  async function withdrawApplication(application) {
    const actor = window.prompt('Enter candidate / actor identity:')
    if (!actor?.trim()) return
    const reason = window.prompt('Reason for withdrawal:')
    if (!reason?.trim()) return
    setBusyId(application.id)
    try {
      const updated = await apiRequest(`/applications/${application.id}/withdraw`, {
        method: 'POST',
        body: JSON.stringify({ actor: actor.trim(), reason: reason.trim() }),
      })
      setItems((current) => current.map((item) => item.id === application.id ? updated : item))
      setNotice('Application withdrawn.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  return (
    <div className="page-content applications-page">
      <section className="module-heading">
        <div><div className="eyebrow">TALENT OPERATIONS / WORKSPACE</div><h1>Applications</h1><p>Track candidate progress from application through screening and hiring decisions.</p></div>
        <button className="button-primary" onClick={() => setShowCreate(true)} disabled={!apiOnline}><FilePlus2 size={16} /> Create application</button>
      </section>

      {notice && <div className={`connection-notice ${apiOnline ? 'success' : 'warning'}`}><AlertCircle size={16} /><span>{notice}</span>{!apiOnline && <button onClick={() => { setLoading(true); loadData() }}>Retry connection</button>}<button className="notice-close" aria-label="Dismiss" onClick={() => setNotice('')}><X size={15} /></button></div>}

      {screeningResult && <section className="screening-result-card"><div className="screening-result-icon"><ShieldCheck size={19} /></div><div className="screening-result-main"><strong>Screening result · {screeningResult.candidateName}</strong><span>{screeningResult.decision} decision · Score {screeningResult.score}/100</span><ul>{screeningResult.reasons.map((reason, index) => <li key={index}>{reason}</li>)}</ul></div><button onClick={() => setScreeningResult(null)} aria-label="Dismiss result"><X size={15} /></button></section>}

      <section className="application-stats">
        <div><span>Total applications</span><strong>{items.length}</strong><small>All candidate submissions</small></div>
        <div><span>Active pipeline</span><strong>{activeCount}</strong><small>Not yet in a terminal stage</small></div>
        <div><span>In screening</span><strong>{screeningCount}</strong><small>Awaiting recruiter decision</small></div>
        <div><span>Shortlisted</span><strong>{shortlistedCount}</strong><small>Ready for interview scheduling</small></div>
      </section>

      <section className="panel applications-table-panel">
        <div className="applications-toolbar"><div><h2>Application pipeline</h2><p>{filtered.length} records shown</p></div><div className="applications-filters"><label className="application-search"><Search size={15} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search candidate, role or ID..." /></label><select value={stageFilter} onChange={(e) => setStageFilter(e.target.value)}><option value="ALL">All stages</option>{stages.map((stage) => <option key={stage} value={stage}>{labels[stage]}</option>)}</select></div></div>
        <div className="table-wrap"><table><thead><tr><th>CANDIDATE</th><th>POSITION</th><th>APPLICATION ID</th><th>STAGE</th><th>APPLIED</th><th>NEXT ACTION</th></tr></thead><tbody>
          {loading ? <tr><td colSpan="6" className="empty-state">Loading applications…</td></tr> : filtered.map((item) => {
            const candidate = candidateMap.get(item.candidateId)
            const role = requisitionMap.get(item.requisitionId)
            const busyRow = busyId === item.id
            return <tr key={item.id}><td><div className="candidate-cell"><div className="application-avatar">{(candidate?.fullName || item.candidateId).split(' ').map((part) => part[0]).slice(0,2).join('').toUpperCase()}</div><div className="application-candidate"><strong>{candidate?.fullName || item.candidateId}</strong><small>{candidate?.email || 'Candidate profile unavailable'}</small></div></div></td><td><strong className="application-role">{role?.title || item.requisitionId}</strong></td><td><span className="application-id">{item.id}</span></td><td><span className={`application-stage ${item.stage.toLowerCase()}`}>{labels[item.stage] || item.stage}</span></td><td>{dateLabel(item.appliedAt)}</td><td><div className="application-actions">{item.stage === 'APPLIED' && <button disabled={!apiOnline || busyRow} onClick={() => screenApplication(item)}>{busyRow ? '…' : 'Screen'}</button>}{item.stage === 'SCREENING' && <><button disabled={!apiOnline || busyRow} onClick={() => decideApplication(item, 'SHORTLISTED')}>Shortlist</button><button className="reject-application" disabled={!apiOnline || busyRow} onClick={() => decideApplication(item, 'REJECTED')}>Reject</button></>}{['SHORTLISTED','INTERVIEW'].includes(item.stage) && <button className="reject-application" disabled={!apiOnline || busyRow} onClick={() => decideApplication(item, 'REJECTED')}>Reject</button>}{!['HIRED','REJECTED','WITHDRAWN'].includes(item.stage) && <button className="withdraw-application" disabled={!apiOnline || busyRow} onClick={() => withdrawApplication(item)}>Withdraw</button>}</div></td></tr>
          })}
          {!loading && filtered.length === 0 && <tr><td colSpan="6" className="empty-state">No applications match your filters.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {filtered.length} of {items.length} applications</span><span><ClipboardList size={13} /> Stage history is recorded by the backend</span></div>
      </section>

      {showCreate && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreate(false) }}><section className="create-modal application-modal" role="dialog" aria-modal="true" aria-labelledby="application-modal-title"><div className="modal-heading"><div><span className="eyebrow">NEW CANDIDATE SUBMISSION</span><h2 id="application-modal-title">Create application</h2><p>Link an existing candidate to an approved requisition.</p></div><button className="modal-close" onClick={() => setShowCreate(false)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={createApplication}><div className="form-grid">
          <label className="form-field span-2">Approved requisition<select required name="requisitionId" value={form.requisitionId} onChange={updateForm}><option value="">Select an open requisition</option>{requisitions.filter((item) => item.status === 'APPROVED').map((item) => <option key={item.id} value={item.id}>{item.title} · {item.id}</option>)}</select></label>
          <label className="form-field span-2">Candidate<select required name="candidateId" value={form.candidateId} onChange={updateForm}><option value="">Select a candidate profile</option>{candidates.map((item) => <option key={item.id} value={item.id}>{item.fullName} · {item.email}</option>)}</select></label>
          <label className="form-field span-2">Resume ID (optional)<input name="resumeId" value={form.resumeId} onChange={updateForm} maxLength="40" placeholder="RES-2026-000001" /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowCreate(false)}>Cancel</button><button type="submit" className="button-primary" disabled={busy || !apiOnline || !requisitions.some((item) => item.status === 'APPROVED') || !candidates.length}>{busy ? 'Creating…' : 'Create application'} <ArrowRight size={14} /></button></div>
        </form></section></div>}
    </div>
  )
}
