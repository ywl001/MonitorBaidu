<?php

	$imageFile = $_POST['files'];
	$arr = explode(" ",$imageFile);
	$dir = $_SERVER['DOCUMENT_ROOT'].'/monitor_image/';
	$times = 0;
	foreach($arr as $file){
		$file_path = $dir.$file;
		if(unlink($file_path)){
			$times++;
		}
	}
	echo $times;
?>