package de.neo.smarthome.mediaserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

import de.neo.remote.rmi.RemoteException;
import de.neo.remote.rmi.RMILogger.LogPriority;
import de.neo.smarthome.RemoteLogger;
import de.neo.smarthome.api.PlayerException;
import de.neo.smarthome.api.PlayingBean;
import de.neo.smarthome.api.PlayingBean.STATE;

public class MPlayer extends AbstractPlayer
{

	protected Process mMplayerProcess;
	protected PrintStream mMplayerIn;
	private int mSeekValue;
	private Object mPlayListfolder;
	private PlayerObserver mPlayerObserver;

	public MPlayer(String playListfolder) 
	{
		this.mPlayListfolder = playListfolder;
	}

	protected void writeCommand(String cmd) throws PlayerException 
	{
		if (mMplayerIn == null)
			throw new PlayerException("mplayer is down");
		mMplayerIn.print(cmd);
		mMplayerIn.print("\n");
		mMplayerIn.flush();
	}

	@Override
	public void play(String file) 
	{
		if (mMplayerProcess == null)
			startPlayer();

		if (new File(file).isDirectory()) 
		{
			createPlayList(file);
			mMplayerIn.print("loadlist " + mPlayListfolder + "/playlist.pls\n");
			mMplayerIn.flush();
		}
		else 
		{
			mMplayerIn.print("loadfile \"" + file + "\" 0\n");
			mMplayerIn.flush();
		}
		try 
		{
			writeVolume();
		} 
		catch (PlayerException e) 
		{
		}
		if(mPlayerObserver != null)
		{
			mPlayerObserver.setRadio(null);
		}
		super.play(file);
	}

	private void createPlayList(String file) 
	{
		try
		{
			Process exec = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "find \"" + file + "/\" | sort" });
			PrintStream output = new PrintStream(new FileOutputStream(mPlayListfolder + "/playlist.pls"));
			BufferedReader input = new BufferedReader(new InputStreamReader(exec.getInputStream()));
			BufferedReader error = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
			String line = "";
			while ((line = input.readLine()) != null)
				output.println(line);
			while ((line = error.readLine()) != null)
				RemoteLogger.performLog(LogPriority.ERROR, "Error creating playlist: " + line, "Mediaserver");
			output.close();
			input.close();
			error.close();
		} 
		catch (IOException e) 
		{
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
	}
	
	private static void setAmixerVolum(int volume)
	{
		try 
		{
			String[] amixerArgs = new String[] { "/usr/bin/amixer", "-q", "-M", "sset", "Speaker", volume + "%" };
			Process amixer = Runtime.getRuntime().exec(amixerArgs);
			amixer.waitFor();
			BufferedReader buf = new BufferedReader(new InputStreamReader(amixer.getInputStream()));
			String line = "";
			while ((line=buf.readLine())!=null) 
			{
				RemoteLogger.performLog(LogPriority.WARNING, "Set amixer volume: " + line, "MPlayer");
			}
		} 
		catch (IOException e) 
		{
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		} 
		catch (InterruptedException e) 
		{
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
	}

	protected void startPlayer() 
	{
		try 
		{
			// Set Volume
			setAmixerVolum(mVolume);
			
			String[] args = new String[] { "/usr/bin/mplayer", "-slave", "-idle" };
			mMplayerProcess = Runtime.getRuntime().exec(args);
			// the standard input of MPlayer
			mMplayerIn = new PrintStream(mMplayerProcess.getOutputStream());
			// start player observer
			mPlayerObserver = new PlayerObserver(mMplayerProcess.getInputStream());
			mPlayerObserver.start();
			// set default volume
			mMplayerIn.print("volume " + mVolume + " 1\n");
			mMplayerIn.flush();
			// wait for mplayer to get the new volume
			Thread.sleep(200);
		} 
		catch (IOException e) 
		{
			// throw new PlayerException(e.getMessage());
		} 
		catch (InterruptedException e) 
		{
			// ignore
		}
	}

	@Override
	public void playPause() throws PlayerException 
	{
		writeCommand("pause");
		super.playPause();
	}

	@Override
	public void quit() throws PlayerException 
	{
		writeCommand("quit");
		setAmixerVolum(100);
		mMplayerIn = null;
		mMplayerProcess = null;
		mPlayerObserver = null;
		super.quit();
	}

	@Override
	public void next() throws PlayerException 
	{
		writeCommand("pt_step 1");
		super.next();
	}

	@Override
	public void previous() throws PlayerException 
	{
		writeCommand("pt_step -1");
		super.previous();
	}

	@Override
	public void seekForwards() throws RemoteException, PlayerException 
	{
		if (mSeekValue <= 0)
		{
			mSeekValue = 5;
		}
		else
		{
			mSeekValue = Math.min(300, mSeekValue * 2);
		}
		writeCommand("seek " + mSeekValue + " 0");
	}

	@Override
	public void seekBackwards() throws RemoteException, PlayerException 
	{
		if (mSeekValue >= 0)
		{
			mSeekValue = -5;
		}
		else
		{
			mSeekValue = Math.max(-300, mSeekValue * 2);
		}
		writeCommand("seek " + mSeekValue + " 0");
	}

	@Override
	public void volUp() throws PlayerException 
	{
		mVolume += 3;
		if (mVolume > 100)
			mVolume = 100;
		writeVolume();
	}

	@Override
	public void volDown() throws PlayerException 
	{
		mVolume -= 3;
		if (mVolume < 0)
			mVolume = 0;
		writeVolume();
	}

	private void writeVolume() throws PlayerException 
	{
		writeCommand("volume " + mVolume + " 1");
	}

	@Override
	public void fullScreen(boolean full) throws PlayerException 
	{
		if (full)
			writeCommand("vo_fullscreen 1");
		else
			writeCommand("vo_fullscreen 0");
	}

	@Override
	public void nextAudio() throws PlayerException 
	{
		writeCommand("switch_audio -1");
	}

	@Override
	public void playFromYoutube(String url) throws RemoteException, PlayerException 
	{
		if (mMplayerIn == null)
			startPlayer();
		String[] split = url.split(" ");
		String title = "";
		for (int i = 0; i < split.length - 1; i++)
			title = title + " " + split[i];
		String youtubeStreamUrl = getStreamUrl(YOUTUBE_DL_FILE, split[split.length - 1]);
		writeCommand("loadfile " + youtubeStreamUrl);
	}

	@Override
	public void playPlayList(String pls) throws RemoteException, PlayerException 
	{
		if (mMplayerIn == null) 
		{
			startPlayer();
		}
		File file = new File(pls);
		if (!file.exists())
			throw new PlayerException("playlist " + pls + " does not exist");

		if (lineOfFileStartsWith(pls, "[playlist]") != null) 
		{
			String url = lineOfFileStartsWith(pls, "File");
			url = url.substring(url.indexOf("=") + 1);
			mMplayerIn.print("loadfile " + url + "\n");
		} 
		else
			mMplayerIn.print("loadlist \"" + pls + "\"\n");
		if(mPlayerObserver != null)
		{
			mPlayerObserver.setRadio(file.getName());
		}
		writeVolume();
	}

	private String lineOfFileStartsWith(String file, String prefix) 
	{
		String match = null;
		try 
		{
			BufferedReader reader = new BufferedReader(new FileReader(new File(file)));
			String line = null;
			while ((line = reader.readLine()) != null)
				if (line.startsWith(prefix)) 
				{
					match = line;
					break;
				}
			reader.close();
		} 
		catch (IOException e) 
		{
			RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(), "MPlayer");
		}
		return match;
	}

	class PlayerObserver extends Thread 
	{
		private BufferedReader mInput;
		private PlayingBean mBean;
		private String mRadio;
		

		public PlayerObserver(InputStream stream) 
		{
			mInput = new BufferedReader(new InputStreamReader(stream));
			mBean = new PlayingBean();
		}
		
		void setRadio(String radio)
		{
			mRadio = radio;
			if (mBean != null)
			{
				mBean.mRadio = radio;
			}
		}

		@Override
		public void run() 
		{
			String line = null;
			try {
				while ((line = mInput.readLine()) != null) 
				{
					if (line.startsWith("Playing")) 
					{
						try 
						{
							String file = line.substring(8);
							file = file.substring(0, file.length() - 1);
							mBean = readFileInformations(new File(file));
							mBean.mRadio = mRadio;
						} 
						catch (IOException e) 
						{
						}
						String file = line.substring(line.lastIndexOf(File.separator) + 1);
						file = file.substring(0, file.length() - 1);
						mBean.mFile = file.trim();
						mSeekValue = 0;
					}
					if (line.startsWith(" Title: ") && mBean.mTitle == null)
					{
						String title = line.substring(8).trim();
						if(title.length() > 0)
							mBean.mTitle = line.substring(8).trim();
					}
					if (line.startsWith(" Artist: ") && mBean.mArtist == null)
					{
						String artist = line.substring(9).trim();
						if(artist.length() > 0)
							mBean.mArtist = artist; 
					}
					if (line.startsWith(" Album: "))
					{
						String album = line.substring(8).trim();
						if(album.length() > 0)
							mBean.mAlbum = album;
					}
					if (line.equals("Starting playback...")) {
						mBean.mState = PlayingBean.STATE.PLAY;
						loadThumbnail(mBean);
						informPlayingBean(mBean);
					}
					if (line.startsWith("ICY Info")) 
					{
						mBean.parseICYInfo(line);
						mBean.mState = STATE.PLAY;
						mBean.mRadio = mRadio;
						loadThumbnail(mBean);
						informPlayingBean(mBean);
						mSeekValue = 0;
					}
					int indexOf = line.indexOf("of");
					int indexA = line.indexOf("A:");
					if (indexA >= 0 && indexOf >= 0)
					{
						try
						{
							String[] splitA = line.substring(indexA + 2).trim().split(" ");
							String[] splitOf = line.substring(indexOf + 2).trim().split(" ");
							if (splitA.length > 3 && splitOf.length > 2)
							{
								mBean.mInTrackSec = (int)Double.parseDouble(splitA[0]);
								mBean.mDurationSec = (int)Double.parseDouble(splitOf[0]);
								if (mPlayingBean != null)
								{
									mBean.mState = mPlayingBean.mState;	
								}
								informPlayingBean(mBean);
							}
						}
						catch (Exception e)
						{
							// Ignore failing duration on track timing
						}
					}
				}
				mBean.mVolume = mVolume;
				mBean.mState = PlayingBean.STATE.DOWN;
				informPlayingBean(mBean);
			} 
			catch (IOException e) 
			{
				RemoteLogger.performLog(LogPriority.ERROR, e.getClass().getSimpleName() + ": " + e.getMessage(),
						"MPlayerListener");
			}
		}

	}

	@Override
	public void setPlayingPosition(int second) throws RemoteException, PlayerException 
	{
		writeCommand("seek " + second + " 2");
	}

	@Override
	public void useShuffle(boolean shuffle) throws RemoteException, PlayerException 
	{
		throw new PlayerException("shuffle is not supported jet.");
	}

	@Override
	public void setVolume(int volume) throws RemoteException, PlayerException 
	{
		mVolume = volume;
		if (mPlayingBean != null)
			mPlayingBean.mVolume = volume;
		writeVolume();
	}
}
