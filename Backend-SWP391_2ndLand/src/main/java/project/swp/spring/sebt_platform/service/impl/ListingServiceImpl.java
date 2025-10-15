package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import project.swp.spring.sebt_platform.dto.object.Battery;
import project.swp.spring.sebt_platform.dto.object.Ev;
import project.swp.spring.sebt_platform.dto.object.Image;
import project.swp.spring.sebt_platform.dto.object.Location;
import project.swp.spring.sebt_platform.dto.object.Product;
import project.swp.spring.sebt_platform.dto.object.Seller;
import project.swp.spring.sebt_platform.dto.request.BatteryFilterFormDTO;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.request.EvFilterFormDTO;
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
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.repository.BatteryRepository;
import project.swp.spring.sebt_platform.repository.EvVehicleRepository;
import project.swp.spring.sebt_platform.repository.FavoriteRepository;
import project.swp.spring.sebt_platform.repository.ListingImageRepository;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.repository.LocationRepository;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.ProductRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.ListingService;
import project.swp.spring.sebt_platform.service.ListingFeePolicy;
import project.swp.spring.sebt_platform.service.WalletLedgerService;

@Service
public class ListingServiceImpl implements ListingService {

    private static final Logger logger = LoggerFactory.getLogger(ListingServiceImpl.class);

    private final PostRequestRepository postRequestRepository;
    private final EvVehicleRepository evVehicleRepository;
    private final BatteryRepository batteryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final LocationRepository locationRepository;
    private final FavoriteRepository favoriteRepository;
    private final ListingFeePolicy listingFeePolicy;
    private final WalletLedgerService walletLedgerService;

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository,
                              UserRepository userRepository,
                              EvVehicleRepository evVehicleRepository,
                              BatteryRepository batteryRepository,
                              ProductRepository productRepository,
                              ListingRepository listingRepository,
                              ListingImageRepository listingImageRepository,
                              LocationRepository locationRepository,
                              FavoriteRepository favoriteRepository,
                              ListingFeePolicy listingFeePolicy,
                              WalletLedgerService walletLedgerService) {
        this.postRequestRepository = postRequestRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.locationRepository = locationRepository;
        this.evVehicleRepository = evVehicleRepository;
        this.batteryRepository = batteryRepository;
    this.productRepository = productRepository;
    this.favoriteRepository = favoriteRepository;
    this.listingFeePolicy = listingFeePolicy;
    this.walletLedgerService = walletLedgerService;
    }

    @Override
    public Page<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable) {
        try {
            if (keyWord == null || keyWord.trim().isEmpty()) {
                logger.warn("Search keyword is null or empty");
                return Page.empty(pageable);
            }


            Page<ListingEntity> listingsPage = listingRepository.findByTitleContainingIgnoreCaseAndStatus(
                    keyWord.trim(), ListingStatus.ACTIVE, pageable);


            return listingsPage.map(listing -> {
                boolean isFavorited = userId != null &&
                        favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;

                return new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        isFavorited
                );
            });
        } catch (Exception e) {
            logger.error("Error searching listings by keyword: " + keyWord, e);
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional
    public boolean createListing(CreateListingFormDTO createListingForm,
                                 Long sellerId, List<Image> listingImages) {
        logger.info("[CREATE_LISTING] Start - sellerId={} title='{}' price={} hasEV={} hasBattery={} imagesCount={}",
                sellerId,
                createListingForm != null ? createListingForm.getTitle() : null,
                createListingForm != null ? createListingForm.getPrice() : null,
                (createListingForm != null && createListingForm.getProduct() != null && createListingForm.getProduct().getEv() != null),
                (createListingForm != null && createListingForm.getProduct() != null && createListingForm.getProduct().getBattery() != null),
                listingImages != null ? listingImages.size() : 0
        );

        if (createListingForm == null) {
            logger.error("Create listing form is null");
            return false;
        }

        if (createListingForm.getLocation() == null) {
            logger.error("Location is null");
            return false;
        }

        if (createListingForm.getProduct() == null) {
            logger.error("Product is null");
            return false;
        }

        if (createListingForm.getProduct().getEv() == null && createListingForm.getProduct().getBattery() == null) {
            logger.error("Both EV vehicle and Battery details are null");
            return false;
        }

        UserEntity user = userRepository.findById(sellerId).orElse(null);
        if (user == null) {
            logger.error("User not found with ID: " + sellerId);
            return false;
        }

        // Create and save EV vehicle or Battery first
        ProductEntity productEntity = new ProductEntity();

        if (createListingForm.getProduct().getEv() != null) {
            Ev evDto = createListingForm.getProduct().getEv();
            logger.debug("[CREATE_LISTING] EV DTO -> type={} name='{}' brand='{}' year={} mileage={} batteryCapacity={} condition={}",
                    evDto.getType(), evDto.getName(), evDto.getBrand(), evDto.getYear(), evDto.getMileage(), evDto.getBatteryCapacity(), evDto.getConditionStatus());

            // Defensive validation for required EV fields
            if (evDto.getName() == null || evDto.getName().isBlank()) {
                logger.error("EV name is null/blank");
                return false;
            }
            if (evDto.getBrand() == null || evDto.getBrand().isBlank()) {
                logger.error("EV brand is null/blank");
                return false;
            }
            if (evDto.getType() == null) {
                logger.error("EV type is null");
                return false;
            }
            if (evDto.getYear() == null) {
                logger.error("EV year is null");
                return false;
            }

            EvVehicleEntity evVehicleEntity = new EvVehicleEntity();
            evVehicleEntity.setName(evDto.getName());
            evVehicleEntity.setBrand(evDto.getBrand());
            evVehicleEntity.setYear(evDto.getYear());
            if (evDto.getBatteryCapacity() > 0) {
                evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(evDto.getBatteryCapacity()));
            }
            evVehicleEntity.setConditionStatus(evDto.getConditionStatus() != null ? evDto.getConditionStatus() : VehicleCondition.GOOD);
            evVehicleEntity.setMileage(evDto.getMileage() != null ? evDto.getMileage() : 0);
            evVehicleEntity.setType(evDto.getType());
            evVehicleEntity = evVehicleRepository.save(evVehicleEntity);
            productEntity.setEvVehicle(evVehicleEntity);
        }

        if (createListingForm.getProduct().getBattery() != null) {
            Battery b = createListingForm.getProduct().getBattery();
            logger.debug("[CREATE_LISTING] Battery DTO -> brand={} capacity={} health%={} condition={}",
                    b.getBrand(), b.getCapacity(), b.getHealthPercentage(), b.getConditionStatus());
            BatteryEntity batteryEntity = new BatteryEntity();
            batteryEntity.setBrand(b.getBrand());
            batteryEntity.setHealthPercentage(b.getHealthPercentage());
            batteryEntity.setCapacity(BigDecimal.valueOf(b.getCapacity()));
            batteryEntity.setCompatibleVehicles(b.getCompatibleVehicles());
            batteryEntity.setConditionStatus(b.getConditionStatus());
            batteryEntity = batteryRepository.save(batteryEntity);
            productEntity.setBattery(batteryEntity);
        }

        // Save product
        productEntity = productRepository.save(productEntity);
        logger.debug("[CREATE_LISTING] Saved product id={} evId={} batteryId={}", productEntity.getId(),
                productEntity.getEvVehicle() != null ? productEntity.getEvVehicle().getId() : null,
                productEntity.getBattery() != null ? productEntity.getBattery().getId() : null);

        // Create and save listing
        ListingEntity listingEntity = new ListingEntity();
        listingEntity.setTitle(createListingForm.getTitle());
        listingEntity.setDescription(createListingForm.getDescription());
        if (createListingForm.getListingType() != null) {
            listingEntity.setListingType(createListingForm.getListingType());
        }
        listingEntity.setPrice(BigDecimal.valueOf(createListingForm.getPrice()));
        listingEntity.setSeller(user);
        listingEntity.setProduct(productEntity);

        // Set thumbnail - lấy ảnh đầu tiên từ listingImages làm thumbnail
        if (listingImages != null && !listingImages.isEmpty()) {
            listingEntity.setThumbnailImage(listingImages.get(0).getUrl());
            logger.debug("[CREATE_LISTING] Set thumbnail from first image: {}", listingImages.get(0).getUrl());
        } else {
            logger.error("Image list is null or empty - cannot create listing without images");
            return false;
        }

        // Save listing first to get ID (fee will be charged after building related entities)
        listingEntity = listingRepository.save(listingEntity);
        logger.debug("[CREATE_LISTING] Saved listing id={}", listingEntity.getId());

        // Save listing images với URL và publicId từ Cloudinary
        if (listingImages != null && !listingImages.isEmpty()) {
            List<ListingImageEntity> listingImageEntities = new ArrayList<>();
            for (Image image : listingImages) {
                ListingImageEntity listingImageEntity = new ListingImageEntity();
                listingImageEntity.setImageUrl(image.getUrl());
                listingImageEntity.setPublicId(image.getPublicId());
                listingImageEntity.setListing(listingEntity);
                listingImageEntities.add(listingImageEntity);
            }
            listingImageRepository.saveAll(listingImageEntities);
            logger.debug("[CREATE_LISTING] Saved {} listing images with publicIds", listingImageEntities.size());
        }

        // Create and save location
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setProvince(createListingForm.getLocation().getProvince());
        locationEntity.setDistrict(createListingForm.getLocation().getDistrict());
        locationEntity.setDetails(createListingForm.getLocation().getDetails());
        locationEntity.setListing(listingEntity);
        locationRepository.save(locationEntity);
        logger.debug("[CREATE_LISTING] Saved location id={}", locationEntity.getId());

        // Create and save post request
        PostRequestEntity postRequestEntity = new PostRequestEntity();
        postRequestEntity.setStatus(ApprovalStatus.PENDING);
        postRequestEntity.setListing(listingEntity);
        postRequestRepository.save(postRequestEntity);
        logger.debug("[CREATE_LISTING] Saved post request id={}", postRequestEntity.getId());

        logger.info("[CREATE_LISTING] SUCCESS listingId={} with {} images uploaded to Cloudinary",
                listingEntity.getId(), listingImages.size());
        return true;
    }

    @Override
    public ListingDetailResponseDTO getListingDetailById(Long listingId, Long userId) {
        ListingEntity listing = listingRepository.findById(listingId).orElse(null);
        List<ListingImageEntity> listingImageEntities = listingImageRepository.findByListingId(listingId);
        UserEntity user = userRepository.findById(userId).orElse(null);

        if (listing == null || listing.getStatus() != ListingStatus.ACTIVE) {
            logger.warn("Listing not found or inactive with ID: " + listingId);
            return null;
        }

        // Increment views count
        if (user != null && !Objects.equals(listing.getSeller().getId(), userId) && user.getRole() == UserRole.MEMBER) {
            listing.setViewsCount(listing.getViewsCount() + 1);
            listingRepository.save(listing);
        }

        // Convert to DTO
        ListingDetailResponseDTO detailDTO = new ListingDetailResponseDTO();

        detailDTO.setTitle(listing.getTitle());
        detailDTO.setDescription(listing.getDescription());
        detailDTO.setListingType(listing.getListingType());
        detailDTO.setId(listing.getId());
        detailDTO.setCreatedAt(listing.getCreatedAt().toString());
        detailDTO.setUpdatedAt(listing.getUpdatedAt().toString());
        detailDTO.setStatus(listing.getStatus().toString());
        detailDTO.setListingType(listing.getListingType());
        detailDTO.setPrice(listing.getPrice().doubleValue());
        detailDTO.setThumbnail(listing.getThumbnailImage());

        List<String> images = new ArrayList<>();
        for (ListingImageEntity listingImageEntity : listingImageEntities) {
            images.add(listingImageEntity.getImageUrl());
        }

        detailDTO.setImages(images);

        ProductEntity product = listing.getProduct();
        EvVehicleEntity evVehicleEntity = listing.getProduct().getEvVehicle();
        BatteryEntity batteryEntity = listing.getProduct().getBattery();

        Product productResp;
        if (product.getEvVehicle() != null) {
            productResp = new Product(new Ev(evVehicleEntity.getType(),
                    evVehicleEntity.getName(),
                    evVehicleEntity.getBrand(),
                    evVehicleEntity.getYear(),
                    evVehicleEntity.getMileage(),
                    evVehicleEntity.getBatteryCapacity().doubleValue(),
                    evVehicleEntity.getConditionStatus()), null);
        } else {
            productResp = new Product(null,
                    new Battery(batteryEntity.getBrand(),
                            batteryEntity.getCapacity().doubleValue(),
                            batteryEntity.getHealthPercentage(),
                            batteryEntity.getCompatibleVehicles(),
                            batteryEntity.getConditionStatus()
                    ));
        }

        // Set location
        LocationEntity location = locationRepository.findByListingId(listingId);
        if (location != null) {
            detailDTO.setLocation(new Location(
                    location.getProvince(),
                    location.getDistrict(),
                    location.getDetails()
            ));
        } else {
            detailDTO.setLocation(null);
        }

        // Set seller info
        UserEntity seller = listing.getSeller();
        if (seller != null) {
            detailDTO.setSeller(new Seller(
                    seller.getId(),
                    seller.getUsername(),
                    seller.getEmail(),
                    seller.getPhoneNumber(),
                    seller.getAvatar()
            ));
        } else {
            detailDTO.setSeller(null);

        }

        detailDTO.setProduct(productResp);

        return detailDTO;
    }

    @Override
    public ListingDetailResponseDTO getListingDetailByIdForAdmin(Long listingId) {
        ListingEntity listing = listingRepository.findById(listingId).orElse(null);
        List<ListingImageEntity> listingImageEntities = listingImageRepository.findByListingId(listingId);

        if (listing == null) {
            logger.warn("Listing not found with ID: " + listingId);
            return null;
        }

        // DO NOT check status - admin can see all listings
        // DO NOT increment views count - admin viewing

        // Convert to DTO
        ListingDetailResponseDTO detailDTO = new ListingDetailResponseDTO();

        detailDTO.setTitle(listing.getTitle());
        detailDTO.setDescription(listing.getDescription());
        detailDTO.setListingType(listing.getListingType());
        detailDTO.setId(listing.getId());
        detailDTO.setCreatedAt(listing.getCreatedAt().toString());
        detailDTO.setUpdatedAt(listing.getUpdatedAt().toString());
        detailDTO.setStatus(listing.getStatus().toString());
        detailDTO.setListingType(listing.getListingType());
        detailDTO.setPrice(listing.getPrice().doubleValue());
        detailDTO.setThumbnail(listing.getThumbnailImage());

        List<String> images = new ArrayList<>();
        for (ListingImageEntity listingImageEntity : listingImageEntities) {
            images.add(listingImageEntity.getImageUrl());
        }

        detailDTO.setImages(images);

        ProductEntity product = listing.getProduct();
        EvVehicleEntity evVehicleEntity = listing.getProduct().getEvVehicle();
        BatteryEntity batteryEntity = listing.getProduct().getBattery();

        Product productResp;
        if (product.getEvVehicle() != null) {
            productResp = new Product(new Ev(evVehicleEntity.getType(),
                    evVehicleEntity.getName(),
                    evVehicleEntity.getBrand(),
                    evVehicleEntity.getYear(),
                    evVehicleEntity.getMileage(),
                    evVehicleEntity.getBatteryCapacity().doubleValue(),
                    evVehicleEntity.getConditionStatus()), null);
        } else {
            productResp = new Product(null,
                    new Battery(batteryEntity.getBrand(),
                            batteryEntity.getCapacity().doubleValue(),
                            batteryEntity.getHealthPercentage(),
                            batteryEntity.getCompatibleVehicles(),
                            batteryEntity.getConditionStatus()
                    ));
        }

        // Set location
        LocationEntity location = locationRepository.findByListingId(listingId);
        if (location != null) {
            detailDTO.setLocation(new Location(
                    location.getProvince(),
                    location.getDistrict(),
                    location.getDetails()
            ));
        } else {
            detailDTO.setLocation(null);
        }

        // Set seller info
        UserEntity seller = listing.getSeller();
        if (seller != null) {
            detailDTO.setSeller(new Seller(
                    seller.getId(),
                    seller.getUsername(),
                    seller.getEmail(),
                    seller.getPhoneNumber(),
                    seller.getAvatar()
            ));
        } else {
            detailDTO.setSeller(null);

        }

        detailDTO.setProduct(productResp);

        return detailDTO;
    }

    @Override
    public Page<ListingCartResponseDTO> getEvListingCarts(Long userId, Pageable pageable) {
        try {
            // Sử dụng repository method trả về Page
            Page<ListingEntity> listingsPage = listingRepository.findEvListingsByStatus(ListingStatus.ACTIVE, pageable);

            // Convert Page<ListingEntity> thành Page<ListingCartResponseDTO>
            return listingsPage.map(listing -> {
                boolean isFavorited = userId != null &&
                        favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;

                return new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        isFavorited
                );
            });
        } catch (Exception e) {
            logger.error("Error getting EV listing carts", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ListingCartResponseDTO> getBatteryListingCarts(Long userId, Pageable pageable) {
        try {
            // Sử dụng repository method trả về Page
            Page<ListingEntity> listingsPage = listingRepository.findBatteryListingsByStatus(ListingStatus.ACTIVE, pageable);

            // Convert Page<ListingEntity> thành Page<ListingCartResponseDTO>
            return listingsPage.map(listing -> {
                boolean isFavorited = userId != null &&
                        favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;

                return new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        isFavorited
                );
            });
        } catch (Exception e) {
            logger.error("Error getting battery listing carts", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId, Pageable pageable) {
        try {
            // Sử dụng repository method trả về Page
            Page<ListingEntity> listingsPage = listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);

            // Convert Page<ListingEntity> thành Page<ListingCartResponseDTO>
            return listingsPage.map(listing -> {
                // For seller's own listings, they don't need favorite status (always false)
                return new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        false // Seller doesn't favorite their own listings
                );
            });
        } catch (Exception e) {
            logger.error("Error getting listings by seller ID: " + sellerId, e);
        }
        return Page.empty(pageable);
    }

    @Override
    public int deleteListingImages(List<String> publicIds) {
        return 0;
    }

    @Override
    public Page<ListingCartResponseDTO> filterEvListings(
            EvFilterFormDTO evFilterFormDTO,
            Long userId,
            Pageable pageable) {
        try {
            logger.info("[FILTER_EV_SERVICE] Starting EV filter with criteria: {}", evFilterFormDTO);
            
            return listingRepository.filterEvListings(
                evFilterFormDTO.year(),
                evFilterFormDTO.minYear(),
                evFilterFormDTO.maxYear(),
                evFilterFormDTO.vehicleType(),
                evFilterFormDTO.brand(),
                evFilterFormDTO.province(),
                evFilterFormDTO.district(),
                evFilterFormDTO.conditionStatus(),
                evFilterFormDTO.minMileage(),
                evFilterFormDTO.maxMileage(),
                evFilterFormDTO.minBatteryCapacity(),
                evFilterFormDTO.maxBatteryCapacity(),
                evFilterFormDTO.minPrice() != null ? BigDecimal.valueOf(evFilterFormDTO.minPrice()) : null,
                evFilterFormDTO.maxPrice() != null ? BigDecimal.valueOf(evFilterFormDTO.maxPrice()) : null,
                pageable
            ).map(listing -> {
                        // Kiểm tra favorite status cho user hiện tại
                        boolean isFavorited = userId != null &&
                                favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;
                        
                        // Tạo response DTO với thông tin đầy đủ
                        return new ListingCartResponseDTO(
                                listing.getId(),
                                listing.getTitle(),
                                listing.getThumbnailImage(),
                                listing.getPrice().doubleValue(),
                                listing.getViewsCount(),
                                listing.getSeller().getPhoneNumber(),
                                isFavorited
                        );
                    }
            );
        } catch (Exception e) {
            logger.error("Error in filterEvListings: ", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public Page<ListingCartResponseDTO> filterBatteryListings(BatteryFilterFormDTO batteryFilterFormDTO,
                                                              Long userId,
                                                              Pageable pageable) {
        try {
            logger.info("[FILTER_BATTERY_SERVICE] Starting battery filter with criteria: {}", batteryFilterFormDTO);
            
            return listingRepository.filterBatteryListings(
                    batteryFilterFormDTO.brand(),
                    batteryFilterFormDTO.name(),
                    batteryFilterFormDTO.year(),
                    batteryFilterFormDTO.minYear(),
                    batteryFilterFormDTO.maxYear(),
                    batteryFilterFormDTO.province(),
                    batteryFilterFormDTO.district(),
                    batteryFilterFormDTO.conditionStatus(),
                    batteryFilterFormDTO.compatibility(),
                    batteryFilterFormDTO.minBatteryCapacity(),
                    batteryFilterFormDTO.maxBatteryCapacity(),
                    batteryFilterFormDTO.minHealthPercentage(),
                    batteryFilterFormDTO.maxHealthPercentage(),
                    batteryFilterFormDTO.minPrice() != null ? BigDecimal.valueOf(batteryFilterFormDTO.minPrice()) : null,
                    batteryFilterFormDTO.maxPrice() != null ? BigDecimal.valueOf(batteryFilterFormDTO.maxPrice()) : null,
                    pageable
            ).map(listing -> {
                        // Kiểm tra favorite status cho user hiện tại
                        boolean isFavorited = userId != null &&
                                favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;

                        // Tạo response DTO với thông tin đầy đủ
                        return new ListingCartResponseDTO(
                                listing.getId(),
                                listing.getTitle(),
                                listing.getThumbnailImage(),
                                listing.getPrice().doubleValue(),
                                listing.getViewsCount(),
                                listing.getSeller().getPhoneNumber(),
                                isFavorited
                        );
                    }
            );
        } catch (Exception e) {
            logger.error("Error in filterBatteryListings: ", e);
            return Page.empty(pageable);
        }
    }

    @Override
    public List<String> getAllProvinces() {
        try {
            return locationRepository.findDistinctProvinces();
        } catch (Exception e) {
            logger.error("Error getting all provinces: ", e);
            return List.of();
        }
    }

    @Override 
    public List<String> getAllDistricts(String province) {
        try {
            if (province != null && !province.trim().isEmpty()) {
                return locationRepository.findDistinctDistrictsByProvince(province.trim());
            } else {
                return locationRepository.findDistinctDistricts();
            }
        } catch (Exception e) {
            logger.error("Error getting districts for province {}: ", province, e);
            return List.of();
        }
    }

    @Override
    public List<String> getAllEvBrands() {
        try {
            return evVehicleRepository.findDistinctBrands();
        } catch (Exception e) {
            logger.error("Error getting all EV brands: ", e);
            return List.of();
        }
    }

    @Override
    public List<String> getAllBatteryBrands() {
        try {
            return batteryRepository.findDistinctBrands();
        } catch (Exception e) {
            logger.error("Error getting all battery brands: ", e);
            return List.of();
        }
    }

    @Override
    public List<String> getAllBatteryNames() {
        try {
            return batteryRepository.findDistinctBatteryNames();
        } catch (Exception e) {
            logger.error("Error getting all battery names: ", e);
            return List.of();
        }
    }

    @Override
    public List<String> getAllCompatibleVehicles() {
        try {
            List<String> allCompatibleVehicles = batteryRepository.findAllCompatibleVehicles();
            Set<String> uniqueVehicles = new HashSet<>();
            
            // Parse comma-separated compatible vehicles and extract unique values
            for (String compatibleVehiclesStr : allCompatibleVehicles) {
                if (compatibleVehiclesStr != null && !compatibleVehiclesStr.trim().isEmpty()) {
                    String[] vehicles = compatibleVehiclesStr.split(",");
                    for (String vehicle : vehicles) {
                        String trimmedVehicle = vehicle.trim();
                        if (!trimmedVehicle.isEmpty()) {
                            uniqueVehicles.add(trimmedVehicle);
                        }
                    }
                }
            }
            
            return new ArrayList<>(uniqueVehicles);
        } catch (Exception e) {
            logger.error("Error getting all compatible vehicles: ", e);
            return List.of();
        }
    }

    @Override
    public List<Integer> getAllEvYears() {
        try {
            return evVehicleRepository.findDistinctYears();
        } catch (Exception e) {
            logger.error("Error getting all EV years: ", e);
            return List.of();
        }
    }

    @Override
    public List<Integer> getAllBatteryYears() {
        try {
            return batteryRepository.findDistinctBatteryYears()
                    .stream()
                    .sorted(Collections.reverseOrder()) // Latest years first
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting all battery years: ", e);
            return List.of();
        }
    }
}

