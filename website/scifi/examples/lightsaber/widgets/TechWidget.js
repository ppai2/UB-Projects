
(function ($) {

AjaxSolr.TechWidget = AjaxSolr.AbstractWidget.extend({
  start: 0,

  beforeRequest: function () {
    $(this.target).html($('<img>').attr('src', 'images/ajax-loader.gif'));
  },

  facetLinks: function (facet_field, facet_values) {
    var links = [];
    if (facet_values) {
      for (var i = 0, l = facet_values.length; i < l; i++) {
        if (facet_values[i] !== undefined) {
          links.push(
            $('<a href="#"></a>')
            .text(facet_values[i])
            .click(this.facetHandler(facet_field, facet_values[i]))
          );
        }
        else {
          links.push('no items found in current selection');
        }
      }
    }
    return links;
  },

  facetHandler: function (facet_field, facet_value) {
    var self = this;
    return function () {
      self.manager.store.remove('fq');
      self.manager.store.addByValue('fq', facet_field + ':' + AjaxSolr.Parameter.escapeValue(facet_value));
      self.doRequest();
      return false;
    };
  },

  afterRequest: function () {
    $(this.target).empty();
	
    for (var i = 0; i < this.manager.response.response.docs.length; i++) {
      var doc = this.manager.response.response.docs[i];
	  if (doc.title != undefined && doc.title != null) {
		  $(this.target).append(this.template(doc));
		  
		  var items = [];
		  items = items.concat(this.facetLinks('topics', doc.tag));
		  //items = items.concat(this.facetLinks('organisations', doc.organisations));
		  //items = items.concat(this.facetLinks('exchanges', doc.exchanges));

		  var $links = $('#links_' + doc.id);
		  $links.empty();
		  for (var j = 0, m = items.length; j < m; j++) {
			$links.append($('<li></li>').append(items[j]));
		  }
	  }
    }
	$(this.target).append('<div><p class="poweredBy">powered by <a class="bloglink" href="http://www.technovelgy.com/" target="_blank"> technovelgy.com</a></p></div></br>');
	first = false;
  },

  template: function (doc) {
    var snippet = '';
    if (doc.body.length > 100) {
      snippet += doc.body.substring(0, 100);
      snippet += '<span style="display:none;">' + doc.body.substring(100);
      snippet += '</span> <a href="#" class="more">more</a>';
    }
    else {
      snippet += doc.body;
    }

    //var output = '<div id="title_'+doc.id+'"  onclick="opendoc('+doc.id+')"><h2>' + doc.title + '</h2>';
	var output = '<div id="title_'+doc.id+'" class="doctitle"><p class="relatedDocTitle"><b>' + doc.title + '</b></p>';
    output += '<p>' + snippet + '</p></div>';
	
    return output;
  },

  init: function () {
    $(document).on('click', 'a.more', function () {
      var $this = $(this),
          span = $this.parent().find('span');

      if (span.is(':visible')) {
        span.hide();
        $this.text('more');
      }
      else {
        span.show();
        $this.text('less');
      }

      return false;
    });
  }
});

})(jQuery);