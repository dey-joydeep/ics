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
                            <div class="col-10">
                                <div class="row no-gutters">
                                    <div class="users text-truncate col-7">
                                        <span class="user-status"></span>
                                        <label class="fullname"></label>
                                     </div>
                                    <div class="col message-time text-right">
                                    </div>
                                    <div class="w-100"></div>
                                    <div class="message-summary col-10 text-truncate"></div>
                                    <div class="col text-right">
                                        <i class="material-icons md-12 status-icon"></i>
                                        <span class="badge badge-dark unread-count"></span>
                                    </div>
                                    </div>
                             </div>
                          </div>`;
