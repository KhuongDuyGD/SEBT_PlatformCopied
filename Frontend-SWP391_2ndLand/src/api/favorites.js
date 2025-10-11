import api from './axios'

// Favorites API (legacy fallback removed – backend now guarantees new endpoints)
export async function markFavorite(listingId) {
  const { data } = await api.put(`/members/favorites/${listingId}`)
  return data
}

export async function unmarkFavorite(listingId) {
  const { data } = await api.delete(`/members/favorites/${listingId}`)
  return data
}

export default { markFavorite, unmarkFavorite }
