
(function ($) {

AjaxSolr.TextWidget = AjaxSolr.AbstractTextWidget.extend({
  init: function () {
    var self = this;
	var userQuery = '';
	
    $(this.target).find('input').bind('keydown', function(e) {
      if (e.which == 13) {
	    $('#left div').empty();
		var left = document.getElementById('left');
		while (left.hasChildNodes()) {
			left.removeChild(left.lastChild);
		}
		
		$('#result div').empty();
		$('#result div').innerHTML = '';
		$('#search_results div').empty();
		$('#search_results div').innerHTML = '';
	    var selectedTag = getTag();
        var value = $(this).val();
		value = value.trim();
		value = value.replace(/\s+/g, ' title:');
		value = 'title:' + value;
		if (selectedTag != 'all'){
		  //value = value + ' & fq=tag:' + selectedTag;
		  //self.store.addByValue('fq',selectedTag);
		 // self.setFQ(selectedTag);
		 self.manager.store.remove('fq');
		 self.manager.store.addByValue('fq', 'tag:'+selectedTag);
		}
		else{
			self.manager.store.remove('fq');
		}
		console.log('query:'+value);
		//console.log('fq:'+this.manager.store.values('fq'));
		self.manager.store.remove('q');
        self.manager.store.addByValue('q', value);
		self.doRequest();
		/*if (value && self.set(value)) {
          self.doRequest();
        }*/
      }
    });
	
	$('#searchButton').click(function () {
		$('#left div').empty();
		var left = document.getElementById('left');
		while (left.hasChildNodes()) {
			left.removeChild(left.lastChild);
		}
		$('#result div').empty();
		$('#result div').innerHTML = '';
		$('#search_results div').empty();
		$('#search_results div').innerHTML = '';
		
		userQuery = $('#query').val();
	    var selectedTag = getTag();
	    userQuery = userQuery.trim();
	    userQuery = userQuery.replace(/\s+/g, ' title:');
	    userQuery = 'title:' + userQuery;
	    if (selectedTag != 'all'){
	      //value = value + ' & fq=tag:' + selectedTag;
		  //self.store.addByValue('fq',selectedTag);
		 // self.setFQ(selectedTag);
		 self.manager.store.remove('fq');
		 self.manager.store.addByValue('fq', 'tag:'+selectedTag);
		 // userQuery = userQuery + ' tag:' + selectedTag;
	    }
		self.manager.store.remove('q');
        self.manager.store.addByValue('q', userQuery);
		self.doRequest();
        /* if (userQuery && self.set(userQuery)) {
          self.doRequest();
        } */
	});
	
	getTag = function () {
	  var radios = document.getElementsByName('tag');
      var tag = '';
      for (var k=0; k < radios.length; k++) {
        if (radios[k].checked) {
	      tag = radios[k].id;
	      break;
	    }
      }
	  return tag;
	}
	
  },

  afterRequest: function () {
    $(this.target).find('input').val('');	
  }
  
});

})(jQuery);
