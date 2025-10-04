import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Local autosave draft hook for CreateListing form (Phase 1 - localStorage only)
 * NOTE: File objects (images) are NOT persisted - only form text data
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

  /**
   * Serialize form values - EXCLUDE images (File objects)
   * Files cannot be stored in localStorage
   */
  const serializeForStorage = (values) => {
    try {
      const { images, ...rest } = values;
      // Store everything except File objects
      return JSON.stringify({
        ...rest,
        imagesCount: Array.isArray(images) ? images.length : 0 // Just store count for reference
      });
    } catch {
      return '';
    }
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
      // Restore values but keep images empty (files can't be persisted)
      const { imagesCount, ...restoredValues } = parsed.values;
      reset({ ...restoredValues, images: [] }, { keepDefaultValues: true });
      if (parsed.meta?.step) setCurrentStep(parsed.meta.step);
      setLastSaved(parsed.updatedAt);
      setRestoreStatus('restored');
      lastSerializedRef.current = serializeForStorage(restoredValues);
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
      const serialized = serializeForStorage(values);
      if (serialized === lastSerializedRef.current) return; // no change
      localStorage.setItem(draftKey, JSON.stringify({
        ...payload,
        values: JSON.parse(serialized) // Store the serializable part
      }));
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
