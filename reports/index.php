<?PHP

define('NOINT', true); define('PREFIX', 'http://www.dmdirc.com/'); define('SUBPAGE', 'reports'); define('PAGE', 'dev');
define('TITLE', 'Reports');
require_once('/home/dmdirc/www/inc/header.php');
?>
  <style type="text/css">
   table { border-collapse: collapse; width: 100%; }
   td, th { border: 1px solid black; padding: 3px; }
   tr.good td.result { background-color: #0a0; color: white; }
   tr.bad td.result { background-color: #a00; color: white; }
  </style>
  <div class="content">
    <p>
     We use a variety of tools for testing DMDirc. The output of these tools is
     summarised below.
    </p>
    <table>
     <tr><th>Tool</th><th>Information</th><th>Results</th></tr>
<?PHP

 function showTool($link, $name, $desc, $result, $status = 'unknown') {
  echo '<tr class="', $status, '"><td><a href="', $link, '">', $name, '</a></td>';
  echo '<td>', $desc, '</td>';
  echo '<td class="result">', $result, '</td></tr>';
 }

 /** Junit **/
 $data = @file_get_contents('/home/dmdirc/www/junit/overview-summary.html');
 preg_match('#href="all-tests.html">([0-9]+)</a>#i', $data, $tests);
 preg_match('#href="alltests-fails.html">([0-9]+)</a>#i', $data, $fails);
 preg_match('#href="alltests-errors.html">([0-9]+)</a>#i', $data, $errors);
 $result = $tests[1] . ' tests, ' . $fails[1] . ' failure(s), ' . $errors[1] . ' error(s)';
 $status = ($fails[1] + $errors[1] == 0) ? 'good' : 'bad';
 showTool('frame-junit.html', 'JUnit', 'Unit test framework', $result, $status);

 /** Clover **/
 $data = @file_get_contents('clover/dashboard.html');
 preg_match('#\s*<table><tr><td>([0-9.]+)%\s*$#im', $data, $cover);
 showTool('frame-clover.html', 'Clover', 'Unit test coverage analyser', $cover[1] . '% coverage', (double) $cover[1] < 25 ? 'bad' : 'good');

 /** CPD **/
 $data = @file_get_contents('report-cpd.html');
 preg_match('#^<td class="SummaryNumber">([0-9]+)</td>#im', $data, $cpd);
 showTool('frame-cpd.html', 'Copy &amp; Paste Detection', 'Detects repeated code that may be better off refactored', $cpd[1] . ' duplications', $cpd[1] > 100 ? 'bad' : 'good');

 /** PMD **/
 $data = @file_get_contents('report-pmd.html');
 preg_match('#^<td>([0-9]+)</td><td>$#im', $data, $pmd);
 showTool('frame-pmd.html', 'PMD', 'Detects potential problems with code', $pmd[1] . ' violations', $pmd[1] > 2600 ? 'bad' : 'good');

 /** Checkstyle **/
 function showCheckstyle($title, $page, $target = 100) {
  $data = @file_get_contents('report-'.$page);
  preg_match('#^<td>([0-9]+)</td><td>([0-9]+)</td>$#im', $data, $cs);
  showTool('frame-checkstyle-'.$page, 'Checkstyle', 'Detects style errors (scope: ' . $title . ')', $cs[2] . ' errors', $cs[2] > $target ? 'bad' : 'good');
 }

 showCheckstyle('whole project', 'all.html', 750);
 showCheckstyle('actions', 'actions.html');
 showCheckstyle('command parser', 'commandparser.html');
 showCheckstyle('core', 'core.html');
 showCheckstyle('config', 'config.html');
 showCheckstyle('logger', 'logger.html');
 showCheckstyle('IRC parser', 'parser.html');
 showCheckstyle('Plugins', 'plugins.html');
 showCheckstyle('UI', 'ui.html');
?>     
    </table>
    <h2>Findbugs report</h2>
    <ul>
      <li><a href="frame-findbugs.html" title="Findbugs report">Findbugs Report</a></li>
    </ul>
    <h2>Doc Check report</h2>
    <ul>
     <li><a href="frame-doccheck.html" title="DocCheck">Doc Check report</a></li>
    </ul>
   </div>
   <div id="footer">
   </div>
  </div>
 </body>
</html>
