package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import project.swp.spring.sebt_platform.model.*;
import project.swp.spring.sebt_platform.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.ListingService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {

    private final PostRequestRepository postRequestRepository;
    private final UserRepository userRepository;

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository, UserRepository userRepository) {
        this.postRequestRepository = postRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord) {
        return List.of();
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

}
