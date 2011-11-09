function Location() {
    this.lat = 0;
    this.lon = 0;

    this.update = function(lat, lon) {
        this.lat = lat;
        this.lon = lon;
        this.onUpdate(lat, lon);
    };

    this.onUpdate = function(lat, lon) {
    };
}
var loc = new Location();

$(document).ready(function() {
    var geo = null;
    if (navigator.geolocation)
        geo = navigator.geolocation;

    navigator.geolocation.getCurrentPosition(
        function (position) {
            loc.update(position.coords.latitude,position.coords.longitude)
        },
        function (error) {
            switch(error.code) {
                case error.TIMEOUT:
					$("#content").innerHTML = "Sorry, timed out while getting location.";
					break;
				case error.POSITION_UNAVAILABLE:
					$("#content").innerHTML = "Sorry, your browser is unable to locate you.";
					break;
				case error.UNKNOWN_ERROR:
                    $("#content").innerHTML = "Sorry, your browser is unable to locate you.";
					break;
			}
        }
    )

});