<?php include('auth.php'); ?>
<html>
<head>
<title>Gallery Champaign
</title>
<script src="https://www.google.com/jsapi?key=ABQIAAAA3gEIruBrbLZ7QVwNKWB-LBQVNXOUXbq_x6dWScl8H471LBxP5BRIBNg_1LUH4j9ob8zBAPA2KYrVsg"></script>
    <script type="text/javascript">

      google.load('search', '1');

      var imageSearch;

      function addPaginationLinks() {
      
        // To paginate search results, use the cursor function.
        var cursor = imageSearch.cursor;
        var curPage = cursor.currentPageIndex; // check what page the app is on
        var pagesDiv = document.createElement('div');
        for (var i = 0; i < cursor.pages.length; i++) {
          var page = cursor.pages[i];
          if (curPage == i) { 

          // If we are on the current page, then don't make a link.
            var label = document.createTextNode(' ' + page.label + ' ');
            pagesDiv.appendChild(label);
          } else {

            // Create links to other pages using gotoPage() on the searcher.
            var link = document.createElement('a');
            link.href="/image-search/v1/javascript:imageSearch.gotoPage("+i+');';
            link.innerHTML = page.label;
            link.style.marginRight = '2px';
            pagesDiv.appendChild(link);
          }
        }

        var contentDiv = document.getElementById('content');
        contentDiv.appendChild(pagesDiv);
      }

      function searchComplete() {

        // Check that we got results
        if (imageSearch.results && imageSearch.results.length > 0) {

          // Grab our content div, clear it.
          var contentDiv = document.getElementById('content');
          contentDiv.innerHTML = '';

          // Loop through our results, printing them to the page.
          var results = imageSearch.results;
          for (var i = 0; i < results.length; i++) {
            // For each result write it's title and image to the screen
            var result = results[i];
            var imgContainer = document.createElement('div');
            var title = document.createElement('div');
            
            // We use titleNoFormatting so that no HTML tags are left in the 
            // title
            title.innerHTML = result.titleNoFormatting;
            var newImg = document.createElement('img');

            // There is also a result.url property which has the escaped version
            newImg.src="/image-search/v1/result.tbUrl;"
            imgContainer.appendChild(title);
            imgContainer.appendChild(newImg);

            // Put our title + image in the content
            contentDiv.appendChild(imgContainer);
          }

          // Now add links to additional pages of search results.
          addPaginationLinks(imageSearch);
        }
      }

      function OnLoad() {
      
        // Create an Image Search instance.
        imageSearch = new google.search.ImageSearch();

        // Set searchComplete as the callback function when a search is 
        // complete.  The imageSearch object will have results in it.
        imageSearch.setSearchCompleteCallback(this, searchComplete, null);

        // Find me a beautiful car.
        imageSearch.execute("Subaru STI");
        
        // Include the required Google branding
        google.search.Search.getBranding('branding');
      }
      google.setOnLoadCallback(OnLoad);
    </script>

  </head>
  <body style="font-family: Arial;border: 0 none;">
    <div id="branding"  style="float: left;"></div><br />
    <div id="content">Loading...</div>
<div align="center">
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
	$pid = "";

	#read parameter if specified
	if(!empty($_GET)) {
		$pid = htmlspecialchars($_GET["id"]);
	}

	#scan files
	$files = scandir($path);
	$nFiles = count($files);

	#set usefull id
	if($pid == "" or $pid < 2 or $pid >= $nFiles)
	{
		$pid = 2;
	}

	#if a file exists
	if($nFiles > 2) { 
		$file = $files[$pid];
?>
<table border="0">  <colgroup width="140" span="3"></colgroup><tr> <th> 
<?php
		#insert previous arrow
		if($pid > 2) {
			$temp = $pid-1;
			echo "<a href=\"index.php?id=$temp\"><img src=\"backarrow.png\"/></a>";
		}
?>
</th><th>
<?php
		#insert back arrow
		if($pid < $nFiles -1) {
			$temp = $pid+1;
			echo "<a href=\"index.php?id=$temp\"><img src=\"forwardarrow.png\"/></a>";
		}
?>
</th></tr></table>
<?php
		#display picture
		echo "<br><br><h1>$file</h1><br> <img src=\"$relative$file\"/> <br><br>";

		#query google
		$apikey = "ABQIAAAA3gEIruBrbLZ7QVwNKWB-LBQVNXOUXbq_x6dWScl8H471LBxP5BRIBNg_1LUH4j9ob8zBAPA2KYrVsg";

	} else {
		#error
		echo "No files in Gallery";
	}
?>
</div>
</body>
</html>
