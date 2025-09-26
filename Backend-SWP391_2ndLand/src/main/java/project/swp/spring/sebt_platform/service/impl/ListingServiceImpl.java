package project.swp.spring.sebt_platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.dto.request.CreateListingFormDTO;
import project.swp.spring.sebt_platform.model.dto.response.ListingCartResponseDTO;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.service.ListingService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ListingServiceImpl implements ListingService {


    private final PostRequestRepository postRequestRepository;

    @Autowired
    public ListingServiceImpl(PostRequestRepository postRequestRepository) {
        this.postRequestRepository = postRequestRepository;
    }

    @Override
    public List<ListingCartResponseDTO> getListingsByKeyWord(String keyWord) {
        return List.of();
    }

    @Override
    public boolean createListing(CreateListingFormDTO createListingForm) {

        ListingEntity listingEntity = new ListingEntity();
        listingEntity.setTitle(createListingForm.title());
        listingEntity.setDescription(createListingForm.description());
        listingEntity.setListingType(createListingForm.listingType());
        listingEntity.setMainImage(createListingForm.mainImage());
        listingEntity.setLocation(createListingForm.location().toString());
        listingEntity.setProduct(createListingForm.product());
        listingEntity.setListingImages(createListingForm.listingImages());
        listingEntity.setPrice(BigDecimal.valueOf(createListingForm.price()));

        PostRequestEntity postRequestEntity = new PostRequestEntity();
        postRequestEntity.setStatus(ApprovalStatus.PENDING);
        postRequestEntity.setListing(listingEntity);

        boolean success = postRequestRepository.save(postRequestEntity) != null;
        return success;
    }
}
