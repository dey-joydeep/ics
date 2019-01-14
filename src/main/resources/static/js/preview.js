/**
 * Copyright Joydeep Dey 2018-2019
 */
function FilePreview(fileSource, targetId) {
	"use strict";
	var preview;
	var isInternalCall = false;
	const MAX_FILE_COUNT = 25;

	this.fileSource = fileSource;
    this.targetId = targetId;
    this.finalFileList = {};
    this.fileType = new FileType();
    
    var createThumbnailPanes = function(files){
        var count = files.length;
        var fileAddThumb = preview.targetId.find('.m-thumbnail .file-add');
        var lastThumb = preview.targetId.find('.m-thumbnail .thumbs.data').last();
        var lastDataId = 0;
        if(lastThumb.length > 0){
            lastDataId = parseInt(lastThumb.attr('data-id')) + 1;
        }
        var proto = `<div class="thumbs data unloaded">
                         <i class="material-icons remove-icon" title="Remove"> remove_circle </i>
                     </div>`;
        for(var i = 0; i < count ; i++){
            var thumb = $(proto);
            thumb.attr('data-id', lastDataId + i);
            thumb.attr('title', files[i].name);
            thumb.insertBefore(fileAddThumb);
        }
        
        setViewControllerStyle(lastDataId + count - 1);
    };

    var renderedFileCount = 0;
    var renderFile = function(renderParams) {
    	renderedFileCount++;
        console.log(renderParams.idx + ': ' + renderParams.fileName);
        var thumb = preview.targetId.find(`.m-thumbnail .thumbs[data-id="${renderParams.idx}"]`);
        // If the thumbnail is removed during async data loading
        if(thumb.length === 0) {
            return false;
        }
        preview.finalFileList[renderParams.idx] = {
          'name' : renderParams.fileName,
          'data' : renderParams.fileDataUrl
        };
        // Manage Thumbnail
        thumb.removeClass('unloaded');
      	var thumbData;
        if(typeof renderParams.thumbDataUrl !== 'undefined'){
            thumbData = renderParams.thumbDataUrl;
        } else {
            thumbData = renderParams.previewDataUrl;
        }
        thumb.css('background-image', `url('${thumbData}')`);
        var lastThumb = preview.targetId.find('.m-thumbnail .thumbs.data').last();
        // Manage preview content
        var pContent = $(`<div class="p-content" data-id="${renderParams.idx}"></div>`);
        var previewTarget = preview.targetId.find('.view-controller.control-left');
        
        if(renderParams.previewDataUrl.startsWith('blob')) {
            var media;
            if(renderParams.fileDataUrl.startsWith('audio/')){
                media = $('<audio />', {
                    src: renderParams.previewDataUrl,
                    controls: true
                })[0];
            } else {
                media = $('<video />', {
                    src: renderParams.previewDataUrl,
                    controls: true
                })[0];
            }
            pContent.html(media);
        } else {
            if(renderParams.showIcon)
                pContent.addClass('type-icon');
            else
                pContent.addClass('type-image');
            pContent.css('background-image', `url('${renderParams.previewDataUrl}')`);
        }
        
        // re-check thumb existence before final appending
        if (preview.targetId.find(`.m-thumbnail .thumbs[data-id="${renderParams.idx}"]`).length === 0)
            return false;
        pContent.insertAfter(previewTarget);
        
        // Manage active thumbnail+preview
        if(parseInt(lastThumb.attr('data-id')) === renderParams.idx){
            thumb.addClass('active-thumb');
            preview.targetId.find('.up-filename').text(renderParams.fileName);
            preview.targetId.find('.active-data-id').val(renderParams.idx);
        } else {
            pContent.hide();
        }
        
        thumb.click(function(e){
            console.log(e);
            if(!$(this).hasClass('active-thumb') && e.currentTarget === e.target)
                changeActive(0, $(this));
        });
        
        if(renderedFileCount === preview.targetId.find('.m-thumbnail .thumbs.data').length)
        	preview.fileSource.val('');
    };

    var pauseMedia = function(parent){
        if(parent.children().length > 0){
            var media = parent.children()[0];
            if(media.currentTime > 0 && (!media.paused || !media.ended)){
                media.pause();
            }
        }
    };

    var changeActive = function(direction, target) {
        var thumbnailCount = preview.targetId.find('.m-thumbnail .thumbs.data').length;
        if(thumbnailCount === 1 && direction !== 0)
            return false;
        
        var nextActiveIdx = 0;
        var activeThumb = preview.targetId.find('.m-thumbnail .thumbs.active-thumb');
        var activeIndex = -1;
        if(activeThumb.length > 0)
            activeIndex = parseInt(activeThumb.attr('data-id'));
        if(direction === 1){
            target = activeThumb.next();
        } else if (direction === -1){
            target = activeThumb.prev();
        } else {
            // do nothing
        }
        
        if(target.length === 0)
            return false;
        nextActiveIdx = parseInt(target.attr('data-id'));

        if(activeIndex > -1) {
            activeThumb.removeClass('active-thumb');
            var currPreview = preview.targetId.find(`.p-content[data-id="${activeIndex}"]`); 
            currPreview.hide();
            pauseMedia(currPreview);
        }
        
        target.addClass('active-thumb');
        preview.targetId.find(`.p-content[data-id="${nextActiveIdx}"]`).show();
        preview.targetId.find('.up-filename').text(preview.finalFileList[nextActiveIdx].name);
        preview.targetId.find('.active-data-id').val(nextActiveIdx);
        setViewControllerStyle(nextActiveIdx);
    };

    var setViewControllerStyle = function(activeIdx){
        var ctrlLft = preview.targetId.find('.view-controller.control-left>button');
        var ctrlRght = preview.targetId.find('.view-controller.control-right>button');
        
        var hasPrevious = (preview.targetId.find(`.thumbs.data[data-id="${activeIdx}"]`).prev().length !== 0);
        var hasNext = (preview.targetId.find(`.thumbs.data[data-id="${activeIdx}"]`).nextUntil('.file-add').length !== 0);
        if(!hasPrevious && !hasNext){
            ctrlLft.hide();
            ctrlRght.hide();
        } else if(!hasPrevious){
            ctrlLft.hide();
            ctrlRght.show();
        } else if(!hasNext) {
            ctrlLft.show();
            ctrlRght.hide();
        } else {
            ctrlLft.show();
            ctrlRght.show();
        }
    };

    var removeFile = function(target) {
        var dataId = $(target).parent().parent().find('.active-data-id').val();
        if(typeof dataId === 'undefined')
            dataId = $(target).parent().attr('data-id');
        dataId = parseInt(dataId);
        // Clear final file list
        delete preview.finalFileList[dataId];
        // Find parent preview and thumbnail and Check other thumbnails
		// availability
        var thumbParent = preview.targetId.find(`.thumbs.data[data-id="${dataId}"]`);
        var previewParent = preview.targetId.find(`.p-content[data-id="${dataId}"]`);
        var prevThumb = thumbParent.prev();
        var nextThumb = thumbParent.nextUntil('.file-add').first();
        var hasPrevious = (prevThumb.length !== 0);
        var hasNext = (nextThumb.length !== 0);
        
        // Execute removal
        thumbParent.remove();
        previewParent.remove();

        if(thumbParent.hasClass('active-thumb'))
            // Execute active change
            if(hasNext){
                changeActive(0, nextThumb);
            } else if(hasPrevious){
                changeActive(0, prevThumb);
            } else {
                preview.close();
            }
        
        // Confirmation, only for testing
        console.log(`Removal target ID: ${dataId}`);
        var keys = '';
        $.each(preview.finalFileList, function(i) {
            keys += (i + ' ');
        });
        console.log(`Remaining keys: ${keys}`);
    };

    var readTextFile = function(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            var pContent = preview.targetId.find('.p-content');
            reader.readAsText(file);
            reader.onload = () => resolve(textToImage(reader.result, pContent.width(), pContent.height()));
            reader.onerror = error => reject(error);
        });
    };

    var getBlobUrl = function (file) {
       return window.URL.createObjectURL(file);
    };

    var bindEvents = function () {
        isInternalCall = true;
        preview.targetId.on('click', '.thumbs.file-add', function(){
            preview.fileSource.click();
        });
        preview.targetId.on('click', '.control-left>*', function(e){
            changeActive(-1);
        });
        preview.targetId.on('click', '.control-right>*', function(e){
            changeActive(1);
        });
        preview.targetId.on('click', '.remove-icon', function(e){
            removeFile(this);
        });
        preview.targetId.on('click' ,'button.close', function(e){
            preview.close();
        });
        preview.targetId.keydown(function(e){
            if(typeof preview.targetId === 'undefined' || preview.targetId.is(':hidden'))
                return true;
            switch(e.which) {
            case 9: // Tab
                return false;
            case 27: // Esc
                preview.targetId.find('button.close').click();
                break;
            case 35: // End
                preview.targetId.find('.thumbs.data').last().click();
                break;
            case 36: // Home
                preview.targetId.find('.thumbs.data').first().click();
                break;
            case 37: // ←
                var lftCtrl = preview.targetId.find('.view-controller.control-left>button');
                if(lftCtrl.is(':visible'))
                    lftCtrl.click();
                break;
            case 39: // →
                var rghtCtrl = preview.targetId.find('.view-controller.control-right>button');
                if(rghtCtrl.is(':visible'))
                    rghtCtrl.click();
                break;
            case 35 : // insert
                preview.targetId.find('.add-file　').click(); 
                break;
            case 46: // delete
                preview.targetId.find('.file-control .remove-icon').click(); 
                break;
            default:
                return true;
            }
            e.preventDefault();
        });
    };

    var unbindEvents = function () {
        isInternalCall = false;
        preview.targetId.off('click', '.thumbs.file-add');
        preview.targetId.off('click', '.control-left>*');
        preview.targetId.off('click', '.control-right>*');
        preview.targetId.off('click', '.remove-icon');
        preview.targetId.off('click' ,'button.close');
        preview.targetId.off('keydown');
    };
    

    FilePreview.prototype.preview = function () {
        preview = this;
        if(!isInternalCall)
            bindEvents();
        var files = preview.fileSource[0].files;
        if(files.length === 0)
            return false;
        if(Object.keys(preview.finalFileList).length + files.length > MAX_FILE_COUNT) {
            alert(`Cannot process more than ${MAX_FILE_COUNT} files at once.`);
            return false;
        }
        preview.targetId.show();
        preview.targetId.focus();
        var renderParams = {};
        var lastThumb = preview.targetId.find('.m-thumbnail .thumbs.data').last();
        var lastIdx = 0;
        if(lastThumb.length > 0)
            lastIdx =  parseInt(lastThumb.attr('data-id')) + 1;
        var currPreview = preview.targetId.find('.p-content').filter(':visible'); 
        currPreview.hide();
        pauseMedia(currPreview);
        preview.targetId.find('.thumbs.active-thumb').removeClass('active-thumb');
        createThumbnailPanes(files); 
        Array.from(files).forEach((file, i) => {
            file.id = lastIdx + i;
          	var previewDataUrl, ext;
            if (preview.fileType.isStaticImage(file.type)) {
                getCompressFile(file).then(function(dataUrl){
                    renderParams = {
                            'idx' : file.id,
                            'fileName' : file.name, 
                            'fileDataUrl' : dataUrl, 
                            'previewDataUrl' : dataUrl,
                            'showIcon' : false
                     };
                    renderFile(renderParams);
                });
            } else if (preview.fileType.isDynamicImage(file.type)) {
                getBase64(file).then(function(dataUrl){
                    renderParams = {
                            'idx' : file.id,
                            'fileName' : file.name, 
                            'fileDataUrl' : dataUrl, 
                            'previewDataUrl' : dataUrl,
                            'showIcon' : false
                     };
                    renderFile(renderParams);
                });
            } else if (preview.fileType.isAudio(file.type)) {
                previewDataUrl = getBlobUrl(file);
                preview.fileType.getThumbnail('audio').then(function(thumbDataUrl){
                    getBase64(file).then(function(fileDataUrl){
                        renderParams = {
                                'idx' : file.id,
                                'fileName' : file.name, 
                                'fileDataUrl' : fileDataUrl,
                                'previewDataUrl' : previewDataUrl,
                                'thumbDataUrl' : thumbDataUrl,
                                'showIcon' : false
                         };
                        renderFile(renderParams);
                    });
                });
            } else if (preview.fileType.isVideo(file.type)) {
                previewDataUrl = getBlobUrl(file);
                var w = preview.targetId.find('.thumbs').width();
                var h = preview.targetId.find('.thumbs').height();
                videoToImage(previewDataUrl, file.type, w, h).then(function(thumbDataUrl){
                    getBase64(file).then(function(fileDataUrl){
                        renderParams = {
                                'idx' : file.id,
                                'fileName' : file.name, 
                                'fileDataUrl' : fileDataUrl,
                                'previewDataUrl' : previewDataUrl,
                                'thumbDataUrl' : thumbDataUrl,
                                'showIcon' : false
                         };
                        renderFile(renderParams);
                    });
                });
            } else if (preview.fileType.isText(file.type)) {
                readTextFile(file).then(function(previewDataUrl){
                    getBase64(file).then(function(fileDataUrl){
                        renderParams = {
                                'idx' : file.id,
                                'fileName' : file.name, 
                                'fileDataUrl' : fileDataUrl, 
                                'previewDataUrl' : previewDataUrl,
                                'showIcon' : false
                         };
                        renderFile(renderParams);
                    });
                });
            } else if (preview.fileType.isPdf(file.type)) {
                ext = file.name.substring(file.name.lastIndexOf('.') + 1);
                preview.fileType.getThumbnail(ext).then(function(previewDataUrl){
                    getBase64(file).then(function(fileDataUrl){
                        renderParams = {
                                'idx' : file.id,
                                'fileName' : file.name, 
                                'fileDataUrl' : fileDataUrl, 
                                'previewDataUrl' : previewDataUrl,
                                'showIcon' : true
                         };
                        renderFile(renderParams);
                    });
                });
            } else if (preview.fileType.isOctet(file.type)) {
                ext = file.name.substring(file.name.lastIndexOf('.') + 1);
                this.fileType.getThumbnail(ext).then(function(previewDataUrl){
                    getBase64(file).then(function(fileDataUrl){
                        renderParams = {
                                'idx' : file.id,
                                'fileName' : file.name, 
                                'fileDataUrl' : fileDataUrl, 
                                'previewDataUrl' : previewDataUrl,
                                'showIcon' : true
                         };
                        renderFile(renderParams);
                    });
                });
            } else {
                alert('Unsupported file');
                preview.close();
                return false;
            }
        });
    };

    FilePreview.prototype.close = function(){
        unbindEvents();
        preview.targetId.hide();
        preview.finalFileList = {};
        preview.targetId.find('.p-content').remove();
        preview.targetId.find('.thumbs.data').remove();
        preview.targetId.find('.up-filename').empty();
        preview.targetId.find('.active-data-id').val('');
    };
}