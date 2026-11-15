import { useState } from 'react'
import { AlertCircle, History, Search } from 'lucide-react'
import { apiRequest } from '../../shared/api/api.js'
import './AuditTrail.css'

function AuditTrail() {
  const [aggregateType, setAggregateType] = useState('APPLICATION')
  const [aggregateId, setAggregateId] = useState('')
  const [events, setEvents] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [searched, setSearched] = useState(false)

  async function handleSearch(event) {
    event.preventDefault()
    if (!aggregateType.trim() || !aggregateId.trim()) {
      setError('Enter an aggregate type and ID to search its audit history.')
      return
    }
    setLoading(true)
    setError('')
    setSearched(true)
    try {
      const query = new URLSearchParams({ aggregateType: aggregateType.trim(), aggregateId: aggregateId.trim() })
      const result = await apiRequest(`/audit-events?${query.toString()}`)
      setEvents(Array.isArray(result) ? result : [])
    } catch (requestError) {
      setEvents([])
      setError(requestError.message || 'Unable to load audit history.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="audit-page">
      <div className="page-heading">
        <div><span className="eyebrow">TRACEABILITY</span><h1>Audit trail</h1><p>Review the recorded events for a recruitment record.</p></div>
        <div className="audit-heading-icon"><History size={22} /></div>
      </div>
      <form className="audit-search" onSubmit={handleSearch}>
        <label>Aggregate type
          <select value={aggregateType} onChange={(event) => setAggregateType(event.target.value)}>
            <option value="APPLICATION">Application</option><option value="JOB_REQUISITION">Requisition</option>
            <option value="CANDIDATE">Candidate</option><option value="INTERVIEW">Interview</option>
            <option value="OFFER">Offer</option><option value="SKILL">Skill</option>
            <option value="SCREENING_RULE">Screening rule</option><option value="RESUME_ARTIFACT">Resume metadata</option>
            <option value="CANDIDATE_SKILL">Candidate skill</option>
          </select>
        </label>
        <label className="audit-id-field">Aggregate ID
          <input value={aggregateId} onChange={(event) => setAggregateId(event.target.value)}
            placeholder="Enter the record ID" maxLength={100} required />
        </label>
        <button className="button-primary" type="submit" disabled={loading}><Search size={16} />{loading ? 'SearchingΓÇª' : 'View history'}</button>
      </form>
      {error && <div className="audit-error" role="alert"><AlertCircle size={17} />{error}</div>}
      <div className="audit-results">
        <div className="audit-results-heading"><div><h2>Event history</h2><p>Events are stored as append-only records.</p></div><span>{events.length} events</span></div>
        {!searched ? <div className="audit-empty">Enter a record ID above to inspect its audit history.</div>
          : loading ? <div className="audit-empty" role="status">Loading audit eventsΓÇª</div>
            : events.length === 0 ? <div className="audit-empty">No audit events found for this record.</div>
              : <div className="audit-event-list">{events.map((item) => (
                <article className="audit-event" key={item.id}>
                  <div className="audit-event-marker" />
                  <div className="audit-event-content">
                    <div className="audit-event-topline"><strong>{item.eventType.replaceAll('_', ' ')}</strong>
                      <time dateTime={item.occurredAt}>{new Date(item.occurredAt).toLocaleString()}</time></div>
                    <p>Actor: {item.actor}</p><pre>{JSON.stringify(item.eventPayload, null, 2)}</pre>
                  </div>
                </article>
              ))}</div>}
      </div>
    </section>
  )
}

export default AuditTrail

