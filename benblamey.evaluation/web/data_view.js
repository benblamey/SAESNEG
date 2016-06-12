var $gDialogContainer;

jQuery(document).ready(function($) {
	$gDialogContainer = $('#dialog_container');
    $('li.socialnetworkdatum .ui-icon-zoomin').click(datum_zoom);
});

// Used by more than one page.


//Click handler for the datum zoom button.
function datum_zoom(eventObject) {
	// eventObject, normalized proroperties: target, relatedTarget, pageX, pageY, which, metaKey
	console.log(eventObject);
	
	// Find the parent datum. 
	var $socialNetworkDatum = $(eventObject.target).parents('li.socialnetworkdatum');
	
	// Create a clone of the div, and remove the ID attribute.
	
	var $socialNetworkDatumForDialog = $socialNetworkDatum.clone(); 
	$socialNetworkDatumForDialog.attr('data-datumid-clone', 
		$socialNetworkDatumForDialog.attr('data-datumid'));
	$socialNetworkDatumForDialog.attr('data-datumid', null);
	
	fullSizeImageURL = $socialNetworkDatumForDialog.attr('full-size-image');
	if (typeof fullSizeImageURL != "undefined") {
		$('img',$socialNetworkDatumForDialog).attr("src", fullSizeImageURL);
	}
	
	$socialNetworkDatumForDialog.dialog({
		modal: true, // Whilst the dialog is open, other items on the page will be disabled.
		appendTo: $gDialogContainer, // The container has the theme class set - so that themeing is enabled on the children.
		height: 650,
		width: 700,
		title: $('h5',$socialNetworkDatum).text(),
	});
}