package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingResponseDTO;
import project.swp.spring.sebt_platform.model.BatteryEntity;
import project.swp.spring.sebt_platform.model.EvVehicleEntity;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.ListingImageEntity;
import project.swp.spring.sebt_platform.model.LocationEntity;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.ProductEntity;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.repository.BatteryRepository;
import project.swp.spring.sebt_platform.repository.EvVehicleRepository;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.ProductRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.ListingService;

@Service
public class ListingServiceImpl implements ListingService {

    private final PostRequestRepository postRequestRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ProductRepository productRepository;
    private final BatteryRepository batteryRepository;
    private final EvVehicleRepository evVehicleRepository;

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository, 
                             UserRepository userRepository,
                             ListingRepository listingRepository,
                             ProductRepository productRepository,
                             BatteryRepository batteryRepository,
                             EvVehicleRepository evVehicleRepository) {
        this.postRequestRepository = postRequestRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.productRepository = productRepository;
        this.batteryRepository = batteryRepository;
        this.evVehicleRepository = evVehicleRepository;
    }

    @Override
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord) {
        return List.of();
    }

    @Override
    @Transactional
    public boolean createListingDirect(CreateListingFormDTO createListingForm, Long sellerId) {
        try {
            System.out.println("🚀 CREATE LISTING DIRECT - Starting...");
            System.out.println("📥 DTO received: " + createListingForm);
            System.out.println("👤 Seller ID: " + sellerId);
            
            if (createListingForm == null) {
                System.err.println("❌ Create listing form is null");
                return false;
            }

            if (createListingForm.location() == null) {
                System.err.println("Location is null");
                return false;
            }

            if (createListingForm.product() == null) {
                System.err.println("Product is null");
                return false;
            }

            if (createListingForm.product().ev() == null && createListingForm.product().battery() == null) {
                System.err.println("Both EV vehicle and Battery details are null");
                return false;
            }

            UserEntity user = userRepository.findById(sellerId).orElse(null);

            if (user == null) {
                System.err.println("User not found with ID: " + sellerId);
                return false;
            }

            // Create ListingEntity and set its fields
            ListingEntity listingEntity = new ListingEntity();
            listingEntity.setTitle(createListingForm.title());
            listingEntity.setDescription(createListingForm.description());
            listingEntity.setListingType(createListingForm.listingType());
            listingEntity.setMainImage(createListingForm.mainImage());
            listingEntity.setPrice(BigDecimal.valueOf(createListingForm.price()));
            listingEntity.setStatus(ListingStatus.ACTIVE); // Tạo trực tiếp với status ACTIVE

            listingEntity.setImages(new ArrayList<>());
            listingEntity.setSeller(user);

            // Create ProductEntity
            ProductEntity productEntity = new ProductEntity();

            // Set EV vehicle details if available
            if (createListingForm.product().ev() != null) {
                EvVehicleEntity evVehicleEntity = new EvVehicleEntity();
                evVehicleEntity.setName(createListingForm.product().ev().name());
                evVehicleEntity.setBrand(createListingForm.product().ev().brand());
                evVehicleEntity.setModel(createListingForm.product().ev().model());
                evVehicleEntity.setYear(createListingForm.product().ev().year());
                evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(createListingForm.product().ev().batteryCapacity()));
                evVehicleEntity.setConditionStatus(createListingForm.product().ev().conditionStatus());
                evVehicleEntity.setMileage(createListingForm.product().ev().mileage());
                evVehicleEntity.setType(createListingForm.product().ev().type());
                productEntity.setEvVehicle(evVehicleEntity);
            }

            // Set battery details if available
            if (createListingForm.product().battery() != null) {
                BatteryEntity batteryEntity = new BatteryEntity();
                batteryEntity.setBrand(createListingForm.product().battery().brand());
                batteryEntity.setModel(createListingForm.product().battery().model());
                batteryEntity.setHealthPercentage(createListingForm.product().battery().healthPercentage());
                batteryEntity.setCapacity(BigDecimal.valueOf(createListingForm.product().battery().capacity()));
                batteryEntity.setCompatibleVehicles(createListingForm.product().battery().compatibleVehicles());
                batteryEntity.setConditionStatus(createListingForm.product().battery().conditionStatus());
                productEntity.setBattery(batteryEntity);
            }
            
            // Set bidirectional relationship between listing and product
            listingEntity.setProduct(productEntity);
            productEntity.setListing(listingEntity);

            // Create LocationEntity and set its fields
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.setProvince(createListingForm.location().province());
            locationEntity.setDistrict(createListingForm.location().district());
            locationEntity.setDetails(createListingForm.location().details());
            listingEntity.setLocation(locationEntity);
            locationEntity.setListing(listingEntity);

            // Set listing images with bidirectional relationships
            if (createListingForm.listingImages() != null && !createListingForm.listingImages().isEmpty()) {
                for (var img : createListingForm.listingImages()) {
                    ListingImageEntity listingImageEntity = new ListingImageEntity();
                    listingImageEntity.setImageUrl(img.imageUrl());
                    listingImageEntity.setListing(listingEntity);
                    listingEntity.getImages().add(listingImageEntity);
                }
            }

            // Lưu trực tiếp listing mà không tạo PostRequest
            System.out.println("💾 Saving listing entity...");
            ListingEntity savedEntity = listingRepository.save(listingEntity);
            System.out.println("✅ Listing saved successfully with ID: " + savedEntity.getId());
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error in create listing direct: " + e.getMessage());
            System.err.println("❌ Exception class: " + e.getClass().getSimpleName());
            System.err.println("❌ Stack trace: ");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean createListing(CreateListingFormDTO createListingForm,Long sellerId) {
        try {
            if (createListingForm == null) {
                System.err.println("Create listing form is null");
                return false;
            }

            if (createListingForm.location() == null) {
                System.err.println("Location is null");
                return false;
            }

            if (createListingForm.product() == null) {
                System.err.println("Product is null");
                return false;
            }

            if (createListingForm.product().ev() == null && createListingForm.product().battery() == null) {
                System.err.println("Both EV vehicle and Battery details are null");
                return false;
            }

            UserEntity user = userRepository.findById(sellerId).orElse(null);

            if (user == null) {
                System.err.println("User not found with ID: " + sellerId);
                return false;
            }

            // Create ListingEntity and set its fields
            ListingEntity listingEntity = new ListingEntity();
            listingEntity.setTitle(createListingForm.title());
            listingEntity.setDescription(createListingForm.description());
            listingEntity.setListingType(createListingForm.listingType());
            listingEntity.setMainImage(createListingForm.mainImage());
            listingEntity.setPrice(BigDecimal.valueOf(createListingForm.price()));

            listingEntity.setImages(new ArrayList<>());
            listingEntity.setSeller(user);

            // Create ProductEntity
            ProductEntity productEntity = new ProductEntity();

            // Set EV vehicle details if available - ADD NULL CHECK
            if (createListingForm.product().ev() != null) {
                EvVehicleEntity evVehicleEntity = new EvVehicleEntity();
                evVehicleEntity.setName(createListingForm.product().ev().name());
                evVehicleEntity.setBrand(createListingForm.product().ev().brand());
                evVehicleEntity.setModel(createListingForm.product().ev().model());
                evVehicleEntity.setYear(createListingForm.product().ev().year());
                evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(createListingForm.product().ev().batteryCapacity()));
                evVehicleEntity.setConditionStatus(createListingForm.product().ev().conditionStatus());
                evVehicleEntity.setMileage(createListingForm.product().ev().mileage());
                evVehicleEntity.setType(createListingForm.product().ev().type());
                productEntity.setEvVehicle(evVehicleEntity);
            }

            // Set battery details if available - ADD NULL CHECK
            if (createListingForm.product().battery() != null) {
                BatteryEntity batteryEntity = new BatteryEntity();
                batteryEntity.setBrand(createListingForm.product().battery().brand());
                batteryEntity.setModel(createListingForm.product().battery().model());
                batteryEntity.setHealthPercentage(createListingForm.product().battery().healthPercentage());
                batteryEntity.setCapacity(BigDecimal.valueOf(createListingForm.product().battery().capacity()));
                batteryEntity.setCompatibleVehicles(createListingForm.product().battery().compatibleVehicles());
                batteryEntity.setConditionStatus(createListingForm.product().battery().conditionStatus());
                productEntity.setBattery(batteryEntity);
            }

            // Set bidirectional relationship between listing and product
            listingEntity.setProduct(productEntity);
            productEntity.setListing(listingEntity);

            // Create LocationEntity and set its fields
            LocationEntity locationEntity = new LocationEntity();
            locationEntity.setProvince(createListingForm.location().province());
            locationEntity.setDistrict(createListingForm.location().district());
            locationEntity.setDetails(createListingForm.location().details());

            listingEntity.setLocation(locationEntity);
            locationEntity.setListing(listingEntity);

            // Set listing images with bidirectional relationships - ADD NULL CHECK
            if (createListingForm.listingImages() != null && !createListingForm.listingImages().isEmpty()) {
                for (var img : createListingForm.listingImages()) {
                    ListingImageEntity listingImageEntity = new ListingImageEntity();
                    listingImageEntity.setImageUrl(img.imageUrl());
                    listingImageEntity.setListing(listingEntity);
                    listingEntity.getImages().add(listingImageEntity);
                }
            }

            // Create PostRequestEntity with bidirectional relationship
            PostRequestEntity postRequestEntity = new PostRequestEntity();
            postRequestEntity.setStatus(ApprovalStatus.PENDING);
            postRequestEntity.setListing(listingEntity);
            listingEntity.setPostRequests(postRequestEntity);

            // Save only PostRequestEntity - cascade will handle the rest
            postRequestRepository.save(postRequestEntity);
            return true;

        } catch (Exception e) {
            System.err.println("Error in create listing: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<ListingResponseDTO> getAllActiveListings() {
        try {
            // Lấy tất cả listing có status ACTIVE
            List<ListingEntity> listings = listingRepository.findByStatusOrderByCreatedAtDesc(ListingStatus.ACTIVE);
            return listings.stream()
                    .map(this::convertToListingResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting all active listings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public ListingResponseDTO getListingById(Long listingId) {
        try {
            ListingEntity listing = listingRepository.findById(listingId).orElse(null);
            if (listing == null) {
                return null;
            }
            return convertToListingResponseDTO(listing);
        } catch (Exception e) {
            System.err.println("Error getting listing by ID: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<ListingResponseDTO> getCarListings() {
        try {
            // Lấy tất cả listing có sản phẩm là xe ô tô (CAR)
            List<ListingEntity> carListings = listingRepository.findCarListingsByStatus(
                    ListingStatus.ACTIVE, VehicleType.CAR);
            return carListings.stream()
                    .map(this::convertToListingResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting car listings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ListingResponseDTO> getPinListings() {
        try {
            // Lấy tất cả listing có sản phẩm là pin
            List<ListingEntity> pinListings = listingRepository.findPinListingsByStatus(ListingStatus.ACTIVE);
            return pinListings.stream()
                    .map(this::convertToListingResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting pin listings: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ListingResponseDTO> getListingsBySeller(Long sellerId) {
        try {
            List<ListingEntity> listings = listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId);
            return listings.stream()
                    .map(this::convertToListingResponseDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error getting listings by seller: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void incrementViewCount(Long listingId) {
        try {
            ListingEntity listing = listingRepository.findById(listingId).orElse(null);
            if (listing != null) {
                listing.setViewsCount(listing.getViewsCount() + 1);
                listingRepository.save(listing);
            }
        } catch (Exception e) {
            System.err.println("Error incrementing view count: " + e.getMessage());
        }
    }

    /**
     * Helper method để chuyển đổi ListingEntity thành ListingResponseDTO
     * @param listing ListingEntity cần chuyển đổi
     * @return ListingResponseDTO
     */
    private ListingResponseDTO convertToListingResponseDTO(ListingEntity listing) {
        // Tạo thông tin người bán
        ListingResponseDTO.SellerInfoDTO sellerInfo = new ListingResponseDTO.SellerInfoDTO(
                listing.getSeller().getId(),
                listing.getSeller().getUsername(),
                listing.getSeller().getAvatar()
        );

        // Tạo thông tin sản phẩm
        ListingResponseDTO.ProductInfoDTO productInfo = createProductInfo(listing.getProduct());

        // Tạo thông tin vị trí
        ListingResponseDTO.LocationInfoDTO locationInfo = null;
        if (listing.getLocation() != null) {
            locationInfo = new ListingResponseDTO.LocationInfoDTO(
                    listing.getLocation().getId(),
                    listing.getLocation().getProvince(),
                    listing.getLocation().getDistrict(),
                    listing.getLocation().getDetails()
            );
        }

        // Tạo danh sách ảnh
        List<String> imageUrls = new ArrayList<>();
        if (listing.getImages() != null) {
            imageUrls = listing.getImages().stream()
                    .map(ListingImageEntity::getImageUrl)
                    .collect(Collectors.toList());
        }

        return new ListingResponseDTO(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getMainImage(),
                imageUrls,
                listing.getStatus(),
                listing.getListingType(),
                listing.getViewsCount(),
                listing.getCreatedAt(),
                listing.getUpdatedAt(),
                listing.getExpiresAt(),
                sellerInfo,
                productInfo,
                locationInfo
        );
    }

    /**
     * Helper method để tạo thông tin sản phẩm
     * @param product ProductEntity
     * @return ProductInfoDTO
     */
    private ListingResponseDTO.ProductInfoDTO createProductInfo(ProductEntity product) {
        String productType;
        ListingResponseDTO.VehicleInfoDTO vehicleInfo = null;
        ListingResponseDTO.BatteryInfoDTO batteryInfo = null;

        if (product.isVehicleProduct()) {
            productType = "VEHICLE";
            EvVehicleEntity vehicle = product.getEvVehicle();
            vehicleInfo = new ListingResponseDTO.VehicleInfoDTO(
                    vehicle.getId(),
                    vehicle.getType().toString(),
                    vehicle.getName(),
                    vehicle.getModel(),
                    vehicle.getBrand(),
                    vehicle.getYear(),
                    vehicle.getMileage(),
                    vehicle.getBatteryCapacity(),
                    vehicle.getConditionStatus().toString()
            );
        } else if (product.isBatteryProduct()) {
            productType = "BATTERY";
            BatteryEntity battery = product.getBattery();
            batteryInfo = new ListingResponseDTO.BatteryInfoDTO(
                    battery.getId(),
                    battery.getBrand(),
                    battery.getModel(),
                    battery.getCapacity(),
                    battery.getHealthPercentage(),
                    battery.getCompatibleVehicles(),
                    battery.getConditionStatus().toString()
            );
        } else {
            productType = "UNKNOWN";
        }

        return new ListingResponseDTO.ProductInfoDTO(
                product.getId(),
                productType,
                vehicleInfo,
                batteryInfo
        );
    }

}
