package de.neo.rmi.web;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.neo.rmi.api.WebField;

public class JSON {

	public static class JSONObject {

		private Map<String, Object> mValues = new HashMap<>();
		private int mDepth = 0;

		public void put(String key, String value) {
			if (value == null)
				mValues.put(key, "null");
			else
				mValues.put(key, "\"" + value + "\"");
		}

		public void put(String key, boolean value) {
			mValues.put(key, Boolean.toString(value));
		}

		public void put(String key, int value) {
			mValues.put(key, String.valueOf(value));
		}

		public void put(String key, double value) {
			mValues.put(key, String.valueOf(value));
		}

		public void put(String key, float value) {
			mValues.put(key, String.valueOf(value));
		}

		public void put(String key, JSONObject value) {
			if (value == null)
				mValues.put(key, "null");
			else
				mValues.put(key, value);
		}

		public void put(String key, JSONArray value) {
			mValues.put(key, value);
		}

		@Override
		public String toString() {
			if (mValues.size() == 0)
				return "{}";
			StringBuilder sb = new StringBuilder("\n");
			for (int i = 0; i < mDepth; i++)
				sb.append("  ");
			sb.append("{\n");
			int count = mValues.size();
			for (String key : mValues.keySet()) {
				Object value = mValues.get(key);
				if (value instanceof JSONArray)
					((JSONArray) value).mDepth = mDepth + 1;
				if (value instanceof JSONObject)
					((JSONObject) value).mDepth = mDepth + 1;
				for (int i = 0; i <= mDepth; i++)
					sb.append("  ");
				sb.append("\"" + key + "\": ");
				sb.append(value);
				if (--count > 0) {
					sb.append(",");
					sb.append("\n");
				}
			}
			sb.append("\n");
			for (int i = 0; i < mDepth; i++)
				sb.append("  ");
			sb.append("}");
			return sb.toString();
		}
	}

	public static class JSONArray {

		private List<Object> mValues = new ArrayList<>();
		private int mDepth = 0;

		public void add(String value) {
			if (value == null)
				mValues.add("null");
			else
				mValues.add("\"" + value + "\"");
		}

		public void add(int value) {
			mValues.add(value);
		}

		public void add(double value) {
			mValues.add(value);
		}

		public void add(float value) {
			mValues.add(value);
		}

		public void add(JSONObject value) {
			mValues.add(value);
		}

		public void add(JSONArray value) {
			mValues.add(value);
		}

		public void add(boolean value) {
			mValues.add(Boolean.toString(value));
		}

		@Override
		public String toString() {
			if (mValues.size() == 0)
				return "[]";
			StringBuilder sb = new StringBuilder("");

			int count = mValues.size();
			boolean flat = true;
			for (Object value : mValues) {
				if (value instanceof JSONArray) {
					((JSONArray) value).mDepth = mDepth + 1;
					flat = false;
				}
				if (value instanceof JSONObject) {
					((JSONObject) value).mDepth = mDepth + 1;
					flat = false;
				}
				if (count == mValues.size()) {
					sb.append("[");
				}
				sb.append(value);
				if (--count > 0) {
					sb.append(",");					
				}
			}
			if (mValues.size() == 0)
				sb.append("[");
			if (!flat) {
				sb.append("\n");
				for (int i = 0; i < mDepth; i++)
					sb.append("  ");
			}
			sb.append("]");
			return sb.toString();
		}

	}

	public static JSONObject createByException(Exception e) {
		JSONObject result = new JSONObject();
		JSONObject error = new JSONObject();
		error.put("message", e.getMessage());
		error.put("class", e.getClass().getSimpleName());
		result.put("error", error);
		result.put("success", false);
		return result;
	}

	public static Object createByObject(Object o) {
		if (o instanceof List<?>) {
			JSONArray result = new JSONArray();
			List<?> list = (List<?>) o;
			for (Object a : list) {
				Object element = createByObject(a);
				if (element instanceof JSONObject)
					result.add((JSONObject) element);
				if (element instanceof JSONArray)
					result.add((JSONArray) element);
			}
			return result;
		} else if (o instanceof Map<?, ?>) {
			JSONObject result = new JSONObject();
			Map<?, ?> map = (Map<?, ?>) o;
			for (Object key : map.keySet()) {
				Object value = map.get(key);
				Object element = createByObject(map.get(key));
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
			}
			return result;
		} else {
			JSONObject result = new JSONObject();
			for (Field field : o.getClass().getDeclaredFields()) {
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
							Object sub = createByObject(field.get(o));
							if (sub instanceof JSONObject)
								result.put(webField.name(), (JSONObject) sub);
							if (sub instanceof JSONArray)
								result.put(webField.name(), (JSONArray) sub);
						}
					} catch (Exception e) {
						// Ignore
					}
				}
			}
			if (result.mValues.size() > 0)
				return result;
		}
		return null;
	}

}
