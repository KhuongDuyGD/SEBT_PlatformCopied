package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.LinkedHashSet;
import org.springframework.data.domain.PageImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import project.swp.spring.sebt_platform.dto.object.*;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.dto.response.ListingDetailResponseDTO;
import project.swp.spring.sebt_platform.dto.response.PostAnoucementResponseDTO;
import project.swp.spring.sebt_platform.model.*;
import project.swp.spring.sebt_platform.model.enums.*;
import project.swp.spring.sebt_platform.repository.*;
import project.swp.spring.sebt_platform.service.ListingService;

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

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository,
                              UserRepository userRepository,
                              EvVehicleRepository evVehicleRepository,
                              BatteryRepository batteryRepository,
                              ProductRepository productRepository,
                              ListingRepository listingRepository,
                              ListingImageRepository listingImageRepository,
                              LocationRepository locationRepository,
                              FavoriteRepository favoriteRepository) {
        this.postRequestRepository = postRequestRepository;
        this.userRepository = userRepository;
        this.listingRepository = listingRepository;
        this.listingImageRepository = listingImageRepository;
        this.locationRepository = locationRepository;
        this.evVehicleRepository = evVehicleRepository;
        this.batteryRepository = batteryRepository;
        this.productRepository = productRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @Override
    public Page<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable) {
        try {
            if (keyWord == null || keyWord.trim().isEmpty()) {
                logger.warn("Search keyword is null or empty");
                return Page.empty(pageable);
            }

            // Sử dụng repository method trả về Page
            Page<ListingEntity> listingsPage = listingRepository.findByTitleContainingIgnoreCaseAndStatus(
                    keyWord.trim(), ListingStatus.ACTIVE, pageable);

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
            logger.error("Error searching listings by keyword: " + keyWord, e);
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional
    public boolean createListing(CreateListingFormDTO createListingForm,
                                 Long sellerId) {
        logger.info("[CREATE_LISTING] Start - sellerId={} title='{}' price={} mainImage={} imagesCount={} hasEV={} hasBattery={}",
                sellerId,
                createListingForm != null ? createListingForm.title() : null,
                createListingForm != null ? createListingForm.price() : null,
                createListingForm != null ? createListingForm.mainImageUrl() : null,
                (createListingForm != null && createListingForm.imageUrls() != null) ? createListingForm.imageUrls().size() : 0,
                (createListingForm != null && createListingForm.product() != null && createListingForm.product().ev() != null),
                (createListingForm != null && createListingForm.product() != null && createListingForm.product().battery() != null)
        );

        if (createListingForm == null) {
            logger.error("Create listing form is null");
            return false;
        }

        if (createListingForm.location() == null) {
            logger.error("Location is null");
            return false;
        }

        if (createListingForm.product() == null) {
            logger.error("Product is null");
            return false;
        }

        if (createListingForm.product().ev() == null && createListingForm.product().battery() == null) {
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

        if (createListingForm.product().ev() != null) {
            Ev evDto = createListingForm.product().ev();
            logger.debug("[CREATE_LISTING] EV DTO -> type={} name='{}' brand='{}' model='{}' year={} mileage={} batteryCapacity={} condition={}",
                    evDto.type(), evDto.name(), evDto.brand(), evDto.model(), evDto.year(), evDto.mileage(), evDto.batteryCapacity(), evDto.conditionStatus());
            // Defensive validation for required EV fields
            if (evDto.name() == null || evDto.name().isBlank()) {
                logger.error("EV name is null/blank");
                return false;
            }
            if (evDto.brand() == null || evDto.brand().isBlank()) {
                logger.error("EV brand is null/blank");
                return false;
            }
            if (evDto.type() == null) {
                logger.error("EV type is null");
                return false;
            }
            if (evDto.year() == null) {
                logger.error("EV year is null");
                return false;
            }
            EvVehicleEntity evVehicleEntity = new EvVehicleEntity();
            evVehicleEntity.setName(evDto.name());
            evVehicleEntity.setBrand(evDto.brand());
            evVehicleEntity.setModel(evDto.model());
            evVehicleEntity.setYear(evDto.year());
            if (evDto.batteryCapacity() > 0) {
                evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(evDto.batteryCapacity()));
            }
            evVehicleEntity.setConditionStatus(evDto.conditionStatus() != null ? evDto.conditionStatus() : VehicleCondition.GOOD);
            evVehicleEntity.setMileage(evDto.mileage() != null ? evDto.mileage() : 0);
            evVehicleEntity.setType(evDto.type());
            evVehicleEntity = evVehicleRepository.save(evVehicleEntity);
            productEntity.setEvVehicle(evVehicleEntity);
        }

        if (createListingForm.product().battery() != null) {
            Battery b = createListingForm.product().battery();
            logger.debug("[CREATE_LISTING] Battery DTO -> brand={} model={} capacity={} health%={} condition={}",
                    b.brand(), b.model(), b.capacity(), b.healthPercentage(), b.conditionStatus());
            BatteryEntity batteryEntity = new BatteryEntity();
            batteryEntity.setBrand(b.brand());
            batteryEntity.setModel(b.model());
            batteryEntity.setHealthPercentage(b.healthPercentage());
            batteryEntity.setCapacity(BigDecimal.valueOf(b.capacity()));
            batteryEntity.setCompatibleVehicles(b.compatibleVehicles());
            batteryEntity.setConditionStatus(b.conditionStatus());
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
        listingEntity.setTitle(createListingForm.title());
        listingEntity.setDescription(createListingForm.description());
        if (createListingForm.listingType() != null) {
            listingEntity.setListingType(createListingForm.listingType());
        }
        listingEntity.setPrice(BigDecimal.valueOf(createListingForm.price()));
        listingEntity.setSeller(user);
        listingEntity.setProduct(productEntity);
    // DEV CHANGE (Patch A): set ACTIVE immediately so it appears in listing endpoints without admin approval.
    // TODO: Revert to SUSPENDED (or PENDING flow) when implementing approval workflow.
    listingEntity.setStatus(ListingStatus.ACTIVE);

            // Set main image (thumbnail)
        if (createListingForm.mainImageUrl() != null) {
            listingEntity.setThumbnailImage(createListingForm.mainImageUrl());
        } else {
            logger.error("Thumbnail URL is null");
            return false;
        }

        // Save listing first to get ID
        listingEntity = listingRepository.save(listingEntity);
        logger.debug("[CREATE_LISTING] Saved listing id={}", listingEntity.getId());

            // Save listing images
        if (createListingForm.imageUrls() != null && !createListingForm.imageUrls().isEmpty()) {
            List<ListingImageEntity> listingImageEntities = new ArrayList<>();
            for (String imageUrl : createListingForm.imageUrls()) {
                ListingImageEntity listingImageEntity = new ListingImageEntity();
                listingImageEntity.setImageUrl(imageUrl);
                listingImageEntity.setListing(listingEntity);
                listingImageEntities.add(listingImageEntity);
            }
            listingImageRepository.saveAll(listingImageEntities);
            logger.debug("[CREATE_LISTING] Saved {} listing images", listingImageEntities.size());
        } else {
            logger.error("Image URLs list is null or empty");
            return false;
        }

            // Create and save location
        LocationEntity locationEntity = new LocationEntity();
        locationEntity.setProvince(createListingForm.location().province());
        locationEntity.setDistrict(createListingForm.location().district());
        locationEntity.setDetails(createListingForm.location().details());
        locationEntity.setListing(listingEntity);
        locationRepository.save(locationEntity);
        logger.debug("[CREATE_LISTING] Saved location id={}", locationEntity.getId());

            // Create and save post request
        PostRequestEntity postRequestEntity = new PostRequestEntity();
        postRequestEntity.setStatus(ApprovalStatus.PENDING);
        postRequestEntity.setListing(listingEntity);
        postRequestRepository.save(postRequestEntity);
        logger.debug("[CREATE_LISTING] Saved post request id={}", postRequestEntity.getId());

        logger.info("[CREATE_LISTING] SUCCESS listingId={}", listingEntity.getId());
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
        if(user != null && !Objects.equals(listing.getSeller().getId(), userId) && user.getRole() == UserRole.MEMBER) {
            listing.setViewsCount(listing.getViewsCount() + 1);
            listingRepository.save(listing);
        }

        // Convert to DTO
        ListingDetailResponseDTO detailDTO = new ListingDetailResponseDTO();

        if (listing == null) return null;

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
            productResp = new Product(new Ev( evVehicleEntity.getType(),
                    evVehicleEntity.getName(),
                    evVehicleEntity.getModel(),
                    evVehicleEntity.getBrand(),
                    evVehicleEntity.getYear(),
                    evVehicleEntity.getMileage(),
                    evVehicleEntity.getBatteryCapacity().doubleValue(),
                    evVehicleEntity.getConditionStatus()),null);
        }  else {
            productResp = new Product(null,
                    new Battery( batteryEntity.getBrand(),
                            batteryEntity.getModel(),
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
            Page<ListingEntity> listingsPage = listingRepository.findCarListingsByStatus(ListingStatus.ACTIVE, pageable);

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

    private String extractPublicIdFromCloudinaryUrl(String cloudinaryUrl) {
        try {
            if (cloudinaryUrl == null || cloudinaryUrl.isEmpty()) {
                return null;
            }

            String[] parts = cloudinaryUrl.split("/upload/");
            if (parts.length < 2) {
                return null;
            }

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

            return cloudinaryUrl;
        }
    }

    @Override
    public Page<ListingCartResponseDTO> searchListingsAdvanced(
            String title,
            String brand,
            Integer year,
            VehicleType vehicleType,
            Double minPrice,
            Double maxPrice,
            Long userId,
            Pageable pageable) {

        try {
            logger.info("Advanced search - title: {}, brand: {}, year: {}", title, brand, year);

            Set<ListingEntity> mergedResults = new LinkedHashSet<>();
            boolean isFirstFilter = true;

            // Query theo từng trường và merge bằng Set

            // Filter theo title
            if (title != null && !title.trim().isEmpty()) {
                List<ListingEntity> titleResults = listingRepository.findByTitleContaining(title.trim());
                logger.debug("Title filter found {} results", titleResults.size());

                if (isFirstFilter) {
                    mergedResults.addAll(titleResults);
                    isFirstFilter = false;
                } else {
                    // Intersection - chỉ giữ những item có trong cả 2 set
                    mergedResults.retainAll(new HashSet<>(titleResults));
                }
            }

            // Filter theo vehicle type
            if (vehicleType != null) {
                List<ListingEntity> typeResults = listingRepository.findByVehicleType(vehicleType);
                logger.debug("Vehicle type filter found {} results", typeResults.size());

                if (isFirstFilter) {
                    mergedResults.addAll(typeResults);
                    isFirstFilter = false;
                } else {
                    mergedResults.retainAll(new HashSet<>(typeResults));
                }
            }

            // Filter theo price range - Sửa: chuyển đổi từ Double sang BigDecimal
            if (minPrice != null && maxPrice != null) {
                List<ListingEntity> priceResults = listingRepository.findByPriceBetween(
                        BigDecimal.valueOf(minPrice),
                        BigDecimal.valueOf(maxPrice)
                );
                logger.debug("Price range filter found {} results", priceResults.size());

                if (isFirstFilter) {
                    mergedResults.addAll(priceResults);
                    isFirstFilter = false;
                } else {
                    mergedResults.retainAll(new HashSet<>(priceResults));
                }
            }

            // Filter theo brand
            if (brand != null && !brand.trim().isEmpty()) {
                List<ListingEntity> brandResults = listingRepository.findByBrand(brand.trim());
                logger.debug("Brand filter found {} results", brandResults.size());

                if (isFirstFilter) {
                    mergedResults.addAll(brandResults);
                    isFirstFilter = false;
                } else {
                    mergedResults.retainAll(new HashSet<>(brandResults));
                }
            }

            // Filter theo year
            if (year != null) {
                List<ListingEntity> yearResults = listingRepository.findByYear(year);
                logger.debug("Year filter found {} results", yearResults.size());

                if (isFirstFilter) {
                    mergedResults.addAll(yearResults);
                    isFirstFilter = false;
                } else {
                    mergedResults.retainAll(new HashSet<>(yearResults));
                }
            }

            // Nếu không có filter nào, lấy tất cả active listings
            if (isFirstFilter) {
                mergedResults.addAll(listingRepository.findAllActiveListings());
                logger.debug("No filters applied, getting all active listings: {}", mergedResults.size());
            }

            logger.info("Merged results total: {}", mergedResults.size());

            // Convert Set thành List để sort
            List<ListingEntity> sortedList = new ArrayList<>(mergedResults);

            // Sort by createdAt DESC (mới nhất trước)
            sortedList.sort(Comparator.comparing(ListingEntity::getCreatedAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            // Convert entities thành DTOs
            List<ListingCartResponseDTO> dtoList = sortedList.stream()
                    .map(listing -> convertToListingCartDTO(listing, userId))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Manual pagination từ List đã sort
            Page<ListingCartResponseDTO> pagedResult = createPageFromList(dtoList, pageable);

            logger.info("Advanced search completed. Total results: {}, Page: {}/{}",
                    pagedResult.getTotalElements(), pageable.getPageNumber() + 1, pagedResult.getTotalPages());

            return pagedResult;

        } catch (Exception e) {
            logger.error("Error in searchListingsAdvanced: ", e);
            return Page.empty(pageable);
        }
    }

    private ListingCartResponseDTO convertToListingCartDTO(ListingEntity listing, Long userId) {
        try {
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
        } catch (Exception e) {
            logger.warn("Error converting listing {} to DTO: {}", listing.getId(), e.getMessage());
            return null;
        }
    }

    private Page<ListingCartResponseDTO> createPageFromList(List<ListingCartResponseDTO> list, Pageable pageable) {
        if (list == null) list = new ArrayList<>();

        int total = list.size();
        int start = Math.min((int) pageable.getOffset(), total);
        int end = Math.min(start + pageable.getPageSize(), total);

        List<ListingCartResponseDTO> pageContent = list.subList(start, end);

        return new PageImpl<>(pageContent, pageable, total);
    }
}

