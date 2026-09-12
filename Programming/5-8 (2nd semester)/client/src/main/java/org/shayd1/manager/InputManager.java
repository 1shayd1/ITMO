package org.shayd1.manager;

import org.shayd1.exceptions.InputCancelledException;
import org.shayd1.model.Coordinates;
import org.shayd1.model.Location;
import org.shayd1.utils.Validator;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.Predicate;

public class InputManager {

    private final Deque<Scanner> scannerStack = new ArrayDeque<>();

    public InputManager(){
        scannerStack.push(new Scanner(System.in));
    }

    private Scanner currentScanner(){
        return scannerStack.peek();
    }

    public String readline(){

        if (isScriptMode()){
            Scanner scanner = currentScanner();
            if (!scanner.hasNextLine()){
                return null;
            }
            return scanner.nextLine().trim();
        }

        StringBuilder buffer = new StringBuilder();

        try{
            while (true){
                int ch = System.in.read();

                if (ch == -1){
                    return null;
                }

                if (ch == 27) {           //ESC
                    System.in.read();     //[
                    System.in.read();     //A/B/C/D
                    continue;
                }

                //Enter
                if (ch == '\n' || ch == '\r'){
                    System.out.println();
                    return buffer.toString().trim();
                }

                //Ctrl+C
                if (ch == 3){
                    System.out.println("Watafa");
                    return null;
                }

                //Ctrl+D
                if (ch == 4){
                    System.out.println("Yooooo, buddy, what's going on?\nU wanted to use 'Ctrl+D'?\nOh nooo, it doesn't works(\nChill out baby)");
                    return null;
                }

                //Ctrl+Z
                if (ch == 26){
                    System.out.println("Pepe shneine");
                    return null;
                }

                //Backspace
                if (ch == 127 || ch == 8){
                    if (buffer.length() > 0){
                        buffer.deleteCharAt(buffer.length()-1);
                        System.out.print("\b \b");
                    }
                    continue;
                }

                buffer.append((char)ch);
            }
        } catch (IOException e){
            return null;
        }
    }

    public void pushScanner(Scanner scanner){
        scannerStack.push(scanner);
    }

    public void popScanner(){
        if (scannerStack.size() > 1){
            scannerStack.pop();
        }
    }

    public boolean isScriptMode(){
        return scannerStack.size() > 1;
    }

    public <T> T readValue(
            String prompt,
            Function<String, T> parser,
            Predicate<T> validator,
            String errormessage
    ){
        while (true){

            if (!isScriptMode()){
                System.out.print(prompt);
            }

            String input = readline();

            if (input == null){
                throw new InputCancelledException("");
            }

            if(errormessage.contains("and have less than 5 digits after the dot")){
                if (input.contains(",")){
                    input = input.replace(',','.');
                }

                if (!input.matches("\\d+(\\.\\d{1,5})?")){
                    System.out.println(errormessage);
                    continue;
                }
            }

            try{
                T value = parser.apply(input);
                if (validator == null || validator.test(value)){
                    return value;
                }
            } catch (Exception ignored){}

            if (isScriptMode()){
                throw new RuntimeException("Invalid value in script.");
            }

            System.out.println(errormessage);
        }
    }
    //Location +

    public String readLocationName(){
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type location name: ");
        }

        String input = readline();

        if (input == null){
            throw new InputCancelledException("");
        }

        if (input.isEmpty()){
            return null;
        }

        return input;
    }

    public Double readLocationDoubleX(){
        return readValue(
                "Enter the 'double' type coordinate X: ",
                Double::parseDouble,
                null,
                "Coordinate X must have type 'double' and have less than 5 digits after the dot."
        );
    }

    public Double readLocationDoubleY(){
        return readValue(
                "Enter the 'float' type coordinate Y: ",
                Double::parseDouble,
                null,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot!"
        );
    }

    public Integer readLocationIntegerZ(){
        return readValue(
                "Enter the 'Integer' type coordinate Z: ",
                Integer::parseInt,
                Validator::validateLocationZ,
                "Coordinate Z must have type 'Integer'."
        );
    }


    //Coordinates +

    public Float readCoordinateFloatX(){
        return readValue(
                "Enter the 'Float' type coordinate X: ",
                Float::parseFloat,
                Validator::validateCoordinateX,
                "Coordinate X must have type 'Float' and have less than 5 digits after the dot."
        );
    }

    public Float readCoordinateFloatY(){
        return readValue(
                "Enter the 'Float' type coordinate Y: ",
                Float::parseFloat,
                Validator::validateCoordinateY,
                "Coordinate Y must have type 'float' and have less than 5 digits after the dot."
        );
    }

    //Route +

    public String readRouteName(){
        return readValue(
                "Enter the Route name: ",
                Function.identity(),
                Validator::validateRouteName,
                "Route name must not be empty!"
        );
    }

    public Float readRouteDistance(){
        return readValue(
                "Enter the 'float' type Route distance: ",
                Float::parseFloat,
                Validator::validateRouteDistance,
                "Route distance must have type 'float'."
        );
    }

    public Coordinates readCoordinates(){
        return new Coordinates(readCoordinateFloatX(), readCoordinateFloatY());
    }

    public Location readLocation(){

        Location location = null;
        if (!isScriptMode()){
            System.out.println("Press 'Enter' to insert null or type 'location' to create Location");
        }

        while(true){
            String line = readline();

            if (line == null){
                throw new InputCancelledException("");
            }
            if (line.trim().isEmpty()){
                return location;
            } else if (line.trim().equals("location")) {
                break;
            } else {
                System.out.println("Press 'Enter' or type 'location'");
            }
        }

        System.out.println("Creating Location...");

        int type = readValue(
                "Choose type '1' or '2':\n1 - (x, y, z)\n2 - (x, y, z, name)\n",
                Integer::parseInt,
                Validator::validateLocationType,
                "Type '1' or '2'"
        );

        switch (type){
            case 1 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ());
            case 2 -> location = new Location(readLocationDoubleX(), readLocationDoubleY(), readLocationIntegerZ(), readLocationName());
        }
        return location;
    }
}