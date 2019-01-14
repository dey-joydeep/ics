const messageCache = {};
$(function () {
    // $(window).bind('beforeunload', function (e) {
    // if (!$('#text-in').is(':empty'))
    // return 'You have an unsent message. Sure to leave?';
    // else
    // return true;
    // });
    $('#logout').click(function () {
        logout();
    });
    $('#username').change(function () {
        openConnection();
    });
    $('#ws-stat').change(function () {
        if (this.value === "1")
            loadUsers();
    });

    loadUsername();

    $('#search-id').on('input', function () {
        searchUser();
    });

    $('#up-file').change(function () {

    });
    $('.av-in').click(function () {
        $('#av-file').attr('accept', MEDIA_AV);
        $('#av-file').click();
    });
    $('.doc-in').click(function () {
        $('#doc-file').attr('accept', MEDIA_DOC);
        $('#doc-file').click();
    });

    $('#text-in').on('input', function () {
        if ($(this).text().length > 0) {
            $('#send-btn').removeAttr('disabled');
        } else {
            $('#send-btn').attr('disabled', 'disabled');
        }
    });

    adjustPanelDisplay();
    $(window).resize(function () {
        adjustPanelDisplay();
    });

    $('#back-to-friend-list').click(function () {
        $('.chat-sub-panel').hide();
        $('#user-panel').show();
    });

    $('#message-out').scroll(function () {
        // TODO: Implement scroll to bottom code.
    });

    var avPreview = new FilePreview($('#av-file'), $('#m-preview-pane'));
    $('#av-file').change(function () {
        avPreview.preview();
    });
    var docPreview = new FilePreview($('#doc-file'), $('#m-preview-pane'));
    $('#doc-file').change(function () {
        docPreview.preview();
    });

    $('#send-btn').click(function (e) {
        sendMessage();
        var currentUser = $('#chat-in-display').val();
        if (messageCache.hasOwnProperty(currentUser))
            delete messageCache[currentUser];
    });

    $('#m-send-btn').click(function () {
        var activePreview;
        if (Object.keys(avPreview.finalFileList).length > 0) {
            activePreview = avPreview;
            fileList = avPreview.finalFileList;
        } else if (Object.keys(docPreview.finalFileList).length > 0) {
            activePreview = docPreview;
        } else {
            return false;
        }
        sendFiles(activePreview.finalFileList);
        activePreview.close();
    });

    $("#text-in").bind("paste", function (e) {
        e.preventDefault();
        var text = (e.originalEvent || e).clipboardData.getData('text/plain');
        if (text.trim() !== '') {
            document.execCommand("insertHTML", false, text);
            $('#text-in').keypress();
        }
    });
    $('#text-in').keypress(function (e) {
        if ($(this).text().trim().length > 0)
            $('#send-btn').removeAttr('disabled');
        else
            $('#send-btn').attr('disabled', 'disabled');
        if (e.keyCode !== 13) {
            sendTypeStatus();
        } else {
            if (!e.shiftKey && $('#enter-send-check').is(':checked')) {
                $('#send-btn').click();
                return false;
            }
        }
    });

    $('#message-out').on('mouseover', '.msg', function () {
        if ($('.delete-marker').length > 0)
            return false;
        $(this).find('.msg-control').show();
    });
    $('#message-out').on('mouseleave', '.msg', function () {
        hideMessageControl(this);

    });
    $('#message-out').on('contextmenu', '.msg', function (e) {
        e.preventDefault();
        if ($('.delete-marker').length > 0)
            return false;
        $(this).find('.msg-control>i').click();
    });
    $('#message-out').on('click', '.msg>.msg-control a', function () {
        var mu = $(this).closest('.msg').find('.msg-div');
        switch (parseInt($(this).attr('data-action'))) {
        case 1:
            showMessageInfo(mu);
            break;
        case 2:
            previewReplyContent(mu);
            break;
        case 3:
            break;
        case 4:
            initDeleteProc(mu);
            break;
        default:
            console.log('Unsupported action selected.');
            break;
        }
    });
    $('#msg-dlt-btn').click(function () {
        execDeleteProc();
    });
    $('#msg-dlt-cancel-btn').click(function () {
        cancelDeleteProc();
    });
    $('#summary-panel>.self-control').click(function () {
        $(this).parent().hide();
        $(this).parent().find('.content-area').empty();
    });

    $('#message-out').on(
            'click',
            '.msg-reply-prev, .msg-reply-prev *',
            function () {
                var msgReplyDiv = $(this).hasClass('msg-reply-prev') ? $(this)
                        : $(this).parent();
                var tgtId = msgReplyDiv.find('.r-msg-id').val();
                var tgtMsg = $(`.msg:has(.msg-ids[value="${tgtId}"])`);
                tgtMsg[0].scrollIntoView();
                tgtMsg.find('.msg-div').addClass("redirect").delay(1500).queue(
                        function () {
                            $(this).removeClass("redirect").dequeue();
                        });
            });
});

function showMessageInfo(mu) {
    $('#summary-panel').show();
    var div = $(MESSAGE_INFO_PROTO);
    var nDiv = div.find('.msg-info-title');
    var cDiv = div.find('.msg-content-div');
    var sDiv = div.find('.delivery-status-div');

    var userUnit = $(`.user-unit:has(.friend-ids[value="${$('#chat-in-display').val()}"])`);

    var sName, rName;
    if (mu.hasClass('msg-self')) {
        sName = 'You';
        rName = userUnit.find('label.fullname').text();
    } else {
        sName = userUnit.find('label.fullname').text();
        rName = 'You';
    }
    nDiv.find('.s-name').text(sName);
    nDiv.find('.r-name').text(rName);
    var imgSrc = mu.find('.attach-content>img').attr('src');
    if (isPresent(imgSrc))
        cDiv.find('.attach-content>.msg-info-attach').attr('src', imgSrc);
    else
        cDiv.find('.attach-content').hide();
    var textContent = mu.find('.text-content').html();
    if (textContent.trim().length > 0)
        cDiv.find('.text-content').html(textContent);
    else
        cDiv.find('.text-content').hide();
    sDiv.find('.sdt').text(mu.find('.ts-div>.s-dt').val());
    sDiv.find('.ddt').text(mu.find('.ts-div>.d-dt').val());
    sDiv.find('.rdt').text(mu.find('.ts-div>.r-dt').val());
    $('#summary-panel>.content-area').html(div);
}

function previewReplyContent(mu) {
    $('#text-in').focus();
    $('#message-in').find('.msg-reply').remove();
    var div = $(MESSAGE_REPLY_PROTO);
    var imgSrc;
    if (mu.find('.attach-content img').length > 0)
        imgSrc = mu.find('.attach-content img').attr('src');
    else if (mu.find('audio').length > 0)
        imgSrc = `./images/thumb/${fileType.THUMBNAILS.audio}`;
    else
        ;
    if (isPresent(imgSrc))
        div.find('.msg-info-attach').attr('src', imgSrc);
    else
        div.find('.msg-info-attach').hide();
    var textContent = mu.find('>.text-content').html();
    if (textContent.trim().length > 0)
        div.find('.text-content').html(textContent);
    else
        div.find('.text-content').hide();
    div.find('.r-msg-id').val(mu.find('.msg-ids').val());
    $('#message-in').prepend(div);
    $('.r-close').click(function () {
        $(this).parent().remove();
    });
}

function hideMessageControl(elem) {
    var msgCtrl = $(elem).find('.msg-control');
    msgCtrl.hide();
    if (msgCtrl.hasClass('show'))
        msgCtrl.click();
}
function initDeleteProc(mu) {
    $('#message-action-area').showFlex();
    var msgDivs = $('#message-out .msg-div');
    var chechbox = '<input type="checkbox" class="delete-marker">';
    hideMessageControl(mu.parent());
    $.each(msgDivs, function () {
        var checkboxElem = $(MESSAGE_DEL_CHECKBOX_PROTO);
        $(this).prepend(checkboxElem);
        checkboxElem.find('i').click(function () {
            if ($(this).text() === 'check_box_outline_blank') {
                $(this).text('check_box');
                $(this).addClass('delete-checked');
                $('#msg-dlt-btn').removeAttr('disabled');
            } else {
                $(this).text('check_box_outline_blank');
                $(this).removeClass('delete-checked');
                var totalDelete = $('.delete-marker>.delete-checked').length;
                if (totalDelete === 0)
                    $('#msg-dlt-btn').attr('disabled', 'disabled');
            }
        });
        var parentMu = $(this).parent();
        if (parentMu.find('.msg-ids').val() === mu.find('.msg-ids').val())
            checkboxElem.find('i').click();
    });
    $('#send-btn').attr('disabled', 'disabled');
    $('#text-in').prop('contenteditable', false);
}

function cancelDeleteProc() {
    $('.delete-marker').remove();
    $('#message-action-area').hide();
    $('#send-btn').removeAttr('disabled');
    $('#text-in').prop('contenteditable', true);
}

function execDeleteProc() {
    var dltMu = $('.delete-marker:has(i.delete-checked)').parent();
    var dltMsgIds = [];
    $.each(dltMu, function () {
        dltMsgIds.push($(this).find('.msg-ids').val());
    });

    if (dltMsgIds.length === 0)
        return;
    var data = {
        username : $('#username').val(),
        messageIds : dltMsgIds
    };
    var jqXhr = $.ajax({
        url : 'chat/delete/units',
        method : 'post',
        data : JSON.stringify(data),
        contentType : CONTENT_TYPE_JSON
    });

    jqXhr.done(function (response) {
        if (response.success) {
            cancelDeleteProc();
            loadUsers();
            $.each(dltMu, function () {
                $(this).parent().remove();
            });
        } else {
            console.log(response.message);
        }
    });
}

function logout() {
    var jqXhr = $.ajax({
        url : 'logout',
        method : 'get',
    });

    jqXhr.done(function () {
        console.log('Logout suucess!');
    });
}

function adjustPanelDisplay() {
    if (isMobile()) {
        $('#user-panel').replaceClass('col', 'col-3');
        $('#search-id').addClass('form-control-sm');
    } else {
        $('#user-panel').replaceClass('col-3', 'col');
        $('#search-id').removeClass('form-control-sm');
    }
}

/**
 * Load the friend/user list associated with the logged in user
 */
function loadUsername() {
    var jqXhr = $.ajax({
        url : 'load/selfdetails',
        method : 'get',
    });

    jqXhr.done(function (data) {
        $('#self-avatar').attr('src', data.avatar);
        $('#self-avatar').attr('alt', data.firstname + ' ' + data.lastname);
        var text = `Welcome ${data.firstname}`
        if (isPresent(data.lastLoginDateTime)
                && data.lastLoginDateTime.length > 0)
            text += `! Your last login: ${data.lastLoginDateTime}`;
        $('#self-summary').text(text);
        $('#firstname').val(data.firstname);
        $('#lastname').val(data.lastname);
        $('#username').val(data.username).trigger('change');
    });
}
function loadUsers() {
    var jqXhr = $.ajax({
        url : 'load/chathistory',
        data : {
            username : $('#username').val()
        },
        method : 'get',
        contentType : CONTENT_TYPE_JSON
    });

    jqXhr.done(function (data) {
        var div = $('#user-list');
        if (data.length > 0) {
            div.empty();
            var receivers = [];
            $('#search-id').removeAttr('disabled');
            $.each(data, function (idx, user) {
                generateUserList(div, user);
                if (user.unreadMessageCount > 0)
                    receivers.push(user.memberId);
            });

            if (receivers.length > 0) {
                sendBulkDeliveredReceipt(receivers);
            }

            $('.user-unit').click(
                    function () {
                        var user = $(this);
                        var selectedUsername = user.find('.friend-ids').val();
                        if (selectedUsername !== '')
                            $('#chat-instruction').remove();
                        if ($('#chat-in-display').val() === selectedUsername)
                            return false;
                        if (isPresent($('#text-in').text().trim()))
                            messageCache[$('#chat-in-display').val()] = $(
                                    '#text-in').html();
                        $('#text-in').empty();
                        if (messageCache.hasOwnProperty(selectedUsername))
                            $('#text-in').html(messageCache[selectedUsername]);
                        $('#chat-in-display').val(selectedUsername).trigger(
                                'change');
                        user.find('.message-summary').removeClass('unread');
                        user.find('.unread-count').empty();
                        openChatPanel(user);
                    });
        }
    });
}

/**
 * Generate list of users info depending
 * 
 * @param mainDiv
 *            DIV, where user list will be added
 * @param user
 *            Details of an user fetched from server
 */
function generateUserList(mainDiv, member) {
    var proto = $(USER_UNIT_PROTO);
    mainDiv.append(proto);

    proto.find('.friend-ids').val(member.memberId);
    var img = proto.find('.avatar');
    img.attr('src', member.avatar);
    img.prop('alt', member.memberName);

    var statusSpan = proto.find('.user-status');
    if (member.online)
        statusSpan.addClass('online');
    else
        statusSpan.addClass('offline');
    proto.find('.last-online').val(member.lastOnlineAt);
    proto.find('.fullname').text(member.memberName);

    var lastMessage = member.lastMessage;
    if (lastMessage !== null) {
        proto.find('.d-none').removeClass('d-none');
        var isSelfSender = (lastMessage.sender !== member.memberId)
        proto.find('.last-message-id').val(lastMessage.messageId);
        proto.find('.last-message-sender').val(lastMessage.sender);
        var sentAt = lastMessage.sentAt;
        sentAt = sentAt.substring(0, sentAt.lastIndexOf(':'))
        proto.find('.message-time').text(sentAt);
        var messageSummaryDiv = proto.find('.message-summary');
        var content = getMessageSummaryContent(lastMessage, isSelfSender);
        messageSummaryDiv.html(content);

        if (member.unreadMessageCount > 0) {
            messageSummaryDiv.addClass('unread');
            proto.find('.unread-count').text(member.unreadMessageCount);
        }

        if (!isSelfSender)
            return;
        // false;
        var status = proto.find('.status-icon');
        switch (lastMessage.messageStatus) {
        case AcknowledgeType.SENT:
            status.text(StatusIconText.SENT);
            break;
        case AcknowledgeType.DELIVERED:
            status.text(StatusIconText.DELIVERED);
            break;
        case AcknowledgeType.READ:
            status.addClass('read');
            status.text(StatusIconText.DELIVERED);
            break;
        default:
            status.addClass('send-failed');
            status.text('error_outline');
            break;
        }
    }

    // return true;
}

/**
 * Open chat panel for selected user passed via parameter as jQuery object.
 * 
 * @param user
 *            Selected user
 */
function openChatPanel(user) {
    $('#curr-size').val(0);
    $('#next-size').val(100);
    $('#msg-panel').show();
    $('#message-out>div.col').empty();
    $('#message-in>.msg-reply').remove();

    if (isMobile())
        $('#user-panel').hide();
    else
        $('#user-panel').show();

    var userDiv = $('#user-info');
    userDiv.html(USER_INFO_PROTO);
    var src, tgt;
    src = user.find('.avatar');
    tgt = userDiv.find('.avatar');
    tgt.attr('src', src.attr('src'));
    tgt.attr('alt', src.attr('alt'));

    src = user.find('.user-status');
    tgt = userDiv.find('.user-status');
    tgt.removeAttr('class');
    tgt.addClass(src.attr('class'));

    src = user.find('.fullname');
    tgt = userDiv.find('.fullname');
    tgt.text(src.text());

    src = user.find('.last-online');
    var lastSeen = src.val();
    tgt = userDiv.find('.last-online');
    if (isPresent(lastSeen) && lastSeen.length > 0) {
        var lastSeenFmt = getLastSeenFormatted(lastSeen);
        tgt.text(lastSeenFmt);
        lastSeen = formatDateTime(new Date(lastSeen));
        if (lastSeenFmt !== lastSeen)
            tgt.attr('title', lastSeen);
    }
    $('#receiver-id').val(user.find('.friend-ids').val());

    loadMessages(1);
}

/**
 * Load message for given user. If called by user selection, only last 100
 * messages will be loaded. If called by scrolling up, next 100 from current
 * will be loaded. Calling from message notification is same as user selection.
 * 
 * @param user
 *            Selected user
 * @param caller
 *            caller identifier {0: scroll, 1: other}
 */
function loadMessages() {
    var jqXhr = $.ajax({
        url : 'load/message',
        data : {
            sender : $('#receiver-id').val(),
            receiver : $('#username').val(),
            currentSize : $('#curr-size').val(),
            nextSize : $('#next-size').val()
        },
        method : 'get',
        contentType : CONTENT_TYPE_JSON
    });

    jqXhr.done(function (chats) {
        var firstUnreadId;
        var messageDiv = $('#message-out>div.col');
        if (chats.length > 0) {
            $.each(chats,
                    function (idx, msg) {
                        if (msg.messageStatus === AcknowledgeType.SENT
                                && !(msg.messageId > firstUnreadId)
                                && msg.sender !== $('#username').val())
                            firstUnreadId = msg.messageId;
                        messageAppender(messageDiv, msg,
                                ChatAppenderCaller.USER_SELECT);
                    });
            console.log(`First unread message ID: ${firstUnreadId}`);
            sendBulkReadReceipt(chats);

            var msgTgt;
            if (isPresent(firstUnreadId))
                msgTgt = $(`.msg:has(.msg-ids[value="${firstUnreadId}"])`);
            else
                msgTgt = $('.msg').last();
            msgTgt[0].scrollIntoView();
            // var images = messageDiv.find('img');
            // if (images.length === 0) {
            // scrollToBottom(messageDiv.parent());
            // return;
            // }

            // var loadCount = 1;
            // $.each(images, function (i) {
            // if (this.complete) {
            // console.log(`${i+1} Image loaded. Src: ${this.src}`);
            // $(this).trigger('load');
            // loadCount++;
            // }
            // $(this).one('load', function () {
            // if (loadCount === images.length) {
            // scrollToBottom(messageDiv.parent());
            // return false;
            // }
            // })
            // });
            // $.each(images, function () {
            //
            // });
        }
    });
}

function searchUser() {
    var input, filter, ul, li;
    input = $('#search-id');
    filter = input.val().toLowerCase();
    ul = $('#user-list>ul');
    li = $('li');
    for (var i = 0; i < li.length; i++) {
        if ($(li[i]).text().toLowerCase().indexOf(filter) > -1)
            $(li[i]).show();
        else
            $(li[i]).hide();
    }
}