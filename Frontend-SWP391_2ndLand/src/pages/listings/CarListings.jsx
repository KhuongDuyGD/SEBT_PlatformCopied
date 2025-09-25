import React from "react";
import ListingPage from "./ListingPage";

// Sample car auction data
const auctionCars = [
  {
    id: 1,
    image: "/images/car1.jpg",
    brand: "Peugeot 2008 GT Line 2022",
    location: "Hà Nội",
    km: "27,000 km",
    left: "1 ngày",
    price: "499 triệu",
    owner: "******7817",
    comments: 6,
    description: "tư vấn thêm mấy dòng 🙌",
  },
  {
    id: 2,
    image: "/images/car2.jpg",
    brand: "Mazda 2S 2014 Trắng",
    location: "Thanh Hóa",
    km: "130,000 km",
    left: "23:03:42",
    price: "Trả giá ngay",
    owner: "Sơn",
    comments: 1,
    description: "mình có chiếc mitsubishi mi...",
  },
  {
    id: 3,
    image: "/images/car3.jpg",
    brand: "Vinfast Lux a 2.0 Cao cấp...",
    location: "TP. Hồ Chí Minh",
    km: "14,700 km",
    left: "22:42:19",
    price: "Trả giá ngay",
    owner: "",
    comments: 0,
    description: "Chưa có bình luận",
    certified: true,
  },
];

function CarListings() {
  return (
    <ListingPage
      pageTitle="Các xe mới nhất"
      searchPlaceholder="Tìm kiếm xe..."
      brandPlaceholder="VD: Vinfast, Peugeot..."
      items={auctionCars}
      showStatusFilter={true}
    />
  );
}

export default CarListings;