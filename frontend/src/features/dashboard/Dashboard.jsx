import { useEffect, useMemo, useState } from 'react'
import { BriefcaseBusiness, CalendarDays, ClipboardList, Users, WalletCards, AlertCircle, Search } from 'lucide-react'
import { apiRequest } from '../../shared/api/api.js'

const stages = ['APPLIED', 'SCREENING', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'OFFERED', 'HIRED', 'REJECTED', 'WITHDRAWN']

function MetricCard({ title, value, note, icon: Icon, color }) {
  return <article className="metric-card"><div className="metric-top"><span>{title}</span><div className={`metric-icon ${color}`}><Icon size={18} /></div></div><div className="metric-value">{value}</div><div className="metric-bottom"><span className="neutral">{note}</span></div></article>
}

export default function Dashboard() {
  const [applications, setApplications] = useState([])
  const [candidates, setCandidates] = useState([])
  const [requisitions, setRequisitions] = useState([])
  const [interviews, setInterviews] = useState([])
  const [offers, setOffers] = useState([])
  const [query, setQuery] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [apps, people, roles, offerRows] = await Promise.all([
          apiRequest('/applications'), apiRequest('/candidates'),
          apiRequest('/requisitions'), apiRequest('/offers'),
        ])
        const recent = [...apps].sort((a, b) => new Date(b.appliedAt) - new Date(a.appliedAt))
        const interviewGroups = await Promise.all(recent.slice(0, 8).map((app) =>
          apiRequest(`/interviews/by-application/${encodeURIComponent(app.id)}`).catch(() => [])
        ))
        if (!active) return
        setApplications(apps)
        setCandidates(people)
        setRequisitions(roles)
        setOffers(offerRows)
        setInterviews(interviewGroups.flat().filter((item) => item.status === 'SCHEDULED').sort((a, b) => new Date(a.scheduledStart) - new Date(b.scheduledStart)))
        setError('')
      } catch (err) {
        if (active) setError(err.message || 'Unable to load dashboard data. Check the backend connection.')
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => { active = false }
  }, [])

  const candidateMap = useMemo(() => new Map(candidates.map((item) => [item.id, item])), [candidates])
  const requisitionMap = useMemo(() => new Map(requisitions.map((item) => [item.id, item])), [requisitions])
  const filtered = useMemo(() => [...applications]
    .sort((a, b) => new Date(b.appliedAt) - new Date(a.appliedAt))
    .filter((item) => {
      const candidate = candidateMap.get(item.candidateId)
      const role = requisitionMap.get(item.requisitionId)
      return `${candidate?.fullName || ''} ${candidate?.email || ''} ${role?.title || ''} ${item.id}`.toLowerCase().includes(query.toLowerCase())
    }).slice(0, 8), [applications, candidateMap, requisitionMap, query])

  const pipeline = stages.slice(0, 6).map((stage) => ({ stage, count: applications.filter((item) => item.stage === stage).length }))
  const maxPipeline = Math.max(1, ...pipeline.map((item) => item.count))
  const activeApplications = applications.filter((item) => !['HIRED', 'REJECTED', 'WITHDRAWN'].includes(item.stage)).length
  const pendingOffers = offers.filter((item) => ['DRAFT', 'PENDING_APPROVAL', 'APPROVED', 'ISSUED'].includes(item.status)).length

  return <div className="page-content">
    <section className="welcome-row">
      <div><div className="eyebrow">LIVE WORKSPACE DATA</div><h1>Hiring overview <span>âœ¦</span></h1><p>Recruitment activity calculated from the connected Spring Boot API.</p></div>
      <div className="welcome-actions"><span className="demo-indicator"><i /> {error ? 'API unavailable' : 'Live API data'}</span></div>
    </section>
    {error && <div className="connection-notice warning"><AlertCircle size={16}/><span>{error}</span><button onClick={() => window.location.reload()}>Retry</button></div>}
    <section className="metric-grid" aria-label="Hiring metrics">
      <MetricCard title="Total candidates" value={loading ? 'â€”' : candidates.length.toLocaleString('en-IN')} note="Candidate profiles" icon={Users} color="blue"/>
      <MetricCard title="Open requisitions" value={loading ? 'â€”' : requisitions.filter((item) => item.status === 'APPROVED').length.toLocaleString('en-IN')} note="Approved roles" icon={BriefcaseBusiness} color="violet"/>
      <MetricCard title="Active applications" value={loading ? 'â€”' : activeApplications.toLocaleString('en-IN')} note="In the hiring pipeline" icon={ClipboardList} color="amber"/>
      <MetricCard title="Pending offers" value={loading ? 'â€”' : pendingOffers.toLocaleString('en-IN')} note="Awaiting candidate / action" icon={WalletCards} color="green"/>
    </section>
    <section className="content-grid">
      <div className="panel pipeline-panel">
        <div className="panel-header"><div><h2>Hiring pipeline</h2><p>Applications by current stage</p></div></div>
        <div className="pipeline-total"><strong>{applications.length.toLocaleString('en-IN')}</strong><span>total applications</span></div>
        <div className="pipeline-bars">{pipeline.map((item) => <div className="pipeline-row" key={item.stage}><span>{item.stage.replaceAll('_', ' ')}</span><div className="bar-track"><div className="bar-fill" style={{ width: `${item.count / maxPipeline * 100}%` }}/></div><strong>{item.count}</strong></div>)}</div>
        <div className="pipeline-foot"><span><i className="legend-dot"/> Derived from application records</span></div>
      </div>
      <div className="panel interviews-panel">
        <div className="panel-header"><div><h2>Upcoming interviews</h2><p>Scheduled interviews from recent applications</p></div></div>
        <div className="interview-list">{interviews.slice(0, 5).map((item) => {
          const app = applications.find((row) => row.id === item.applicationId)
          const candidate = app ? candidateMap.get(app.candidateId) : null
          const role = app ? requisitionMap.get(app.requisitionId) : null
          return <div className="interview-item" key={item.id}><div className="person-avatar blue">{(candidate?.fullName || 'C').split(' ').map((part) => part[0]).slice(0,2).join('').toUpperCase()}</div><div className="interview-person"><strong>{candidate?.fullName || app?.candidateId || 'Candidate'}</strong><span>{role?.title || 'Requisition'}</span><small>{item.interviewType}</small></div><time>{new Intl.DateTimeFormat('en-IN',{dateStyle:'medium',timeStyle:'short'}).format(new Date(item.scheduledStart))}</time></div>
        })}{!loading && interviews.length === 0 && <p className="empty-state">No upcoming interviews found in the latest applications.</p>}</div>
        <div className="calendar-link"><CalendarDays size={15}/> {interviews.length} scheduled interviews found</div>
      </div>
    </section>
    <section className="panel applications-panel">
      <div className="panel-header"><div><h2>Recent applications</h2><p>Latest candidate activity from the API</p></div><label className="search-box"><Search size={15}/><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search applications..."/></label></div>
      <div className="table-wrap"><table><thead><tr><th>CANDIDATE</th><th>POSITION</th><th>STAGE</th><th>SCREENING SCORE</th><th>LAST ACTIVITY</th></tr></thead><tbody>
        {loading ? <tr><td colSpan="5" className="empty-state">Loading dashboard dataâ€¦</td></tr> : filtered.map((item) => {
          const candidate = candidateMap.get(item.candidateId)
          const role = requisitionMap.get(item.requisitionId)
          return <tr key={item.id}><td><div className="candidate-cell"><div className="person-avatar small blue">{(candidate?.fullName || 'C').split(' ').map((part) => part[0]).slice(0,2).join('').toUpperCase()}</div><strong>{candidate?.fullName || item.candidateId}</strong></div></td><td>{role?.title || item.requisitionId}</td><td><span className="stage-pill blue">{item.stage}</span></td><td><strong className="score-value">{item.screeningScore == null ? 'â€”' : `${item.screeningScore}%`}</strong></td><td className="muted-cell">{item.lastStageChangedAt ? new Intl.DateTimeFormat('en-IN',{dateStyle:'medium'}).format(new Date(item.lastStageChangedAt)) : 'â€”'}</td></tr>
        })}
        {!loading && filtered.length === 0 && <tr><td colSpan="5" className="empty-state">No matching applications.</td></tr>}
      </tbody></table></div>
    </section>
    <footer className="page-footer"><span>RecruitOS Â· Recruitment operations workspace</span><span>{loading ? 'Connectingâ€¦' : error ? 'Showing no stale demo values' : 'Metrics sourced from backend records'}</span></footer>
  </div>
}
