import { useEffect, useMemo, useState } from 'react'
import { AlertCircle, CalendarClock, CalendarPlus, Check, MessageSquareText, Search, Users, X } from 'lucide-react'
import { apiRequest } from './api'
import './Interviews.css'

const demoApplications = [
  { id: 'APP-DEMO-0003', requisitionId: 'REQ-2026-DEMO001', candidateId: 'CAND-DEMO001', stage: 'INTERVIEW' },
  { id: 'APP-DEMO-0004', requisitionId: 'REQ-2026-DEMO002', candidateId: 'CAND-DEMO002', stage: 'SHORTLISTED' },
]
const demoCandidates = [
  { id: 'CAND-DEMO001', fullName: 'Ananya Sharma' },
  { id: 'CAND-DEMO002', fullName: 'Rahul Kumar' },
]
const demoRoles = [
  { id: 'REQ-2026-DEMO001', title: 'Senior Java Engineer' },
  { id: 'REQ-2026-DEMO002', title: 'Product Designer' },
]
const demoInterviewers = [
  { id: 'INTV-DEMO001', fullName: 'Ravi Kumar', email: 'ravi.kumar@example.test', timezone: 'Asia/Kolkata' },
  { id: 'INTV-DEMO002', fullName: 'Meera Reddy', email: 'meera.reddy@example.test', timezone: 'Asia/Kolkata' },
]
const demoInterviews = [{
  id: 'INT-2026-DEMO001', applicationId: 'APP-DEMO-0003', interviewType: 'TECHNICAL',
  scheduledStart: '2026-11-16T10:30:00+05:30', scheduledEnd: '2026-11-16T11:30:00+05:30',
  mode: 'ONLINE', meetingLocation: 'Google Meet', status: 'SCHEDULED',
  panel: [{ interviewerId: 'INTV-DEMO001', interviewerName: 'Ravi Kumar', interviewerEmail: 'ravi.kumar@example.test', role: 'LEAD' }],
}]
const statusLabels = { SCHEDULED: 'Scheduled', RESCHEDULED: 'Rescheduled', COMPLETED: 'Completed', CANCELLED: 'Cancelled', NO_SHOW: 'No show' }
const initialSchedule = { applicationId: '', interviewType: 'TECHNICAL', scheduledStart: '', scheduledEnd: '', mode: 'ONLINE', meetingLocation: '', actor: 'admin@yottaflex.ai', leadInterviewerId: '', panelInterviewerIds: [] }
const initialFeedback = { interviewerId: '', score: '8', recommendation: 'YES', strengths: '', concerns: '' }

function localIso(value) {
  return value ? new Date(value).toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—'
}

export default function Interviews() {
  const [applications, setApplications] = useState([])
  const [candidates, setCandidates] = useState([])
  const [roles, setRoles] = useState([])
  const [interviewers, setInterviewers] = useState([])
  const [interviews, setInterviews] = useState([])
  const [loading, setLoading] = useState(true)
  const [apiOnline, setApiOnline] = useState(false)
  const [query, setQuery] = useState('')
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [showSchedule, setShowSchedule] = useState(false)
  const [schedule, setSchedule] = useState(initialSchedule)
  const [showFeedback, setShowFeedback] = useState(null)
  const [feedbackForm, setFeedbackForm] = useState(initialFeedback)
  const [busy, setBusy] = useState(false)
  const [busyId, setBusyId] = useState('')
  const [notice, setNotice] = useState('')

  async function loadData() {
    try {
      const [apps, people, jobs, panel] = await Promise.all([
        apiRequest('/applications'), apiRequest('/candidates'),
        apiRequest('/requisitions'), apiRequest('/interviewers'),
      ])
      const eligible = apps.filter((item) => ['SHORTLISTED', 'INTERVIEW'].includes(item.stage))
      const interviewGroups = await Promise.all(eligible.map(async (application) => {
        const records = await apiRequest(`/interviews/by-application/${application.id}`)
        return Promise.all(records.map(async (record) => ({
          ...record,
          feedback: await apiRequest(`/interviews/${record.id}/feedback`),
        })))
      }))
      setApplications(apps)
      setCandidates(people)
      setRoles(jobs)
      setInterviewers(panel)
      setInterviews(interviewGroups.flat())
      setApiOnline(true)
      setNotice('')
    } catch {
      setApplications(demoApplications)
      setCandidates(demoCandidates)
      setRoles(demoRoles)
      setInterviewers(demoInterviewers)
      setInterviews(demoInterviews.map((item) => ({ ...item, feedback: [] })))
      setApiOnline(false)
      setNotice('Backend is not connected. Showing sample interview data in read-only demo mode.')
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
  const eligibleApplications = applications.filter((item) => ['SHORTLISTED', 'INTERVIEW'].includes(item.stage))
  const filtered = useMemo(() => interviews.filter((item) => {
    const application = applicationMap.get(item.applicationId)
    const candidate = candidateMap.get(application?.candidateId)
    const role = roleMap.get(application?.requisitionId)
    const text = `${candidate?.fullName || ''} ${role?.title || ''} ${item.id}`.toLowerCase()
    return text.includes(query.toLowerCase()) && (statusFilter === 'ALL' || item.status === statusFilter)
  }), [interviews, applicationMap, candidateMap, roleMap, query, statusFilter])

  const completedCount = interviews.filter((item) => item.status === 'COMPLETED').length
  const scheduledCount = interviews.filter((item) => ['SCHEDULED', 'RESCHEDULED'].includes(item.status)).length
  const feedbackCount = interviews.reduce((sum, item) => sum + item.feedback.length, 0)

  function updateSchedule(event) {
    setSchedule((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  function updateFeedback(event) {
    setFeedbackForm((current) => ({ ...current, [event.target.name]: event.target.value }))
  }

  function togglePanelist(id) {
    setSchedule((current) => ({
      ...current,
      panelInterviewerIds: current.panelInterviewerIds.includes(id)
        ? current.panelInterviewerIds.filter((item) => item !== id)
        : [...current.panelInterviewerIds, id],
    }))
  }

  async function scheduleInterview(event) {
    event.preventDefault()
    if (!schedule.leadInterviewerId) {
      setNotice('Select exactly one lead interviewer.')
      return
    }
    const panel = [
      { interviewerId: schedule.leadInterviewerId, role: 'LEAD' },
      ...schedule.panelInterviewerIds.filter((id) => id !== schedule.leadInterviewerId)
        .map((interviewerId) => ({ interviewerId, role: 'PANELIST' })),
    ]
    setBusy(true)
    try {
      const created = await apiRequest('/interviews', {
        method: 'POST',
        body: JSON.stringify({
          applicationId: schedule.applicationId,
          interviewType: schedule.interviewType,
          scheduledStart: new Date(schedule.scheduledStart).toISOString(),
          scheduledEnd: new Date(schedule.scheduledEnd).toISOString(),
          mode: schedule.mode,
          meetingLocation: schedule.meetingLocation || null,
          actor: schedule.actor,
          panel,
        }),
      })
      setInterviews((current) => [{ ...created, feedback: [] }, ...current])
      setApplications((current) => current.map((item) => item.id === created.applicationId
        ? { ...item, stage: 'INTERVIEW' } : item))
      setShowSchedule(false)
      setSchedule(initialSchedule)
      setNotice('Interview scheduled and panel assignments saved.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  async function interviewAction(interview, action) {
    const actor = window.prompt('Enter your work email / actor identity:')
    if (!actor?.trim()) return
    const reason = window.prompt(`Reason for ${action}:`)
    if (!reason?.trim()) return
    let body = { actor: actor.trim(), reason: reason.trim() }
    if (action === 'reschedule') {
      const start = window.prompt('New start time (ISO format, e.g. 2026-11-20T10:00:00+05:30):')
      const end = window.prompt('New end time (ISO format):')
      if (!start || !end) return
      body = { ...body, scheduledStart: start, scheduledEnd: end }
    }
    setBusyId(interview.id)
    try {
      const updated = await apiRequest(`/interviews/${interview.id}/${action}`, {
        method: 'POST', body: JSON.stringify(body),
      })
      setInterviews((current) => current.map((item) => item.id === interview.id
        ? { ...updated, feedback: item.feedback } : item))
      setNotice(`Interview ${action} completed.`)
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  function openFeedback(interview) {
    const submitted = new Set(interview.feedback.map((item) => item.interviewerId))
    const remaining = interview.panel.filter((item) => !submitted.has(item.interviewerId))
    if (!remaining.length) {
      setNotice('All assigned interviewers have already submitted feedback.')
      return
    }
    setFeedbackForm({ ...initialFeedback, interviewerId: remaining[0].interviewerId })
    setShowFeedback(interview)
  }

  async function submitFeedback(event) {
    event.preventDefault()
    setBusy(true)
    try {
      const saved = await apiRequest(`/interviews/${showFeedback.id}/feedback`, {
        method: 'POST',
        body: JSON.stringify({
          ...feedbackForm,
          score: Number(feedbackForm.score),
        }),
      })
      setInterviews((current) => current.map((item) => item.id === showFeedback.id
        ? { ...item, feedback: [...item.feedback, saved] } : item))
      setShowFeedback(null)
      setNotice('Interviewer feedback saved and audited.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusy(false)
    }
  }

  async function selectApplication(applicationId) {
    const actor = window.prompt('Enter recruiter identity:')
    if (!actor?.trim()) return
    const reason = window.prompt('Selection reason:')
    if (!reason?.trim()) return
    setBusyId(applicationId)
    try {
      await apiRequest(`/applications/${applicationId}/select`, {
        method: 'POST', body: JSON.stringify({ actor: actor.trim(), reason: reason.trim() }),
      })
      setApplications((current) => current.map((item) => item.id === applicationId
        ? { ...item, stage: 'SELECTED' } : item))
      setNotice('Candidate selected. Application moved to SELECTED.')
    } catch (error) {
      setNotice(error.message)
    } finally {
      setBusyId('')
    }
  }

  function canSelect(applicationId) {
    const related = interviews.filter((item) => item.applicationId === applicationId)
    if (!related.length) return false
    return related.every((item) => item.status === 'CANCELLED' || (
      item.status === 'COMPLETED' && item.feedback.length >= item.panel.length
    )) && related.some((item) => item.status === 'COMPLETED')
  }

  return (
    <div className="page-content interviews-page">
      <section className="module-heading">
        <div><div className="eyebrow">TALENT OPERATIONS / WORKSPACE</div><h1>Interviews</h1><p>Schedule panels, manage interview changes and capture structured feedback.</p></div>
        <button className="button-primary" onClick={() => setShowSchedule(true)} disabled={!apiOnline || !eligibleApplications.length}><CalendarPlus size={16} /> Schedule interview</button>
      </section>

      {notice && <div className={`connection-notice ${apiOnline ? 'success' : 'warning'}`}><AlertCircle size={16} /><span>{notice}</span>{!apiOnline && <button onClick={() => { setLoading(true); loadData() }}>Retry connection</button>}<button className="notice-close" aria-label="Dismiss" onClick={() => setNotice('')}><X size={15} /></button></div>}

      <section className="interview-stats">
        <div><span>Total interviews</span><strong>{interviews.length}</strong><small>Across eligible applications</small></div>
        <div><span>Scheduled / rescheduled</span><strong>{scheduledCount}</strong><small>Upcoming assessments</small></div>
        <div><span>Completed</span><strong>{completedCount}</strong><small>Ready for feedback review</small></div>
        <div><span>Feedback submitted</span><strong>{feedbackCount}</strong><small>Panel assessments received</small></div>
      </section>

      <section className="panel interviews-table-panel">
        <div className="interviews-toolbar"><div><h2>Interview schedule</h2><p>{filtered.length} interviews shown</p></div><div className="interviews-filters"><label className="interview-search"><Search size={15} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search candidate, role or ID..." /></label><select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}><option value="ALL">All statuses</option><option value="SCHEDULED">Scheduled</option><option value="RESCHEDULED">Rescheduled</option><option value="COMPLETED">Completed</option><option value="CANCELLED">Cancelled</option><option value="NO_SHOW">No show</option></select></div></div>
        <div className="table-wrap"><table><thead><tr><th>CANDIDATE / ROLE</th><th>INTERVIEW</th><th>SCHEDULED TIME</th><th>PANEL</th><th>STATUS</th><th>ACTIONS</th></tr></thead><tbody>
          {loading ? <tr><td colSpan="6" className="empty-state">Loading interviews…</td></tr> : filtered.map((item) => {
            const app = applicationMap.get(item.applicationId)
            const candidate = candidateMap.get(app?.candidateId)
            const role = roleMap.get(app?.requisitionId)
            const busyRow = busyId === item.id
            const canChange = ['SCHEDULED','RESCHEDULED'].includes(item.status)
            const remainingFeedback = item.panel.filter((person) => !item.feedback.some((entry) => entry.interviewerId === person.interviewerId)).length
            return <tr key={item.id}><td><div className="interview-candidate"><strong>{candidate?.fullName || app?.candidateId || item.applicationId}</strong><small>{role?.title || app?.requisitionId}</small></div></td><td><strong className="interview-type">{item.interviewType}</strong><small className="interview-id">{item.id}</small></td><td><span className="interview-time"><CalendarClock size={13} />{localIso(item.scheduledStart)}</span><small className="interview-mode">{item.mode}{item.meetingLocation ? ` · ${item.meetingLocation}` : ''}</small></td><td><div className="panel-cell"><Users size={13} /><span>{item.panel.length} assigned</span><small>{item.panel.map((person) => person.interviewerName).join(', ')}</small></div></td><td><span className={`interview-status ${item.status.toLowerCase()}`}>{statusLabels[item.status] || item.status}</span></td><td><div className="interview-actions">{canChange && <><button disabled={!apiOnline || busyRow} onClick={() => interviewAction(item,'reschedule')}>Move</button><button disabled={!apiOnline || busyRow} onClick={() => interviewAction(item,'complete')}>Complete</button><button className="cancel-interview" disabled={!apiOnline || busyRow} onClick={() => interviewAction(item,'cancel')}>Cancel</button></>}{item.status === 'COMPLETED' && remainingFeedback > 0 && <button disabled={!apiOnline || busyRow} onClick={() => openFeedback(item)}>Feedback</button>}</div></td></tr>
          })}
          {!loading && filtered.length === 0 && <tr><td colSpan="6" className="empty-state">No interviews match your filters. Schedule one for a shortlisted application.</td></tr>}
        </tbody></table></div>
        <div className="table-footer"><span>Showing {filtered.length} interviews</span><span>Panel booking conflicts are checked by the backend</span></div>
      </section>

      <section className="panel selection-panel"><div><h2>Selection readiness</h2><p>Applications become selectable only after all non-cancelled interviews and assigned panel feedback are complete.</p></div><div className="selection-list">{eligibleApplications.filter((item) => item.stage === 'INTERVIEW').map((app) => <div key={app.id}><span><strong>{candidateMap.get(app.candidateId)?.fullName || app.candidateId}</strong><small>{roleMap.get(app.requisitionId)?.title || app.requisitionId}</small></span>{canSelect(app.id) ? <button disabled={!apiOnline || busyId === app.id} onClick={() => selectApplication(app.id)}><Check size={14} /> Select candidate</button> : <small className="waiting-label">Awaiting completed interviews and feedback</small>}</div>)}{eligibleApplications.every((item) => item.stage !== 'INTERVIEW') && <span className="empty-selection">No applications are currently awaiting interview selection.</span>}</div></section>

      {showSchedule && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowSchedule(false) }}><section className="create-modal interview-modal" role="dialog" aria-modal="true" aria-labelledby="schedule-title"><div className="modal-heading"><div><span className="eyebrow">INTERVIEW PLANNING</span><h2 id="schedule-title">Schedule interview</h2><p>Assign one lead interviewer and any additional panelists.</p></div><button className="modal-close" onClick={() => setShowSchedule(false)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={scheduleInterview}><div className="form-grid">
          <label className="form-field span-2">Application<select required name="applicationId" value={schedule.applicationId} onChange={updateSchedule}><option value="">Select a shortlisted application</option>{eligibleApplications.map((app) => <option key={app.id} value={app.id}>{candidateMap.get(app.candidateId)?.fullName || app.candidateId} · {roleMap.get(app.requisitionId)?.title || app.requisitionId}</option>)}</select></label>
          <label className="form-field">Interview type<select name="interviewType" value={schedule.interviewType} onChange={updateSchedule}><option value="SCREENING">Screening</option><option value="TECHNICAL">Technical</option><option value="MANAGERIAL">Managerial</option><option value="HR">HR</option><option value="FINAL">Final</option></select></label>
          <label className="form-field">Mode<select name="mode" value={schedule.mode} onChange={updateSchedule}><option value="ONLINE">Online</option><option value="ONSITE">Onsite</option><option value="PHONE">Phone</option></select></label>
          <label className="form-field">Start time<input required type="datetime-local" name="scheduledStart" value={schedule.scheduledStart} onChange={updateSchedule} /></label>
          <label className="form-field">End time<input required type="datetime-local" name="scheduledEnd" value={schedule.scheduledEnd} onChange={updateSchedule} /></label>
          <label className="form-field span-2">Meeting link / location<input name="meetingLocation" value={schedule.meetingLocation} onChange={updateSchedule} placeholder="Meeting URL or office location" /></label>
          <label className="form-field span-2">Lead interviewer<select required name="leadInterviewerId" value={schedule.leadInterviewerId} onChange={updateSchedule}><option value="">Select one lead</option>{interviewers.map((person) => <option key={person.id} value={person.id}>{person.fullName} · {person.email}</option>)}</select></label>
          <div className="form-field span-2"><span>Additional panelists</span><div className="panelist-options">{interviewers.filter((person) => person.id !== schedule.leadInterviewerId).map((person) => <label key={person.id}><input type="checkbox" checked={schedule.panelInterviewerIds.includes(person.id)} onChange={() => togglePanelist(person.id)} /><span>{person.fullName}</span><small>{person.email}</small></label>)}</div></div>
          <label className="form-field span-2">Scheduled by<input required name="actor" value={schedule.actor} onChange={updateSchedule} /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowSchedule(false)}>Cancel</button><button type="submit" className="button-primary" disabled={busy || !apiOnline || !interviewers.length}>{busy ? 'Scheduling…' : 'Schedule interview'}</button></div>
        </form></section></div>}

      {showFeedback && <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) setShowFeedback(null) }}><section className="create-modal interview-modal" role="dialog" aria-modal="true" aria-labelledby="feedback-title"><div className="modal-heading"><div><span className="eyebrow">STRUCTURED ASSESSMENT</span><h2 id="feedback-title">Submit interviewer feedback</h2><p>Feedback is recorded once per assigned interviewer.</p></div><button className="modal-close" onClick={() => setShowFeedback(null)} aria-label="Close"><X size={18} /></button></div>
        <form onSubmit={submitFeedback}><div className="form-grid">
          <label className="form-field span-2">Interviewer<select required name="interviewerId" value={feedbackForm.interviewerId} onChange={updateFeedback}>{showFeedback.panel.filter((person) => !showFeedback.feedback.some((entry) => entry.interviewerId === person.interviewerId)).map((person) => <option key={person.interviewerId} value={person.interviewerId}>{person.interviewerName} · {person.role}</option>)}</select></label>
          <label className="form-field">Score (0–10)<input required type="number" min="0" max="10" step="0.5" name="score" value={feedbackForm.score} onChange={updateFeedback} /></label>
          <label className="form-field">Recommendation<select name="recommendation" value={feedbackForm.recommendation} onChange={updateFeedback}><option value="STRONG_YES">Strong yes</option><option value="YES">Yes</option><option value="HOLD">Hold</option><option value="NO">No</option><option value="STRONG_NO">Strong no</option></select></label>
          <label className="form-field span-2">Strengths<textarea required rows="3" name="strengths" value={feedbackForm.strengths} onChange={updateFeedback} placeholder="What did the candidate do well?" /></label>
          <label className="form-field span-2">Concerns (optional)<textarea rows="2" name="concerns" value={feedbackForm.concerns} onChange={updateFeedback} placeholder="Areas to probe or improve…" /></label>
        </div>
        <div className="modal-actions"><button type="button" className="button-secondary" onClick={() => setShowFeedback(null)}>Cancel</button><button type="submit" className="button-primary" disabled={busy}>{busy ? 'Saving…' : 'Submit feedback'} <MessageSquareText size={14} /></button></div>
        </form></section></div>}
    </div>
  )
}
