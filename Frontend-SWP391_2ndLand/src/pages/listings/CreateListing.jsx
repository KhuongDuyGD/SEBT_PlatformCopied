import React, { useState, useContext, useRef, useEffect } from "react";
import { ArrowLeft, Car, Battery, MapPin, FileText, CheckCircle, AlertCircle, ChevronRight, ChevronLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
import listingsApi from "../../api/listings";
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { baseListingSchema } from '../../utils/validation/listingSchema';
import "../../components/CloudinaryImageUploadGallery.css";
import StepProgress from "../../components/listings/StepProgress";
import BasicInfoStep from "../../components/listings/steps/BasicInfoStep";
import ProductDetailsStep from "../../components/listings/steps/ProductDetailsStep";
import LocationReviewStep from "../../components/listings/steps/LocationReviewStep";
import "./CreateListing.css";
import { AuthContext } from "../../contexts/AuthContext";
import { useListingDraft } from '../../hooks/useListingDraft';

const DEFAULT_VALUES = {
    title: '', description: '', price: '', images: [], mainImageIndex: 0,
    productType: 'VEHICLE',
    vehicle: { type: 'CAR', name: '', model: '', brand: '', year: new Date().getFullYear(), mileage: 0, batteryCapacity: 0, conditionStatus: 'GOOD' },
    battery: { brand: '', model: '', capacity: 0, healthPercentage: 100, compatibleVehicles: '', conditionStatus: 'GOOD' },
    location: { province: '', district: '', details: '' }
};

const STEP_FIELDS = {
    1: ['title', 'price', 'description', 'images'],
    2: (productType) => productType === 'VEHICLE'
        ? ['vehicle.type', 'vehicle.brand', 'vehicle.name', 'vehicle.year', 'vehicle.conditionStatus']
        : ['battery.brand', 'battery.capacity', 'battery.healthPercentage', 'battery.conditionStatus'],
    3: ['location.province']
};

function CreateListing() {
    const navigate = useNavigate();
    const { userInfo } = useContext(AuthContext) || {};
    const [currentStep, setCurrentStep] = useState(1);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const redirectTimeoutRef = useRef(null);

    const { register, handleSubmit, setValue, getValues, watch, reset, trigger, formState: { errors } } = useForm({
        defaultValues: DEFAULT_VALUES,
        resolver: yupResolver(baseListingSchema),
        mode: 'onBlur'
    });

    const formData = watch();

    const { lastSaved, restoreStatus, clearDraft } = useListingDraft({
        watch, reset,
        getCurrentStep: () => currentStep,
        setCurrentStep,
        userId: userInfo?.id,
        defaultValues: DEFAULT_VALUES
    });

    const steps = [
        { id: 1, title: "Thông tin cơ bản", icon: FileText, description: "Nhập tiêu đề, giá và mô tả sản phẩm" },
        { id: 2, title: "Chi tiết sản phẩm", icon: formData.productType === 'VEHICLE' ? Car : Battery,
          description: formData.productType === 'VEHICLE' ? "Thông tin chi tiết về xe điện" : "Thông tin chi tiết về pin" },
        { id: 3, title: "Vị trí & Hoàn tất", icon: MapPin, description: "Thông tin vị trí và xác nhận đăng bán" }
    ];

    // Handle input change
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        if (name.includes('.')) {
            const [section, field] = name.split('.');
            const castVal = ['year', 'mileage', 'batteryCapacity', 'capacity', 'healthPercentage'].includes(field) && value !== ''
                ? Number(value) : value;
            setValue(section, { ...getValues(section), [field]: castVal }, { shouldValidate: true, shouldDirty: true });
        } else {
            const castVal = name === 'price' && value !== '' ? Number(value) : value;
            setValue(name, castVal, { shouldValidate: true, shouldDirty: true });
        }
    };

    // Handle images upload
    const handleImagesUpload = (files) => {
        const uniqueFiles = Array.from(new Set(files));
        const nextMain = formData.mainImageIndex < uniqueFiles.length ? formData.mainImageIndex : 0;
        setValue('images', uniqueFiles, { shouldValidate: true, shouldDirty: true });
        setValue('mainImageIndex', nextMain, { shouldValidate: true });
    };

    const handleChangeMainImage = (index) => {
        const imgs = getValues('images') || [];
        if (index >= 0 && index < imgs.length) setValue('mainImageIndex', index, { shouldValidate: true });
    };

    // Build FormData for submission
    const buildFormData = (values) => {
        const formData = new FormData();

        // Basic fields
        formData.append('title', values.title);
        formData.append('description', values.description || '');
        formData.append('price', values.price);
        formData.append('category', values.productType === 'VEHICLE' ? 'EV' : 'BATTERY');
        formData.append('listingType', 'NORMAL');

        // Product fields
        const productData = values.productType === 'VEHICLE' ? values.vehicle : values.battery;
        const prefix = values.productType === 'VEHICLE' ? 'product.ev' : 'product.battery';

        Object.entries(productData).forEach(([key, val]) => {
            formData.append(`${prefix}.${key}`, val ?? '');
        });

        // Location fields
        Object.entries(values.location).forEach(([key, val]) => {
            formData.append(`location.${key}`, val || '');
        });

        // Images - main image first
        const sortedImages = [...values.images];
        const mainIndex = values.mainImageIndex || 0;
        if (mainIndex > 0 && mainIndex < sortedImages.length) {
            const [mainImg] = sortedImages.splice(mainIndex, 1);
            sortedImages.unshift(mainImg);
        }
        sortedImages.forEach(file => formData.append('images', file));

        return formData;
    };

    // Submit handler
    const onSubmit = async (values) => {
        if (loading) return;

        try {
            setLoading(true);
            setError(null);

            if (!values.images?.length) throw new Error('Vui lòng chọn ít nhất 1 ảnh');

            // Get user from multiple sources
            const currentUser = userInfo ||
                JSON.parse(localStorage.getItem('userInfo') || localStorage.getItem('user') || sessionStorage.getItem('user') || 'null');

            const formData = buildFormData(values);
            const response = await listingsApi.createListing(formData, currentUser?.id);

            if (response.status === 200 || response.status === 201) {
                setSuccess(true);
                redirectTimeoutRef.current = setTimeout(() => {
                    reset(DEFAULT_VALUES);
                    clearDraft();
                    navigate('/');
                }, 3000);
            }

        } catch (err) {
            console.error('❌ Lỗi khi tạo listing:', err);

            const errorMessages = {
                400: '❌ Thông tin không hợp lệ: ' + (err.response?.data?.message || 'Vui lòng kiểm tra lại'),
                401: 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại',
                413: '📦 File ảnh quá lớn. Vui lòng chọn ảnh nhỏ hơn 10MB',
                500: '⚠️ Lỗi server: ' + (err.response?.data?.message || 'Vui lòng thử lại sau')
            };

            setError(err.response?.status
                ? errorMessages[err.response.status] || `Lỗi HTTP ${err.response.status}`
                : err.request ? '🌐 Không thể kết nối server' : err.message || 'Có lỗi xảy ra');
        } finally {
            setLoading(false);
        }
    };

    // Navigation handlers
    const nextStep = async () => {
        if (currentStep >= steps.length) return;
        const fields = typeof STEP_FIELDS[currentStep] === 'function'
            ? STEP_FIELDS[currentStep](formData.productType)
            : STEP_FIELDS[currentStep];
        const valid = await trigger(fields, { shouldFocus: true });
        if (valid) setCurrentStep(s => s + 1);
    };

    const prevStep = () => currentStep > 1 && setCurrentStep(s => s - 1);
    const goToStep = (step) => step >= 1 && step <= steps.length && setCurrentStep(step);

    // Effects
    useEffect(() => () => redirectTimeoutRef.current && clearTimeout(redirectTimeoutRef.current), []);

    useEffect(() => {
        const imgs = formData.images || [];
        if ((imgs.length === 0 && formData.mainImageIndex !== 0) || formData.mainImageIndex >= imgs.length) {
            setValue('mainImageIndex', 0);
        }
    }, [formData.images, formData.mainImageIndex, setValue]);

    // Flatten errors for display
    const flattenErrors = (errs, prefix = '') => {
        if (!errs) return [];
        return Object.entries(errs).flatMap(([k, v]) => {
            const path = prefix ? `${prefix}.${k}` : k;
            return v?.message ? [{ field: path, message: v.message }] :
                   typeof v === 'object' ? flattenErrors(v, path) : [];
        });
    };

    return (
        <div className="create-listing-container">
            <div className="create-listing-wrapper">
                {/* Header */}
                <div className="create-listing-header">
                    <button onClick={() => navigate(-1)} className="back-button">
                        <ArrowLeft className="w-5 h-5 mr-2" />
                        Quay lại Home
                    </button>
                    <h1 className="main-title">Tạo Listing Mới</h1>
                    <p className="main-subtitle">Chia sẻ sản phẩm của bạn với cộng đồng</p>
                </div>

                {/* Product Type Selector */}
                {currentStep === 1 && (
                    <div className="product-type-selector">
                        {[
                            { type: 'VEHICLE', icon: Car, title: 'Xe Điện', desc: 'Đăng bán ô tô điện, xe máy điện, xe đạp điện...' },
                            { type: 'BATTERY', icon: Battery, title: 'Pin Xe Điện', desc: 'Đăng bán pin lithium, pin thay thế, phụ kiện pin' }
                        ].map(({ type, icon: Icon, title, desc }) => (
                            <div key={type}
                                className={`product-type-card ${formData.productType === type ? 'selected' : ''}`}
                                onClick={() => setValue('productType', type)}>
                                <div className="product-type-icon"><Icon className="w-8 h-8" /></div>
                                <h3 className="product-type-title">{title}</h3>
                                <p className="product-type-description">{desc}</p>
                            </div>
                        ))}
                    </div>
                )}

                <StepProgress steps={steps} currentStep={currentStep} onStepClick={goToStep} />

                {/* Alerts */}
                {error && (
                    <div className="alert-error">
                        <div className="alert-error-header">
                            <AlertCircle className="w-5 h-5 mr-3" />Có lỗi xảy ra
                        </div>
                        <p className="alert-error-text">{error}</p>
                    </div>
                )}

                {success && (
                    <div className="alert-success">
                        <div className="alert-success-header">
                            <CheckCircle className="w-5 h-5 mr-3" />Đăng bài thành công!
                        </div>
                        <p className="alert-success-text">
                            🎉 Bài đăng đã được gửi thành công và đang chờ admin xét duyệt.<br />
                            🏠 Đang chuyển về trang chủ trong 3 giây...
                        </p>
                    </div>
                )}

                {/* Autosave Status */}
                <div className="autosave-status mt-2 mb-3 text-xs text-gray-500 flex items-center gap-3">
                    {restoreStatus === 'restored' && (
                        <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded border border-blue-200">
                            Đã khôi phục bản nháp
                        </span>
                    )}
                    {lastSaved && !success && <span>Lưu tự động: {new Date(lastSaved).toLocaleTimeString()}</span>}
                </div>

                {/* Form */}
                <div className="form-container">
                    <form onSubmit={handleSubmit(onSubmit)}>
                        {currentStep === 1 && (
                            <BasicInfoStep
                                formData={formData}
                                onChange={handleInputChange}
                                onImagesUpload={handleImagesUpload}
                                onChangeMainImage={handleChangeMainImage}
                                loading={loading}
                                errors={errors}
                                register={register}
                            />
                        )}
                        {currentStep === 2 && (
                            <ProductDetailsStep formData={formData} onChange={handleInputChange} errors={errors} register={register} />
                        )}
                        {currentStep === 3 && (
                            <LocationReviewStep formData={formData} onChange={handleInputChange} errors={errors} register={register} />
                        )}

                        {/* Navigation */}
                        <div className="navigation-buttons">
                            <button type="button" onClick={prevStep} disabled={currentStep === 1}
                                className={`nav-button nav-button-back ${currentStep === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}>
                                <ChevronLeft className="w-5 h-5 mr-2" />Quay lại
                            </button>
                            <div className="flex space-x-4">
                                {currentStep < steps.length ? (
                                    <button type="button" onClick={nextStep} className="nav-button nav-button-next">
                                        Tiếp tục<ChevronRight className="w-5 h-5 ml-2" />
                                    </button>
                                ) : (
                                    <button type="submit" disabled={loading || Object.keys(errors).length > 0}
                                        className={`nav-button nav-button-submit ${loading || Object.keys(errors).length > 0 ? 'opacity-50 cursor-not-allowed' : ''}`}>
                                        {loading ? (
                                            <><div className="loading-spinner"></div>Đang tạo...</>
                                        ) : (
                                            <><CheckCircle className="w-5 h-5 mr-2" />Tạo Listing</>
                                        )}
                                    </button>
                                )}
                            </div>
                        </div>
                    </form>

                    {/* Error Summary */}
                    {flattenErrors(errors).length > 0 && (
                        <div className="mt-6 p-4 border border-red-300 bg-red-50 rounded text-sm text-red-700">
                            <p className="font-semibold mb-2">Một số trường chưa hợp lệ:</p>
                            <ul className="list-disc pl-5 space-y-1">
                                {flattenErrors(errors).map(err => (
                                    <li key={err.field}>
                                        <span className="font-mono">{err.field}</span>: {err.message}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default CreateListing;

 