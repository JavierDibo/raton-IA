package mouserun.mouse;

import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

public class RatonPrueba extends Mouse {

    long numPasos;
    long numCasillasUnicas;


    public RatonPrueba() {
        super("Ratoncito");
        numPasos = getSteps();
        numCasillasUnicas = getExploredGrids();
    }

    public int move(Grid currentGrid, Cheese cheese) {

        return 1;
    }

    public void newCheese() {

    }

    public void respawned() {

    }

}