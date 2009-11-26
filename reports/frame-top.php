<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
 <head>
  <title>DMDirc :: Reports</title>
  <link rel="stylesheet" type="text/css" href="http://www.dmdirc.com/res/style.css">
  <link rel="icon" type="image/png" href="http://www.dmdirc.com/res/favicon.png">
 </head>
 <body>
  <div id="container">
   <!-- <img id="logo" src="http://www.dmdirc.com/res/logo.png" alt="DMDirc logo"> -->
<!--   <h1>DMDirc<span id="tagline">the intelligent IRC client</span></h1>-->
<?PHP
 ob_start();
 define('NOINT', true); define('PREFIX', 'http://www.dmdirc.com/'); define('SUBPAGE', $_GET['page']); define('PAGE', 'dev'); require_once('/home/dmdirc/www/inc/menu.php');
 $data = ob_get_contents();
 ob_end_clean();

 echo str_replace('href="', 'target="_parent" href="', $data);
 ?>
  </div>
 </body>
</html>
