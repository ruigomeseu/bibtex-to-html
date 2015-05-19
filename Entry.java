import java.util.*;
import Exceptions.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DateFormatSymbols;

public class Entry {

	private String id;
	private String type;
	private HashMap<String, String> properties;

	public Entry() {
	}

	public Entry(String id, String type) {
		this.id = id;
		this.type = type;
		properties = new HashMap<String, String>();
	}

	public void addProperty(String property, String content) {
		content = cleanProperty(content);
		properties.put(property, content);
	}

	public HashMap<String, String> getProperties() {
		return properties;
	}

	private String cleanProperty(String property) {
		property = property.trim();
		if(property.charAt(0) == '{') {
			property = property.substring(1, property.length() - 1);
		}

		return property;
	}

	public String convertToHtml(String property, String content)
	{
		if(property.equals("author")) {
			//TODO: transform author into right format
		} else if (property.equals("month")) {
			content = getMonth(Integer.parseInt(content));
		} else if (property.equals("pages")) {
			content = content.replace("--", "&mdash;");
		} 

		Pattern accute = Pattern.compile("\\\\'\\{([a-zA-Z])\\}");
		Matcher match = accute.matcher(content);

		if(match.find()) {
			content = match.replaceAll("&" + match.group(1) + "acute;");
		}

		return content;
	}

	public String replaceOnTemplate(String template) {
		for (Map.Entry<String, String> property : this.properties.entrySet()) {
			template = template.replace(
				"{{" + property.getKey().toUpperCase() + "}}",
				this.convertToHtml(property.getKey(), property.getValue())
			);

			template = template.replace(
				"{{DISPLAY_" + property.getKey().toUpperCase() + "}}",
				"display: inline;"
			);
		}

		//Hide non-existent properties
		Pattern accute = Pattern.compile("\\{\\{DISPLAY_[A-Z]+\\}\\}");
		Matcher match = accute.matcher(template);

		if(match.find()) {
			template = match.replaceAll("display: none;");
		}

		return template;
	}

	public String getMonth(int month) {
    	return new DateFormatSymbols().getMonths()[month-1];
    }



}