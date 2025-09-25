import React from "react";
import ListingPage from "./ListingPage";

// Sample pin auction data
const auctionPins = [
  {
    id: 1,
    image: "/images/pin1.jpg",
    brand: "Pin Lithium-ion cho Vinfast",
    location: "Hà Nội",
    km: "Còn 95%",
    left: "1 ngày",
    price: "25 triệu",
    owner: "******7817",
    comments: 6,
    description: "Pin còn mới, ít sử dụng.",
  },
  {
    id: 2,
    image: "/images/pin2.jpg",
    brand: "Pin LFP cho xe điện",
    location: "Đà Nẵng",
    km: "Còn 90%",
    left: "12:15:30",
    price: "18 triệu",
    owner: "Minh",
    comments: 2,
    description: "Pin tháo xe, hoạt động tốt.",
  },
  {
    id: 3,
    image: "/images/pin3.jpg",
    brand: "Pin second-hand cho xe máy điện",
    location: "TP. Hồ Chí Minh",
    km: "Còn 80%",
    left: "2 giờ",
    price: "Trả giá ngay",
    owner: "",
    comments: 0,
    description: "Chưa có bình luận",
    certified: true,
  },
];

function PinListings() {
  return (
    <ListingPage
      pageTitle="Các pin mới nhất"
      searchPlaceholder="Tìm kiếm pin..."
      brandPlaceholder="VD: Lithium-ion, LFP..."
      items={auctionPins}
      showStatusFilter={false}
    />
  );
}

export default PinListings;