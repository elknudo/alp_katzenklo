<?php include('auth.php'); ?>
<html>
<head>
<title>Gallery Champaign
</title>
</head>
<body bgcolor=#ffcc77>


<?php
	
	if(isset($_SESSION)&&$_SESSION['loggedin']==true){
		echo '<a href="logout.php">logout</a>';
		echo '<br><br><br><br>
			<form action="upload.php" method="post" enctype="multipart/form-data">
			<input type="file" name="picture"><br>
			<input type="submit" value="Hochladen">
			</form>';}
	else
		echo '<a href="login.php">login to upload pictures</a>';

	echo '<br><br><br><br><br>';

	$path = "/opt/lampp/htdocs/userdata";
	$relative = "userdata/";
	$dir_handle = @opendir($path) or die("Unable to open $path");

	while ($file = readdir($dir_handle)) {

		if($file == "." || $file == ".." || $file == "index.php" ) {
			continue;
		}

		$size[0] = 40;
		$size[1] = 50;
		echo "$file: <br> <img src=\"$relative$file\"/> <br><br>";

	}

	closedir($dir_handle); 
?>


</body>
</html>
