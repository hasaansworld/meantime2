package dot.albums;

public class Username {
    public static String encode(String name){
        return name.replace(".", ",").replace("_", "-");
    }

    public static String decode(String name){
        return "@"+name.replace(",", ".").replace("-", "_");
    }
}
