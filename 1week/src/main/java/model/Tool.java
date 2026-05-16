package model;

public class Tool {
    private String name; // 도구 이름
    protected int level; // 도구 레벨
    private int price; // 도구 가격

    protected Tool(String name, int price) {
        this.name = name;
        this.level = 0;
        this.price = price;
    }

    // 정보 출력
    protected void getInfo() {
        System.out.println("-----현재 무기 상태-----");
        System.out.println("이름 : " + name);
        System.out.println("레벨 : " + level);
        System.out.println("가격 : " + price);
    }

    // 가격 설정
    protected int calculatePrice() {
        return (int)(Math.pow(2, level) * 1000);
    }
}