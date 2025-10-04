import React, { useState, useContext, useRef, useEffect } from "react";
import { ArrowLeft, Car, Battery, MapPin, DollarSign, FileText, CheckCircle, AlertCircle, ChevronRight, ChevronLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
// import api from "../../api/axios"; // removed (unused)
import listingsApi from "../../api/listings";
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import { baseListingSchema, buildPayload } from '../../utils/validation/listingSchema';
import "../../components/CloudinaryImageUpload.css";
import StepProgress from "../../components/listings/StepProgress";
import BasicInfoStep from "../../components/listings/steps/BasicInfoStep";
import ProductDetailsStep from "../../components/listings/steps/ProductDetailsStep";
import LocationReviewStep from "../../components/listings/steps/LocationReviewStep";
import "./CreateListing.css"; // Import CSS file để trang điểm cho UI/UX
import { AuthContext } from "../../contexts/AuthContext"; // Import AuthContext để lấy user info
import { useListingDraft } from '../../hooks/useListingDraft';

function CreateListing() {
    const navigate = useNavigate();
    // Lấy đúng thông tin user từ AuthContext (App cung cấp userInfo)
    const { userInfo } = useContext(AuthContext) || {}; // userInfo: { id, username, email, role, ... }
    const [currentStep, setCurrentStep] = useState(1);
    const redirectTimeoutRef = useRef(null);

        const defaultValues = {
            title: '',
            description: '',
            price: '',
            images: [],
            mainImageIndex: 0,
            productType: 'VEHICLE',
            vehicle: {
                type: 'CAR',
                name: '',
                model: '',
                brand: '',
                year: new Date().getFullYear(),
                mileage: 0,
                batteryCapacity: 0,
                conditionStatus: 'GOOD'
            },
            battery: {
                brand: '',
                model: '',
                capacity: 0,
                healthPercentage: 100,
                compatibleVehicles: '',
                conditionStatus: 'GOOD'
            },
            location: {
                province: '',
                district: '',
                details: ''
            }
        };

        const { register, handleSubmit, setValue, getValues, watch, reset, trigger, formState: { errors } } = useForm({
            defaultValues,
            resolver: yupResolver(baseListingSchema),
            mode: 'onBlur'
        });

        const formData = watch();

    // States
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

        // Autosave draft hook
        const { lastSaved, restoreStatus, clearDraft } = useListingDraft({
            watch,
            reset,
            getCurrentStep: () => currentStep,
            setCurrentStep,
            userId: userInfo?.id,
            defaultValues
        });

    // Cấu hình các bước tạo listing với icon động theo loại sản phẩm
    const steps = [
        {
            id: 1,
            title: "Thông tin cơ bản",
            icon: FileText,
            description: "Nhập tiêu đề, giá và mô tả sản phẩm"
        },
        {
            id: 2,
            title: "Chi tiết sản phẩm",
            icon: formData.productType === 'VEHICLE' ? Car : Battery,
            description: formData.productType === 'VEHICLE'
                ? "Thông tin chi tiết về xe điện"
                : "Thông tin chi tiết về pin"
        },
        {
            id: 3,
            title: "Vị trí & Hoàn tất",
            icon: MapPin,
            description: "Thông tin vị trí và xác nhận đăng bán"
        }
    ];

    /**
     * Xử lý thay đổi giá trị input trong form
     * @param {Event} e - Event từ input
     */
        const handleInputChange = (e) => {
            const { name, value } = e.target;
            if (name.includes('.')) {
                const [section, field] = name.split('.');
                const current = getValues(section);
                let castVal = value;
                if (['year','mileage','batteryCapacity','capacity','healthPercentage'].includes(field)) {
                    castVal = value === '' ? '' : Number(value);
                }
                setValue(section, { ...current, [field]: castVal }, { shouldValidate: true, shouldDirty: true });
            } else {
                let castVal = value;
                if (name === 'price') {
                    castVal = value === '' ? '' : Number(value);
                }
                setValue(name, castVal, { shouldValidate: true, shouldDirty: true });
            }
        };

    /**
     * Xử lý khi upload ảnh thành công từ Cloudinary
     * @param {string} imageUrl - URL ảnh từ Cloudinary
     */
        const handleImagesUpload = (imageUrls) => {
            const currentMain = getValues('mainImageIndex');
            const unique = Array.from(new Set(imageUrls));
            const nextMain = currentMain < unique.length ? currentMain : 0;
            setValue('images', unique, { shouldValidate: true, shouldDirty: true });
            setValue('mainImageIndex', nextMain, { shouldValidate: true });
        };

        const handleChangeMainImage = (index) => {
            const imgs = getValues('images') || [];
            if (index >=0 && index < imgs.length) {
                setValue('mainImageIndex', index, { shouldValidate: true });
            }
        };

    /**
     * Submit form tạo listing - Không cần admin duyệt, đăng trực tiếp
     */
    const onSubmit = async (values) => {
        if (loading) return; // guard double submit
        try {
            setLoading(true);
            setError(null);
            const formData = values; // alias

            // ==== AUTH HANDLING (Simplified + Dev Fallback) ====
            const userFromContext = userInfo;
            let userFromLocalStorage = null;
            let userFromSessionStorage = null;
            let userFromUserInfo = null;
            try { userFromLocalStorage = JSON.parse(localStorage.getItem('user') || 'null'); } catch {}
            try { userFromSessionStorage = JSON.parse(sessionStorage.getItem('user') || 'null'); } catch {}
            try { userFromUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null'); } catch {}

            let currentUser = userFromContext || userFromUserInfo || userFromLocalStorage || userFromSessionStorage;

            // Dev fallback (ONLY if still missing) - allow backend to assign default or test user
            if (!currentUser) {
                console.warn('⚠️ No authenticated user found. Proceeding WITHOUT user header (backend will fallback).');
            }

                        if (import.meta.env.DEV) {
                                console.debug('[CreateListing] auth context:', {
                                    resolvedUser: currentUser,
                                    localStorageKeys: Object.keys(localStorage),
                                    sessionStorageKeys: Object.keys(sessionStorage)
                                });
                        }

            // Dedupe images defensively
            const uniqueImages = Array.from(new Set(formData.images));
            if (uniqueImages.length !== formData.images.length) {
                setValue('images', uniqueImages, { shouldValidate: true });
                formData.images = uniqueImages;
            }

            const payload = buildPayload(formData);

            // Debug payload trước khi gửi với Backend DTO mapping
                        if (import.meta.env.DEV) {
                            console.debug('[CreateListing] form raw:', formData);
                            console.debug('[CreateListing] payload:', payload);
                        }

            // QUAN TRỌNG: Ảnh đã được upload lên Cloudinary rồi (qua CloudinaryImageUpload component)
            // Chỉ cần gửi Cloudinary URL trong JSON payload, KHÔNG cần upload lại

            // Kiểm tra ảnh đã được upload lên Cloudinary chưa
            if (!formData.images || formData.images.length === 0) {
                throw new Error('Vui lòng upload ít nhất 1 ảnh');
            }
            if (import.meta.env.DEV) console.debug('[CreateListing] images ready:', formData.images);

            console.log('Final payload với Cloudinary URLs:', payload);

            // Gửi JSON request (không phải multipart) vì ảnh đã có sẵn trên Cloudinary
            const response = await listingsApi.createListing(payload, currentUser?.id);

            if (import.meta.env.DEV) console.debug('[CreateListing] backend response status:', response.status);

            if (response.status === 200 || response.status === 201) {
                if (import.meta.env.DEV) console.debug('[CreateListing] success create');
                setSuccess(true);

                // Hiển thị thông báo và chuyển về home screen sau 3 giây
                redirectTimeoutRef.current = setTimeout(() => {
                    // Reset form về trạng thái ban đầu để chuẩn bị cho lần đăng bài tiếp theo
                    reset(defaultValues);
                    clearDraft();

                    setSuccess(false);
                    setCurrentStep(1);

                    // Chuyển về trang chủ thay vì trang listing cụ thể
                    // Vì bài đăng cần được admin duyệt trước khi hiện ra trong listing
                    if (import.meta.env.DEV) console.debug('[CreateListing] redirect home');
                    navigate('/');
                }, 3000); // Tăng thời gian lên 3 giây để user đọc được thông báo
            }

        } catch (err) {
            // Enhanced error logging cho backend errors
            console.error('❌ Lỗi khi tạo listing:', {
                error_message: err.message,
                error_name: err.name,
                response_status: err.response?.status,
                response_data: err.response?.data,
                response_headers: err.response?.headers,
                request_config: err.config,
                full_error: err
            });

            // Phân tích loại lỗi cụ thể
            let userErrorMessage = 'Có lỗi xảy ra khi đăng bài';

            if (err.response) {
                const { status, data } = err.response;

                console.error('Phân tích lỗi từ backend:', {
                    status: status,
                    data: data,
                    is_validation_error: status === 400,
                    is_unauthorized: status === 401,
                    is_server_error: status >= 500,
                    is_method_not_allowed: status === 405
                });

                if (status === 400) {
                    userErrorMessage = '❌ Thông tin không hợp lệ: ' + (data || 'Vui lòng kiểm tra lại các thông tin đã nhập');
                } else if (status === 401) {
                    userErrorMessage = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để đăng bài';
                } else if (status === 405) {
                    userErrorMessage = '🚫 Phương thức không được hỗ trợ. Vui lòng liên hệ hỗ trợ kỹ thuật';
                } else if (status === 403) {
                    userErrorMessage = '🚫 Không có quyền đăng bài: Tài khoản của bạn chưa được phép đăng bài';
                } else if (status === 500) {
                    if (data?.includes && data.includes('rollback')) {
                        userErrorMessage = 'Lỗi cơ sở dữ liệu: Vui lòng thử đăng bài lại sau ít phút';
                    } else {
                        userErrorMessage = '⚠️ Lỗi hệ thống: Server đang bảo trì, vui lòng thử lại sau';
                    }
                } else {
                    userErrorMessage = `Lỗi HTTP ${status}: ${data || 'Vui lòng liên hệ hỗ trợ kỹ thuật'}`;
                }
            } else if (err.request) {
                userErrorMessage = '🌐 Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng';
            }

            console.error('👤 User Error Message:', userErrorMessage);
            setError(userErrorMessage);
        } finally {
            setLoading(false);
        }
    };


    /**
     * Chuyển trực tiếp đến bước cụ thể
     * @param {number} step - Số thứ tự bước cần chuyển đến
     */
    const goToStep = (step) => {
        if (step >= 1 && step <= steps.length) {
            setCurrentStep(step);
        }
    };

    /**
     * Validate dữ liệu theo từng bước để kiểm soát navigation
     * @param {number} step - Bước cần validate
     * @returns {boolean} - True nếu dữ liệu hợp lệ
     */
        // Field names per step for trigger validation
        const stepFields = {
            1: ['title','price','description','images'],
            2: formData.productType === 'VEHICLE'
                ? ['vehicle.type','vehicle.brand','vehicle.name','vehicle.year','vehicle.mileage','vehicle.batteryCapacity','vehicle.conditionStatus']
                : ['battery.brand','battery.model','battery.capacity','battery.healthPercentage','battery.compatibleVehicles','battery.conditionStatus'],
            3: ['location.province','location.district','location.details']
        };

        const nextStep = async () => {
            if (currentStep >= steps.length) return;
            const fieldsToValidate = stepFields[currentStep] || [];
            const valid = await trigger(fieldsToValidate, { shouldFocus: true });
            if (valid) setCurrentStep(s => s + 1);
        };

        const prevStep = () => {
            if (currentStep > 1) setCurrentStep(s => s - 1);
        };

    // Cleanup redirect timeout if component unmounts
    useEffect(()=> () => { if (redirectTimeoutRef.current) clearTimeout(redirectTimeoutRef.current); }, []);

    // Ensure mainImageIndex never goes out of range if user removes images elsewhere
    useEffect(()=> {
        const imgs = formData.images || [];
        if (imgs.length === 0 && formData.mainImageIndex !== 0) {
            setValue('mainImageIndex', 0);
        } else if (formData.mainImageIndex >= imgs.length && imgs.length > 0) {
            setValue('mainImageIndex', 0);
        }
    }, [formData.images, formData.mainImageIndex, setValue]);

    // Flatten nested react-hook-form/yup errors for summary rendering
    const flattenErrors = (errs, prefix='') => {
        if (!errs) return [];
        const out = [];
        Object.entries(errs).forEach(([k,v]) => {
            const path = prefix ? `${prefix}.${k}` : k;
            if (v && v.message) {
                out.push({ field: path, message: v.message });
            } else if (v && typeof v === 'object') {
                out.push(...flattenErrors(v, path));
            }
        });
        return out;
    };
    const flatErrors = flattenErrors(errors);

    return (
        <div className="create-listing-container">
            <div className="create-listing-wrapper">
                {/* Header với nút quay lại và tiêu đề chính */}
                <div className="create-listing-header">
                    <button
                        onClick={() => navigate(-1)}
                        className="back-button"
                    >
                        <ArrowLeft className="w-5 h-5 mr-2" />
                        Quay lại Home
                    </button>
                    <h1 className="main-title">
                        Tạo Listing Mới
                    </h1>
                    <p className="main-subtitle">Chia sẻ sản phẩm của bạn với cộng đồng</p>
                </div>

                {/* Selector chọn loại sản phẩm - Hiển thị ở step đầu tiên */}
                                {currentStep === 1 && (
                                    <div className="product-type-selector">
                                        <div
                                            className={`product-type-card ${formData.productType === 'VEHICLE' ? 'selected' : ''}`}
                                            onClick={() => setValue('productType', 'VEHICLE')}
                                        >
                                            <div className="product-type-icon">
                                                <Car className="w-8 h-8" />
                                            </div>
                                            <h3 className="product-type-title">Xe Điện</h3>
                                            <p className="product-type-description">Đăng bán ô tô điện, xe máy điện, xe đạp điện...</p>
                                        </div>
                                        <div
                                            className={`product-type-card ${formData.productType === 'BATTERY' ? 'selected' : ''}`}
                                            onClick={() => setValue('productType', 'BATTERY')}
                                        >
                                            <div className="product-type-icon">
                                                <Battery className="w-8 h-8" />
                                            </div>
                                            <h3 className="product-type-title">Pin Xe Điện</h3>
                                            <p className="product-type-description">Đăng bán pin lithium, pin thay thế, phụ kiện pin</p>
                                        </div>
                                    </div>
                                )}

                {/* Progress Steps với CSS classes */}
                <StepProgress steps={steps} currentStep={currentStep} onStepClick={goToStep} />

                {/* Thông báo lỗi */}
                {error && (
                    <div className="alert-error">
                        <div className="alert-error-header">
                            <AlertCircle className="w-5 h-5 mr-3" />
                            Có lỗi xảy ra
                        </div>
                        <p className="alert-error-text">{error}</p>
                    </div>
                )}

                {/* Thông báo thành công */}
                {success && (
                    <div className="alert-success">
                        <div className="alert-success-header">
                            <CheckCircle className="w-5 h-5 mr-3" />
                            Đăng bài thành công!
                        </div>
                        <p className="alert-success-text">
                            🎉 Bài đăng của bạn đã được gửi thành công và đang chờ admin xét duyệt.
                            📧 Bạn sẽ nhận được thông báo qua email khi bài đăng được phê duyệt.
                            <br />
                            🏠 Đang chuyển về trang chủ trong 3 giây...
                        </p>
                    </div>
                )}

                                {/* Autosave status */}
                                <div className="autosave-status mt-2 mb-3 text-xs text-gray-500 flex items-center gap-3">
                                    {restoreStatus === 'restored' && (
                                        <span className="px-2 py-1 bg-blue-50 text-blue-600 rounded border border-blue-200">Đã khôi phục bản nháp</span>
                                    )}
                                    {lastSaved && !success && (
                                        <span>Lưu tự động: {new Date(lastSaved).toLocaleTimeString()}</span>
                                    )}
                                </div>

                                {/* Multi-Step Form Container */}
                <div className="form-container">
                    <form onSubmit={handleSubmit(onSubmit)}>
                        {/* Step 1: Thông tin cơ bản */}
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

                        {/* Step 2: Chi tiết sản phẩm */}
                        {currentStep === 2 && (
                          <ProductDetailsStep formData={formData} onChange={handleInputChange} errors={errors} register={register} />
                        )}

                        {/* Step 3: Vị trí & Hoàn tất */}
                        {currentStep === 3 && (
                          <LocationReviewStep formData={formData} onChange={handleInputChange} errors={errors} register={register} />
                        )}

                        {/* Navigation Buttons */}
                        <div className="navigation-buttons">
                            <button
                                type="button"
                                onClick={prevStep}
                                disabled={currentStep === 1}
                                className={`nav-button nav-button-back ${currentStep === 1 ? 'opacity-50 cursor-not-allowed' : ''}`}
                            >
                                <ChevronLeft className="w-5 h-5 mr-2" />
                                Quay lại
                            </button>

                            <div className="flex space-x-4">
                                                                {currentStep < steps.length ? (
                                                                    <button
                                                                        type="button"
                                                                        onClick={nextStep}
                                                                        className="nav-button nav-button-next"
                                                                    >
                                                                        Tiếp tục
                                                                        <ChevronRight className="w-5 h-5 ml-2" />
                                                                    </button>
                                                                ) : (
                                                                    <button
                                                                        type="submit"
                                                                        disabled={loading || Object.keys(errors).length > 0}
                                                                        className={`nav-button nav-button-submit ${(loading || Object.keys(errors).length>0) ? 'opacity-50 cursor-not-allowed' : ''}`}
                                                                    >
                                                                        {loading ? (
                                                                            <>
                                                                                <div className="loading-spinner"></div>
                                                                                Đang tạo...
                                                                            </>
                                                                        ) : (
                                                                            <>
                                                                                <CheckCircle className="w-5 h-5 mr-2" />
                                                                                Tạo Listing
                                                                            </>
                                                                        )}
                                                                    </button>
                                                                )}
                            </div>
                        </div>
                                        </form>
                                        {flatErrors.length > 0 && (
                                            <div className="mt-6 p-4 border border-red-300 bg-red-50 rounded text-sm text-red-700">
                                                <p className="font-semibold mb-2">Một số trường chưa hợp lệ:</p>
                                                <ul className="list-disc pl-5 space-y-1">
                                                    {flatErrors.map(err => (
                                                        <li key={err.field}><span className="font-mono">{err.field}</span>: {err.message}</li>
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