package extendedyoutubeplaylist;

public class SymbolChecker {
    public static boolean isFirstCharSymbol(String str) {
        if (str == null || str.isEmpty()) {
            return false; // 空文字列やnullは記号ではない
        }
        char firstChar = str.charAt(0);
        int type = Character.getType(firstChar);

        return type == Character.OTHER_SYMBOL ||
               type == Character.MATH_SYMBOL ||
               type == Character.CURRENCY_SYMBOL ||
               type == Character.MODIFIER_SYMBOL ||
               type == Character.DASH_PUNCTUATION ||
               type == Character.START_PUNCTUATION ||
               type == Character.END_PUNCTUATION ||
               type == Character.CONNECTOR_PUNCTUATION ||
               type == Character.OTHER_PUNCTUATION;
    }
}
