import model.Axe;
import model.Sword;

import java.util.Scanner;

public class Main {
    public static void main() {
        Scanner scanner = new Scanner(System.in);
        Sword sword = null;
        Axe axe = null;

        System.out.println("----- 무 기 선 택 -----");
        System.out.println(" 1. 검");
        System.out.println(" 2. 도끼");
        System.out.println(" 0. 종료하기");
        System.out.print("메뉴를 선택하세요 : ");
        int weaponType = scanner.nextInt();
        scanner.nextLine();

        System.out.print("무기 이름을 정해주세요. : ");
        String inputName = scanner.next();

        if(weaponType == 1) { sword = new Sword(inputName, 0, scanner); }
        else if (weaponType == 2) { axe = new Axe(inputName, 0, scanner); }
        else { System.out.println("잘못된 선택입니다. 종료합니다."); }

        while (true) {
            System.out.println("----- M E N U -----");
            System.out.println(" 1. 무기 정보 보기");
            System.out.println(" 2. 무기 강화하기");
            System.out.println(" 3. 무기 판매하기");
            System.out.println(" 4. 전체 잔액 보기");
            System.out.println(" 0. 종료하기");
            System.out.print("메뉴를 선택하세요 : ");
            int choiceMenu = scanner.nextInt();
            scanner.nextLine();

            if (weaponType == 1) {
                switch (choiceMenu) {
                    case 1:
                        sword.getInfo();
                        break;
                    case 2:
                        sword.enhance();
                        break;
                    case 3:
                        sword.sell();
                        sword.setName(scanner);
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
            } else if (weaponType == 2) {
                switch (choiceMenu) {
                    case 1:
                        axe.getInfo();
                        break;
                    case 2:
                        axe.enhance();
                        break;
                    case 3:
                        axe.sell();
                        axe.setName(scanner);
                        break;
                    case 4:
                        axe.getTotalPrice();
                        break;
                    case 0:
                        System.out.println("종료");
                        scanner.close();
                        return;
                    default:
                        System.out.println("잘못된 번호를 입력했습니다.");
                        break;
                }
            }

            System.out.print("계속 하려면 엔터를 누르세요.");
            scanner.nextLine();
        }
    }
}