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
    public Boolean belongToFront(int a) {
        return sparse[a] < divIndex;
    }

    // 判断一个元素是否属于后类
    public Boolean belongToLatter(int a) {
        return sparse[a] >= divIndex;
    }

    //将一个元素放到后类
    public void addToLatter(int a) {
        if (belongToFront(a)) {
            swap(sparse[a], divIndex - 1);
            divIndex -= 1;
        } else {
            System.out.println("not belong to front");
        }

    }

    public void addToPrevious(int a) {
        if (belongToLatter(a)) {
            swap(divIndex, sparse[a]);
            divIndex += 1;
        } else {
            System.out.println("not belong to latter");
        }

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

    public Iterator frontBegin() {
        return new Iterator(0);
    }

    public Iterator latterBegin() {
        return new Iterator(divIndex);
    }

    public Iterator frontEnd() {
        return new Iterator(divIndex - 1);
    }

    public Iterator latterEnd() {
        return new Iterator(length - 1);
    }

    public void getFrontBegin(Iterator it) {
        it.index = 0;
    }

    public void getFrontEnd(Iterator it) {
        it.index = divIndex - 1;
    }

    public void getLatterBegin(Iterator it) {
        it.index = divIndex;
    }

    public void getLatterEnd(Iterator it) {
        it.index = length - 1;
    }

    public class Iterator {
        private int index;

        public Iterator(int i) {
            index = i;
        }

        public boolean isFrontBegin() {
            return index == 0;
        }

        public boolean isFrontEnd() {
            return index == divIndex - 1;
        }

        public boolean isLatterBegin() {
            return index == divIndex;
        }

        public boolean isLatterEnd() {
            return index == length - 1;
        }

        public boolean beforeFrontBegin() {
            return index < 0;
        }

        public boolean afterFrontEnd() {
            return index >= divIndex;
        }

        public boolean beforeLatterBegin() {
            return index < divIndex;
        }

        public boolean afterLatterEnd() {
            return index >= length;
        }

        public void previous() {
            --index;
        }

        public void next() {
            ++index;
        }

        public void moveElementToLatterAndGoPrevious() {
            addToLatter(dense[index]);
            if (!beforeFrontBegin()) {
                previous();
            }
        }

        public void moveElementToFrontAndGoNext() {
            addToPrevious(dense[index]);
            if (!afterLatterEnd()) {
                next();
            }
        }

        public void moveElementToFrontAndGoPrevious() {
            addToPrevious(dense[index]);
            if (!beforeLatterBegin()) {
                previous();
            }
        }

        public int getValue() {
            if (index < length && index > -1) {
                return dense[index];
            } else {
                System.out.println("error");

            }
            return -1;
        }

        public int getIndex() {
            return index;
        }
    }

}
