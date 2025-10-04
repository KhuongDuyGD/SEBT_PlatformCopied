package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;

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
        List<Long> ids = listingsPage.getContent().stream().map(ListingEntity::getId).toList();
        List<Long> favoritedIds = (userId != null && !ids.isEmpty()) ?
            favoriteRepository.findFavoritedListingIds(userId, ids) : List.of();
        Set<Long> favSet = new java.util.HashSet<>(favoritedIds);

        return listingsPage.map(listing -> new ListingCartResponseDTO(
            listing.getId(),
            listing.getTitle(),
            listing.getThumbnailImage(),
            listing.getPrice().doubleValue(),
            listing.getViewsCount(),
            listing.getSeller().getPhoneNumber(),
            favSet.contains(listing.getId())
        ));
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
            logger.debug("[CREATE_LISTING] EV DTO -> type={} name='{}' brand='{}' model='{}' year={} mileage={} batteryCapacity={} condition={}",
                    evDto.getType(), evDto.getName(), evDto.getBrand(), evDto.getModel(), evDto.getYear(), evDto.getMileage(), evDto.getBatteryCapacity(), evDto.getConditionStatus());

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
            evVehicleEntity.setModel(evDto.getModel());
            evVehicleEntity.setYear(evDto.getYear());
            // Battery capacity bắt buộc > 0
            if (evDto.getBatteryCapacity() <= 0) {
                logger.error("EV battery capacity <= 0");
                return false;
            }
            evVehicleEntity.setBatteryCapacity(BigDecimal.valueOf(evDto.getBatteryCapacity()));
            evVehicleEntity.setConditionStatus(evDto.getConditionStatus() != null ? evDto.getConditionStatus() : VehicleCondition.GOOD);
            evVehicleEntity.setMileage(evDto.getMileage() != null ? evDto.getMileage() : 0);
            evVehicleEntity.setType(evDto.getType());
            evVehicleEntity = evVehicleRepository.save(evVehicleEntity);
            productEntity.setEvVehicle(evVehicleEntity);
        }

        if (createListingForm.getProduct().getBattery() != null) {
            Battery b = createListingForm.getProduct().getBattery();
            logger.debug("[CREATE_LISTING] Battery DTO -> brand={} model={} capacity={} health%={} condition={}",
                    b.getBrand(), b.getModel(), b.getCapacity(), b.getHealthPercentage(), b.getConditionStatus());
            BatteryEntity batteryEntity = new BatteryEntity();
            batteryEntity.setBrand(b.getBrand());
            batteryEntity.setModel(b.getModel());
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
        listingEntity.setStatus(ListingStatus.ACTIVE);

        // Set thumbnail - lấy ảnh đầu tiên từ listingImages làm thumbnail
        if (listingImages != null && !listingImages.isEmpty()) {
            listingEntity.setThumbnailImage(listingImages.get(0).url());
            logger.debug("[CREATE_LISTING] Set thumbnail from first image: {}", listingImages.get(0).url());
        } else {
            logger.error("Image list is null or empty - cannot create listing without images");
            return false;
        }

        // Save listing first to get ID
        listingEntity = listingRepository.save(listingEntity);
        logger.debug("[CREATE_LISTING] Saved listing id={}", listingEntity.getId());

        // Save listing images với URL và publicId từ Cloudinary
        if (listingImages != null && !listingImages.isEmpty()) {
            List<ListingImageEntity> listingImageEntities = new ArrayList<>();
            for (Image image : listingImages) {
                ListingImageEntity listingImageEntity = new ListingImageEntity();
                listingImageEntity.setImageUrl(image.url());
                listingImageEntity.setPublicId(image.publicId());
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
        if(user != null && !Objects.equals(listing.getSeller().getId(), userId) && user.getRole() == UserRole.MEMBER) {
            listing.setViewsCount(listing.getViewsCount() + 1);
            listingRepository.save(listing);
        }

        // Convert to DTO
        ListingDetailResponseDTO detailDTO = new ListingDetailResponseDTO();

    // listing was already checked above for null & ACTIVE status

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

    // Build product response model with null-safety to avoid 500 errors (e.g. missing batteryCapacity)
    Product productResp;
    if (product != null && product.getEvVehicle() != null) {
        // Safely extract fields
        var cap = (evVehicleEntity.getBatteryCapacity() != null) ? evVehicleEntity.getBatteryCapacity().doubleValue() : 0d;
        var mileage = evVehicleEntity.getMileage() != null ? evVehicleEntity.getMileage() : 0;
        productResp = new Product(
            new Ev(
                evVehicleEntity.getType(),
                evVehicleEntity.getName(),
                evVehicleEntity.getModel(),
                evVehicleEntity.getBrand(),
                evVehicleEntity.getYear(),
                mileage,
                cap,
                evVehicleEntity.getConditionStatus()
            ),
            null
        );
    } else if (product != null && product.getBattery() != null) {
        var capacity = batteryEntity.getCapacity() != null ? batteryEntity.getCapacity().doubleValue() : 0d;
        productResp = new Product(
            null,
            new Battery(
                batteryEntity.getBrand(),
                batteryEntity.getModel(),
                capacity,
                batteryEntity.getHealthPercentage(),
                batteryEntity.getCompatibleVehicles(),
                batteryEntity.getConditionStatus()
            )
        );
    } else {
        productResp = new Product(null, null); // Fallback unexpected case
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
        List<Long> ids = listingsPage.getContent().stream().map(ListingEntity::getId).toList();
        Set<Long> favSet = (userId != null && !ids.isEmpty()) ?
            new java.util.HashSet<>(favoriteRepository.findFavoritedListingIds(userId, ids)) : java.util.Collections.<Long>emptySet();
        return listingsPage.map(listing -> new ListingCartResponseDTO(
            listing.getId(),
            listing.getTitle(),
            listing.getThumbnailImage(),
            listing.getPrice().doubleValue(),
            listing.getViewsCount(),
            listing.getSeller().getPhoneNumber(),
            favSet.contains(listing.getId())
        ));
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
        List<Long> ids = listingsPage.getContent().stream().map(ListingEntity::getId).toList();
        Set<Long> favSet = (userId != null && !ids.isEmpty()) ?
            new java.util.HashSet<>(favoriteRepository.findFavoritedListingIds(userId, ids)) : java.util.Collections.<Long>emptySet();
        return listingsPage.map(listing -> new ListingCartResponseDTO(
            listing.getId(),
            listing.getTitle(),
            listing.getThumbnailImage(),
            listing.getPrice().doubleValue(),
            listing.getViewsCount(),
            listing.getSeller().getPhoneNumber(),
            favSet.contains(listing.getId())
        ));
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

            // Fast path: only vehicleType filter (common case from ListingPage sidebar)
            boolean onlyVehicleType = vehicleType != null &&
                    (title == null || title.isBlank()) &&
                    (brand == null || brand.isBlank()) &&
                    year == null &&
                    minPrice == null && maxPrice == null;

            if (onlyVehicleType) {
                Page<ListingEntity> pageData = listingRepository.findByStatusAndVehicleType(ListingStatus.ACTIVE, vehicleType, pageable);
                logger.info("[ADV_SEARCH_FAST_PATH] type={} activeCount={} pageContent={} page={} size={}",
                        vehicleType,
                        pageData.getTotalElements(),
                        pageData.getContent().size(),
                        pageable.getPageNumber(),
                        pageable.getPageSize());
                if (pageData.getContent().isEmpty()) {
                    logger.warn("[ADV_SEARCH_FAST_PATH] No ACTIVE listings found for type={}. Potential causes: (1) All listings not ACTIVE (2) No data inserted (3) Enum mismatch.", vehicleType);
                    try {
                        List<ListingEntity> allType = listingRepository.findAllByVehicleTypeNoStatus(vehicleType);
                        logger.warn("[ADV_SEARCH_FAST_PATH][DIAG] totalAnyStatus={} first5={} statuses={}", allType.size(),
                                allType.stream().limit(5).map(ListingEntity::getId).collect(Collectors.toList()),
                                allType.stream().limit(5).map(l -> l.getStatus()+":"+l.getId()).collect(Collectors.toList()));
                    } catch (Exception diagEx) {
                        logger.error("[ADV_SEARCH_FAST_PATH][DIAG] error while fetching all by type {}: {}", vehicleType, diagEx.getMessage());
                    }
                }
                // Batch favorites
                List<Long> ids = pageData.getContent().stream().map(ListingEntity::getId).toList();
                Set<Long> favSet = (userId != null && !ids.isEmpty()) ? new java.util.HashSet<>(favoriteRepository.findFavoritedListingIds(userId, ids)) : java.util.Collections.<Long>emptySet();
                return pageData.map(le -> convertToListingCartDTOWithFav(le, favSet.contains(le.getId())));
            }

            // Specification xây dựng động
            Specification<ListingEntity> spec = (root, query, cb) -> cb.equal(root.get("status"), ListingStatus.ACTIVE);

            if (title != null && !title.isBlank()) {
                String t = title.trim().toLowerCase();
                spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("title")), "%" + t + "%"));
            }
            if (vehicleType != null) {
                spec = spec.and((root, q, cb) -> cb.equal(root.join("product").join("evVehicle").get("type"), vehicleType));
            }
            if (brand != null && !brand.isBlank()) {
                String b = brand.trim();
                // brand có thể ở evVehicle hoặc battery
                spec = spec.and((root, q, cb) -> {
                    var evJoin = root.join("product").join("evVehicle", jakarta.persistence.criteria.JoinType.LEFT);
                    var batJoin = root.join("product").join("battery", jakarta.persistence.criteria.JoinType.LEFT);
                    return cb.or(
                            cb.equal(evJoin.get("brand"), b),
                            cb.equal(batJoin.get("brand"), b)
                    );
                });
            }
            if (year != null) {
                spec = spec.and((root, q, cb) -> cb.equal(root.join("product").join("evVehicle").get("year"), year));
            }
            if (minPrice != null && maxPrice != null) {
                spec = spec.and((root, q, cb) -> cb.between(root.get("price"), BigDecimal.valueOf(minPrice), BigDecimal.valueOf(maxPrice)));
            }

            Page<ListingEntity> resultPage = listingRepository.findAll(spec, pageable);
            logger.info("[ADV_SEARCH_SPEC] total={} page={} size={} filtersApplied title?{} type?{} brand?{} year?{} priceRange?{}",
                    resultPage.getTotalElements(), pageable.getPageNumber(), pageable.getPageSize(),
                    title != null && !title.isBlank(), vehicleType != null, brand != null && !brand.isBlank(), year != null,
                    (minPrice != null && maxPrice != null));

            List<Long> ids = resultPage.getContent().stream().map(ListingEntity::getId).toList();
            Set<Long> favSet = (userId != null && !ids.isEmpty()) ? new java.util.HashSet<>(favoriteRepository.findFavoritedListingIds(userId, ids)) : java.util.Collections.<Long>emptySet();
            return resultPage.map(le -> convertToListingCartDTOWithFav(le, favSet.contains(le.getId())));

        } catch (Exception e) {
            logger.error("Error in searchListingsAdvanced: ", e);
            return Page.empty(pageable);
        }
    }

    private ListingCartResponseDTO convertToListingCartDTOWithFav(ListingEntity listing, boolean isFavorited) {
        if (listing == null) {
            return new ListingCartResponseDTO(null, "(null)", null, 0d, 0, null, false);
        }
        double priceVal = 0d;
        if (listing.getPrice() != null) {
            try { priceVal = listing.getPrice().doubleValue(); } catch (Exception ex) { logger.warn("Price convert error listing {}: {}", listing.getId(), ex.getMessage()); }
        }
        String phone = null;
        try { if (listing.getSeller() != null) phone = listing.getSeller().getPhoneNumber(); } catch (Exception ex) { logger.warn("Seller phone fetch failed listing {}: {}", listing.getId(), ex.getMessage()); }
        return new ListingCartResponseDTO(
                listing.getId(),
                listing.getTitle(),
                listing.getThumbnailImage(),
                priceVal,
                listing.getViewsCount(),
                phone,
                isFavorited
        );
    }

}

