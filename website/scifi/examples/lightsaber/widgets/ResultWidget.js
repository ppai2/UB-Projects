var docarray;
//var first = true;
var oldresult;

(function ($) {

AjaxSolr.ResultWidget = AjaxSolr.AbstractWidget.extend({
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
	//if (first == true) {
	  docarray = new Array(this.manager.response.response.docs.length);
	//}
    for (var i = 0, l = this.manager.response.response.docs.length; i < l; i++) {
      var doc = this.manager.response.response.docs[i];
	  //if (first == true) {
	    docarray[i] = doc;
	  //}
	  
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
	//first = false;
  },

  template: function (doc) {
    var snippet = '';
    if (doc.body.length > 300) {
      snippet += doc.body.substring(0, 300);
      snippet += '<span style="display:none;">' + doc.body.substring(300);
      snippet += '</span> <a href="#" class="more">more</a>';
    }
    else {
      snippet += doc.body;
    }

    var output = '<div class="singleDoc" id="title_'+doc.id+'" onclick="opendoc('+doc.id+')"><h2><p class="capitalize">' + doc.title + '</p></h2>';
	//output += '<p class="label label-default">Category: '+doc.tag+'</p>';
	output += '<p class="tag">Category: '+doc.tag+'</p>';
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

opendoc = function (docid) {
    oldresult = document.getElementById('right').innerHTML;
	$('#result div').empty();
	$('.left').empty();
	var doc;
	for (var d=0; d < docarray.length; d++) {
	  if (docarray[d].id == docid) {
	    doc = docarray[d];
	  }
	}
	populateLeft(doc);
	var newdiv = document.createElement('div');
	var newnav = document.createElement('div');
	var back = '<button id="back" class="btn btn-default" onclick="goback();">Back</button>';
	var output = '<div id="docOutput"><h2><p class="capitalize">' + doc.title + '</p></h2>';
	output += '<p class="tag">Category: '+doc.tag+'</p>';
	output += '<p>' + doc.body + '</p></div>';
	newdiv.innerHTML = output;
	newnav.innerHTML = back;
	document.getElementById('result').appendChild(newnav);
	document.getElementById('result').appendChild(newdiv);
}

goback = function () {
  $('#left div').empty();
  $('#right div').empty();
  $('#search_results div').empty();
  $('#search_results div').innerHTML = '';
  var result = document.getElementById('left');
  var right = document.getElementById('right');
  //result.removeChild(result.childNodes[0]);
  while (result.hasChildNodes()) {
    result.removeChild(result.lastChild);
  }
  var nou = document.createElement('div');
  nou.innerHTML = oldresult;
  right.appendChild(nou);
}

populateLeft = function (doc) {
  var Manager;
  var BlogManager;
  var relatedTech;
  var relatedBlog;
  var hasTech = false;
  var hasBlog = false;
  if (doc.hasOwnProperty('relatedTech')) {
    relatedTech = doc.relatedTech;
  }
  if (doc.hasOwnProperty('relatedBlog')) {
    relatedBlog = doc.relatedBlog;
  }
  var techquery = '';
  var blogquery = '';
  var left = document.getElementById('left');
  var ltech = document.createElement('div');
  var techheader = document.createElement('div');
  var lblog = document.createElement('div');
  var blogheader = document.createElement('div');
  var tweets = document.createElement('div');
  tweets.id = 'search_results';
  //var divider = document.createElement('div');
  ltech.id = 'ltech';
  lblog.id = 'lblog';
  var ltechtext = '';
  var lblogtext = '';
  var dvd = '<div id="divider">----------------------------------------------------------------------------</div>';
  var theader = '<div id="techheader" style="text-align:center;"><h2><p class="capitalize">Inspired Technologies</p></h2></div>';
  var bheader = '<div id="blogheader" style="text-align:center;"><h2><p class="capitalize">Related Blogs</p></h2></div>';
  techheader.innerHTML = theader;
  blogheader.innerHTML = bheader;
  //divider.innerHTML = dvd;
  if (relatedTech != undefined && relatedTech.hasOwnProperty('length')) {
    if (relatedTech.length > 0) {
	  hasTech = true;
	  for (var i=0; i < relatedTech.length; i++) {
        techquery += 'id:' + relatedTech[i] + ' ';
      }  
	}
  }
  if (relatedBlog != undefined && relatedBlog.hasOwnProperty('length')) {
    if (relatedBlog.length > 0) {
	  hasBlog = true;
	  for (var i=0; i < relatedBlog.length; i++) {
        blogquery += 'id:' + relatedBlog[i] + ' ';
      }
	}
  }
  
  if (hasTech) {
    Manager = new AjaxSolr.Manager({
      solrUrl: 'http://localhost:8983/solr/core1/'
    });
    Manager.addWidget(new AjaxSolr.TechWidget({
      id: 'result',
      target: '#ltech'
    }));
    Manager.init();
    Manager.store.addByValue('q', techquery);
    Manager.doRequest();
  } else {
    ltechtext = '<div>No related tech articles found</div>';
	ltech.innerHTML = ltechtext;
  }
  
  if (hasBlog) {
    BlogManager = new AjaxSolr.Manager({
      solrUrl: 'http://localhost:8983/solr/core2/'
    });
    BlogManager.addWidget(new AjaxSolr.BlogWidget({
      id: 'result',
      target: '#lblog'
    }));
    BlogManager.init();
    BlogManager.store.addByValue('q', blogquery);
    BlogManager.doRequest();
  } else {
    lblogtext = '<div>No related blogs found</div>';
	lblog.innerHTML = lblogtext;
  }
  
  $('#search_results').html('<img src="ajax_loader.gif"> Searching Twitter...');
  // Get the value of the input field
  // Encode it for use in a URL
  var search_value = doc.title;
  
  // Send the search terms to the server in an Ajax request
  // This URL is for illustration only
  // You MUST change it to your server
  $.ajax({
    url: 'http://localhost/scifi/examples/lightsaber/search_server.php?q=' + search_value,
	success: function(data){
	  // Display the results
	  data = '<div id="tweetsheader" style="text-align:center;"><h2><p class="capitalize">Related Tweets</p></h2></div><br />' + data;
	  $('#search_results').html(data);
	}
  });

  left.appendChild(techheader);
  left.appendChild(ltech);
  //left.appendChild(divider);
  left.appendChild(blogheader);
  left.appendChild(lblog);
  left.appendChild(tweets);
	
}

clearInput = function () {
  var all = document.getElementById('all');
  var author = document.getElementById('Author');
  var film = document.getElementById('Film');
  var character = document.getElementById('Character');
  var theme = document.getElementById('Theme');
  
  all.checked = true;
  author.checked = false;
  film.checked = false;
  character.checked = false;
  theme.checked = false;
  
  document.getElementById('query').value = '';
}
