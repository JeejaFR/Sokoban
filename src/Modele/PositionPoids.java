package Modele;

public class PositionPoids implements Comparable<PositionPoids> {
    int l;
    int c;
    int poids;

    public PositionPoids(int l, int c, int p){
        this.l = l;
        this.c = c;
        this.poids = p;
    }

    public void setPoids(int p){
        this.poids = p;
    }

    @Override
    public int compareTo(PositionPoids o) {
        if(this.poids < o.poids){
            return -1;
        }else if(this.poids > o.poids){
            return 1;
        }
        return 0;
    }

    public Position getPos(){
        return new Position(l,c);
    }

    public int getL(){
        return l;
    }

    public int getC(){
        return c;
    }

    public String affiche(){
        String s = "("+l+","+c+"), poids : "+poids;
        return s;
    }
}

