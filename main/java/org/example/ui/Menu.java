
package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class Menu {

    private final Scanner input;
    private final UserDao userDao;
    private boolean running;

    public Menu() {
        input = new Scanner(System.in);
        userDao = new UserDao();
        running = true;
    }

    public void start() {
        while (running) {
            System.out.println("\n================================");
            System.out.println("          TASK TRACKER");
            System.out.println("================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.println("================================");

            String choice = readText("Choose an option: ");

            switch (choice) {
                case "1":
                    registerAccount();
                    break;

                case "2":
                    loginAccount();
                    break;

                case "0":
                    running = false;
                    System.out.println("Thank you for using Task Tracker!");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }

        input.close();
    }

    private void registerAccount() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        String username = readText("Enter username: ");
        String password = readText("Enter password: ");

        System.out.println("\nSelect Role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");

        String choice = readText("Choose role: ");
        String role;

        switch (choice) {
            case "1":
                role = "TEACHER";
                break;

            case "2":
                role = "STUDENT";
                break;

            default:
                System.out.println("Invalid role selection.");
                return;
        }

        boolean registered = userDao.register(
                username, password, role);

        if (registered) {
            System.out.println("\nAccount registered successfully!");
            System.out.println("Username: " + username);
            System.out.println("Role: " + role);
            System.out.println("Status: PENDING");
            System.out.println("Your account is awaiting approval.");
        }
    }

    private void loginAccount() {
        System.out.println("\n--- LOGIN ---");

        String username = readText("Enter username: ");
        String password = readText("Enter password: ");

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        System.out.println("\nLogin successful!");
        userSession(user);
    }

    private void userSession(User user) {
        boolean loggedIn = true;

        while (loggedIn && running) {
            System.out.println("\n================================");
            System.out.println("           USER SESSION");
            System.out.println("================================");
            System.out.println("Username: " + user.username());
            System.out.println("Role: " + user.roles());
            System.out.println("Status: " + user.status());
            System.out.println("--------------------------------");
            System.out.println("1. View Account Information");
            System.out.println("2. Logout");
            System.out.println("0. Exit Application");
            System.out.println("================================");

            String choice = readText("Choose an option: ");

            switch (choice) {
                case "1":
                    displayAccount(user);
                    break;

                case "2":
                    loggedIn = false;
                    System.out.println("Logged out successfully.");
                    break;

                case "0":
                    loggedIn = false;
                    running = false;
                    System.out.println("Application closed.");
                    break;

                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void displayAccount(User user) {
        System.out.println("\n--- ACCOUNT INFORMATION ---");
        System.out.println("User ID : " + user.id());
        System.out.println("Username: " + user.username());
        System.out.println("Role    : " + user.roles());
        System.out.println("Status  : " + user.status());
    }

    private String readText(String message) {
        while (true) {
            System.out.print(message);
            String value = input.nextLine().trim();

            if (!value.isEmpty()) {
                return value;
            }

            System.out.println("This field cannot be empty.");
        }
    }
}
