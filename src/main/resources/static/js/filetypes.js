/**
 * Copyright Joydeep Dey 2018-2019
 */
function FileType() {
    this.FILE_TYPES = {
        static_image : [ 'image/jpeg', 'image/png' ],
        dynamic_image : [ 'image/gif' ],
        audio : [ 'audio/basic', 'audio/mid', 'audio/mpeg', 'audio/mp4', 'audio/ogg', 'audio/vnd.wav', 'audio/wav','audio/aac', 'audio/vnd.dlna.adts' ],
        video : [ 'video/mp4', 'video/3gpp', 'video/mpeg', 'video/x-msvideo', 'video/ogg', 'video/quicktime', 'video/x-flv' ],
        text : [ 'text/plain', 'text/csv', 'text/tsv', 'application/json' ],
        pdf : [ 'application/pdf' ],
        octet : [
                'application/msword',
                'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
                'application/vnd.ms-powerpoint',
                'application/vnd.openxmlformats-officedocument.presentationml.presentation',
                'application/vnd.ms-excel',
                'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                'application/x-rar-compressed', 'application/x-tar',
                'application/zip', 'application/x-zip-compressed', 'application/x-7z-compressed' ]
    };
    this.THUMBNAILS = {
        pdf : 'thumb_pdf.png',
        doc : 'thumb_doc.png',
        docx : 'thumb_docx.png',
        xls : 'thumb_xls.png',
        xlsx : 'thumb_xlsx.png',
        ppt : 'thumb_ppt.png',
        pptx : 'thumb_pptx.png',
        rar : 'thumb_rar.png',
        '7z' : 'thumb_7z.png',
        zip: 'thumb_zip.png',
        tar: 'thumb_tar.png',
        audio : 'thumb_audio.png',
        play : 'thumb_play.png',
        other: 'thumb_file.png'
    };
}

var getCheckResult = function (source, targets) {
    for (var i = 0; i < targets.length; i++) {
        if (source === targets[i]){
            console.log(`Detected file type: ${targets[i]}`);
            return true;
        }
    }
    return false;
};

FileType.prototype.getThumbnail = function (ext) {
    return new Promise((resolve, reject) => {
        if(this.THUMBNAILS.hasOwnProperty(ext))
            imgUrl = this.THUMBNAILS[ext];
        else
            imgUrl = this.THUMBNAILS['other'];
            // helper Image object
        const image = new Image();
        image.src = `./images/thumb/${imgUrl}`;
        image.onload = function(img){
            var width = img.srcElement.width;
            var height = img.srcElement.height;
            var canvas = $(`<canvas id="myCanvas" width="${width}" height="${height}"></canvas>`)[0];
            var ctx = canvas.getContext("2d");
            ctx.drawImage(img.srcElement, 0, 0, width, height);
            resolve(canvas.toDataURL()); 
        };
        image.onerror = error => reject(error);
    });
};

FileType.prototype.isStaticImage = function (type) {
    return getCheckResult(type, this.FILE_TYPES.static_image);
};
FileType.prototype.isDynamicImage = function (type) {
    return getCheckResult(type, this.FILE_TYPES.dynamic_image);
};
FileType.prototype.isImage = function (type) {
    return getCheckResult(type, this.FILE_TYPES.static_image) || getCheckResult(type, this.FILE_TYPES.dynamic_image);
};
FileType.prototype.isDynamicImage = function (type) {
    return getCheckResult(type, this.FILE_TYPES.dynamic_image);
};
FileType.prototype.isAudio = function (type) {
    return getCheckResult(type, this.FILE_TYPES.audio);
};
FileType.prototype.isVideo = function (type) {
    return getCheckResult(type, this.FILE_TYPES.video);
};
FileType.prototype.isText = function (type) {
    return getCheckResult(type, this.FILE_TYPES.text);
};
FileType.prototype.isPdf = function (type) {
    return getCheckResult(type, this.FILE_TYPES.pdf);
};
FileType.prototype.isOctet = function (type) {
    return getCheckResult(type, this.FILE_TYPES.octet);
};
