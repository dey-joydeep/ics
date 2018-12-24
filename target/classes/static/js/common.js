const CONTENT_TYPE_JSON = 'application/json; charset=utf-8';
const URL_REGEX = /(?:(?:https?|ftp?):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u{00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?/igm;

const MEDIA_AV = [
// JPEG images
'image/jpeg',
// Portable Network Graphics
'image/png',
// Graphics Interchange Format (GIF)
'image/gif',
// 3GPP video container
'video/3gpp',
// MPEG Video
'video/mpeg',
// AVI: Audio Video Interleave
'video/x-msvideo',
// OGG video
'video/ogg' ]
const MEDIA_DOC = [
// Comma-separated values (CSV)
'text/csv',
// Tab-separated values (TSV)
'text/tsv',
// Microsoft Word
'application/msword',
// Microsoft Word (OpenXML)
'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
// JSON format
'application/json',
// Adobe Portable Document Format (PDF)
'application/pdf',
// Microsoft PowerPoint
'application/vnd.ms-powerpoint',
// Microsoft PowerPoint (OpenXML)
'application/vnd.openxmlformats-officedocument.presentationml.presentation',
// RAR archive
'application/x-rar-compressed',
// Tape Archive (TAR)
'application/x-tar',
// ext, (generally ASCII or ISO 8859-n)
'text/plain',
// application/vnd.ms-excel
'application/vnd.ms-excel',
// Microsoft Excel OpenXML)
'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
// ZIP archive
'application/zip',
// 7-zip archive
'application/x-7z-compressed' ];

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
        return '<a href="' + url + '">' + url + '</a>';
    })
    // or alternatively
    // return text.replace(urlRegex, '<a href="$1">$1</a>')
}

function isPresent(data) {
    return (typeof (data) !== 'undefined' && data !== null);
}

$(function () {
    var name;
    var value;
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