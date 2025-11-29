import java.util.ArrayList;
import java.util.Arrays;

public class DominoMap {

    private ArrayList<ArrayList<Integer>> map;

    public void printMap(){
        for (ArrayList<Integer> a : map){
            for (int b : a){
                if (b > 0)
                    System.out.print(' ');
                System.out.print(b);
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    public int get(int x, int y){
        ArrayList<Integer> row = map.get(y);
        return row.get(x);
    }

    public int getLength(){
        ArrayList<Integer> row = map.getFirst();
        return row.size();
    }

    public int getWidth(){
        return map.size();
    }

    public void set(int v, int x, int y){
        ArrayList<Integer> row = map.get(y);
        row.remove(x);
        row.add(x, v);
    }

    public void startMap(Die die){

        map = new ArrayList<>();
        map.add(new ArrayList<> (Arrays.asList(-1, -1,       -1,       -1, -1)));
        map.add(new ArrayList<> (Arrays.asList(-1, -1,       -1,       -1, -1)));
        map.add(new ArrayList<> (Arrays.asList(-1, -1, die.getHead(),  -1, -1)));
        map.add(new ArrayList<> (Arrays.asList(-1, -1, die.getTail(),  -1, -1)));
        map.add(new ArrayList<> (Arrays.asList(-1, -1,       -1,       -1, -1)));
        map.add(new ArrayList<> (Arrays.asList(-1, -1,       -1,       -1, -1)));
    }

    public int[] extendMap(){
        int paddingX = 0;
        int paddingY = 0;

        for (int n = 1; n <= 2; n++) {
            // верхняя/нижняя граница
            for (int v : map.get(1))
                if (v != -1) {
                    map.addFirst(getNullRow());
                    paddingY++;
                    break;
                }
            for (int v : map.get(map.size() - 2))
                if (v != -1) {
                    map.addLast(getNullRow());
                    break;
                }

            boolean checkedL = false;
            boolean checkedR = false;

            // лево/право
            for (int i = 0; i < map.size() - 2; i++) {
                ArrayList<Integer> row = map.get(i);
                if (row.get(1) != -1 && !checkedL) {
                    addNullColumL();
                    checkedL = true;
                    paddingX++;
                }

                if (row.get(row.size() - 2) != -1 && !checkedR) {
                    addNullColumR();
                    checkedR = true;
                }
            }
        }
        return new int[]{paddingX, paddingY};
    }

    public ArrayList<Integer> getNullRow(){
        ArrayList<Integer> resultRow = new ArrayList<>();
        ArrayList<Integer> mapRow = map.getLast();
        for (int v : mapRow)
            resultRow.addLast(-1);
        return resultRow;
    }

    public void addNullColumR(){
        for (ArrayList<Integer> row : map)
            row.addLast(-1);
    }

    public void addNullColumL(){
        for (ArrayList<Integer> row : map)
            row.addFirst(-1);
    }

}
