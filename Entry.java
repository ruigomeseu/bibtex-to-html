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
		System.out.println();
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
    	ArrayList<String[]> output = new ArrayList<String[]>();

    	for(String bibtex_name : authorstring.split(" and ")){
    		String[] splitedName;
    		int commas_nmr = countTrueCommas(bibtex_name);
    		switch(commas_nmr){
    		//Its a First von Last (no commas).
    		case 0:{
    			splitedName = extractFirstVonLast(bibtex_name.trim());
    			if(splitedName != null)
    			System.out.println("CASE1: First{"+splitedName[0]+"} "
    				+"Last{"+splitedName[1]+"} "
    				+"von{"+splitedName[2]+"} "
    				+"jr{"+splitedName[3]+"}"
    				);
    			}
    			break;
    		//It's the 'von Last, First' case.
    		case 1:{
    			splitedName = extractVonLast_First(bibtex_name.trim());
    			if(splitedName != null)
    			System.out.println("CASE2: First{"+splitedName[0]+"} "
    				+"Last{"+splitedName[1]+"} "
    				+"von{"+splitedName[2]+"} "
    				+"jr{"+splitedName[3]+"}"
    				);
    			}
    			break;
    		//It's the 'von Last, Jr, First' case.
    		case 2:{
    			splitedName = extractVonLast_Jr_First(bibtex_name.trim());
    			if(splitedName != null)
    			System.out.println("CASE3: First{"+splitedName[0]+"} "
    				+"Last{"+splitedName[1]+"} "
    				+"von{"+splitedName[2]+"} "
    				+"jr{"+splitedName[3]+"}"
    				);
    			}
    			break;
    		default:
    			//ERROR cannot have more than 2 commas?
    			break;
    		}
    		//output.add(new ArrayList<String>(new));
    		//System.out.println(bibtex_name.trim());
    	}
    	return null;
    }

    private String[] extractFirstVonLast(String trimmedbibtexname){
    	String[] split_name = trimmedbibtexname.split(" "); //TODO special cases for split in { } and the '~' and '-'
		String lastname = split_name[split_name.length-1].trim();
		String first = "", last= "", von= "";
		boolean isFirst = true;
		//if the last name is nothing at all then we got a problem..
		if(lastname.length() < 1){
			System.out.println("invalid author field: No valid 'Last' name is of size 0.");//TODO Throw exception
		}else{
			for(int i=0; i+1 < split_name.length; ++i){
				String name = split_name[i].trim();
				if(name.length() > 0){
					//If first is upper case the is First or Last
					if( firstIsUpper(name) ){
						if(isFirst){
							first += name+" ";
						}else{
							last += name+" ";
						}
					//if not upper case start von
					}else{
						isFirst = false;
						von += name+" ";
					}
				}
			}
		}
		//never has JR in this format.
		//trim all (last+lastname) because last can come empty.
    	return new String[]{first.trim(), (last.trim()+" "+lastname).trim(), von.trim(), ""};
    }

    private String[] extractVonLast_First(String trimmedbibtexname){
    	String first = "", last= "", von= "";
    	boolean isLast = true;
		String[] split_commas = splitTrueCommas(trimmedbibtexname);

    	if(split_commas.length != 2)
    		System.out.println("Invalid author field: String must only have 1 and only 1 comma that is not between brackets. String: "+trimmedbibtexname);//TODO Throw exception
    	else{
    		//First name is in the end, after the comma
    		first = split_commas[1].trim();
    		//Last and von to be splited
    		String[] split_name = split_commas[0].split(" "); //TODO special cases for split in { }
    		//Last name is always existent
    		last = split_name[split_name.length-1].trim();

    		if(last.length() < 1){
				System.out.println("Invalid author field: 'Last' name can't have length of 0.");//TODO Throw exception
			}else{
				for(int i=split_name.length-2; 0 <= i; --i){
					String name = split_name[i].trim();
					if(name.length() > 0){
						//If is upper case and isLast
						if( firstIsUpper(name) && isLast){	
							last = name+" "+last;
						}else{
						//if not upper case start von and isLast=false
							isLast = false;
							von = name + " " + von;
						}
					}
				}
			}
		}
		//never has JR in this format.
    	return new String[]{first.trim(), last.trim(), von.trim(), ""};
    }

    private String[] extractVonLast_Jr_First(String trimmedbibtexname){
    	String first = "", last = "", von = "", jr = "";
    	boolean isLast = true;
    	String[] split_commas = splitTrueCommas(trimmedbibtexname);

    	if(split_commas.length != 3)
    		System.out.println("Invalid author field: String must only have 2 and only 2 comma that are not between brackets. String: "+trimmedbibtexname);//TODO Throw exception
    	else{
    		//First name is in the end, after the last comma
    		first = split_commas[2].trim();
    		//Jr name is in the middle of the commas
    		jr = split_commas[1].trim();
    		//Last and von to be splited
    		String[] split_name = split_commas[0].split(" "); //TODO special cases for split in { }
    		//Last name is always existent
    		last = split_name[split_name.length-1].trim();
    		if(last.length() < 1){
				System.out.println("Invalid author field: 'Last' name can't have length of 0.");//TODO Throw exception
			}else{
				for(int i=split_name.length-2; 0 <= i; --i){
					String name = split_name[i].trim();
					if(name.length() > 0){
						//If is upper case and isLast
						if( firstIsUpper(name) && isLast){	
							last = name+" "+last;
						}else{
						//if not upper case start von and isLast=false
							isLast = false;
							von = name + " " + von;
						}
					}
				}
			}
    	}
		return new String[]{first, last.trim(), von.trim(), jr};
    }

    private boolean firstIsUpper(String trimmedName){
    	char c = trimmedName.charAt(0);
		if( Character.isUpperCase(c) || c == '{' ) return true;
		else return false;
    }

    //true commas are the ones NOT in between brackets
    private int countTrueCommas(String text){
    	int inbrkts = 0, commas = 0;
    	for(char c : text.toCharArray()){
    		switch(c){
			case '{':
    			inbrkts++;
    			break;
			case '}':
    			inbrkts++;
    			break;
    		case ',':
    			if(inbrkts == 0) commas++;
    			break;
    		default:
    		break;
    		}
    	}
    	return commas;
    }

    //true commas are the ones NOT in between brackets
	private String[] splitTrueCommas(String text){
    	int inbrkts = 0, lastcut = -1;
    	ArrayList<String> output = new ArrayList<String>();
    	char[] charray = text.toCharArray();
    	for(int i = 0; i < text.length(); ++i){
    		switch(text.charAt(i)){
			case '{':
    			++inbrkts;
    			break;
			case '}':
    			if(inbrkts >= 0) --inbrkts;
    			else ;//TODO throws error? or returns -1. '}' was found more times than '{'
    			break;
    		case ',':
    			if(inbrkts == 0){
    				if(i!=0) output.add(text.substring(lastcut+1,i) );
    				else output.add("");
    				lastcut = i+1;
    			}
    			break;
    		default:
    		break;
    		}
    	}
    	output.add(text.substring(lastcut+1,text.length() ) );

    	//for(String s : output) System.out.println("split: '"+s+"'");
    	return output.toArray(new String[output.size()]);
    }



}