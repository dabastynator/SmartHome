package de.neo.remote.mobile.persistence;

import android.content.Context;
import de.neo.android.persistence.NeoDataBase;

public class RemoteDataBase extends NeoDataBase {

	public RemoteDataBase(Context context) {
		super(context, "de.neo.remote", 1);
	}

}
