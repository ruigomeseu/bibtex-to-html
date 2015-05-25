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

		content = this.convertAccents(content);

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
		Pattern nonExistent = Pattern.compile("\\{\\{DISPLAY_[A-Z]+\\}\\}");
		Matcher match = nonExistent.matcher(template);

		if(match.find()) {
			template = match.replaceAll("display: none;");
		}

		return template;
	}

	public String getMonth(int month) {
    	return new DateFormatSymbols().getMonths()[month-1];
    }

    private String convertAccents(String content)
    {
    	Pattern acute = Pattern.compile("\\\\'\\{([a-zA-Z])\\}");
		Matcher acuteMatch = acute.matcher(content);

		if(acuteMatch.find()) {
			content = acuteMatch.replaceAll("&" + acuteMatch.group(1) + "acute;");
		}

		Pattern grave = Pattern.compile("\\\\`\\{([a-zA-Z])\\}");
		Matcher graveMatch = grave.matcher(content);

		if(graveMatch.find()) {
			content = graveMatch.replaceAll("&" + graveMatch.group(1) + "grave;");
		}

		Pattern uml = Pattern.compile("\\\\\"\\{([a-zA-Z])\\}");
		Matcher umlMatch = uml.matcher(content);

		if(umlMatch.find()) {
			content = umlMatch.replaceAll("&" + umlMatch.group(1) + "uml;");
		}

		Pattern tilde = Pattern.compile("\\\\~\\{([a-zA-Z])\\}");
		Matcher tildeMatch = tilde.matcher(content);

		if(tildeMatch.find()) {
			content = tildeMatch.replaceAll("&" + tildeMatch.group(1) + "tilde;");
		}

		Pattern circ = Pattern.compile("\\\\\\^\\{([a-zA-Z])\\}");
		Matcher circMatch = circ.matcher(content);

		if(circMatch.find()) {
			content = circMatch.replaceAll("&" + circMatch.group(1) + "circ;");
		}

		Pattern elig = Pattern.compile("\\\\(ae|AE)");
		Matcher eligMatch = elig.matcher(content);

		if(eligMatch.find()) {
			content = eligMatch.replaceAll("&" + eligMatch.group(1) + "elig;");
		}

		Pattern cedil = Pattern.compile("\\\\c\\{([a-zA-Z])\\}");
		Matcher cedilMatch = cedil.matcher(content);

		if(cedilMatch.find()) {
			content = cedilMatch.replaceAll("&" + cedilMatch.group(1) + "cedil;");
		}



		return content;
    }



}