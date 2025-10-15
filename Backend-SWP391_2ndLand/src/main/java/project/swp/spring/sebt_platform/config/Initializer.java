package project.swp.spring.sebt_platform.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import project.swp.spring.sebt_platform.model.BatteryEntity;
import project.swp.spring.sebt_platform.model.EvVehicleEntity;
import project.swp.spring.sebt_platform.model.ListingEntity;
import project.swp.spring.sebt_platform.model.LocationEntity;
import project.swp.spring.sebt_platform.model.PostRequestEntity;
import project.swp.spring.sebt_platform.model.ProductEntity;
import project.swp.spring.sebt_platform.model.UserEntity;
import project.swp.spring.sebt_platform.model.enums.ApprovalStatus;
import project.swp.spring.sebt_platform.model.enums.BatteryCondition;
import project.swp.spring.sebt_platform.model.enums.ConfigDataType;
import project.swp.spring.sebt_platform.model.enums.ListingStatus;
import project.swp.spring.sebt_platform.model.enums.ListingType;
import project.swp.spring.sebt_platform.model.enums.UserRole;
import project.swp.spring.sebt_platform.model.enums.VehicleCondition;
import project.swp.spring.sebt_platform.model.enums.VehicleType;
import project.swp.spring.sebt_platform.repository.BatteryRepository;
import project.swp.spring.sebt_platform.repository.EvVehicleRepository;
import project.swp.spring.sebt_platform.repository.ListingRepository;
import project.swp.spring.sebt_platform.repository.LocationRepository;
import project.swp.spring.sebt_platform.repository.PostRequestRepository;
import project.swp.spring.sebt_platform.repository.ProductRepository;
import project.swp.spring.sebt_platform.repository.UserRepository;
import project.swp.spring.sebt_platform.service.AdminService;
import project.swp.spring.sebt_platform.service.AuthService;
//cmd run with profile and args can modify ev-count and battery-count
//if not provided, default to 5 each
//mvn spring-boot:run -Dspring-boot.run.profiles=devautoseed -Dspring-boot.run.arguments="--seeder.listings.ev-count=[amount],--seeder.listings.battery-count=[amount]"
@Configuration
@Profile("devautoseed") // Only active when profile 'devautoseed' is enabled
public class Initializer {

    private static final Logger logger = LoggerFactory.getLogger(Initializer.class);

    @Autowired
    private AuthService authService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ListingRepository listingRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private EvVehicleRepository evVehicleRepository;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PostRequestRepository postRequestRepository;

    @Value("${seeder.listings.ev-count:5}")
    private int evListingCount;

    @Value("${seeder.listings.battery-count:5}")
    private int batteryListingCount;

    // Danh sách email của người dùng (11 thành viên + 2 admin)
    private static final String[] USER_EMAILS = {
        "noreplysebtplatform@gmail.com",      // Admin chính
        "nguyentantai22032005@gmail.com",     // Admin phụ  
        "ducminh852005@gmail.com",            // Member từ cũ
        "donguyenkhuongduy1202@gmail.com",    // 10 Members mới
        "mutoy4351@gmail.com",
        "loriesandy4@gmail.com",
        "jennieandre8@gmail.com",
        "channelkhampha4@gmail.com",
        "kingroyalelol20@gmail.com",
        "ducdaonguyen378@gmail.com",
        "satokazuo155@gmail.com",
        "otakugamer73565@gmail.com",
        "npln.0307@gmail.com"                 // Member mới được thêm
    };

    // Danh sách mật khẩu tương ứng cho từng tài khoản - tất cả đổi thành 123456
    private static final String[] PASSWORDS = {
        "123456",        // Admin chính
        "123456",        // Admin phụ
        "123456",        // Member từ cũ
        "123456",        // 10 members
        "123456",
        "123456",
        "123456",
        "123456",
        "123456",
        "123456",
        "123456",
        "123456",
        "123456"         // Member mới được thêm
    };

    // Danh sách vai trò tương ứng
    private static final UserRole[] USER_ROLES = {
        UserRole.ADMIN,   // Admin chính
        UserRole.ADMIN,   // Admin phụ
        UserRole.MEMBER,  // Member từ cũ
        UserRole.MEMBER,  // 10 members mới
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER,
        UserRole.MEMBER   // Member mới được thêm
    };

    // Danh sách các thương hiệu xe điện
    private static final String[] EV_BRANDS = {
        "VinFast", "Tesla", "BMW", "Audi", "Mercedes-Benz",
        "Nissan", "Hyundai", "Kia", "BYD", "Lucid"
    };

    // Danh sách các loại xe theo enum hiện có
    private static final VehicleType[] VEHICLE_TYPES = {
        VehicleType.CAR, VehicleType.MOTORBIKE, VehicleType.BIKE
    };

    // Danh sách các thương hiệu pin
    private static final String[] BATTERY_BRANDS = {
        "CATL", "LG Chem", "Samsung SDI", "Panasonic", "BYD",
        "SK Innovation", "CALB", "Gotion", "Northvolt", "Contemporary Amperex"
    };

    // Danh sách đầy đủ 63 tỉnh thành Việt Nam
    private static final String[] PROVINCES = {
        // 5 Thành phố trực thuộc Trung ương
        "Hà Nội", "TP. Hồ Chí Minh", "Hải Phòng", "Đà Nẵng", "Cần Thơ",
        
        // Vùng Đồng bằng sông Hồng
        "Vĩnh Phúc", "Bắc Ninh", "Hải Dương", "Hưng Yên", "Hà Nam", 
        "Nam Định", "Thái Bình", "Ninh Bình",
        
        // Vùng Trung du và miền núi phía Bắc
        "Hà Giang", "Cao Bằng", "Lào Cai", "Lai Châu", "Sơn La", "Điện Biên",
        "Yên Bái", "Hòa Bình", "Tuyên Quang", "Phú Thọ", "Bắc Kạn", "Thái Nguyên",
        "Lạng Sơn", "Bắc Giang", "Quảng Ninh",
        
        // Bắc Trung Bộ
        "Thanh Hóa", "Nghệ An", "Hà Tĩnh", "Quảng Bình", "Quảng Trị", "Thừa Thiên Huế",
        
        // Nam Trung Bộ
        "Quảng Nam", "Quảng Ngãi", "Bình Định", "Phú Yên", "Khánh Hòa", "Ninh Thuận", "Bình Thuận",
        
        // Tây Nguyên
        "Kon Tum", "Gia Lai", "Đắk Lắk", "Đắk Nông", "Lâm Đồng",
        
        // Đông Nam Bộ
        "Bình Phước", "Bình Dương", "Đồng Nai", "Tây Ninh", "Bà Rịa - Vũng Tàu",
        
        // Đồng bằng sông Cửu Long
        "Long An", "Tiền Giang", "Bến Tre", "Trà Vinh", "Vĩnh Long",
        "Đồng Tháp", "An Giang", "Kiên Giang", "Hậu Giang", "Sóc Trăng",
        "Bạc Liêu", "Cà Mau"
    };

    // Danh sách quận/huyện mẫu
    private static final String[] DISTRICTS = {
        "Quận 1", "Quận 2", "Quận 3", "Quận 4", "Quận 5",
        "Quận 6", "Quận 7", "Quận 8", "Quận 9", "Quận 10",
        "Quận 11", "Quận 12", "Quận Tân Bình", "Quận Bình Thạnh", 
        "Quận Phú Nhuận", "Quận Gò Vấp", "Quận Thủ Đức",
        "Huyện Củ Chi", "Huyện Hóc Môn", "Huyện Bình Chánh",
        "Thành phố Thủ Dầu Một", "Thị xã Dĩ An", "Thị xã Thuận An",
        "Thành phố Biên Hòa", "Thành phố Long Khánh",
        "Quận Hồng Bàng", "Quận Ngô Quyền", "Quận Lê Chân",
        "Quận Hải Châu", "Quận Thanh Khê", "Quận Sơn Trà",
        "Quận Ba Đình", "Quận Hoàn Kiếm", "Quận Đống Đa",
        "Quận Hai Bà Trưng", "Quận Cầu Giấy", "Quận Tây Hồ",
        "Quận Ninh Kiều", "Quận Cái Răng", "Quận Ô Môn"
    };

    // Danh sách tiêu đề cho xe điện (tiếng Việt)
    private static final String[] EV_TITLES = {
        "Xe điện thông minh - Tiết kiệm năng lượng vượt trội",
        "Ô tô điện cao cấp - Công nghệ hiện đại",
        "Xe điện gia đình - An toàn và tiện nghi",
        "Xe điện thể thao - Hiệu suất mạnh mẽ",
        "Ô tô điện sang trọng - Đẳng cấp doanh nhân",
        "Xe điện compact - Phù hợp đô thị",
        "Xe điện SUV - Phiêu lưu mọi địa hình",
        "Ô tô điện hybrid - Kết hợp hoàn hảo",
        "Xe điện premium - Trải nghiệm đỉnh cao",
        "Ô tô điện eco - Thân thiện môi trường"
    };

    // Danh sách mô tả cho xe điện (tiếng Việt)
    private static final String[] EV_DESCRIPTIONS = {
        "Xe điện chất lượng cao với công nghệ tiên tiến. Thiết kế hiện đại, tiết kiệm năng lượng. Được bảo dưỡng định kỳ, vận hành ổn định. Phù hợp di chuyển hàng ngày trong thành phố. Liên hệ để xem xe và lái thử.",
        "Ô tô điện sang trọng với trang bị đầy đủ tiện nghi. Hệ thống an toàn thông minh, nội thất cao cấp. Động cơ điện mạnh mẽ, vận hành êm ái. Đã qua kiểm định chất lượng. Bảo hành chính hãng.",
        "Xe điện gia đình an toàn và tiện lợi. Không gian rộng rãi, ghế ngồi thoải mái. Pin dung lượng lớn, quãng đường di chuyển xa. Hệ thống sạc nhanh hiện đại. Giá cả hợp lý.",
        "Ô tô điện hiệu suất cao với khả năng tăng tốc ấn tượng. Công nghệ pin tiên tiến, thời gian sạc ngắn. Thiết kế thể thao năng động. Hệ thống điều khiển thông minh. Phù hợp người yêu tốc độ.",
        "Xe điện cao cấp dành cho doanh nhân thành đạt. Nội thất da thật, âm thanh hi-end. Công nghệ tự lái, hệ thống an toàn 5 sao. Bảo dưỡng tại đại lý chính hãng. Còn bảo hành dài hạn.",
        "Ô tô điện compact thích hợp cho thành phố đông đúc. Kích thước nhỏ gọn, dễ đỗ xe. Tiêu thụ năng lượng thấp, chi phí vận hành rẻ. Vận hành êm ái, không tiếng ồn. Lựa chọn lý tưởng cho gia đình trẻ."
    };

    // Danh sách tiêu đề cho pin điện (tiếng Việt)
    private static final String[] BATTERY_TITLES = {
        "Pin lithium cao cấp - Dung lượng lớn, bền bỉ",
        "Bộ pin điện chính hãng - Chất lượng đảm bảo",
        "Pin xe điện thương hiệu - Công nghệ tiên tiến",
        "Bộ pin ắc quy điện - Hiệu suất cao",
        "Pin lithium-ion - Sạc nhanh, độ bền vượt trội",
        "Pin xe điện nhập khẩu - Tiêu chuẩn quốc tế",
        "Bộ pin thông minh - Hệ thống quản lý BMS",
        "Pin điện cao cấp - Tuổi thọ dài, ổn định",
        "Pin xe điện mới - Chưa sử dụng, seal nguyên",
        "Bộ pin chính hãng - Bảo hành dài hạn"
    };

    // Danh sách mô tả cho pin điện (tiếng Việt)
    private static final String[] BATTERY_DESCRIPTIONS = {
        "Pin lithium dung lượng cao, chất lượng đảm bảo. Đã qua kiểm tra kỹ thuật, hoạt động ổn định. Tương thích với nhiều loại xe điện phổ biến. Bảo hành chính thức, hỗ trợ kỹ thuật.",
        "Bộ pin điện chính hãng với công nghệ tiên tiến. Tuổi thọ cao, khả năng giữ điện tốt. Hệ thống quản lý pin thông minh. Lắp đặt và bảo trì chuyên nghiệp. Giá cả cạnh tranh.",
        "Pin xe điện nhập khẩu chất lượng cao. Dung lượng lớn, thời gian sạc nhanh. Độ an toàn cao, chống cháy nổ. Tương thích đa dạng loại xe. Có chứng nhận chất lượng quốc tế.",
        "Bộ pin lithium-ion hiệu suất vượt trội. Công nghệ sạc nhanh, tuổi thọ dài. Thiết kế nhỏ gọn, trọng lượng nhẹ. Hệ thống bảo vệ quá áp, quá dòng. Lựa chọn tin cậy cho xe điện.",
        "Pin điện cao cấp với độ bền vượt trội. Khả năng chịu nhiệt tốt, hoạt động ổn định. Dung lượng thực cao, ít suy giảm theo thời gian. Bảo hành 2 năm, đổi mới nếu lỗi.",
        "Bộ pin thông minh có hệ thống quản lý BMS. Theo dõi trạng thái pin real-time. Cảnh báo sớm khi có vấn đề. Tối ưu hóa quá trình sạc và xả. Tăng tuổi thọ pin đáng kể."
    };

    // Danh sách địa chỉ chi tiết (tiếng Việt)
    private static final String[] VIETNAMESE_STREET_NAMES = {
        "Đường Nguyễn Văn Linh", "Đường Lê Lợi", "Đường Trần Hưng Đạo", "Đường Hai Bà Trưng",
        "Đường Nguyễn Huệ", "Đường Lý Thường Kiệt", "Đường Phan Bội Châu", "Đường Điện Biên Phủ",
        "Đường Cách Mạng Tháng Tám", "Đường 3 Tháng 2", "Đường Võ Văn Tần", "Đường Nam Kỳ Khởi Nghĩa",
        "Đường Pasteur", "Đường Đồng Khởi", "Đường Nguyễn Thị Minh Khai", "Đường Cao Thắng"
    };

    // Biến đếm để đảm bảo mỗi tỉnh có ít nhất 1 listing
    private static int provinceIndex = 0;

    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            // Idempotent guard: if there is already any listing, skip entirely
            if (listingRepository.count() > 0) {
                logger.info("[Seeder] Existing listings detected -> skip seeding (profile devautoseed)");
                return;
            }

            logger.info("[Seeder] Starting minimal demo seeding (profile=devautoseed)");
            logger.info("[Seeder] Mode: create-drop expected on dev -> data will not persist across restarts");
            try {
                initializeSystemConfig();
                List<UserEntity> users = createUsers();
                // Filter members once
                List<UserEntity> members = new ArrayList<>();
                for (UserEntity u : users) if (u.getRole() == UserRole.MEMBER) members.add(u);
                if (members.isEmpty()) {
                    logger.warn("[Seeder] No members created -> abort listing seeding");
                    return;
                }
                seedMinimalListings(members);
                logger.info("[Seeder] Created 10 demo listings.");
            } catch (Exception e) {
                logger.error("[Seeder] Error: {}", e.getMessage(), e);
            }
        }; 
    }

    /**
     * Initialize required system configurations
     */
    private void initializeSystemConfig() {
        try {
            adminService.addConfig("POST_LISTING_NORMAL_FEE", "20000", ConfigDataType.NUMBER, "Normal listing fee");
            adminService.addConfig("POST_LISTING_PREMIUM_FEE", "50000", ConfigDataType.NUMBER, "Premium listing fee");  
            adminService.addConfig("POST_LISTING_FEATURED_FEE", "100000", ConfigDataType.NUMBER, "Featured listing fee");
            logger.info("System configuration initialized successfully");
        } catch (Exception e) {
            logger.warn("System config may already exist or initialization error: {}", e.getMessage());
        }
    }

    /**
     * Create user list using AuthService
     * 2 admins + 10 members
     */
    private List<UserEntity> createUsers() {
        List<UserEntity> users = new ArrayList<>();

        for (int i = 0; i < USER_EMAILS.length; i++) {
            // Use AuthService to register users (automatic password hashing and salt)
            boolean registered = authService.register(PASSWORDS[i], USER_EMAILS[i], USER_ROLES[i]);
            
            if (registered) {
                // Get newly created user from database
                UserEntity savedUser = userRepository.findUserByEmail(USER_EMAILS[i]);
                if (savedUser != null) {
                    users.add(savedUser);
                    
                    if (USER_ROLES[i] == UserRole.ADMIN) {
                        logger.info("Created ADMIN: {} with password: {}", USER_EMAILS[i], PASSWORDS[i]);
                    } else {
                        logger.info("Created MEMBER: {} with password: {}", USER_EMAILS[i], PASSWORDS[i]);
                    }
                }
            } else {
                logger.warn("Unable to create user or already exists: {}", USER_EMAILS[i]);
            }
        }

        return users;
    }

    /**
     * Create 100 listings (50 EVs + 50 batteries) and randomly distribute to members
     * Note: Admins cannot create listings, only MEMBERs can create
     */
    private void seedMinimalListings(List<UserEntity> members) {
        Random random = new Random();
        provinceIndex = 0;

        // Simple fixed status cycle to showcase different states
        ListingStatus[] cycle = {ListingStatus.PENDING, ListingStatus.ACTIVE, ListingStatus.SOLD, ListingStatus.SUSPENDED};

        int total = 0;
        // 5 EV
        for (int i = 0; i < evListingCount; i++) {
            try {
                UserEntity seller = members.get(random.nextInt(members.size()));
                EvVehicleEntity ev = createRandomEvVehicle(random, i);
                EvVehicleEntity savedEv = evVehicleRepository.save(ev);
                ProductEntity product = new ProductEntity();
                product.setEvVehicle(savedEv);
                product.setBattery(null);
                ProductEntity savedProduct = productRepository.save(product);
                ListingStatus status = cycle[i % cycle.length];
                ListingEntity listing = createListingForProduct(seller, savedProduct, "EV", random, status);
                ListingEntity savedListing = listingRepository.save(listing);
                LocationEntity location = createRandomLocation(savedListing, random);
                locationRepository.save(location);
                // Only create PostRequest if not already created elsewhere
                try {
                    PostRequestEntity pr = new PostRequestEntity();
                    pr.setListing(savedListing);
                    pr.setStatus(ApprovalStatus.APPROVED);
                    pr.setRequestedDate(LocalDate.now());
                    pr.setReviewedAt(LocalDateTime.now());
                    postRequestRepository.save(pr);
                } catch (Exception ex) {
                    logger.debug("[Seeder] Skip duplicate PostRequest for listing {}: {}", savedListing.getId(), ex.getMessage());
                }
                total++;
            } catch (Exception e) {
                logger.warn("[Seeder] EV listing error #{}: {}", i + 1, e.getMessage());
            }
        }

        // 5 Battery
        for (int i = 0; i < batteryListingCount; i++) {
            try {
                UserEntity seller = members.get(random.nextInt(members.size()));
                BatteryEntity battery = createRandomBattery(random, i);
                BatteryEntity savedBattery = batteryRepository.save(battery);
                ProductEntity product = new ProductEntity();
                product.setBattery(savedBattery);
                product.setEvVehicle(null);
                ProductEntity savedProduct = productRepository.save(product);
                ListingStatus status = cycle[(i + 5) % cycle.length];
                ListingEntity listing = createListingForProduct(seller, savedProduct, "BATTERY", random, status);
                ListingEntity savedListing = listingRepository.save(listing);
                LocationEntity location = createRandomLocation(savedListing, random);
                locationRepository.save(location);
                try {
                    PostRequestEntity pr = new PostRequestEntity();
                    pr.setListing(savedListing);
                    pr.setStatus(ApprovalStatus.APPROVED);
                    pr.setRequestedDate(LocalDate.now());
                    pr.setReviewedAt(LocalDateTime.now());
                    postRequestRepository.save(pr);
                } catch (Exception ex) {
                    logger.debug("[Seeder] Skip duplicate PostRequest for listing {}: {}", savedListing.getId(), ex.getMessage());
                }
                total++;
            } catch (Exception e) {
                logger.warn("[Seeder] Battery listing error #{}: {}", i + 1, e.getMessage());
            }
        }
        logger.info("[Seeder] Minimal listings created: {}", total);
    }

    /**
     * Create a random electric vehicle with random information
     * 
     * @param random Random object for generating random data
     * @param index EV index (used to create unique name)
     * @return Initialized EvVehicleEntity object (not yet saved to database)
     */
    private EvVehicleEntity createRandomEvVehicle(Random random, int index) {
        EvVehicleEntity ev = new EvVehicleEntity();
        
        // Randomly select brand and vehicle type
        String brand = EV_BRANDS[random.nextInt(EV_BRANDS.length)];
        VehicleType type = VEHICLE_TYPES[random.nextInt(VEHICLE_TYPES.length)];
        
        // Set basic information
        ev.setBrand(brand);
        ev.setType(type);
        ev.setName(brand + " " + type.name() + " " + (index + 1));
        ev.setYear(2018 + random.nextInt(7)); // Manufacturing year from 2018-2024
        
        // Set technical specifications
        ev.setMileage(random.nextInt(100000)); // Mileage from 0-100,000 km
        ev.setBatteryCapacity(new BigDecimal(40 + random.nextInt(60))); // Battery capacity from 40-100 kWh
        
        // Randomly select vehicle condition
        VehicleCondition[] conditions = VehicleCondition.values();
        ev.setConditionStatus(conditions[random.nextInt(conditions.length)]);
        
        return ev;
    }

    /**
     * Create a random battery
     * 
     * @param random Random object for generating random data
     * @param index Battery index (used to create unique model)
     * @return Initialized BatteryEntity object (not yet saved to database)
     */
    private BatteryEntity createRandomBattery(Random random, int index) {
        BatteryEntity battery = new BatteryEntity();
        
        // Randomly select battery brand
        String brand = BATTERY_BRANDS[random.nextInt(BATTERY_BRANDS.length)];
        int year = 2018 + random.nextInt(7); // Manufacturing year from 2018-2024
        
        // Set basic information
        battery.setName(brand + " Battery Pack " + (index + 1)); // Battery name
        battery.setBrand(brand);
        battery.setYear(year); // Manufacturing year
        
        // Set technical specifications
        battery.setCapacity(new BigDecimal(30 + random.nextInt(70))); // Capacity from 30-100 kWh
        battery.setHealthPercentage(60 + random.nextInt(41)); // Battery health from 60-100%
        battery.setCompatibleVehicles(generateCompatibleVehicles(random));
        
        // Randomly select battery condition
        BatteryCondition[] conditions = BatteryCondition.values();
        battery.setConditionStatus(conditions[random.nextInt(conditions.length)]);
        
        return battery;
    }

    /**
     * Create listing for a product
     * 
     * @param seller Seller (must be MEMBER, not ADMIN)
     * @param product Product (electric vehicle or battery)
     * @param productType Product type: "EV" for electric vehicle, "BATTERY" for battery
     * @param random Random object for generating random data
     * @param status Predetermined listing status
     * @return Initialized ListingEntity object (not yet saved to database)
     */
    private ListingEntity createListingForProduct(UserEntity seller, ProductEntity product, 
                                                   String productType, Random random, ListingStatus status) {
        ListingEntity listing = new ListingEntity();
        
        // Set seller and product
        listing.setSeller(seller);
        listing.setProduct(product);
        
        // Create title and description based on product type with Vietnamese content
        if ("EV".equals(productType)) {
            listing.setTitle(EV_TITLES[random.nextInt(EV_TITLES.length)]);
            listing.setDescription(EV_DESCRIPTIONS[random.nextInt(EV_DESCRIPTIONS.length)]);
            listing.setThumbnailImage("https://res.cloudinary.com/dkvldb91c/image/upload/v1759568865/swp391/listings/c5jic9fai7l0rq87ojng.webp");
        } else {
            listing.setTitle(BATTERY_TITLES[random.nextInt(BATTERY_TITLES.length)]);
            listing.setDescription(BATTERY_DESCRIPTIONS[random.nextInt(BATTERY_DESCRIPTIONS.length)]);
            listing.setThumbnailImage("https://res.cloudinary.com/dkvldb91c/image/upload/v1760317167/images_2_mrdrjy.jpg");
        }
        
        // Set random price from 50 million to 2 billion VND
        BigDecimal price = new BigDecimal(50000000 + random.nextInt(1950000000));
        listing.setPrice(price);
        
        // Set listing status from parameter with accurate distribution
        listing.setStatus(status);
        
        // Set listing type: 80% NORMAL, 15% PREMIUM, 5% FEATURED
        int typeRandom = random.nextInt(100);
        if (typeRandom < 80) {
            listing.setListingType(ListingType.NORMAL);
        } else if (typeRandom < 95) {
            listing.setListingType(ListingType.PREMIUM);
        } else {
            listing.setListingType(ListingType.FEATURED);
        }
        
        // Set random view count from 0-999
        listing.setViewsCount(random.nextInt(1000));
        
        // Set expiration time: 30-90 days from now
        listing.setExpiresAt(LocalDateTime.now().plusDays(30 + random.nextInt(61)));
        
        return listing;
    }

    /**
     * Create random location for listing
     * 
     * @param listing Listing that needs location (already saved to database)
     * @param random Random object for generating random data
     * @return Initialized LocationEntity object (not yet saved to database)
     */
    private LocationEntity createRandomLocation(ListingEntity listing, Random random) {
        LocationEntity location = new LocationEntity();
        
        // Link location with listing
        location.setListing(listing);
        
        // Ensure all provinces have at least one listing by cycling through provinces first,
        // then random distribution for remaining listings
        String selectedProvince;
        if (provinceIndex < PROVINCES.length) {
            selectedProvince = PROVINCES[provinceIndex];
            provinceIndex++;
        } else {
            // After covering all provinces, use random selection
            selectedProvince = PROVINCES[random.nextInt(PROVINCES.length)];
        }
        location.setProvince(selectedProvince);
        
        // Randomly select district
        location.setDistrict(DISTRICTS[random.nextInt(DISTRICTS.length)]);
        
        // Create random detailed address with Vietnamese street names
        location.setDetails("Số " + (random.nextInt(999) + 1) + " " + 
                          VIETNAMESE_STREET_NAMES[random.nextInt(VIETNAMESE_STREET_NAMES.length)]);
        
        return location;
    }

    /**
     * Generate compatible vehicle list for battery
     * 
     * @param random Random object for generating random data
     * @return String of compatible vehicle list, separated by comma
     */
    private String generateCompatibleVehicles(Random random) {
        List<String> vehicles = new ArrayList<>();
        
        // Create 1-3 compatible vehicle types
        int count = 1 + random.nextInt(3);
        
        for (int i = 0; i < count; i++) {
            vehicles.add(EV_BRANDS[random.nextInt(EV_BRANDS.length)]);
        }
        
        return String.join(", ", vehicles);
    }

    // Removed legacy distribution + logging methods (overkill for minimal demo seed)
}
