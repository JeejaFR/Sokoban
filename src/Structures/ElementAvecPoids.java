package Structures;

public class ElementAvecPoids<E> implements Comparable<ElementAvecPoids<E>> {
    private final E element;
    private final int poids;

    public ElementAvecPoids(E element, int poids) {
        this.element = element;
        this.poids = poids;
    }

    public E getElement() {
        return element;
    }

    public double getPoids() {
        return poids;
    }

    @Override
    public int compareTo(ElementAvecPoids<E> o) {
        return Integer.compare(this.poids, o.poids);
    }
}
