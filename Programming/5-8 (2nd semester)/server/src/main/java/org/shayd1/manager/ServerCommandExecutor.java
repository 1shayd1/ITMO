package org.shayd1.manager;

import org.shayd1.audit.AuditProducer;
import org.shayd1.auth.AuthService;
import org.shayd1.client_commands.*;
import org.shayd1.client_commands.auth.Login;
import org.shayd1.client_commands.auth.Logout;
import org.shayd1.client_commands.auth.Register;
import org.shayd1.client_commands.read.*;
import org.shayd1.client_commands.write.*;
import org.shayd1.db.RouteRepository;
import org.shayd1.exceptions.InputCancelledException;
import org.shayd1.request_and_response.CommandType;
import org.shayd1.request_and_response.Request;
import org.shayd1.request_and_response.Response;
import org.shayd1.server_commands.Exit;
import org.shayd1.server_commands.SaveWithPath;
import org.shayd1.server_commands.ServerCommand;
import org.shayd1.server_commands.ServerHelp;

import java.sql.SQLException;
import java.util.*;

public class ServerCommandExecutor {

    private final Map<String, ClientCommand> clientCommands = new LinkedHashMap<>();
    private final Map<String, ServerCommand> serverCommands = new LinkedHashMap<>();
    private final AuditProducer auditProducer;
    private final AuthService authService;
    private final RouteRepository routeRepository;

    private static final Set<CommandType> AUDITED_COMMANDS = Set.of(
            CommandType.INSERT,
            CommandType.UPDATE,
            CommandType.REMOVE_KEY,
            CommandType.REMOVE_GREATER_KEY,
            CommandType.REMOVE_GREATER,
            CommandType.REPLACE_IF_LOWER,
            CommandType.CLEAR,
            CommandType.SHOW
    );

    private static final Set<String> NO_AUTH = Set.of("register", "login", "logout", "help");

    public ServerCommandExecutor(CollectionManager collectionManager,
                                 AuditProducer auditProducer,
                                 AuthService authService,
                                 RouteRepository routeRepository) {
        this.auditProducer = auditProducer;
        this.authService = authService;
        this.routeRepository = routeRepository;

        // read
        clientCommands.put("average_of_distance", new AverageOfDistance(collectionManager));
        clientCommands.put("help", new Help(this));
        clientCommands.put("info", new Info(collectionManager));
        clientCommands.put("show", new Show(collectionManager));
        clientCommands.put("count_by_distance", new CountByDistance(collectionManager));
        clientCommands.put("filter_less_than_distance", new FilterLessThanDistance(collectionManager));
        clientCommands.put("check_insert_key", new CheckInsertKey(collectionManager, routeRepository));
        clientCommands.put("check_update_key", new CheckUpdateKey(collectionManager, routeRepository));

        // write
        clientCommands.put("clear", new Clear(collectionManager, routeRepository));
        clientCommands.put("remove_key", new RemoveKey(collectionManager, routeRepository));
        clientCommands.put("remove_greater_key", new RemoveGreaterKey(collectionManager, routeRepository));
        clientCommands.put("insert", new Insert(collectionManager, routeRepository));
        clientCommands.put("update", new Update(collectionManager, routeRepository));
        clientCommands.put("replace_if_lower", new ReplaceIfLower(collectionManager, routeRepository));
        clientCommands.put("remove_greater", new RemoveGreater(collectionManager, routeRepository));

        // auth
        clientCommands.put("login", new Login(authService));
        clientCommands.put("logout", new Logout());
        clientCommands.put("register", new Register(authService));

        serverCommands.put("exit", new Exit());
        serverCommands.put("save_with_path", new SaveWithPath(collectionManager));
        serverCommands.put("help_server", new ServerHelp(this));
    }

    public Response execute(Request request) throws SQLException {
        String name = request.commandType().toString().toLowerCase();
        ClientCommand command = clientCommands.get(name);
        if (command == null) {
            return new Response(false, "Unknown command. Type 'help' to see available commands");
        }

        Long userId = null;
        if (!NO_AUTH.contains(name)) {
            if (request.login() == null || request.password() == null) {
                return new Response(false, "You must login first! Use: login <login> <password>");
            }
            try {
                userId = authService.login(request.login(), request.password());
                if (userId == null) {
                    return new Response(false, "Authorization failed. Wrong login/password.");
                }
            } catch (SQLException e) {
                return new Response(false, "Database connection error! Server cannot verify your credentials right now.");
            }
        }

        String effectiveArg = request.argument();
        if ((name.equals("register") || name.equals("login")) && request.login() != null) {
            effectiveArg = request.login()
                    + (request.password() != null ? " " + request.password() : "");
        }

        Response response = command.execute(effectiveArg, request.route(), userId);

        if (auditProducer != null && AUDITED_COMMANDS.contains(request.commandType())) {
            auditProducer.sendIfAuditable(
                    request.commandType().name(),
                    request.argument(),
                    response.success(),
                    response.message()
            );
        }

        return response;
    }

    public String execute(String input) {
        if (input == null || input.trim().isEmpty()) return null;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        String arg = parts.length > 1 ? parts[1] : null;

        ServerCommand command = serverCommands.get(commandName);
        if (command == null) {
            System.out.println("Unknown command. Type 'help_server' to see available commands.");
            return null;
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

    public Map<String, ClientCommand> getCommands() {
        return clientCommands;
    }

    public Map<String, ServerCommand> getServerCommands() {
        return serverCommands;
    }
}
