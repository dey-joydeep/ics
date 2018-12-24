const messageCache = {};
$(function () {
    // $(window).bind('beforeunload', function(e) {
    // // save info somewhere
    // console.log(e);
    // return 'are you sure you want to leave?';
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

    $('.av-in').click(function () {
        $('#av-file').attr('accept', MEDIA_AV);
        $('#av-file').click();
    });
    $('.doc-in').click(function () {
        $('#doc-file').attr('accept', MEDIA_DOC);
        $('#doc-file').click();
    });

    $('#text-in').on('input', function () {
        controlChatContent($(this));
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

    $('#av-file').change(function () {
        showPreview(this);
    });

    $('#file-link').click(function () {
        $('#filename').click();
    });

    $('#send-btn').click(function (e) {
        sendMessage();
        var currentUser = $('#chat-in-display').val();
        if (messageCache.hasOwnProperty(currentUser))
            delete messageCache[currentUser];
    });

    $('#av-send-btn').click(function () {
        sendFiles();
    });

    $('#filename').change(function () {
        if (this.files.length > 0)
            $('#send-btn').removeAttr('disabled');
        else
            $('#send-btn').attr('disabled', 'disabled');
    });

    $("#text-in").bind("paste", function (e) {
        e.preventDefault();
        // get text representation of clip board
        var text = (e.originalEvent || e).clipboardData.getData('text/plain');
        if (text.trim() !== '') {
            // insert text manually
            document.execCommand("insertHTML", false, text);
            $('#text-in').keypress();
        }
        // navigator.clipboard.readText()
        // .then(text => {
        // if(text.trim()!=='')
        // $('#text-in').keypress();
        // });
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
});

function logout() {
    var jqXhr = $.ajax({
        url : 'logout',
        method : 'get',
    });

    jqXhr.done(function (result) {
        if (result.success)
            window.location.replace('./');
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
        var text = 'Welcome ' + data.firstname + '! Your last login: '
                + data.lastLoginDateTime;
        $('#self-summary').text(text);
        $('#firstname').val(data.firstname);
        $('#username').val(data.username).trigger('change');
    });
}
function loadUsers() {
    var jqXhr = $.ajax({
        url : 'load/member',
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
            // var isSelfSender;
            $.each(data, function (idx, user) {
                // isSelfSender =
                generateUserList(div, user);
                // if (!isSelfSender)
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
        proto.find('.last-message-id').val(lastMessage.messageId);
        proto.find('.last-message-sender').val(lastMessage.sender);
        var sentAt = lastMessage.sentAt;
        sentAt = sentAt.substring(0, sentAt.lastIndexOf(':'))
        proto.find('.message-time').text(sentAt);
        var messageSummaryDiv = proto.find('.message-summary');
        messageSummaryDiv.html(decodeURIComponent(lastMessage.content));
        if (member.unreadMessageCount > 0) {
            messageSummaryDiv.addClass('unread');
            proto.find('.unread-count').text(member.unreadMessageCount);
        }

        if (lastMessage.sender === member.memberId)
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
        lastSeen = getLastSeenFormatted(lastSeen);
        tgt.text(lastSeen)
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
function loadMessages(caller) {
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
        var messageDiv = $('#message-out>div.col');
        if (chats.length > 0) {
            $.each(chats,
                    function (idx, msg) {
                        messageAppender(messageDiv, msg,
                                ChatAppenderCaller.USER_SELECT);
                    });
            if (caller === 1) {
                scrollToBottom(messageDiv.parent());
            }
            sendBulkReadReceipt(chats)
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
        if ($(li[i]).text().toLowerCase().indexOf(filter) > -1) {
            $(li[i]).show();
        } else {
            $(li[i]).hide();

        }
    }
}

function controlChatContent(elem) {
    if (elem.text().length > 0) {
        $('#send-btn').removeClass('disabled');
    } else {
        $('#send-btn').addClass('disabled');
    }
}