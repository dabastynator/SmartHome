package de.neo.smarthome.mobile.util;

import android.os.Parcel;
import android.os.Parcelable;
import de.neo.smarthome.api.IWebAction.BeanAction;
import de.neo.smarthome.api.IWebLEDStrip.BeanLEDStrips;
import de.neo.smarthome.api.IWebMediaServer.BeanFileSystem;
import de.neo.smarthome.api.IWebMediaServer.BeanMediaServer;
import de.neo.smarthome.api.IWebMediaServer.BeanPlaylist;
import de.neo.smarthome.api.IWebMediaServer.BeanPlaylistItem;
import de.neo.smarthome.api.IWebMediaServer.FileType;
import de.neo.smarthome.api.IWebSwitch;
import de.neo.smarthome.api.IWebSwitch.BeanSwitch;
import de.neo.smarthome.api.PlayingBean;

public class ParcelableWebBeans {

	public static class ParcelFileSystemBean extends BeanFileSystem implements Parcelable {

		public ParcelFileSystemBean(Parcel source) {
			setName(source.readString());
			setFileType(FileType.values()[source.readInt()]);
		}

		public ParcelFileSystemBean(BeanFileSystem bean) {
			setName(bean.getName());
			setFileType(bean.getFileType());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeInt(getFileType().ordinal());
		}

		public static final Parcelable.Creator<ParcelFileSystemBean> CREATOR = new Parcelable.Creator<ParcelFileSystemBean>() {

			@Override
			public ParcelFileSystemBean createFromParcel(Parcel source) {
				return new ParcelFileSystemBean(source);
			}

			@Override
			public ParcelFileSystemBean[] newArray(int size) {
				return new ParcelFileSystemBean[size];
			}
		};

	}

	public static PlayingBean readPlayingBean(Parcel source) {
		PlayingBean bean = new PlayingBean();
		bean.setAlbum(source.readString());
		bean.setArtist(source.readString());
		bean.setFile(source.readString());
		bean.setLengthTime(source.readInt());
		bean.setPath(source.readString());
		bean.setRadio(source.readString());
		bean.setStartTime(source.readLong());
		bean.setTitle(source.readString());
		bean.setVolume(source.readInt());
		bean.setState(PlayingBean.STATE.values()[source.readInt()]);
		return bean;
	}

	public static void writePlayingBean(Parcel output, PlayingBean bean) {
		if (bean == null) {
			bean = new PlayingBean();
			bean.setState(PlayingBean.STATE.DOWN);
		}
		output.writeString(bean.getAlbum());
		output.writeString(bean.getArtist());
		output.writeString(bean.getFile());
		output.writeLong(bean.getLengthTime());
		output.writeString(bean.getPath());
		output.writeString(bean.getRadio());
		output.writeLong(bean.getStartTime());
		output.writeString(bean.getTitle());
		output.writeInt(bean.getVolume());
		output.writeInt(bean.getState().ordinal());
	}

	public static class ParcelBeanMediaServer extends BeanMediaServer implements Parcelable {

		public ParcelBeanMediaServer(Parcel source) {
			setName(source.readString());
			setDescription(source.readString());
			setID(source.readString());
			setX(source.readFloat());
			setY(source.readFloat());
			setZ(source.readFloat());
			setCurrentPlaying(readPlayingBean(source));
		}

		public ParcelBeanMediaServer(BeanMediaServer bean) {
			setName(bean.getName());
			setDescription(bean.getDescription());
			setID(bean.getID());
			setX(bean.getX());
			setY(bean.getY());
			setZ(bean.getZ());
			setCurrentPlaying(bean.getCurrentPlaying());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeString(getDescription());
			dest.writeString(getID());
			dest.writeFloat(getX());
			dest.writeFloat(getY());
			dest.writeFloat(getZ());
			writePlayingBean(dest, getCurrentPlaying());
		}

		public static final Parcelable.Creator<ParcelBeanMediaServer> CREATOR = new Parcelable.Creator<ParcelBeanMediaServer>() {

			@Override
			public ParcelBeanMediaServer createFromParcel(Parcel source) {
				return new ParcelBeanMediaServer(source);
			}

			@Override
			public ParcelBeanMediaServer[] newArray(int size) {
				return new ParcelBeanMediaServer[size];
			}
		};

	}

	public static class ParcelBeanSwitch extends BeanSwitch implements Parcelable {

		public ParcelBeanSwitch(Parcel source) {
			setName(source.readString());
			setDescription(source.readString());
			setID(source.readString());
			setX(source.readFloat());
			setY(source.readFloat());
			setZ(source.readFloat());
			setType(source.readString());
			setState(IWebSwitch.State.values()[source.readInt()]);
		}

		public ParcelBeanSwitch(BeanSwitch bean) {
			setName(bean.getName());
			setDescription(bean.getDescription());
			setID(bean.getID());
			setX(bean.getX());
			setY(bean.getY());
			setZ(bean.getZ());
			setType(bean.getType());
			setState(bean.getState());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeString(getDescription());
			dest.writeString(getID());
			dest.writeFloat(getX());
			dest.writeFloat(getY());
			dest.writeFloat(getZ());
			dest.writeString(getType());
			dest.writeInt(getState().ordinal());
		}

		public static final Parcelable.Creator<ParcelBeanSwitch> CREATOR = new Parcelable.Creator<ParcelBeanSwitch>() {

			@Override
			public ParcelBeanSwitch createFromParcel(Parcel source) {
				return new ParcelBeanSwitch(source);
			}

			@Override
			public ParcelBeanSwitch[] newArray(int size) {
				return new ParcelBeanSwitch[size];
			}
		};

	}

	public static class ParcelBeanLEDStrips extends BeanLEDStrips implements Parcelable {

		public ParcelBeanLEDStrips(Parcel source) {
			setName(source.readString());
			setDescription(source.readString());
			setID(source.readString());
			setX(source.readFloat());
			setY(source.readFloat());
			setZ(source.readFloat());
			setRed(source.readInt());
			setGreen(source.readInt());
			setBlue(source.readInt());
		}

		public ParcelBeanLEDStrips(BeanLEDStrips bean) {
			setName(bean.getName());
			setDescription(bean.getDescription());
			setID(bean.getID());
			setX(bean.getX());
			setY(bean.getY());
			setZ(bean.getZ());
			setRed(bean.getRed());
			setGreen(bean.getGreen());
			setBlue(bean.getBlue());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeString(getDescription());
			dest.writeString(getID());
			dest.writeFloat(getX());
			dest.writeFloat(getY());
			dest.writeFloat(getZ());
			dest.writeInt(getRed());
			dest.writeInt(getGreen());
			dest.writeInt(getBlue());
		}

		public static final Parcelable.Creator<ParcelBeanLEDStrips> CREATOR = new Parcelable.Creator<ParcelBeanLEDStrips>() {

			@Override
			public ParcelBeanLEDStrips createFromParcel(Parcel source) {
				return new ParcelBeanLEDStrips(source);
			}

			@Override
			public ParcelBeanLEDStrips[] newArray(int size) {
				return new ParcelBeanLEDStrips[size];
			}
		};

	}

	public static class ParcelBeanAction extends BeanAction implements Parcelable {

		public ParcelBeanAction(Parcel source) {
			setName(source.readString());
			setDescription(source.readString());
			setID(source.readString());
			setX(source.readFloat());
			setY(source.readFloat());
			setZ(source.readFloat());
			setClientAction(source.readString());
			setIconBase64(source.readString());
			setRunning(source.readInt() > 0);
		}

		public ParcelBeanAction(BeanAction bean) {
			setName(bean.getName());
			setDescription(bean.getDescription());
			setID(bean.getID());
			setX(bean.getX());
			setY(bean.getY());
			setZ(bean.getZ());
			setClientAction(bean.getClientAction());
			setIconBase64(bean.getIconBase64());
			setRunning(bean.isRunning());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeString(getDescription());
			dest.writeString(getID());
			dest.writeFloat(getX());
			dest.writeFloat(getY());
			dest.writeFloat(getZ());
			dest.writeString(getClientAction());
			dest.writeString(getIconBase64());
			dest.writeInt(isRunning() ? 1 : 0);
		}

		public static final Parcelable.Creator<ParcelBeanAction> CREATOR = new Parcelable.Creator<ParcelBeanAction>() {

			@Override
			public ParcelBeanAction createFromParcel(Parcel source) {
				return new ParcelBeanAction(source);
			}

			@Override
			public ParcelBeanAction[] newArray(int size) {
				return new ParcelBeanAction[size];
			}
		};

	}

	public static class ParcelBeanPlsList extends BeanPlaylist implements Parcelable {

		public ParcelBeanPlsList(Parcel source) {
			setName(source.readString());
			setItemCount(source.readInt());
		}

		public ParcelBeanPlsList(BeanPlaylist bean) {
			setName(bean.getName());
			setItemCount(bean.getItemCount());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeInt(getItemCount());
		}

		public static final Parcelable.Creator<ParcelBeanPlsList> CREATOR = new Parcelable.Creator<ParcelBeanPlsList>() {

			@Override
			public ParcelBeanPlsList createFromParcel(Parcel source) {
				return new ParcelBeanPlsList(source);
			}

			@Override
			public ParcelBeanPlsList[] newArray(int size) {
				return new ParcelBeanPlsList[size];
			}
		};

	}

	public static class ParcelBeanPlsItem extends BeanPlaylistItem implements Parcelable {

		public ParcelBeanPlsItem(Parcel source) {
			setName(source.readString());
			setPath(source.readString());
		}

		public ParcelBeanPlsItem(BeanPlaylistItem bean) {
			setName(bean.getName());
			setPath(bean.getPath());
		}

		@Override
		public int describeContents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			dest.writeString(getName());
			dest.writeString(getPath());
		}

		public static final Parcelable.Creator<ParcelBeanPlsItem> CREATOR = new Parcelable.Creator<ParcelBeanPlsItem>() {

			@Override
			public ParcelBeanPlsItem createFromParcel(Parcel source) {
				return new ParcelBeanPlsItem(source);
			}

			@Override
			public ParcelBeanPlsItem[] newArray(int size) {
				return new ParcelBeanPlsItem[size];
			}
		};

	}

}
