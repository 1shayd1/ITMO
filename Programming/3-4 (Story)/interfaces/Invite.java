package interfaces;

public interface Invite {
    default void beInvited(){
        System.out.println(" is invited");
    }
}
