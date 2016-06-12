var RECYCLE_ICON_HTML = "<a title='Recycle this image' class='ui-icon ui-icon-refresh'>Recycle image</a>";
var TRASH_ICON_HTML = "<a title='Delete this image' class='ui-icon ui-icon-trash'>Delete image</a>";
var AJAX_URL = 'http://localhost.com:8080/benblamey.evaluation/SaveUserStory';

var EVENT_SYNC_STATE_CLEAN = 'CLEAN';
var EVENT_SYNC_STATE_DIRTY ='DIRTY';
var EVENT_SYNC_STATE_SENT = 'SENT';

var JQUERY_DATUM_DROPPABLE_CONFIG = {
	    accept: "li.socialnetworkdatum",
	    activeClass: "ui-state-highlight",
	    hoverClass: "drop-hover",
	    drop: droppable_datum_drop, 
	};


var gEventSyncState = EVENT_SYNC_STATE_CLEAN;
var gHaveShownSaveErrorMessage = false;
var $gGallery,$gNewEvent,$gSaveButton,$gSyncStatus,$gEvents,$gZoomDialog;    

jQuery(document).ready(function($) {
	
	var JQUERY_DATUM_DRAGGABLE_CONFIG = {
			cancel: "a.ui-icon", // clicking an icon won't initiate dragging
			revert: "invalid", // when not dropped, the item will revert back to its initial position
			//containment: "document",
			/*clones the object - This has the important side-effect that the clone is outside the scrollable area - so you don't get odd scrolling behaviour. */
			helper: "clone", 
			cursor: "move",
			cursorAt: { top: 0, left: 0 }, // Setting this to zero seems to fix the offset bug:
			// http://stackoverflow.com/questions/5791886/jquery-draggable-shows-helper-in-wrong-place-when-scrolled-down-page
			iframeFix: true,
			
			// We would like to use the #dragarea div to specify the containment.
			// Unfortunately there is an issue: $('#dragrea').height() returns 0 - pres. because it contains scrollable divs.
			// Width is unaffected (because of the scroll direction, one presumes.
			// Instead we use the postion of adjacent elements to specify the area.
			containment: [0,
			              0,
			              $("#dragarea").width(),
			              $("#footerdiv").position().top]
	};
	
	// there's the gallery and the trash
    $gGallery = $( "#gallery" );
    $gNewEvent = $( "#newevent" );
    $gSyncStatus = $("#sync_status");
    $gEvents = $('#events');
    $gZoomDialog = $('#zoom_dialog');
    
    
    // Make all datums draggable
    $( "li.socialnetworkdatum").draggable(JQUERY_DATUM_DRAGGABLE_CONFIG);

    // Make all the events (and the trash), be droppable.
    $(".event").each(function() {
    	initEvent($(this));
    });
    
    // Let the gallery be droppable as well, accepting items from any event.
    $gGallery.droppable(JQUERY_DATUM_DROPPABLE_CONFIG);
    
    //$('li.socialnetworkdatum .ui-icon-zoomin').click(datum_zoom);
    $('#im_finished_button').click(im_finished_click);
    
    // Save automatically every so often.
    setInterval(save_ajax,15000);
    updateStateView();
});


// The "I'm finished" button.
function im_finished_click(eventObject) {
	if (gEventSyncState != EVENT_SYNC_STATE_CLEAN) {
		save_ajax();
	}
	
    $.ajax("GroundTruth", {
        cache: false,
        data: { // Must be key-value.
        		finished: true
        	  }, 
        type: 'POST', // Do a HTTP Post.
        dataType: 'text', // Datatype we are expecting back.
        statusCode: {
        	512: function() { // 512 means the state is out of sync - and the page must be refreshed.
        		location.reload(true); // Reload without caching.
        	},
        },
        error: function($data, textStatus, errorThrown) {
        	// Failed.
        	alert("Failed! Maybe try to refresh the page? Still getting problems? please email blamey.ben@gmail.com");
        },
        success: function() {
        	// Redirect to the index of the current directory.
        	window.location.href = ".";
        }
    });
}




/**
 * Properties of $ui:
    draggable - A jQuery object representing the draggable element.
    helper - A jQuery object representing the helper that is being dragged.
    position - Current CSS position of the draggable helper as { top, left } object.
    offset - Current offset position of the draggable helper as { top, left } object.
 */
function droppable_datum_drop($ui_event, $ui) {
	
	$draggable = $ui.draggable; // The datum.
	$target = $ui_event.target; // The drop target.
	
	console.log("draggable:");
	console.log($draggable);
	console.log("draggable parent:");
	console.log($draggable.parent());
	console.log("target:");
	console.log($target);
	
	if ($draggable.parent().is($target)) {
		// Clone is automatically destroyed.
		console.log("Drag Source = Drag Target. Ignoring.");
		return;
	} else if ($draggable.parent().parent().is($target)) {
		// For the events, the target is the DIV, the parent is the UL.
		// Clone is automatically destroyed.
		console.log("Drag Source = Drag Target. Ignoring.");
		return;
	} else if ($gGallery.is($target)) {
		
		var $item = $draggable;
		
		// The item has been dropped into the gallery - save it there.
		$item.fadeOut(function() { // Fade the original to opaque, then do this:

		$item.prependTo( $gGallery ); // Put the image at the start.
		
		$gGallery
			// Scroll the gallery to the top with an animation.
			.animate({scrollTop: 0}, 'slow')
			// The 'complete' function doesn't seem to work,
			// so an alternative method is to obtain a promise object.
			.promise().done(
				function() {
					// Fade in when complete.
					$item.fadeIn(); 
				}
			);
		});
		
	} else if ($gNewEvent.is($target)) {
		
		// Make a new event with the image.
	    var eventName = "Event_"+($("div.event").length + 100000); // Allows alphab ordering, easier than zero padding.

	    // Create the div for the new event.
	    var $eventDiv = $
	    	('<div id='+eventName+' class="event">'
	    		+'<ul></ul>'
	        +'</div>');
	    
	    // Append the div to the DOM.
	    $("#events").append($eventDiv);
	    
	    // Configure the event div.
	    initEvent($eventDiv);
	    
		$gEvents
			// Scroll the events to the bottom with an animation.
			.animate({scrollTop: 99999}, 'slow')
			// The 'complete' function doesn't seem to work,
			// so an alternative method is to obtain a promise object.
			.promise().done(
				function() {
					// Fade in when complete.
					$eventDiv.fadeIn();
					insertDatumIntoEvent($draggable, $eventDiv);
				}
			);
		
	} else {
		insertDatumIntoEvent($draggable, $target);
	}
	
	gEventSyncState = EVENT_SYNC_STATE_DIRTY;
	updateStateView();
}
    
function insertDatumIntoEvent($datumDiv, $eventDiv) {
    $datumDiv.fadeOut(function() {
        var $list = $( "ul", $eventDiv );
	    $datumDiv.appendTo( $list );
	    $datumDiv.fadeIn();
	    $datumDiv.attr("style", ""); // Remove any leftover absolute positioning info. 
	    $datumDiv.fadeIn();
    });        
}

function save_ajax() {
	console.log("save_ajax");
	
	if (gEventSyncState != EVENT_SYNC_STATE_DIRTY) {
		return;
	}
	
    var eventLayout = {};
    eventLayout.lifeStoryFileName = gLifeStoryFileName;
    eventLayout.timestamp = $.now();
    eventLayout.events = [];
    
    $('div.event').each(function($index) {
    	
        var eventInfo = {};
        eventInfo.id = $(this).attr('id');
        
        // Skip the 'newevent'.
        if (eventInfo.id == 'newevent') {
        	return;
        }
        
        eventInfo.datums = [];
        $(this).find('li').each(function($index2) {
            var datumInfo = {};
            
            // As an example, store the title as the metadata.
            datumInfo.title = $(this).find('h5').text();
            datumInfo.id = $(this).attr('data-datumid');
            datumInfo.pseudoEventType = $(this).attr('pseudoEventType'); 
            
            eventInfo.datums.push(datumInfo);
        });
        
        eventLayout.events.push(eventInfo);
    });
    
    //console.log(JSON.stringify(eventLayout, null, '\t'));
    
    gEventSyncState = EVENT_SYNC_STATE_SENT;
    updateStateView();
    
    $.ajax("GroundTruth", {
        cache: false,
        data: { // Must be key-value.
        		events: JSON.stringify(eventLayout),
        		ground_truth_state: gGroundTruthState}, 
        type: 'POST', // Do a HTTP Post.
        dataType: 'text', // Datatype we are expecting back.
        statusCode: {
        	512: function() { // 512 means the state is out of sync - and the page must be refreshed.
        		location.reload(true); // Reload without caching.
        	},
        },
        error: function($data, textStatus, errorThrown) {
        	// Send failed, revert to dirty.
        	gEventSyncState = EVENT_SYNC_STATE_DIRTY;
        	$gSyncStatus.html("SAVE FAILED - ERROR: " + textStatus);
        	if (!gHaveShownSaveErrorMessage) {
        		alert("Save failed! You can only have one window open. Maybe try to refresh the page? Still getting problems? please email blamey.ben@gmail.com");
        		gHaveShownSaveErrorMessage = true;
        	}
        },
        success: function() {
        	// Successfully sent the request.
        	if (gEventSyncState == EVENT_SYNC_STATE_SENT) {
        		gEventSyncState = EVENT_SYNC_STATE_CLEAN;
        	}
        	updateStateView();
        }
    });
}

function updateStateView() {
	if (gEventSyncState == EVENT_SYNC_STATE_CLEAN) {
		$gSyncStatus.html('All changes saved.');
	} else if (gEventSyncState == EVENT_SYNC_STATE_DIRTY) {
		$gSyncStatus.html('Saving...');
	} else if (gEventSyncState == EVENT_SYNC_STATE_SENT) {
		 $gSyncStatus.html('Saving...');
	} else {
		 $gSyncStatus.html('Unknown Status?!');
	}
}

function initEvent($eventDiv) {
	console.log("initEvent() called");
	
    // let the new event be droppable, accepting the gGalleryDiv items
	$eventDiv.droppable(JQUERY_DATUM_DROPPABLE_CONFIG);
}

function eventDiv_blur($eventObject) {
	console.log("eventDiv_blur() called");
	// Read the text and set it back - this has the effect of removing '<br/>' tags.
	gEventSyncState = EVENT_SYNC_STATE_DIRTY;
	updateStateView();
}
