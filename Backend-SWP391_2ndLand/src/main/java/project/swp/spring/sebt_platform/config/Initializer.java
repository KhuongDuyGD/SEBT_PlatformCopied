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
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

@Configuration
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
            logger.info("=== STARTING DATABASE SEEDING FOR SEBT PLATFORM ===");
            logger.info("Mode: create-drop - Database will be recreated on each startup");

            try {
                // Step 1: Initialize system config
                logger.info("Step 1: Initializing system config...");
                initializeSystemConfig();
                
                // Step 2: Create users
                logger.info("Step 2: Creating {} users...", USER_EMAILS.length);
                List<UserEntity> users = createUsers();
                logger.info("Successfully created {} users", users.size());

                // Step 3: Create listings (50 EVs + 50 batteries)
                logger.info("Step 3: Creating 100 listings (50 EVs + 50 batteries)...");
                createListings(users);
                logger.info("Successfully created all listings");

                logger.info("=== DATABASE SEEDING COMPLETED SUCCESSFULLY ===");

            } catch (Exception e) {
                logger.error("ERROR DURING DATABASE SEEDING: {}", e.getMessage(), e);
                throw e;
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
    private void createListings(List<UserEntity> users) {
        Random random = new Random();
        
        // Reset province index to ensure all provinces get listings
        provinceIndex = 0;
        
        // Filter only members (exclude all admins)
        List<UserEntity> members = new ArrayList<>();
        for (UserEntity user : users) {
            if (user.getRole() == UserRole.MEMBER) {
                members.add(user);
            }
        }
        
        logger.info("Total members eligible to create listings: {}", members.size());

        if (members.isEmpty()) {
            logger.warn("No members available to create listings");
            return;
        }

        // Create status distribution with exact ratios: 40% PENDING, 35% ACTIVE, 15% SOLD, 10% SUSPENDED
        List<ListingStatus> statusDistribution = createStatusDistribution();

        // Create 100 EV listings
        logger.info("Starting to create 100 EV listings...");
        for (int i = 0; i < 100; i++) {
            try {
                // Randomly select a member as seller
                UserEntity seller = members.get(random.nextInt(members.size()));
                
                // Step 1: Create and save electric vehicle
                EvVehicleEntity ev = createRandomEvVehicle(random, i);
                EvVehicleEntity savedEv = evVehicleRepository.saveAndFlush(ev);
                logger.debug("Saved EvVehicle with ID: {}", savedEv.getId());
                
                // Step 2: Create Product linked to EV (no cascade)
                ProductEntity product = new ProductEntity();
                product.setEvVehicle(savedEv);
                product.setBattery(null); // Ensure battery is null
                ProductEntity savedProduct = productRepository.saveAndFlush(product);
                logger.debug("Saved Product with ID: {}", savedProduct.getId());
                
                // Step 3: Create Listing linked to Product and Seller - use status from distribution
                ListingStatus listingStatus = statusDistribution.get(i);
                ListingEntity listing = createListingForProduct(seller, savedProduct, "EV", random, listingStatus);
                ListingEntity savedListing = listingRepository.saveAndFlush(listing);
                logger.debug("Saved Listing with ID: {} with status: {}", savedListing.getId(), listingStatus);
                
                // Step 4: Create Location linked to Listing
                LocationEntity location = createRandomLocation(savedListing, random);
                locationRepository.saveAndFlush(location);
                logger.debug("Saved Location with ID: {}", location.getId());
                
                // Step 5: Create PostRequest with APPROVED status for immediate listing display
                PostRequestEntity postRequest = new PostRequestEntity();
                postRequest.setListing(savedListing);
                postRequest.setStatus(ApprovalStatus.APPROVED);
                postRequest.setRequestedDate(LocalDate.now().minusDays(random.nextInt(30))); // Requested 0-30 days ago
                postRequest.setReviewedAt(LocalDateTime.now().minusDays(random.nextInt(7))); // Reviewed 0-7 days ago
                postRequestRepository.saveAndFlush(postRequest);
                logger.debug("Saved PostRequest with ID: {} for Listing: {}", postRequest.getId(), savedListing.getId());
                
                // Log progress every 20 listings
                if ((i + 1) % 20 == 0) {
                    logger.info("Created {}/100 EV listings", i + 1);
                }
                
            } catch (Exception e) {
                logger.error("Error creating EV listing #{}: {}", i + 1, e.getMessage());
            }
        }
        logger.info("Completed creating 100 EV listings");

        // Create 100 battery listings
        logger.info("Starting to create 100 battery listings...");
        for (int i = 0; i < 100; i++) {
            try {
                // Randomly select a member as seller
                UserEntity seller = members.get(random.nextInt(members.size()));
                
                // Step 1: Create and save battery
                BatteryEntity battery = createRandomBattery(random, i);
                BatteryEntity savedBattery = batteryRepository.saveAndFlush(battery);
                logger.debug("Saved Battery with ID: {}", savedBattery.getId());
                
                // Step 2: Create Product linked to battery (no cascade)
                ProductEntity product = new ProductEntity();
                product.setBattery(savedBattery);
                product.setEvVehicle(null); // Ensure evVehicle is null
                ProductEntity savedProduct = productRepository.saveAndFlush(product);
                logger.debug("Saved Product with ID: {}", savedProduct.getId());
                
                // Step 3: Create Listing linked to Product and Seller - use status from distribution
                ListingStatus listingStatus = statusDistribution.get(100 + i); // Offset 100 to get remaining distribution
                ListingEntity listing = createListingForProduct(seller, savedProduct, "BATTERY", random, listingStatus);
                ListingEntity savedListing = listingRepository.saveAndFlush(listing);
                logger.debug("Saved Listing with ID: {} with status: {}", savedListing.getId(), listingStatus);
                
                // Step 4: Create Location linked to Listing
                LocationEntity location = createRandomLocation(savedListing, random);
                locationRepository.saveAndFlush(location);
                logger.debug("Saved Location with ID: {}", location.getId());
                
                // Step 5: Create PostRequest with APPROVED status for immediate listing display
                PostRequestEntity postRequest = new PostRequestEntity();
                postRequest.setListing(savedListing);
                postRequest.setStatus(ApprovalStatus.APPROVED);
                postRequest.setRequestedDate(LocalDate.now().minusDays(random.nextInt(30))); // Requested 0-30 days ago
                postRequest.setReviewedAt(LocalDateTime.now().minusDays(random.nextInt(7))); // Reviewed 0-7 days ago
                postRequestRepository.saveAndFlush(postRequest);
                logger.debug("Saved PostRequest with ID: {} for Listing: {}", postRequest.getId(), savedListing.getId());
                
                // Log progress every 20 listings
                if ((i + 1) % 20 == 0) {
                    logger.info("Created {}/100 battery listings", i + 1);
                }
                
            } catch (Exception e) {
                logger.error("Error creating battery listing #{}: {}", i + 1, e.getMessage());
            }
        }
        logger.info("Completed creating 100 battery listings");
        
        // Log final status distribution results
        logStatusDistributionResult();
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

    /**
     * Create listing status list with accurate ratios
     * 40% PENDING (80 items), 35% ACTIVE (70 items), 15% SOLD (30 items), 10% SUSPENDED (20 items)
     * Total 200 items to distribute among 200 listings
     */
    private List<ListingStatus> createStatusDistribution() {
        List<ListingStatus> statusList = new ArrayList<>();
        
        // Add 80 PENDING (40%)
        for (int i = 0; i < 80; i++) {
            statusList.add(ListingStatus.PENDING);
        }
        
        // Add 70 ACTIVE (35%)
        for (int i = 0; i < 70; i++) {
            statusList.add(ListingStatus.ACTIVE);
        }
        
        // Add 30 SOLD (15%)
        for (int i = 0; i < 30; i++) {
            statusList.add(ListingStatus.SOLD);
        }
        
        // Add 20 SUSPENDED (10%)
        for (int i = 0; i < 20; i++) {
            statusList.add(ListingStatus.SUSPENDED);
        }
        
        // Shuffle list to create randomness
        java.util.Collections.shuffle(statusList);
        
        logger.info("Created status distribution: {} PENDING, {} ACTIVE, {} SOLD, {} SUSPENDED", 
                   80, 70, 30, 20);
        
        return statusList;
    }

    /**
     * Log kết quả phân phối trạng thái listing từ database
     * Để xác minh tỷ lệ đã đúng theo yêu cầu: 40% PENDING, 35% ACTIVE, 15% SOLD, 10% SUSPENDED
     */
    private void logStatusDistributionResult() {
        try {
            long totalListings = listingRepository.count();
            long pendingCount = listingRepository.countListingEntitiesByStatus(ListingStatus.PENDING);
            long activeCount = listingRepository.countListingEntitiesByStatus(ListingStatus.ACTIVE);
            long soldCount = listingRepository.countListingEntitiesByStatus(ListingStatus.SOLD);
            long suspendedCount = listingRepository.countListingEntitiesByStatus(ListingStatus.SUSPENDED);
            
            // Calculate percentages
            double pendingPercent = totalListings > 0 ? (pendingCount * 100.0 / totalListings) : 0;
            double activePercent = totalListings > 0 ? (activeCount * 100.0 / totalListings) : 0;
            double soldPercent = totalListings > 0 ? (soldCount * 100.0 / totalListings) : 0;
            double suspendedPercent = totalListings > 0 ? (suspendedCount * 100.0 / totalListings) : 0;
            
            logger.info("=== LISTING STATUS DISTRIBUTION RESULTS ===");
            logger.info("Total listings: {}", totalListings);
            logger.info("PENDING: {} listings ({:.1f}%)", pendingCount, pendingPercent);
            logger.info("ACTIVE: {} listings ({:.1f}%)", activeCount, activePercent);
            logger.info("SOLD: {} listings ({:.1f}%)", soldCount, soldPercent);
            logger.info("SUSPENDED: {} listings ({:.1f}%)", suspendedCount, suspendedPercent);
            
            // Check if the distribution meets expected ratios
            if (Math.abs(pendingPercent - 40.0) < 2.0 && 
                Math.abs(activePercent - 35.0) < 2.0 && 
                Math.abs(soldPercent - 15.0) < 2.0 && 
                Math.abs(suspendedPercent - 10.0) < 2.0) {
                logger.info("Status distribution is ACCURATE!");
            } else {
                logger.warn("Status distribution is INACCURATE. Expected: 40% PENDING, 35% ACTIVE, 15% SOLD, 10% SUSPENDED");
            }
            
        } catch (Exception e) {
            logger.error("Error logging status distribution results: {}", e.getMessage());
        }
    }
}
