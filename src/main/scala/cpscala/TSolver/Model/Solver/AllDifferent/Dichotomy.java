package cpscala.TSolver.Model.Solver.AllDifferent;

// 二分集 前面的集合叫former，后边的集合叫latter
public class Dichotomy {
    //
    int[] dense;
    int[] sparse;

//    // 第一种类型，假设集合元素初始时不确定
//    // 默认不确定
//    // 从此索引之前（不包括此索引）都属于集合former
//    int previousIndex;
//    // 从此索引之后（不包括此索引）都属于集合latter
//    int nextIndex;

    // 第二种类型，假设集合元素初始时确定，默认都不属于former
    int divIndex;

    // 总长度
    int length;

    public Dichotomy(int size, boolean unknown) {

        // 第二种类型构造函数。
        if (!unknown) {
            dense = new int[size];
            sparse = new int[size];
            length = size;

            for (int i = 0; i < size; ++i) {
                dense[i] = i;
                sparse[i] = i;
            }
            divIndex = 0;
        }
    }


    protected void swap(int i, int j) {
        int tmp = dense[i];
        dense[i] = dense[j];
        dense[j] = tmp;

        sparse[dense[i]] = i;
        sparse[dense[j]] = j;
    }

    // 判断一个元素是否属于前类
    public Boolean belongToFormer(int a) {
        return sparse[a] < divIndex;
    }

    // 判断一个元素是否属于后类
    public Boolean belongToLatter(int a) {
        return sparse[a] >= divIndex;
    }

    //将一个元素放到后类
    public void addToLatter(int a) {
        if (belongToFormer(a)) {
            swap(sparse[a], divIndex - 1);
        }
        divIndex -= 1;
    }

    public void addToPrevious(int a) {
        if (belongToLatter(a)) {
            swap(divIndex - 1, sparse[a]);
        }
        divIndex += 1;
    }

    public int previousSize() {
        return divIndex;
    }

    public int nextSize() {
        return length - divIndex;
    }

    public void allToPrevious() {
        divIndex = length;
    }

    public void allToNext() {
        divIndex = 0;
    }

    public DichIterator frontBegin() {
        return new DichIterator(this, 0, dense[0]);
    }

    public DichIterator frontEnd() {
        return new DichIterator(this, divIndex - 1, dense[divIndex - 1]);
    }

    public void getFrontBegin(DichIterator it) {
        it.index = 0;
        it.value = dense[0];
    }

    public void getFrontEnd(DichIterator it) {
        it.index = divIndex - 1;
        it.value = dense[divIndex - 1];
    }

    public void getLatterBegin(DichIterator it) {
        it.index = divIndex;
        it.value = dense[divIndex];
    }

    public void getLatterEnd(DichIterator it) {
        it.index = length - 1;
        it.value = dense[length - 1];
    }
}
