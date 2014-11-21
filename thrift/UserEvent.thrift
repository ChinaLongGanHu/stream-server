namespace java com.to8to.userevent.thrift
namespace php com.to8to.userevent.thrift

struct UserEvent
{
1:string vt //访问时间 visit_time
2:i16 et   //事件类型 event_type
3:string en //事件名称 event_name
4:string vr //访问资源 visit_resouce
5:string ci //当前日志ID current_id
}

struct PutLogReq
{
1:string uid //用户ID user_id
2:string cid //用户cookieid cookieid
3:string sid //session_id
4:string ul //用户的位置 user_location
5:string ip //用户的IP地址 ip_address
6:i16 osv //os的版本 os_version
7:i16 ost //os的type os_type
8:string pn //产品名称 product_name
9:string pv //产品版本 product_version
10:i16 ua //用户的 user_agent
11:string ev //浏览器版本 explorer_version
12:i16 st //运营商的类型 sp_type
13:i16 nt //网络类型 network_type
14:i16 dt //设备名称 device_type
15:string di //设备id device_id
16:string ds //设备分辨率 display_solution
17:string lt //离开时间 leave_time
18:string vf //访问来源 visit_from
19:list<UserEvent> e //CV事件的日志
}

service LogService
{
	void putLog(1:PutLogReq putLogReq)
}
