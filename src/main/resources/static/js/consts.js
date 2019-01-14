const CONTENT_TYPE_JSON = 'application/json; charset=utf-8';
const URL_REGEX = /(?:(?:https?|ftp?):\/\/)(?:\S+(?::\S*)?@)?(?:(?!10(?:\.\d{1,3}){3})(?!127(?:\.\d{1,3}){3})(?!169\.254(?:\.\d{1,3}){2})(?!192\.168(?:\.\d{1,3}){2})(?!172\.(?:1[6-9]|2\d|3[0-1])(?:\.\d{1,3}){2})(?:[1-9]\d?|1\d\d|2[01]\d|22[0-3])(?:\.(?:1?\d{1,2}|2[0-4]\d|25[0-5])){2}(?:\.(?:[1-9]\d?|1\d\d|2[0-4]\d|25[0-4]))|(?:(?:[a-z\u{00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)(?:\.(?:[a-z\u00a1-\uffff0-9]+-?)*[a-z\u00a1-\uffff0-9]+)*(?:\.(?:[a-z\u00a1-\uffff}]{2,})))(?::\d{2,5})?(?:\/[^\s]*)?/igm;
const fileType = new FileType();
const MEDIA_AV = `${fileType.FILE_TYPES.static_image
					.concat(fileType.FILE_TYPES.dynamic_image)
					.concat(fileType.FILE_TYPES.audio)
					.concat(fileType.FILE_TYPES.video)}`;
const MEDIA_DOC = `${fileType.FILE_TYPES.text
					.concat(fileType.FILE_TYPES.pdf)
					.concat(fileType.FILE_TYPES.octet)}`;

const USER_INFO_PROTO = `<div class="image col">
                            <img class="avatar float-right" src="" alt="">
                            </div>
                            <div class="info col">
                                <div class="row">
                                    <div class="col">
                                        <span class="user-status"></span>
                                        <label class="fullname"></label>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col">
                                        <span class="last-online"></span>
                                    </div>
                                </div>
                            </div>`;

const USER_UNIT_PROTO = `<div class="row no-gutters user-unit">
                            <input type="hidden" class="friend-ids">
                            <input type="hidden" class="last-message-id">
                            <input type="hidden" class="last-message-sender">
                            <input type="hidden" class="last-online">
                            <div class="col-2">
                                <img class="avatar" height="40" width="40" >
                            </div>
                            <div class="col-10 flex-display">
                                <div class="row no-gutters w-100">
                                    <div class="users text-truncate col-7">
                                        <span class="user-status"></span>
                                        <label class="fullname"></label>
                                     </div>
                                    <div class="col message-time text-right d-none"></div>
                                    <div class="w-100"></div>
                                    <div class="message-summary col-10 text-truncate d-none"></div>
                                    <div class="col text-right d-none">
                                        <i class="material-icons md-12 status-icon"></i>
                                        <span class="badge badge-dark unread-count"></span>
                                    </div>
                                    </div>
                             </div>
                          </div>`;
const MESSAGE_PROTO = `<div class="msg">
							<div class="msg-control dropdown" aria-labelledby="dropdown-msg-control">
								<i class="dropdown-msg-control material-icons md-25 dropdown-toggle" 
									data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">arrow_drop_down</i>
								<div class="dropdown-menu" aria-labelledby="dropdown-msg-control">
									<a class="dropdown-item" href="#" data-action="1">
										<i class="material-icons md-16">info</i>&nbsp;Info
									</a>
									<a class="dropdown-item" href="#" data-action="2">
										<i class="material-icons md-16">reply</i>&nbsp;Reply
									</a>
									<a class="dropdown-item" href="#" data-action="3">
										<i class="material-icons md-16">forward</i>&nbsp;Forward
									</a>
									<a class="dropdown-item" href="#" data-action="4">
										<i class="material-icons md-16">delete_forever</i>&nbsp;Delete
									</a>
								</div>
							</div>
					   		<div class="msg-div">
					   		    <input type="hidden" class="msg-ids">
					   			<div class="attach-content">
					   				<input type="hidden" class="attach-source">
					   				<input type="hidden" class="attach-filename">
					   				<input type="hidden" class="attach-mediatype">
					   			</div>
								<p class="text-content"></p>
					   			<div class="ts-div">
					   				<span class="ts"></span>
					   				<input type="hidden" class="s-dt">
					   				<input type="hidden" class="d-dt">
					   				<input type="hidden" class="r-dt">
					   				<i class="material-icons md-12 status-icon"></i>
					   			</div>
					   		</div>
					   </div>`;
const MESSAGE_INFO_PROTO = `<div class="msg-info">
								<div class="msg-info-title">
									<span class="s-name float-left"></span>
									<span class="r-name float-right"></span>
								</div>
						   		<div class="msg-content-div">
						   			<div class="attach-content">
						   				<img class="msg-info-attach">
						   				<span class="file-link">
						   			</div>
						   			<p class="text-content"></p>
						   		</div>
						   		<div class="delivery-status-div">
						   			<div class="row">
						   				<div class="col-1">
						   					<i class="material-icons md-12 status-icon" title="Sent at">
						   						${StatusIconText.SENT}
						   					</i>
						   				</div>
						   				<div class="col">
						   					<span class="sdt dt"></span>
						   				</div>
						   			</div>
						   			<div class="row">
						   				<div class="col-1">
						   					<i class="material-icons md-12 status-icon" title="Delivered at">
						   						${StatusIconText.DELIVERED}
						   					</i>
						   				</div>
						   				<div class="col">
						   					<span class="ddt dt"></span>
						   				</div>
						   			</div>
						   			<div class="row">
						   				<div class="col-1">
						   					<i class="material-icons md-12 status-icon read" title="Read at">
						   						${StatusIconText.DELIVERED}
						   					</i>
						   				</div>
						   				<div class="col">
						   					<span class="rdt dt"></span>
						   				</div>
						   			</div>
						   		</div>
						   </div>`;
const MESSAGE_REPLY_PROTO = `<div class="msg-reply">
								<div class="r-close" title="Cancel">&times;</div>
							 	<img class="msg-info-attach">
							 	<p class="text-content"></p>
							 	<input type="hidden" class="r-msg-id">
							 </div>`;
const MESSAGE_REPLY_PREV_PROTO = `<div class="msg-reply-prev">
                                     <div class="reply-origin-name text-truncate"></div>
                                     <p class="text-content text-truncate"></p>
                                     <img class="msg-info-attach">
                                     <input type="hidden" class="r-msg-id">
                                  </div>`;
const MESSAGE_DEL_CHECKBOX_PROTO = `<div class="delete-marker">
                                        <i class="material-icons">check_box_outline_blank</i>
                                    </div>` 