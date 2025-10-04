import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Local autosave draft hook for CreateListing form (Phase 1 - localStorage only)
 * @param {object} params
 * @param {function} params.watch - react-hook-form watch
 * @param {function} params.reset - react-hook-form reset
 * @param {() => number} params.getCurrentStep - function returning current step
 * @param {function} params.setCurrentStep - setter for step (used when restoring)
 * @param {string|number|undefined} params.userId - current user id (to namespace drafts)
 * @param {object} params.defaultValues - default form values
 * @returns {object} { lastSaved, restoreStatus, clearDraft }
 */
export function useListingDraft({ watch, reset, getCurrentStep, setCurrentStep, userId, defaultValues }) {
  const draftKey = `listing_draft_v1_${userId || 'guest'}`;
  const [lastSaved, setLastSaved] = useState(null);
  const [restoreStatus, setRestoreStatus] = useState('idle'); // idle | restored | none | error
  const debounceTimer = useRef(null);
  const lastSerializedRef = useRef('');

  const serialize = (payload) => {
    try { return JSON.stringify(payload); } catch { return ''; }
  };

  // Restore draft on mount
  useEffect(() => {
    try {
      const raw = localStorage.getItem(draftKey);
      if (!raw) { setRestoreStatus('none'); return; }
      const parsed = JSON.parse(raw);
      if (!parsed || !parsed.values) { setRestoreStatus('none'); return; }
      // Expire after 7 days
      const sevenDays = 7 * 24 * 3600 * 1000;
      if (Date.now() - (parsed.updatedAt || 0) > sevenDays) {
        localStorage.removeItem(draftKey);
        setRestoreStatus('none');
        return;
      }
      reset(parsed.values, { keepDefaultValues: true });
      if (parsed.meta?.step) setCurrentStep(parsed.meta.step);
      setLastSaved(parsed.updatedAt);
      setRestoreStatus('restored');
      lastSerializedRef.current = serialize(parsed.values);
    } catch (e) {
      console.warn('Restore draft failed', e);
      setRestoreStatus('error');
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [draftKey]);

  const saveDraftNow = useCallback((values) => {
    try {
      const payload = {
        version: 1,
        updatedAt: Date.now(),
        meta: { step: getCurrentStep() },
        values
      };
      const serialized = serialize(values);
      if (serialized === lastSerializedRef.current) return; // no change
      localStorage.setItem(draftKey, JSON.stringify(payload));
      lastSerializedRef.current = serialized;
      setLastSaved(payload.updatedAt);
    } catch (e) {
      console.warn('Autosave failed', e);
    }
  }, [draftKey, getCurrentStep]);

  // Subscribe to form changes
  useEffect(() => {
    const subscription = watch((values) => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
      debounceTimer.current = setTimeout(() => saveDraftNow(values), 700);
    });
    return () => {
      if (debounceTimer.current) clearTimeout(debounceTimer.current);
      subscription.unsubscribe();
    };
  }, [watch, saveDraftNow]);

  const clearDraft = useCallback(() => {
    localStorage.removeItem(draftKey);
    setLastSaved(null);
  }, [draftKey]);

  return { lastSaved, restoreStatus, clearDraft };
}
