<?php
	date_default_timezone_set('PRC');
	//设置上传文件夹，$_REQUEST["fileDir"]为客户端传递过来的目录
	$subDir = date("Y-m");
	$uploadDir = $_SERVER['DOCUMENT_ROOT'].'/monitorBaidu/'.$_REQUEST["fileDir"].'/'.$subDir.'/';
	//如果文件夹不存在，创建
	if(!is_dir($uploadDir)){
		mkdir($uploadDir,0777,true);
	}
	//文件重新命名，防止重名
	$mainName = date("YmdGis").rand(100,999);//上传改后文件名
	$extensionName = substr($_FILES['Filedata']['name'],-4,4);//获取源文件扩展名
	//完整文件路径
	$uploadfile = $uploadDir .$mainName.$extensionName;
	//缩略图文件路径
	$thumbFile = $uploadDir .$mainName."_thumb".$extensionName;
	$temploadfile = $_FILES['Filedata']['tmp_name'];

	move_uploaded_file($temploadfile , $uploadfile);
	
	
	//使用easyphpthumbnail生成缩略图
	include_once('easyphpthumbnail.class.php');
	$thumb = new easyphpthumbnail;
	$thumb -> Thumbsize = 400;
	$thumb -> Thumbfilename = $thumbFile;//设定缩略图文件名
	$thumb -> Thumblocation = $uploadDir;//缩略图保存路径
	$thumb -> Createthumb($uploadfile,'file');
	
	//返回一个相对路径，用于保存到数据库
	echo $_REQUEST["fileDir"].'/'.$subDir.'/'.$mainName.$extensionName;
	//thumbs($uploadfile,$thumbFile,300,200);

	
?>