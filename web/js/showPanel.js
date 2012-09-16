/**********************************************************************************************
 *  showPanel - Draw JMRI panels on browser screen
 *    Retrieves panel xml and attempts to build panel client-side from the xml, including
 *    click functions.  Sends and listens for changes to panel elements using the xmlio server.
 *    If no parm passed, page will list links to available panels.
 *  Approach:  Read panel's xml and create widget objects with all needed attributes.  There are 
 *    3 "widgetFamily"s: text, icon and drawn.  States are handled by naming members 
 *    iconX, textX, cssX where X is the state.
 *    CSS classes are used throughout to attach events to correct widgets, as well as control appearance.
 *    The xmlio element name is used to send changes to xmlio server and to process changes made elsewhere.
 *    Drawn widgets are handled by drawing directly on the javascript "canvas" layer.
 *  Loop: 	1) request panel and process the returned panel xml.
 *  		2) send list of current states to server and wait for changes
 *  		3) receive change? set related widget states and go back to 2)
 *  		4) browser user clicks on widget? send "set state" command and go to 3)
 *  		5) error? go back to 2) 
 *  TODO: handle multisensors, other widgets correctly
 *  TODO: move occupiedsensor state to widget from block, to allow skipping of unchanged
 *  TODO: handle dashed track
 *  TODO: if no elements found, don't send list to xmlio server (prevent looping)
 *  TODO: handle drawn ellipse, levelxing (for LMRC APB)
 *  TODO: fix issue with FireFox using size of alt text for rotation of unloaded images
 *  TODO: add error message and stop heartbeat on change failure, add retry button
 *  TODO: address color differences between java panel and javascript panel (e.g. lightGray)
 *  TODO: determine proper level (z-index) for canvas layer
 *  TODO: fix getNextState() to handle clicking multi-state widgets (like signalheads)
 *  TODO: diagnose and correct the small position issues visible with footscray
 *  TODO: verify that assuming same rotation and scale for all icons in a "set" is OK
 *  TODO: deal with mouseleave, mouseout, touchout, etc.
 *  TODO: remove duplicates from list before sending
 *  TODO: handle really exotic layoutturnouts, such as double-crossovers
 *  TODO: handle turnoutdrawunselectedleg = "yes"
 *  TODO: store all tracksegments (mainline) before main loop (cross-dependencies, turnout to tracksegment) 
 *   
 **********************************************************************************************/

//persistent (global) variables
var $gTimeout = 120; //timeout in seconds
var $gWidgets = {};  //array of all widget objects, key=CSSId
var $gPanel = {}; 	//store overall panel info
var $gPts = {}; 	//array of all points, key="pointname.pointtype" (used for layoutEditor panels)
var $gBlks = {}; 	//array of all blocks, key="pointname.pointtype" (used for layoutEditor panels)
var $gXHRList;  	//persistent variable to allow aborting of superseded "list" connections
var $gXHRChg;	  	//persistent variable to allow aborting of superseded "change" connections
var $gCtx;  //persistent context of canvas layer   
var DOWNEVENT;  //either mousedown or touchstart, based on device
var UPEVENT;    //either mouseup or touchend, based on device

var SIZE = 3;  //default factor for circles
var UNKNOWN = 		'1';  //constants to match JMRI state names
var ACTIVE =  		'2';
var CLOSED =  		'2';
var INACTIVE= 		'4';
var THROWN =  		'4';
var INCONSISTENT = 	'8';
var PT_CEN = ".1";  //named constants for point types
var PT_A = ".2";
var PT_B = ".3";
var PT_C = ".4";
var PT_D = ".5";
var DARK        = 0x00;  //named constants for signalhead states
var RED         = 0x01;
var FLASHRED    = 0x02;
var YELLOW      = 0x04;
var FLASHYELLOW = 0x08;
var GREEN       = 0x10;
var FLASHGREEN  = 0x20;
var LUNAR       = 0x40;
var FLASHLUNAR  = 0x80;
var RH_TURNOUT = 1; //named constants for turnout types
var LH_TURNOUT = 2;
var WYE_TURNOUT = 3;
var DOUBLE_XOVER = 4;
var RH_XOVER = 5;
var LH_XOVER = 6;
var SINGLE_SLIP = 7;
var DOUBLE_SLIP = 8;

//process the response returned for the requestPanelXML command
var $processPanelXML = function($returnedData, $success, $xhr) {

	var $xml = $($returnedData);  //jQuery-ize returned data for easier access

	//remove whitespace
	$xml.xmlClean();

	//get the panel-level values from the xml
	var $panel = $xml.find('panel');
	$($panel[0].attributes).each(function() {
		$gPanel[this.name] = this.value;
	});
	$('div#panelArea').width($gPanel.width);
	$('div#panelArea').height($gPanel.height);
	
	//insert the canvas layer and set up context used by layouteditor "drawn" objects, set some defaults 
	if ($gPanel.paneltype == "LayoutPanel") {
		$('div#panelArea').before("<canvas id='panelCanvas' width=" + $gPanel.width + "px height=" + 
				$gPanel.height +"px style='position:absolute;z-index:2;'>");
		var canvas = document.getElementById("panelCanvas");  
		$gCtx = canvas.getContext("2d");  
		$gCtx.strokeStyle = $gPanel.defaulttrackcolor;
		$gCtx.lineWidth = $gPanel.sidetrackwidth;
		
		//set background color from panel attribute
		$('div#panelArea').css({backgroundColor: $gPanel.backgroundcolor});
	}

	//process all widgets in the panel xml, drawing them on screen, and building persistent arrays
	$panel.contents().each( 
			function() {
				//convert attributes to an object array
				var $widget = new Array();
				$widget['widgetType'] = this.nodeName;
				$(this.attributes).each(function() {
					$widget[this.name] = this.value;
				});
				//default various css attributes to not-set, then set in later code as needed
				var $hoverText = "";  

				//add and normalize the various type-unique values, from the various spots they are stored
				//  icons named based on states returned from xmlio server, 
				//    1=unknown, 2=active/closed, 4=inactive/thrown, 8=inconsistent
				$widget['state'] = UNKNOWN; //initial state is unknown
				$widget['element']  =	""; //default to no xmlio type (avoid undefined)
				$widget['scale']  =	"1.0"; //default to no scale
				$widget['id'] = "spWidget_" + $gUnique();//set id to a unique value (since same element can be in multiple widgets)
				$widget['widgetFamily'] = $getWidgetFamily($widget);
				var $jc = "";
				if ($widget.class != undefined) {
					var $ta = $widget.class.split('.'); //get last part of java class name for a css class
					$jc = $ta[$ta.length - 1];
				}
				$widget['classes'] = $widget.widgetType + " " + $widget.widgetFamily + " rotatable " + $jc;
				if ($widget.momentary == "true") {
					$widget.classes += " momentary ";
				}
				if ($widget.hidden == "yes") {
					$widget.classes += " hidden ";
				}
				//set additional values in this widget
				switch ($widget.widgetFamily) {
				case "icon" :
					switch ($widget.widgetType) {
					case "fastclock" :
						$widget['icon1'] = 		"/resources/clock2.gif";
						$widget['scale'] = 		$(this).attr('scale');
						$widget['level'] =		10;  //not included in xml
						break;
					case "positionablelabel" :
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						var $rotation = 		$(this).find('icon').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icon').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('icon').attr('scale');
						break;
					case "turnouticon" :
						$widget['name']  =		$widget.turnout; //normalize name
						$widget['element']  =	"turnout"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('icons').find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('icons').find('closed').attr('url');
						$widget['icon4'] =  	$(this).find('icons').find('thrown').attr('url');
						$widget['icon8'] =		$(this).find('icons').find('inconsistent').attr('url');
						var $rotation = 		$(this).find('icons').find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icons').find('unknown').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('icons').find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "sensoricon" :
						$widget['name']  =		$widget.sensor; //normalize name
						$widget['element']  =	"sensor"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('active').attr('url');
						$widget['icon4'] =  	$(this).find('inactive').attr('url');
						$widget['icon8'] =		$(this).find('inconsistent').attr('url');
						var $rotation = 		$(this).find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('unknown').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "signalheadicon" :
						$widget['name']  =		$widget.signalhead; //normalize name
						$widget['element']  =	"signalhead"; //what xmlio server calls this
						$widget['icon' + DARK] 	=	$(this).find('icons').find('dark').attr('url');
						$widget['icon' + RED] =  	$(this).find('icons').find('red').attr('url');
						$widget['icon' + YELLOW] = $(this).find('icons').find('yellow').attr('url');
						$widget['icon' + GREEN] =	$(this).find('icons').find('green').attr('url');
						$widget['icon' + FLASHRED] =	$(this).find('icons').find('flashred').attr('url');
						$widget['icon' + FLASHYELLOW] =	$(this).find('icons').find('flashyellow').attr('url');
						$widget['icon' + FLASHGREEN] =	$(this).find('icons').find('flashgreen').attr('url');
						var $rotation = 		$(this).find('icons').find('dark').find('rotation').text();
						$widget['degrees'] = 	($(this).find('icons').find('dark').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('icons').find('dark').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "multisensoricon" :
						$widget['name'] =  		$(this).find('active').attr('sensor'); //get first active name
						$widget['element']  =	"sensor"; //what xmlio server calls this
						$widget['icon1'] = 		$(this).find('unknown').attr('url');
						$widget['icon2'] =  	$(this).find('active').attr('url');
						$widget['icon4'] =  	$(this).find('inactive').attr('url');
						$widget['icon8'] =		$(this).find('inconsistent').attr('url');
						var $rotation = 		$(this).find('unknown').find('rotation').text();
						$widget['degrees'] = 	($(this).find('unknown').attr('degrees') * 1) + ($rotation * 90);
						$widget['scale'] = 		$(this).find('unknown').attr('scale');
						if ($widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					}
					$widget['safeName'] = $safeName($widget.name);  //add a html-safe version of name

					if ($widget.name) { //if name available, use it as hover text
						$hoverText = " title='"+$widget.name+"' alt='"+$widget.name+"'";
					}

					//add the image to the panel area, with appropriate css classes and id (skip any unsupported)
					if ($widget.icon1 != undefined) {
						$("div#panelArea").append("<img id=" + $widget.id +
								" class='" + $widget.classes +
								"' src='" + $widget["icon"+$widget['state']] + "' " + $hoverText + "/>");

						//also add in overlay text if specified  (append "overlay" to id to keep them unique)
						if ($widget.text != undefined) {
							$("div#panelArea").append("<div id=" + $widget.id + "overlay class='overlay'>" + $widget.text + "</div>");
							$("div#panelArea>#"+$widget.id+"overlay").css({position:'absolute',left:$widget.x+'px',top:$widget.y+'px',zIndex:($widget.level-1)});
						} 
					}
					$preloadWidgetImages($widget);
					$gWidgets[$widget.id] = $widget; //store widget in persistent array
					$setWidgetPosition($("div#panelArea>#"+$widget.id));
					break;

				case "text" :
					$widget['styles'] = $getTextCSSFromObj($widget);
					switch ($widget.widgetType) {
					case "sensoricon" :
						$widget['name']  =		$widget.sensor; //normalize name
						$widget['element']  =	"sensor"; //what xmlio server calls this
						//set each state's text
						$widget['text1'] = 		$(this).find('unknownText').attr('text');
						$widget['text2'] =  	$(this).find('activeText').attr('text');
						$widget['text4'] =  	$(this).find('inactiveText').attr('text');
						$widget['text8'] =		$(this).find('inconsistentText').attr('text');
						//set each state's css attribute array (text color, etc.)
						$widget['css1'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('unknownText')[0]));
						$widget['css2'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('activeText')[0]));
						$widget['css4'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('inactiveText')[0]));
						$widget['css8'] = 		$getTextCSSFromObj($getObjFromXML($(this).find('inconsistentText')[0]));
						if ($widget.name != undefined && $widget.forcecontroloff != "true") {
							$widget.classes += 		$widget.element + " clickable ";
						}
						break;
					case "locoicon" :
						//also set the background icon for this one (additional css in .html file)
						$widget['icon1'] = 		$(this).find('icon').attr('url');
						$widget.styles['background-image'] = "url('" + $widget.icon1 + "')";
						break;
					case "memoryicon" :
						$widget['name']  =		$widget.memory; //normalize name
						$widget['element']  =	"memory"; //what xmlio server calls this
						$widget['text']  =		$widget.memory; //use name for initial text
						break;
					}
					$widget['safeName'] = $safeName($widget.name);
					//put the text element on the page
					$("div#panelArea").append("<div id=" + $widget.id + " class='"+$widget.classes+"'>" + $widget.text + "</div>");
					$("div#panelArea>#"+$widget.id).css( $widget.styles ); //apply style array to widget

					$gWidgets[$widget.id] = $widget; //store widget in persistent array
					$setWidgetPosition($("div#panelArea>#"+$widget.id));
					break;

				case "drawn" :
					switch ($widget.widgetType) {
					case "positionablepoint" :
						//just store these points in persistent variable for use when drawing tracksegments and layoutturnouts
						//id is ident plus ".type", e.g. "A4.2"
						$gPts[$widget.ident+"."+$widget.type] = $widget;
						if ($widget.ident.substring(0,2) == "EB") {  //End bumpers use wrong type, so also store type 1
							$gPts[$widget.ident+".1"] = $widget;
						}
						break;
					case "layoutblock" :
						$widget['state'] = UNKNOWN;  //add a state member for this block
						//store these blocks in a persistent var
						//id is username
						$gBlks[$widget.username] = $widget;
						break;
					case "layoutturnout" :
						$widget['name']  =		$widget.turnoutname; //normalize name
						$widget['safeName'] = 	$safeName($widget.name);  //add a html-safe version of name
						$widget['element']  =	"turnout"; //what xmlio server calls this
						$widget['x']  		=	$widget.xcen; //normalize x,y 
						$widget['y']  		=	$widget.ycen;
						$widget.classes 	+= 	$widget.element + " clickable ";
						//set widget occupancysensor from block to speed affected changes later
						if ($gBlks[$widget.blockname] != undefined) {
							$widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor; 
						}
						//store widget in persistent array
						$gWidgets[$widget.id] = $widget; 
						//also store the turnout's 3 end points for other connections
						$storeTurnoutPoints($widget);  
						//draw the turnout
						$drawTurnout($widget);  
						//add an empty, but clickable, div to the panel and position it over the turnout circle
						$hoverText = " title='"+$widget.name+"' alt='"+$widget.name+"'";
						$("div#panelArea").append("<div id=" + $widget.id + " class='"+$widget.classes+"' "+ $hoverText +"></div>");
						var $cr = $gPanel.turnoutcirclesize * SIZE;  //turnout circle radius
						var $cd = $cr * 2;
						$("div#panelArea>#"+$widget.id).css(
								{position:'absolute',left:($widget.x-$cr)+'px',top:($widget.y-$cr)+'px',zIndex:3,
									width:$cd+'px', height:$cd+'px'});
						break;
					case "tracksegment" :
						//set widget occupancysensor from block to speed affected changes later
						if ($gBlks[$widget.blockname] != undefined) {
							$widget['occupancysensor'] = $gBlks[$widget.blockname].occupancysensor; 
						}
						//store this widget in persistent array, with ident as key
						$widget['id'] = $widget.ident; 
						$gWidgets[$widget.id] = $widget;
						//draw the tracksegment
						$drawTrackSegment($widget);
						break;
					case "backgroundColor" :  //set background color of the panel itself
						$('div#panelArea').css({"background-color" : "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ")"});
						break;
					}
					break;
				default:
					//log any unsupported widgets, listing childnodes as info
					$("div#logArea").append("<br />Unsupported: " + $widget.widgetType + ":"); 
					$(this.attributes).each(function(){
						$("div#logArea").append(" " + this.name);
					});
					$("div#logArea").append(" | ");
					$(this.childNodes).each(function(){
						$("div#logArea").append(" " + this.nodeName);
					});
					break;
				}
			}  //end of function
	);  //end of each

	//hook up mousedown state toggle function to non-momentary clickable widgets
	$('.clickable:not(.momentary)').bind(DOWNEVENT, function(e) {
	    var $newState = $getNextState($gWidgets[this.id].state);  //determine next state from current state
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	});
	
	//momentary widgets always go active on mousedown, and inactive on mouseup
	$('.clickable.momentary').bind(DOWNEVENT, function(e) {
	    var $newState = ACTIVE; 
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	}).bind(UPEVENT, function(e) {
	    var $newState = INACTIVE; 
		$setWidgetState(this.id, $newState);
		$sendElementChange($gWidgets[this.id].element, $gWidgets[this.id].name, $newState);  //send new value to xmlio server
	});
	
	//add a dummy sensor widget for use as heartbeat
	$widget = new Array();
	$widget['element'] = "sensor";
	$widget['name'] = "ISXMLIOHEARTBEAT";  //internal sensor, will be created if not there
	$widget['id'] 	= $widget['name'];
	$widget['safeName']	= $widget['name'];
	$widget['state'] = UNKNOWN;
	$gWidgets[$widget.id] = $widget;

	//send initial states to xmlio server
	$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');

};    	

//draw a Circle (color and width are optional)
function $drawCircle($ptx, $pty, $radius, $color, $width) {
	var $savStrokeStyle = $gCtx.strokeStyle;
	var $savLineWidth = $gCtx.lineWidth;
	if ($color != undefined)  $gCtx.strokeStyle = $color;
	if ($width != undefined)  $gCtx.lineWidth = $width;
	$gCtx.beginPath();
	$gCtx.arc($ptx, $pty, $radius, 0, 2*Math.PI, false);
	$gCtx.stroke();
	// put color and widths back to default
	$gCtx.lineWidth = $savLineWidth;    
	$gCtx.strokeStyle = $savStrokeStyle;    
};

//draw a Tracksegment (pass in widget)
function $drawTrackSegment($widget) {
	//get the endpoints by name
	var $pt1 = $gPts[$widget.connect1name+"."+$widget.type1];
	if ($pt1 == undefined) {
		if (window.console) console.log("can't draw tracksegment "+$widget.connect1name+"."+$widget.type1+" undefined (1)");
		return;
	}
	var $pt2 = $gPts[$widget.connect2name+"."+$widget.type2];
	if ($pt2 == undefined) {
		if (window.console) console.log("can't draw tracksegment "+$widget.connect2name+"."+$widget.type2+" undefined (2)");
		return;
	}
	
	//	set trackcolor based on block occupancy state
	var $color = $gPanel.defaulttrackcolor;
	var $blk = $gBlks[$widget.blockname];
	if ($blk != undefined) {
		if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
			$color = $blk.occupiedcolor;
		} else {
			$color = $blk.trackcolor;
		}
	}

	var $width = $gPanel.sidetrackwidth;
	if ($widget.mainline == "yes") {
		$width = $gPanel.mainlinetrackwidth;
	}
	if ($widget.angle == undefined) {
		//draw straight line between the points
		$drawLine($pt1.x, $pt1.y, $pt2.x, $pt2.y, $color, $width);
	} else {
		//draw curved line 
		if ($widget.flip == "yes") {
			$drawArc($pt2.x, $pt2.y, $pt1.x, $pt1.y, $widget.angle, $color, $width);
		} else {
			$drawArc($pt1.x, $pt1.y, $pt2.x, $pt2.y, $widget.angle, $color, $width);
		}
	}

};

//draw a Turnout (pass in widget)
function $drawTurnout($widget) {
	var $width = $gPanel.sidetrackwidth;
	 //set turnout width same as the A track
	if ($gWidgets[$widget.connectaname] !=undefined) {
		if ($gWidgets[$widget.connectaname].mainline == "yes") {
			$width = $gPanel.mainlinetrackwidth; 
		}
	} else {
//		if (window.console) console.log("could not get trackwidth of "+$widget.connectaname+" for "+$widget.name);
	}
	if ($gPanel.turnoutcircles == "yes") {
		$drawCircle($widget.xcen, $widget.ycen, $gPanel.turnoutcirclesize * SIZE, $gPanel.turnoutcirclecolor, 1);
	}
	var cenx = $widget.xcen;
	var ceny = $widget.ycen
	var ax = $gPts[$widget.ident+PT_A].x;
	var ay = $gPts[$widget.ident+PT_A].y;
	var bx = $gPts[$widget.ident+PT_B].x;
	var by = $gPts[$widget.ident+PT_B].y;
	var abx = ax+((bx-ax)*0.5); // midpoint AB
	var aby = ay+((by-ay)*0.5);
	var cx = $gPts[$widget.ident+PT_C].x;
	var cy = $gPts[$widget.ident+PT_C].y;
	var dx, dy, dcx, dcy;
	if ($gPts[$widget.ident+PT_D] != undefined) {
		dx = $gPts[$widget.ident+PT_D].x;
		dy = $gPts[$widget.ident+PT_D].y;
		dcx = dx+((cx-dx)*0.5); // midpoint DC
		dcy = dy+((cy-dy)*0.5);
	}
	var erase = $gPanel.backgroundcolor;
	
	//	set trackcolor based on block occupancy state
	var $color = $gPanel.defaulttrackcolor;
	var $blk = $gBlks[$widget.blockname];
	if ($blk != undefined) {
		if ($blk.occupiedsense == $blk.state) { //set the color based on occupancy state
			$color = $blk.occupiedcolor;
		} else {
			$color = $blk.trackcolor;
		}
	}

	//turnout A--B
	//         \-C
	if ($widget.type==LH_TURNOUT||$widget.type==RH_TURNOUT||$widget.type==WYE_TURNOUT) {
		$drawLine(ax, ay, cenx, ceny, $color, $width); //A to center (incoming)
		$drawLine(cenx, ceny, bx, by, erase, $width); //erase center to B (straight leg)
		$drawLine(cenx, ceny, cx, cy, erase, $width); //erase center to C (diverging leg)
		//if closed or thrown, draw the selected leg in the default track color
		if ($widget.state == CLOSED || $widget.state == THROWN) {
			if ($widget.state == $widget.continuing) {
				$drawLine(cenx, ceny, bx, by, $color, $width); //center to B (straight leg)
			} else {
				$drawLine(cenx, ceny, cx, cy, $color, $width); //center to C (diverging leg)
			}
		}
	// xover A--B
	//       D--C
	} else if ($widget.type==LH_XOVER||$widget.type==RH_XOVER) {
		if ($widget.state == CLOSED || $widget.state == THROWN) {
			$drawLine(ax, ay, bx, by, erase, $width); //erase A to B
			$drawLine(dx, dy, cx, cy, erase, $width); //erase D to C
			$drawLine(abx, aby, dcx, dcy, erase, $width); //erase midAB to midDC
			$drawLine(abx, aby, dcx, dcy, erase, $width); //erase midAB to midDC
			if ($widget.state == $widget.continuing) {
				$drawLine(ax, ay, bx, by, $color, $width); //A to B
				$drawLine(dx, dy, cx, cy, $color, $width); //D to C
			} else {
				if ($widget.type==RH_XOVER) {
					$drawLine(ax, ay, abx, aby, $color, $width); //A to midAB
					$drawLine(abx, aby, dcx, dcy, $color, $width); //midAB to midDC
					$drawLine(dcx, dcy, cx, cy, $color, $width); //midDC to C
				} else {  //LH_XOVER
					$drawLine(bx, by, abx, aby, $color, $width); //B to midAB
					$drawLine(abx, aby, dcx, dcy, $color, $width); //midAB to midDC
					$drawLine(dcx, dcy, dx, dy, $color, $width); //midDC to D
				}
			}
		}
	}
};

//store the various points defined with a Turnout (pass in widget)
//   see jmri.jmrit.display.layoutEditor.LayoutTurnout for background
function $storeTurnoutPoints($widget) {
	var $t = [];
	$t['ident'] = $widget.ident+PT_B;  //store B endpoint
	$t['x'] = $widget.xb * 1.0;
	$t['y'] = $widget.yb * 1.0;
	$gPts[$t.ident] = $t;
	var $t = [];
	$t['ident'] = $widget.ident+PT_C;  //store C endpoint
	$t['x'] = $widget.xc * 1.0;
	$t['y'] = $widget.yc * 1.0;
	$gPts[$t.ident] = $t;
	var $t = [];
	if ($widget.type==LH_TURNOUT||$widget.type==RH_TURNOUT||$widget.type==WYE_TURNOUT) {
		$t['ident'] = $widget.ident+PT_A;  //calculate and store A endpoint (mirror of B for these)
		$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
		$gPts[$t.ident] = $t;
		var $t = [];
	} else if ($widget.type==LH_XOVER||$widget.type==RH_XOVER||$widget.type==WYE_XOVER) {
		$t['ident'] = $widget.ident+PT_A;  //calculate and store A endpoint (mirror of C for these)
		$t['x'] = $widget.xcen - ($widget.xc - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yc - $widget.ycen);
		$gPts[$t.ident] = $t;
		var $t = [];
		$t['ident'] = $widget.ident+PT_D;  //calculate and store D endpoint (mirror of B for these)
		$t['x'] = $widget.xcen - ($widget.xb - $widget.xcen);
		$t['y'] = $widget.ycen - ($widget.yb - $widget.ycen);
		$gPts[$t.ident] = $t;
	}
};    	

//drawLine, passing in values from xml
function $drawLine($pt1x, $pt1y, $pt2x, $pt2y, $color, $width) {
	var $savLineWidth = $gCtx.lineWidth;
	var $savStrokeStyle = $gCtx.strokeStyle;
	if ($color != undefined)  $gCtx.strokeStyle = $color;
	if ($width != undefined)  $gCtx.lineWidth = $width;
	$gCtx.beginPath();  
	$gCtx.moveTo($pt1x, $pt1y);  
	$gCtx.lineTo($pt2x, $pt2y);  
	$gCtx.closePath();  
	$gCtx.stroke();  
	// put color and width back to default
	$gCtx.strokeStyle = $savStrokeStyle;    
	$gCtx.lineWidth = $savLineWidth;    
};    	

//drawArc, passing in values from xml
function $drawArc(pt1x, pt1y, pt2x, pt2y, degrees, $color, $width) {
    // Compute arc's chord
    var a = pt2x - pt1x;
    var o = pt2y - pt1y;
    var chord=Math.sqrt(((a*a)+(o*o))); //in pixels

    if (chord > 0.0) {  //don't bother if no length
    	//save track settings for restore
    	var $savLineWidth = $gCtx.lineWidth;
    	var $savStrokeStyle = $gCtx.strokeStyle;
    	if ($color != undefined)  $gCtx.strokeStyle = $color;
    	if ($width != undefined)  $gCtx.lineWidth = $width;

    	var halfAngle = (degrees/2) * Math.PI / 180; //in radians
        var radius = (chord/2)/(Math.sin(halfAngle));  //in pixels
        // Circle
        var startRad = Math.atan2(a, o) - halfAngle; //in radians
        // calculate center of circle
        var cx = ((pt2x * 1.0) - Math.cos(startRad) * radius);  
        var cy = ((pt2y * 1.0) + Math.sin(startRad) * radius);

        //calculate start and end angle
        var startAngle 	= Math.atan2(pt1y-cy, pt1x-cx); //in radians
        var endAngle 	= Math.atan2(pt2y-cy, pt2x-cx); //in radians
        var counterClockwise = false;

		$gCtx.beginPath();
        $gCtx.arc(cx, cy, radius, startAngle, endAngle, counterClockwise);
		$gCtx.stroke();
    	// put color and width back to default
    	$gCtx.strokeStyle = $savStrokeStyle;    
    	$gCtx.lineWidth = $savLineWidth;    
    }
}

//set object attributes from xml attributes, returning object 
var $getObjFromXML = function(e){
	var $widget = {};
	$(e.attributes).each(function() {
		$widget[this.name] = this.value;
	});
	return $widget;
};    	

//build and return CSS array from attributes passed in
var $getTextCSSFromObj = function($widget){
	var $retCSS = {};
	$retCSS['color'] = '';  //only clear attributes 
	$retCSS['background-color'] = '';
	if ($widget.red != undefined) {
		$retCSS['color'] = "rgb(" + $widget.red + "," + $widget.green + "," + $widget.blue + ") ";
	}
	if ($widget.redBack != undefined) {
		$retCSS['background-color'] = "rgb(" + $widget.redBack + "," + $widget.greenBack + "," + $widget.blueBack + ") ";
	}
	if ($widget.size != undefined) {
		$retCSS['font-size'] = $widget.size + "px ";
	}
	if ($widget.margin != undefined) {
		$retCSS['padding'] = $widget.margin + "px ";
	}
	if ($widget.borderSize != undefined) {
		$retCSS['border-width'] = $widget.borderSize + "px ";
	}
	if ($widget.redBorder != undefined) {
		$retCSS['border-color'] = "rgb(" + $widget.redBorder + "," + $widget.greenBorder + "," + $widget.blueBorder + ") ";;
		$retCSS['border-style'] = 'solid';
	}
	if ($widget.style != undefined) {
		switch ($widget.style) { //set font based on style attrib from xml
		case "1":
			$retCSS['font-weight'] = 'bold';
			break;
		case "2":
			$retCSS['font-style'] = 'italic';
			break;
		case "3":
			$retCSS['font-weight'] = 'bold';
			$retCSS['font-style'] = 'italic';
			break;
		}
	}

    return $retCSS;
};



//place widget in correct position, rotation, z-index and scale.
var $setWidgetPosition = function(e) {

	var $id = e.attr('id');
	var $widget = $gWidgets[$id];  //look up the widget and get its panel properties

	if ($widget != undefined) {  //don't bother if widget not found

		var $height = e.height() * $widget.scale;
		var $width =  e.width()  * $widget.scale;

		//if image is not loaded yet, set callback to do this again when it is loaded
		//TODO: firefox returns a height for the alt text, so this doesn't work right
		if ($height == 0) {
			e.load(function(){
				$setWidgetPosition($(this));
			});
		} else {
			//calculate x and y adjustment needed to keep upper left of bounding box in the same spot
			//  adapted to match JMRI's NamedIcon.rotate().  Note: transform-origin set in .html file
			var tx = 0.0;
			var ty = 0.0;

			if ($widget.degrees != undefined && $widget.degrees != 0 && $height > 0) {
				var $rad = $widget.degrees*Math.PI/180.0;

				if (0<=$widget.degrees && $widget.degrees<90 || -360<$widget.degrees && $widget.degrees<=-270){
					tx = $height*Math.sin($rad);
					ty = 0.0;
				} else if (90<=$widget.degrees && $widget.degrees<180 || -270<$widget.degrees && $widget.degrees<=-180) {
					tx = $height*Math.sin($rad)-$width*Math.cos($rad);
					ty = -$height*Math.cos($rad);
				} else if (180<=$widget.degrees && $widget.degrees<270 || -180<$widget.degrees && $widget.degrees<=-90) {
					tx = -$width*Math.cos($rad);
					ty = -$width*Math.sin($rad)-$height*Math.cos($rad);
				} else /*if (270<=$widget.degrees && $widget.degrees<360)*/ {
					tx = 0.0;
					ty = -$width*Math.sin($rad);
				}
			}
			//position widget to adjusted position, set z-index, then set rotation
			e.css({position:'absolute',left:(parseInt($widget.x)+tx)+'px',top:(parseInt($widget.y)+ty)+'px',zIndex:$widget.level});
			if ($widget.degrees != undefined && $widget.degrees != 0){
				var $rot = "rotate("+$widget.degrees+"deg)";
				e.css({MozTransform:$rot,WebkitTransform:$rot,msTransform:$rot});
			}
			//set new height and width if scale specified 
			if ($widget.scale != 1 && $height > 0){
				e.css({height:$height+'px',width:$width+'px'});
			}
		} //if height == 0
	}
};

//set new value for all widgets having specified type and element name
var $setElementState = function($element, $name, $newState) {
//	if (window.console) console.log( "setting " + $element + " " + $name + " --> " + $newState);
	//loop thru widgets, setting all matches
	jQuery.each($gWidgets, function($id, $widget) {
		//make the change if widget matches, and not already this value
		if ($widget.element == $element && $widget.safeName == $name && $widget.state != $newState) {
			$setWidgetState($id, $newState);
		}
		//if sensor and changed, also check widget's occupancy sensor and redraw widget's color if matched 
		if ($element == 'sensor' && $widget.occupancysensor == $name) {
//			if (window.console) console.log( "setting widget "+$id +" for block sensor " + $widget.occupancysensor );
			$gBlks[$widget.blockname].state = $newState; //set the block to the newstate first
			switch ($widget.widgetType) {
			case 'layoutturnout' :
				$drawTurnout($widget);
				break;
			case 'tracksegment' :
				$drawTrackSegment($widget);
				break;
			}
		}

	});
};

//set new value for widget, showing proper icon, return widgets changed
var $setWidgetState = function($id, $newState) {
	var $widget = $gWidgets[$id];
	if ($widget.state != $newState) {  //don't bother if already this value
		if (window.console) console.log( "setting " + $id + " for " + $widget.element + " " + $widget.name + " --> " + $newState);
		$widget.state = $newState;  
		switch ($widget.widgetFamily) {
		case "icon" :
			$('img#'+$id).attr('src', $widget['icon'+$newState]);  //set image src to next state's image
			break;
		case "text" :
			if ($widget.element == "memory") {
				$('div#'+$id).text($newState);  //set memory text to new value from server
			} else {
				if ($widget['text'+$newState] != undefined) {
					$('div#'+$id).text($widget['text'+$newState]);  //set text to new state's text
				}
				if ($widget['css'+$newState] != undefined) {
					$('div#'+$id).css($widget['css'+$newState]); //set css to new state's css
				}
			}
			break;
		case "drawn" :
			if ($widget.widgetType == "layoutturnout") {
				$drawTurnout($widget);
			}
			break;
		}
		$gWidgets[$id].state = $newState;  //update the changed widget back to persistent var
	}
};

//return a unique ID # when called
var $gUnique = function () {
    if (typeof $gUnique.id === 'undefined') {
    	$gUnique.id = 0;
    }
    $gUnique.id++;
    return $gUnique.id;
};

//clean up a name, for example to use as an id  
var $safeName = function($name){
  if ($name == undefined) {
	  return "unique-" + $gUnique();
  } else {
	  return $name.replace(/:/g, "_").replace(/ /g, "_").replace(/%20/g, "_");
  }
};

//send request for state change 
var $sendElementChange = function($element, $name, $nextState){
	if (window.console) console.log("sending " + $element + " " + $name + " --> " + $nextState);
	var $commandstr = "<xmlio><" + $element + " name='" + $name + "' set='" + $nextState +"'/></xmlio>";
	$sendXMLIOChg($commandstr);
};

//build xml for current state values for all jmri elements, to be sent to xmlio server to wait on changes 
var $getXMLStateList = function(){
	var $retXml = "";
	jQuery.each($gWidgets, function($id, $widget) {
		if ($widget.name != undefined) { 
			$retXml += "<" + $widget.element + " name='" + $widget.name + "' value='" + $widget.state +"'/>";
		}
	});
    return $retXml;
};

//send the list of current values to the server, and setup callback to process the response (will stall and wait for changes)
var $sendXMLIOList = function($commandstr){
//	if (window.console) console.log( "sending a list: " + $commandstr);
	if (window.console) console.log( "sending a list");
	if ($gXHRList != undefined && $gXHRList.readyState != 4) {  
		if (window.console) console.log( "aborting active list connection");
		$gXHRList.abort();
	}
	$gXHRList = $.ajax({ 
		type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
			if (window.console) console.log( "processing returned list");
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
//				if (window.console) console.log("set type="+this.nodeName+" name="+$(this).attr('name')+" to value="+$(this).attr('value'));
				$setElementState(this.nodeName, $safeName($(this).attr('name')), $(this).attr('value'));
			});
			//send current xml states to xmlio server, will "stall" and wait for changes if matched
			$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
			endAndStartTimer();
		},
		async: true,
		timeout: $gTimeout * 3 * 1000,  //triple the time timeout
		dataType: 'xml' //<--dataType
	});
};

//send a "set value" command to the server, and process the returned value
var $sendXMLIOChg = function($commandstr){
	if (window.console) console.log( "sending a change");
	if ($gXHRChg != undefined && $gXHRChg.readyState != 4) {  
		if (window.console) console.log( "aborting active change connection");
		$gXHRChg.abort();
	}
	$gXHRChg = $.ajax({ 
		type: 'POST',
		url:  '/xmlio/',
		data: $commandstr,
		success: function($r, $s, $x){
//		    if (window.console) console.log( "rcving a change request");
			var $r = $($r);
			$r.xmlClean();
			$r.find("xmlio").children().each(function(){
				if (window.console) console.log("rcvd change "+this.nodeName+" "+$(this).attr('name')+" --> "+$(this).attr('value'));
				$setElementState(this.nodeName, $safeName($(this).attr('name')), $(this).attr('value'));
				//send current xml states to xmlio server, will "stall" and wait for changes if matched
				$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');  //TODO: remove this once multi-session fixed
				endAndStartTimer();
			});
		},
		async: true,
		timeout: 5000,  //five seconds
		dataType: 'xml' //<--dataType
	});
};

//handle ajax errors, excluding abort, but including timeout, by resending the list
$(document).ajaxError(function(event,xhr,opt, exception){
	if (xhr.statusText !="abort") {
		if (window.console) console.log("AJAX error: " + xhr.statusText + ", retrying....");
		$sendXMLIOList('<xmlio>' + $getXMLStateList() + '</xmlio>');
//	} else {
//		if (window.console) console.log("AJAX Error requesting " + opt.url + ", status= " + xhr.status + " " + xhr.statusText+ " exception=" + exception);
	}
});

//clear out whitespace from xml, function adapted from 
//http://stackoverflow.com/questions/1539367/remove-whitespace-and-line-breaks-between-html-elements-using-jquery/3103269#3103269
jQuery.fn.xmlClean = function() {
	this.contents().filter(function() {
		if (this.nodeType != 3) {
			$(this).xmlClean();
			return false;
		}
		else {
			return !/\S/.test(this.nodeValue);
		}
	}).remove();
}
//handle the toggling of the next state
var $getNextState = function($state){
	var $nextState = ($state == ACTIVE ? INACTIVE : ACTIVE);
	return $nextState;
};

//parse the page's input parameter and return value for name passed in
function getParameterByName(name) {
	var match = RegExp('[?&]' + name + '=([^&]*)')
	.exec(window.location.search);
	return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

//request and show a list of available panels from the server (used when no panel passed in)
var $showPanelList = function($panelName){
	$.ajax({
		url:  '/xmlio/list', //request proper url
		data: {type: "panel"},
		success: function($r, $s, $x){
			$(document).attr('title', 'Client-side panels');
			var $h = "<h1>Client-side panels:</h1>";
			$h += "<table><tr><th>View Panel</th><th>Type</th><th>XML</th></tr>";
            $($r).find("panel").each(function(){
            	var $t = $(this).attr("name").split("/");
            	$h += "<tr><td><a href='?name=" + $(this).attr("name") + "'>" + $(this).attr("userName") + "</a></td>";
            	$h += "<td>" + $t[0] + "</td>";
            	$h += "<td><a href='/panel/" + $(this).attr("name") + "' target=_new >XML</a></td></tr>";
            });
            $h += "</table>";
            $('div#panelArea').html($h); //put table on page
		},
		error: function($r, $s, $message){
			$('div#logArea').append("ERROR: " + $message + " sts=" + $s);
		},
		dataType: 'xml' //<--dataType
	});
};

//request the panel xml from the server, and setup callback to process the response
var $requestPanelXML = function($panelName){

	$.ajax({
		type: 'GET',
		url:  '/panel/' + $panelName, //request proper url
		success: function($r, $s, $x){
			$processPanelXML($r, $s, $x); //handle returned data
		},
		error: function($r, $s, $message){
			$('div#logArea').append("ERROR: " + $message + " sts=" + $s);
		},
		async: true,
		timeout: 5000,  
		dataType: 'xml' //<--dataType
	});
};

//preload all images referred to by the widget
var $preloadWidgetImages = function($widget) {
	if ($widget['icon1'] != undefined) $("<img src='" + $widget['icon1'] + "'/>");
	if ($widget['icon2'] != undefined) $("<img src='" + $widget['icon2'] + "'/>");
	if ($widget['icon4'] != undefined) $("<img src='" + $widget['icon4'] + "'/>");
	if ($widget['icon8'] != undefined) $("<img src='" + $widget['icon8'] + "'/>");
};    	

//determine widget "family" for broadly grouping behaviors
//note: not-yet-supported widgets are commented out here so as to return undefined
var $getWidgetFamily = function($widget) {

	if ($widget.widgetType== "positionablelabel" && $widget.text != undefined) {
		return "text";  //special case to distinguish text vs. icon labels
	}
	if ($widget.widgetType== "sensoricon" && $widget.icon == "no") {
		return "text";  //special case to distinguish text vs. icon labels
	}
	switch ($widget.widgetType) {
	case "memoryicon" :
	case "locoicon" :
//	case "reportericon" :
		return "text";
		break;
	case "positionablelabel" :
	case "turnouticon" :
	case "sensoricon" :
	case "multisensoricon" :
	case "fastclock" :
	case "signalheadicon" :
//	case "signalmasticon" :
		return "icon";
		break;
	case "layoutturnout" :
	case "tracksegment" :
	case "positionablepoint" :
	case "backgroundColor" :
	case "layoutblock" :
		return "drawn";
		break;
	};
	
	return; //unrecognized widget returns undefined
};    	

var timer;
function endAndStartTimer() {
  window.clearTimeout(timer);
  timer = window.setTimeout(function(){
//	  if (window.console) console.log("timer fired");
	  var $nextState = $getNextState($gWidgets["ISXMLIOHEARTBEAT"].state);
	  $gWidgets["ISXMLIOHEARTBEAT"].state = $nextState; 
	  $sendElementChange("sensor", "ISXMLIOHEARTBEAT", $nextState)
	  endAndStartTimer(); //repeat
  	}, $gTimeout * 1000); 
}


//-----------------------------------------javascript processing starts here (main) ---------------------------------------------
$(document).ready(function() {
	
	//if panelname not passed in, show list of available panels
	var $panelName = getParameterByName('name');
	if ($panelName == undefined) {
        $showPanelList();
	} else {
		//set up events based on browser's support for touch events
		var $is_touch_device = 'ontouchstart' in document.documentElement;
		if ($is_touch_device) {
			if (window.console) console.log("touch events enabled");
			DOWNEVENT = 'touchstart';
			UPEVENT   = 'touchend';
		} else {
			if (window.console) console.log("mouse events enabled");
			DOWNEVENT = 'mousedown';
			UPEVENT   = 'mouseup';
		}
		
        //include name of panel in page title
		$(document).attr('title', 'Show JMRI Panel: ' + $panelName);
		//add a link to the panel xml
		$("div#panelFooter").append("&nbsp;<a href='/panel/" + $panelName + "' target=_new>[Panel XML]</a>");

		//request actual xml of panel, and process it on return
		$requestPanelXML($panelName);
		
		endAndStartTimer();
	}
	
});
