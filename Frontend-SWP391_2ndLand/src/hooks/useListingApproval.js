// src/hooks/useListingApproval.js
import { useState, useCallback } from 'react';
import { getPendingListings, approveListing, rejectListing } from '../api/admin.js';
import { ApprovalStatus } from '../constants/enums.js';

export const useListingApproval = () => {
    const [listings, setListings] = useState([]);
    const [loading, setLoading] = useState(false);
    const [actionLoading, setActionLoading] = useState({});
    const [pagination, setPagination] = useState({
        totalElements: 0,
        totalPages: 0,
        size: 12,
        number: 0
    });
    const [error, setError] = useState(null);

    // Fetch pending listings
    const fetchPendingListings = useCallback(async (params = {}) => {
        try {
            setLoading(true);
            setError(null);

            const response = await getPendingListings(params);

            // Accept response from admin.post-request normalized mapping
            const content = response.content || response.data || [];

            const mapped = (Array.isArray(content) ? content : []).map(item => {
                const approval = item.approvalStatus || item.status || item.approval || ApprovalStatus.PENDING;
                return {
                    // post-request items may include requestId and ListingId
                    id: item.id ?? item.requestId ?? item.requestID,
                    listingId: item.listingId ?? item.ListingId ?? item.ListingID,
                    title: item.title ?? item.name ?? '—',
                    price: item.price ?? null,
                    thumbnail: item.thumbnail ?? item.thumbnailUrl ?? null,
                    approvalStatus: approval,
                    raw: item
                };
            });

            setListings(mapped);
            setPagination({
                totalElements: response.totalElements || response.evCount + response.batteryCount || 0,
                totalPages: response.totalPages || response.totalPages || 0,
                size: response.size || response.size || 12,
                number: response.number || response.page || 0
            });
        } catch (err) {
            console.error('Error fetching pending listings:', err);
            setError('Không thể tải danh sách listing chờ duyệt');
        } finally {
            setLoading(false);
        }
    }, []);

    // Approve listing
    const handleApproveListing = useCallback(async (listingId, note = '') => {
        try {
            console.log('🎯 handleApproveListing in hook - listingId:', listingId);

            setActionLoading(prev => ({ ...prev, [listingId]: true }));
            setError(null);

            if (!listingId) {
                throw new Error('listingId/requestId is required for approval');
            }

            await approveListing(listingId, note);

            // Remove approved listing from current list
            setListings(prev => prev.filter(listing => listing.id !== listingId));

            // Update pagination if needed
            setPagination(prev => ({
                ...prev,
                totalElements: Math.max(0, prev.totalElements - 1)
            }));

            console.log('✅ Approval successful');
            return { success: true, message: 'Đã phê duyệt listing thành công' };
        } catch (err) {
            console.error('❌ Error approving listing:', err);
            const errorMessage = err.response?.data?.message || err.message || 'Không thể phê duyệt listing';
            setError(errorMessage);
            return { success: false, message: errorMessage };
        } finally {
            setActionLoading(prev => ({ ...prev, [listingId]: false }));
        }
    }, []);

    // Reject listing
    const handleRejectListing = useCallback(async (listingId, reason) => {
        try {
            setActionLoading(prev => ({ ...prev, [listingId]: true }));
            setError(null);

            await rejectListing(listingId, reason);

            // Remove rejected listing from current list
            setListings(prev => prev.filter(listing => listing.id !== listingId));

            // Update pagination if needed
            setPagination(prev => ({
                ...prev,
                totalElements: Math.max(0, prev.totalElements - 1)
            }));

            return { success: true, message: 'Đã từ chối listing thành công' };
        } catch (err) {
            console.error('Error rejecting listing:', err);
            const errorMessage = err.response?.data?.message || 'Không thể từ chối listing';
            setError(errorMessage);
            return { success: false, message: errorMessage };
        } finally {
            setActionLoading(prev => ({ ...prev, [listingId]: false }));
        }
    }, []);

    // Refresh current page
    const refreshListings = useCallback(() => {
        fetchPendingListings({
            page: pagination.number,
            size: pagination.size
        });
    }, [fetchPendingListings, pagination.number, pagination.size]);

    // Clear error
    const clearError = useCallback(() => {
        setError(null);
    }, []);

    return {
        // State
        listings,
        loading,
        actionLoading,
        pagination,
        error,

        // Actions
        fetchPendingListings,
        handleApproveListing,
        handleRejectListing,
        refreshListings,
        clearError,

        // Debug helpers
        setListings,
        setPagination
    };
};