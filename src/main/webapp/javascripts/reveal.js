(function($) {
    $('a[reveal]').live('click', function(e) {
        e.preventDefault();
        var modalLocation = $(this).attr('reveal');
        $('#' + modalLocation).slideToggle();
    });
})(jQuery);