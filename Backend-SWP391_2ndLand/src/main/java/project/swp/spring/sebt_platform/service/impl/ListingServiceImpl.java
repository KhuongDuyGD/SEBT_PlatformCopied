package project.swp.spring.sebt_platform.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord, Long userId, Pageable pageable) {
       return null;
    }

    @Override
    @Transactional
    public boolean createListing(CreateListingFormDTO createListingForm,
                                 Long sellerId) {
        try {
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

            // Set main image (thumbnail)
            if (createListingForm.mainImageUrl() != null) {
                listingEntity.setThumbnailImage(createListingForm.mainImageUrl());
            } else {
                logger.error("Thumbnail URL is null");
                return false;
            }

            // Save listing first to get ID
            listingEntity = listingRepository.save(listingEntity);

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

            // Create and save post request
            PostRequestEntity postRequestEntity = new PostRequestEntity();
            postRequestEntity.setStatus(ApprovalStatus.PENDING);
            postRequestEntity.setListing(listingEntity);
            postRequestRepository.save(postRequestEntity);

            return true;

        } catch (Exception e) {
            logger.error("Error in create listing: " + e.getMessage(), e);
            // Use proper logging instead of printStackTrace
            return false;
        }
    }

    @Override
    public ListingDetailResponseDTO getListingDetailById(Long listingId) {
        ListingEntity listing = listingRepository.findById(listingId).orElse(null);
        List<ListingImageEntity> listingImageEntities = listingImageRepository.findByListingId(listingId);
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

        detailDTO.setProduct(productResp);

        return detailDTO;
    }

    @Override
    public List<ListingCartResponseDTO> getEvListingCarts(Long userId, Pageable pageable) {
        try {
            List<ListingEntity> listings = listingRepository.findCarListingsByStatus(ListingStatus.ACTIVE, pageable);
            List<ListingCartResponseDTO> listingCarts = new ArrayList<>();
            for (ListingEntity listing : listings) {
                boolean isFavorited = userId != null &&
                    favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;
                ListingCartResponseDTO cartDTO = new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        isFavorited
                );
                listingCarts.add(cartDTO);
            }
            return listingCarts;
        } catch (Exception e) {
            logger.error("Error getting EV listing carts", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<ListingCartResponseDTO> getBatteryListingCarts(Long userId, Pageable pageable) {
        try {
            List<ListingEntity> listings = listingRepository.findBatteryListingsByStatus(ListingStatus.ACTIVE, pageable);
            List<ListingCartResponseDTO> listingCarts = new ArrayList<>();
            for (ListingEntity listing : listings) {
                boolean isFavorited = userId != null &&
                    favoriteRepository.findByUserIdAndListingId(userId, listing.getId()) != null;
                ListingCartResponseDTO cartDTO = new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        isFavorited
                );
                listingCarts.add(cartDTO);
            }
            return listingCarts;
        } catch (Exception e) {
            logger.error("Error getting battery listing carts", e);
            return new ArrayList<>();
        }
    }


    @Override
    public List<ListingCartResponseDTO> getListingCartsBySeller(Long sellerId, Pageable pageable) {
        try {
            List<ListingEntity> listings = listingRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
            List<ListingCartResponseDTO> listingCarts = new ArrayList<>();
            for (ListingEntity listing : listings) {
                // For seller's own listings, they don't need favorite status (always false)
                ListingCartResponseDTO cartDTO = new ListingCartResponseDTO(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getThumbnailImage(),
                        listing.getPrice().doubleValue(),
                        listing.getViewsCount(),
                        listing.getSeller().getPhoneNumber(),
                        false // Seller doesn't favorite their own listings
                );
                listingCarts.add(cartDTO);
            }
            return listingCarts;
        } catch (Exception e) {
            logger.error("Error getting listings by seller ID: " + sellerId, e);
            return new ArrayList<>();
        }
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

}
