package dot.albums;

public class Username {
    public static String encode(String name){
        return name.replace(".", ",").replace("_", "-").substring(1);
    }

    public static String decode(String name){
        return "@"+name.replace(",", ".").replace("-", "_");
    }
}
