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

		//FIXME author is purified, no need to convertAccents.
		content = this.convertAccents(content);

		return content;
	}

	private String convertAuthor(String author)
	{
		String author_s = "";
		ArrayList<String[]> authors = getAuthors( author );
		//NOTE: String[x] [0]=first, [1]=last, [2]=von, [3]=jr

		if(this.style.equals("chicago")) {
			boolean first = true;
			for(String[] a : authors){
				if(first) author_s = a[1]+", "+a[0];
				else author_s += ", "+a[0]+" "+a[1];
			}
			//System.out.println(getInitials(author_s));
			return author_s;

		} else if (this.style.equals("apa"))
		{
			//Works by a single author should list the author's last name and initials
			int asize = authors.size();
			//one author
			if(asize == 1){
				String[] atemp = authors.get(0);
				if(atemp[1] != null && atemp[0] != null){
					author_s = atemp[1]+", "+getInitials(atemp[0]);
					return author_s;
				}
			}else //two authors
			if(asize == 2){
				String[] atemp1 = authors.get(0);
				String[] atemp2 = authors.get(1);
				if(atemp1[1] != null && atemp2[1] != null){
					author_s = atemp1[1];
					if(atemp1[0]!=null) author_s += ", "+getInitials(atemp1[0]);
					author_s +=", & "+atemp2[1];
					if(atemp2[0]!=null) author_s += ", "+getInitials(atemp2[0]);
					return author_s;
				}
			}else //more than two (with more than seven)
			if( asize > 2){
				boolean first = true;
				int count = 0;
				for(String[] a : authors){
					if(count == 7){
						author_s += ",...";
						return author_s;
					}
					if(count != asize-1){
						if(!first) author_s += ", ";
						else first = false;
						author_s += a[1];
						if(a[0]!=null) author_s +=", "+getInitials(a[0]);
					}else{
						author_s += ", & "+a[1];
						if(a[0]!=null) author_s +=", "+getInitials(a[0]);
					}
					++count;
				}
				return author_s;
			}
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
    		case 0:
    			splitedName = extractFirstVonLast(bibtex_name.trim());
    			if(splitedName != null) output.add(splitedName);
    			break;
    		//It's the 'von Last, First' case.
    		case 1:
    			splitedName = extractVonLast_First(bibtex_name.trim());
    			if(splitedName != null) output.add(splitedName);
    			break;
    		//It's the 'von Last, Jr, First' case.
    		case 2:
    			splitedName = extractVonLast_Jr_First(bibtex_name.trim());
    			if(splitedName != null) output.add(splitedName);
    			break;
    		default:
    			//ERROR cannot have more than 2 commas?
    			break;
    		}
    	}
    	return output;
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
    	return new String[]{purify(first), purify(last.trim()+" "+lastname), purify(von), ""};
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
    	return new String[]{ purify(first), purify(last), purify(von), "" };
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
		return new String[]{purify(first), purify(last), purify(von), purify(jr)};
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

    	return output.toArray(new String[output.size()]);
    }

    /**
     * Cleans each String in array using purify(String.
     * @return a clean String array free of brackets and special characters
     *
     */
    private String[] purify(String[] unpure_text_array){
    	if(unpure_text_array != null){
    		for(int i=0; i<unpure_text_array.length; ++i){
    			unpure_text_array[i] = purify(unpure_text_array[i]);
    		}
    	}
    	return unpure_text_array;
    }

	/**
     * The cleaning consists in trimming, then replacing special
     * characters and then removing the brackets between words in each string in this order.
     * @return a clean String trimmed and free of brackets and special characters
     */
    private String purify(String unpure){
    	if(unpure != null || !unpure.equals("")){
			unpure = convertAccents(unpure.trim());
			//TODO replace only the ones outside a pair of brackets
			unpure = unpure.replaceAll("\\{","").replaceAll("\\}","");
		}
		return unpure;
	}

	private String getInitials(String text){
		String out = "";
		boolean in_name = false;
		for(char c : text.toCharArray()){
			if(c != ' '){
				if(!in_name){
					in_name = true;
					out += c+". ";
				}
			}else{
				in_name = false;
			}
		}
		return out.trim();
	}

	/*
	private String removeOutsideBrackets(String bracketed){
		StringBuffer unbracketed = new StringBuffer();
		boolean inside = false;
		for(int i=0; i<bracketed.length; ++i){
			char currentChar = bracketed.charAt(i);
			switch(currentChar){
			case '}':
				if()
				if(inside)
			case '{':
				if(inside) unbracketed.append(currentChar);
				else inside = true;
			}


			if(currentChar = '{' && !inside) inside = true;
			else{

			} unbracketed.append(currentChar);

		}
		return unbracketed;
	}*/

}