import test from 'node:test'
import assert from 'node:assert/strict'
import { apiRequest } from './api.js'

test('apiRequest returns parsed JSON for successful responses', async () => {
  const originalFetch = globalThis.fetch
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/v1/candidates')
    assert.equal(options.headers['Content-Type'], 'application/json')
    return new Response(JSON.stringify([{ id: 'CAND-1' }]), { status: 200 })
  }

  try {
    const result = await apiRequest('/candidates')
    assert.deepEqual(result, [{ id: 'CAND-1' }])
  } finally {
    globalThis.fetch = originalFetch
  }
})

test('apiRequest throws the API message for unsuccessful responses', async () => {
  const originalFetch = globalThis.fetch
  globalThis.fetch = async () => new Response(
    JSON.stringify({ message: 'Candidate already exists' }),
    { status: 409 },
  )

  try {
    await assert.rejects(
      apiRequest('/candidates'),
      { message: 'Candidate already exists' },
    )
  } finally {
    globalThis.fetch = originalFetch
  }
})

test('apiRequest handles no-content responses', async () => {
  const originalFetch = globalThis.fetch
  globalThis.fetch = async () => new Response(null, { status: 204 })

  try {
    assert.equal(await apiRequest('/candidates/CAND-1', { method: 'DELETE' }), null)
  } finally {
    globalThis.fetch = originalFetch
  }
})
