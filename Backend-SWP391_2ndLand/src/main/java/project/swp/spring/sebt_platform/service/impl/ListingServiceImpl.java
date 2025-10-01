package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.model.BatteryEntity;
import project.swp.spring.sebt_platform.model.EvVehicleEntity;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.ListingImageEntity;
import project.swp.spring.sebt_platform.model.LocationEntity;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.ProductEntity;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.repository.BatteryRepository;
import project.swp.spring.sebt_platform.repository.EvVehicleRepository;
import project.swp.spring.sebt_platform.repository.ListingImageRepository;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.repository.LocationRepository;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.ProductRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.ListingService;

@Service
public class ListingServiceImpl implements ListingService {

    private final PostRequestRepository postRequestRepository;
    private final EvVehicleRepository evVehicleRepository;
    private final BatteryRepository batteryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final LocationRepository locationRepository;

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository, 
                             UserRepository userRepository,
                             EvVehicleRepository evVehicleRepository,
                             BatteryRepository batteryRepository,
                             ProductRepository productRepository,
                             ListingRepository listingRepository,
                             ListingImageRepository listingImageRepository,
                             LocationRepository locationRepository) {
        this.postRequestRepository = postRequestRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.locationRepository = locationRepository;
        this.evVehicleRepository = evVehicleRepository;
        this.batteryRepository = batteryRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord) {
        return List.of();
    }

    @Override
    @Transactional
    public boolean createListing(CreateListingFormDTO createListingForm,
                                 Long sellerId,
                                 List<Image> imageUrls,
                                 Image thumbnailUrl) {
        try {
            System.out.println("🔥 [SERVICE DEBUG] Bắt đầu createListing service...");
            System.out.println("📝 [SERVICE DEBUG] Params - sellerId: " + sellerId);
            System.out.println("📝 [SERVICE DEBUG] Params - imageUrls count: " + (imageUrls != null ? imageUrls.size() : "null"));
            System.out.println("📝 [SERVICE DEBUG] Params - thumbnailUrl: " + (thumbnailUrl != null ? "present" : "null"));
            System.out.println("📝 [SERVICE DEBUG] Params - createListingForm: " + createListingForm);
            // Bước 1: Validate đầu vào
            if (createListingForm == null) {
                System.err.println("❌ [SERVICE DEBUG] Create listing form is null");
                return false;
            }

            if (createListingForm.location() == null) {
                System.err.println("❌ [SERVICE DEBUG] Location is null");
                return false;
            }

            if (createListingForm.product() == null) {
                System.err.println("❌ [SERVICE DEBUG] Product is null");
                return false;
            }

            if (createListingForm.product().ev() == null && createListingForm.product().battery() == null) {
                System.err.println("❌ [SERVICE DEBUG] Both EV vehicle and Battery details are null");
                return false;
            }
            System.out.println("✅ [SERVICE DEBUG] Validation passed");

            // Bước 2: Tìm user trong database
            System.out.println("🔄 [SERVICE DEBUG] Tìm user với ID: " + sellerId);
            UserEntity user = userRepository.findById(sellerId).orElse(null);
            if (user == null) {
                System.err.println("❌ [SERVICE DEBUG] User not found with ID: " + sellerId);
                return false;
            }
            System.out.println("✅ [SERVICE DEBUG] User found: " + user.getUsername());

            // Bước 3: Tạo product entity (EV hoặc Battery)
            System.out.println("🔄 [SERVICE DEBUG] Tạo ProductEntity...");
            ProductEntity productEntity = new ProductEntity();

            if (createListingForm.product().ev() != null) {
                System.out.println("🚗 [SERVICE DEBUG] Tạo EV Vehicle...");
                EvVehicleEntity evVehicleEntity = new EvVehicleEntity();
                evVehicleEntity.setName(createListingForm.product().ev().name());
                evVehicleEntity.setBrand(createListingForm.product().ev().brand());
                evVehicleEntity.setModel(createListingForm.product().ev().model());
                evVehicleEntity.setYear(createListingForm.product().ev().year());
                evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(createListingForm.product().ev().batteryCapacity()));
                evVehicleEntity.setConditionStatus(createListingForm.product().ev().conditionStatus());
                evVehicleEntity.setMileage(createListingForm.product().ev().mileage());
                evVehicleEntity.setType(createListingForm.product().ev().type());

                // Save EV vehicle first
                evVehicleEntity = evVehicleRepository.save(evVehicleEntity);
                productEntity.setEvVehicle(evVehicleEntity);
            }

            if (createListingForm.product().battery() != null) {
                BatteryEntity batteryEntity = new BatteryEntity();
                batteryEntity.setBrand(createListingForm.product().battery().brand());
                batteryEntity.setModel(createListingForm.product().battery().model());
                batteryEntity.setHealthPercentage(createListingForm.product().battery().healthPercentage());
                batteryEntity.setCapacity(BigDecimal.valueOf(createListingForm.product().battery().capacity()));
                batteryEntity.setCompatibleVehicles(createListingForm.product().battery().compatibleVehicles());
                batteryEntity.setConditionStatus(createListingForm.product().battery().conditionStatus());

                // Save battery first
                batteryEntity = batteryRepository.save(batteryEntity);
                productEntity.setBattery(batteryEntity);
            }

            // Save product
            productEntity = productRepository.save(productEntity);

            // Create and save listing
            ListingEntity listingEntity = new ListingEntity();
            listingEntity.setTitle(createListingForm.title());
            listingEntity.setDescription(createListingForm.description());
            listingEntity.setListingType(createListingForm.listingType());
            listingEntity.setPrice(BigDecimal.valueOf(createListingForm.price()));
            listingEntity.setSeller(user);
            listingEntity.setProduct(productEntity);

            // Bước 1: Lưu ảnh thumbnail chính cho listing từ Cloudinary URL
            String mainImageUrl = createListingForm.mainImageUrl();
            if (mainImageUrl != null && !mainImageUrl.isEmpty()) {
                // Extract public ID từ Cloudinary URL để có thể xóa sau này nếu cần
                String publicId = extractPublicIdFromCloudinaryUrl(mainImageUrl);
                listingEntity.setThumbnailPublicId(publicId);
                listingEntity.setThumbnailImage(mainImageUrl);
                System.out.println("✅ [SERVICE DEBUG] Main image URL được thiết lập: " + mainImageUrl);
            } else {
                System.err.println("❌ [SERVICE DEBUG] Main image URL trống - không thể tạo listing");
                return false;
            }

            // Bước 2: Lưu listing trước để có ID (cần thiết cho các entity liên quan)
            listingEntity = listingRepository.save(listingEntity);

            // Bước 3: Lưu các ảnh chi tiết của listing từ Cloudinary URLs
            List<String> imageUrlsList = createListingForm.imageUrls();
            if (imageUrlsList != null && !imageUrlsList.isEmpty()) {
                List<ListingImageEntity> listingImageEntities = new ArrayList<>();
                for (String imageUrl : imageUrlsList) {
                    ListingImageEntity listingImageEntity = new ListingImageEntity();
                    listingImageEntity.setImageUrl(imageUrl);
                    listingImageEntity.setPublicId(extractPublicIdFromCloudinaryUrl(imageUrl));
                    listingImageEntity.setListing(listingEntity);
                    listingImageEntities.add(listingImageEntity);
                }
                listingImageRepository.saveAll(listingImageEntities);
                System.out.println("✅ [SERVICE DEBUG] Lưu " + imageUrlsList.size() + " ảnh chi tiết thành công");
            } else {
                System.err.println("❌ [SERVICE DEBUG] Danh sách ảnh listing trống");
                return false;
            }

            // Bước 4: Tạo và lưu thông tin địa điểm cho listing
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.setProvince(createListingForm.location().province());
            locationEntity.setDistrict(createListingForm.location().district());
            locationEntity.setDetails(createListingForm.location().details());
            locationEntity.setListing(listingEntity);
            locationRepository.save(locationEntity);

            // Bước 5: TẠO YÊU CẦU XÉT DUYỆT - đây là bước quan trọng nhất
            // Listing sẽ ở trạng thái PENDING và chỉ hiển thị khi admin APPROVE
            PostRequestEntity postRequestEntity = new PostRequestEntity();
            postRequestEntity.setStatus(ApprovalStatus.PENDING); // Chờ admin xét duyệt
            postRequestEntity.setListing(listingEntity);
            postRequestRepository.save(postRequestEntity);

            return true;

        } catch (Exception e) {
            System.err.println("❌ LỖI trong quá trình tạo listing và yêu cầu xét duyệt: " + e.getMessage());
            e.printStackTrace(); // In chi tiết lỗi để debug
            // Trong môi trường production nên sử dụng proper logging (log4j, slf4j)
            return false;
        }
    }

    @Override
    public List<ListingCartResponseDTO> getAllActiveListingCarts() {

        return List.of();
    }

    @Override
    public ListingDetailResponseDTO getListingDetailById(Long listingId) {
        return null;
    }

    @Override
    public List<ListingCartResponseDTO> getCarListingCarts() {
        return List.of();
    }

    @Override
    public List<ListingCartResponseDTO> getPinListingCarts() {
        return List.of();
    }

    @Override
    public List<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId) {
        return List.of();
    }

    @Override
    public int deleteListingImages(List<String> publicIds) {
        return 0;
    }

    /**
     * Helper method để extract public ID từ Cloudinary URL
     * Ví dụ: https://res.cloudinary.com/demo/image/upload/v1234567890/sample.jpg -> sample
     */
    private String extractPublicIdFromCloudinaryUrl(String cloudinaryUrl) {
        try {
            if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
                return null;
            }
            
            // Tìm phần cuối của URL sau /upload/
            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }
            
            // Lấy phần sau /upload/ và remove version number (vXXXXXXXX/)
            String afterUpload = parts[1];
            String[] segments = afterUpload.split("/");
            
            // Nếu có version number, bỏ qua segment đầu
            int startIndex = (segments.length > 1 && segments[0].startsWith("v")) ? 1 : 0;
            
            // Kết hợp các segment còn lại và remove file extension
            StringBuilder publicId = new StringBuilder();
            for (int i = startIndex; i < segments.length; i++) {
                if (i > startIndex) {
                    publicId.append("/");
                }
                // Remove file extension from last segment
                String segment = segments[i];
                if (i == segments.length - 1 && segment.contains(".")) {
                    segment = segment.substring(0, segment.lastIndexOf("."));
                }
                publicId.append(segment);
            }
            
            return publicId.toString();
        } catch (Exception e) {
            System.err.println("❌ [SERVICE DEBUG] Lỗi extract publicId từ URL: " + cloudinaryUrl);
            return cloudinaryUrl; // Fallback: return original URL
        }
    }

}
