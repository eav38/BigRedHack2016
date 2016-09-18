package brh.isabella.bingo;

public class Tuple<X, Y, Z> {
    public X loc;
    public Y name;
    public Z visits;
    public Tuple(X x, Y y, Z z) {
        this.loc = x;
        this.name = y;
        this.visits = z;
    }
} 