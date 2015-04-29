package tinyjs;

/**
 * @author Alexy
 */
public class TypeContainer<X,Y> {
    public X Item1;
    public Y Item2;

    public TypeContainer() { }
    public TypeContainer(X item1, Y item2) {
        this.Item1 = item1;
        this.Item2 = item2;
    }
}