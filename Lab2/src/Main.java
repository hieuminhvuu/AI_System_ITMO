import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    private static class Pair<A,B> implements Comparable<Pair<Integer, String>>{
        private A first;
        private B second;

        public Pair(A first, B second) {
            super();
            this.first = first;
            this.second = second;
        }

        public int hashCode() {
            int hashFirst = first != null ? first.hashCode() : 0;
            int hashSecond = second != null ? second.hashCode() : 0;

            return (hashFirst + hashSecond) * hashSecond + hashFirst;
        }

        public boolean equals(Object other) {
            if (other instanceof Pair) {
                Pair otherPair = (Pair) other;
                return
                        ((  this.first == otherPair.first ||
                                ( this.first != null && otherPair.first != null &&
                                        this.first.equals(otherPair.first))) &&
                                (  this.second == otherPair.second ||
                                        ( this.second != null && otherPair.second != null &&
                                                this.second.equals(otherPair.second))) );
            }

            return false;
        }

        public String toString()
        {
            return "(" + first + ", " + second + ")";
        }

        public A getFirst() {
            return first;
        }

        public void setFirst(A first) {
            this.first = first;
        }

        public B getSecond() {
            return second;
        }

        public void setSecond(B second) {
            this.second = second;
        }

        @Override
        public int compareTo(Pair<Integer, String> o) {
            return 0;
        }
    }

    private static class Graph{

        public Graph(String filename){

            List<String> lines = Collections.emptyList();

            try{
                lines = Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(String str: lines){
                String[] strs = str.split(" ");
                addEdge(strs[0],strs[1],Integer.parseInt(strs[2]));
            }
        }

        public Set<String> getAdjacent(String node){
            return hashMap.get(node).keySet();
        }

        int edges;
        HashMap<String, HashMap<String,Integer>> hashMap = new HashMap();

        public boolean hasEdge(String a, String b){
            return hashMap.get(a).containsKey(b);
        }

        public boolean hasVertexes(String a){
            return hashMap.containsKey(a);
        }

        public void addEdge(String a, String b, Integer weight){
            if(!hasVertexes(a)) hashMap.put(a,new HashMap<>());
            if(!hasVertexes(b)) hashMap.put(b,new HashMap<>());
            if(!hasEdge(a,b)){
                edges ++;
                hashMap.get(a).put(b,weight);
                hashMap.get(b).put(a,weight);
            }
        }

        //Uninformed search
        public void BFS(String start, String end){
            Deque<String> deque = new ArrayDeque<>();
            HashMap<String,String> parent = new HashMap<>();
            boolean flag = false;
            deque.add(start);
            parent.put(start,null);
            while(deque.size()!=0){
                String current = deque.pop();
                for(String x: getAdjacent(current)){
                    if(!parent.keySet().contains(x)){
                        parent.put(x, current);
                        deque.add(x);
                        if(x.equals(end)){ flag = true;
                            break;}
                    }
                }
                if(flag) break;
            }
            String temp = end;
            while(temp != null){
                System.out.println(temp);
                temp = parent.get(temp);
            }
        }

        public void DFS_loop(String cur, String end,String par, HashMap<String, String> parent){
            parent.put(cur,par);
            if(cur.equals(end)) return;
            for(String i: getAdjacent(cur)){
                if(!parent.containsKey(i)) DFS_loop(i,end,cur,parent);
            }
        }

        public void DFS(String start, String end){
            HashMap<String,String> parent = new HashMap<>();
            DFS_loop(start,end,null,parent);
            String temp = end;
            while(parent.containsKey(temp)){
                System.out.println(temp);
                temp = parent.get(temp);
            }
        }

        public boolean DLS(String start, String end,int maxDepth,Queue<String> path){
            if(start.equals(end)){
                path.add(start);
                return true;
            }
            if(path.size() >= maxDepth){
                return false;
            }
            if(!path.contains(start)){
                path.add(start);
                for(String x: getAdjacent(start)){
                    if(DLS(x, end, maxDepth, path)) return true;
                }
                path.remove(start);
            }
            return false;
        }

        public void DLS_loop(String start, String end){
            Queue<String> queue = new ArrayDeque<>();
            int max_depth = 0;
            while(!DLS(start,end,max_depth,queue)){
                max_depth++;
                queue.clear();
            }
            while(!queue.isEmpty()) System.out.println(queue.remove());
        }

        public void BDS(String start, String end){
            System.out.println("BDS");
            Deque<String> deque1 = new ArrayDeque<>();
            HashMap<String,String> parent1 = new HashMap<>();
            parent1.put(start,null);
            deque1.add(start);
            boolean flag = false;
            String temp = "";
            Deque<String> deque2 = new ArrayDeque<>();
            HashMap<String, String> parent2 = new HashMap<>();
            deque2.add(end);
            parent2.put(end,null);
            while(deque1.size()!=0 && deque2.size()!=0){
                String current = deque1.pop();
                for(String x: getAdjacent(current)){
                    if(!parent1.containsKey(x)){
                        parent1.put(x, current);
                        deque1.add(x);
                        if(parent2.containsKey(x)){ flag = true;
                            temp = x;
                            break;}
                    }
                }

                String current2 = deque2.pop();
                for(String x: getAdjacent(current2)){
                    if(!parent2.containsKey(x)){
                        parent2.put(x,current2);
                        deque2.add(x);
                        if(parent1.containsKey(x)){
                            flag = true;
                            temp = x;
                            break;
                        }
                    }
                }

                if(flag == true){break;}
            }
            String temp1 = temp;
            while(temp1 != null){
                System.out.println(temp1);
                temp1 = parent1.get(temp1);
            }
            temp1 = temp;
            while(temp1!= null){
                System.out.println(temp1);
                temp1 = parent2.get(temp1);
            }
        }

        //Informed Search
        int GDFS(String start, String end){
            PriorityQueue<Integer> pd_open = new PriorityQueue<>();
            HashMap<Integer,String> stored = new HashMap<>();
            Set<String> set_open = new HashSet<>();
            Set<String> set_close = new HashSet<>();
            HashMap<String , String> parent = new HashMap<>();
            int distance = 0;
            pd_open.add(0);
            stored.put(0,start);
            set_open.add(start);
            boolean flag = false;
            while(!pd_open.isEmpty()){
                if(flag) break;
                Integer int_cur = pd_open.remove();
                String cur = stored.get(int_cur);
                set_close.add(cur);
                for(String x: getAdjacent(cur)){
                    if(x.equals(end)){
                        parent.put(x, cur);
                        flag = true;
                        break;
                    }
                    if(!set_open.contains(x) && !set_close.contains(x)){
                        parent.put(x, cur);
                        pd_open.add(hashMap.get(cur).get(x));
                        stored.put(hashMap.get(cur).get(x),x);
                        set_open.add(x);
                    }
                }
            }
            if(flag){
                String temp = end;
                while(parent.containsKey(temp)){
                    distance+=hashMap.get(temp).get(parent.get(temp));
                    temp = parent.get(temp);
                }
            }
            return distance;
        }

        HashMap<String, Integer> h = new HashMap<>();

        public void heuristics( String end){
            h.put(end,0);
            for(String x: hashMap.keySet()){
                if(!x.equals(end)){
                    int d = GDFS(x,end);
                    h.put(x,d);
                }
            }
        }

        public int A_star(String start, String end){
            heuristics(end);
            Set<String> open = new HashSet<>();
            Set<String> close = new HashSet<>();
            HashMap<String, Integer> g = new HashMap<>();
            HashMap<String, String> parent = new HashMap<>();
            g.put(start,0);
            open.add(start);
            parent.put(start,start);
            while(!open.isEmpty()){
                String cur= null;
                for(String x: open){
                    if(cur == null || g.get(x) + h.get(x) < g.get(cur) + h.get(cur)) cur = x;
                }
                if(cur == null){
                    System.out.println("Path does not exist!");
                    return 0;
                }
                if(cur.equals(end)){
                    int distance = 0;
                    while(!Objects.equals(parent.get(cur), cur)){
                        distance += hashMap.get(cur).get(parent.get(cur));
                        System.out.println(cur);
                        cur = parent.get(cur);
                    }
                    System.out.println(start);
                    System.out.println(distance);
                    return distance;
                }
                for(String m: getAdjacent(cur)){
                    int weight = hashMap.get(cur).get(m);
                    if(!open.contains(m) && !close.contains(m)){
                        open.add(m);
                        parent.put(m,cur);
                        g.put(m,g.get(cur) + weight);
                    } else {
                        if(g.get(m) > g.get(cur) + weight){
                            g.remove(m);
                            g.put(m,g.get(cur) + weight);
                            parent.remove(m);
                            parent.put(m,cur);
                            if(close.contains(m)){
                                close.remove(m);
                                open.add(m);
                            }
                        }
                    }
                }
                open.remove(cur);
                close.add(cur);
            }
            System.out.println("No found");
            return 0;
        };
    }

    public static void main(String args[]){
        Graph graph = new Graph("./src/resources/distances.txt");
        //graph.BFS("Вильнюс","Одесса");
        graph.DFS("Вильнюс","Одесса");
//        Queue<String> queue = new ArrayDeque<>();
//        graph.DLS("Вильнюс","Одесса",4,queue);
//        if(queue.isEmpty()) System.out.println("No path");
//        while(!queue.isEmpty()) System.out.println(queue.remove());
//        graph.DLS_loop("Вильнюс","Одесса");
        //graph.BDS("Вильнюс","Одесса");
        //graph.GDFS("Одесса","Одесса");
        //graph.A_star("Вильнюс","Одесса");
//        graph.heuristics("Одесса");
//        for(String x: graph.h.keySet()){
//            System.out.println(graph.h.get(x));
//        }
    }
}