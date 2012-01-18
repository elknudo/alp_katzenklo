<?php include('auth.php'); ?>
<html>
<head>
<title>Gallery Champaign
</title>
</head>
<body bgcolor=#ffcc77>

<?php
	$type = GetImageSize($_FILES['picture']['tmp_name']);
	if($type[2] != 0)
   	{
      			move_uploaded_file($_FILES['picture']['tmp_name'], "userdata/".$_FILES['picture']['name']);
      			echo "Picture ".$_FILES['picture']['name']." received";
	}
	else
	{
		echo "Error: File is not a picture";
	}
?>

</body>
</html>
