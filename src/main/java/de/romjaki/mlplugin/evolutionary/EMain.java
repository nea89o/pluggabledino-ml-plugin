package de.romjaki.mlplugin.evolutionary;

public class EMain {
    static String target = "11111111111111110000010100001010100101010";

    public static void main(String[] args) {

        Population<Character> pop = new Population<Character>(69,
                random -> new Genetype<Character>(target.length(), () -> (char) random.nextInt(127)));

        pop.evaluate(EMain::fitness);
        while (fitness(pop.getFittest()) - target.length() < -0.001f) {
            System.out.println(show(pop.getFittest()));
            System.out.println(fitness(pop.getFittest()));
            pop = pop.decimate(character -> character == '1' ? '0' : '1');
            pop.evaluate(EMain::fitness);
        }


    }

    private static String show(Genetype<Character> fittest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < target.length(); i++) {
            sb.append(fittest.getGene(i));
        }
        return sb.toString();
    }

    private static float fitness(Genetype<Character> genetype) {
        int score = 0;
        for (int i = 0; i < target.length(); i++) {
            if (genetype.getGene(i) == target.charAt(i))
                score++;
        }
        return (float) score;
    }
}
