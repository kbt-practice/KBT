package model;

import java.util.Random;
import java.util.Scanner;

public class Enhence extends Weapon{
    private double percentage;
    private Scanner scanner;

    public void setName(Scanner scanner) {
        String newName = scanner.nextLine();
        this.name = newName;
        reset();
    }

    public Enhence(String name, int price, Scanner scanner) {
        super(name, price);
        this.percentage = 0.8;
        this.scanner = scanner;
    }

    @Override
    public void getInfo() {
        super.getInfo();
        System.out.println("강화 확률 : " + (int)(percentage*100) + "%");
    }

    public boolean enhance() {
        try {
            while(true) {
                System.out.println("강화 시도 중... (성공률: " + (int)(percentage * 100) + "%)");
                Thread.sleep(1500);

                if (new Random().nextDouble() < percentage) {
                    level++;
                    percentage = Math.max(0.1, percentage - 0.1);
                    System.out.println("강화 성공! (level." + level + ")");
                    System.out.println("계속 강화하기 > Enter    메뉴로 돌아가기 > 아무 문자");
                    String input = scanner.nextLine();
                    if (input.isEmpty()) {
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    reset();
                    System.out.println("강화 실패로 검이 깨졌습니다. 검은 무로 돌아갑니다.");
                    return false;
                }
            }
        } catch (InterruptedException e) {
            System.out.println("강화 중 오류가 발생했습니다 다시 시도해 주세요.");
            throw new RuntimeException(e);
        }
    }

    void reset() {
        level = 0;
        percentage = 0.8;
    }

    void sell() {
        int sellPrice = getSellPrice();
        totalPrice += sellPrice;
        reset();

        System.out.println("판매 완료 : +" + sellPrice + "원");
        System.out.println("전체 잔액 : " + totalPrice + "원");
    }
}
