// jQuery script for search request with server
jQuery(document).ready(function($) {

	// Run when Search button is clicked
	$('#search_button').click(function(){
		
		// Display a progress indicator
		$('#search_results').html('<img src="ajax_loader.gif"> Searching Twitter...');
		
		// Get the value of the input field
		// Encode it for use in a URL
		var search_value = encodeURIComponent($('input[name=search_terms]').val());
		
		// Send the search terms to the server in an Ajax request
		// This URL is for illustration only
		// You MUST change it to your server
		$.ajax({
			url: 'http://localhost/scifi/examples/lightsaber/search_server.php?q=' + search_value,
			success: function(data){
				
				// Display the results
				$('#search_results').html(data);
			}
		})
	})
});