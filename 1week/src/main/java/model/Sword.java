package model;

import java.util.Random;
import java.util.Scanner;

public class Sword extends Weapon {
    private double percentage = getPercentage();

    public Sword(String name, int price) {
        super(name, price);
        this.type = "검"; // 출력 메세지 내 종류 설정
    }

    // 엔터 눌렀을 때 계속 강화 시도 - 점점 확률 줄어들기
    @Override
    public boolean enhance(Scanner scanner) {
        try {
            while (true) {
                System.out.println("강화 시도 중... (성공률: " + (int)(percentage * 100) + "%)");
                Thread.sleep(1500);

                if (new Random().nextDouble() < percentage) {
                    levelUp();
                    percentage = Math.max(0.1, percentage - 0.1);
                    System.out.println("강화 성공! (level." + getLevel() + ")");
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
            System.out.println("강화 중 오류가 발생했습니다. 다시 시도해 주세요.");
            throw new RuntimeException(e);
        }
    }
}