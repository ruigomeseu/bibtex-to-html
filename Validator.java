import java.util.*;
import Exceptions.*;

public class Validator {

	public enum PropertyStatus {
	    REQUIRED,
	    OPTIONAL,
	    FOUND
	}
	private enum EditionValues {
		first,
		second,
		third,
		fourth,
		fifth,
		sixth,
		seventh,
		eighth,
		ninth,
		tenth,
		eleventh,
		twelfth
		//TODO add more? more languages?
	}

	private ArrayList<String> ids;
	private HashMap<String, Validator.PropertyStatus> hash;
	private String entryId;

	public Validator() {
	}

	public Validator(ArrayList<String> ids, String entryId, String entryType) {
		this.ids = ids;
		hash = this.getRequiredProperties(entryType);
		this.entryId = entryId;
	}

	public void addId(String id) {
		ids.add(id);
	}

	public ArrayList<String> getIds() {
		return ids;
	}


	public void validatePages(String pages) throws PagesOrderException {
		pages = pages.replace("{", "").replace("}", "");
		if (pages.contains("--")) {
			String[] pagesArray = pages.split("--");
			String pages1 = pagesArray[0];
			String pages2 = pagesArray[1];
			if(Integer.parseInt(pages1) > Integer.parseInt(pages2)) {
				throw new PagesOrderException("Invalid page order on " + entryId + " - " + pages1 + " > " + pages2 + " (Entry ID: " + entryId + ")");
			}
		}
	}
	
	//validate edition(optional) in ordinal form (long form) compares with all values within enumeration EditionValues
	public void validateEditionValue(String edition) throws NonOrdinalEditionException {
		String ed = edition.replace("{", "").replace("}", "");
		ed = ed.toLowerCase();
		
		for( EditionValues val : EditionValues.values()){
			if(val.toString().equals(ed)){
				return;
			}
		}
		throw new NonOrdinalEditionException("Invalid non-ordinal value \""+ed+"\" in edition field (Entry ID: " + entryId + ")");
	}

	public void validateCrossRef(String crossref) throws CrossRefException {
		crossref = crossref.replace("{", "").replace("}", "");

		Boolean mrBoolean = false;
		
		for(int i = 0; i < ids.size(); i++) {
			if(ids.get(i).equals(crossref)) {
				mrBoolean = true;
			}
		}
		if(!mrBoolean) {
			throw new CrossRefException("Cross Reference \"" + crossref + "\" not found (Entry ID: " + entryId + ")");
		}
	}

	public void validateProperty(String type, String property) throws InvalidPropertyException {
		if(hash.get(property) != null) {
			hash.put(property, Validator.PropertyStatus.FOUND);
		} else {
			throw new InvalidPropertyException("Property \"" + property + "\" is not part of the \"" + type + "\" entry (Entry ID: " + entryId + ")");
		}
	}

	public void validateRequiredProperties() throws MissingPropertyException {
		//Loop over the hashmap and throw an error if any required property wasn't found
		for (Map.Entry<String, Validator.PropertyStatus> property : hash.entrySet()) {
			if(property.getValue() == Validator.PropertyStatus.REQUIRED) {
				throw new MissingPropertyException("Property \"" + property.getKey() + "\" is REQUIRED and was not found (Entry ID: " + entryId + ")");
			}
		}
	}
	

	public HashMap<String, PropertyStatus> getRequiredProperties(String type) {
		HashMap<String, PropertyStatus> hash = new HashMap<String, PropertyStatus>();

		if(type.equals("Article")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("journal", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			hash.put("volume", PropertyStatus.REQUIRED);
			//Optional
			hash.put("number", PropertyStatus.OPTIONAL);
			hash.put("pages", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Book")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("publisher", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("edition", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Booklet")) {
			hash.put("title", PropertyStatus.REQUIRED);
			//Optional
			hash.put("author", PropertyStatus.OPTIONAL);
			hash.put("howpublished", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("year", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Conference")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("booktitle", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("editor", PropertyStatus.OPTIONAL);
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("pages", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("organization", PropertyStatus.OPTIONAL);
			hash.put("publisher", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Inbook")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("pages", PropertyStatus.REQUIRED);
			hash.put("publisher", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("type", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("edition", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Incollection")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("booktitle", PropertyStatus.REQUIRED);
			hash.put("publisher", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("editor", PropertyStatus.OPTIONAL);
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("type", PropertyStatus.OPTIONAL);
			hash.put("chapter", PropertyStatus.OPTIONAL);
			hash.put("pages", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("edition", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Inproceedings")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("booktitle", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("editor", PropertyStatus.OPTIONAL);
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("pages", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("organization", PropertyStatus.OPTIONAL);
			hash.put("publisher", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Manual")) {
			hash.put("title", PropertyStatus.REQUIRED);
			//Optional
			hash.put("author", PropertyStatus.OPTIONAL);
			hash.put("organization", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("edition", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("year", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Mastersthesis")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("school", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("type", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Misc")) {
			//Optional
			hash.put("author", PropertyStatus.OPTIONAL);
			hash.put("title", PropertyStatus.OPTIONAL);
			hash.put("howPublished", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("year", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Phdthesis")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("school", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("type", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Proceedings")) {
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("editor", PropertyStatus.OPTIONAL);
			hash.put("volume", PropertyStatus.OPTIONAL);
			hash.put("series", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("publisher", PropertyStatus.OPTIONAL);
			hash.put("organization", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Techreport")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("institution", PropertyStatus.REQUIRED);
			hash.put("year", PropertyStatus.REQUIRED);
			//Optional
			hash.put("type", PropertyStatus.OPTIONAL);
			hash.put("number", PropertyStatus.OPTIONAL);
			hash.put("address", PropertyStatus.OPTIONAL);
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("note", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		} else if (type.equals("Unpublished")) {
			hash.put("author", PropertyStatus.REQUIRED);
			hash.put("title", PropertyStatus.REQUIRED);
			hash.put("note", PropertyStatus.REQUIRED);
			//Optional
			hash.put("month", PropertyStatus.OPTIONAL);
			hash.put("year", PropertyStatus.OPTIONAL);
			hash.put("key", PropertyStatus.OPTIONAL);
			hash.put("crossref", PropertyStatus.OPTIONAL);
		}

		return hash;			
	}
}