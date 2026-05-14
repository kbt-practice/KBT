import model.Sword;

import java.util.Scanner;

public class Main {
    public static void main() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("마검 이름을 정해주세요. : ");
        String inputName = scanner.next();

        Sword sword = new Sword(inputName, 0, scanner);

        while (true) {
            System.out.println("----- M E N U -----");
            System.out.println(" 1. 마검 정보 보기");
            System.out.println(" 2. 마검 강화하기");
            System.out.println(" 3. 마검 판매하기");
            System.out.println(" 4. 전체 잔액 보기");
            System.out.println(" 0. 종료하기");
            System.out.print("메뉴를 선택하세요 : ");
            int choiceMenu = scanner.nextInt();
            scanner.nextLine();

            switch (choiceMenu) {
                case 1:
                    sword.getInfo();
                    break;
                case 2:
                    sword.enhance();
                    break;
                case 3:
                    sword.sell();
                    System.out.print("새로운 마검 이름을 정해주세요. : ");
                    String newName = scanner.nextLine();
                    sword.setName(newName);
                    break;
                case 4:
                    sword.getTotalPrice();
                    break;
                case 0:
                    System.out.println("종료");
                    scanner.close();
                    return;
                default:
                    System.out.println("잘못된 번호를 입력했습니다.");
                    break;
            }

            System.out.print("계속 하려면 엔터를 누르세요.");
            scanner.nextLine();
        }
    }
}