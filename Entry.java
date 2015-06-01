import java.util.*;
import Exceptions.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DateFormatSymbols;

public class Entry {

	private String id;
	private String type;
	private HashMap<String, String> properties;
	private String style;

	public Entry() {
	}

	public Entry(String id, String type, String style) {
		this.id = id;
		this.type = type;
		this.style = style;
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
			content = convertAuthor(content);
		} else if (property.equals("month")) {
			content = getMonth(Integer.parseInt(content));
		} else if (property.equals("pages")) {
			content = content.replace("--", "&mdash;");
		}

		content = this.convertAccents(content);

		return content;
	}

	private String convertAuthor(String author)
	{
		System.out.println(author);
		ArrayList<String[]> authors = getAuthors( author );

		if(this.style.equals("chicago")) {

			return "SIM";
		} else if (this.style.equals("apa"))
		{
			return this.style;
		}
		return "";
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

    private ArrayList<String[]> getAuthors( String authorstring ) { //TODO throw exception
    	String[] splited = authorstring.split("and");
    	ArrayList<String[]> output = new ArrayList<String[]>();

    	for(String bibtex_name : authorstring.split("and")){
    		String[] splitedName;
    		//Its a First von Last (no commas)
    		if(!bibtex_name.contains(",")){
    			splitedName = extractFirstVonLast(bibtex_name.trim());
    			if(splitedName != null)
    			System.out.println("First{"+splitedName[0]+"} "
    				+"Last{"+splitedName[1]+"} "
    				+"von{"+splitedName[2]+"} "
    				+"jr{"+splitedName[3]+"}"
    				);
    		}else
    		//The other two cases (w/ commas)
    		if(true);




    		//output.add(new ArrayList<String>(new));
    		//System.out.println(bibtex_name.trim());
    	}
    	return null;
    }

    private String[] extractFirstVonLast(String trimmedbibtexname){

    	String[] split_name = trimmedbibtexname.split(" "); //TODO special cases for split in { }
		/*
    	//Always need a Last name
    	char lachar = split_name[split_name.length-1].trim().charAt(0);
    	if(!Character.isUpperCase(lachar))
    		System.out.println("invalid author field: No valid 'Last' name in string '"+trimmedbibtexname+"'.");//TODO Throw exception
		else{*/
		String lastname = split_name[split_name.length-1].trim();
		//if the last name is nothing at all then we got a problem..
		if(lastname.length() < 1){
			System.out.println("invalid author field: No valid 'Last' name is of size = 0.");//TODO Throw exception
		}else{
			String first = "", last= "", von= "";
			boolean isfirst = true;
			for(int i=0; i+1 < split_name.length; ++i){
				String name = split_name[i].trim();
				if(name.length() > 0){
					//If first is upper case the is First or Last
					if( firstIsUpper(name) ){
						if(isfirst){
							first += name+" ";
						}else{
							last += name+" ";
						}
					//if not upper case start von
					}else{
						isfirst = false;
						von += name+" ";
					}
				}
			}
			//never has JR in this format.
			//trim all (last+lastname) because last can come empty.
			return new String[]{first.trim(), (last.trim()+" "+lastname).trim(), von.trim(), ""};
		}
    	return null;
    }

    private String[] extractVonLastFirst(String trimmedbibtexname){
    	return null;
    }

    private String[] extractVonLastJrFirst(String trimmedbibtexname){
    	return null;
    }

    private boolean firstIsUpper(String trimmedName){
    	char c = trimmedName.charAt(0);
		if( Character.isUpperCase(c) || c == '{' ) return true;
		else return false;
    }



}