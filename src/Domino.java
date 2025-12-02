
import java.util.ArrayList;
import java.util.Random;


public class Domino {

    private DominoMap map;
    private ArrayList<Die> pool;
    private ArrayList<ArrayList<Die>> playersHands;
    private final int playersAmount;
    private int[] edgePoint1;
    private int[] edgePoint2;


    public Domino(int playersAmount) {
        this.map = new DominoMap();
        this.playersAmount = playersAmount;
    }

    public DominoMap getMap() {
        return map;
    }

    public ArrayList<Die> getPlayerHand(int index) {
        return playersHands.get(index);
    }

    public int getPlayersAmount() {
        return playersAmount;
    }

    public int[] getEdgePoint1() {
        return edgePoint1;
    }

    public int[] getEdgePoint2() {
        return edgePoint2;
    }

    public void makeHands(){
        playersHands = new ArrayList<>();
        for (int i = 0; i < playersAmount; i++) {
            ArrayList<Die> hand = new ArrayList<>();
            Random rand = new Random();
            for (int j = 0; j < 7; j++) {
                int index = rand.nextInt(pool.size());
                hand.add(pool.get(index));
                pool.remove(index);
            }
            playersHands.addLast(hand);
        }
    }

    public void pullDie(int player){
        if (pool.isEmpty())
            throw new ArrayIndexOutOfBoundsException("pool is empty");
        Random rand = new Random();
        int index = rand.nextInt(pool.size());
        playersHands.get(player).addLast(pool.get(index));
        pool.remove(index);
    }

    public void generateDieSet(){
        pool = new ArrayList<Die>();
        for (int i = 0; i < 7; i++)
            for (int j = i; j < 7; j++)
                pool.addLast(new Die(i, j));

    }

    public void startMap(){
        Die curentStartDie = playersHands.getFirst().getFirst();
        int index = 0;

        for (int i = 0; i < playersHands.size(); i++){
            ArrayList<Die> hand = playersHands.get(index);

            for (Die currentDie : hand){
                if (currentDie.getHead() != 0 && currentDie.getTail() !=0) {

                    if (curentStartDie.isDieDouble()) {
                        if (currentDie.isDieDouble() && currentDie.sum() < curentStartDie.sum()) {
                            curentStartDie = currentDie;
                            index = i;
                        }
                    } else if (currentDie.isDieDouble() || currentDie.sum() < curentStartDie.sum()) {
                        curentStartDie = currentDie;
                        index = i;
                    }
                }
            }
        }

        playersHands.get(index).remove(curentStartDie);
        map.startMap(curentStartDie);
        edgePoint1 = new int[]{2, 2};
        edgePoint2 = new int[]{2, 3};
    }

    public void makeMove(int player, int index, int headX, int headY, int paddingX, int paddingY){
        ArrayList<Die> hand = playersHands.get(player);
        placeDie(hand.get(index), headX, headY, paddingX, paddingY);
        hand.remove(index);
    }

    public void placeDie(Die die, int headX, int headY, int paddingX, int paddingY){
        if (isAttachedToEdge(headX, headY) +
                isAttachedToEdge(headX + paddingX, headY + paddingY) > 0)

            if (paddingX * paddingY == 0 && Math.abs(paddingX + paddingY) == 1) // не диагонально
                if (isPossibleToPlace(die.getHead(), headX, headY) +
                        isPossibleToPlace(die.getTail(), headX + paddingX, headY + paddingY) >= 1) {

                    map.set(die.getHead(), headX, headY);
                    map.set(die.getTail(), headX + paddingX, headY + paddingY);

                    int attachingParameter = isAttachedToEdge(headX, headY);
                    if (attachingParameter > 0)
                        switch (attachingParameter){
                            case 1:
                                edgePoint1[0] = headX + paddingX;
                                edgePoint1[1] = headY + paddingY;
                                break;
                            case 2:
                                edgePoint2[0] = headX + paddingX;
                                edgePoint2[1] = headY + paddingY;
                                break;
                        }

                    attachingParameter = isAttachedToEdge(headX + paddingX, headY + paddingY);
                    if (attachingParameter > 0){
                        switch (attachingParameter){
                            case 1:
                                edgePoint1[0] = headX;
                                edgePoint1[1] = headY;
                                break;
                            case 2:
                                edgePoint2[0] = headX;
                                edgePoint2[1] = headY;
                                break;
                        }
                    }

                    int[] padding = map.extendMap();

                    for (int i = 0; i < edgePoint1.length; i++)
                        edgePoint1[i] += padding[i];
                    for (int i = 0; i < edgePoint2.length; i++)
                        edgePoint2[i] += padding[i];

                }else throw new IllegalArgumentException("Illegal Move");
            else throw new IllegalArgumentException("Illegal Move: diagonal placement");
        else throw new IllegalArgumentException("Illegal Move: not on edge");
    }

    public int isAttachedToEdge(int x, int y){
        if (Math.abs(x - edgePoint1[0]) + Math.abs(y - edgePoint1[1]) == 1)
            return 1;
        if (Math.abs(x - edgePoint2[0]) + Math.abs(y - edgePoint2[1]) == 1)
            return 2;
        return 0;
    }

    public int isPossibleToPlace(int value, int x, int y){
        int sum = 0;

        if (map.get(x,y) != -1)
            return -1;

        if (x - 1 > 0) {
            if (map.get(x - 1, y) != -1 && map.get(x - 1, y) != value)
                return -1;
            sum += map.get(x - 1, y) + 1;
        }
        if (y + 1 < map.getWidth() - 1){
            if (map.get(x, y + 1) != -1 && map.get(x, y + 1) != value)
                return -1;
            sum += map.get(x, y + 1) + 1;
        }
        if (x + 1 < map.getLength() - 1) {
            if (map.get(x + 1, y) != -1 && map.get(x + 1, y) != value)
                return -1;
            sum += map.get(x + 1, y) + 1;
        }
        if (y - 1 > 0) {
            if (map.get(x, y - 1) != -1 && map.get(x, y - 1) != value)
                return -1;
            sum += map.get(x, y - 1) + 1;
        }

        if (sum == 0)
            return 0;

        return 1;
    }

    public boolean handIsEmpty(){
        int result = 1;
        for (ArrayList<Die> hand : playersHands)
            result *= hand.size();
        return result == 0;
    }

    public boolean fishHappens(){
        for (Die d : pool)
            if (d.getTail() == map.get(edgePoint1[0], edgePoint1[1]) ||
                d.getTail() == map.get(edgePoint2[0], edgePoint2[1]) ||
                d.getHead() == map.get(edgePoint1[0], edgePoint1[1]) ||
                d.getHead() == map.get(edgePoint2[0], edgePoint2[1]))
                return false;

        for (ArrayList<Die> h : playersHands)
            for (Die d : h)
                if (d.getTail() == map.get(edgePoint1[0], edgePoint1[1]) ||
                        d.getTail() == map.get(edgePoint2[0], edgePoint2[1]) ||
                        d.getHead() == map.get(edgePoint1[0], edgePoint1[1]) ||
                        d.getHead() == map.get(edgePoint2[0], edgePoint2[1]))
                    return false;

        return true;
    }

    public int countPoints(int player){
        int sum = 0;
        ArrayList<Die> hand = playersHands.get(player);
        if (!hand.isEmpty())
            for (Die d : hand){
                int x = d.getTail() + d.getHead();
                if (x == 0)
                    sum += 10;
                sum += x;
            }
        return sum;
    }

}
