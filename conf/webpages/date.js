
  function writeDateEN() {
    dayNames = new Array("Sun", "Mon", "Tue","Wed","Thu", "Fri","Sat");
    monthNames = new Array("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec");
	now = new Date();	
    var mday = now.getDate();		//1-31
    var wday = now.getDay(); 		//0-6
    var month = now.getMonth();    	//1-12
    var year = now.getYear();		// after 1900 for NN, after 0 for IE
    if (year < 1000) year += 1900;	// NN year adgustment
    document.write( dayNames[wday] + ", " + mday + "-" + monthNames[month] + "-" + year);
  }
	
  function writeDateGR() {
    dayNames = new Array("Κυρ", "Δευ", "Τρί","Τετ","Πέμ", "Παρ","Σάβ");
    monthNames = new Array("Ιαν","Φεβ","Μαρ","Απρ","Μαϊ","Ιουν","Ιουλ","Αυγ","Σεπ","Οκτ","Νοε","Δεκ");
	now = new Date();	
    var mday = now.getDate();		//1-31
    var wday = now.getDay(); 		//0-6
    var month = now.getMonth();    	//1-12
    var year = now.getYear();		// after 1900 for NN, after 0 for IE
    if (year < 1000) year += 1900;	// NN year adgustment
    document.write( dayNames[wday] + ", " + mday + "-" + monthNames[month] + "-" + year);
  }
