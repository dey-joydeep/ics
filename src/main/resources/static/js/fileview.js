/**
 * Copyright Joydeep Dey 2018-2019
 */
function FileViewer(targetId, viewTarget) {
	'use strict';
	var fileview;
    this.targetId = targetId;
    this.viewTarget = viewTarget;
    
    var pauseMedia = function(parent) {
    	var media = parent.children()[0];
    	var tagName = $(media).prop('tagName').toLowerCase();
    	if(!(tagName === 'audio' || tagName === 'video'))
    		return false;
    	if(media.currentTime > 0 && (!media.paused || !media.ended))
    	    media.pause();
    };

    var changeActive = function(direction) {
    	var activeView = fileview.targetId.find('.p-content').filter(':visible');
        var target = direction === 1 ? activeView.next() : activeView.prev();
        
        if(target.length === 0)
            return false;
        
        activeView.hide();
        target.show();
        pauseMedia(activeView);
        var src, fname = target.attr('data-filename');
        var childElem = $(target.children()[0]);
        var tagName = childElem.prop('tagName').toLowerCase();
        if(tagName === 'object')
        	src = childElem.attr('data');
        else
        	src = childElem.attr('src');
        fileview.targetId.find('.up-filename').text(fname);
        fileview.targetId.find('.fileview-d-link').attr('href', src);
        fileview.targetId.find('.fileview-d-link').attr('download', fname);
        
        setViewControllerStyle();
    };

    var setViewControllerStyle = function() {
        var ctrlLft = fileview.targetId.find('.view-controller.control-left>button');
        var ctrlRght = fileview.targetId.find('.view-controller.control-right>button');
        
        var activeView = fileview.targetId.find('.p-content').filter(':visible');
        var hasPrevious = (activeView.prevUntil('.view-controller').length !== 0);
        var hasNext = (activeView.nextUntil('.view-controller').length !== 0);
        
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

    var bindEvents = function() {
        fileview.targetId.on('click', '.control-left>*', function(e){
            changeActive(-1);
        });
        fileview.targetId.on('click', '.control-right>*', function(e){
            changeActive(1);
        });
        fileview.targetId.on('click' ,'button.close', function(e){
            fileview.close();
        });
        fileview.targetId.keydown(function(e){
            if(typeof fileview.targetId === 'undefined' || fileview.targetId.is(':hidden'))
                return true;
            switch(e.which) {
            case 9: // Tab
                return false;
            case 27: // Esc
                fileview.targetId.find('button.close').click();
                break;
            case 37: // ←
                var lftCtrl = fileview.targetId.find('.view-controller.control-left>button');
                if(lftCtrl.is(':visible'))
                    lftCtrl.click();
                break;
            case 39: // →
                var rghtCtrl = fileview.targetId.find('.view-controller.control-right>button');
                if(rghtCtrl.is(':visible'))
                    rghtCtrl.click();
                break;
            default:
                return true;
            }
            e.preventDefault();
        });
    };

    var unbindEvents = function () {
        fileview.targetId.off('click', '.control-left>*');
        fileview.targetId.off('click', '.control-right>*');
        fileview.targetId.off('click' ,'button.close');
        fileview.targetId.off('keydown');
    };

    FileViewer.prototype.fileView = function () {
        fileview = this;
        if(fileview.viewTarget.length === 0){
        	console.log('No item found to view');
        	return false;
        }
        bindEvents();
        fileview.targetId.show();
        fileview.targetId.focus();
        var pContent;
        var previewTarget = fileview.targetId.find('.view-controller.control-right');
        fileview.viewTarget.forEach((media, i) => {
        	var content;
            if (MediaType.IMAGE === media.type) {
            	content = `<img src="${media.src}" class="content-image">`;
            } else if (MediaType.VIDEO === media.type) {
            	content = `<video src="${media.src}" controls="controls" class="content-video">`;
            } else if (MediaType.TEXT === media.type) {
            	content = `<object data="${media.src}" type="text/plain" class="content-object">`;
            } else if (MediaType.PDF === media.type) {
            	content = `<object data="${media.src}" type="application/pdf" class="content-object">`;
            } else {
                console.log(`Unsupported media: ${media}`);
            }
            pContent = $(`<div class="p-content" data-filename="${media.name}" data-mediatype="${media.type}">${content}</div>`);
            pContent.insertBefore(previewTarget);

            if(!media.isActive) {
            	pContent.hide();
            } else {
	            fileview.targetId.find('.file-control>.fileview-d-link').attr('href', media.src);
	            fileview.targetId.find('.file-control>.fileview-d-link').attr('download', media.name);
            	fileview.targetId.find('.file-control>.fileview-d-link>.up-filename').text(media.name);
            }
        });
        setViewControllerStyle();
    };

    FileViewer.prototype.close = function(){
        unbindEvents();
        fileview.targetId.hide();
        fileview.targetId.find('.p-content').remove();
        fileview.targetId.find('.up-filename').empty();
        fileview.targetId.find('.fileview-d-link').removeAttr('href');
        fileview.targetId.find('.fileview-d-link').removeAttr('download');
    };
}
