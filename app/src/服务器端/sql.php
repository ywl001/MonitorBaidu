<?php

	$link = mysql_connect("localhost","root","ywl6918592");
	if (!$link) {
		echo "数据库连接失败" . mysql_error();
	}

	mysql_select_db("monitor");
	mysql_query("SET NAMES utf8");


	$sql = $_POST['sql'];
	$sql = stripslashes($sql);

    $action = $_POST['action'];

	$result = mysql_query($sql);

	if($action == 'insert'){
		echo mysql_insert_id();
	}
	else if($action == 'update' || $action =='del')
	{
		echo $result;
	}
	else if($action == 'select'){
		$arrResult=array();
		while ($temp=mysql_fetch_assoc($result)){
			array_push($arrResult,$temp);
		}
		echo json_encode($arrResult);
	}

	mysql_close($link);

?>
