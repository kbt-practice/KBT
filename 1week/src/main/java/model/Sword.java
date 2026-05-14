package model;

import java.util.Scanner;

public class Sword extends Enhance {
    public Sword(String name, int price, Scanner scanner) {
        super(name, price, scanner);
    }

    @Override
    public void getInfo() {
        super.getInfo();
        System.out.println("종류 : 검");
    }

    @Override
    public void setName(Scanner scanner) {
        System.out.print("새로운 검 이름을 정해주세요. : ");
        super.setName(scanner);
    }

    @Override
    public void sell() {
        try{
            System.out.print("검을 판매합니다.");
            Thread.sleep(500);
            super.sell();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}