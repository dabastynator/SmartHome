package de.neo.rmi.api;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class JSONUtils {

	@SuppressWarnings("unchecked")
	public static JSONObject exceptionToJson(Exception e) {
		JSONObject result = new JSONObject();
		JSONObject error = new JSONObject();
		error.put("message", e.getMessage());
		error.put("class", e.getClass().getSimpleName());
		result.put("error", error);
		result.put("success", false);
		return result;
	}

	private static List<Field> getAllFields(List<Field> fields, Class<?> type) {
		fields.addAll(Arrays.asList(type.getDeclaredFields()));

		if (type.getSuperclass() != null) {
			fields = getAllFields(fields, type.getSuperclass());
		}

		return fields;
	}

	@SuppressWarnings("unchecked")
	public static Object objectToJson(Object o) {
		if (o == null)
			return null;
		if (o instanceof List<?>) {
			JSONArray result = new JSONArray();
			List<?> list = (List<?>) o;
			for (Object a : list) {
				Object element = objectToJson(a);
				result.add(element);
			}
			return result;
		} else if (o instanceof Map<?, ?>) {
			JSONObject result = new JSONObject();
			Map<?, ?> map = (Map<?, ?>) o;
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				Object element = objectToJson(map.get(key));
				if (element instanceof JSONObject)
					result.put(key.toString(), (JSONObject) element);
				else if (element instanceof JSONArray)
					result.put(key.toString(), (JSONArray) element);
				else if (value instanceof Integer)
					result.put(key.toString(), (Integer) value);
				else if (value instanceof Float)
					result.put(key.toString(), (Float) value);
				else if (value instanceof Double)
					result.put(key.toString(), (Double) value);
				else if (value instanceof Boolean)
					result.put(key.toString(), (Boolean) value);
				else if (value instanceof String)
					result.put(key.toString(), (String) value);
				else if (value == null)
					result.put(key.toString(), (JSONObject) null);
			}
			return result;
		} else if (o.getClass().isEnum() || o instanceof Number || o instanceof String) {
			return o.toString();
		} else {
			JSONObject result = new JSONObject();
			for (Field field : getAllFields(new ArrayList<Field>(), o.getClass())) {
				WebField webField = field.getAnnotation(WebField.class);
				if (webField != null) {
					field.setAccessible(true);
					try {
						if (field.getType().equals(int.class) || field.getType().equals(Integer.class))
							result.put(webField.name(), field.getInt(o));
						else if (field.getType().equals(float.class) || field.getType().equals(Float.class))
							result.put(webField.name(), field.getFloat(o));
						else if (field.getType().equals(double.class) || field.getType().equals(Double.class))
							result.put(webField.name(), field.getDouble(o));
						else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))
							result.put(webField.name(), field.getBoolean(o));
						else if (field.getType().equals(String.class))
							result.put(webField.name(), (String) field.get(o));
						else {
							Object value = field.get(o);
							Object sub = objectToJson(value);
							if (sub instanceof JSONObject)
								result.put(webField.name(), (JSONObject) sub);
							else if (sub instanceof JSONArray)
								result.put(webField.name(), (JSONArray) sub);
							else if (value == null)
								result.put(webField.name(), (JSONArray) null);
						}
					} catch (Exception e) {
						// Ignore
					}
				}
			}
			return result;
		}
	}

	@SuppressWarnings("unchecked")
	public static Object jsonToObject(Class<?> resultClass, Object json, WebRequest request, WebField webfield)
			throws InstantiationException, IllegalAccessException {
		if (json == null)
			return null;
		Object result = resultClass.newInstance();
		if (result instanceof Collection<?>) {
			Collection<? super Object> resultList = (Collection<? super Object>) result;
			if (!(json instanceof JSONArray))
				throw new IllegalArgumentException("Json should be array for object");
			JSONArray jsonArray = (JSONArray) json;
			Class<?> genericClass = null;
			if (request != null)
				genericClass = request.genericClass();
			if (webfield != null)
				genericClass = webfield.genericClass();
			for (Object o : jsonArray)
				resultList.add(jsonToObject(genericClass, o, null, null));
		} else if (result instanceof Map<?, ?>) {
			Map<String, ? super Object> resultList = (Map<String, ? super Object>) result;
			if (!(json instanceof JSONObject))
				throw new IllegalArgumentException("Json should be array for object");
			JSONObject jsonObject = (JSONObject) json;
			Class<?> genericClass = null;
			if (request != null)
				genericClass = request.genericClass();
			if (webfield != null)
				genericClass = webfield.genericClass();
			for (Object key : jsonObject.keySet())
				resultList.put((String) key, jsonToObject(genericClass, jsonObject.get(key), null, null));
		} else {
			if (!(json instanceof JSONObject))
				throw new IllegalArgumentException("Json should be map for object");
			JSONObject jsonObject = (JSONObject) json;
			for (Field field : getAllFields(new ArrayList<Field>(), resultClass)) {
				WebField webField = field.getAnnotation(WebField.class);
				if (webField != null) {
					field.setAccessible(true);
					try {
						if (field.getType().equals(int.class) || field.getType().equals(Integer.class))
							field.setInt(result, ((Integer) jsonObject.get(webField.name())).intValue());
						else if (field.getType().equals(float.class) || field.getType().equals(Float.class))
							field.setFloat(result, ((Double) jsonObject.get(webField.name())).floatValue());
						else if (field.getType().equals(double.class) || field.getType().equals(Double.class))
							field.setDouble(result, ((Double) jsonObject.get(webField.name())).doubleValue());
						else if (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))
							field.setBoolean(result, ((Boolean) jsonObject.get(webField.name())).booleanValue());
						else if (field.getType().equals(String.class))
							field.set(result, jsonObject.get(webField.name()));
						else if (field.getType().isEnum()) {
							Class cls = field.getType();
							field.set(result, Enum.valueOf(cls, jsonObject.get(webField.name()).toString()));
						} else {
							if (jsonObject.get(webField.name()) instanceof JSONObject) {
								Object child = jsonToObject(field.getType(), jsonObject.get(webField.name()), null,
										webField);
								if (child != null)
									field.set(result, child);
							}
						}
					} catch (Exception e) {
						// Ignore
						System.out.println(e.getMessage());
					}
				}
			}
		}
		return result;
	}
}
