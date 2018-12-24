$(function () {
    if (isMobile())
        $('#login-btn').parent().replaceClass('col', 'col-4');

    $('input').keypress(function (e) {
        if (e.which == 13) {
            $('#login-btn').click();
        }
    });

    $("#login-btn").click(function (e) {
        $('#msg-div').empty();
        $('#msg-div').hide();
        $('.field-error.error').remove();
        var form = $('#login-form');
        var sendData = convertToJsonString(form.serializeArray());
        var formURL = form.attr("action");
        var type = form.attr("method");
        $body = $("body");

        $(document).on({
            ajaxStart : function () {
                $body.addClass("loading");
            },
            ajaxStop : function () {
                $body.removeClass("loading");
            }
        });

        $.ajax({
            url : formURL,
            type : type,
            data : sendData,
            contentType : CONTENT_TYPE_JSON,
            success : function (response) {
                if (response.success) {
                    window.location.replace('./');
                }

                var vErrors = response.errors;
                if (vErrors !== null) {
                    evaluateValidationError('#login-form', vErrors);
                } else {
                    $('#msg-div').text(response.message);
                    $('#msg-div').show();
                }
            },
            error : function (jqXHR, textStatus, errorThrown) {
                var response = eval("(" + jqXHR.responseText + ")");
                if (typeof response === 'undefined')
                    $('#msg-div').text('Server not running');
                else
                    $('#msg-div').text(response.message);
                $('#msg-div').show();
            }
        });
        e.preventDefault();
    });
});

function redirect() {
    window.location.replace('./');
}