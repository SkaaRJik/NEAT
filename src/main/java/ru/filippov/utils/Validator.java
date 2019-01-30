package ru.filippov.utils;

import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Рус:
 * <p>
 * Проверка полей, на правильность заполнения.
 * <p>
 * Eng:
 * <p>
 * Validator for fields
 */
public class Validator {

    private static String[] notAllowedFileNames = {
      "con", "pm", "aux", "nul"
    };




    /**
     * Рус:
     * <p>
     * Проверка строки на правильность заполнения: должны быть только цифры
     * <p>
     * Eng:
     * <p>
     * Validation of 'Номер истории': there are only numbers in format DD[./ ]MM[./ ]YYYY.
     * @param string - verifiable string
     * @return true - if field is correct, otherwise - false
     */
   public static boolean isOnlyNumbers(String string){
       Pattern p = Pattern.compile("[а-яА-ЯёЁa-zA-Z\\s]", Pattern.UNICODE_CHARACTER_CLASS);
       Matcher m = p.matcher(string);
       if (m.find()) return false;
       return true;
   }

   public static boolean allowedNameOfFile(String string){
       Pattern p = Pattern.compile("[*\\\\|:\"'<>?@/]|(^com[\\d]*$)|(^lpt[\\d]*$)|(^pm$)|(^aux$)|(^nul$)", Pattern.UNICODE_CHARACTER_CLASS);
       //Pattern p = Pattern.compile("(^com[\\d]*$)", Pattern.UNICODE_CHARACTER_CLASS);
       Matcher m = p.matcher(string);
       if (m.find()) return false;
       return true;
   }


   public static boolean checkFullName(String fullname){
       Pattern p = Pattern.compile("[.]", Pattern.UNICODE_CHARACTER_CLASS);
       Matcher m = p.matcher(fullname);
       if(m.find()) return false;
       else {
           StringTokenizer stringTokenizer = new StringTokenizer(fullname);
           if(stringTokenizer.countTokens() != 3) return false;
       }
       return true;
   }


}
