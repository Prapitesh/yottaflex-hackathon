import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, BriefcaseBusiness, ChevronDown, CirclePlus, Clock3, Filter, MapPin, MoreHorizontal, Search, X } from 'lucide-react'
import { apiRequest } from '../../shared/api/api.js'
import './Requisitions.css'

const demoDepartments = [{ id: 'DEP-DEMO-01', name: 'Engineering', costCenter: 'ENG-01' }, { id: 'DEP-DEMO-02', name: 'Product', costCenter: 'PROD-01' }, { id: 'DEP-DEMO-03', name: 'Data & Analytics', costCenter: 'DATA-01' }]
const demoRequisitions = [
  { id: 'REQ-2026-DEMO001', departmentId: 'DEP-DEMO-01', title: 'Senior Java Engineer', employmentType: 'FULL_TIME', location: 'Hyderabad', openings: 3, salaryMin: 900000, salaryMax: 1500000, currency: 'INR', status: 'APPROVED', requestedBy: 'Engineering Lead' },
  { id: 'REQ-2026-DEMO002', departmentId: 'DEP-DEMO-02', title: 'Product Designer', employmentType: 'FULL_TIME', location: 'Bengaluru', openings: 1, salaryMin: 800000, salaryMax: 1200000, currency: 'INR', status: 'PENDING_APPROVAL', requestedBy: 'Design Lead' },
  { id: 'REQ-2026-DEMO003', departmentId: 'DEP-DEMO-03', title: 'Data Analyst', employmentType: 'FULL_TIME', location: 'Remote - India', openings: 2, salaryMin: 600000, salaryMax: 900000, currency: 'INR', status: 'DRAFT', requestedBy: 'Analytics Lead' },
  { id: 'REQ-2026-DEMO004', departmentId: 'DEP-DEMO-01', title: 'Frontend Developer', employmentType: 'CONTRACT', location: 'Hyderabad', openings: 2, salaryMin: 500000, salaryMax: 850000, currency: 'INR', status: 'CLOSED', requestedBy: 'Engineering Lead' },
]

const initialForm = { departmentId: '', title: '', description: '', employmentType: 'FULL_TIME', location: '', openings: '1', salaryMin: '', salaryMax: '', currency: 'INR', requestedBy: 'admin@yottaflex.ai' }
const statusLabels = { DRAFT: 'Draft', PENDING_APPROVAL: 'Pending approval', APPROVED: 'Open', REJECTED: 'Rejected', CLOSED: 'Closed' }

function formatMoney(value, currency = 'INR') {
  return new Intl.NumberFormat('en-IN', { style: 'currency', currency, maximumFractionDigits: 0 }).format(Number(value || 0))
}

export default function Requisitions() {
  const [items, setItems] = useState([])
  const [departments, setDepartments] = useState([])
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
      const [reqs, deps] = await Promise.all([
        apiRequest('/requisitions'),
        apiRequest('/departments'),
      ])
      setItems(reqs)
      setDepartments(deps)
      setApiOnline(true)
      setNotice('')
    } catch {
      setItems(demoRequisitions)
      setDepartments(demoDepartments)
      setApiOnline(false)
      setNotice('Backend is not connected. Showing sample requisitions until PostgreSQL is configured and the API is running.')
    } finally {
      setLoading(false)
    }
  }

  // This effect intentionally starts an asynchronous API synchronization on mount.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { loadData() }, [])

  const filtered = useMemo(() => items.filter((item) => {
    const department = departments.find((dep) => dep.id === item.departmentId)?.name || ''
    const matchesQuery = `${item.title} ${item.id} ${item.location} ${department}`.toLowerCase().includes(query.toLowerCase())
    return matchesQuery && (statusFilter === 'ALL' || item.status === statusFilter)
  }), [items, departments, query, statusFilter])

  const counts = useMemo(() => ({
    total: items.length,
    open: items.filter((item) => item.status === 'APPROVED').length,
    pending: items.filter((item) => item.status === 'PENDING_APPROVAL').length,
    draft: items.filter((item) => item.status === 'DRAFT').length,
  }), [items])

  function updateForm(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
  }

  async function createRequisition(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const created = await apiRequest('/requisitions', {
        method: 'POST',
        body: JSON.stringify({
          ...form,
          openings: Number(form.openings),
          salaryMin: Number(form.salaryMin),
          salaryMax: Number(form.salaryMax),
        }),
      })
      setItems((current) => [created, ...current])
      setShowCreate(false)
      setForm(initialForm)
      setNotice('Requisition created successfully.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  async function runAction(item, action) {
    const actor = window.prompt('Enter your work email / actor identity:')
    if (!actor?.trim()) return
    const reason = window.prompt('Enter the reason for this action:')
    if (!reason?.trim()) return
    setBusyId(item.id)
    try {
      const updated = await apiRequest(`/requisitions/${item.id}/${action}`, {
        method: 'POST',
        body: JSON.stringify({ actor: actor.trim(), reason: reason.trim() }),
      })
      setItems((current) => current.map((row) => row.id === item.id ? updated : row))
      setNotice(`Requisition ${action} action completed.`)
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  return (
    <div className="page-content requisitions-page">
      <section className="module-heading">
        <div><div className="eyebrow">TALENT OPERATIONS / WORKSPACE</div><h1>Requisitions</h1><p>Create, review and manage approved hiring requests across departments.</p></div>
        <button className="button-primary" onClick={() => setShowCreate(true)}><CirclePlus size={16} /> New requisition</button>
      </section>

      {notice && <div className={`connection-notice ${apiOnline ? 'success' : 'warning'}`}><AlertCircle size={16} /><span>{notice}</span>{!apiOnline && <button onClick={() => { setLoading(true); loadData() }}>Retry connection</button>}<button className="notice-close" aria-label="Dismiss" onClick={() => setNotice('')}><X size={15} /></button></div>}

      <section className="requisition-stats">
        <div><span>Total requisitions</span><strong>{counts.total}</strong><small>Across all departments</small></div>
        <div><span>Open roles</span><strong>{counts.open}</strong><small>Approved and accepting applications</small></div>
        <div><span>Awaiting approval</span><strong>{counts.pending}</strong><small>Require an approver decision</small></div>
        <div><span>Drafts</span><strong>{counts.draft}</strong><small>Not yet submitted</small></div>
      </section>

      <section className="panel requisition-table-panel">
        <div className="requisition-toolbar"><div><h2>All requisitions</h2><p>{filtered.length} records shown</p></div><div className="requisition-filters"><label className="req-search"><Search size={15} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search title, ID, location..." /></label><label className="filter-select"><Filter size={14} /><select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}><option value="ALL">All statuses</option><option value="DRAFT">Draft</option><option value="PENDING_APPROVAL">Pending approval</option><option value="APPROVED">Open</option><option value="REJECTED">Rejected</option><option value="CLOSED">Closed</option></select><ChevronDown size={13} /></label></div></div>
        <div className="table-wrap"><table><thead><tr><th>ROLE / REQUISITION</th><th>DEPARTMENT</th><th>LOCATION</th><th>OPENINGS</th><th>SALARY RANGE</th><th>STATUS</th><th>ACTIONS</th></tr></thead><tbody>
          {loading ? <tr><td colSpan="7" className="empty-state">Loading requisitionsâ€¦</td></tr> : filtered.map((item) => {
            const department = departments.find((dep) => dep.id === item.departmentId)?.name || 'â€”'
            const action = item.status === 'DRAFT' ? 'submit' : item.status === 'PENDING_APPROVAL' ? 'approve' : item.status === 'APPROVED' ? 'close' : ''
            return <tr key={item.id}><td><div className="req-title-cell"><div className="req-icon"><BriefcaseBusiness size={16} /></div><div><strong>{item.title}</strong><small>{item.id}</small></div></div></td><td>{department}</td><td><span className="location-cell"><MapPin size={12} />{item.location}</span></td><td>{item.openings}</td><td><strong className="salary-cell">{formatMoney(item.salaryMin, item.currency)} â€“ {formatMoney(item.salaryMax, item.currency)}</strong></td><td><span className={`req-status ${item.status.toLowerCase()}`}>{statusLabels[item.status] || item.status}</span></td><td><div className="req-actions">{action && <button disabled={!apiOnline || busyId === item.id} onClick={() => runAction(item, action)}>{busyId === item.id ? 'â€¦' : action === 'submit' ? 'Submit' : action === 'approve' ? 'Approve' : 'Close'}</button>}{item.status === 'PENDING_APPROVAL' && <button className="reject-action" disabled={!apiOnline || busyId === item.id} onClick={() => runAction(item, 'reject')}>Reject</button>}<button className="more-action" title="More details" onClick={() => setNotice(`${item.title} Â· Requested by ${item.requestedBy || 'â€”'}`)}><MoreHorizontal size={16} /></button></div></td></tr>
          })}
          {!loading && filtered.length === 0 && <tr><td colSpan="7" className="empty-state">No requisitions match your filters.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {filtered.length} of {items.length} requisitions</span><span><Clock3 size={13} /> Updated just now</span></div>
      </section>

      {showCreate && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowCreate(false) }}><section className="create-modal" role="dialog" aria-modal="true" aria-labelledby="create-title"><div className="modal-heading"><div><span className="eyebrow">HIRING REQUEST</span><h2 id="create-title">Create requisition</h2><p>Define the role, hiring plan and approved salary band.</p></div><button className="modal-close" onClick={() => setShowCreate(false)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={createRequisition}><div className="form-grid">
          <label className="form-field span-2">Job title<input required name="title" value={form.title} onChange={updateForm} placeholder="e.g. Java Backend Engineer" maxLength="160" /></label>
          <label className="form-field">Department<select required name="departmentId" value={form.departmentId} onChange={updateForm}><option value="">Select department</option>{departments.map((dep) => <option key={dep.id} value={dep.id}>{dep.name}</option>)}</select></label>
          <label className="form-field">Employment type<select name="employmentType" value={form.employmentType} onChange={updateForm}><option value="FULL_TIME">Full time</option><option value="PART_TIME">Part time</option><option value="CONTRACT">Contract</option><option value="INTERN">Intern</option></select></label>
          <label className="form-field">Location<input required name="location" value={form.location} onChange={updateForm} placeholder="Hyderabad / Remote" maxLength="160" /></label>
          <label className="form-field">Number of openings<input required type="number" min="1" name="openings" value={form.openings} onChange={updateForm} /></label>
          <label className="form-field">Minimum annual salary<input required type="number" min="0" step="0.01" name="salaryMin" value={form.salaryMin} onChange={updateForm} placeholder="600000" /></label>
          <label className="form-field">Maximum annual salary<input required type="number" min="0" step="0.01" name="salaryMax" value={form.salaryMax} onChange={updateForm} placeholder="1200000" /></label>
          <label className="form-field">Currency<select name="currency" value={form.currency} onChange={updateForm}><option value="INR">INR Â· Indian Rupee</option><option value="USD">USD Â· US Dollar</option><option value="EUR">EUR Â· Euro</option></select></label>
          <label className="form-field">Requested by<input required name="requestedBy" value={form.requestedBy} onChange={updateForm} maxLength="120" /></label>
          <label className="form-field span-2">Role description<textarea required name="description" value={form.description} onChange={updateForm} rows="3" placeholder="Responsibilities, required skills and experienceâ€¦" /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowCreate(false)}>Cancel</button><button type="submit" className="button-primary" disabled={busy || !apiOnline || !departments.length}>{busy ? 'Creatingâ€¦' : 'Create draft requisition'}</button></div>
        {!apiOnline && <p className="modal-hint">Connect the backend to save new requisitions. Demo mode is read-only.</p>}
        </form></section></div>}
    </div>
  )
}
