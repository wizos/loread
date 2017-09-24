package me.wizos.loread.utils;

import android.util.ArrayMap;

import java.util.List;
import java.util.Map;

/**
 * 该类，在使用的时候太麻烦了
 * Created by Wizos on 2017/9/24.
 */

public class Doubler2<E, T> {
    private List<E> listA;
    private List<T> listB;
    private Map<Object, Integer> mapTemp;
    private Map<Object, E> mapBox;
    private DoubleListenter DoubleListenter;
    private Map<String, Object> returnValue;


    private List<E> partlistA;
    private List<T> partlistB;
    private List<E> partListDouble;

    /**
     * 构造函数
     *
     * @param listA 数据量较大的
     * @param listB 数据量较小的
     */
    public Doubler2(List<E> listA, List<T> listB) {
        if (listA == null || listB == null) {
            return;
        }
        this.listA = listA;
        this.listB = listB;
//        if(listA.size()>listB.size()){
//            this.listA = listA;
//            this.listB = listB;
//        }else {
//            this.listA = listB;
//            this.listB = listA;
//        }
    }

    public Map<String, Object> startDeDouble() {
        if (DoubleListenter == null) {
            return null;
        }
        // 第1步(准备工作)，遍历数据量大的一方A，将其比对项目放入Map中，计数为1
        for (E itemA : listA) {
            Object aID = DoubleListenter.onListANext(itemA);
            mapTemp.put(aID, 1);
            mapBox.put(aID, itemA);
        }

        // 第2步，遍历数据量小的一方B。到Map中找，是否含有b中的比对项。有则XX，无则YY
        for (T itemB : listB) {
            Object bID = DoubleListenter.onListBNext(itemB);
            Integer temp = mapTemp.get(bID);
            if (temp != null) { // 存在重复
                mapTemp.put(bID, ++temp);
//                DoubleListener.onFoundItemDouble( mapBox.get( bID ) );
                partListDouble.add(mapBox.get(bID));
            } else { // 不存在重复
//                DoubleListener.onFoundItemB( mapBox.get( bID ) );
                partlistB.add(itemB);
            }
        }
        // 第3步(收尾工作)，遍历没有重复的listA
        for (Map.Entry<Object, Integer> entry : mapTemp.entrySet()) {
            if (entry.getValue() == 1) {
//                DoubleListener.onFoundItemA( mapBox.get(entry.getKey()) );
                partlistA.add(mapBox.get(entry.getKey()));
            }
        }
        returnValue = new ArrayMap<>();
        returnValue.put("partlistA", partlistA);
        returnValue.put("partlistB", partlistB);
        returnValue.put("partlistDouble", partListDouble);
        return returnValue;
    }


    public void setDoubleListenter(DoubleListenter DoubleListenter) {
        this.DoubleListenter = DoubleListenter;
    }


    // 接口（interface）是抽像类的变体。在接口中，所有方法都是抽像的。多继承性可通过实现这样的接口而获得。接口中的所有方法都是抽像的，没有一个有程序体。接口只可以定义static final成员变量。接口的实现与子类相似，除了该实现类不能从接口定义中继承行为。当类实现特殊接口时，它定义（即将程序体给予）所有这种接口的方法。然后，它可以在实现了该接口的类的任何对像上调用接口的方法。由于有抽像类，它允许使用接口名作为引用变量的类型。通常的动态联编将生效。引用可以转换到接口类型或从接口类型转换，instanceof 运算符可以用来决定某对象的类是否实现了接口
    public interface DoubleListenter<E, T> {
        // 获取 ListA item 的比对成员
        Object onListANext(E itemA);

        // 获取 ListB item 的比对成员
        Object onListBNext(T itemB);
//        void onFoundItemA(  E itemTemp );
//        void onFoundItemB(  E itemTemp );
//        void onFoundItemDouble( E itemTemp );
//        void onFoundItemA(  E itemTemp ){}
//        void onFoundItemB(  E itemTemp ){}
//        void onFoundItemDouble( E itemTemp ){}
//        <E,T> void onDouble( List<E> listA, List<T> listB, E itemTemp );
    }

}
