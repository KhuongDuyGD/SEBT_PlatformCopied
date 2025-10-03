import React, { useState, useContext } from "react";
import { ArrowLeft, Car, Battery, MapPin, DollarSign, FileText, CheckCircle, AlertCircle, ChevronRight, ChevronLeft } from "lucide-react";
import { useNavigate } from "react-router-dom";
import api from "../../api/axios";
import CloudinaryImageUpload from "../../components/CloudinaryImageUpload";
import "../../components/CloudinaryImageUpload.css";
import "./CreateListing.css"; // Import CSS file để trang điểm cho UI/UX
import { AuthContext } from "../../contexts/AuthContext"; // Import AuthContext để lấy user info

function CreateListing() {
    const navigate = useNavigate();
    const { user } = useContext(AuthContext) || {}; // Lấy user từ AuthContext
    const [currentStep, setCurrentStep] = useState(1);
    // State quản lý dữ liệu form với các trường cần thiết
    const [formData, setFormData] = useState({
        title: '', // Tiêu đề listing
        description: '', // Mô tả chi tiết
        price: '', // Giá bán
        mainImage: '', // URL ảnh chính từ Cloudinary
        productType: 'VEHICLE', // Loại sản phẩm: 'VEHICLE' hoặc 'BATTERY'

        // Thông tin chi tiết xe điện
        vehicle: {
            type: 'CAR', // Loại xe: CAR, BIKE, MOTORBIKE
            name: '', // Tên xe
            model: '', // Model xe
            brand: '', // Hãng xe
            year: new Date().getFullYear(), // Năm sản xuất
            mileage: 0, // Số km đã đi
            batteryCapacity: 0, // Dung lượng pin (kWh)
            conditionStatus: 'GOOD' // Tình trạng xe
        },

        // Thông tin chi tiết pin
        battery: {
            brand: '', // Hãng pin
            model: '', // Model pin
            capacity: 0, // Dung lượng pin (kWh)
            healthPercentage: 100, // Độ khỏe pin (%)
            compatibleVehicles: '', // Xe tương thích
            conditionStatus: 'GOOD' // Tình trạng pin
        },

        // Thông tin vị trí
        location: {
            province: '', // Tỉnh/Thành phố
            district: '', // Quận/Huyện
            details: '' // Địa chỉ chi tiết
        }
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);

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

        // Xử lý cho các field nested (vd: vehicle.name, battery.brand)
        if (name.includes('.')) {
            const [section, field] = name.split('.');
            setFormData(prev => ({
                ...prev,
                [section]: {
                    ...prev[section],
                    // Convert số cho các field số
                    [field]: field === 'year' || field === 'mileage' || field === 'batteryCapacity'
                    || field === 'capacity' || field === 'healthPercentage'
                        ? parseInt(value) || 0
                        : value
                }
            }));
        } else {
            // Xử lý cho các field cấp cao
            setFormData(prev => ({
                ...prev,
                [name]: name === 'price' ? parseFloat(value) || 0 : value
            }));
        }
    };

    /**
     * Xử lý khi upload ảnh thành công từ Cloudinary
     * @param {string} imageUrl - URL ảnh từ Cloudinary
     */
    const handleImageUpload = (imageUrl) => {
        setFormData(prev => ({
            ...prev,
            mainImage: imageUrl
        }));
    };

    /**
     * Submit form tạo listing - Không cần admin duyệt, đăng trực tiếp
     */
    const handleSubmit = async (e) => {
        e.preventDefault();

        try {
            setLoading(true);
            setError(null);

            // Validate dữ liệu đầu vào trước khi gửi yêu cầu đăng bài
            if (!formData.title.trim()) {
                throw new Error('Vui lòng nhập tiêu đề bài đăng');
            }

            if (!formData.price || formData.price <= 0) {
                throw new Error('Vui lòng nhập giá bán hợp lệ');
            }

            if (!formData.location.province.trim()) {
                throw new Error('Vui lòng chọn tỉnh/thành phố');
            }

            // Kiểm tra authentication - cần có user để làm seller
            const userFromContext = user;
            const userFromLocalStorage = JSON.parse(localStorage.getItem('user') || 'null');
            const userFromSessionStorage = JSON.parse(sessionStorage.getItem('user') || 'null');
            const userFromUserInfo = JSON.parse(localStorage.getItem('userInfo') || 'null');

            console.log('🔍 AUTHENTICATION DEBUG:', {
                userFromContext: userFromContext,
                userFromLocalStorage: userFromLocalStorage,
                userFromSessionStorage: userFromSessionStorage,
                userFromUserInfo: userFromUserInfo,
                localStorage_keys: Object.keys(localStorage),
                sessionStorage_keys: Object.keys(sessionStorage)
            });

            let currentUser = userFromContext || userFromUserInfo || userFromLocalStorage || userFromSessionStorage;

            // FALLBACK: Nếu không tìm thấy user, thử tạo từ các thông tin có sẵn
            if (!currentUser && localStorage.getItem('isLoggedIn') === 'true') {
                console.warn('⚠️ User data not found but isLoggedIn=true, creating fallback user');
                currentUser = {
                    id: 1, // Fallback user ID - thay đổi theo user thực tế
                    username: 'test_user',
                    email: 'Saitohtedofu1982@gamil.com'
                };
            }

            console.log('👤 Final currentUser:', currentUser);

            if (!currentUser || !currentUser.id) {
                console.error('❌ Authentication failed - currentUser:', currentUser);
                console.error('❌ Please login again or check user data storage');
                throw new Error('Vui lòng đăng nhập lại để tạo listing');
            }

            // Validate dữ liệu theo loại sản phẩm với DB constraints
            if (formData.productType === 'VEHICLE') {
                if (!formData.vehicle.name.trim() || !formData.vehicle.brand.trim()) {
                    throw new Error('Vui lòng nhập đầy đủ thông tin xe điện');
                }
                if (!formData.vehicle.type || !['CAR', 'BIKE', 'MOTORBIKE'].includes(formData.vehicle.type)) {
                    throw new Error('Loại xe không hợp lệ');
                }
                if (formData.vehicle.year < 2000 || formData.vehicle.year > new Date().getFullYear() + 1) {
                    throw new Error('Năm sản xuất không hợp lệ');
                }
            } else {
                if (!formData.battery.brand.trim() || formData.battery.capacity <= 0) {
                    throw new Error('Vui lòng nhập đầy đủ thông tin pin');
                }
                if (formData.battery.healthPercentage < 0 || formData.battery.healthPercentage > 100) {
                    throw new Error('Độ khỏe pin phải từ 0-100%');
                }
            }

            // Tạo payload theo ĐÚNG Backend DTO CreateListingFormDTO
            const payload = {
                // Listing basic information
                title: formData.title.trim(),
                description: formData.description?.trim() || null,
                price: Number(formData.price),
                listingType: 'NORMAL', // Backend sẽ map thành database enum: NORMAL, PREMIUM, FEATURED
                category: formData.productType, // 'VEHICLE' or 'BATTERY' để backend biết loại sản phẩm

                // Cloudinary image URLs (đã được upload từ frontend)
                mainImageUrl: formData.mainImage || null,
                imageUrls: formData.mainImage ? [formData.mainImage] : [],

                // Product object theo Backend DTO structure
                product: formData.productType === 'VEHICLE' ? {
                    // Nếu là EV Vehicle - chỉ có ev, battery = null
                    ev: {
                        type: formData.vehicle.type, // CAR, BIKE, MOTORBIKE
                        name: formData.vehicle.name.trim(),
                        model: formData.vehicle.model?.trim() || null,
                        brand: formData.vehicle.brand.trim(),
                        year: Number(formData.vehicle.year),
                        mileage: Number(formData.vehicle.mileage) || 0,
                        batteryCapacity: Number(formData.vehicle.batteryCapacity) || 0.0,
                        conditionStatus: formData.vehicle.conditionStatus // EXCELLENT, GOOD, FAIR, POOR, NEEDS_MAINTENANCE
                    },
                    battery: null
                } : {
                    // Nếu là Battery - chỉ có battery, ev = null
                    ev: null,
                    battery: {
                        brand: formData.battery.brand.trim(),
                        model: formData.battery.model?.trim() || null,
                        capacity: Number(formData.battery.capacity),
                        healthPercentage: Number(formData.battery.healthPercentage),
                        compatibleVehicles: formData.battery.compatibleVehicles?.trim() || null,
                        conditionStatus: formData.battery.conditionStatus // EXCELLENT, GOOD, FAIR, POOR, NEEDS_REPLACEMENT
                    }
                },

                // Location object theo Backend DTO structure
                location: {
                    province: formData.location.province.trim(),
                    district: formData.location.district?.trim() || null,
                    details: formData.location.details?.trim() || null
                }
            };

            // Debug payload trước khi gửi với Backend DTO mapping
            console.log('DEBUG - Dữ liệu form chuẩn bị đăng bài:', formData);
            console.log('Thông tin xác thực người dùng:', {
                user_from_context: user,
                user_from_storage: JSON.parse(localStorage.getItem('user') || sessionStorage.getItem('user') || 'null'),
                currentUser: currentUser,
                hasValidAuth: !!(currentUser && currentUser.id)
            });

            console.log('Payload sẽ gửi cho backend (tạo bài chờ duyệt):', payload);

            console.log('✅ Backend DTO compliance check:', {
                has_required_listing_fields: !!(payload.title && payload.price && payload.listingType),
                has_product_object: !!payload.product,
                has_location_object: !!payload.location,
                product_type: payload.category,
                has_ev_or_battery: !!(payload.product?.ev || payload.product?.battery),
                ev_vehicle_details: payload.product?.ev,
                battery_details: payload.product?.battery,
                location_details: payload.location,
                mainImageUrl: payload.mainImageUrl,
                imageUrls_count: payload.imageUrls?.length || 0
            });

            console.log('Gửi yêu cầu đăng bài đến backend (cần admin xét duyệt)...');

            // QUAN TRỌNG: Ảnh đã được upload lên Cloudinary rồi (qua CloudinaryImageUpload component)
            // Chỉ cần gửi Cloudinary URL trong JSON payload, KHÔNG cần upload lại

            // Kiểm tra ảnh đã được upload lên Cloudinary chưa
            if (!formData.mainImage || !formData.mainImage.startsWith('http')) {
                throw new Error('Vui lòng upload ảnh trước khi đăng bài');
            }

            console.log('✅ Ảnh đã có sẵn từ Cloudinary:', formData.mainImage);

            console.log('Final payload với Cloudinary URLs:', payload);

            // Gửi JSON request (không phải multipart) vì ảnh đã có sẵn trên Cloudinary
            const response = await api.post('/listings/create', payload);

            console.log('Response từ backend:', {
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
                data: response.data
            });

            if (response.status === 200 || response.status === 201) {
                console.log('✅ Đăng bài thành công! Bài đăng đang chờ admin xét duyệt.');
                setSuccess(true);

                // Hiển thị thông báo và chuyển về home screen sau 3 giây
                setTimeout(() => {
                    // Reset form về trạng thái ban đầu để chuẩn bị cho lần đăng bài tiếp theo
                    setFormData({
                        title: '',
                        description: '',
                        price: '',
                        mainImage: '',
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
                    });

                    setSuccess(false);
                    setCurrentStep(1);

                    // Chuyển về trang chủ thay vì trang listing cụ thể
                    // Vì bài đăng cần được admin duyệt trước khi hiện ra trong listing
                    console.log('Chuyển hướng về trang chủ...');
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
     * Điều hướng đến bước tiếp theo
     * Chỉ cho phép next nếu chưa đến bước cuối
     */
    const nextStep = () => {
        if (currentStep < steps.length && validateStep(currentStep)) {
            setCurrentStep(currentStep + 1);
        }
    };

    /**
     * Quay lại bước trước đó
     * Chỉ cho phép back nếu không phải bước đầu
     */
    const prevStep = () => {
        if (currentStep > 1) {
            setCurrentStep(currentStep - 1);
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
    const validateStep = (step) => {
        switch (step) {
            case 1:
                // Bước 1: Kiểm tra thông tin cơ bản
                return formData.title.trim().length > 0 && formData.price > 0;

            case 2:
                // Bước 2: Kiểm tra thông tin sản phẩm theo loại với validation chi tiết
                if (formData.productType === 'VEHICLE') {
                    return formData.vehicle.name.trim().length > 0 &&
                        formData.vehicle.brand.trim().length > 0 &&
                        formData.vehicle.year >= 2000 && // Xe phải từ năm 2000 trở lên
                        formData.vehicle.year <= new Date().getFullYear() + 1; // Không quá năm hiện tại + 1
                } else {
                    return formData.battery.brand.trim().length > 0 &&
                        formData.battery.capacity > 0 &&
                        formData.battery.healthPercentage > 0 &&
                        formData.battery.healthPercentage <= 100; // Độ khỏe pin từ 1-100%
                }

            case 3:
                // Bước 3: Kiểm tra thông tin vị trí (chỉ cần tỉnh/thành phố)
                return formData.location.province.trim().length > 0;

            default:
                return true;
        }
    };

    // Kiểm tra xem có thể tiếp tục bước tiếp theo không
    const canProceed = validateStep(currentStep);

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
                        {/* Card chọn xe điện */}
                        <div
                            className={`product-type-card ${formData.productType === 'VEHICLE' ? 'selected' : ''}`}
                            onClick={() => setFormData(prev => ({ ...prev, productType: 'VEHICLE' }))}
                        >
                            <div className="product-type-icon">
                                <Car className="w-8 h-8" />
                            </div>
                            <h3 className="product-type-title">Xe Điện</h3>
                            <p className="product-type-description">
                                Đăng bán ô tô điện, xe máy điện, xe đạp điện và các loại xe điện khác
                            </p>
                        </div>

                        {/* Card chọn pin */}
                        <div
                            className={`product-type-card ${formData.productType === 'BATTERY' ? 'selected' : ''}`}
                            onClick={() => setFormData(prev => ({ ...prev, productType: 'BATTERY' }))}
                        >
                            <div className="product-type-icon">
                                <Battery className="w-8 h-8" />
                            </div>
                            <h3 className="product-type-title">Pin Xe Điện</h3>
                            <p className="product-type-description">
                                Đăng bán pin lithium, pin thay thế, pin cũ và phụ kiện pin xe điện
                            </p>
                        </div>
                    </div>
                )}

                {/* Progress Steps với CSS classes */}
                <div className="progress-stepper">
                    {steps.map((step, index) => {
                        const Icon = step.icon;
                        const isActive = currentStep === step.id;
                        const isCompleted = currentStep > step.id;

                        return (
                            <div key={step.id} className="step-item">
                                <div
                                    onClick={() => goToStep(step.id)}
                                    className={`step-icon ${
                                        isCompleted ? 'completed' : isActive ? 'active' : 'inactive'
                                    }`}
                                >
                                    {isCompleted ? <CheckCircle className="w-6 h-6" /> : <Icon className="w-6 h-6" />}
                                </div>
                                <div className="step-info">
                                    <div className="step-number">Bước {step.id}</div>
                                    <div className="step-title">{step.title}</div>
                                </div>
                                {index < steps.length - 1 && (
                                    <div className={`step-connector ${isCompleted ? 'completed' : 'inactive'}`} />
                                )}
                            </div>
                        );
                    })}
                </div>

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

                {/* Multi-Step Form Container */}
                <div className="form-container">
                    <form onSubmit={handleSubmit}>
                        {/* Step 1: Thông tin cơ bản */}
                        {currentStep === 1 && (
                            <div>
                                <div className="step-header">
                                    <h2 className="step-header-title">
                                        <FileText className="w-7 h-7 text-blue-500 mr-3" />
                                        Thông tin cơ bản
                                    </h2>
                                    <p className="step-header-subtitle">Nhập thông tin cơ bản về listing của bạn</p>
                                </div>

                                <div className="step-content">
                                    <div className="form-row cols-2">
                                        <div className="form-group">
                                            <label className="form-label required">Tiêu đề listing</label>
                                            <input
                                                type="text"
                                                name="title"
                                                value={formData.title}
                                                onChange={handleInputChange}
                                                className="form-input"
                                                placeholder="VD: VinFast VF8 2023 như mới"
                                                required
                                            />
                                        </div>

                                        <div className="form-group">
                                            <label className="form-label required">Giá bán (VND)</label>
                                            <div className="input-with-icon">
                                                <DollarSign className="input-icon w-5 h-5" />
                                                <input
                                                    type="number"
                                                    name="price"
                                                    value={formData.price}
                                                    onChange={handleInputChange}
                                                    className="form-input with-icon"
                                                    placeholder="500000000"
                                                    required
                                                />
                                            </div>
                                        </div>
                                    </div>

                                    <div className="form-group">
                                        <label className="form-label">Mô tả chi tiết</label>
                                        <textarea
                                            name="description"
                                            value={formData.description}
                                            onChange={handleInputChange}
                                            rows={4}
                                            className="form-textarea"
                                            placeholder="Mô tả chi tiết về sản phẩm, tình trạng, lịch sử sử dụng..."
                                        />
                                    </div>

                                    {/* Loại bỏ "Loại listing" và "Loại sản phẩm" vì đã có selector ở trên */}

                                    <div className="form-group">
                                        <label className="form-label">Ảnh chính sản phẩm</label>
                                        <CloudinaryImageUpload
                                            onImageUpload={handleImageUpload}
                                            currentImage={formData.mainImage}
                                            disabled={loading}
                                            className="mt-2"
                                        />
                                        <p className="text-sm text-gray-500 mt-2">
                                            Upload ảnh lên cloud để hiển thị sản phẩm của bạn một cách tốt nhất
                                        </p>
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* Step 2: Chi tiết sản phẩm */}
                        {currentStep === 2 && (
                            <div>
                                <div className="step-header">
                                    <h2 className="step-header-title">
                                        {formData.productType === 'VEHICLE'
                                            ? <Car className="w-7 h-7 text-blue-500 mr-3" />
                                            : <Battery className="w-7 h-7 text-blue-500 mr-3" />
                                        }
                                        Chi tiết sản phẩm
                                    </h2>
                                    <p className="step-header-subtitle">
                                        Nhập thông tin chi tiết về {formData.productType === 'VEHICLE' ? 'xe điện' : 'pin'}
                                    </p>
                                </div>

                                <div className="step-content">
                                    {/* Form riêng biệt cho xe điện */}
                                    {formData.productType === 'VEHICLE' && (
                                        <div className="vehicle-form-section">
                                            <div className="vehicle-form-header">
                                                <Car className="w-6 h-6 text-blue-600" />
                                                <h3>Thông tin xe điện</h3>
                                            </div>

                                            <div className="form-row cols-3">
                                                <div className="form-group">
                                                    <label className="form-label required">Loại xe</label>
                                                    <select
                                                        name="vehicle.type"
                                                        value={formData.vehicle.type}
                                                        onChange={handleInputChange}
                                                        className="form-select"
                                                    >
                                                        <option value="CAR">Ô tô điện</option>
                                                        <option value="BIKE">Xe đạp điện</option>
                                                        <option value="MOTORBIKE">Xe máy điện</option>
                                                    </select>
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label required">Hãng xe</label>
                                                    <input
                                                        type="text"
                                                        name="vehicle.brand"
                                                        value={formData.vehicle.brand}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: VinFast"
                                                        required
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label required">Tên xe</label>
                                                    <input
                                                        type="text"
                                                        name="vehicle.name"
                                                        value={formData.vehicle.name}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: VF8"
                                                        required
                                                    />
                                                </div>
                                            </div>

                                            <div className="form-row cols-3">
                                                <div className="form-group">
                                                    <label className="form-label">Model</label>
                                                    <input
                                                        type="text"
                                                        name="vehicle.model"
                                                        value={formData.vehicle.model}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: Plus"
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label required">Năm sản xuất</label>
                                                    <input
                                                        type="number"
                                                        name="vehicle.year"
                                                        value={formData.vehicle.year}
                                                        onChange={handleInputChange}
                                                        min="2000"
                                                        max={new Date().getFullYear() + 1}
                                                        className="form-input"
                                                        required
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label">Số km đã đi</label>
                                                    <input
                                                        type="number"
                                                        name="vehicle.mileage"
                                                        value={formData.vehicle.mileage}
                                                        onChange={handleInputChange}
                                                        min="0"
                                                        className="form-input"
                                                        placeholder="0"
                                                    />
                                                </div>
                                            </div>

                                            <div className="form-row cols-2">
                                                <div className="form-group">
                                                    <label className="form-label">Dung lượng pin (kWh)</label>
                                                    <input
                                                        type="number"
                                                        name="vehicle.batteryCapacity"
                                                        value={formData.vehicle.batteryCapacity}
                                                        onChange={handleInputChange}
                                                        min="0"
                                                        step="0.1"
                                                        className="form-input"
                                                        placeholder="75.3"
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label required">Tình trạng</label>
                                                    <select
                                                        name="vehicle.conditionStatus"
                                                        value={formData.vehicle.conditionStatus}
                                                        onChange={handleInputChange}
                                                        className="form-select"
                                                    >
                                                        <option value="EXCELLENT">⭐ Xuất sắc</option>
                                                        <option value="GOOD">👍 Tốt</option>
                                                        <option value="FAIR">👌 Khá</option>
                                                        <option value="POOR">👎 Kém</option>
                                                        <option value="NEEDS_MAINTENANCE">🔧 Cần bảo trì</option>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    )}

                                    {/* Form riêng biệt cho pin */}
                                    {formData.productType === 'BATTERY' && (
                                        <div className="battery-form-section">
                                            <div className="battery-form-header">
                                                <Battery className="w-6 h-6 text-amber-600" />
                                                <h3>Thông tin pin xe điện</h3>
                                            </div>

                                            <div className="form-row cols-2">
                                                <div className="form-group">
                                                    <label className="form-label required">Hãng pin</label>
                                                    <input
                                                        type="text"
                                                        name="battery.brand"
                                                        value={formData.battery.brand}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: CATL, LG Chem, BYD"
                                                        required
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label">Model pin</label>
                                                    <input
                                                        type="text"
                                                        name="battery.model"
                                                        value={formData.battery.model}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: NCM523, LFP"
                                                    />
                                                </div>
                                            </div>

                                            <div className="form-row cols-2">
                                                <div className="form-group">
                                                    <label className="form-label required">Dung lượng (kWh)</label>
                                                    <input
                                                        type="number"
                                                        name="battery.capacity"
                                                        value={formData.battery.capacity}
                                                        onChange={handleInputChange}
                                                        min="0"
                                                        step="0.1"
                                                        className="form-input"
                                                        placeholder="75.3"
                                                        required
                                                    />
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label required">Độ khỏe pin (%)</label>
                                                    <input
                                                        type="number"
                                                        name="battery.healthPercentage"
                                                        value={formData.battery.healthPercentage}
                                                        onChange={handleInputChange}
                                                        min="0"
                                                        max="100"
                                                        className="form-input"
                                                        required
                                                    />
                                                </div>
                                            </div>

                                            <div className="form-row cols-2">
                                                <div className="form-group">
                                                    <label className="form-label required">Tình trạng</label>
                                                    <select
                                                        name="battery.conditionStatus"
                                                        value={formData.battery.conditionStatus}
                                                        onChange={handleInputChange}
                                                        className="form-select"
                                                    >
                                                        <option value="EXCELLENT">⭐ Xuất sắc</option>
                                                        <option value="GOOD">👍 Tốt</option>
                                                        <option value="FAIR">👌 Khá</option>
                                                        <option value="POOR">👎 Kém</option>
                                                        <option value="NEEDS_REPLACEMENT">🔄 Cần thay thế</option>
                                                    </select>
                                                </div>

                                                <div className="form-group">
                                                    <label className="form-label">Xe tương thích</label>
                                                    <input
                                                        type="text"
                                                        name="battery.compatibleVehicles"
                                                        value={formData.battery.compatibleVehicles}
                                                        onChange={handleInputChange}
                                                        className="form-input"
                                                        placeholder="VD: VinFast VF8, VF9"
                                                    />
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}

                        {/* Step 3: Vị trí & Hoàn tất */}
                        {currentStep === 3 && (
                            <div>
                                <div className="step-header">
                                    <h2 className="step-header-title">
                                        <MapPin className="w-7 h-7 text-blue-500 mr-3" />
                                        Vị trí & Hoàn tất
                                    </h2>
                                    <p className="step-header-subtitle">Nhập thông tin vị trí và xem lại toàn bộ listing</p>
                                </div>

                                <div className="step-content">
                                    {/* Thông tin vị trí */}
                                    <div className="form-group mb-6">
                                        <h3 className="text-lg font-semibold text-gray-900 mb-4 flex items-center">
                                            <MapPin className="w-5 h-5 text-blue-500 mr-2" />
                                            Thông tin vị trí
                                        </h3>
                                        <div className="form-row cols-2">
                                            <div className="form-group">
                                                <label className="form-label required">Tỉnh/Thành phố</label>
                                                <input
                                                    type="text"
                                                    name="location.province"
                                                    value={formData.location.province}
                                                    onChange={handleInputChange}
                                                    className="form-input"
                                                    placeholder="VD: Hà Nội"
                                                    required
                                                />
                                            </div>

                                            <div className="form-group">
                                                <label className="form-label">Quận/Huyện</label>
                                                <input
                                                    type="text"
                                                    name="location.district"
                                                    value={formData.location.district}
                                                    onChange={handleInputChange}
                                                    className="form-input"
                                                    placeholder="VD: Ba Đình"
                                                />
                                            </div>
                                        </div>

                                        <div className="form-group">
                                            <label className="form-label">Địa chỉ chi tiết</label>
                                            <textarea
                                                name="location.details"
                                                value={formData.location.details}
                                                onChange={handleInputChange}
                                                rows={3}
                                                className="form-textarea"
                                                placeholder="Số nhà, tên đường, phường/xã..."
                                            />
                                        </div>
                                    </div>

                                    {/* Xem lại thông tin */}
                                    <div className="review-section">
                                        <div className="review-header">
                                            <CheckCircle className="w-6 h-6" />
                                            <h3>Xem lại thông tin listing</h3>
                                        </div>
                                        <div className="space-y-3">
                                            <div className="review-item">
                                                <span className="review-label">Tiêu đề:</span>
                                                <span className="review-value">{formData.title || 'Chưa nhập'}</span>
                                            </div>
                                            <div className="review-item">
                                                <span className="review-label">Giá:</span>
                                                <span className="review-value font-bold">
                          {formData.price ? `${formData.price.toLocaleString()} VND` : 'Chưa nhập'}
                        </span>
                                            </div>
                                            <div className="review-item">
                                                <span className="review-label">Loại sản phẩm:</span>
                                                <span className="review-value">{formData.productType === 'VEHICLE' ? 'Xe điện' : 'Pin'}</span>
                                            </div>
                                            {formData.productType === 'VEHICLE' && (
                                                <div className="review-item">
                                                    <span className="review-label">Xe:</span>
                                                    <span className="review-value">
                            {formData.vehicle.brand} {formData.vehicle.name} ({formData.vehicle.year})
                          </span>
                                                </div>
                                            )}
                                            {formData.productType === 'BATTERY' && (
                                                <div className="review-item">
                                                    <span className="review-label">Pin:</span>
                                                    <span className="review-value">
                            {formData.battery.brand} - {formData.battery.capacity}kWh ({formData.battery.healthPercentage}%)
                          </span>
                                                </div>
                                            )}
                                            <div className="review-item">
                                                <span className="review-label">Vị trí:</span>
                                                <span className="review-value">{formData.location.province || 'Chưa nhập'}</span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
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
                                        disabled={!canProceed}
                                        className={`nav-button nav-button-next ${!canProceed ? 'opacity-50 cursor-not-allowed' : ''}`}
                                    >
                                        Tiếp tục
                                        <ChevronRight className="w-5 h-5 ml-2" />
                                    </button>
                                ) : (
                                    <button
                                        type="submit"
                                        disabled={loading || !canProceed}
                                        className={`nav-button nav-button-submit ${loading || !canProceed ? 'opacity-50 cursor-not-allowed' : ''}`}
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
                </div>
            </div>
        </div>
    );
}

export default CreateListing;