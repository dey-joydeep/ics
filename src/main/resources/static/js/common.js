String.prototype.replaceAll = function (search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
}

String.prototype.replaceLast = function (search, replacement) {
    var target = this;
    return target.replace(new RegExp(search + '$'), replacement);
}

const isMobile = function () {
    return window.matchMedia('(max-width:576px)').matches;
}

/**
 * Check if media screen is 1200px or larger
 * 
 * @returns checked value
 */
const is1200EL = function () {
    return window.matchMedia('(min-width: 1200px)').matches;
}

$.fn.replaceClass = function (newClass, oldClass) {
    $(this).removeClass(oldClass).addClass(newClass);
};

var convertToJsonString = function (formArray) {
    var data = '{';
    for (var i = 0; i < formArray.length; i++) {
        data += (`"${formArray[i]['name']}":"${formArray[i]['value']}"`);
        if (i < formArray.length - 1)
            data += ',';
    }
    data += '}';
    return data;
}

$.fn.showFlex = function () {
    $(this).css('display', 'flex');
}

var evaluateValidationError = function (inputParent, errors) {
    var inputs = $(inputParent).find('input');
    if (inputs.length === 0)
        return;
    var errorField = '<span class="field-error error"></span>';
    $.each(errors, function (f, m) {
        for (var i = 0; i < inputs.length; i++) {
            if ($(inputs[i]).attr('name') === f) {
                var errorSpan = $(errorField);
                errorSpan.text(m);
                errorSpan.insertAfter($(inputs[i]).parent()
                        .find('.field-label'));
            }
        }
    });
}
var urlify = function (text) {
    return text.replace(URL_REGEX, function (url) {
        return `<a href="${url}" target="_blank">${url}</a>`;
    })
}

function isPresent(data) {
    return (typeof (data) !== 'undefined' && data !== null);
}

function isAppCompatible() {
    return ('SharedWorker' in window) && ('WebSocket' in window)
            && ('TextEncoder' in window);
}

function redirect() {
    window.location.replace('./');
}

$(function () {
    var name, value;
    var requireUpdate = false;
    var cookies = document.cookie;
    if (cookies !== null)
        for (var i = 0; i < cookies.length; i++) {
            name = cookies[i].split('=')[0];
            if (name === 'ts')
                value = cookies[i].split('=')[1];
            break;
        }
    else {
        requireUpdate = true;
    }

    var currentTz = Intl.DateTimeFormat().resolvedOptions().timeZone;
    requireUpdate = (value !== currentTz);

    var d = new Date();
    d.setTime(d.getTime() + (365 * 10 * 24 * 60 * 60 * 1000));
    var expires = `expires=${d.toUTCString()}`;
    if (requireUpdate) {
        document.cookie = `ts=${currentTz};${expires};path=/`;
    }
    document.cookie = `lang=${(navigator.language || navigator.userLanguage)};${expires};path=/`;
});
