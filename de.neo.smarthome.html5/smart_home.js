
var mEndpoint = '';
var mToken = '';
var mMediaCenter = '';
var mPath = '';
var mPlaylist = '';

function initialize() {
	readSetting();
	align();
	window.addEventListener('resize', align);

	if (mEndpoint == null || mEndpoint == ''){
		mEndpoint = mDefaultEndpoint;
		mToken = mDefaultToken;
	}

	var volume_input = document.getElementById('volume_input');
	volume_input.addEventListener('input', setVolume);

	loop();
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
	
	// Setup refresh loop for 5 seconds
	setInterval(loop, 1000 * 5);

	if (mEndpoint == null || mEndpoint == '')
		showDialog('settings');
}

function readSetting(){
	mEndpoint = window.localStorage.getItem("endpoint");
	mToken = window.localStorage.getItem("token");
	mMediaCenter = window.localStorage.getItem("mediacenter");
	mPath = window.localStorage.getItem("path");
	var animate = window.localStorage.getItem("animation");
	mAnimation = (animate != null) && (animate === "yes");
	if (mPath == null)
		mPath = '';
}

function initSettings(){
	var endpoint = document.getElementById('setting_endpoint');
	var token = document.getElementById('setting_token');
	var animate = document.getElementById('setting_animation');
	if (mEndpoint != null)
		endpoint.value = mEndpoint;
	else
		endpoint.value = '';
	if (mToken != null)
		token.value = mToken;
	else
		token.value = '';
	animate.checked = mAnimation;
}

function saveSettings(){
	mEndpoint = document.getElementById('setting_endpoint').value;
	mToken = document.getElementById('setting_token').value;
	mAnimation = document.getElementById('setting_animation').checked;
	window.localStorage.setItem("endpoint", mEndpoint);
	window.localStorage.setItem("token", mToken);
	window.localStorage.setItem("animation", mAnimation ? "yes" : "no");
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
}

function loop() {
	refreshPlayer();
	refreshSwitches();
}

function refreshSwitches(){
	if (mEndpoint != null && mEndpoint != ''){
		var root = document.getElementById('west');
		var east = document.getElementById('east');
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/switch/list?token=' + mToken;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var switches = JSON.parse(request.responseText);
				switches.sort(function(a, b){return a.name.localeCompare(b.name)});
				var content = "";
				for (var i = 0; i < switches.length; i++) {
					var s = switches[i];
					if (s.state == "ON") {
						content += '<div onclick="switchClick(\'' + s.id + '\', \'OFF\')" class="switch on">';
					} else {
						content += '<div onclick="switchClick(\'' + s.id + '\', \'ON\')" class="switch off">';
					}
					content += s.name + "</div>";
					if (i % 2 == 1)
						content += "<br/>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No switches';
			}
			align();
		});
		request.send();
	}
}

function switchClick(id, state){
	var request = new XMLHttpRequest();
	var url = mEndpoint + '/switch/set?token=' + mToken + '&id=' + id + '&state=' + state;
	request.open("GET", url);
	request.send();
	request.addEventListener('load', function(event) {
		refreshSwitches();
	});
}

function mediaClick(id){
	mMediaCenter = id;
	window.localStorage.setItem("mediacenter", mMediaCenter);
	mPath = '';
	refreshControlCenter();
	refreshFiles();
	refreshPlaylist();
}

function getPlaying(callback){
	if (mMediaCenter != null && mMediaCenter != '' && mEndpoint != null && mEndpoint != ''){
		var url = mEndpoint + '/mediaserver/list?token=' + mToken;
		var req = new XMLHttpRequest();  
		req.open("GET", url);
		req.addEventListener('load', function(event) {
			var handled = false;
			if (req.status >= 200 && req.status < 300) {
				var response = JSON.parse(req.responseText);
				if (response !== undefined && !response.error) {
					if (response.length > 0 && response[0] !== undefined){
						for (var i = 0; i < response.length; i++) {
							var m = response[i];
							if (m.id == mMediaCenter) {
								callback(m.current_playing);
								handled = true;
							}
						}
					}
				} else {
					// Ignore error
				}
			} else if (req.status == 404 || req.status == 500) {
				// Ignore error
			}
			if (!handled)
				callback(null);
		});	
		req.timeout = 4000; // 4 seconds timeout
		req.open("GET", url);
		req.send();  
	}
}

function refreshPlayer(){
	getPlaying(function(playing){
		var content = document.getElementById('player_content');
		var console = document.getElementById('player_console');
		var text = '';
		if (playing != null){
			if (playing.path != null && playing.path != '')
				text = '<img class="player_btn" src="img/pls.png" onclick="addFileToPls(null, \'' + playing.path + '\')"/>';
			text += '<div style="float: right">';
			if (playing.artist != null && playing.artist != '')
				text += playing.artist + '<br/>';
			if (playing.title != null && playing.title != '')
				text += playing.title + '<br/>';
			if (playing.artist == null || playing.title == null || playing.artist == '' || playing.title == '')
				text += playing.file + '<br/>';
			text += '</div>';
		} else {
			text += 'Nothing played';
		}
		content.innerHTML = text;
	});
}

function refreshControlCenter(){
	var root = document.getElementById('controlcenter');
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/list?token=' + mToken;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			title = 'Smart Home Console';
			if (request.status >= 200 && request.status < 300) {
				var media = JSON.parse(request.responseText);
				media.sort(function(a, b){return a.name.localeCompare(b.name)});
				var content = "";
				for (var i = 0; i < media.length; i++) {
					var m = media[i];
					if (m.id == mMediaCenter) {
						content += '<div onclick="mediaClick(\'' + m.id + '\')" class="switch on">';
						title = m.name + ' - Smart Home Console';
					} else {
						content += '<div onclick="mediaClick(\'' + m.id + '\')" class="switch off">';
					}
					content += m.name + "</div>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No mediaserver';
			}
			document.title = title;
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No mediaserver';
	}
}

function directoryClick(dir){
	if (dir == '<->'){
		if (mPath.lastIndexOf('<->') >= 1)
			mPath = mPath.substring(0, mPath.lastIndexOf('<->'));
		else
			mPath = '';
	}else if (mPath == '')
		mPath = dir;
	else
		mPath = mPath + '<->' + dir;
	refreshFiles();
}

function play(file){
	if (mEndpoint != null && mEndpoint != ''){
		mFile = file;
		if (mPath != '' && mPath != null)
			file = mPath + '<->' + file; 
		playPath(file);
	}
}

function playPath(file){
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/play_file?token=' + mToken + '&id=' + mMediaCenter + '&player=mplayer&file=' + file;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				showMessage(mFile, 'Start playing file');
			}
		});
		request.send();
	}
}

function extendPls(pls, file){
	hideDialog('playlist');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlist_extend?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls + '&item=' + file;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				showMessage('Playlist extended', 'Playlist <b>' + pls + '</b> was extended.');	
			}
		});
		request.send();
	}
}

function addFileToPls(path, file){
	mFile = file;
	if (path != null && path != '')
		mFile = path + '<->' + file;
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlists?token=' + mToken + '&id=' + mMediaCenter;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var pls = JSON.parse(request.responseText);
				var content = "";
				var root = document.getElementById('playlist_content');
				var title = document.getElementById('playlist_title');
				title.innerHTML = "Select playlist";
				pls.sort(function(a, b){return a.name.localeCompare(b.name);});
				for (var i = 0; i < pls.length; i++) {
					var p = pls[i];
					content += '<div onclick="extendPls(\'' + p.name + '\', \'' + mFile + '\')" class="file link">' + p.name + "</div>";
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No playlists';
			}
			showDialog('playlist');
		});
		request.send();
	}
}

function refreshFiles(){
	var root = document.getElementById('center');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/files?token=' + mToken + '&id=' + mMediaCenter + '&path=' + mPath;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var files = JSON.parse(request.responseText);
				var content = "";
				if (mPath != ''){
					content = '<div onclick="directoryClick(\'<->\')" class="file dir link"><table width="100%"><tr>';
					content += '<td><img src="img/arrow.png" height="16px" class="link"></td><td width="100%">' + mPath.replace(/<->/g, ' | ') + '</td>';
					content += '</tr></table></div>';
				}
				files.sort(function(a, b){if (a.filetype == b.filetype) { return a.name.localeCompare(b.name);} return a.filetype.localeCompare(b.filetype);});
				for (var i = 0; i < files.length; i++) {
					var f = files[i];
					if (f.filetype == "Directory") {
						content += '<div class="file dir">';
					} else {
						content += '<div class="file">';
					}
					content += '<table width="100%"><tr>';
					if (f.filetype == "Directory") {
						content += '<td class="link" onclick="directoryClick(\'' + f.name + '\')" >' + f.name + '</td>';
					} else {
						content += '<td class="link" onclick="play(\'' + f.name + '\')" >' + f.name + '</td>';
					}
					content += '<td align="right" width="70px">';
					content += '<img src="img/play.png" height="32px"/ class="link" onclick="play(\'' + f.name + '\')">';
					content += '<img src="img/pls.png" height="32px"/ class="link" onclick="addFileToPls(mPath, \'' + f.name + '\')">';
					content += '</td></tr></table></div>';
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No files';
			}
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No files';
	}
}

function showMessage(title, msg){
	var doc_title = document.getElementById("msg_title");
	var doc_msg = document.getElementById("msg_content");
	doc_title.innerHTML = title;
	doc_msg.innerHTML = msg;
	showDialog('message');
}

function plsClick(pls){
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/play_playlist?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls + '&player=mplayer';
		request.open("GET", url);
		mPlaylist = pls;
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var pls = JSON.parse(request.responseText);
				showMessage(mPlaylist, 'Play playlist ' + mPlaylist);
			}
		});
		request.send();
	}
}

function deletePlsItem(pls, item){
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		mPls = pls;
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlist_delete_item?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls + '&item=' + item;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				showPlsContent(mPls);
			}
		});
		request.send();
	}
}

function showPlsContent(pls){
	mPls = pls;
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlist_content?token=' + mToken + '&id=' + mMediaCenter + '&playlist=' + pls;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var pls = JSON.parse(request.responseText);
				var content = "";
				var root = document.getElementById('playlist_content');
				var title = document.getElementById('playlist_title');
				title.innerHTML = "Playlist content";
				for (var i = 0; i < pls.length; i++) {
					var p = pls[i];
					content += '<div class="file"><table width="100%"><tr>';
					content += '<td width="90%" onclick="mFile=\'' + p.name + '\';playPath(\'' + p.path + '\')" class="link">' + p.name + "</td>";
					content += '<td align="right">';
					content += '<img src="img/delete.png" height="32px"/ class="link" onclick="deletePlsItem(\'' + mPls + '\', \'' + p.path + '\')">';
					content += '</td></tr></table></div>';
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No items';
			}
			showDialog('playlist');
		});
		request.send();
	}
}

function refreshPlaylist(){
	var root = document.getElementById('east');
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/playlists?token=' + mToken + '&id=' + mMediaCenter;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (request.status >= 200 && request.status < 300) {
				var pls = JSON.parse(request.responseText);
				var content = "";
				pls.sort(function(a, b){return a.name.localeCompare(b.name);});
				for (var i = 0; i < pls.length; i++) {
					var p = pls[i];
					content += '<div class="file"><table width="100%"><tr>';
					content += '<td width="90%" onclick="plsClick(\'' + p.name + '\')" class="link">' + p.name + "</td>";
					content += '<td align="right">';
					//content += '<img src="img/play.png" height="32px"/ class="link" onclick="plsClick(\'' + p.name + '\')">';
					content += '<img src="img/pls.png" height="32px"/ class="link" onclick="showPlsContent(\'' + p.name + '\')">';
					content += '</td></tr></table></div>';
				}
				root.innerHTML = content;
			} else {
				root.innerHTML = 'No playlists';
			}
			align();
		});
		request.send();
	} else {
		root.innerHTML = 'No playlists';
	}
}

function checkResult(request){
	if (request.status >= 200 && request.status < 300) {
		var pls = JSON.parse(request.responseText);
		if (pls != null && pls.success != null && pls.success == false){
			if (pls.error != null)
			showMessage("Error", pls.error.message);
			return false;
		}
		return true;
	} else {
		return false;
	}
}

function playerAction(action, parameter){
	if (mEndpoint != null && mEndpoint != '' && mMediaCenter != null && mMediaCenter != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/mediaserver/' + action + '?token=' + mToken + '&id=' + mMediaCenter + '&player=mplayer';
		if (typeof parameter !== 'undefined') {
			url = url + '&' + parameter;
		}
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				refreshPlayer();
			}
		});
		request.send();
	}
}

function getVolume(){
	getPlaying(function(playing){
		if (playing != null){
			var input = document.getElementById('volume_input');
			input.value = playing.volume;
		}
	});
}

function setVolume(){
	var input = document.getElementById('volume_input');
	playerAction('volume', 'volume=' + (input.value));
}

function doTrigger(trigger){
	mTrigger = trigger;
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/controlcenter/dotrigger?token=' + mToken + '&trigger=' + trigger;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var rules = JSON.parse(request.responseText);
				showMessage('Perform trigger', 'Perform <b>' + mTrigger + '</b> with ' + rules.triggered_rules + ' events.' );
			}
		});
		request.send();
	}
}

function deleteEventRule(trigger){
	mTrigger = trigger;
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/controlcenter/delete_event_rule?token=' + mToken + '&trigger=' + trigger;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var rules = JSON.parse(request.responseText);
				showRules();
			}
		});
		request.send();
	}
}

function addInfo(){
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var info = document.getElementById('trigger_infos');
		var url = mEndpoint + '/controlcenter/set_information_for_event_rule?token=' + mToken + '&trigger=' + mRuleId + '&informations=' + info.value;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var rules = JSON.parse(request.responseText);
				hideDialog('trigger');
				showRules();
			}
		});
		request.send();
	}
}

function deleteEvent(index){
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/controlcenter/delete_event_in_rule?token=' + mToken + '&trigger=' + mRuleId + '&index=' + index;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var rules = JSON.parse(request.responseText);
				hideDialog('trigger');
				showRules();
			}
		});
		request.send();
	}
}

function showTrigger(index){
	var trigger = mRules[index];
	mRuleIndex = index;
	mRuleId = trigger.trigger;
	title = document.getElementById('trigger_title');
	content = document.getElementById('trigger_content');
	id = document.getElementById('trigger_id');
	infos = document.getElementById('trigger_infos');
	
	title.innerHTML = 'Edit trigger';
	id.value = trigger.trigger;
	infoText = '';
	for (var i = 0; i < trigger.information.length; i++)
		infoText += trigger.information[i].key + ', ';
	infos.value = infoText;

	contentText = '<table width="100%">';
	for (var i = 0; i < trigger.events.length; i++){
		var event = trigger.events[i];
		contentText += '<tr><td colspan="2" class="highlight">' + event.unit_id + ''
		contentText += '<img src="img/delete.png" height="32px"/ class="link right" onclick="deleteEvent(\'' + i + '\')">';
		contentText += '</td></tr>';
		condition = '';
		if (event.condition != null)
			condition = event.condition;
		contentText += '<tr><td style="width: 195px">Condition</td><td><input class="fill" value="' + condition + '"/></td></tr>';
		parameter = '';
		for (var property in event.parameter) {
			if (event.parameter.hasOwnProperty(property)) {
				parameter += property + '=\'' + event.parameter[property] + '\', ';
			}
		}
		contentText += '<tr><td>Parameter</td><td><input class="fill" value="' + parameter + '"/></td></tr>';

	}
	contentText += '</table>';
	content.innerHTML = contentText;

	showDialog('trigger');
}

function createEventRule(){
	var input = document.getElementById('new_event_rule');
	if (mEndpoint != null && mEndpoint != '' && input != null){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/controlcenter/create_event_rule?token=' + mToken + '&trigger=' + input.value;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				var rules = JSON.parse(request.responseText);
				showRules();
			}
		});
		request.send();
	}
}

function showRules(){
	var root = document.getElementById('rules_content');
	showDialog('rules');
	root.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	if (mEndpoint != null && mEndpoint != ''){
		var request = new XMLHttpRequest();
		var url = mEndpoint + '/controlcenter/rules?token=' + mToken;
		request.open("GET", url);
		request.addEventListener('load', function(event) {
			if (checkResult(request)) {
				mRules = JSON.parse(request.responseText);
				var root = document.getElementById('rules_content');
				var content = "";
				for (var i = 0; i < mRules.length; i++) {
					var rule = mRules[i];
					content += '<div class="file"><table width="100%"><tr>';
					content += '<td width="80%" onclick="showTrigger(' + i + ')" class="link">' + rule.trigger + "</td>";
					content += '<td align="right">';
					content += '<img src="img/play.png" height="32px"/ class="link" onclick="doTrigger(\'' + rule.trigger + '\')">';
					content += '<img src="img/delete.png" height="32px"/ class="link" onclick="deleteEventRule(\'' + rule.trigger + '\')">';
					content += '</td></tr></table></div>';
				}
				content += '<div class="file"><table width="100%"><tr>';
				content += '<td width="90%"><input value="new.event_rule" id="new_event_rule" class="fill"/></td>';
				content += '<td align="right">';
				content += '<img src="img/add.png" height="32px" class="link" onclick="createEventRule()">';
				content += '</td></tr></table></div>';
				root.innerHTML = content;
			}
		});
		request.send();
	}
}

