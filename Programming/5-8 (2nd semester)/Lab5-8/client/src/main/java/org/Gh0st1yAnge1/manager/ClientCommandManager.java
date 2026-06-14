package org.Gh0st1yAnge1.manager;

import org.Gh0st1yAnge1.client_commands.Command;
import org.Gh0st1yAnge1.client_commands.*;
import org.Gh0st1yAnge1.client_commands.auth.Login;
import org.Gh0st1yAnge1.client_commands.auth.Logout;
import org.Gh0st1yAnge1.client_commands.auth.Register;
import org.Gh0st1yAnge1.client_commands.read.*;
import org.Gh0st1yAnge1.client_commands.write.*;
import org.Gh0st1yAnge1.exceptions.InputCancelledException;
import org.Gh0st1yAnge1.request_and_response.CommandType;
import org.Gh0st1yAnge1.request_and_response.Request;
import org.Gh0st1yAnge1.request_and_response.Response;
import org.Gh0st1yAnge1.utils.RouteBuilder;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClientCommandManager {


    @FunctionalInterface
    public interface RequestSender {
        Response send(Request request) throws IOException, ClassNotFoundException;
    }

    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final InputManager inputManager;
    private final RouteBuilder routeBuilder;

    private final Set<String> scriptStack = new HashSet<>();

    public ClientCommandManager(InputManager inputManager, RouteBuilder routeBuilder) {
        this.inputManager = inputManager;
        this.routeBuilder = routeBuilder;

        // read
        commands.put("average_of_distance", new AverageOfDistance());
        commands.put("help", new Help());
        commands.put("info", new Info());
        commands.put("show", new Show());
        commands.put("count_by_distance", new CountByDistance());
        commands.put("filter_less_than_distance", new FilterLessThanDistance());
        commands.put("check_insert_key", new CheckInsertKey());
        commands.put("check_update_key", new CheckUpdateKey());

        //write
        commands.put("clear", new Clear());
        commands.put("remove_key", new RemoveKey());
        commands.put("remove_greater_key", new RemoveGreaterKey());
        commands.put("insert", new Insert(routeBuilder));
        commands.put("update", new Update(routeBuilder));
        commands.put("replace_if_lower", new ReplaceIfLower(routeBuilder));
        commands.put("remove_greater", new RemoveGreater(routeBuilder));

        //others
        commands.put("execute_script", new ExecuteScript());
        commands.put("exit", new Exit());

        //auth
        commands.put("login", new Login());
        commands.put("logout", new Logout());
        commands.put("register", new Register());

    }

    public Request execute(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;

        Command command = commands.get(commandName);
        if (command == null) {
            System.out.println("Unknown command. Type 'help' to see available commands.");
            return null;
        }

        if (commandName.equals("execute_script")) {
            return new Request(CommandType.EXECUTE_SCRIPT, arg, null);
        }

        if (commandName.equals("insert")) {
            return new Request(CommandType.CHECK_INSERT_KEY, arg, null);
        }

        if (commandName.equals("update")) {
            return new Request(CommandType.CHECK_UPDATE_KEY, arg, null);
        }

        try {
            return command.execute(arg);
        } catch (InputCancelledException ex) {
            System.out.println("Command cancelled.");
        } catch (Exception e) {
            System.out.println("Error while executing command: " + e.getMessage());
        }
        return null;
    }

    public void executeScript(String fileName, RequestSender sender) {
        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("Script file name is required.");
            return;
        }

        try {
            File file = new File(fileName);
            if (!file.exists()) {
                System.out.println("Script file not found: " + fileName);
                return;
            }

            String canonicalPath = file.getCanonicalPath();

            if (scriptStack.contains(canonicalPath)) {
                System.out.println("Recursive script detected, skipping: " + fileName);
                scriptStack.clear();
                return;
            }

            scriptStack.add(canonicalPath);
            Scanner fileScanner = new Scanner(file);
            inputManager.pushScanner(fileScanner);

            System.out.println("Executing script: " + fileName);
            try {
                String line;
                while ((line = inputManager.readline()) != null) {
                    if (line.trim().isEmpty()) continue;
                    System.out.println("> " + line);

                    String[] parts = line.trim().split("\\s+", 2);
                    String commandName = parts[0];
                    String arg = parts.length > 1 ? parts[1] : null;

                    if (commandName.equals("execute_script")) {
                        executeScript(arg, sender);
                        continue;
                    }

                    if (commandName.equals("insert")||commandName.equals("update")) {
                        Request checkRequest;
                        if (commandName.equals("insert")){
                            checkRequest = new Request(CommandType.CHECK_INSERT_KEY, arg, null);
                        } else {
                            checkRequest = new Request(CommandType.CHECK_UPDATE_KEY, arg, null);
                        }

                        Response checkResponse = sender.send(checkRequest);
                        if (checkResponse == null) {
                            System.out.println("Server disconnected during script.");
                            return;
                        }
                        if (checkResponse.success()) {
                            System.out.println(checkResponse.message());
                            try {
                                Request insertRequest = new Insert(routeBuilder).execute(arg);
                                Response insertResponse = sender.send(insertRequest);
                                if (insertResponse == null) {
                                    System.out.println("Server disconnected during script.");
                                    return;
                                }
                                if (insertResponse.message() != null)
                                    System.out.println(insertResponse.message());
                            } catch (InputCancelledException e) {
                                System.out.println("Route building cancelled.");
                            }
                        } else {
                            System.out.println(checkResponse.message());
                        }
                        continue;
                    }

                    Request request = execute(line);
                    if (request == null) continue;

                    Response response = sender.send(request);
                    if (response == null) {
                        System.out.println("Server disconnected during script.");
                        return;
                    }
                    if (response.message() != null) System.out.println(response.message());
                }
            } finally {
                inputManager.popScanner();
                fileScanner.close();
                scriptStack.remove(canonicalPath);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error while executing script: " + e.getMessage());
        }
    }
}
