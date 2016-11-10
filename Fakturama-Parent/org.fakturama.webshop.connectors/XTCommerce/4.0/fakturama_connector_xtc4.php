<?php
/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * 
 * Webshop connector script for xt:Commerce 4 (www.xt-commerce.com)
 * based on the fakturama_connector script by Gerd Bartelt
 *
 * This script comes with no warranty.
 *
 * Date: 2013-10-30
 * 
 * Copy this file into the root folder of your webshop, where you 
 * find the index.php of your xt:Commerce 4.x installation
 * 
 */

define('FAKTURAMA_CONNECTOR_VERSION', '1.6.2');


// set the timezone
$set_timezone = 'Europe/Berlin';

if (ini_get('date.timezone')) {
  $set_timezone = ini_get('date.timezone');
}
date_default_timezone_set($set_timezone);

// Only for debugging. All the data is encrypted.
//define ('ENCRYPT_DATA',true);	

error_reporting(E_ALL & ~E_NOTICE);
ini_set("display_errors", "1");

// Return true if $str starts with $sub
function startsWith($str, $sub){ 
    return substr($str, 0, strlen($sub)) == $sub;
}

// Encrypt the data
function my_encrypt($s) {
	// Replace all characters
	if (defined('ENCRYPT_DATA') ) {
		$s = preg_replace("/[a-z]/", "x", $s);
		$s = preg_replace("/[A-Z]/", "X", $s);
		$s = preg_replace("/[0-9]/", "0", $s);
	}
	return $s;
}

// Remove invalid XML Characters
function stripInvalidXml($value)
{
    $ret = "";
    $current;
    if (empty($value)) 
        return $ret;
 
    $length = strlen($value);
    for ($i=0; $i < $length; $i++)
    {
        $current = ord($value{$i});
        if (($current == 0x9) ||
            ($current == 0xA) ||
            ($current == 0xD) ||
            (($current >= 0x20) && ($current <= 0xD7FF)) ||
            (($current >= 0xE000) && ($current <= 0xFFFD)) ||
            (($current >= 0x10000) && ($current <= 0x10FFFF)))
        {
            $ret .= chr($current);
        }
        else
        {
            $ret .= " ";
        }
    }
    return $ret;
}

// Convert a string to proper UTF-8
function convertToUTF8($s) { 
 
    if(!mb_check_encoding($s, 'UTF-8') 
        OR !($s === mb_convert_encoding(mb_convert_encoding($s, 'UTF-32', 'UTF-8' ), 'UTF-8', 'UTF-32'))) { 

        $s = mb_convert_encoding($s, 'UTF-8'); 
    } 
    return $s; 
} 


// Convert a string to UTF-8 and encode the special characters
// No need to check if PHP_VERSION is > 5, because
// xt:Commerce 4 requires at least Ver. 5.1.2
function my_encode($s) {

	// Replace html spaces with ASCII-Code 32 instead of 160
	// before html_entity_decode to make the string trimmable
	$s = str_replace('&nbsp;', chr(32), $s);

	// Replace <br> to newline
	$s = preg_replace('/\<br(\s*)?\/?\>/i', "\n", $s);

	// Convert to UTF-8
	$s = convertToUTF8($s);

	// Strip all HTML Tags
	$s = strip_tags($s);
	
	// Encrypt the data
	$s = my_encrypt($s);

	// Convert entities like &uuml; to ü
	$s = html_entity_decode($s, ENT_COMPAT, 'UTF-8');

	// Replace special characters
	$s = htmlspecialchars($s, ENT_COMPAT, 'UTF-8');

	// Remove invalid characters
	$s = stripInvalidXml($s);

	// Finally remove left and right spaces
	$s = trim($s);

	return $s;
}


// Exit with error message
function exit_with_error($err) {
	echo (" <error>" . $err . "</error>\n");
	echo ("</webshopexport>\n");
	exit(); 
}

//  Shopsystem defines and includes start here
define('_VALID_CALL', 'true');

$root_dir = dirname(__FILE__) .'/';

$sys_dir = $_SERVER['SCRIPT_NAME'];
$sys_dir = substr($sys_dir, 0, strripos($sys_dir, '/')+1);

define('_SRV_WEBROOT', $root_dir);
define('_SRV_WEB', $sys_dir);

include _SRV_WEBROOT .'conf/paths.php';
include _SRV_WEBROOT .'conf/config.php';
include _SRV_WEBROOT .'conf/database.php';
// Character Set of the webshop. This is used to send notification comments.
include _SRV_WEBROOT .'conf/config_charsets.php';

include _SRV_WEBROOT .'xtFramework/classes/class.filter.php';
$filter = new filter();

include _SRV_WEBROOT .'xtFramework/library/smarty/Smarty.class.php';
include _SRV_WEBROOT .'xtFramework/library/smarty/SmartyValidate.class.php';
require _SRV_WEBROOT._SRV_WEB_FRAMEWORK .'classes/class.template.php';

// PHPmailer
include _SRV_WEBROOT._SRV_WEB_FRAMEWORK.'library/phpmailer/class.phpmailer.php';

include _SRV_WEBROOT._SRV_WEB_FRAMEWORK .'library/adodb/adodb-exceptions.inc.php';
include _SRV_WEBROOT._SRV_WEB_FRAMEWORK .'library/adodb/adodb.inc.php';
include _SRV_WEBROOT._SRV_WEB_FRAMEWORK .'library/adodb/session/adodb-session2.php';

$ADODB_CACHE_DIR = _SRV_WEBROOT .'cache';

define('ADODB_ERROR_LOG_TYPE', 3);
define('ADODB_ERROR_LOG_DEST', _SRV_WEBROOT._SRV_WEB_LOG. 'db_error.log');

// DB Connection
$db = ADONewConnection('mysql');
$db->Connect(_SYSTEM_DATABASE_HOST, _SYSTEM_DATABASE_USER, _SYSTEM_DATABASE_PWD, _SYSTEM_DATABASE_DATABASE);
$db->Execute("SET NAMES 'utf8'");
$db->Execute("SET CHARACTER_SET_CLIENT=utf8");
$db->Execute("SET CHARACTER_SET_RESULTS=utf8");

function get_data( $query, $db )
{
$record = $db->Execute($query);
	if ($record->RecordCount() > 0) {
		while(!$record->EOF){

			$records[] = $record->fields;

			$record->MoveNext();
		} $record->Close();
	  return $records;
	}
}

include _SRV_WEBROOT .'xtFramework/classes/class.plugin.php';

include _SRV_WEBROOT .'xtFramework/classes/class.permissions.php';
include _SRV_WEBROOT .'xtFramework/classes/class.item_permissions.php';

include _SRV_WEBROOT .'xtFramework/functions/build_define.inc.php';

require _SRV_WEBROOT._SRV_WEB_FRAMEWORK .'classes/class.hookpoint.php';
$xtPlugin = new hookpoint();

// store handler
include _SRV_WEBROOT .'xtFramework/store_handler.php';

// some magic... ;o)
$current_shop_id = $store_handler->shop_id;

_buildDefine($db, TABLE_CONFIGURATION);

/*
 *  All configuration should be done now...
 */

header('Content-Type: application/xml; charset=utf-8');


// parse POST parameters
//$getshipped = (isset($_POST['getshipped']) ? $_POST['getshipped'] : '');
$action = (isset($_POST['action']) ? $_POST['action'] : '');
$orderstosync = (isset($_POST['setstate']) ? $_POST['setstate'] : '{}');
$maxproducts = (isset($_POST['maxproducts']) ? $_POST['maxproducts'] : '');
$lasttime = (isset($_POST['lasttime']) ? $_POST['lasttime'] : '');


$orderstosync = trim($orderstosync, '{}');
$orderstosync = explode(",", $orderstosync);

$username = $_POST['username'];
$password = $_POST['password'];

// does action start with "get" ?
if (strncmp($action, "get", 3) == 0) {
  // does the action contains one of the following keys:
  $action_getproducts = strpos($action,"products");
  $action_getorders   = strpos($action,"orders");
  $action_getcontacts = strpos($action,"contacts");
}


// get the Admin
$admin_query = $db->Execute("SELECT email 
			    FROM ". TABLE_ADMIN_ACL_AREA_USER ." 
			    WHERE handle = '". $username ."' 
			    AND user_password = md5('". $password ."') 
			    AND user_id = 1");

if( $admin_query->RecordCount() < 1 )  exit_with_error('Invalid username or password');

// generate header of response
echo ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
echo ("<webshopexport version=\"". FAKTURAMA_CONNECTOR_VERSION ."\" >\n");

echo ("<phpversion>". PHP_VERSION ."</phpversion>\n");
echo ("<webshop shop=\"xt:Commerce ". _SYSTEM_VERSION ."\" ");

echo ("url=\"". _SYSTEM_BASE_HTTP ."\"");	
echo ("></webshop>\n");


			/*
			 *  Smarty DB Resource
			 */
			function resource_db_template($tpl_name, & $tpl_source, & $smarty) {
				global $mail_data;

				if ($tpl_name == 'html') {
					$tpl_source = $mail_data['mail_body_html'];
				}
				elseif ($tpl_name == 'txt') {
					$tpl_source = $mail_data['mail_body_txt'];
				}

				return true;

			}

			function resource_db_timestamp($tpl_name, & $tpl_timestamp, & $smarty) {
				$tpl_timestamp = time();
				return true;

			}

			function resource_db_secure($tpl_name, & $smarty) {
				// assume all templates are secure
				return true;
			}

			function resource_db_trusted($tpl_name, & $smarty) {
				// not used for templates
			}

      // Update the shop values
	foreach ($orderstosync as $ordertosync) {

 	    list($orders_id_tosync, $orders_status_tosync) = explode("=", trim($ordertosync));

	    $customer_notified = 0;
	    $customer_show_comment = 0;

	    // Notify the customer
	    $notify_comments = '';

	    // Is there a comment ?
	    if (strlen ($orders_status_tosync) > 1) {
		$notify_comments = substr($orders_status_tosync, 1);
	    }
	
	    // First character is the new status
	    $orders_status_tosync = substr($orders_status_tosync, 0, 1);

	    // Change the Fakturama status_id to Veyton status_id
	    $orders_status_tosync_array = array('1'=>16, '2'=>17, '3'=>33);

	    if( !empty($orders_status_tosync) && array_key_exists($orders_status_tosync, $orders_status_tosync_array) )
	    {
	      $orders_status_tosync = $orders_status_tosync_array[$orders_status_tosync];
	    }
 		
	    if (startsWith($notify_comments,"*")) {

            // Remove the "*"
	    $notify_comments = substr($notify_comments, 1);

	    // Replace the &comma;
            $notify_comments = str_replace('&comma;', ",", $notify_comments);

	    // Replace the &equal;
            $notify_comments = str_replace('&equal;', "=", $notify_comments);

	    // Convert it the correct character encoding if necessary
	    if (function_exists('iconv') && (strtolower( _SYSTEM_CHARSET ) != 'utf-8'))
	    {
	    	$notify_comments = iconv("UTF-8", _SYSTEM_CHARSET . "//TRANSLIT", $notify_comments);
	    }

	    $notify_comments_mail = "\n".$notify_comments;
	    $customer_show_comment = 1;
	    
		$query_update_orders = $db->Execute("SELECT o.orders_status, o.language_code, o.customers_email_address, status.status_name 
						    FROM ". TABLE_ORDERS ." o 
						    LEFT JOIN ". TABLE_SYSTEM_STATUS_DESCRIPTION ." status 
						    ON (status.status_id = $orders_status_tosync) AND (o.language_code = status.language_code) 
						    WHERE o.shop_id = ". $current_shop_id ." 
						    AND o.orders_id = ". (int)$orders_id_tosync );

		$update_orders = $query_update_orders->fields;

		$email_valid = 1;
		if (empty ($update_orders['customers_email_address']))
		{
		  $email_valid = 0;
		  echo (" <error>" . 'No valid email' . "</error>\n");
		}
		else
		{
		$rs = $db->Execute("SELECT mt.tpl_special, 
					      mt.email_from, mt.email_from_name, mt.email_reply, mt.email_reply_name, mt.email_forward, 
					      mtc.mail_body_html, mtc.mail_body_txt, mtc.mail_subject, 
					      o.billing_firstname, o.billing_lastname, o.language_code, o.shop_id 
					      FROM ". TABLE_MAIL_TEMPLATES ." mt 
					      LEFT JOIN ". TABLE_MAIL_TEMPLATES_CONTENT ." mtc 
					      ON (mtc.tpl_id = mt.tpl_id) 
					      LEFT JOIN ". TABLE_ORDERS ." o 
					      ON (o.orders_id = ". (int)$orders_id_tosync .") 
					      WHERE mt.tpl_type = 'update_order-admin' 
					      AND mtc.language_code = o.language_code");
		$mail_data = $rs->fields;


			$mail = new PHPMailer();

			if (isset ($_SESSION['language_charset'])) {
				$mail->CharSet = $_SESSION['language_charset'];
			} else {
				$lang_query = $db->Execute("SELECT language_charset FROM ". TABLE_LANGUAGES ." WHERE code = '". _STORE_LANGUAGE ."'");
				  
				    $lang_charset = $lang_query->fields['language_charset'];
				  
				$mail->CharSet = $lang_charset;
			}
			// SetLanguage Multilanguage
		    if (isset (	$_SESSION['language_code'])) {
				$lang_code = $_SESSION['language_code'];
			} else $lang_code = _STORE_LANGUAGE;


			$mail->SetLanguage($lang_code, _SRV_WEBROOT._SRV_WEB_FRAMEWORK.'library/phpmailer/');
			
			if (_SYSTEM_MAIL_TYPE == 'smtp') {
				$mail->IsSMTP();
				$mail->SMTPKeepAlive = true; // turn on SMTP authentication true/false
				if (_STORE_SMTP_AUTH == 'true') {
				    $mail->SMTPAuth = true; 
				} else {
				    $mail->SMTPAuth = false; 
				}
				$mail->Username = _STORE_SMTP_USERNAME; // SMTP username
				$mail->Password = _STORE_SMTP_PASSWORD; // SMTP password
				$mail->Host = _STORE_SMTP_HOST; // specify SMTP server "smtp1.example.com;smtp2.example.com"
				$mail->Port = _STORE_SMTP_PORT;
			}

			if (_SYSTEM_MAIL_TYPE == 'sendmail') {
				$mail->IsSendmail();
				$mail->Sendmail = _SYSTEM_SENDMAIL_PATH;
			}
			if (_SYSTEM_MAIL_TYPE == 'mail') {
				$mail->IsMail();
			}


				$smarty = new Smarty;
				// assign language to template for caching
				$smarty->assign('language', $mail_data['language_code']);
				$smarty->force_compile = true;

				$smarty->compile_dir = _SRV_WEBROOT .'templates_c';
				$smarty->register_resource("db", array(
				"resource_db_template",
				"resource_db_timestamp",
				"resource_db_secure",
				"resource_db_trusted"));

				$smarty->assign('_system_template', _STORE_DEFAULT_TEMPLATE);
				$smarty->assign('_system_base_url', _SYSTEM_BASE_URL._SRV_WEB);
				$smarty->assign('_system_logo_url', _SYSTEM_BASE_URL._SRV_WEB .'media/logo/'. _STORE_LOGO);
				$smarty->assign('_system_footer_txt', _STORE_EMAIL_FOOTER_TXT);
				$smarty->assign('_system_footer_html', _STORE_EMAIL_FOOTER_HTML);
				$smarty->assign('_system_mail_css', _SYSTEM_BASE_URL._SRV_WEB .'templates/'. _STORE_DEFAULT_TEMPLATE .'/css/mail.css');

				$smarty->assign('order_data',
						array('billing_firstname' => $mail_data['billing_firstname'],
						      'billing_lastname' => $mail_data['billing_lastname'],
						      'orders_id' => $orders_id_tosync)
						);
				$smarty->assign('comments', nl2br($notify_comments_mail)); 
				$smarty->assign('status', $update_orders['status_name']);

				$html_mail = $smarty->fetch("db:html");
				$text_mail = $smarty->fetch("db:txt");



		$from_email_address = $mail_data['email_from'];
		$from_email_name = empty($mail_data['email_from_name']) ? _STORE_NAME : $mail_data['email_from_name'];

		$to_email_address = $update_orders['customers_email_address'];

		$email_subject = $mail_data['mail_subject'];

		if ($mail_data['email_reply'] != '') {
		  $reply_address = $mail_data['email_reply'];
		  $reply_address_name = $mail_data['email_reply_name'];
		}
		else
		{
		  $reply_address = $mail_data['email_from'];
		  $reply_address_name = '';
		}

			$mail->From = $from_email_address;
			$mail->Sender = $from_email_address;
			$mail->FromName = $from_email_name;
			$mail->AddAddress($to_email_address, '');
			if ($mail_data['email_forward'] != '') {
			      $emails = explode(',', $mail_data['email_forward']);
			      foreach ($emails as $key => $val) {
				$mail->AddBCC($val, '');
			      }
			}
			$mail->AddReplyTo($reply_address, $reply_address_name);

			$mail->WordWrap = 75; // set word wrap to 75 characters
			$mail->Subject = $email_subject;

			if( $html_mail != '' )
			{
			$mail->IsHTML(true);
			$mail->Body = $html_mail;
			$mail->AltBody = $text_mail;
			}
			else
			{
			$mail->IsHTML(false);
			$mail->Body = $text_mail;
			}

			$customer_notified = 1;

			if (!$mail->Send())
			{
			  $customer_notified = 0;
			  return "Error sending email to: \"". $to_email_address ."\" - " . $mail->ErrorInfo;
			}

	    	}

	    }
	    	if ( in_array($orders_status_tosync, $orders_status_tosync_array) ){
			$db->Execute("UPDATE ". TABLE_ORDERS ." 
					  SET orders_status = ". $orders_status_tosync ."
					  WHERE orders_id = ". (int)$orders_id_tosync );

			$db->Execute("INSERT INTO ". TABLE_ORDERS_STATUS_HISTORY ." 
						 (orders_id, orders_status_id, date_added, customer_notified, comments, change_trigger, customer_show_comment)
					  VALUES (". (int)$orders_id_tosync .", ". $orders_status_tosync .", now(), ". $customer_notified .", '". $notify_comments ."', 'admin', ". $customer_show_comment .")");

		}

	}



		// Generate list of all products
		if ($action_getproducts) 
		{
		  $imagepath =  _SRV_WEB . _SRV_WEB_IMAGES . _DIR_INFO;
		  $fs_imagepath = _SRV_WEBROOT . _SRV_WEB_IMAGES . _DIR_INFO;

		  // Select only modified products since $lasttime
		  $lasttime_query = "";
		  if ($lasttime > 0) {
			  $lasttime_query = " AND ( prod.last_modified > '". $lasttime ."') ";
		  }

		  // Limit the query to $maxproducts
		  $productslimit_query = "";
		  if ($maxproducts > 0) {
			  $productslimit_query = " LIMIT ". (int)$maxproducts;
		  }

		  echo (" <products imagepath=\"". $imagepath ."\">\n");

		  $query_get_products = get_data("SELECT pro_des.products_name, pro_des.products_description, pro_des.products_short_description, 
							prod.products_model, prod.products_image, prod.products_quantity, prod.products_id, 
							prod.products_ean, prod.products_price, 
							status_des.status_name, cat_des.categories_name, 
							cat.parent_id, tr.tax_rate, tc.tax_class_title 

						  FROM ". TABLE_PRODUCTS_DESCRIPTION ." pro_des 
						  LEFT JOIN ". TABLE_PRODUCTS ." prod 
						  ON (prod.products_id = pro_des.products_id) 
						  LEFT JOIN ". TABLE_PRODUCTS_TO_CATEGORIES ." prod_cat 
						  ON (prod_cat.products_id = prod.products_id) 
						  LEFT JOIN ". TABLE_CATEGORIES_DESCRIPTION ." cat_des 
						  ON (cat_des.categories_id = prod_cat.categories_id) AND (cat_des.language_code = '". _STORE_LANGUAGE ."')  
						  LEFT JOIN ". TABLE_SYSTEM_STATUS_DESCRIPTION ." status_des 
						  ON (prod.products_vpe = status_des.status_id) AND (status_des.language_code = '". _STORE_LANGUAGE ."') 
						  LEFT JOIN ". TABLE_CATEGORIES ." cat 
						  ON (cat.categories_id = cat_des.categories_id) 
						  LEFT JOIN ". TABLE_LANGUAGES ." lang 
						  ON (lang.code = pro_des.language_code) 
						  LEFT JOIN ". TABLE_TAX_RATES ." tr 
						  ON (tr.tax_class_id = prod.products_tax_class_id) 
						  LEFT JOIN ". TABLE_TAX_CLASS ." tc 
						  ON (prod.products_tax_class_id = tc.tax_class_id) 
						  WHERE
						  (lang.code = '". _STORE_LANGUAGE ."') AND (prod.products_status = 1)
						  ". $lasttime_query ."
						  ". $productslimit_query, $db);


		      $last_products_model_name = "";

		    foreach( $query_get_products as $products )
		    {
		      $products_model_name = $products['products_model'] . $products['products_name'];

			if( $last_products_model_name != $products_model_name )
			{
			echo ("  <product ");
			echo ("gross=\"". my_encode(number_format($products['products_price'], 2)) ."\" ");
			echo ("vatpercent=\"". my_encode(number_format($products['tax_rate'], 2)) ."\" ");
			echo ("quantity=\"". my_encode($products['products_quantity']) ."\" ");
			echo ("id=\"". my_encode($products['products_id']) ."\" ");
			echo (">\n");
			echo ("   <model>". my_encode($products['products_model']) ."</model>\n");
			echo ("   <ean>". my_encode($products['products_ean']) ."</ean>\n");
			echo ("   <name>". my_encode($products['products_name']) ."</name>\n");

			#####################
			## CATEGORIES PATH ##
			#####################
			if( $products['parent_id'] > 0 )
			{
			  $parentindex = $products['parent_id'];
			  $parent_cat = array();
			  while( $parentindex > 0 )
			  {
			    $query_get_parent_categories = get_data("SELECT cat_des.categories_name AS parent_cat , cat.parent_id AS p_id 
								    FROM ". TABLE_CATEGORIES_DESCRIPTION ." cat_des 
								    INNER JOIN ". TABLE_CATEGORIES ." cat 
								    ON (cat_des.categories_id = cat.categories_id) 
								    WHERE cat_des.categories_id = ". $parentindex ." 
								    AND language_code = '". _STORE_LANGUAGE ."'", $db);

			      foreach( $query_get_parent_categories as $parent_categories ) 
			      {
				$parent_cat[] = $parent_categories['parent_cat'];
			      }

			    $parentindex = $parent_categories['p_id'];
			  }
			
			  // bring it in the right order
			  $parent_cat = array_reverse($parent_cat);
			  // build a path from the found categories
			  $cat_path = implode("/", $parent_cat) ."/";;

			echo ("   <category>". my_encode($cat_path . $products['categories_name']) ."</category>\n");
			}
			else
			{
			echo ("   <category>". my_encode($products['categories_name']) ."</category>\n");
			}

			echo ("   <qunit>". my_encode($products['status_name']) ."</qunit>\n");
			echo ("   <vatname>". my_encode($products['tax_class_title']) ."</vatname>\n");
			echo ("   <short_description>". my_encode($products['products_short_description']) ."</short_description>\n");
			// Use the image only, if it exists	
			if (is_file($fs_imagepath . $products['products_image']))
			{
			echo ("   <image>". my_encode($products['products_image']) ."</image>\n");
			}
			echo ("  </product>\n\n");

			$last_products_model_name = $products_model_name;
			}

		    }

		  echo (" </products>\n\n\n\n");
		}


		// Generate list of all orders
		if ($action_getorders)
		{
		  //  ORDERS_STATUS FROM FAKTURAMA
		  //   1 = Offen / Open
		  //   2 = In Bearbeitung / Pending
		  //   3 = Versandt / Shipped
		  //  ORDERS_STATUS IN VEYTON
		  //  16 = Offen / Open
		  //  17 = In Bearbeitung / Pending
		  //  23 = Zahlung erhalten / Payment received
		  //  32 = Zahlung storniert / Payment canceled
		  //  33 = Versandt / Shipped
		  //  34 = Storniert / Canceled
		  $query_orders = $db->Execute("SELECT * FROM ". TABLE_ORDERS ." 
						WHERE shop_id = ". $current_shop_id ." 
						AND orders_status = 16 
						ORDER BY orders_id DESC");
		  while(!$query_orders->EOF)
		  {
		    $new_orders[] = $query_orders->fields;
		    $query_orders->MoveNext();
		  }

		    echo (" <orders>\n");

		    for( $x = 0; count($new_orders) > $x; $x++ )
		    {
		    $billing_company_merge = $new_orders[$x]['billing_company'] ." ". $new_orders[$x]['billing_company2'] ." ". $new_orders[$x]['billing_company3'];
		    $delivery_company_merge = $new_orders[$x]['delivery_company'] ." ". $new_orders[$x]['delivery_company2'] ." ". $new_orders[$x]['delivery_company3'];
		    echo ("  <order id=\"". my_encode($new_orders[$x]['orders_id']) ."\" date=\"". my_encode($new_orders[$x]['date_purchased']) ."\" ");
		    echo ("currency=\"". $new_orders[$x]['currency_code'] ."\" ");
		    echo ("currency_value=\"". $new_orders[$x]['currency_value'] ."\" ");

		    $query_orders_status = $db->Execute("SELECT status_name 
						      FROM ". TABLE_SYSTEM_STATUS_DESCRIPTION ." 
						      WHERE language_code = '". _STORE_LANGUAGE ."' AND status_id = ". $new_orders[$x]['orders_status']);

			$orders_status = $query_orders_status->fields;

		    echo ("status=\"". $orders_status['status_name'] ."\" ");
		    echo (">\n");

		    echo ("   <contact id=\"". my_encode($new_orders[$x]['customers_cid'])."\">\n");
		    echo ("    <gender>". my_encode($new_orders[$x]['billing_gender'])."</gender>\n");
		    echo ("    <firstname>". my_encode($new_orders[$x]['billing_firstname'])."</firstname>\n");
		    echo ("    <lastname>". my_encode($new_orders[$x]['billing_lastname'])."</lastname>\n");
		    echo ("    <company>". my_encode($billing_company_merge)."</company>\n");
		    echo ("    <street>". my_encode($new_orders[$x]['billing_street_address'])."</street>\n");
		    echo ("    <zip>". my_encode($new_orders[$x]['billing_postcode'])."</zip>\n");
		    echo ("    <city>". my_encode($new_orders[$x]['billing_city'])."</city>\n");
		    echo ("    <country>". my_encode($new_orders[$x]['billing_country'])."</country>\n");
		    echo ("    <delivery_gender>". my_encode($new_orders[$x]['delivery_gender'])."</delivery_gender>\n");
		    echo ("    <delivery_firstname>". my_encode($new_orders[$x]['delivery_firstname'])."</delivery_firstname>\n");
		    echo ("    <delivery_lastname>". my_encode($new_orders[$x]['delivery_lastname'])."</delivery_lastname>\n");
		    echo ("    <delivery_company>". my_encode($delivery_company_merge)."</delivery_company>\n");
		    echo ("    <delivery_street>". my_encode($new_orders[$x]['delivery_street_address'])."</delivery_street>\n");
		    echo ("    <delivery_zip>". my_encode($new_orders[$x]['delivery_postcode'])."</delivery_zip>\n");
		    echo ("    <delivery_city>". my_encode($new_orders[$x]['delivery_city'])."</delivery_city>\n");
		    echo ("    <delivery_country>". my_encode($new_orders[$x]['delivery_country'])."</delivery_country>\n");
		    echo ("    <phone>". my_encode($new_orders[$x]['billing_phone'])."</phone>\n");
		    echo ("    <email>". my_encode($new_orders[$x]['customers_email_address'])."</email>\n");
		    echo ("   </contact>\n");

		    $query_orders_history = $db->Execute("SELECT orders_status_id, date_added, comments 
						      FROM ". TABLE_ORDERS_STATUS_HISTORY ." 
						      WHERE orders_id = ". $new_orders[$x]['orders_id']);

		      $new_comment = $query_orders_history->fields;
		      
			if( strlen($new_comment['comments']) > 0 )
			{
			echo ("    <comment date=\"". my_encode($new_comment['date_added']) ."\">");
			echo ( my_encode(stripslashes($new_comment['comments'])) );
			echo ("</comment>\n");
			}


				      $query_new_products = get_data("SELECT op.products_id, op.products_model, op.products_name, 
									      op.products_quantity, op.products_price, op.products_tax, 
									      FORMAT((op.products_price * (1 + op.products_tax / 100)), 2) AS GROSS, 
									    p.products_ean, p.products_image, p.products_vpe, 
									    cat_des.categories_name, status_des.status_name, 
									    stats.orders_stats_price, tax.tax_class_title 
								      FROM ". TABLE_ORDERS_PRODUCTS ." op 
								      LEFT JOIN ". TABLE_PRODUCTS ." p 
								      ON (op.products_id = p.products_id) 
								      LEFT JOIN ". TABLE_PRODUCTS_TO_CATEGORIES ." cat 
								      ON (cat.products_id = p.products_id) 
								      LEFT JOIN ". TABLE_CATEGORIES_DESCRIPTION ." cat_des 
								      ON (cat.categories_id = cat_des.categories_id) AND (cat_des.language_code = '". _STORE_LANGUAGE ."') 
								      LEFT JOIN ". TABLE_SYSTEM_STATUS_DESCRIPTION ." status_des 
								      ON (p.products_vpe = status_des.status_id) AND (status_des.language_code = '". _STORE_LANGUAGE ."') 
								      LEFT JOIN ". TABLE_ORDERS_STATS ." stats 
								      ON (stats.orders_id = ". $new_orders[$x]['orders_id'] .") 
								      LEFT JOIN ". TABLE_TAX_CLASS ." tax 
								      ON (p.products_tax_class_id = tax.tax_class_id) 
								      WHERE op.orders_id = ". $new_orders[$x]['orders_id'], $db);
				      

				      $query_shipping = get_data("SELECT ts.shipping_id, sd.shipping_name, 
								    FORMAT((sc.shipping_price * (1 + tr.tax_rate / 100)), 2) AS SHIPGROSS, 
								    tax.tax_class_title, tr.tax_rate 
								    FROM ". TABLE_SHIPPING ." ts 
								    LEFT JOIN ". TABLE_SHIPPING_COST ." sc 
								    ON (sc.shipping_id = ts.shipping_id) 
								    LEFT JOIN ". TABLE_SHIPPING_DESCRIPTION ." sd 
								    ON (sd.shipping_id = ts.shipping_id) AND (sd.language_code = '". _STORE_LANGUAGE ."') 
								    LEFT JOIN ". TABLE_TAX_CLASS ." tax 
								    ON (ts.shipping_tax_class = tax.tax_class_id) 
								    LEFT JOIN ". TABLE_TAX_RATES ." tr 
								    ON (tr.tax_zone_id = sc.shipping_geo_zone) AND (tr.tax_class_id = tax.tax_class_id) 
								    WHERE ts.shipping_code = '". $new_orders[$x]['shipping_code'] ."'", $db);


				      $query_payment = get_data("SELECT p.payment_code, pd.payment_name 
								  FROM ". TABLE_PAYMENT ." p 
								  LEFT JOIN ". TABLE_PAYMENT_DESCRIPTION ." pd 
								  ON (pd.payment_id = p.payment_id) AND (pd.language_code = '". _STORE_LANGUAGE ."') 
								  WHERE p.payment_code = '". $new_orders[$x]['payment_code'] ."'", $db);
		      
		      
				    foreach( $query_new_products as $new_products )
				    {
				      echo ("   <item ");
				      echo ("productid=\"". my_encode($new_products['products_id'])."\" ");
				      echo ("quantity=\"". my_encode($new_products['products_quantity'])."\" ");
				      echo ("gross=\"". my_encode($new_products['GROSS'])."\" ");
				      echo ("vatpercent=\"". my_encode(number_format($new_products['products_tax'], 2))."\">\n");
				      echo ("    <model>". my_encode((empty($new_products['products_model']) ? $new_products['products_name'] : $new_products['products_model']))."</model>\n");
				      echo ("    <ean>". my_encode($new_products['products_ean'])."</ean>\n");
				      echo ("    <name>". my_encode($new_products['products_name'])."</name>\n");
				      echo ("    <category>". my_encode($new_products['categories_name'])."</category>\n");
				      echo ("    <qunit>". my_encode($new_products['status_name'])."</qunit>\n");
				      echo ("    <vatname>". my_encode($new_products['tax_class_title'])."</vatname>\n");

				    // There are no <attributes> in xt:Commerce 4 
				    // Every attribute is a new article (Master/Slave system)

				      echo ("   </item>\n");
				    }

				    foreach ( $query_shipping as $shipping )
				    {
				      echo ("   <shipping ");
				      echo ("gross=\"". my_encode($shipping['SHIPGROSS'])."\" ");
				      echo ("vatpercent=\"". my_encode(number_format($shipping['tax_rate'], 2))."\">\n");
				      echo ("    <name>". my_encode($shipping['shipping_name'])."</name>\n");
				      echo ("    <vatname>". my_encode($shipping['tax_class_title'])."</vatname>\n");
				      echo ("   </shipping>\n");
				    }

				    foreach ( $query_payment as $payment )
				    {
				      echo ("   <payment ");
				      echo ("type=\"". my_encode($payment['payment_code'])."\" ");
				      echo ("total=\"". my_encode(number_format($new_products['orders_stats_price'], 2))."\">\n");
				      echo ("    <name>". my_encode($payment['payment_name'])."</name>\n");
				      echo ("   </payment>\n");
				    }

		    echo ("  </order>\n\n");
		    }

		    echo (" </orders>\n");
		}

echo ("</webshopexport>\n");

?>