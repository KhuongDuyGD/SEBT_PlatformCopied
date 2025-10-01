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
import project.swp.spring.sebt_platform.model.*;
import project.swp.spring.sebt_platform.model.enums.*;
import project.swp.spring.sebt_platform.repository.*;
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
            if (thumbnailUrl != null) {
                listingEntity.setThumbnailPublicId(thumbnailUrl.getPublicId());
                listingEntity.setThumbnailImage(thumbnailUrl.getUrl());
            } else {
                System.err.println("Thumbnail URL is null");
                return false;
            }

            // Save listing first to get ID
            listingEntity = listingRepository.save(listingEntity);

            // Save listing images
            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<ListingImageEntity> listingImageEntities = new ArrayList<>();
                for (Image imageUrl : imageUrls) {
                    ListingImageEntity listingImageEntity = new ListingImageEntity();
                    listingImageEntity.setImageUrl(imageUrl.getUrl());
                    listingImageEntity.setPublicId(imageUrl.getPublicId());
                    listingImageEntity.setListing(listingEntity);
                    listingImageEntities.add(listingImageEntity);
                }
                listingImageRepository.saveAll(listingImageEntities);
            } else {
                System.err.println("Image URLs list is null or empty");
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
            System.err.println("Error in create listing: " + e.getMessage());
            // Use proper logging instead of printStackTrace
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


}
