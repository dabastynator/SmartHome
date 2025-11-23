

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

class LocalPlayer
{
	constructor()
	{
		this.files = null;
		this.idx = 0;
		this.currentFile = null;
		this.audio = new Audio();
		this.audio.onended = function() {
			playerAction("next");
		};
		this.audio.addEventListener("error", () => {
			showToast(`Error loading: ${this.currentFile.name}`);
		});
	}

	stop()
	{
		if (!this.audio.paused)
		{
			this.audio.pause();
		}
		this.currentFile = null;
	}

	startPlay(idx = -1)
	{
		if (idx >= 0)
		{
			this.idx = parseInt(idx);
		}
		if (this.idx < 0)
		{
			showToast("Nothing to play!");
			return;
		}
		if (!this.audio.paused)
		{
			this.audio.pause();
		}
		this.currentFile = this.files[this.idx];
		this.audio.src = getFileUrl(this.currentFile.path);
		this.audio.play();
	}

	moveToNext(step = 1)
	{
		this.idx = this.getNextAudioFileIdx(this.idx + step, step);
		this.startPlay();
	}

	getNextAudioFileIdx(start, delta)
	{
		if (this.files == null)
		{
			return -1;
		}
		var idx = start;
		do
		{
			var ext = this.files[idx].name.split('.').pop().toLowerCase();
			if (ext == "mp3" || ext == "wav" || ext == "ogg" || ext == "wma" || ext == "m4a")
				return idx;
			idx = (idx + delta + this.files.length) % this.files.length;
		}
		while(idx != start);
		return -1;
	}

	assignFiles(files, cover = null)
	{
		this.files = files;
		for (var i = 0; i < this.files.length; i++)
			this.files[i].cover = cover;
		this.idx = this.getNextAudioFileIdx(0, 1);
	}

	isCurrentPath(path)
	{
		if (this.currentFile != null)
		{
			if (this.currentFile.path.includes(path))
				return true;
		}
		if (mCurrentPlaying != null && mCurrentPlaying.path != null && mCurrentPlaying.state == "PLAY")
		{
			if(mCurrentPlaying.path.includes(path))
				return true;
		}
		return false;
	}
}
const Separator = "/";
const PlayerProgressSteps = 300;

var apiTrigger = new APIHandler();
var apiMediaServer = new APIHandler();
var apiSwitch = new APIHandler();
var apiScenes = new APIHandler();
var apiAction = new APIHandler();
var apiUser = new APIHandler();
var apiInformation = new APIHandler();
var mPlayback = "remote";
var mLocalPlayer = new LocalPlayer();
var mMediaCenter = '';
var mPath = '';
var mPathObj = null;
var mUserList;
var mSelectedUser;
var mFiles;
var mPlaylists;
var mPlaylistContent;
var mSwitchesCache = '';
var mCurrentPlaying = null;
var mCurrentPlayingToPlaylist = null;

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
var htmlPlsBack;
var htmlPlayPause;
var htmlPlayDlgTitle;
var htmlPlayDlgArtist;
var htmlPlayDlgAlbum;
var htmlPlayDlgPlayImg;
var htmlPlayDlgInTrack;
var htmlPlayDlgDuration;
var htmlPlayDlgInTrackProgress;
var htmlPlayDlgVolume;


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
	htmlPlsBack = document.getElementById('playlist_back');
	htmlPlayPause = document.getElementById('play_pause');
	htmlPlayDlgInfos = document.getElementById('player_dlg_infos');
	htmlPlayDlgTitle = document.getElementById('player_dlg_title');
	htmlPlayDlgArtist = document.getElementById('player_dlg_artist');
	htmlPlayDlgAlbum = document.getElementById('player_dlg_album');
	htmlPlayDlgPlayImg = document.getElementById('player_dlg_play_btn');
	htmlPlayDlgInTrack = document.getElementById('player_dlg_in_track');
	htmlPlayDlgDuration = document.getElementById('player_dlg_duration');
	htmlPlayDlgInTrackProgress = document.getElementById('player_dlg_progress');
	htmlPlayDlgVolume = document.getElementById('volume_input');

	readSetting();
	placeDialogs();
	updateVisibleCard();

	htmlPlayDlgVolume.addEventListener('input', setVolume);
	htmlPlayDlgInTrackProgress.addEventListener('input', setTrackSeekPosition);

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
	mPlayback = window.localStorage.getItem("playback");
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
	var playback = document.getElementById('setting_playback');

	endpoint.value = window.localStorage.getItem("endpoint");
	token.value = window.localStorage.getItem("token");
	appearence.value = window.localStorage.getItem("appearence");
	playback.value = window.localStorage.getItem("playback");
}

function saveSettings()
{
	var endpoint = document.getElementById('setting_endpoint').value;
	var token = document.getElementById('setting_token').value;
	var appearence = document.getElementById('setting_appearence');
	var playback = document.getElementById('setting_playback');
	mMediaCenter = '';
	window.localStorage.setItem("endpoint", endpoint);
	window.localStorage.setItem("token", token);
	window.localStorage.setItem("appearence", appearence.value);
	window.localStorage.setItem("playback", playback.value);
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
	if (row.hasOwnProperty('icon'))
	{
		file += row.icon;
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
		if (row.hasOwnProperty('subbutton') && row.subbutton.length > 0)
		{
			file += '<div class="gradient"></div>';
		}
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

function formatTime(seconds)
{
	var hours = '';
	var addZero = false;
	if (seconds > 60 * 60)
	{
		hours = Math.floor(seconds / (60 * 60));
		seconds -= hours * 60 * 60;
		hours = hours + ':';
		addZero = true;
	}
	var minutes = Math.floor(seconds / 60);
	if (addZero && minutes < 10)
	{
		minutes = '0' + minutes;
	}
	var seconds = (seconds % 60);
	if (seconds < 10)
	{
		seconds = '0' + seconds;
	}
	return hours + minutes + ':' + seconds;
}

function setPlayingInfo(playing)
{
	var text = '';
	var progressWidth = '0%';
	var progressInput = 0;
	var oldPath = null;
	var newPath = null;
	if (mCurrentPlaying)
	{
		oldPath = mCurrentPlaying.path;
	}
	mCurrentPlaying = playing;
	if (mCurrentPlaying)
	{
		newPath = mCurrentPlaying.path;
	}
	if (newPath != oldPath)
	{
		updatePlayingFolder();
	}
	var title = '-';
	var artist = '-';
	var album = '-';
	var inTrack = '-';
	var duration = '-';
	if (playing != null){
		if (playing.artist != null && playing.artist != '')
		{
			text += playing.artist + '<br/>';
			artist = playing.artist;
		}
		if (playing.title != null && playing.title != '')
		{
			text += playing.title + '<br/>';
			title = playing.title;
		}
		if (playing.album != null && playing.album != '')
		{
			album = playing.album;
		}
		else if (playing.file != null && playing.file != '')
		{
			album = playing.file;
		}
		else if (playing.radio != null)
		{
			album = playing.radio;
		}
		if (playing.artist == null || playing.title == null || playing.artist == '' || playing.title == '')
			text += playing.file + '<br/>';
		if (playing.state == "PLAY")
		{
			htmlPlayPause.src = 'player/pause.png';
			htmlPlayDlgPlayImg.src = 'player/pause.png';
			htmlPlayDlgPlayImg.classList.remove('player_dlg_img_play');
		}
		else
		{
			htmlPlayPause.src = 'player/play.png';
			htmlPlayDlgPlayImg.src = 'player/play.png';
			htmlPlayDlgPlayImg.classList.add('player_dlg_img_play');
		}
		if (playing.durationSec > 0)
		{
			progressInput = Math.round(PlayerProgressSteps * playing.inTrackSec / playing.durationSec);
			progressWidth = Math.round(100 * playing.inTrackSec / playing.durationSec) + '%';
			duration = formatTime(playing.durationSec);

		}
		inTrack = formatTime(playing.inTrackSec);
		htmlPlayDlgVolume.value = playing.volume;
		if (htmlPlayDlgInfos.style.display != 'block')
		{
			htmlPlayDlgInfos.style.display = 'block';
			placeDialogs();
		}
	} else {
		htmlPlayPause.src = 'player/play.png';
		text += 'Nothing played';
		if (htmlPlayDlgInfos.style.display != 'none')
		{
			htmlPlayDlgInfos.style.display = 'none';
			placeDialogs();
		}
	}

	if (('mediaSession' in navigator) && playing != null)
	{
		artwork = [];
		if (playing.artwork != null)
		{
			artwork.push({ src: playing.artwork, type: 'image/jpeg' });
		}
		navigator.mediaSession.metadata = new MediaMetadata({
			title: title,
			artist: artist,
			album: album,
			artwork: artwork
		});

		navigator.mediaSession.setActionHandler('play', function() { playerAction("play_pause"); });
		navigator.mediaSession.setActionHandler('pause', function() { playerAction("play_pause"); });
		navigator.mediaSession.setActionHandler('seekbackward', function() { playerAction("seek_backward"); });
		navigator.mediaSession.setActionHandler('seekforward', function() { playerAction("seek_forward"); });
		navigator.mediaSession.setActionHandler('previoustrack', function() { playerAction("previous"); });
		navigator.mediaSession.setActionHandler('nexttrack', function() { playerAction("next"); });
	}

	htmlPlayDlgTitle.innerHTML = title;
	htmlPlayDlgArtist.innerHTML = artist;
	htmlPlayDlgAlbum.innerHTML = album;
	htmlPlayDlgInTrack.innerHTML = inTrack;
	htmlPlayDlgDuration.innerHTML = duration;
	htmlPlayDlgInTrackProgress.value = progressInput;
	htmlPlayInfo.innerHTML = text;
	htmlPlayProgress.style.width = progressWidth;
}

function refreshPlayer(){
	if (mPlayback == "local")
	{
		var playing = null;
		if(mLocalPlayer.currentFile != null)
		{
			var file = mLocalPlayer.currentFile;
			playing = {
				"state" : (mLocalPlayer.audio.paused ? "PAUSE": "PLAY"),
				"inTrackSec" : Math.round(mLocalPlayer.audio.currentTime),
				"durationSec" : Math.round(mLocalPlayer.audio.duration),
				"volume" : mLocalPlayer.audio.volume * 100,
				"path" : mLocalPlayer.currentFile.path
			};
			var info = splitNameToArtistFile(file.name, null, file.name);
			playing.artist = info.artist;
			playing.title = info.title;
			if(file.cover != null && file.cover.length > 0)
				playing.artwork = getFileUrl(file.cover);
		}
		setPlayingInfo(playing);
	}
	else
	{
		getPlaying(function(playing){ setPlayingInfo(playing); });
	}
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
	mPathObj = null;
	if (index == Separator)
	{
		if (mPath.lastIndexOf(Separator) >= 1)
			mPath = mPath.substring(0, mPath.lastIndexOf(Separator));
		else
			mPath = '';
	}
	else
	{
		mPathObj = mFiles[index];
		mPath = getPath(mPathObj);
	}
	window.localStorage.setItem("path", mPath);
	refreshFiles();
}

function play(index)
{
	mFile = mFiles[index];
	if (mPlayback == "local")
	{
		playerAction("stop");

		if(mFile.filetype == "Directory")
		{
			apiMediaServer.call('files', function(files)
			{
				if (checkResult(files, htmlFiles))
				{
					files.sort(function(a, b){return a.name.localeCompare(b.name);});
					mLocalPlayer.assignFiles(files, mFile.cover);
					if (mLocalPlayer.idx < 0)
					{
						showToast("No playable file in folder!");
					}
					else
					{
						mLocalPlayer.startPlay();
					}
					updatePlayingFolder();
				}
			}, {'path': mFile.path});
		}
		else
		{
			var cover = null;
			if (mPathObj != null)
			{
				cover = mPathObj.cover;
			}
			mLocalPlayer.assignFiles(mFiles, cover);
			mLocalPlayer.startPlay(index);
			updatePlayingFolder();
		}
	}
	else
	{
		apiMediaServer.call('play_file', function(result)
		{
			if (checkResult(result)) {
				showToast('Start playing <b>' + mFile.name + '</b>');
			}
		}, {'file': mFile.path});
	}
}

function updatePlayingFolder()
{
	var cases = document.getElementsByClassName("cd-case");
	for(var i = 0; i < cases.length; i++)
	{
		var cdCase = cases[i];
		var cover = cdCase.getElementsByClassName("case-cover")[0];
		var cd = cdCase.getElementsByClassName("cd-disc")[0];
		if(mLocalPlayer.isCurrentPath(cdCase.id))
		{
			cover.classList.add("case-cover-open");
			cd.classList.add("cd-rotate");
		}
		else
		{
			cover.classList.remove("case-cover-open");
			cd.classList.remove("cd-rotate");
		}
	}
	var icons = document.getElementsByClassName("file_button_left");
	for(var i = 0; i < icons.length; i++)
	{
		var icon = icons[i];
		if(mLocalPlayer.isCurrentPath(icon.id))
		{
			icon.classList.remove("invisible");
		}
		else
		{
			icon.classList.add("invisible");
		}
	}
}

function playPlsFile(index)
{
	var plsFile = mPlaylistContent[index];
	if (mPlayback == "local")
	{
		playerAction("stop");
		mLocalPlayer.files = mPlaylistContent;
		mLocalPlayer.startPlay(index);
	}
	else
	{
		apiMediaServer.call('play_file', function(result)
		{
			if (checkResult(result)) {
				showToast('Start playing file <b>' + plsFile.name + '</b>');
			}
		}, {'file': plsFile.path});
	}
}

function extendPls(index){
	mPlaylist = mPlaylists[index];
	hideDialog('playlist');
	var path = null;
	if (mFile != null)
	{
		path = mFile.path;
	}
	else if (mCurrentPlayingToPlaylist != null)
	{
		path = mCurrentPlayingToPlaylist.path
	}
	if (path == null)
	{
		showToast("No file to add to playlist!");
		return;
	}
	apiMediaServer.call('playlist_extend', function(result)
	{
		if (checkResult(result)) {
			showToast('Playlist <b>' + mPlaylist.name + '</b> was extended.');
		}
	}, {'playlist': mPlaylist.name, 'item': path});
}

function addFileToPls(index){
	mFile = null;
	if (index >= 0)
	{
		mFile = mFiles[index];
	}
	else if (mCurrentPlaying != null)
	{
		mCurrentPlayingToPlaylist = mCurrentPlaying;
	}
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

function textInputDlg(title, placeholder, value, labelText, onSubmit)
{
	var headline = document.getElementById("text_input_title");
	var input = document.getElementById("text_input_edit");
	var label = document.getElementById("text_input_label");
	var form = document.getElementById("text_input_form");
	headline.innerHTML = title;
	input.placeholder = placeholder;
	input.value = value;
	label.innerHTML = labelText;
	form.onsubmit = function()
	{
		hideDialog('text_input');
		onSubmit();
		return false;
	};
	showDialog('text_input');
	setTimeout(function() {
		input.focus();
		input.select();
	}, 10);
}

function getPath(file)
{
	if (file.filetype == "Directory")
	{
		return file.path;
	}
	return file.path.substr(0, file.path.lastIndexOf(Separator));
}

function getFileUrl(file)
{
	var current_url = window.location.href.substring(0, window.location.href.lastIndexOf('/')) + "/";
	var final_url = current_url + '/' + mMediaCenter + '/' + file;
	return encodeURI(final_url);
}

function splitNameToArtistFile(name, defaultArtist, defaultTitle)
{
	var result = {"artist": defaultArtist, "title": defaultTitle};
	var split = name.split(" - ");
	if (split.length > 1)
	{
		if(split.length > 2 && split[0].length == 2)
		{
			split.shift();
		}
		result.artist = split[0];
		result.title = split[1];
		for (var i=2; i < split.length; i++)
			result.title += ' - ' + split[i];
	}
	if (result.title.length > 4 && result.title.charAt(result.title.length-4) == '.')
	{
		result.title = result.title.substr(0, result.title.length - 4);
	}
	return result;
}

function cdItem(f, buttons)
{
	var img_src = getFileUrl(f.cover);
	var info = splitNameToArtistFile(f.name, "Various", f.name);
	var medium = "cd";
	var cdClass = "cd-disc";
	var coverClass = 'case-cover';
	if(appearence == 'sepia')
	{
		medium = 'vinyl';
	}
	if(mLocalPlayer.isCurrentPath(f.path))
	{
		cdClass += ' cd-rotate';
		coverClass += ' case-cover-open';
	}
	result = '<div class="album-card">';
	result += ' <div class="cd-case" id="' + f.path + '">';
	result += '  <img class="' + coverClass + '" src="' + img_src + '" alt="Album Cover">';
	result += '  <img class="' + cdClass + '" src="img/' + medium + '.png">';
	result += ' </div>';

	result += ' <div class="album-info">';
	result += '  <div class="marquee">';
	result += '   <h3 class="marquee_inner">' + info.title + '</h3>';
	result += '  </div>';
	result += '  <p>' + info.artist + '</p>';
	result += ' </div>';

	result += ' <div class="album-actions">';
	for(var i = 0; i < buttons.length; i++)
	{
		result += '  <button class="file_button" ' + buttons[i].onclick + '><img src="' + buttons[i].src + '"></button>';
	}
	result += ' </div>';
	result += '</div>';

	return result;
}

function showFiles(files, isSearch)
{
	var content = "";
	var listContent = "";
	var cdItems = "";
	var cdItemsEnabled = !isSearch;
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
		var visibility = "invisible";
		if(mLocalPlayer.isCurrentPath(f.path))
		{
			visibility = "";
		}
		row.icon = '<img id="' + f.path + '" class="file_button_left rotate ' + visibility + '" src="img/cd.png"/>';
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
			{"src": "img/pls.png", "onclick": 'onclick="addFileToPls(\'' + i + '\')"'}
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
		if (cdItemsEnabled && f.cover != null && f.cover.length > 0)
		{
			buttons = [{"src": "img/go.png", "onclick": 'onclick="directoryClick(' + i + ')"'}, ...buttons];
			cdItems += cdItem(f, buttons);
		}
		else
		{
			listContent += fileRow(row, buttons);
		}
	}
	if (files.length == 0)
	{
		listContent += '<div class="headline">';
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
	content += '<div class=album_container>' + cdItems + '</div>';
	content += listContent + '<div style="height:50px"></div>';
	content += '</div>';
	htmlFiles.innerHTML = content;
	if(!isSearch && mPathObj != null && mPathObj.cover != null && mPathObj.cover.length > 0)
	{
		var url = 'url("' + getFileUrl(mPathObj.cover) + '")';
		htmlFiles.style.backgroundImage = 'linear-gradient(to left, rgba(255,255,255,0) 50%, var(--background)),' + url;
	}
	else
	{
		htmlFiles.style.backgroundImage = 'None';
	}
	updateMarquees();
}

function refreshFiles()
{
	htmlFiles.classList.add("list_gone");
	apiMediaServer.call('files', function(files)
	{
		htmlFiles.classList.remove("list_gone");
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
			if (files.error != null)
			{
				var message = files.error.message;
				if (message == null && files.error.class != null){
					message = 'Ups, that shouldn\'t happen... ' + files.error.class;
				}
				showToast("Error: <b>" + message + '</b>');
				if (mPath != "")
				{
					mPath = "";
					refreshFiles();
				}
			}	
		}
	}, {'path': mPath});
}

function plsClick(index){
	mPlaylist = mPlaylists[index];
	if (mPlayback == "local")
	{
		apiMediaServer.call('playlist_content', function(result)
			{
				if (checkResult(result))
				{
					mLocalPlayer.files = result;
					mLocalPlayer.idx = mLocalPlayer.getNextAudioFileIdx(0, 1);
					if (mLocalPlayer.idx < 0)
					{
						showToast("No playable file in folder!");
					}
					else
					{
						mLocalPlayer.startPlay();
					}
				}
			}, {'playlist': mPlaylist.name});		
	}
	else
	{
		apiMediaServer.call('play_playlist', function(result)
		{
			if (checkResult(result)) {
				showToast('Play playlist <b>' + mPlaylist.name + '</b>');
			}
		}, {'playlist': mPlaylist.name});
	}
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

function showPlaylist(filter)
{
	var content = "";
	var groupName = null;
	var groups = [];
	var hasFilter = filter != "";
	for (var i = 0; i < mPlaylists.length; i++)
	{
		var p = mPlaylists[i];
		if (!hasFilter || p.name.startsWith(filter))
		{
			groupName = p.name.split(" ")[0];
			var doMerge = groups.length == 0 || groups[groups.length-1].name != groupName;
			if (doMerge || hasFilter)
			{
				groups.push({"name": groupName, "count": 1, "pls": p, "index": i});
			}
			else
			{
				groups[groups.length-1].count = groups[groups.length-1].count+1;
			}
		}
	}

	for (var i = 0; i < groups.length; i++)
	{
		var group = groups[i];
		var pls = group.pls;
		if (group.count > 1)
		{
			var onclick = 'onclick="showPlaylist(\'' + group.name + '\')"';
			content += fileRow(
				{"caption": group.name, "onclick": onclick, "bold": true,
				"subrow": {
					"caption": group.count + " Playlists",
					"onclick": onclick
				}},
				[{"src": "img/go.png", "onclick": onclick}]
			);
		}
		else
		{
			content += fileRow(
				{"caption": pls.name, "onclick": 'onclick="plsClick(\'' + group.index + '\')"'},
				[{"src": "img/pls.png", "onclick": 'onclick="showPlsContent(\'' + group.index + '\')"'}]
			);
		}
	}
	content += '<div style="height:50px"></div>';
	content += '</div>';
	htmlPls.innerHTML = content;
	htmlPlsAdd.classList.remove("gone");
	if (hasFilter)
	{
		htmlPlsBack.classList.remove("gone");
	}
	else
	{
		htmlPlsBack.classList.add("gone");
	}
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
			showPlaylist('');
		}
		else
		{
			htmlPlsAdd.classList.add("gone");
			htmlPlsBack.classList.add("gone");
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
	if (mPlayback == "local")
	{
		if (action == "volume")
		{
			mLocalPlayer.audio.volume = parameter.volume / 100;
		}
		if (action == "volume_delta")
		{
			mLocalPlayer.audio.volume += parameter.delta / 100;
		}
		if (action == "stop")
		{
			mLocalPlayer.stop();
			updatePlayingFolder();
		}
		if(action == "next")
		{
			mLocalPlayer.moveToNext(1);
		}
		if(action == "previous")
		{
			mLocalPlayer.moveToNext(-1);
		}
		if(action == "seek")
		{
			mLocalPlayer.audio.currentTime = parameter.seek_time_sec;
		}
		if (action == "play_pause")
		{
			if (mLocalPlayer.audio.paused)
				mLocalPlayer.audio.play();
			else
				mLocalPlayer.audio.pause();
		}
		if (action == "seek_backward")
		{
			mLocalPlayer.audio.currentTime = mLocalPlayer.audio.currentTime - 10;
		}
		if (action == "seek_forward")
		{
			mLocalPlayer.audio.currentTime = mLocalPlayer.audio.currentTime + 10;
		}
		refreshPlayer();
	}
	else
	{
		apiMediaServer.call(action, function(result)
		{
			if (checkResult(result)) {
				refreshPlayer();
			}
		}, parameter);
	}
}

function setVolume()
{
	playerAction('volume', {'volume': htmlPlayDlgVolume.value});
}

function setTrackSeekPosition()
{
	if (mCurrentPlaying != null)
	{
		var sec = Math.round(htmlPlayDlgInTrackProgress.value * mCurrentPlaying.durationSec / PlayerProgressSteps);
		playerAction('seek', {'seek_time_sec': sec});
	}
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
