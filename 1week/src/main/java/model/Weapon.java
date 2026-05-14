package model;

public class Weapon {
    protected String name;
    protected int level;
    protected int price;
    protected int totalPrice = 0;

    public Weapon(String name, int price) {
        this.name = name;
        this.level = 0;
        this.price = price;
    }

    public void getInfo() {
        System.out.println("-----현재 무기 상태-----");
        System.out.println("이름 : " + name);
        System.out.println("레벨 : " + level);
        System.out.println("가격 : " + priceSetting()  + "원");
    }

    private int priceSetting() {
        return (int)(price + Math.pow(2, level) * 1000);
    }

    public int getSellPrice() {
        return priceSetting();
    }

    public void getTotalPrice() {
        System.out.println("지갑 : " + totalPrice  + "원");
    }
}