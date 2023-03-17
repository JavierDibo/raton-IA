package mouserun.mouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import mouserun.game.Mouse;
import mouserun.game.Grid;
import mouserun.game.Cheese;

/**
 * @author Javier Francisco Dibo Gomez
 * @author Cristian Ojeda del Moral
 */
public class M23C10extra extends Mouse {
    private final static int MAS_PRIORITARIO = 0;
    private final static int LIMITE = 200;
    private HashMap<Pair<Integer, Integer>, Grid> celdasVisitadas; // (Cordenadas, celda)
    private final HashMap<Pair<Integer, Integer>, Grid> celdasTotales;
    private Stack<Integer> pilaMovimientos;

    public M23C10extra() {
        super("JD-CO-bpl");
        celdasVisitadas = new HashMap<>();
        celdasTotales = new HashMap<>();
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
     * Se basa en las celdas vecinas de la celda actual y llama al metodo registrarCamino() para agregar los movimientos
     * posibles a la lista de movimientos pasada como parametro. Tiene en cuenta el limite de profundidad, si este se
     * alcanza se deja de tener en cuenta los posibles caminos, pues se consideran fuera de limites. Se toma una decision
     * a la hora de elegir ramas en el arbol, el raton toma los caminos en sentido horario comenzando por la izquierda.
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
     * Este metodo devuelve el siguiente movimiento del raton siguiente un orden. Concretamente, el raton toma las
     * direcciones Izquierda, Arriba, Derecha o Abajo, en ese orden, de ser posible. Este metodo sustituye la
     * funcionalidad previa que tomaba un movimiento aleatorio.
     *
     * @param celdaActual la celda actual en la que se encuentra el raton.
     */
    public int movimientoRigido(Grid celdaActual) {

        if (celdaActual.canGoLeft()) {
            return Mouse.LEFT;
        }
        if (celdaActual.canGoUp()) {
            return Mouse.UP;
        }
        if (celdaActual.canGoRight()) {
            return Mouse.RIGHT;
        }
        if (celdaActual.canGoDown()) {
            return Mouse.DOWN;
        }

        return 0;
    }

    /**
     * Este metodo decide el siguiente movimiento del raton en el laberinto. Si hay movimientos nuevos disponibles,
     * elige el mas prioritario entre ellos y lo agrega a una pila de movimientos. Si no hay movimientos nuevos disponibles y la pila de
     * movimientos no esta vacia, retrocede al ultimo movimiento en la pila. Si no hay movimientos nuevos disponibles y
     * la pila de movimientos esta vacia, elige un movimiento en un orden concreto.
     *
     * @param hayMovimientosNuevos un indicador booleano que indica si hay movimientos nuevos disponibles.
     * @param listaMovimientos     la lista de movimientos posibles a partir de la celda actual.
     * @return el siguiente movimiento del raton a realizar.
     */
    public int decidirMovimiento(boolean hayMovimientosNuevos, ArrayList<Integer> listaMovimientos, Grid celdaActual) {

        int movimientoFinal, ultimoMovimiento;

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
            movimientoFinal = movimientoRigido(celdaActual);
        }

        return movimientoFinal;
    }

    /**
     * Este metodo mueve el raton a la siguiente celda del laberinto. Primero registra la celda actual en un Map para
     * evitar visitas repetidas, para evitar problemas en entorno multiagente, se utiliza otro mapa temporal que
     * almacena los movimientos hasta que se pise una bomba. Luego genera una lista de movimientos posibles a partir de
     * la celda actual y elige un movimiento. Finalmente, devuelve el movimiento elegido.
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

        if (!celdasTotales.containsKey(coordenadas))
            celdasTotales.put(coordenadas, celdaActual);

        celdasVisitadas.put(coordenadas, celdaActual);

        movimientosPosibles(celdaActual, listaMovimientos);

        hayMovimientosNuevos = !listaMovimientos.isEmpty();

        movimientoFinal = decidirMovimiento(hayMovimientosNuevos, listaMovimientos, celdaActual);

        System.err.println("Pasos=" + (int) this.getSteps() + ", Casillas Exploradas=" + celdasTotales.size()
                + ", RatioExp=" + (float) celdasTotales.size() / 400 + ", CeldasAhora=" + celdasVisitadas.size());

        return movimientoFinal;
    }

    /**
     * Se ejecuta cuando se encuentra un nuevo queso. Como el queso puede ser encontrado por otro agente, se resetea
     * la informacion local para poder tener las misma oportunidades de encontrar el queso.
     */
    @Override
    public void newCheese() {
        celdasVisitadas = new HashMap<Pair<Integer, Integer>, Grid>();
        pilaMovimientos = new Stack<Integer>();
    }

    /**
     * Se ejecuta cuando el raton pisa una bomba. Como la bomba coloca al raton en un posicion aleatoria, el raton
     * elemina los datos obtenidos pues no puede trabajar con ellos por perder su referencia.
     */
    @Override
    public void respawned() {
        pilaMovimientos = new Stack<Integer>();
        celdasVisitadas = new HashMap<Pair<Integer, Integer>, Grid>();
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
