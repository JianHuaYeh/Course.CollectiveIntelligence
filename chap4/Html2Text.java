package chap4;

import java.io.*;
import java.util.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

public class Html2Text extends HTMLEditorKit.ParserCallback {
 StringBuffer s;
 ArrayList urllist;
 ArrayList urltextlist;
 boolean inhref = false;

 public Html2Text() {
   urllist = new ArrayList();
   urltextlist = new ArrayList();
 }

 public void parse(Reader in) throws IOException {
   s = new StringBuffer();
   ParserDelegator delegator = new ParserDelegator();
   // the third parameter is TRUE to ignore charset directive
   delegator.parse(in, this, Boolean.TRUE);
 }

 public void handleText(char[] text, int pos) {
   s.append(text).append(" ");
   if (inhref)
       urltextlist.add(new String(text));
 }

 public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes,int position) {
   if (tag == HTML.Tag.A) {
     String url = (String)attributes.getAttribute(HTML.Attribute.HREF);
     urllist.add(url);
     inhref = true;
   }
 }

public void handleEndTag(HTML.Tag tag, int pos) {
    if (tag == HTML.Tag.A) {
        inhref = false;
    }
}


 public String getText() {
   return s.toString();
 }

 public ArrayList getURLs() {
   return urllist;
 }

 public ArrayList getURLTexts() {
   return urltextlist;
 }

 public static void main (String[] args) {
   try {
     // the HTML to convert
     FileReader in = new FileReader("java-new.html");
     Html2Text parser = new Html2Text();
     parser.parse(in);
     in.close();
     System.out.println(parser.getText());
     for (Iterator it=parser.getURLs().iterator(); it.hasNext(); ) {
         System.out.println("URL: "+it.next());
     }
   }
   catch (Exception e) {
     e.printStackTrace();
   }
 }
}

