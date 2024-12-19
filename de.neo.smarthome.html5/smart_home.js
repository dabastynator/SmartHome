

class APIHandler
{
	configure(endpoint, name, defaultParameter = {})
	{
		this.endpoint = endpoint;
		this.name = name;
		this.defaultParameter = JSON.parse(JSON.stringify(defaultParameter));
	}

	addDefaultParameter(key, value)
	{
		this.defaultParameter[key] = value;
	}

	call(method, callback = null, parameters = {})
	{
		var url = this.endpoint + '/' + this.name + '/' + method;
		parameters = {...this.defaultParameter, ...parameters }
		var xmlHttp = new XMLHttpRequest();
		xmlHttp.onreadystatechange = function() { 
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
			{
				var jsonResponse = JSON.parse(xmlHttp.responseText);
				if (callback)
				{
					callback(jsonResponse);
				}				
			}				
		}
		xmlHttp.open("POST", url, true);
		xmlHttp.send(JSON.stringify(parameters));
	}
}

const Separator = "/";

var apiTrigger = new APIHandler();
var apiMediaServer = new APIHandler();
var apiSwitch = new APIHandler();
var apiScenes = new APIHandler();
var apiAction = new APIHandler();
var apiUser = new APIHandler();
var apiInformation = new APIHandler();
var mMediaCenter = '';
var mPath = '';
var mUserList;
var mSelectedUser;
var mFiles;
var mPlaylists;
var mPlaylistContent;
var mSwitchesCache = '';

var htmlFiles;
var htmlPls;
var htmlPlsContent;
var htmlSwitches;
var htmlScenes;
var htmlPlayInfo;
var htmlPlayProgress;
var htmlMediaServer;
var htmlUserList;
var htmlInformation;
var htmlFilesBack;
var htmlFilesSearch;
var htmlPlsAdd;
var htmlPlayPause;

function initialize()
{
	htmlFiles = document.getElementById('filesystem_files');
	htmlPls = document.getElementById('playlists_list');
	htmlPlsContent = document.getElementById('playlist_content');
	htmlSwitches = document.getElementById('switch_list');
	htmlScenes = document.getElementById('scene_list');
	htmlPlayInfo = document.getElementById('player_content');
	htmlPlayProgress = document.getElementById('playing_progress');
	htmlMediaServer = document.getElementById('mediaserver');
	htmlUserList = document.getElementById('userlist_content');
	htmlInformation = document.getElementById('information');
	htmlFilesBack = document.getElementById('filesystem_back');
	htmlFilesSearch = document.getElementById('filesystem_search');
	htmlPlsAdd = document.getElementById('playlist_add');
	htmlPlayPause = document.getElementById('play_pause');

	readSetting();
	placeDialogs();
	updateVisibleCard();

	var volume_input = document.getElementById('volume_input');
	volume_input.addEventListener('input', setVolume);

	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
	refreshInformation();
	refreshPlayer();
	refreshSwitches();
	refreshScenes();
	
	setInterval(loopLong, 1000 * 30);
	setInterval(loopShort, 1000 * 1);

	if (apiMediaServer.endpoint == null || apiMediaServer.endpoint == '')
	{
		showDialog('settings');
	}
}

function findGetParameter(parameterName)
{
    var result = null,
        tmp = [];
    var items = location.search.substr(1).split("&");
	for (var index = 0; index < items.length; index++)
	{
        tmp = items[index].split("=");
        if (tmp[0] === parameterName) result = decodeURIComponent(tmp[1]);
    }
    return result;
}

function clearSettings()
{
	window.localStorage.clear();
}

function readSetting()
{
	endpoint = window.localStorage.getItem("endpoint");
	token = window.localStorage.getItem("token");
	if (endpoint == null || endpoint == '')
	{
		endpoint = findGetParameter('endpoint');
		token = findGetParameter('token');
		if (endpoint && token)
		{
			window.localStorage.setItem("endpoint", endpoint);
			window.localStorage.setItem("token", token);
		}
	}	
	mMediaCenter = window.localStorage.getItem("mediacenter");
	mPath = window.localStorage.getItem("path");	
	if (mPath == null)
		mPath = '';
	parameter = {'token' : token};
	apiTrigger.configure(endpoint, 'trigger', parameter)
	apiMediaServer.configure(endpoint, 'mediaserver', parameter)
	apiMediaServer.addDefaultParameter('player', 'mplayer');
	apiMediaServer.addDefaultParameter('id', mMediaCenter);
	apiSwitch.configure(endpoint, 'switch', parameter);
	apiScenes.configure(endpoint, 'scene', parameter);
	apiAction.configure(endpoint, 'action', parameter);
	apiUser.configure(endpoint, 'user', parameter);
	apiInformation.configure(endpoint, 'information', parameter);

	//document.getElementById('setting_current_user').value = '';
	setIsAdmin(false);
	apiUser.call('current', function(result)
	{
		if (checkResult(result, null, false))
		{
			setIsAdmin(result.role === 'ADMIN');
			//document.getElementById('setting_current_user').value = result.name;
		}
	}, {'token': token});
}

function setIsAdmin(isAdmin)
{
	adminVisible = 'none';
	if (isAdmin)
	{
		adminVisible = 'block';
	}
	document.getElementById('btn_user').style.display = adminVisible;
}

function initSettings()
{
	var endpoint = document.getElementById('setting_endpoint');
	var token = document.getElementById('setting_token');
	var appearence = document.getElementById('setting_appearence');

	endpoint.value = window.localStorage.getItem("endpoint");
	token.value = window.localStorage.getItem("token");
	appearence.value = window.localStorage.getItem("appearence");
}

function saveSettings()
{
	endpoint = document.getElementById('setting_endpoint').value;
	token = document.getElementById('setting_token').value;
	appearence = document.getElementById('setting_appearence');
	mMediaCenter = '';
	window.localStorage.setItem("endpoint", endpoint);
	window.localStorage.setItem("token", token);
	window.localStorage.setItem("appearence", appearence.value);
	loadAppearence();
	readSetting();
	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
}

function loopLong()
{
	refreshInformation();
	refreshScenes();
}

function loopShort()
{
	refreshPlayer();
	refreshSwitches();
}

function refreshScenes()
{
	apiScenes.call('list', function(scenes)
	{
		if (checkResult(scenes, htmlScenes))
		{
			scenes.sort(function(a, b){return a.name.localeCompare(b.name)});
			var content = "";
			for (var i = 0; i < scenes.length; i++)
			{
				var scene = scenes[i];
				content += '<button onclick="sceneClick(\'' + scene.id + '\')" class="switch off" style="width: 140px">';
				content += scene.name + "</button>";
			}
			htmlScenes.innerHTML = content;
		}		
	});
}

function refreshSwitches()
{
	apiSwitch.call('list', function(switches)
	{
		if (checkResult(switches, htmlSwitches))
		{
			switches.sort(function(a, b){return a.name.localeCompare(b.name)});
			var content = "";
			for (var i = 0; i < switches.length; i++)
			{
				var s = switches[i];
				if (s.state == "ON")
				{
					content += '<button onclick="switchClick(\'' + s.id + '\', \'OFF\')" class="switch on" style="width: 140px">';
				}
				else
				{
					content += '<button onclick="switchClick(\'' + s.id + '\', \'ON\')" class="switch off" style="width: 140px">';
				}
				content += s.name + "</button>";
			}
			if (content != mSwitchesCache)
			{
				mSwitchesCache = content;
				htmlSwitches.innerHTML = content;
			}
		}		
	});
}

function fileRow(row, buttons)
{
	var file = '<div class="file">';
	if (row.hasOwnProperty('subrow'))
	{
		file = '<div class="file" style="flex-flow: wrap">';
		file += '<div class="flex_line">';
	}
	var captionClasses = "file_caption"
	var onclick = "";
	if (row.hasOwnProperty('bold') && row.bold)
	{
		captionClasses += " bold";
	}
	if (row.hasOwnProperty('onclick'))
	{
		onclick = row.onclick
	}
	file += '<div class="' + captionClasses + '" ' + onclick + '>' + row.caption + '</div>';
	for(button of buttons)
	{
		var onclick = ''
		if (button.hasOwnProperty('onclick') && button.onclick)
		{
			onclick = button.onclick;
		}
		file += '<img class="file_button" src="' + button.src + '" ' + onclick + '>';
	}
	if (row.hasOwnProperty('subrow'))
	{
		file += '</div>';
		file += '<div class="gradient"></div>';
		file += '<div class="flex_line">';
		file += '<div class="file_caption search_result" ' + row.subrow.onclick + '>' + row.subrow.caption + '</div>';
		if (row.hasOwnProperty('subbutton'))
		{
			for(button of row.subbutton)
			{
				var onclick = ''
				if (button.hasOwnProperty('onclick') && button.onclick)
				{
					onclick = button.onclick;
				}
				file += '<img class="file_button" src="' + button.src + '" ' + onclick + '>';
			}
		}
		file += '</div>';
	}
	file += '</div>';
	return file;
}

function refreshInformation()
{
	apiInformation.call('list', function(informations)
	{
		if (checkResult(informations, htmlInformation))
		{
			var content = "";
			for (var i = 0; i < informations.length; i++)
			{
				var info = informations[i];
				
				if (info.id == "sensor.garage_door")
				{
					content += fileRow({"caption": info.name}, [{'src': 'img/cover_' + info.state + '.png'}]);
				}
				if (info.id == "sensor.rct_power_storage_generator_a_energy_production_day")
				{
					var sunContent = Math.round(Number(info.state) / 100) / 10 + "kWh";
					content += fileRow({"caption": info.name + ' ' + sunContent}, [{'src': 'img/sun_power.png'}]);
				}
				if (info.type == "weather")
				{
					var icon_path = 'weather/';
					var icon_name = '';
					// Respect sunrise-sunset
					if (info.day_night == 'Day')
						icon_name = 'sun';
					else
						icon_name = 'moon';
					// Respect clouds
					if (info.clouds >= 90)
						icon_name = 'cloud';
					else if (info.clouds > 50)
						icon_name += '_cloud';
					else if (info.clouds > 10)
					  icon_name += '_cloud_less';
					// Respect rain and snow
					if (info.rain != null && info.rain)
						icon_name += '_rain';
					else if (info.snow != null && info.snow)
						icon_name += '_snow';
					icon_path += icon_name + '.png"';
					content += fileRow({"caption": 'Draußen ' + Math.round(info.celsius) + '&deg;'}, [{'src': icon_path}]);
				}
			}
			htmlInformation.innerHTML = content;
		}		
	});
}

function switchClick(id, state)
{
	apiSwitch.call('set', refreshSwitches, {'id': id, 'state': state});
}

function sceneClick(id)
{
	apiScenes.call('activate', null, {'id': id});
}

function mediaClick(id){
	mMediaCenter = id;
	window.localStorage.setItem("mediacenter", mMediaCenter);
	apiMediaServer.addDefaultParameter('id', mMediaCenter);
	mPath = '';
	refreshMediaServer();
	refreshFiles();
	refreshPlaylist();
}

function getPlaying(callback){
	apiMediaServer.call('list', function(result)
	{
		handled = false;
		if (checkResult(result, null, false)) {
			if (result.length > 0 && result[0] !== undefined){
				for (var i = 0; i < result.length; i++) {
					var m = result[i];
					if (m.id == mMediaCenter) {
						callback(m.current_playing);
						handled = true;
					}
				}
			}
		}
		if (!handled)
		{
			callback(null);
		}
	});
}

function refreshPlayer(){
	getPlaying(function(playing){
		var text = '';
		var progress = '0%';
		if (playing != null){
			if (playing.artist != null && playing.artist != '')
				text += playing.artist + '<br/>';
			if (playing.title != null && playing.title != '')
				text += playing.title + '<br/>';
			if (playing.artist == null || playing.title == null || playing.artist == '' || playing.title == '')
				text += playing.file + '<br/>';
			if (playing.state == "PLAY")
				htmlPlayPause.src = 'player/pause.png';
			else
				htmlPlayPause.src = 'player/play.png';
			if (playing.durationSec > 0)
			{
				progress = Math.round(100 * playing.inTrackSec / playing.durationSec) + '%';
			}
		} else {
			htmlPlayPause.src = 'player/play.png';
			text += 'Nothing played';
		}
		htmlPlayInfo.innerHTML = text;
		htmlPlayProgress.style.width = progress;
	});
}


function refreshMediaServer()
{
	document.title = 'Smart Home Console';
	apiMediaServer.call('list', function(media) 
	{
		if (checkResult(media, htmlMediaServer))
		{
			media.sort(function(a, b){return a.name.localeCompare(b.name)});
			var content = "";
			var title = 'Smart Home Console';
			if (media.length == 1)
			{
				if (mMediaCenter != media[0].id)
				{
					mediaClick(media[0].id);
				}
			}
			for (var i = 0; i < media.length; i++)
			{
				var m = media[i];
				if (m.id == mMediaCenter)
				{
					content += '<button onclick="mediaClick(\'' + m.id + '\')" class="switch on">';
					title = m.name + ' - Smart Home Console';
				} else {
					content += '<button onclick="mediaClick(\'' + m.id + '\')" class="switch off">';
				}
				content += m.name + "</button>";
			}
			if (content == '')
			{
				content = 'No mediaserver';
			}
			htmlMediaServer.innerHTML = content;
			document.title = title;
		}		
	}, {'id': ''});
}

function directoryClick(index){	
	if (index == Separator)
	{
		if (mPath.lastIndexOf(Separator) >= 1)
			mPath = mPath.substring(0, mPath.lastIndexOf(Separator));
		else
			mPath = '';
	}
	else
	{
		mPath = getPath(mFiles[index]);
	}
	window.localStorage.setItem("path", mPath);
	refreshFiles();
}

function play(index)
{
	mFile = mFiles[index];
	apiMediaServer.call('play_file', function(result)
	{
		if (checkResult(result)) {
			showToast('Start playing <b>' + mFile.name + '</b>');
		}
	}, {'file': mFile.path});
}

function playPlsFile(index)
{
	var plsFile = mPlaylistContent[index];
	apiMediaServer.call('play_file', function(result)
	{
		if (checkResult(result)) {
			showToast('Start playing file <b>' + plsFile.name + '</b>');
		}
	}, {'file': plsFile.path});
}

function extendPls(index){
	mPlaylist = mPlaylists[index];
	hideDialog('playlist');
	apiMediaServer.call('playlist_extend', function(result)
	{
		if (checkResult(result)) {
			showToast('Playlist <b>' + mPlaylist.name + '</b> was extended.');
		}
	}, {'playlist': mPlaylist.name, 'item': mFile.path});
}

function addFileToPls(path, index){
	mFile = mFiles[index];
	apiMediaServer.call('playlists', function(result)
	{
		if (checkResult(result)) {
			var content = "";
			var title = document.getElementById('playlist_title');
			title.innerHTML = "Select playlist";
			result.sort(function(a, b){return a.name.localeCompare(b.name);});
			mPlaylists = result;
			for (var i = 0; i < result.length; i++) {
				var p = result[i];
				content += fileRow(
					{"caption": p.name, "onclick": 'onclick="extendPls(\'' + i + '\')"'},
					[{"src": "img/add.png", "onclick": 'onclick="extendPls(\'' + i + '\')"'}]
				);
			}
			htmlPlsContent.innerHTML = content;
			showDialog('playlist');
		}
	});
}

function searchFiles()
{
	var input = document.getElementById("text_input_edit");
	htmlFiles.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	apiMediaServer.call('search', function(result)
	{
		if (checkResult(result))
		{
			showFiles(result, true);
			if (result.length == 0)
			{
				showToast("No search result");
			}
			else if (result.length == 1)
			{
				showToast("1 search result");
			}
			else
			{
				showToast(result.length + " files found");
			}
		}
		htmlFilesBack.classList.remove("gone");
	}, {"target": input.value, "path": mPath});
}

function searchFilesDlg()
{
	var headline = document.getElementById("text_input_title");
	var input = document.getElementById("text_input_edit");
	var label = document.getElementById("text_input_label");
	var button = document.getElementById("text_input_button");
	headline.innerHTML = "Search";
	input.placeholder = "search...";
	input.value = "";
	label.innerHTML = "Enter text to search for";
	button.onclick = function()
	{
		hideDialog('text_input');
		searchFiles();
	};
	showDialog('text_input');
	input.focus();
}

function getPath(file)
{
	if (file.filetype == "Directory")
	{
		return file.path;
	}
	return file.path.substr(0, file.path.lastIndexOf(Separator));
}

function showFiles(files, isSearch)
{
	var content = "";
	if (mPath != '')
	{
		if (mPath.startsWith("/"))
		{
			mPath = mPath.substr(1);
		}
		content = fileRow(
			{"caption": mPath.replace(/\//g, ' | '), "bold": true, "onclick": 'onclick="directoryClick(\'' + Separator + '\')"'}, 
			[{"src": "img/back.png", "onclick": 'onclick="directoryClick(\'' + Separator + '\')"'}]
		);
	}
	files.sort(function(a, b){if (a.filetype == b.filetype) { return a.name.localeCompare(b.name);} return a.filetype.localeCompare(b.filetype);});
	mFiles = files;
	for (var i = 0; i < files.length; i++)
	{
		var f = files[i];
		var row = {"caption": f.name};
		if (f.filetype == "Directory")
		{
			row.bold = true;
			row.onclick = 'onclick="directoryClick(' + i + ')"';
		}
		else
		{
			row.onclick = 'onclick="play(' + i + ')"';
		}
		var buttons =
		[
			{"src": "player/play.png", "onclick": 'onclick="play(' + i + ')"'},
			{"src": "img/pls.png", "onclick": 'onclick="addFileToPls(mPath, \'' + i + '\')"'}
		];
		if (isSearch)
		{
			path = getPath(f);
			row.subrow = {
				"caption": getPath(f),
				"onclick": 'onclick="directoryClick(' + i + ')"'
			};
			row.subbutton = [
				{"src": "img/go.png", "onclick": 'onclick="directoryClick(' + i + ')"' }
			];
		}
		content += fileRow(row, buttons);
	}
	if (files.length == 0)
	{
		content += '<div class="headline">';
		if (isSearch)
		{
			content += 'No search result';
		}
		else
		{
			content += 'Empty folder';
		}
		content += '</div>';
	}
	content += '<div style="height:50px"></div>';
	content += '</div>';
	htmlFiles.innerHTML = content;
}

function refreshFiles()
{
	apiMediaServer.call('files', function(files)
	{
		if (checkResult(files, htmlFiles))
		{
			showFiles(files, false);
			if (mPath != '')
			{
				htmlFilesBack.classList.remove("gone");
				htmlFilesBack.onclick = function(){ directoryClick(Separator) };
			}
			else
			{
				htmlFilesBack.classList.add("gone");
			}
			htmlFilesSearch.classList.remove("gone");
		}
		else
		{
			htmlFilesBack.classList.add("gone");
			htmlFilesSearch.classList.add("gone");
		}
	}, {'path': mPath});
}

function showToast(message)
{
	var toast = document.getElementById("toast");
	toast.innerHTML = message;
	toast.classList.add("show");
	setTimeout(function()
	{
		toast.classList.remove("show");
	}, 2000);
}

function plsClick(index){
	mPlaylist = mPlaylists[index];
	apiMediaServer.call('play_playlist', function(result)
	{
		if (checkResult(result)) {
			showToast('Play playlist <b>' + mPlaylist.name + '</b>');
		}
	}, {'playlist': mPlaylist.name});
}

function deletePlsItem(index){
	var plsFile = mPlaylistContent[index];
	apiMediaServer.call('playlist_delete_item', function(result)
	{
		if (checkResult(result)) {
			showPlsContent(-1);
		}
	}, {'playlist': mPlaylist.name, 'item': plsFile.path});
}

function deletePlaylist()
{
	if(mPls == null)
	{
		return;
	}
	apiMediaServer.call('playlist_delete', function(result)
	{
		if (checkResult(result))
		{
			showToast('<b>' + mPls.name + "</b> deleted.");
			refreshPlaylist();
		}
		mPls = null;
	}, {'playlist': mPls.name});
}

function showPlsContent(index)
{
	if (index >= 0)
	{
		mPls = mPlaylists[index];
	}
	apiMediaServer.call('playlist_content', function(result)
	{
		if (checkResult(result, htmlPlsContent))
		{
			mPlaylistContent = result;
			var content = "";			
			var title = document.getElementById('playlist_title');
			title.innerHTML = mPls.name;
			for (var i = 0; i < result.length; i++)
			{
				var p = result[i];
				content += fileRow(
					{"caption": p.name, "onclick": 'onclick="playPlsFile(\'' + i + '\')"'},
					[{"src": "img/trash.png", "onclick": 'onclick="deletePlsItem(\'' + i + '\')"'}]
				);
			}
			htmlPlsContent.innerHTML = content;
			showDialog('playlist');
		}
	}, {'playlist': mPls.name});
}

function createNewPlaylist()
{
	var input = document.getElementById("text_input_edit");
	apiMediaServer.call('playlist_create', function(result)
	{
		if (checkResult(result))
		{
			refreshPlaylist();
			showToast("Playlist <b>" + input.value + "</b> created.");
		}		
	}, {"playlist": input.value});
}

function newPlaylist()
{
	var headline = document.getElementById("text_input_title");
	var input = document.getElementById("text_input_edit");
	var label = document.getElementById("text_input_label");
	var button = document.getElementById("text_input_button");
	headline.innerHTML = "Create new Playlist";
	input.placeholder = "Playlist Name";
	input.value = "";
	label.innerHTML = "Name of the new Playlist";
	button.onclick = function()
	{
		hideDialog('text_input');
		createNewPlaylist()
	};
	showDialog('text_input');
	input.focus();
}

function refreshPlaylist()
{
	apiMediaServer.call('playlists', function(result)
	{
		if (checkResult(result, htmlPls))
		{
			var content = "";
			result.sort(function(a, b){return a.name.localeCompare(b.name);});
			mPlaylists = result;
			for (var i = 0; i < result.length; i++)
			{
				var p = result[i];
				content += fileRow(
					{"caption": p.name, "onclick": 'onclick="plsClick(\'' + i + '\')"'},
					[{"src": "img/pls.png", "onclick": 'onclick="showPlsContent(\'' + i + '\')"'}]
				);
			}
			content += '<div style="height:50px"></div>';
			content += '</div>';
			htmlPls.innerHTML = content;
			htmlPlsAdd.classList.remove("gone");
		}
		else
		{
			htmlPlsAdd.classList.add("gone");
		}
	});
}

function checkResult(result, errorHtmlElement = null, showErrorMsg = true){
	if (result != null && result.success != null && result.success == false){
		if (result.error != null)
		{
			var message = result.error.message;
			if (message == null && result.error.class != null){
				message = 'Ups, that shouldn\'t happen... ' + result.error.class;
			}
			if (errorHtmlElement){
				errorHtmlElement.innerHTML = message;
			} else if (showErrorMsg) {
				showToast("Error: <b>" + result.error.message + '</b>');
			}
		}		
		return false;
	}
	return true;
}

function playerAction(action, parameter){
	apiMediaServer.call(action, function(result)
	{
		if (checkResult(result)) {
			refreshPlayer();
		}
	}, parameter);
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
	playerAction('volume', {'volume': input.value});
}

/***************
***** User *****
***************/

function showUser()
{
	apiUser.call('list', function(result)
	{
		if (checkResult(result))
		{
			var content = "";
			mUserList = result;
			for (var i = 0; i < result.length; i++)
			{
				var u = result[i];
				var caption = u.name;
				if (u.role === 'ADMIN')
				{
					caption += ' (Admin)';
				}
				content += fileRow(
					{"caption": caption},
					[{"src": "img/edit.png", "onclick": 'onclick="editUser(\'' + i + '\')"'}]
				);
			}
			htmlUserList.innerHTML = content;
			showDialog('userlist');
		}
	});
}

function editUser(index)
{
	var title = document.getElementById('edit_user_title');
	document.getElementById('user_passwd').value = '';
	var visibility = 'hidden';
	if (index >= 0)
	{
		mSelectedUser = mUserList[index];
		document.getElementById('user_name').value = mSelectedUser.name;
		document.getElementById('user_role').value = mSelectedUser.role;
		title.innerHTML = 'Edit User ' + mSelectedUser.name;
		visibility = 'visible';
	} else {
		mSelectedUser = null;
		document.getElementById('user_name').value = '';
		title.innerHTML = 'Create new User';
	}
	document.getElementById('btn_user_remove').style.visibility = visibility;
	var user_required = document.getElementsByClassName('user_required');
	for (var i = 0; i < user_required.length; i++) {
		user_required[i].style.visibility = visibility;
	}
	showDialog('user_edit');
	refreshUserAccess();
	refreshUserSessions();
}

function deleteUser()
{
	if (mSelectedUser != null)
	{
		apiUser.call('delete', function(result)
		{
			if (checkResult(result))
			{
				hideDialog('user_edit');
				showUser();
			}
		}, {'user_id': mSelectedUser.id});
	}
}

function refreshUserAccess()
{
	var accessList = document.getElementById('user_access');
	if (mSelectedUser == null)
	{
		accessList.innerHTML = '';
		return;
	} else {
		accessList.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	}
	apiUser.call('get_access', function(result)
	{
		var accessList = document.getElementById('user_access');
		if (checkResult(result, accessList))
		{
			var content = "";
			for (var i = 0; i < result.length; i++)
			{
				var access = result[i];
				content += fileRow(
					{"caption": access.name},
					[{"src": "img/trash.png", "onclick": 'onclick="deleteUserAccess(\'' + access.id + '\')"'}]
				);
			}
			if (content == "")
			{
				content = '<i>No User Access</i>';
			}
			accessList.innerHTML = content;
		}
	}, {'user_id': mSelectedUser.id});
}

function deleteUserAccess(id)
{
	apiUser.call('remove_access', function(result)
	{
		if (checkResult(result))
		{
			refreshUserAccess();
		}
	}, {'user_id': mSelectedUser.id, 'unit_id': id});
}

function applyUserName()
{
	var newName = document.getElementById('user_name').value;
	var newPasswd = document.getElementById('user_passwd').value;
	var callback = function(result)
		{
			if (checkResult(result)){
				hideDialog('user_edit');
				showUser();
			}
		};
	if (mSelectedUser != null)
	{
		apiUser.call('change_name', callback, {'user_id': mSelectedUser.id, 'new_name': newName});
		if (newPasswd)
		{
			apiUser.call('change_password', null, {'user_id': mSelectedUser.id, 'new_password': newPasswd});
		}
	} else {
		apiUser.call('create', callback, {'user_name': newName, 'password': newPasswd, 'avatar': ''});
	}
}

function addUnitAccess(unitId)
{
	if (mSelectedUser != null)
	{
		apiUser.call('add_access', function(result)
		{
			if (checkResult(result))
			{
				refreshUserAccess();
			}
		}, {'user_id': mSelectedUser.id, 'unit_id': unitId});
	}
	hideDialog('accesslist');
}

function refreshUserSessions()
{
	var accessList = document.getElementById('user_session');
	if (mSelectedUser == null)
	{
		accessList.innerHTML = '';
		return;
	} else {
		accessList.innerHTML = '<div style="text-align: center"><img src="img/loading.png" class="rotate" width="128px"/></div>';
	}
	apiUser.call('list_tokens', function(result)
	{
		var sessionList = document.getElementById('user_session');
		if (checkResult(result, sessionList))
		{
			var content = "";
			for (var i = 0; i < result.length; i++)
			{
				var session = result[i];
				if (mSelectedUser.id === session.user_id)
				{
					content += fileRow(
						{"caption": session.token},
						[{"src": "img/trash.png", "onclick": 'onclick="deleteUserSession(\'' + session.token + '\')"'}]
					);
				}
			}
			if (content == "")
			{
				content = '<i>No User Session</i>';
			}
			sessionList.innerHTML = content;
		}
	});
}

function deleteUserSession(session)
{
	if (mSelectedUser == null)
	{
		return;
	}
	apiUser.call('delete_token', function(result)
	{
		if (checkResult(result))
		{
			refreshUserSessions();
		}
	}, {'delete_token': session});
}

function addUserSession()
{
	if (mSelectedUser == null)
	{
		return;
	}
	apiUser.call('create_persistent_token', function(result)
	{
		if (checkResult(result))
		{
			refreshUserSessions();
		}
	}, {'user_id': mSelectedUser.id});
}
