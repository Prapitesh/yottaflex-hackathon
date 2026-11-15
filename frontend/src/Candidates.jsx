import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, BriefcaseBusiness, CirclePlus, Edit2, Mail, MapPin, Phone, Search, Users, X } from 'lucide-react'
import { apiRequest } from './api'
import './Candidates.css'

const demoCandidates = [
  { id: 'CAND-DEMO001', fullName: 'Ananya Sharma', email: 'ananya.sharma@example.test', phone: '+91 98765 43210', currentLocation: 'Hyderabad', totalExperienceMonths: 48 },
  { id: 'CAND-DEMO002', fullName: 'Rahul Kumar', email: 'rahul.kumar@example.test', phone: '+91 98765 12345', currentLocation: 'Bengaluru', totalExperienceMonths: 24 },
  { id: 'CAND-DEMO003', fullName: 'Priya Mehta', email: 'priya.mehta@example.test', phone: '+91 99887 77665', currentLocation: 'Pune', totalExperienceMonths: 36 },
  { id: 'CAND-DEMO004', fullName: 'Arjun Verma', email: 'arjun.verma@example.test', phone: '', currentLocation: 'Remote - India', totalExperienceMonths: 18 },
  { id: 'CAND-DEMO005', fullName: 'Neha Singh', email: 'neha.singh@example.test', phone: '+91 91234 56789', currentLocation: 'Hyderabad', totalExperienceMonths: 12 },
]

const emptyForm = { fullName: '', email: '', phone: '', currentLocation: '', totalExperienceMonths: '0' }
const initials = (name = '') => name.split(' ').filter(Boolean).slice(0, 2).map((part) => part[0]).join('').toUpperCase()
const years = (months = 0) => {
  const value = Number(months || 0)
  const fullYears = Math.floor(value / 12)
  const remaining = value % 12
  return fullYears ? `${fullYears}y ${remaining ? `${remaining}m` : ''}`.trim() : `${remaining}m`
}

export default function Candidates() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [apiOnline, setApiOnline] = useState(false)
  const [query, setQuery] = useState('')
  const [locationFilter, setLocationFilter] = useState('ALL')
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState('')
  const [form, setForm] = useState(emptyForm)
  const [busy, setBusy] = useState(false)
  const [notice, setNotice] = useState('')

  async function loadCandidates() {
    try {
      const data = await apiRequest('/candidates')
      setItems(data)
      setApiOnline(true)
      setNotice('')
    } catch {
      setItems(demoCandidates)
      setApiOnline(false)
      setNotice('Backend is not connected. Showing sample candidate profiles in read-only demo mode.')
    } finally {
      setLoading(false)
    }
  }

  // This effect intentionally starts an asynchronous API synchronization on mount.
  // oxlint-disable-next-line react/set-state-in-effect
  useEffect(() => { loadCandidates() }, [])

  const locations = useMemo(() => [...new Set(items.map((item) => item.currentLocation).filter(Boolean))].sort(), [items])
  const filtered = useMemo(() => items.filter((item) => {
    const text = `${item.fullName} ${item.email} ${item.id} ${item.currentLocation || ''}`.toLowerCase()
    return text.includes(query.toLowerCase()) && (locationFilter === 'ALL' || item.currentLocation === locationFilter)
  }), [items, query, locationFilter])
  const totalExperience = items.reduce((sum, item) => sum + Number(item.totalExperienceMonths || 0), 0)
  const averageExperience = items.length ? (totalExperience / items.length / 12).toFixed(1) : '0.0'

  function openCreate() {
    setEditingId('')
    setForm(emptyForm)
    setShowModal(true)
  }

  function openEdit(candidate) {
    setEditingId(candidate.id)
    setForm({
      fullName: candidate.fullName || '',
      email: candidate.email || '',
      phone: candidate.phone || '',
      currentLocation: candidate.currentLocation || '',
      totalExperienceMonths: String(candidate.totalExperienceMonths ?? 0),
    })
    setShowModal(true)
  }

  function updateForm(event) {
    setForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  async function saveCandidate(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, totalExperienceMonths: Number(form.totalExperienceMonths) }
      const saved = await apiRequest(editingId ? `/candidates/${editingId}` : '/candidates', {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      setItems((current) => editingId
        ? current.map((item) => item.id === editingId ? saved : item)
        : [saved, ...current])
      setShowModal(false)
      setEditingId('')
      setForm(emptyForm)
      setNotice(editingId ? 'Candidate profile updated.' : 'Candidate profile created.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="page-content candidates-page">
      <section className="module-heading">
        <div><div className="eyebrow">TALENT OPERATIONS / WORKSPACE</div><h1>Candidates</h1><p>Maintain candidate profiles independently from job applications and hiring decisions.</p></div>
        <button className="button-primary" onClick={openCreate} disabled={!apiOnline}><CirclePlus size={16} /> Add candidate</button>
      </section>

      {notice && <div className={`connection-notice ${apiOnline ? 'success' : 'warning'}`}><AlertCircle size={16} /><span>{notice}</span>{!apiOnline && <button onClick={() => { setLoading(true); loadCandidates() }}>Retry connection</button>}<button className="notice-close" aria-label="Dismiss" onClick={() => setNotice('')}><X size={15} /></button></div>}

      <section className="candidate-stats">
        <div><div className="candidate-stat-icon blue"><Users size={17} /></div><span>Total profiles</span><strong>{items.length.toLocaleString('en-IN')}</strong><small>Candidate master records</small></div>
        <div><div className="candidate-stat-icon violet"><BriefcaseBusiness size={17} /></div><span>Experience pool</span><strong>{totalExperience.toLocaleString('en-IN')} <em>months</em></strong><small>Combined recorded experience</small></div>
        <div><div className="candidate-stat-icon green"><Users size={17} /></div><span>Average experience</span><strong>{averageExperience} <em>years</em></strong><small>Across visible profiles</small></div>
      </section>

      <section className="panel candidates-table-panel">
        <div className="candidates-toolbar"><div><h2>Candidate directory</h2><p>{filtered.length} profiles shown</p></div><div className="candidates-filters"><label className="candidate-search"><Search size={15} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search name, email or ID..." /></label><select className="location-filter" value={locationFilter} onChange={(e) => setLocationFilter(e.target.value)}><option value="ALL">All locations</option>{locations.map((location) => <option key={location} value={location}>{location}</option>)}</select></div></div>
        <div className="table-wrap"><table><thead><tr><th>CANDIDATE</th><th>CONTACT</th><th>LOCATION</th><th>EXPERIENCE</th><th>PROFILE ID</th><th /></tr></thead><tbody>
          {loading ? <tr><td colSpan="6" className="empty-state">Loading candidates…</td></tr> : filtered.map((candidate, index) => <tr key={candidate.id}><td><div className="candidate-cell"><div className={`candidate-initials avatar-${index % 5}`}>{initials(candidate.fullName)}</div><div className="candidate-name"><strong>{candidate.fullName}</strong><small>Candidate profile</small></div></div></td><td><div className="contact-stack"><span><Mail size={12} />{candidate.email}</span>{candidate.phone && <span><Phone size={12} />{candidate.phone}</span>}</div></td><td><span className="location-cell"><MapPin size={12} />{candidate.currentLocation || 'Not provided'}</span></td><td><strong className="experience-value">{years(candidate.totalExperienceMonths)}</strong></td><td><span className="candidate-id">{candidate.id}</span></td><td><button className="edit-candidate" onClick={() => openEdit(candidate)} disabled={!apiOnline} title="Edit candidate profile"><Edit2 size={15} /></button></td></tr>)}
          {!loading && filtered.length === 0 && <tr><td colSpan="6" className="empty-state">No candidate profiles match your filters.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {filtered.length} of {items.length} candidate profiles</span><span>Profile data is separate from application decisions</span></div>
      </section>

      {showModal && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowModal(false) }}><section className="create-modal candidate-modal" role="dialog" aria-modal="true" aria-labelledby="candidate-modal-title"><div className="modal-heading"><div><span className="eyebrow">CANDIDATE MASTER DATA</span><h2 id="candidate-modal-title">{editingId ? 'Edit candidate profile' : 'Add candidate'}</h2><p>Contact and experience details for the candidate record.</p></div><button className="modal-close" onClick={() => setShowModal(false)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={saveCandidate}><div className="form-grid">
          <label className="form-field span-2">Full name<input required name="fullName" value={form.fullName} onChange={updateForm} maxLength="160" placeholder="Candidate's full name" /></label>
          <label className="form-field span-2">Email address<input required type="email" name="email" value={form.email} onChange={updateForm} maxLength="254" placeholder="name@example.com" /></label>
          <label className="form-field">Phone number<input name="phone" value={form.phone} onChange={updateForm} maxLength="30" placeholder="+91 98765 43210" /></label>
          <label className="form-field">Current location<input name="currentLocation" value={form.currentLocation} onChange={updateForm} maxLength="160" placeholder="Hyderabad" /></label>
          <label className="form-field span-2">Total experience (months)<input required type="number" min="0" name="totalExperienceMonths" value={form.totalExperienceMonths} onChange={updateForm} /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowModal(false)}>Cancel</button><button type="submit" className="button-primary" disabled={busy || !apiOnline}>{busy ? 'Saving…' : editingId ? 'Save changes' : 'Create candidate'}</button></div>
        {!apiOnline && <p className="modal-hint">Connect the backend to save candidate profiles. Demo mode is read-only.</p>}
        </form></section></div>}
    </div>
  )
}
