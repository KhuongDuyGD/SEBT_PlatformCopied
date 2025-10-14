// Hook polling trạng thái top-up đến khi không còn PENDING
import { useCallback, useEffect, useRef, useState } from 'react'
import { getTopUpStatus } from '../api/wallet'

/**
 * @param {string|null} orderId
 * @param {object} options { intervalMs?: number, auto?: boolean, onDone?: (tx)=>void }
 */
export function useTopUpStatus(orderId, options = {}) {
  const { intervalMs = 2500, auto = true, onDone } = options
  const [transaction, setTransaction] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const timerRef = useRef(null)
  const doneRef = useRef(false)

  const fetchStatus = useCallback(async () => {
    if (!orderId) return
    setLoading(true); setError(null)
    try {
      const tx = await getTopUpStatus(orderId)
      setTransaction(tx)
      if (tx.status && tx.status !== 'PENDING') {
        doneRef.current = true
        if (onDone) onDone(tx)
        if (timerRef.current) clearInterval(timerRef.current)
      }
    } catch (e) {
      setError(e.message || 'Không lấy được trạng thái top-up')
      if (timerRef.current) clearInterval(timerRef.current)
    } finally {
      setLoading(false)
    }
  }, [orderId, onDone])

  useEffect(() => {
    if (!auto || !orderId) return
    fetchStatus() // initial
    timerRef.current = setInterval(() => {
      if (!doneRef.current) fetchStatus()
    }, intervalMs)
    return () => { if (timerRef.current) clearInterval(timerRef.current) }
  }, [auto, orderId, fetchStatus, intervalMs])

  return { transaction, loading, error, refresh: fetchStatus }
}

export default useTopUpStatus
