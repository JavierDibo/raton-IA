package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Stack;

import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

/**
 * @author Javier Francisco Dibo Gomez
 * @author Cristian Ojeda del Moral
 */
public class M23C10bpl extends Mouse {
    private final static int MAS_PRIORITARIO = 0;
    private final static int LIMITE = 200;
    private HashMap<Pair<Integer, Integer>, Grid> celdasVisitadas; // (Cordenadas, celda)
    private Stack<Integer> pilaMovimientos;

    public M23C10bpl() {
        super("JD-CO-bpl");
        celdasVisitadas = new HashMap<>();
        pilaMovimientos = new Stack<>();
        incExploredGrids();
    }

    /**
     * Este metodo registra el movimiento actual del raton en las coordenadas dadas en la lista de movimientos,
     * si es que estas coordenadas aun no han sido visitadas. Este metodo utiliza un objeto Pair para almacenar
     * las coordenadas en un par ordenado y un Map para realizar una busqueda eficiente de coordenadas visitadas.
     *
     * @param posX             la coordenada x del movimiento actual del raton.
     * @param posY             la coordenada y del movimiento actual del raton.
     * @param direccion        la direccion del movimiento actual del raton.
     * @param listaMovimientos la lista de movimientos en la que se registrara el movimiento actual si es que estas
     *                         coordenadas aun no han sido visitadas.
     */
    public void registrarCamino(int posX, int posY, int direccion, ArrayList<Integer> listaMovimientos) {
        Pair<Integer, Integer> coordeandas;
        coordeandas = new Pair<>(posX, posY);
        if (!celdasVisitadas.containsKey(coordeandas)) {
            listaMovimientos.add(direccion);
        }
    }

    /**
     * Este metodo calcula los movimientos posibles que el raton puede hacer en la celda actual en el laberinto.
     * Se basa en las celdas vecinas de la celda actual y agrega los movimientos posibles a la lista de movimientos
     * pasada como parametro.
     *
     * @param celdaActual      la celda actual en la que se encuentra el raton.
     * @param listaMovimientos la lista de movimientos posibles a la que se agregaran los movimientos calculados.
     */
    public void movimientosPosibles(Grid celdaActual, ArrayList<Integer> listaMovimientos) {

        int posX, posY;

        posX = celdaActual.getX();
        posY = celdaActual.getY();

        if (pilaMovimientos.size() >= LIMITE) {
            return;
        }
        if (celdaActual.canGoLeft()) {
            registrarCamino(posX - 1, posY, Mouse.LEFT, listaMovimientos);
        }
        if (celdaActual.canGoUp()) {
            registrarCamino(posX, posY + 1, Mouse.UP, listaMovimientos);
        }
        if (celdaActual.canGoRight()) {
            registrarCamino(posX + 1, posY, Mouse.RIGHT, listaMovimientos);
        }
        if (celdaActual.canGoDown()) {
            registrarCamino(posX, posY - 1, Mouse.DOWN, listaMovimientos);
        }
    }

    /**
     * Este metodo decide el siguiente movimiento del raton en el laberinto. Si hay movimientos nuevos disponibles,
     * elige el mas prioritario entre ellos y lo agrega a una pila de movimientos. Si no hay movimientos nuevos
     * disponibles y la pila de movimientos no esta vacia, retrocede al ultimo movimiento en la pila. Si no hay
     * movimientos nuevos disponibles y la pila de movimientos esta vacia, elige un movimiento al azar.
     *
     * @param hayMovimientosNuevos un indicador booleano que indica si hay movimientos nuevos disponibles.
     * @param listaMovimientos     la lista de movimientos posibles a partir de la celda actual.
     * @return el siguiente movimiento del raton a realizar.
     */
    public int decidirMovimiento(boolean hayMovimientosNuevos, ArrayList<Integer> listaMovimientos) {

        int movimientoFinal, ultimoMovimiento;
        Random aleatorio;

        aleatorio = new Random();
        movimientoFinal = 0;

        if (hayMovimientosNuevos) {
            movimientoFinal = listaMovimientos.get(MAS_PRIORITARIO);
            pilaMovimientos.push(movimientoFinal);
            incExploredGrids();
        } else if (!pilaMovimientos.isEmpty()) {
            ultimoMovimiento = pilaMovimientos.pop();
            switch (ultimoMovimiento) {
                case Mouse.UP -> movimientoFinal = Mouse.DOWN;
                case Mouse.DOWN -> movimientoFinal = Mouse.UP;
                case Mouse.RIGHT -> movimientoFinal = Mouse.LEFT;
                case Mouse.LEFT -> movimientoFinal = Mouse.RIGHT;
            }
        } else {
            movimientoFinal = aleatorio.nextInt(4) + 1;
        }

        return movimientoFinal;
    }

    /**
     * Este metodo mueve el raton a la siguiente celda del laberinto. Primero registra la celda actual en un Map para
     * evitar visitas repetidas, luego genera una lista de movimientos posibles a partir de la celda actual y elige un
     * movimiento. Finalmente, devuelve el movimiento elegido.
     *
     * @param celdaActual la celda actual en la que se encuentra el raton.
     * @param cheese      el queso que el raton esta buscando.
     * @return el siguiente movimiento del raton a realizar.
     */
    @Override
    public int move(Grid celdaActual, Cheese cheese) {

        ArrayList<Integer> listaMovimientos;
        int posX, posY, movimientoFinal;
        boolean hayMovimientosNuevos;
        Pair<Integer, Integer> coordenadas;

        listaMovimientos = new ArrayList<>();

        posX = celdaActual.getX();
        posY = celdaActual.getY();
        coordenadas = new Pair<>(posX, posY);

        celdasVisitadas.put(coordenadas, celdaActual);

        movimientosPosibles(celdaActual, listaMovimientos);

        hayMovimientosNuevos = !listaMovimientos.isEmpty();

        movimientoFinal = decidirMovimiento(hayMovimientosNuevos, listaMovimientos);

        return movimientoFinal;
    }

    /**
     * Este metodo se ejecuta cuando se encuentra un queso. Como el queso aparece aleatoriamente, se borra la memoria
     * del agente para otorgarle la posibilidad de comenzar nuevamente la busqueda de este. Cabe notar que el queso
     * puede estar fuera de limites, en cuyo caso el raton exploraria todo el laberinto dentro de sus limites sin
     * encontrarlo.
     */
    @Override
    public void newCheese() {
        celdasVisitadas = new HashMap<Pair<Integer, Integer>, Grid>();
        pilaMovimientos = new Stack<Integer>();
    }

    @Override
    public void respawned() {

    }

    // Pair class
    static class Pair<U, V> {

        public final U first;       // el primer campo de un par
        public final V second;      // el segundo campo de un par

        // Construye un nuevo par con valores especificados
        private Pair(U first, V second) {
            this.first = first;
            this.second = second;
        }

        @Override
        // Verifica que el objeto especificado sea "igual a" el objeto actual o no
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair<?, ?> pair = (Pair<?, ?>) o;

            // llamar al metodo `equals()` de los objetos subyacentes
            if (!first.equals(pair.first)) {
                return false;
            }
            return second.equals(pair.second);
        }

        @Override
        // Calcula el codigo hash de un objeto para admitir tablas hash
        public int hashCode() {
            // usa codigos hash de los objetos subyacentes
            return 31 * first.hashCode() + second.hashCode();
        }

        @Override
        public String toString() {
            return "(" + first + ", " + second + ")";
        }

    }

}
