package model;

import java.util.Random;
import java.util.Scanner;

public class Axe extends Weapon {
    public Axe(String name, int price) {
        super(name, price);
        this.type = "도끼"; // 출력 메세지 내 종류 설정
    }

    // 홀짝게임 이겼을 때 강화 시도 - 확률 80% 고정
    @Override
    public boolean enhance(Scanner scanner) {
        while (true) {
            System.out.println("홀짝 게임으로 강화합니다!");
            System.out.print("홀(1 입력) 또는 짝(2 입력)을 선택하세요 : ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            int randomNum = new Random().nextInt(100) + 1;
            boolean isOdd = randomNum % 2 != 0;
            boolean userChoseOdd = choice == 1;

            System.out.println("주사위 결과 : " + randomNum + " (" + (isOdd ? "홀" : "짝") + ")");

            if (isOdd == userChoseOdd) {
                levelUp();
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
                System.out.println("강화 실패로 도끼가 부러졌습니다. 도끼는 무로 돌아갑니다.");
                return false;
            }
        }
    }
}