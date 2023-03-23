package Modele;

public class Position {
    int l;
    int c;

    public Position(int l, int c){
        this.l = l;
        this.c = c;
    }

    public int getL(){
        return l;
    }

    public int getC(){
        return c;
    }

    public String affiche(){
        String s = "("+l+","+c+")";
        return s;
    }
}
