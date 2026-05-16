package model;

import java.util.Random;
import java.util.Scanner;

public class Weapon extends Tool{
    protected double percentage; // 강화 확률
    protected String type = "도구"; // 출력 메세지 내 종류 설정

    protected Weapon(String name, int price) {
        super(name, price);
        this.percentage = 0.8;
    }

    // 무기만의 정보 추가 출력
    @Override
    public void getInfo() {
        super.getInfo();
        System.out.println("종류 : " + type);
        System.out.println("강화 확률 : " + (int)(percentage*100) + "%");
    }

    public boolean enhance(Scanner scanner) {
        return false;
    }

    // 레벨과 확률 처음으로 초기화 -> 단일 객체 생성하고 그 객체로 프로그램이 이어지기 때문
    protected void reset() {
        level = 0;
        percentage = 0.8;
    }

    // 강화된 무기 판매 및 초기화
    public int sell() {
        try {
            System.out.print(type + "을(를) 판매합니다.");
            Thread.sleep(500);

            int sellPrice = calculatePrice();
            reset();
            System.out.println("판매 완료 : +" + sellPrice + "원");
            return sellPrice;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
