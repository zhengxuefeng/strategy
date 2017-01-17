
include "fb303.thrift"

namespace java com.lsh.service.driver.recommend


struct AddressItem{
	1: required string addressId,
	2: optional i32  restrictNo // 1代表限行， 0不限行
	3: required string lat
	4: required string lng
}

struct DriverItem{
	1: required string driverId,
	2: optional i32  carType // 1是4.2m， 2 金杯
	3: optional string carnumber //车牌号
	4: optional i32 has_certificate //是否有打车通行证  1表示有证  0没有证
	5: optional i32 has_wrap //是否有围板
}

struct InputDriverRec{
	1: required list<AddressItem> addressList;
	2: required list<DriverItem> driverList
	3: required i32 totalBoxCount 
	4: required string warehouse// 仓库id
	5: optional i32  cage_car// 是否使用笼车
	6: required i32 sentTime//送货的时间 0 表示当天送  1 表示明天送
	7: optional string lineNumber// 线路编号
	8: optional string boundLineDriverId// 当前线路绑定的司机ID
}



struct OutputDriverRec{
	1: required list<DriverItem> recommendDriverList;
}

service  RecommendService extends fb303.FacebookService{

   OutputDriverRec driverRecommend(1: InputDriverRec req);
	
}