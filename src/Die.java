

public class Die {
    private final int head;
    private final int tale;


    public Die(int head, int tale) {
        this.head = head;
        this.tale = tale;
    }

    public int getHead(){
        return head;
    }

    public int getTail(){
        return tale;
    }

    public boolean isDieDouble(){
        return head == tale;
    }

    public int sum(){
        return head + tale;
    }
}

