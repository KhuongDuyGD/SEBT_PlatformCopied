// Hook đơn giản lấy & refresh số dư ví người dùng
import { useCallback, useEffect, useState } from 'react'
import { getMyWalletBalance } from '../api/wallet'

export function useWalletBalance(auto = true) {
  const [balance, setBalance] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true); setError(null)
    try {
      const { balance } = await getMyWalletBalance()
      setBalance(balance)
    } catch (e) {
      setError(e.message || 'Không lấy được số dư')
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { if (auto) load() }, [auto, load])

  return { balance, loading, error, refresh: load }
}

export default useWalletBalance
