package utilities;

public class Utilities {
    public static String printArray(int[] arr){
        String res="[";
        for (int i: arr){
            res+=i+" ";
        }
        return res+"]";
    }
}
