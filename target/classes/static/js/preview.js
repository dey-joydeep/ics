'use strict';

function showPreview(field) {

    var files = field.files;
    var mainDiv = $('#av-slide>.carousel-inner');
    mainDiv.empty();
    if (files.length === 1) {
        mainDiv.parent().find('a').hide();
    }
    for (var i = 0; i < files.length; i++) {
        var avFile;
        if (!files[i].type.startsWith('image/'))
            continue;
        var childDiv = $('<div class="carousel-item"></div>');
        if (i === 0)
            childDiv.addClass('active');
        var objectUrl = window.URL.createObjectURL(files[i]);
        avFile = $('<img class="d-block w-100"></img>');
        avFile.attr('src', objectUrl);
        avFile[0].onload = objectUrl;
        mainDiv.append(childDiv);
        childDiv.append(avFile);
        // thumb.text(`File-$(i + 1)`);
        // thumb = $('<span class="thumb"></span>');
    }
    var clearFiles=false;
    var allowClose = false;
    $('#av-send-btn').click(function () {
        allowClose = true;
        $('#av-preview').modal('hide');
    });
    $('#av-preview').modal('show');
    $('#av-preview').on('hide.bs.modal', function (e) {
        if (allowClose) {
            if(clearFiles)
            field.value = '';
            return true;
        }
        e.preventDefault();
        e.stopPropagation();
        $('#user-option').modal();
        $('#user-option').find('button').click(function () {
            allowClose = (this.id === 'confirm-yes');
            if (allowClose)
                $('#av-preview').modal('hide');
            $('#user-option').modal('hide');
        });
    });
}

function showThumbnails(field, thumbnailTarget) {
    thumbnailTarget.empty();
    var files = field[0].files;
    for (var i = 0; i < files.length; i++) {
        var thumb;
        if (files[i].type.startsWith('image/')) {
            thumb = $('<img></img>');
            thumb.attr('src', window.URL.createObjectURL(files[i]));
            thumb.height(60);
            thumb[0].onload = window.URL.revokeObjectURL(files[i]);
        } else {
            thumb = $('<span class="thumb"></span>');
            thumb.text(`File-$(i + 1)`);
        }
        thumbnailTarget.append(thumb);
    }
}