package cpscala.TSolver.Model.Solver.AllDifferent;

public class DichIterator {
    int index;
    int value;
    private Dichotomy dich;

    public DichIterator(Dichotomy d, int i, int v) {
        index = i;
        value = v;
        dich = d;
    }

    public boolean isFrontBegin() {
        return index == 0;
    }

    public boolean isFrontEnd() {
        return index == dich.divIndex - 1;
    }

    public boolean isLatterBegin() {
        return index == dich.divIndex;
    }

    public boolean isLatterEnd() {
        return index == dich.length;
    }

    public void previous() {
        --index;
        value = dich.dense[index];
    }

    public void next() {
        ++index;
        value = dich.dense[index];
    }

    public void moveElementToLatterAndGoPrevious() {
        dich.addToLatter(value);
        previous();
    }

    public void moveElementToFrontAndGoNext() {
        dich.addToPrevious(value);
        next();
    }


}
