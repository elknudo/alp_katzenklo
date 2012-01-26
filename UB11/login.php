<?php
     if ($_SERVER['REQUEST_METHOD'] == 'POST') {
      session_start();

      $username = $_POST['username'];
      $password = $_POST['password'];

      // Benutzername und Passwort werden überprüft
      if ($username == 'bla' && $password == 'asdf') {
       $_SESSION['loggedin'] = true;

       header('Location: index.php'); 
       exit;
       }
      }
?>
<html> 
<head>
  <title>Log in!</title>
 </head>
 <body>
  <form action="login.php" method="post">
   Username is "bla" and password is '1234'<br>
   Username: <input type="text" name="username" /><br>
   Passwort: <input type="password" name="password" /><br>
   <input type="submit" value="Anmelden" />
  </form>
 </body>
</html>
