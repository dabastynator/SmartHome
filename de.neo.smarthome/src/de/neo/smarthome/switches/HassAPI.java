package de.neo.smarthome.switches;

import java.io.Serializable;
import java.util.ArrayList;

import de.neo.remote.rmi.RemoteAble;
import de.neo.remote.rmi.RemoteException;
import de.neo.remote.web.WebField;
import de.neo.remote.web.WebParam;
import de.neo.remote.web.WebRequest;

public interface HassAPI extends RemoteAble{

	@WebRequest(description = "", path = "states", genericClass = HassEntity.class)
	public ArrayList<HassEntity> getEntities(
			@WebParam(name = "Authorization", type = WebParam.Type.Header) String authorization
	) throws RemoteException;
	
	@WebRequest(description = "", path = "services/${type}/turn_${state}", genericClass = HassEntity.class, type = WebRequest.Type.Post, content = "application/json")
	public void setState(
			@WebParam(name = "Authorization", type = WebParam.Type.Header) String authorization,
			@WebParam(name = "entity_id", type = WebParam.Type.Payload) String entityId,
			@WebParam(name = "type", type = WebParam.Type.ReplaceUrl) String type,
			@WebParam(name = "state", type = WebParam.Type.ReplaceUrl) String state);
	
	public static class HassEntity implements Serializable{
		
		@WebField(name = "entity_id")
		public String id;
		
		@WebField(name = "state")
		public String state;

		@WebField(name = "attributes", genericClass = Attributes.class)
		public Attributes attributes;
	}
	
	public static class Attributes implements Serializable{
		
		@WebField(name = "friendly_name")
		public String name;
	}
	
}
