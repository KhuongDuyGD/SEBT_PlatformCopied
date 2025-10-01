# Tài liệu chỉnh sửa Entities để tránh Deep Nesting

## Vấn đề ban đầu
Các entities trong project có vấn đề deep nesting nghiêm trọng do:

1. **Bidirectional relationships**: Quá nhiều mối quan hệ hai chiều giữa các entities
2. **Cascade operations không hợp lý**: Sử dụng `CascadeType.ALL` quá nhiều
3. **Circular references**: Vòng lặp tham chiếu giữa Product -> Listing -> Product
4. **OneToMany relationships**: Gây ra N+1 problem và deep nesting khi serialize JSON

## Các thay đổi đã thực hiện

### 1. UserEntity
**Thay đổi chính:**
- ✅ Loại bỏ tất cả `@OneToMany` relationships
- ✅ Thêm `@JsonIgnore` cho password và salt
- ✅ Thêm equals() và hashCode() methods
- ✅ Relationships sẽ được fetch thông qua repository queries khi cần

**Trước:**
```java
@OneToMany(mappedBy = "seller", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<ListingEntity> listings;

@OneToMany(mappedBy = "buyer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<ContractEntity> buyerContracts;
// ... và nhiều OneToMany khác
```

**Sau:**
```java
// Loại bỏ tất cả OneToMany relationships
// Sử dụng repository queries để fetch data khi cần
```

### 2. ListingEntity
**Thay đổi chính:**
- ✅ Loại bỏ các `@OneToMany` relationships (images, contracts, favorites)
- ✅ Giữ lại `@OneToOne` với Product và `@ManyToOne` với User
- ✅ Thay đổi cascade từ `CascadeType.ALL` thành `{CascadeType.PERSIST, CascadeType.MERGE}`
- ✅ Thêm helper method `incrementViewCount()`

### 3. ProductEntity
**Thay đổi chính:**
- ✅ Loại bỏ bidirectional relationship với ListingEntity
- ✅ Giảm cascade operations
- ✅ ProductEntity không cần biết về ListingEntity để tránh circular reference

**Trước:**
```java
@OneToOne(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private ListingEntity listing;
```

**Sau:**
```java
// Loại bỏ hoàn toàn để tránh circular reference
```

### 4. ContractEntity
**Thay đổi chính:**
- ✅ Loại bỏ `@OneToMany` với ContractSignatureEntity
- ✅ Giữ lại các `@ManyToOne` relationships cần thiết
- ✅ Thêm helper methods `isFullySigned()` và `canBeExecuted()`

### 5. FavoriteEntity
**Thay đổi chính:**
- ✅ Cấu trúc đơn giản chỉ với 2 ManyToOne relationships
- ✅ Unique constraint để tránh duplicate favorites
- ✅ Không có OneToMany relationships

## Lợi ích của việc chỉnh sửa

### 1. Performance
- ✅ **Giảm N+1 queries**: Loại bỏ các OneToMany tự động load
- ✅ **Lazy loading hiệu quả**: Chỉ load data khi thực sự cần
- ✅ **Giảm memory usage**: Không load unnecessary relationships

### 2. JSON Serialization
- ✅ **Tránh infinite loops**: Loại bỏ circular references
- ✅ **Response size nhỏ hơn**: Không serialize unnecessary data
- ✅ **Control được output**: Fetch chính xác data cần thiết

### 3. Maintainability
- ✅ **Đơn giản hóa entities**: Mỗi entity tập trung vào responsibility chính
- ✅ **Explicit data fetching**: Rõ ràng khi nào fetch related data
- ✅ **Easier testing**: Mock và test entities đơn giản hơn

## Cách sử dụng sau khi chỉnh sửa

### Fetch related data thông qua Repository
```java
// Thay vì user.getListings()
List<ListingEntity> userListings = listingRepository.findBySellerIdAndStatus(userId, ListingStatus.ACTIVE);

// Thay vì listing.getImages()
List<ListingImageEntity> images = listingImageRepository.findByListingId(listingId);

// Thay vì contract.getSignatures()
List<ContractSignatureEntity> signatures = contractSignatureRepository.findByContractId(contractId);
```

### Service Layer pattern
```java
@Service
public class UserService {
    
    public UserWithListingsDTO getUserWithListings(Long userId) {
        UserEntity user = userRepository.findById(userId);
        List<ListingEntity> listings = listingRepository.findBySellerIdAndStatus(userId, ListingStatus.ACTIVE);
        
        return new UserWithListingsDTO(user, listings);
    }
}
```

## Migration Guide

### Cho Developers:
1. **Update Service layer**: Thay thế direct entity relationships bằng repository queries
2. **Update Controllers**: Sử dụng DTOs thay vì return entities trực tiếp
3. **Update Tests**: Mock repository calls thay vì entity relationships

### Cho Database:
- ✅ Không cần thay đổi database schema
- ✅ Chỉ thay đổi cách entities map với database
- ✅ Foreign keys vẫn hoạt động bình thường

## Best Practices đã áp dụng

1. **Single Responsibility**: Mỗi entity chỉ chứa data thuộc về nó
2. **Explicit over Implicit**: Rõ ràng khi fetch related data
3. **Performance First**: Tối ưu cho performance thay vì convenience
4. **DTO Pattern**: Sử dụng DTOs để control API responses
5. **Repository Pattern**: Centralized data access logic

## Kết luận

Việc chỉnh sửa entities này sẽ:
- ✅ Giải quyết hoàn toàn vấn đề deep nesting
- ✅ Cải thiện performance đáng kể
- ✅ Tránh được circular reference và infinite loops
- ✅ Dễ dàng maintain và extend trong tương lai
- ✅ Follow best practices của JPA/Hibernate

**Lưu ý quan trọng**: Sau khi chỉnh sửa entities, cần update các Service và Controller classes để sử dụng repository queries thay vì direct entity relationships.
