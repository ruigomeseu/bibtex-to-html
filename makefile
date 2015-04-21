name = parser
ccred=$(echo -e "\033[0;31m")
ccyellow=$(echo -e "\033[0;33m")
ccend=$(echo -e "\033[0m")

javac: jj compile

jj: jjt
	echo
	javacc $(name).jj

jjt:
	echo
	jjtree $(name).jjt

compile:
	echo
	javac *.java


.PHONY : clean
clean :
	rm -f $(name).jj
	rm -f Bibtex2HtmlConstants.*
	rm -f Bibtex2HtmlTokenManager.*
	rm -f JJTBibtex2HtmlState.*
	rm -f ParseException.*
	#rm -f SimpleNode.*
	rm -f TokenMgrError.* 
	rm -f Bibtex2Html.* 
	rm -f Bibtex2HtmlTreeConstants.* 
	rm -f Node.* 
	rm -f SimpleCharStream.* 
	rm -f Token.*

cleanall:
	rm -f *.java *.jj *.class
