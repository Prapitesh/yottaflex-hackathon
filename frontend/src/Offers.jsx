import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, CirclePlus, FileCheck2, Search, WalletCards, X } from 'lucide-react'
import { apiRequest } from './api'
import './Offers.css'

const demoApplications = [
  { id: 'APP-DEMO-0005', requisitionId: 'REQ-2026-DEMO001', candidateId: 'CAND-DEMO001', stage: 'SELECTED' },
  { id: 'APP-DEMO-0006', requisitionId: 'REQ-2026-DEMO002', candidateId: 'CAND-DEMO002', stage: 'OFFERED' },
]
const demoCandidates = [
  { id: 'CAND-DEMO001', fullName: 'Ananya Sharma', email: 'ananya.sharma@example.test' },
  { id: 'CAND-DEMO002', fullName: 'Rahul Kumar', email: 'rahul.kumar@example.test' },
]
const demoRoles = [
  { id: 'REQ-2026-DEMO001', title: 'Senior Java Engineer', salaryMin: 900000, salaryMax: 1500000, currency: 'INR' },
  { id: 'REQ-2026-DEMO002', title: 'Product Designer', salaryMin: 800000, salaryMax: 1200000, currency: 'INR' },
]
const demoOffers = [
  { id: 'OFF-2026-DEMO001', applicationId: 'APP-DEMO-0005', salary: 1200000, currency: 'INR', status: 'PENDING_APPROVAL', validUntil: '2026-11-25' },
  { id: 'OFF-2026-DEMO002', applicationId: 'APP-DEMO-0006', salary: 1050000, currency: 'INR', status: 'ISSUED', approvedBy: 'Talent Director', issuedAt: '2026-11-13T10:00:00+05:30', validUntil: '2026-11-20' },
]
const statusLabels = { DRAFT: 'Draft', PENDING_APPROVAL: 'Pending approval', APPROVED: 'Approved', ISSUED: 'Issued', ACCEPTED: 'Accepted', REJECTED: 'Rejected', EXPIRED: 'Expired', WITHDRAWN: 'Withdrawn' }
const activeStatuses = ['DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'ISSUED']
const initialForm = { applicationId: '', salary: '', validUntil: '', actor: 'admin@yottaflex.ai' }
const todayDate = new Date().toISOString().slice(0, 10)

function money(value, currency = 'INR') {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency, maximumFractionDigits: 0 }).format(Number(value || 0))
}

function dateLabel(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat('en-IN', { day: '2-digit', month: 'short', year: 'numeric' }).format(new Date(value))
}

export default function Offers() {
  const [applications, setApplications] = useState([])
  const [candidates, setCandidates] = useState([])
  const [roles, setRoles] = useState([])
  const [offers, setOffers] = useState([])
  const [loading, setLoading] = useState(true)
  const [apiOnline, setApiOnline] = useState(false)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState(initialForm)
  const [busy, setBusy] = useState(false)
  const [busyId, setBusyId] = useState('')
  const [notice, setNotice] = useState('')

  async function loadData() {
    try {
      const [apps, people, jobs] = await Promise.all([
        apiRequest('/applications'), apiRequest('/candidates'), apiRequest('/requisitions'),
      ])
      const offerApplications = apps.filter((item) => ['SELECTED','OFFERED','HIRED','REJECTED','WITHDRAWN'].includes(item.stage))
      const grouped = await Promise.all(offerApplications.map((application) =>
        apiRequest(`/applications/${application.id}/offers`)
      ))
      setApplications(apps)
      setCandidates(people)
      setRoles(jobs)
      setOffers(grouped.flat())
      setApiOnline(true)
      setNotice('')
    } catch {
      setApplications(demoApplications)
      setCandidates(demoCandidates)
      setRoles(demoRoles)
      setOffers(demoOffers)
      setApiOnline(false)
      setNotice('Backend is not connected. Showing sample offers in read-only demo mode.')
    } finally {
      setLoading(false)
    }
  }

  // This effect intentionally starts an asynchronous API synchronization on mount.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { loadData() }, [])

  const candidateMap = useMemo(() => new Map(candidates.map((item) => [item.id, item])), [candidates])
  const roleMap = useMemo(() => new Map(roles.map((item) => [item.id, item])), [roles])
  const applicationMap = useMemo(() => new Map(applications.map((item) => [item.id, item])), [applications])
  const selectedApplications = applications.filter((item) => item.stage === 'SELECTED'
    && !offers.some((offer) => offer.applicationId === item.id && activeStatuses.includes(offer.status)))
  const filtered = useMemo(() => offers.filter((offer) => {
    const app = applicationMap.get(offer.applicationId)
    const candidate = candidateMap.get(app?.candidateId)
    const role = roleMap.get(app?.requisitionId)
    const text = `${offer.id} ${candidate?.fullName || ''} ${role?.title || ''}`.toLowerCase()
    return text.includes(query.toLowerCase()) && (statusFilter === 'ALL' || offer.status === statusFilter)
  }), [offers, applicationMap, candidateMap, roleMap, query, statusFilter])

  const pendingCount = offers.filter((item) => ['DRAFT','PENDING_APPROVAL','APPROVED'].includes(item.status)).length
  const issuedCount = offers.filter((item) => item.status === 'ISSUED').length
  const acceptedCount = offers.filter((item) => item.status === 'ACCEPTED').length
  const totalValueByCurrency = offers.filter((item) => activeStatuses.includes(item.status))
    .reduce((totals, item) => ({ ...totals, [item.currency]: (totals[item.currency] || 0) + Number(item.salary || 0) }), {})
  const totalValueLabel = Object.entries(totalValueByCurrency)
    .map(([currency, amount]) => money(amount, currency)).join(' | ') || '—'

  function updateForm(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  const selectedApplication = applications.find((item) => item.id === form.applicationId)
  const selectedRole = roles.find((item) => item.id === selectedApplication?.requisitionId)

  async function createOffer(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const created = await apiRequest('/offers', {
        method: 'POST',
        body: JSON.stringify({
          applicationId: form.applicationId,
          salary: Number(form.salary),
          currency: selectedRole.currency,
          validUntil: form.validUntil,
          actor: form.actor,
        }),
      })
      setOffers((current) => [created, ...current])
      setShowCreate(false)
      setForm(initialForm)
      setNotice('Draft offer created. Submit it for approval to continue.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  async function runAction(offer, action, response) {
    const actor = window.prompt('Enter your work email / actor identity:')
    if (!actor?.trim()) return
    setBusyId(offer.id)
    try {
      const path = `/offers/${offer.id}/${action}`
      const body = action === 'response'
        ? { actor: actor.trim(), response }
        : { actor: actor.trim() }
      const updated = await apiRequest(path, { method: 'POST', body: JSON.stringify(body) })
      setOffers((current) => current.map((item) => item.id === offer.id ? updated : item))
      if (action === 'issue') {
        setApplications((current) => current.map((item) => item.id === offer.applicationId
          ? { ...item, stage: 'OFFERED' } : item))
      }
      if (action === 'response') {
        const nextStage = response === 'ACCEPTED' ? 'HIRED' : 'REJECTED'
        setApplications((current) => current.map((item) => item.id === offer.applicationId
          ? { ...item, stage: nextStage } : item))
      }
      setNotice(`Offer ${action === 'response' ? response.toLowerCase() : action.replaceAll('-', ' ')} successfully.`)
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  return (
    <div className="page-content offers-page">
      <section className="module-heading">
        <div><div className="eyebrow">TALENT OPERATIONS / WORKSPACE</div><h1>Offers</h1><p>Prepare, approve, issue and track candidate employment offers.</p></div>
        <button className="button-primary" onClick={() => setShowCreate(true)} disabled={!apiOnline || !selectedApplications.length}><CirclePlus size={16} /> Create offer</button>
      </section>

      {notice && <div className={`connection-notice ${apiOnline ? 'success' : 'warning'}`}><AlertCircle size={16} /><span>{notice}</span>{!apiOnline && <button onClick={() => { setLoading(true); loadData() }}>Retry connection</button>}<button className="notice-close" aria-label="Dismiss" onClick={() => setNotice('')}><X size={15} /></button></div>}

      <section className="offer-stats">
        <div><span>Total offers</span><strong>{offers.length}</strong><small>All current and historical offers</small></div>
        <div><span>Awaiting action</span><strong>{pendingCount}</strong><small>Draft, approval or issue steps</small></div>
        <div><span>Issued to candidates</span><strong>{issuedCount}</strong><small>Awaiting candidate response</small></div>
        <div><span>Accepted</span><strong>{acceptedCount}</strong><small>Successful offer outcomes</small></div>
      </section>

      <section className="offer-value-strip"><div className="offer-value-icon"><WalletCards size={18} /></div><div><span>Active offer value</span><strong>{totalValueLabel}</strong></div><small>Active offer value, grouped by currency</small></section>

      <section className="panel offers-table-panel">
        <div className="offers-toolbar"><div><h2>Offer register</h2><p>{filtered.length} offers shown</p></div><div className="offers-filters"><label className="offer-search"><Search size={15} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search candidate, role or offer ID..." /></label><select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}><option value="ALL">All statuses</option>{Object.keys(statusLabels).map((status) => <option key={status} value={status}>{statusLabels[status]}</option>)}</select></div></div>
        <div className="table-wrap"><table><thead><tr><th>CANDIDATE / ROLE</th><th>OFFER ID</th><th>ANNUAL SALARY</th><th>STATUS</th><th>VALID UNTIL</th><th>APPROVAL / ISSUE</th><th>ACTIONS</th></tr></thead><tbody>
          {loading ? <tr><td colSpan="7" className="empty-state">Loading offers…</td></tr> : filtered.map((offer) => {
            const app = applicationMap.get(offer.applicationId)
            const candidate = candidateMap.get(app?.candidateId)
            const role = roleMap.get(app?.requisitionId)
            const busyRow = busyId === offer.id
            return <tr key={offer.id}><td><div className="offer-candidate"><strong>{candidate?.fullName || app?.candidateId || offer.applicationId}</strong><small>{role?.title || app?.requisitionId}</small></div></td><td><span className="offer-id">{offer.id}</span></td><td><strong className="offer-salary">{money(offer.salary, offer.currency)}</strong></td><td><span className={`offer-status ${offer.status.toLowerCase()}`}>{statusLabels[offer.status] || offer.status}</span></td><td>{dateLabel(offer.validUntil)}</td><td><div className="offer-approval"><span>{offer.approvedBy ? `Approved by ${offer.approvedBy}` : 'Not approved'}</span><small>{offer.issuedAt ? `Issued ${dateLabel(offer.issuedAt)}` : 'Not issued'}</small></div></td><td><div className="offer-actions">{offer.status === 'DRAFT' && <button disabled={!apiOnline || busyRow} onClick={() => runAction(offer,'submit-for-approval')}>Submit</button>}{offer.status === 'PENDING_APPROVAL' && <button disabled={!apiOnline || busyRow} onClick={() => runAction(offer,'approve')}>Approve</button>}{offer.status === 'APPROVED' && <button disabled={!apiOnline || busyRow} onClick={() => runAction(offer,'issue')}>Issue</button>}{offer.status === 'ISSUED' && <><button disabled={!apiOnline || busyRow} onClick={() => runAction(offer,'response','ACCEPTED')}>Accept</button><button className="offer-reject" disabled={!apiOnline || busyRow} onClick={() => runAction(offer,'response','REJECTED')}>Reject</button></>}</div></td></tr>
          })}
          {!loading && filtered.length === 0 && <tr><td colSpan="7" className="empty-state">No offers match your filters.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {filtered.length} of {offers.length} offers</span><span><FileCheck2 size={13} /> Approval and candidate decisions are audited</span></div>
      </section>

      {showCreate && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreate(false) }}><section className="create-modal offer-modal" role="dialog" aria-modal="true" aria-labelledby="offer-modal-title"><div className="modal-heading"><div><span className="eyebrow">COMPENSATION PROPOSAL</span><h2 id="offer-modal-title">Create draft offer</h2><p>Offer compensation must remain within the requisition's approved salary band.</p></div><button className="modal-close" onClick={() => setShowCreate(false)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={createOffer}><div className="form-grid">
          <label className="form-field span-2">Selected application<select required name="applicationId" value={form.applicationId} onChange={updateForm}><option value="">Choose a selected candidate</option>{selectedApplications.map((app) => { const candidate=candidateMap.get(app.candidateId); const role=roleMap.get(app.requisitionId); return <option key={app.id} value={app.id}>{candidate?.fullName || app.candidateId} | {role?.title || app.requisitionId}</option> })}</select></label>
          {selectedRole && <div className="salary-band span-2"><span>Approved salary band</span><strong>{money(selectedRole.salaryMin,selectedRole.currency)} - {money(selectedRole.salaryMax,selectedRole.currency)}</strong><small>Currency: {selectedRole.currency}</small></div>}
          <label className="form-field span-2">Annual salary<input required type="number" min={selectedRole?.salaryMin || 1} max={selectedRole?.salaryMax || undefined} step="0.01" name="salary" value={form.salary} onChange={updateForm} placeholder="e.g. 1200000" /></label>
          <label className="form-field">Offer valid until<input required type="date" min={todayDate} name="validUntil" value={form.validUntil} onChange={updateForm} /></label>
          <label className="form-field">Created by<input required name="actor" value={form.actor} onChange={updateForm} /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowCreate(false)}>Cancel</button><button type="submit" className="button-primary" disabled={busy || !apiOnline || !selectedRole}>{busy ? 'Creating…' : 'Create draft offer'}</button></div>
        </form></section></div>}
    </div>
  )
}
