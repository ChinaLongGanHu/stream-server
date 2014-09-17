<?php
namespace thrift_client_code;

error_reporting(E_ALL);

require_once __DIR__.'/lib/Thrift/ClassLoader/ThriftClassLoader.php';
require_once __DIR__.'/classGuestInfo.php';

use Thrift\ClassLoader\ThriftClassLoader;	

$GEN_DIR = realpath(dirname(__FILE__)).'/gen-php';

$loader = new ThriftClassLoader();
$loader->registerNamespace('Thrift', __DIR__ . '/lib');
$loader->registerDefinition('com\to8to\userevent\thrift', $GEN_DIR);
$loader->register();


use Thrift\Protocol\TBinaryProtocol;
use Thrift\Protocol\TCompactProtocol;
use Thrift\Transport\TSocket;
use Thrift\Transport\TFramedTransport;
use Thrift\Exception\TException;

$host = "192.168.3.62";  
$port = 1234; 
	
  
try {  
  
	$socket		= new TSocket( $host , $port );  
	$transport	= new TFramedTransport($socket);  
	$protocol 	= new TCompactProtocol($transport);  
	 
	// get our example client  
	$client = new \com\to8to\userevent\thrift\LogServiceClient($protocol);  
	
	$transport->open();  

	$req = new \com\to8to\userevent\thrift\PutLogReq();
	
	$event_pv = new \com\to8to\userevent\thrift\UserEvent();
	
	
	$post_data = json_decode( $_POST['key'],true );
	
	if( isset($post_data['pv']['gu']) ){
		$getUrl 		= strpos($post_data['pv']['gu'],'/');
		$event_pv->vr	= substr($post_data['pv']['gu'],$getUrl+2);  	 						//访问的URL
	}else{
		$event_pv->vr	= '';
	}
	
	$event_pv->ci		= isset($post_data['pv']['cd']) ? strval($post_data['pv']['cd']) : '';	//cv_id	
	$event_pv->et 		= isset($post_data['pv']['ly']) ? $post_data['pv']['ly'] : '';			//1：页面访问日志；2：页面点击日志；
	$event_pv->en 		= isset($post_data['pv']['cg']) ? $post_data['pv']['cg '] : '';		//log_type=1，不填；log_type=2，具体点击点tag
	$event_pv->vt		= isset($post_data['pv']['vt']) ? strval($post_data['pv']['vt']) : '';		//访问页面时间

	$req->e[0] = $event_pv;
	
	//循环post过来的cv值
	for( $i=0; $i<count($post_data['cv']); $i++)
	{
		$event = new \com\to8to\userevent\thrift\UserEvent();
		if( isset($post_data['cv'][$i]['gu']) ){
			$getUrl 				= strpos($post_data['cv'][$i]['gu'],'/');
			$event->vr	= substr($post_data['cv'][$i]['gu'],$getUrl+2);  	 		//访问的URL
		}else{
			$event->vr	= '';
		}
		
		$event->ci		= isset($post_data['cv'][$i]['cd']) ? strval($post_data['cv'][$i]['cd']) : '';		//cv_id	
		$event->et 		= isset($post_data['cv'][$i]['ly']) ? $post_data['cv'][$i]['ly'] : '';				//1：页面访问日志；2：页面点击日志；
		$event->en 		= isset($post_data['cv'][$i]['cg']) ? $post_data['cv'][$i]['cg'] : '';				//log_type=1，不填；log_type=2，具体点击点tag
		$event->vt		= isset($post_data['cv'][$i]['vt']) ? strval($post_data['cv'][$i]['vt']) : '';		//访问页面时间	

		$req->e[$i+1] = $event;		
	}
	
	$randid					= mt_rand(1000000,9999999).time();
	$to8tocookieid 			= md5($randid);
	
	 if( isset($_COOKIE['to8to_uid']) ){														//获取用户登录ID，如果不存在则获取cookie ID，如果没有cookieid 则生成一个
		$req->uid = $_COOKIE['to8to_uid'];
	}else{
		if( isset($_COOKIE['to8tocookieid']) ){
			$req->uid   	= $_COOKIE['to8tocookieid'];
		}else{
			setcookie( "to8tocookieid",$to8tocookieid,time()+86400*90,"/",".to8to.com" );
			$req->uid	= $to8tocookieid;
		}
	}  	
		
	if( isset($post_data['pv']['lu']) ){
		$lastUrl 	= strpos($post_data['pv']['lu'],'/');
		$req->vf 	= substr($post_data['pv']['lu'],$getUrl+2);							//来源的URL（上一页面的）
	}else{
		$req->vf	= '';
	}
	//$req->uid 		= $to8tocookieid;
	$req->sid 		= isset($_COOKIE['PHPSESSID']) ? $_COOKIE['PHPSESSID'] : '';	//两个相邻的PV时间间隔大于30分钟，则形成2个session		
	$req->ut		= isset($_COOKIE['to8to_uid']) ? 1 : 2;							//1：登录用户；2：游客用户
	$req->lt		= isset($post_data['pv']['lt']) ? strval($post_data['pv']['lt']) : '';		//离开页面时间
	
	$req->osv		= isset($post_data['pv']['ov']) ? $post_data['pv']['ov'] : '';		//操作系统版本
	
	$req->ul 		= '';				//用户的物理位置 	用户的物理位置：经度纬度：经度纬度
	$req->ds		= ''; 				//设备分辨率 		1280*640
	$req->di		= '';				//设备序列号		手机IMEI,电脑MAC
	$req->st		= intval('');		//网络运营商   	 	联通，移动，电信
	$req->nt		= intval('');		//网络类型			2G，3G，4G
	$req->pv		= '';				//产品版本			1.0，2.0
	
	
	$req->pn 	= '';					//“土巴兔”，“微信”，如果是web就是网址(product name)
	$req->ev 	= '';					//IE6.0，CHROME13.0	(浏览器版本)


	$obj = new class_guest_info;
	
	$req->ost 	= intval($obj->GetOS());				//获取访客操作系统类型	

	$req->ua 	= intval($obj->GetBrowser());			//获取访客浏览器。

	$req->ip 	= $obj->GetIP();						//获取访客IP地址。

	$req->dt 	= intval($obj->checkMobileClient());	//设备类型  1：PC；2：android；3：iPhone
	
	//$data =  json_encode($data);
	//exit();
	
	//$req->log_type = 1;
	
	//$arr = array($req);
	
	//echo "-----------PutLogReq------------<br>";
	
	//var_dump($req);  
	
	$client->putLog($req);  
	
	//echo "-----------PutLogRes------------<br>";
	//echo json_encode($res);  	
	
	$transport->close();  
  
} catch (TException $tx) {  
	//print 'Something went wrong: '.$tx->getMessage()."\n";  
} 